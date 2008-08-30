/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ImageProcessor.ProcessingEngine.CommandEnginePlugins.Batik.Instructions;

import ImageProcessor.ProcessingEngine.*;
import ImageProcessor.ProcessingEngine.CommandEnginePlugins.Batik.*;
import ImageProcessor.ProcessingEngine.CommandEnginePlugins.Batik.CommandEngine.*;
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
				workspace.log("Processing of command skipped because it does not have a buffer attribute: " + instructionNode.getNodeName());
				return (false);  // this should throw an exception instead
			}
		} else {
			bufferName = bufferNode.getNodeValue();
			if (bufferName == null || bufferName.length() == 0) {
				if (!workspace.requestInfo.continueOnError) {
					workspace.log("Processing halted because the command does not have a value for the buffer attribute: " + instructionNode.getNodeName());
					return (false);  // this should throw an exception instead
				} else {
					workspace.log("Processing of command skipped because it does not have a value for the buffer attribute: " + instructionNode.getNodeName());
					return (false);  // this should throw an exception instead
				}
			}
		}

		ProcessingEngineImageBuffer iBuffer = workspace.getImageBuffer(bufferName);
		if (iBuffer == null) {
			workspace.log("There is no buffer named '" + bufferName + "' to do a substituteVariables on.");
			return (false);
		}

		if ((!iBuffer.mimeType.equals(CommandEngine.MIME_BUFFERTYPE)) && (!iBuffer.mimeType.equals(CommandEngine.INTERNAL_BUFFERTYPE))){
			workspace.log("Buffer is not of type '" + CommandEngine.MIME_BUFFERTYPE + "' or '" + CommandEngine.INTERNAL_BUFFERTYPE + "' and no conversion is available for substituteVariables: " + bufferName);
			return (false);
		}

		if (workspace.requestInfo.variables == null || workspace.requestInfo.variables.length == 0) {
			workspace.log("This request has no variables associated with it, substituteVariables does not need to proceed: " + bufferName);
			return (true);
		}

		Document doc = null;
		if(iBuffer.mimeType.equals(CommandEngine.MIME_BUFFERTYPE)) {
			String uri = "data://" + CommandEngine.MIME_BUFFERTYPE + ";base64,";
			uri += ImageProcessor.ProcessingEngine.Base64.encodeBytes((byte[]) iBuffer.data);

			try {
				String parser = XMLResourceDescriptor.getXMLParserClassName();
				SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
				//doc = f.createDocument(uri, new java.io.ByteArrayInputStream((byte[])iBuffer.data));
				doc = f.createDocument(uri);
			} catch (IOException ex) {
				workspace.log("An error occurred parsing the SVG file data in the substituteVariables command: " + ex.getMessage());
				return (false);
			}
		} else {
			doc = (Document)iBuffer.data;
		}
		int i = 0;
		int ii = 0;
		int iii = 0;
		int varStart;
		int varEnd;
		NodeList elements = null;
		Node textNode = null;
		Node textStringNode = null;
		String val = null;
		String lines[] = null;
		String oStr = null;

		elements = doc.getElementsByTagName("*");
		int eSize = elements.getLength();
		for (i = 0; i < eSize; i++) {
			textNode = elements.item(i);
			if (textNode != null) {
				if((textNode.getNodeType()==textNode.ELEMENT_NODE)) {
					textStringNode = textNode.getFirstChild();
					while(textStringNode!=null) {
						if(textStringNode.getNodeType()==textStringNode.TEXT_NODE) {
							// check to see if its worth parsing this string
							oStr = textStringNode.getNodeValue();
							if(!oStr.replace('\t', ' ').replace('\n', ' ').replace('\r', ' ').trim().isEmpty()) {
								for (ii = 0; ii < workspace.requestInfo.variables.length; ii++) {
									// get this each time around to make sure we see the changes made from
									// previous passes
									oStr = textStringNode.getNodeValue();
									varStart = oStr.indexOf("{" + workspace.requestInfo.variables[ii].name + "}");
									if(varStart == -1) {
										// the variable we're working with does not appear in this text,
										// go to the next variable
										continue;
									}

									val = workspace.requestInfo.variables[ii].data;
									lines = val.split(java.util.regex.Pattern.quote("\n") + "|" + java.util.regex.Pattern.quote("\r") + "|" + java.util.regex.Pattern.quote("\r\n"));
									//textStringNode.setNodeValue(oStr.replaceAll(java.util.regex.Pattern.quote("{" + workspace.requestInfo.variables[ii].name + "}"), java.util.regex.Matcher.quoteReplacement(workspace.requestInfo.variables[ii].data)));
									if (lines.length == 1) {
										textStringNode.setNodeValue(oStr.replaceAll(java.util.regex.Pattern.quote("{" + workspace.requestInfo.variables[ii].name + "}"), java.util.regex.Matcher.quoteReplacement(workspace.requestInfo.variables[ii].data)));
									} else if (lines.length > 1) {
										String prefix = null;
										String suffix = null;
										if(varStart>0) {
											prefix = oStr.substring(0, varStart);
										}
										varEnd = varStart + workspace.requestInfo.variables[ii].name.length() + 2;
										if(varEnd < oStr.length()) {
											suffix = oStr.substring(varEnd);
										}

											if(textNode.getNodeName().equals("flowPara")) {
											if(prefix!=null && !prefix.isEmpty()) {
												textNode.appendChild(((org.apache.batik.dom.svg12.SVG12OMDocument)doc).createTextNode(prefix));
											}
											
											for(iii = 0; iii < lines.length; iii++) {
												if(iii > 0) {
													// add flowLine first, except on the first pass, this
													// allows for the most natural feel in the wrapping
													textNode.appendChild(((org.apache.batik.dom.svg12.SVG12OMDocument)doc).createElement("flowLine"));
												}
												textNode.appendChild(((org.apache.batik.dom.svg12.SVG12OMDocument)doc).createTextNode(lines[iii]));
											}

											
											if(suffix!=null && !suffix.isEmpty()) {
												textNode.appendChild(((org.apache.batik.dom.svg12.SVG12OMDocument)doc).createTextNode(suffix));
											}
											textNode.removeChild(textStringNode);
										} else {
											// we don't know how to handle multiple lines specially in this element, just replace what we have
											textStringNode.setNodeValue(oStr.replaceAll(java.util.regex.Pattern.quote("{" + workspace.requestInfo.variables[ii].name + "}"), java.util.regex.Matcher.quoteReplacement(workspace.requestInfo.variables[ii].data)));
										}
									}
								}
							}
						}
						textStringNode = textStringNode.getNextSibling();
					}
				}
			}
		}

		if(iBuffer.mimeType.equals(CommandEngine.MIME_BUFFERTYPE)) {
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
		} else {
			iBuffer.data = doc;
		}
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
				workspace.log("Processing of command skipped because it does not have a buffer attribute: " + instructionNode.getNodeName());
				return (false);  // this should throw an exception instead
			}
		} else {
			bufferName = bufferNode.getNodeValue();
			if (bufferName == null || bufferName.length() == 0) {
				if (!workspace.requestInfo.continueOnError) {
					workspace.log("Processing halted because the command does not have a value for the buffer attribute: " + instructionNode.getNodeName());
					return (false);  // this should throw an exception instead
				} else {
					workspace.log("Processing of command skipped because it does not have a value for the buffer attribute: " + instructionNode.getNodeName());
					return (false);  // this should throw an exception instead
				}
			}
		}

		ProcessingEngineImageBuffer iBuffer = workspace.getImageBuffer(bufferName);
		if (iBuffer == null) {
			workspace.log("There is no buffer named '" + bufferName + "' to do a substitution on.");
			return (false);
		}
		if (!iBuffer.mimeType.equals(CommandEngine.MIME_BUFFERTYPE)) {
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
