/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rtsz.dgs4j.ProcessingEngine.CommandEnginePlugins.Batik.Instructions;

import com.rtsz.dgs4j.ProcessingEngine.*;
import com.rtsz.dgs4j.ProcessingEngine.CommandEnginePlugins.Batik.*;
import com.rtsz.dgs4j.ProcessingEngine.CommandEnginePlugins.Batik.CommandEngine.*;
import com.rtsz.dgs4j.ProcessingEngine.Instructions.*;

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
public class replaceImage implements IInstruction {

	String xlinkNS = "http://www.w3.org/1999/xlink";

	public boolean process(ProcessingWorkspace workspace, Node instructionNode) {
		NamedNodeMap nm = instructionNode.getAttributes();
		Node bufferNode = nm.getNamedItem("buffer");
		Node srcImageNode = nm.getNamedItem("srcImage");
		Node imageElementIdNode = nm.getNamedItem("imageElementId");
		if (bufferNode == null) {
			workspace.log("replaceImage command failed: no buffer attribute specified.");
			return (false);
		}
		if (srcImageNode == null) {
			workspace.log("replaceImage command failed: no srcImageNode attribute specified.");
			return (false);
		}
		if (imageElementIdNode == null) {
			workspace.log("replaceImage command failed: no imageElementId attribute specified.");
			return (false);
		}
		String bufferName = bufferNode.getNodeValue();
		String srcImageName = srcImageNode.getNodeValue();
		String imageElementId = imageElementIdNode.getNodeValue();
		if (bufferName.length() == 0) {
			workspace.log("replaceImage command failed: buffer attribute has no data.");
			return (false);
		}
		if (srcImageName.length() == 0) {
			workspace.log("replaceImage command failed: srcImageNode attribute has no data.");
			return (false);
		}
		if (imageElementId.length() == 0) {
			workspace.log("replaceImage command failed: imageElementId attribute has no data.");
			return (false);
		}

		ProcessingEngineImageBuffer iBuffer = workspace.getImageBuffer(bufferName);
		if (iBuffer == null) {
			workspace.log("There is no buffer named '" + bufferName + "' to do a replaceImage on.");
			return (false);
		}
		if ((!iBuffer.mimeType.equals(CommandEngine.MIME_BUFFERTYPE)) && (!iBuffer.mimeType.equals(CommandEngine.INTERNAL_BUFFERTYPE))) {
			workspace.log("Buffer is not of type '" + CommandEngine.MIME_BUFFERTYPE + "' or '" + CommandEngine.INTERNAL_BUFFERTYPE + "' and no conversion is available for replaceImage: " + bufferName);
			return (false);
		}

		ProcessingEngineImageBuffer imgBuffer = workspace.getImageBuffer(srcImageName);
		if (imgBuffer == null) {
			workspace.log("There is no buffer named '" + srcImageName + "' to get the new image from for a replaceImage.");
			return (false);
		}
		if (!imgBuffer.mimeType.equals(CommandEngine.INTERNAL_BUFFERTYPE) && !imgBuffer.mimeType.equals(CommandEngine.MIME_BUFFERTYPE) && !imgBuffer.mimeType.equals("image/png") && !imgBuffer.mimeType.equals("image/gif") && !imgBuffer.mimeType.equals("image/jpeg") && !imgBuffer.mimeType.equals("image/tiff")) {
			workspace.log("New image for replaceImage is not an acceptable type, use png, gif, jpeg, svg, or tiff instead.  Buffer Name: " + srcImageName + " Image MIME Type: " + imgBuffer.mimeType);
			return (false);
		}

		Document doc = null;

		if (iBuffer.mimeType.equals(CommandEngine.MIME_BUFFERTYPE)) {
			String uri = "data://" + CommandEngine.MIME_BUFFERTYPE + ";base64,";
			uri += Base64.encodeBytes((byte[]) iBuffer.data);

			try {
				String parser = XMLResourceDescriptor.getXMLParserClassName();
				SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
				//doc = f.createDocument(uri, new java.io.ByteArrayInputStream((byte[])iBuffer.data));
				doc = f.createDocument(uri);
			} catch (IOException ex) {
				workspace.log("An error occurred parsing the SVG file data: " + ex.getMessage());
				return (false);
			}
		} else {
			doc = (Document) iBuffer.data;
		}

		Element element = doc.getElementById(imageElementId);
		if (element != null) {
			if (element.getNodeName().equals("image")) {
				String dataUri = "";
				if (imgBuffer.mimeType.equals(CommandEngine.INTERNAL_BUFFERTYPE)) {
					Document imgDoc = (Document) imgBuffer.data;
					dataUri = "data://" + CommandEngine.MIME_BUFFERTYPE + ";base64,";
					TransformerFactory tf = TransformerFactory.newInstance();
					ByteArrayOutputStream outStream = new ByteArrayOutputStream();
					Transformer t = null;
					try {
						t = tf.newTransformer();
						t.transform(new DOMSource(imgDoc), new StreamResult(outStream));
					} catch (Exception ex) {
						workspace.log("An error occurred while reconstructing the XML file after replaceImage call: " + ex.getMessage());
						return (false);
					}
					dataUri += Base64.encodeBytes(outStream.toByteArray());
				} else {
					dataUri = "data://" + imgBuffer.mimeType.trim() + ";base64,";
					dataUri += Base64.encodeBytes((byte[]) imgBuffer.data);
				}
				element.setAttributeNS(xlinkNS, "xlink:href", dataUri);
			} else {
				workspace.log("The element with an id of " + imageElementId + " is not an image.");
				return (false);
			}
		}

		if (iBuffer.mimeType.equals(CommandEngine.MIME_BUFFERTYPE)) {
			TransformerFactory tf = TransformerFactory.newInstance();
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			Transformer t = null;
			try {
				t = tf.newTransformer();
				t.transform(new DOMSource(doc), new StreamResult(outStream));
			} catch (Exception ex) {
				workspace.log("An error occurred while reconstructing the XML file after replaceImage call: " + ex.getMessage());
				return (false);
			}
			iBuffer.data = outStream.toByteArray();
		} else {
			iBuffer.data = doc;
		}
		return (true);
	}
}
