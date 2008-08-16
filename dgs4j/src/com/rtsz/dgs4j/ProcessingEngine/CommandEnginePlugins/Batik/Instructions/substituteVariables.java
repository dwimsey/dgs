/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ImageProcessor.ProcessingEngine.CommandEnginePlugins.Batik.Instructions;

import ImageProcessor.ProcessingEngine.*;

import org.w3c.dom.*;

import java.io.*;

import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.util.XMLResourceDescriptor;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

/**
 *
 * @author dwimsey
 */
public class substituteVariables implements ImageProcessor.ProcessingEngine.Instructions.IInstruction {

	public boolean process(ImageProcessor.ProcessingEngine.ProcessingWorkspace workspace, Node instructionNode) {
		NamedNodeMap attributes = instructionNode.getAttributes();
		Node bufferNode = attributes.getNamedItem("buffer");
		String bufferName = null;

		if (bufferNode == null) {
			if (!workspace.requestInfo.continueOnError) {
				workspace.log("Processing halted because the command does not have a buffer attribute: " + instructionNode.getNodeName());
				return (false);  // this should throw an exception instead
			} else {
				workspace.log("Processing of command skipped because it does not have a buffer attributes: " + instructionNode.getNodeName());
				return (false);  // this should throw an exception instead
			}
		} else {
			bufferName = bufferNode.getNodeValue();
			if (bufferName == null || bufferName.length() == 0) {
				if (!workspace.requestInfo.continueOnError) {
					workspace.log("Processing halted because the command does not have a value for the buffer attribute: " + instructionNode.getNodeName());
					return (false);  // this should throw an exception instead
				} else {
					workspace.log("Processing of command skipped because it does not have a value for the buffer attributes: " + instructionNode.getNodeName());
					return (false);  // this should throw an exception instead
				}
			}
		}

		ProcessingEngineImageBuffer iBuffer = workspace.getImageBuffer(bufferName);
		if (iBuffer == null) {
			workspace.log("There is no buffer named '" + bufferName + "' to do a substitution on.");
			return (false);
		}
		if (!iBuffer.mimeType.equals("image/svg+xml")) {
			return (false);
		}

		if (workspace.requestInfo.variables == null || workspace.requestInfo.variables.length == 0) {
			return (true);
		}

		String uri = "data://image/svg+xml;base64,";
		uri += ImageProcessor.ProcessingEngine.Base64.encodeBytes((byte[]) iBuffer.data);
		Document doc = null;

		try {
			String parser = XMLResourceDescriptor.getXMLParserClassName();
			SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
			//doc = f.createDocument(uri, new java.io.ByteArrayInputStream((byte[])iBuffer.data));
			doc = f.createDocument(uri);
		} catch (IOException ex) {
			workspace.log("An error occurred parsing the SVG file data: " + ex.getMessage());
			return (false);
		}

		int i = 0;
		int ii = 0;
		int iii = 0;
		NodeList elements = null;
		Node textNode = null;
		NamedNodeMap attribs = null;
		String nodeId = null;
		Node nodeIdNode = null;
		Element newElement = null;
		String val = null;
		String lines[] = null;
		boolean replacedById = false;
		String oStr = null;

		elements = doc.getElementsByTagName("text");
		for (i = 0; i < elements.getLength(); i++) {
			textNode = elements.item(i);
			if (textNode != null) {
				replacedById = false;
				attribs = textNode.getAttributes();
				if (attribs != null && attribs.getLength() > 0) {
					nodeIdNode = attribs.getNamedItem("id");
					if (nodeIdNode != null) {
						nodeId = nodeIdNode.getNodeValue();
						for (ii = 0; ii < workspace.requestInfo.variables.length; ii++) {
							if (nodeId.equals(workspace.requestInfo.variables[ii].name)) {
								// matching text node which we need to replace the data in.
								replacedById = true;
								textNode.setTextContent("");
								val = workspace.requestInfo.variables[ii].data;
								lines = val.split(java.util.regex.Pattern.quote("\n") + "|" + java.util.regex.Pattern.quote("\r") + "|" + java.util.regex.Pattern.quote("\r\n"));
								if (lines != null) {
									for (iii = 0; iii < lines.length; iii++) {
										newElement = doc.createElement("tspan");
										newElement.setTextContent(lines[iii]);
										newElement.setAttribute("x", "0");
										if (iii > 0) {
											newElement.setAttribute("dy", "1em");
										}
										textNode.appendChild(newElement);
									}
								}
							}
						}
					}
				}
				if (!replacedById) {
					oStr = textNode.getTextContent();
					for (ii = 0; ii < workspace.requestInfo.variables.length; ii++) {
						val = workspace.requestInfo.variables[ii].data;
						lines = val.split(java.util.regex.Pattern.quote("\n") + "|" + java.util.regex.Pattern.quote("\r") + "|" + java.util.regex.Pattern.quote("\r\n"));
						if (lines.length == 1) {
							oStr = oStr.replaceAll(java.util.regex.Pattern.quote("{" + workspace.requestInfo.variables[ii].name + "}"), java.util.regex.Matcher.quoteReplacement(workspace.requestInfo.variables[ii].data));
						}
					}

					String rStr = "";
					for (ii = 0; ii < workspace.requestInfo.variables.length; ii++) {
						val = workspace.requestInfo.variables[ii].data;
						lines = val.split(java.util.regex.Pattern.quote("\n") + "|" + java.util.regex.Pattern.quote("\r") + "|" + java.util.regex.Pattern.quote("\r\n"));
						if (lines.length > 1) {
							for (iii = 0; iii < lines.length; iii++) {
								if (iii == 0) {
									rStr = "<tspan>" + lines[iii] + "</tspan>";
								} else {
									rStr += "<tspan x=\"0\" dy=\"1em\">" + lines[iii] + "</tspan>";
								}
							}
							oStr = oStr.replaceAll(java.util.regex.Pattern.quote("{" + workspace.requestInfo.variables[ii].name + "}"), java.util.regex.Matcher.quoteReplacement(rStr));
						}
					}
					textNode.setTextContent(oStr);
				}
			}
		}

		TransformerFactory tf = TransformerFactory.newInstance();
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		Transformer t = null;
		try {
			t = tf.newTransformer();
			t.transform(new DOMSource(doc), new StreamResult(outStream));
		} catch (Exception ex) {
			workspace.log("An error occurred while reconstructing the XML file after substituteVariables call: " + ex.getMessage());
			return (false);
		}
		iBuffer.data = outStream.toByteArray();
		return (true);
	}

	public boolean process2(ImageProcessor.ProcessingEngine.ProcessingWorkspace workspace, Node instructionNode) {
		NamedNodeMap attributes = instructionNode.getAttributes();
		Node bufferNode = attributes.getNamedItem("buffer");
		String bufferName = null;

		if (bufferNode == null) {
			if (!workspace.requestInfo.continueOnError) {
				workspace.log("Processing halted because the command does not have a buffer attribute: " + instructionNode.getNodeName());
				return (false);  // this should throw an exception instead
			} else {
				workspace.log("Processing of command skipped because it does not have a buffer attributes: " + instructionNode.getNodeName());
				return (false);  // this should throw an exception instead
			}
		} else {
			bufferName = bufferNode.getNodeValue();
			if (bufferName == null || bufferName.length() == 0) {
				if (!workspace.requestInfo.continueOnError) {
					workspace.log("Processing halted because the command does not have a value for the buffer attribute: " + instructionNode.getNodeName());
					return (false);  // this should throw an exception instead
				} else {
					workspace.log("Processing of command skipped because it does not have a value for the buffer attributes: " + instructionNode.getNodeName());
					return (false);  // this should throw an exception instead
				}
			}
		}

		ProcessingEngineImageBuffer iBuffer = workspace.getImageBuffer(bufferName);
		if (iBuffer == null) {
			workspace.log("There is no buffer named '" + bufferName + "' to do a substitution on.");
			return (false);
		}
		if (!iBuffer.mimeType.equals("image/svg+xml")) {
			return (false);
		}

		if (workspace.requestInfo.variables == null || workspace.requestInfo.variables.length == 0) {
			return (true);
		}

		String oStr = null;

		try {
			oStr = new String((byte[]) iBuffer.data, "UTF-8");
		} catch (UnsupportedEncodingException ex) {
			workspace.log("An unexpected encoding error has occurred: " + ex.getMessage());
			return (false);
		}
		for (int i = 0; i < workspace.requestInfo.variables.length; i++) {
			oStr = oStr.replaceAll(java.util.regex.Pattern.quote("{" + workspace.requestInfo.variables[i].name + "}"), java.util.regex.Matcher.quoteReplacement(workspace.requestInfo.variables[i].data));
		}
		oStr = oStr.replaceAll(java.util.regex.Pattern.quote("{{"), java.util.regex.Matcher.quoteReplacement("{"));
		try {
			iBuffer.data = oStr.getBytes("UTF-8");
		} catch (Exception ex) {
			workspace.log("An exception occurred during variable replacement while preparing the SVG result buffer: " + ex.getMessage());
			return (false);
		}
		return (true);
	}
}
