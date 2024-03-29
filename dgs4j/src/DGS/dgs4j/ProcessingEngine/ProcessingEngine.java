/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DGS.dgs4j.ProcessingEngine;

import DGS.dgs4j.ProcessingEngine.Instructions.IInstruction;
import DGS.dgs4j.DGSResponseInfo;

import java.io.*;
import java.util.*;

import org.apache.xerces.parsers.*;
import org.xml.sax.*;
import org.w3c.dom.*;

/**
 *
 * @author dwimsey
 */

/*
 * At this point in time, ProcessingEngine expects a single private
 * ProcessingEngine object to be created during the life of the application,
 * creating a second one (even after destroying the first) will cause problems
 */
public class ProcessingEngine {

	private Map<String, ProcessingCommand> commandList;
	private ArrayList<ICommandEngine> commandEngines;
	private boolean pluginsLoaded;
	private boolean enableSecurity;

	public ProcessingEngine() {
		commandList = new HashMap<String, ProcessingCommand>();
		commandEngines = new ArrayList<ICommandEngine>();
		pluginsLoaded = false;
		enableSecurity = false;
		loadCommandPlugins();
	}

	public DGSResponseInfo processCommandString(ProcessingWorkspace workspace) {
		java.util.Date startTime = new java.util.Date();
		ProcessingWorkspace.setCurrentWorkspace(workspace);
		try {
			DOMParser xParser = new DOMParser();
			try {
				xParser.parse(new InputSource(new StringReader(workspace.requestInfo.instructionsXML)));
			} catch (Exception ex) {
				workspace.logFatal("An error occured parsing the XML command string: " + ex.getMessage());
				return (workspace.generateResultInfo());
			}
			Document doc = xParser.getDocument();
			Element rootNode = doc.getDocumentElement();
			if (rootNode == null) {
				workspace.logFatal("XML document has no <commands> root element.");
				return (workspace.generateResultInfo());
			}

			String rootName = rootNode.getNodeName().intern();
			if (!rootName.equals("commands")) {
				workspace.logFatal("XML command string does not contain the <commands> root element.  Root element received was: " + rootName);
				return (workspace.generateResultInfo());
			}

			NodeList commands = rootNode.getChildNodes();
			if (commands == null) {
				workspace.logFatal("XML command string does not contain any command elements.");
				return (workspace.generateResultInfo());
			}

			Node curNode;
			ProcessingCommand cmd;
			String curNodeName;

			ICommandEngine cEngine;
			NamedNodeMap attributes;
			for (int i = 0; i < commands.getLength(); i++) {
				curNode = commands.item(i);
				curNodeName = curNode.getNodeName().intern();
				if (curNode.getNodeType() != curNode.ELEMENT_NODE) {
					// we only process ELEMENT nodes
					continue;
				}

				attributes = curNode.getAttributes();
				if (attributes == null) {
					if (!workspace.requestInfo.continueOnError) {
						workspace.logFatal("Processing halted because the command does not have any attributes: " + curNodeName);
						break;
					} else {
						workspace.logError	("Processing of command skipped because it does not have any attributes: " + curNodeName);
						continue;
					}
				}

				if (curNodeName.equals("load") || curNodeName.equals("save")) {
					Node bufferNode = attributes.getNamedItem("buffer");
					String bufferName;

					if (bufferNode == null) {
						if (!workspace.requestInfo.continueOnError) {
							workspace.logFatal("Processing halted because the command does not have a buffer attribute: " + curNodeName);
							break;
						} else {
							workspace.logError("Processing of command skipped because it does not have a buffer attribute: " + curNodeName);
							continue;
						}
					} else {
						bufferName = bufferNode.getNodeValue();
						if (bufferName == null || bufferName.length() == 0) {
							if (!workspace.requestInfo.continueOnError) {
								workspace.logFatal("Processing halted because the command does not have a value for the buffer attribute: " + curNodeName);
								break;
							} else {
								workspace.logError("Processing of command skipped because it does not have a value for the buffer attribute: " + curNodeName);
								continue;
							}
						}
					}

					Node fileNameNode = attributes.getNamedItem("filename");
					String fileName;
					if (fileNameNode == null) {
						if (!workspace.requestInfo.continueOnError) {
							workspace.logFatal("Processing halted because the command does not have a filename attribute: " + curNodeName);
							break;
						} else {
							workspace.logError("Processing of command skipped because it does not have a filename attributes: " + curNodeName);
							continue;
						}
					} else {
						fileName = fileNameNode.getNodeValue();
						if (fileName == null || fileName.length() == 0) {
							if (!workspace.requestInfo.continueOnError) {
								workspace.logFatal("Processing halted because the command does not have a value for the filename attribute: " + curNodeName);
								break;
							} else {
								workspace.logFatal("Processing of command skipped because it does not have a value for the filename attributes: " + curNodeName);
								continue;
							}
						}
					}

					String mimeType = "";
					Node mimeTypeNode = attributes.getNamedItem("mimeType");
					if (curNodeName.equals("save")) {
						mimeType = "";
						if (mimeTypeNode == null) {
							if (!workspace.requestInfo.continueOnError) {
								workspace.logFatal("Processing halted because the command does not have a mimeType attribute: " + curNodeName);
								break;
							} else {
								workspace.logError("Processing of command skipped because it does not have a mimeType attributes: " + curNodeName);
								continue;
							}
						} else {
							mimeType = mimeTypeNode.getNodeValue();
							if (mimeType == null || mimeType.length() == 0) {
								if (!workspace.requestInfo.continueOnError) {
									workspace.logFatal("Processing halted because the command does not have a value for the mimeType attribute: " + curNodeName);
									break;
								} else {
									workspace.logError("Processing of command skipped because it does not have a value for the mimeType attributes: " + curNodeName);
									continue;
								}
							}
						}
					} else {
						mimeType = "guess";
						if (mimeTypeNode != null) {
							mimeType = mimeTypeNode.getNodeValue();
							if (mimeType == null || mimeType.length() == 0) {
								mimeType = "guess";
							}
						}
					}
					boolean cmdHandled = false;
					for (int ii = 0; ii < commandEngines.size(); ii++) {
						cEngine = commandEngines.get(ii);
						if (curNodeName.equals("save")) {
							ProcessingEngineImageBuffer iBuffer = workspace.getImageBuffer(bufferName);
							if (iBuffer == null) {
								if (!workspace.requestInfo.continueOnError) {
									workspace.logFatal("Processing halted because the command does not refrence an existing buffer: Command: " + curNodeName + " Buffer: " + bufferName);
									break;
								} else {
									workspace.logError("Processing of command skipped because the command does not refrence an existing buffer: Command: " + curNodeName + " Buffer: " + bufferName);
									break;
								}
							}
							switchToPluginSecurity();
							try {
								if (cEngine.save(workspace, fileName, iBuffer, mimeType, attributes)) {
									cmdHandled = true;
									break;
								}
							} catch (Exception ex) {
								workspace.logFatal("Processing of command caused an internal error: Command: " + curNodeName + " Buffer: " + bufferName + " Error: " + ex.getMessage());
							} finally {
								switchToStandardSecurity();
							}
						} else {
							switchToPluginSecurity();
							try {
								if (cEngine.load(workspace, fileName, bufferName, mimeType, attributes)) {
									cmdHandled = true;
									break;
								}
							} catch (Exception ex) {
								workspace.logFatal("Processing of command caused an internal error: Command: " + curNodeName + " Buffer: " + bufferName + " Error: " + ex.getMessage());
							} finally {
								switchToStandardSecurity();
							}
						}
					}

					if (!cmdHandled) {
						if (!workspace.requestInfo.continueOnError) {
							workspace.logFatal("Processing skipped because the command does not have a handler that is willing to respond to the specified buffer and/or mimeType: Command: " + curNodeName + " Buffer: " + bufferName + " MIME Type: " + mimeType);
							break;
						} else {
							workspace.logError("Processing halted because the command does not have a handler that is willing to respond to the specified buffer and/or mimeType: Command: " + curNodeName + " Buffer: " + bufferName + " MIME Type: " + mimeType);
							continue;
						}
					}
				} else {
					cmd = this.commandList.get(curNodeName);
					if (cmd != null) {
						workspace.logDebug("Processing command: " + curNodeName);
						switchToPluginSecurity();
						boolean rval = false;
						try {
							rval = cmd.process(workspace, curNode);
						} catch (Exception ex) {
							workspace.logFatal("Processing of command caused an internal error: Command: " + curNodeName + " Error: " + ex.getMessage());
						} finally {
							switchToStandardSecurity();
						}
						if (!rval) {
							if (!workspace.requestInfo.continueOnError) {
								workspace.logFatal("Processing halted due to an error: " + curNodeName);
								break;
							} else {
								workspace.logError("Processing of command skipped because of an error: " + curNodeName);
								continue;
							}
						}
					} else {
						if (!workspace.requestInfo.continueOnError) {
							workspace.logFatal("Processing halted due to a missing command error: " + curNodeName);
							break;
						} else {
							workspace.logError("Processing of command skipped because it does not exist: " + curNodeName);
							continue;
						}
					}
				}
			}
			workspace.logInfo("Processing completed in: " + Float.toString(((new java.util.Date().getTime() + 1) - startTime.getTime()) / 1000.0f) + " seconds");
			return (workspace.generateResultInfo());
		} finally {
			ProcessingWorkspace.setCurrentWorkspace(null);
		}
	}

	private synchronized void switchToPluginSecurity() {
		if (!enableSecurity) {
			return;
		}
	}

	private synchronized void switchToStandardSecurity() {
		if (!enableSecurity) {
			return;
		}
	}

	private void loadPlugin(ICommandEngine cEngine) {
		switchToPluginSecurity();
		cEngine.init();
		cEngine.addCommands(this);
		switchToStandardSecurity();
		commandEngines.add(cEngine);
	}

	private void loadCommandPlugins() {
		synchronized (this) {
			if (pluginsLoaded == true) {
				return;
			} else {
				pluginsLoaded = true;
			}

			ICommandEngine pluginEngine;

			switchToPluginSecurity();
			pluginEngine = new DGS.dgs4j.ProcessingEngine.CommandEnginePlugins.Batik.CommandEngine();
			switchToStandardSecurity();

			loadPlugin(pluginEngine);
		}
	}

	public void addCommandInstruction(String commandName, IInstruction instruction) {
		boolean newCmd = false;
		ProcessingCommand cmd;
		commandName = commandName.intern();
		if (commandList.containsKey(commandName)) {
			cmd = commandList.get(commandName);
			newCmd = false;
		} else {
			cmd = new ProcessingCommand(commandName);
			newCmd = true;
		}
		try {
			cmd.addInstruction(instruction);
			if (newCmd) {
				commandList.put(commandName, cmd);
			}
		} catch (Exception ex) {
			// we should report this exception somehow!
		}
	}
}
