/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rtsz.dgs4j.ProcessingEngine.CommandEnginePlugins.Batik;

import org.w3c.dom.svg.*;
import org.w3c.dom.*;
import com.rtsz.dgs4j.*;
import com.rtsz.dgs4j.ProcessingEngine.*;
import com.rtsz.dgs4j.ProcessingEngine.CommandEnginePlugins.Batik.Instructions.*;

import java.io.IOException;
import java.io.ByteArrayOutputStream;

import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.util.XMLResourceDescriptor;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.apache.batik.transcoder.*;

// all these are required for GIF stuffs
import java.awt.image.BufferedImage;
import org.apache.batik.transcoder.keys.*;
import org.apache.batik.dom.svg.*;
import org.apache.batik.dom.svg12.*;
import org.apache.batik.dom.util.*;

/**
 *
 * @author dwimsey
 */
public class CommandEngine implements ICommandEngine {

	public static final TranscodingHints.Key KEY_ANIMATION_ENABLED = new BooleanKey();
	public static final String INTERNAL_BUFFERTYPE = "batik/svgdom";
	public static final String MIME_BUFFERTYPE = "image/svg+xml";
	public static final String COMMAND_ENGINE_ALLOWED_SCRIPT_TYPES = "text/javascript,application/javascript,application/ecmascript,text/ecmascript";
	public static DGSGIFRegistryEntry batikGIFRegistryEntry;
	@Override
	public synchronized void init() {
		if(batikGIFRegistryEntry == null) {
			// add our GIF support to batik first
			batikGIFRegistryEntry = new DGSGIFRegistryEntry();
			org.apache.batik.ext.awt.image.spi.ImageTagRegistry ir = org.apache.batik.ext.awt.image.spi.ImageTagRegistry.getRegistry();
//			java.util.List mTypes = ir.getRegisteredMimeTypes();
//			if(!mTypes.contains("image/gif")) {
				ir.register(batikGIFRegistryEntry);
//			}
//			if(!mTypes.contains("image/png")) {
				ir.register(new org.apache.batik.ext.awt.image.codec.png.PNGRegistryEntry());
//			}
//			if(!mTypes.contains("image/jpeg")) {
				ir.register(new org.apache.batik.ext.awt.image.codec.jpeg.JPEGRegistryEntry());
//			}
//			if(!mTypes.contains("image/tiff")) {
				ir.register(new org.apache.batik.ext.awt.image.codec.tiff.TIFFRegistryEntry());
//			}
		}
	}

	public void addCommands(ProcessingEngine pEngine) {
		pEngine.addCommandInstruction("replaceImage", new replaceImage());
		pEngine.addCommandInstruction("setVisibility", new setVisibility());
		pEngine.addCommandInstruction("substituteVariables", new substituteVariables());
		pEngine.addCommandInstruction("addWatermark", new addWatermark());
	}


	private String getAttributeValue(NamedNodeMap attributes, String aName) {
		Node cNode = attributes.getNamedItem(aName);
		if (cNode == null) {
			return (null);
		}
		return (cNode.getNodeValue());
	}

	public boolean load(ProcessingWorkspace workspace, String fileName, String bufferName, String mimeType, NamedNodeMap attributes) {
		DGSFileInfo dgsFile = null;
		for (int i = 0; i < workspace.requestInfo.files.length; i++) {
			if (workspace.requestInfo.files[i].name.equals(fileName)) {
				dgsFile = workspace.requestInfo.files[i];
				break;
			}
		}
		if (dgsFile == null) {
			workspace.log("Could not find file specified in request info file list: " + fileName);
			return (false);
		}

		ProcessingEngineImageBuffer buffer = null;
		if (dgsFile.mimeType.equals(MIME_BUFFERTYPE)) {
			SVGDocument doc = null;
			try {
				String parser = XMLResourceDescriptor.getXMLParserClassName();
				SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
				doc = f.createSVGDocument(null, new java.io.StringReader((String)new String(dgsFile.data, "UTF8")));
			} catch (IOException ex) {
				workspace.log("An error occurred parsing the SVG file data: " + ex.getMessage());
				return (false);
			}

			buffer = workspace.createImageBuffer(bufferName);
			SVGElement rootNode = doc.getRootElement();
			if(rootNode != null) {
				String valStr = rootNode.getAttribute("height");
				if(valStr != null && valStr.length() > 0) {
					valStr = valStr.trim();
					if(valStr.endsWith(("px"))) {
						valStr = valStr.substring(0, (valStr.length()-2));
					}
					Float ff = Float.parseFloat(valStr);
					buffer.height = (int)ff.intValue();
				} else {
					buffer.height = 0;
				}
				if(valStr != null && valStr.length() > 0) {
					valStr = valStr.trim();
					if(valStr.endsWith(("px"))) {
						valStr = valStr.substring(0, (valStr.length()-2));
					}
					Float ff = Float.parseFloat(valStr);
					buffer.width = (int)ff.intValue();
				} else {
					buffer.width = 0;
				}
			} else {
				buffer.height = 0;
				buffer.width = 0;
			}
			buffer.mimeType = INTERNAL_BUFFERTYPE;
			buffer.data = doc;
			return (true);
		} else {
			if (dgsFile.mimeType.equals("image/png") || dgsFile.mimeType.equals("image/jpeg") || dgsFile.mimeType.equals("image/tiff")) {
				BufferedImage bi = null;
				try {
					bi = javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(dgsFile.data));
					buffer = workspace.createImageBuffer(bufferName);
					buffer.height = bi.getHeight();
					buffer.width = bi.getWidth();
					buffer.mimeType = dgsFile.mimeType.intern();
					buffer.data = dgsFile.data.clone();
					return (true);
				} catch (IOException ie) {
					workspace.log("Image could not be loaded because it is corrupt or can't be loaded by the internal image loader: " + ie.getMessage());
				}
			} else if (dgsFile.mimeType.equals("image/gif")) {
				// for gif's we have to convert them to an internal acceptable format
				// TIFF would be prefered if it supported transparencies like PNG since
				// it is uncompressed and the memory required has to be allocated at
				// some point anyway.

				BufferedImage bi = null;
				java.io.ByteArrayOutputStream os;

				try {
					bi = javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(dgsFile.data));
					os = new java.io.ByteArrayOutputStream();
					if(!javax.imageio.ImageIO.write(bi, "png", os)) {
						workspace.log("GIF Image could not be converted to an acceptable internal format.");
						return(false);
					}
					buffer = workspace.createImageBuffer(bufferName);
					buffer.height = bi.getHeight();
					buffer.width = bi.getWidth();
					buffer.mimeType = dgsFile.mimeType.intern();
					buffer.data = os.toByteArray().clone();
					return (true);
				} catch (IOException ie) {
					workspace.log("An error occurred in the load command: Image could not be loaded because it is corrupt or can't be loaded by the internal image loader: " + ie.getMessage());
				}
			}
		}
		return (false);
	}

	public byte[] getImageData(ProcessingWorkspace workspace, Object svgData, String mimeType, float quality, float snapshotTime, String bufferType, java.awt.Dimension size) {
		org.apache.batik.transcoder.Transcoder t = null;
		if(mimeType.equals("image/png")) {
			t = new DGSPNGTranscoder(workspace);
		} else if (mimeType.equals("image/gif")) {
			t = new DGSGIFTranscoder(workspace);
		} else if (mimeType.equals("image/jpeg")) {
			t = new DGSJPEGTranscoder(workspace, new Float(quality)/100.0f); // the encoder expects a value between 1 and 0, so we must normalize the input value from the range of 0 - 100
		} else if (mimeType.equals("image/tiff")) {
			t = new DGSTIFFTranscoder(workspace);
		} else if (mimeType.equals("application/pdf")) {
			t = new DGSPDFTranscoder(workspace);
		} else if (mimeType.equals(MIME_BUFFERTYPE)) {
			t = new DGSSVGTranscoder(workspace);
		} else {
			workspace.log("Transcoder Error: Unsupported MIME type requested: " + mimeType.toString());
			return (null);
		}

		// for now we do our best to completely disable scripting
		t.addTranscodingHint(org.apache.batik.transcoder.SVGAbstractTranscoder.KEY_ALLOWED_SCRIPT_TYPES, COMMAND_ENGINE_ALLOWED_SCRIPT_TYPES);
		if(workspace.activeStylesheet != null && (!workspace.activeStylesheet.isEmpty())) {
			t.addTranscodingHint(org.apache.batik.transcoder.SVGAbstractTranscoder.KEY_USER_STYLESHEET_URI, workspace.activeStylesheet);
		}

		t.addTranscodingHint(org.apache.batik.transcoder.SVGAbstractTranscoder.KEY_CONSTRAIN_SCRIPT_ORIGIN, true);
		// we now allow scripts and animation to run
		t.addTranscodingHint(org.apache.batik.transcoder.SVGAbstractTranscoder.KEY_EXECUTE_ONLOAD, true);
		t.addTranscodingHint(KEY_ANIMATION_ENABLED, true);
		t.addTranscodingHint(org.apache.batik.transcoder.SVGAbstractTranscoder.KEY_SNAPSHOT_TIME, snapshotTime);

		byte oDat[];
		try {
			TranscoderInput input = null;
			TranscoderOutput output = null;
			Document svgDoc = null;
			if(bufferType.equals(MIME_BUFFERTYPE)) {
				//input = new TranscoderInput(new java.io.ByteArrayInputStream((byte[])svgData.toString().getBytes()));
				try {
					String parser = XMLResourceDescriptor.getXMLParserClassName();
					SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
					svgDoc = f.createSVGDocument(null, new java.io.StringReader((String)new String(((byte[])svgData), "UTF8")));
					// TODO: If this is not set to http:// then the script engines seem to break and refuse to script the svg
					// a real cause and fix needs to be found
					svgDoc.setDocumentURI("http://localhost/workspace.svg");
				} catch (Exception ex) {
					workspace.log("An error occurred parsing the SVG file data for transcoding: " + ex.getMessage());
					return(null);
				}
			} else {
				svgDoc = (Document)svgData;
			}
			input = new TranscoderInput((Document)svgDoc);

			java.io.ByteArrayOutputStream outStream = new java.io.ByteArrayOutputStream();
			output = new TranscoderOutput(outStream);

			try {
				t.transcode(input, output);
			} catch (TranscoderException ex) {
				// TODO: for some reason if we do anything with ex here some times we don't get any output to the workspace log
				workspace.log("Transcoder Error:");
				workspace.log(" -> " + ex.toString());
				return(null);
			} catch (Exception ex) {
				// TODO: for some reason if we do anything with ex here some times we don't get any output to the workspace log
				workspace.log("Transcoder Error:");
				workspace.log(" -> " + ex.toString());
				return(null);
			}

			// Flush and close the stream.
			outStream.flush();
			outStream.close();

			oDat = outStream.toByteArray();
			if (oDat == null || oDat.length == 0) {
				workspace.log("Transcoder did not return any data.");
				return(null);
			}

		} catch (Exception ex) {
			workspace.log("Unexpected transcoder error: " + ex.getMessage());
			return(null);
		}

		if(size != null) {
			if (mimeType.equals(MIME_BUFFERTYPE)) {
				// SVG outputs do not have sizes that we have access to at this time.
////			try {
////				String parser = XMLResourceDescriptor.getXMLParserClassName();
////				SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
////				SVGDocument doc = (org.apache.batik.dom.svg.SVGDocument)f..createDocument(svg);
////				org.apache.batik.swing.svg.GVTBuilder builder = new GVTBuilder();
////				BridgeContext ctx;
////				ctx = new BridgeContext(new UserAgentAdapter());
////				GraphicsNode gvtRoot = builder.build(ctx, doc);
////				return gvtRoot.getSensitiveBounds();
////			} catch (Exception ex) {
////			}
			} else if (mimeType.equals("image/tiff")) {
				// TIFF sizes must be handled differently, might be able to use the
				// same as above
			} else if (mimeType.equals("application/pdf")) {
				// PDF sizes must be handled differently, might be able to use the
				// same as above if the pdf is set to fit the image.
			} else {
				BufferedImage image = null;
				try {
					image = javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(oDat));
					size.height = image.getHeight();
					size.width = image.getWidth();
				} catch (IOException ie) {
					workspace.log("Transcoder frame output is corrupt or can't be loaded by the internal image loader: " + ie.getMessage());
					return(null);
				}
				if(image == null) {
					workspace.log("Transcoder frame output returns null from internal image loader.");
					return(null);
				}
			}
		}
		return(oDat);
	}

	public boolean save(ProcessingWorkspace workspace, String fileName, ProcessingEngineImageBuffer buffer, String mimeType, NamedNodeMap attributes) {
		String extension = "";

		if ((!buffer.mimeType.equals(MIME_BUFFERTYPE)) && (!buffer.mimeType.equals(INTERNAL_BUFFERTYPE))) {
			return (false);
		}

		byte[] originalDocument = null;
		Document inputDoc;
		byte[] oDat = null;
		Object workBuf = null;
		if(buffer.mimeType.equals(INTERNAL_BUFFERTYPE)) {
/*			// BEGIN HACK
			// this is a hack to deal with the fact
			// that the renderer doesn't seem to work properly
			// if rendering directly off the Document after nodes
			// have been inserted/removed/futzed with
			//
			// NOTE: This also makes it so we can pass a byte array
			//		to getImageData and it will copy and convert it 
			//		to a batik/svgdom style internal copy to render.
			//		This prevents the modifications the transcoder
			//		makes to the document during animation from 
			//		affecting future frames.
			TransformerFactory tf = TransformerFactory.newInstance();
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			Transformer t = null;
			try {
				t = tf.newTransformer();
				t.transform(new DOMSource((Document)buffer.data), new StreamResult(outStream));
			} catch (Exception ex) {
				workspace.log("An error occurred while reconstructing the XML file during save call: " + ex.getMessage());
				return (false);
			}
			originalDocument = outStream.toByteArray();
			// END HACK
*/
			DOMImplementation impl;
			Document document;
			document = (Document)buffer.data;
			if (document instanceof SVG12OMDocument) {
				impl = SVG12DOMImplementation.getDOMImplementation();
			} else if ((document instanceof SVGOMDocument)) {
				impl = SVGDOMImplementation.getDOMImplementation();
			} else {
				// try the standard implementation and hope for the best
				impl = SVGDOMImplementation.getDOMImplementation();
			}
			workBuf = (Object)DOMUtilities.deepCloneDocument(document, impl);
		} else {
			originalDocument = new byte[((byte[])buffer.data).length];
			System.arraycopy((byte[])buffer.data, 0, originalDocument, 0, ((byte[])buffer.data).length);
			workBuf = originalDocument;
		}
		if (mimeType.equals("image/png")) {
			extension = ".png";
		} else if (mimeType.equals("image/gif")) {
			extension = ".gif";
		} else if (mimeType.equals("image/jpeg")) {
			extension = ".jpg";
		} else if (mimeType.equals("image/tiff")) {
			extension = ".tif";
		} else if (mimeType.equals("application/pdf")) {
			extension = ".pdf";
		} else if (mimeType.equals(MIME_BUFFERTYPE)) {
			extension = ".svg";
		} else {
			workspace.log("An error occurred in the save command: Unsupported MIME type specified: " + mimeType.toString());
			return (false);
		}

		java.awt.Dimension size = new java.awt.Dimension();
		oDat = this.getImageData(workspace, workBuf, mimeType, 100.0f, 0.0f, buffer.mimeType, size);
		if(oDat == null) {
			workspace.log("Image creation failed.  Input type: " + MIME_BUFFERTYPE + " Output type: " + mimeType);
			return(false);
		}

		String fname = "";
		if (!fileName.endsWith(extension)) {
			fname = fileName.concat(extension);
		}
		DGSFileInfo dgsFile = null;
		for (int i = 0; i < workspace.files.size(); i++) {
			dgsFile = workspace.files.get(i);
			if (dgsFile.name.equals(fname)) {
				break;
			}
			dgsFile = null;
		}
		if (dgsFile == null) {
			// this means there is no file in the workspace already with the name we're trying to save to, so we must create a new one.
			dgsFile = new DGSFileInfo();
			dgsFile.name = fname;
			workspace.files.add(dgsFile);
		}
		dgsFile.mimeType = mimeType;
		dgsFile.height = size.height;
		dgsFile.width = size.width;
		dgsFile.data = oDat;
		return (true);
	}
}
