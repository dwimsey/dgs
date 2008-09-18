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
 * @author dwimsey
 */
public class addWatermark implements ImageProcessor.ProcessingEngine.Instructions.IInstruction {
	String instructionName = "addWatermark";
	String xlinkNS = "http://www.w3.org/1999/xlink";

	public boolean process(ImageProcessor.ProcessingEngine.ProcessingWorkspace workspace, Node instructionNode) {
		NamedNodeMap nm = instructionNode.getAttributes();
		Node bufferNode = nm.getNamedItem("buffer");
		Node srcImageNode = nm.getNamedItem("srcImage");
		if (bufferNode == null) {
		    workspace.log(instructionName + " command failed: no buffer attribute specified.");
		    return (false);
		}
		if (srcImageNode == null) {
		    workspace.log(instructionName + " command failed: no srcImageNode attribute specified.");
		    return (false);
		}

		String bufferName = bufferNode.getNodeValue();
		String srcImageName = srcImageNode.getNodeValue();
		if (bufferName.length() == 0) {
		    workspace.log(instructionName + " command failed: buffer attribute has no data.");
		    return (false);
		}
		if (srcImageName.length() == 0) {
		    workspace.log(instructionName + " command failed: srcImageNode attribute has no data.");
		    return (false);
		}

		ProcessingEngineImageBuffer iBuffer = workspace.getImageBuffer(bufferName);
		if (iBuffer == null) {
			workspace.log(instructionName + " command failed: There is no buffer named '" + bufferName + "'.");
			return (false);
		}
		if ((!iBuffer.mimeType.equals(CommandEngine.MIME_BUFFERTYPE)) && (!iBuffer.mimeType.equals(CommandEngine.INTERNAL_BUFFERTYPE))) {
			workspace.log(instructionName + " command failed: Buffer is not of type '" + CommandEngine.MIME_BUFFERTYPE + "' or '" + CommandEngine.INTERNAL_BUFFERTYPE + "' and no conversion is available: " + bufferName);
			return (false);
		}

		ProcessingEngineImageBuffer imgBuffer = workspace.getImageBuffer(srcImageName);
		if (imgBuffer == null) {
			workspace.log(instructionName + " command failed: There is no buffer named '" + srcImageName + "' to get the watermark image from.");
			return (false);
		}
		if (!imgBuffer.mimeType.equals("image/png") && !imgBuffer.mimeType.equals("image/jpeg") && !imgBuffer.mimeType.equals("image/tiff")) {
			return (false);
		}
		return (false);
	}
}
