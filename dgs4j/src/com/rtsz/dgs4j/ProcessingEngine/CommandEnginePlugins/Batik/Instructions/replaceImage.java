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
public class replaceImage implements ImageProcessor.ProcessingEngine.Instructions.IInstruction {

	public boolean process(ImageProcessor.ProcessingEngine.ProcessingWorkspace workspace, Node instructionNode) {
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
		if (!iBuffer.mimeType.equals("image/svg+xml")) {
			return (false);
		}
		ProcessingEngineImageBuffer imgBuffer = workspace.getImageBuffer(srcImageName);
		if (imgBuffer == null) {
			workspace.log("There is no buffer named '" + srcImageName + "' to get the new image from for a replaceImage.");
			return (false);
		}
		if (!imgBuffer.mimeType.equals("image/png") && !imgBuffer.mimeType.equals("image/jpeg") && !imgBuffer.mimeType.equals("image/tiff")) {
			return (false);
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

		Element element = doc.getElementById(imageElementId);
		if (element != null) {
			if (element.getNodeName().equals("image")) {
				String dataUri = "";
				dataUri = "data://" + imgBuffer.mimeType + ";base64,";
				dataUri += ImageProcessor.ProcessingEngine.Base64.encodeBytes((byte[]) imgBuffer.data);
				element.setAttribute("xlink:href", dataUri);
			} else {
				workspace.log("The element with an id of " + imageElementId + " is not an image.");
				return (false);
			}
		}

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

		return (true);
	}
}
