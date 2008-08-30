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
public class setVisibility implements ImageProcessor.ProcessingEngine.Instructions.IInstruction {

	public boolean process(ImageProcessor.ProcessingEngine.ProcessingWorkspace workspace, Node instructionNode) {
		NamedNodeMap attributes = instructionNode.getAttributes();
		String bufferName = null;
		String idValueStr = null;
		String nameValueStr = null;
		String objectVisible = "inherit";

		Node bufferNode = attributes.getNamedItem("buffer");
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

		Node targetNameNode = attributes.getNamedItem("targetId");
		if (targetNameNode != null) {
			idValueStr = targetNameNode.getNodeValue();
		}

		targetNameNode = attributes.getNamedItem("targetName");
		if (targetNameNode != null) {
			nameValueStr = targetNameNode.getNodeValue();
		}

		// make the strings null if they are zero length so we avoid checking both ways later
		if((idValueStr!=null) && (idValueStr.length() == 0)) {
			idValueStr = null;
		}
		if((nameValueStr!=null) && (nameValueStr.length() == 0)) {
			nameValueStr = null;
		}

		if((idValueStr==null) && (nameValueStr==null)) {
			workspace.log("Processing halted because the command does not have a target name or id attribute: " + instructionNode.getNodeName());
			return (false);  // this should throw an exception instead
		}

		targetNameNode = attributes.getNamedItem("visibility");
		if (targetNameNode == null) {
			if (!workspace.requestInfo.continueOnError) {
				workspace.log("Processing halted because the command does not have a visibility attribute: " + instructionNode.getNodeName());
				return (false);  // this should throw an exception instead
			} else {
				workspace.log("Processing of command skipped because it does not have a visibility attribute: " + instructionNode.getNodeName());
				return (false);
			}
		} else {
			objectVisible = targetNameNode.getNodeValue();
			if (objectVisible == null || objectVisible.length() == 0) {
				if (!workspace.requestInfo.continueOnError) {
					workspace.log("Processing halted because the command does not have a value for the visibility attribute: " + instructionNode.getNodeName());
					return (false);  // this should throw an exception instead
				} else {
					workspace.log("Processing of command skipped because it does not have a value for the visibility attribute: " + instructionNode.getNodeName());
					return (false);
				}
			}
			if(!objectVisible.equals("visible") && !objectVisible.equals("inherit") && !objectVisible.equals("hidden")) {
				workspace.log("Processing of command skipped because it does not have a valid value for the visible attribute: " + instructionNode.getNodeName() + "  Value: " + objectVisible);
				return (false);
			}
		}

		ProcessingEngineImageBuffer iBuffer = workspace.getImageBuffer(bufferName);
		if (iBuffer == null) {
			workspace.log("There is no buffer named '" + bufferName + "' to do a setVisibility on.");
			return (false);
		}
		if ((!iBuffer.mimeType.equals(CommandEngine.MIME_BUFFERTYPE)) && (!iBuffer.mimeType.equals(CommandEngine.INTERNAL_BUFFERTYPE))) {
			workspace.log("Buffer is not of type '" + CommandEngine.MIME_BUFFERTYPE + "' or '" + CommandEngine.INTERNAL_BUFFERTYPE + "' and no conversion is available for setVisibility: " + bufferName);
			return (false);
		}
		org.apache.batik.dom.svg.SVGOMDocument doc = null;

		if(iBuffer.mimeType.equals(CommandEngine.MIME_BUFFERTYPE)) {
			String uri = "data://" + CommandEngine.MIME_BUFFERTYPE + ";base64,";
			uri += ImageProcessor.ProcessingEngine.Base64.encodeBytes((byte[]) iBuffer.data);
			try {
				String parser = XMLResourceDescriptor.getXMLParserClassName();
				SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
				doc = (org.apache.batik.dom.svg.SVGOMDocument)f.createDocument(uri);
			} catch (IOException ex) {
				workspace.log("An error occurred parsing the SVG file data in the setVisibility command: " + ex.getMessage());
				return (false);
			}
		} else {
			doc = (org.apache.batik.dom.svg.SVGOMDocument)iBuffer.data;
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
		String oStr = null;
		boolean nodeMatchs = false;
		// we're checking every element to see if we should setVisibility or not
		elements = doc.getElementsByTagName("*");
		for (i = 0; i < elements.getLength(); i++) {
			textNode = elements.item(i);
			if (textNode != null) {
				if (textNode.hasAttributes()) {
					attribs = textNode.getAttributes();
					nodeMatchs = false;
					if(idValueStr!=null) {
						nodeIdNode = attribs.getNamedItem("id");
						if (nodeIdNode != null) {
							nodeId = nodeIdNode.getNodeValue();
							if(nodeId==null) {
								nodeId = "";
							}
							if(nodeId.equals(idValueStr)) {
								// this is a node we want to set, do it
								nodeMatchs = true;
							}
						}
					}
					if(!nodeMatchs) {
						if(nameValueStr!=null) {
							nodeIdNode = attribs.getNamedItem("name");
							if (nodeIdNode != null) {
								nodeId = nodeIdNode.getNodeValue();
								if(nodeId==null) {
									nodeId = "";
								}
								if(nodeId.equals(nameValueStr)) {
									// this is a node we want to set, do it
									nodeMatchs = true;
								}
							}
						}
					}

					if(nodeMatchs) {
						if(objectVisible.equals("hidden")) {
							// remove the entire named node and it WILL truely be hidden!
							textNode.getParentNode().removeChild(textNode);
						} else {
							Node nodeVisibilityNode = attribs.getNamedItem("visibility");
							if(nodeVisibilityNode == null) {
								// this item does not have a visibility attribute yet, create one and add it
								nodeVisibilityNode = doc.createAttribute("visibility");							
								attribs.setNamedItem(nodeVisibilityNode);
							}
							// since at this point we'll always have a nodeVisibilityNode, set it.
							nodeVisibilityNode.setNodeValue(objectVisible);
						}
					}
				}
			}
		}

		if(iBuffer.mimeType.equals(CommandEngine.MIME_BUFFERTYPE)) {
			// build the DOM back into an xml text file for storage in the buffer
			TransformerFactory tf = TransformerFactory.newInstance();
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			Transformer t = null;
			try {
				t = tf.newTransformer();
				t.transform(new DOMSource(doc), new StreamResult(outStream));
			} catch (Exception ex) {
				workspace.log("An error occurred while reconstructing the XML file after setVisibility call: " + ex.getMessage());
				return (false);
			}
			iBuffer.data = outStream.toByteArray();
		} else {
			iBuffer.data = doc;
		}
		return (true);
	}
}
