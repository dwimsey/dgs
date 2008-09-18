/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ImageProcessor.ProcessingEngine.CommandEnginePlugins.Batik;

import ImageProcessor.ProcessingEngine.*;
import ImageProcessor.DGSFileInfo;

import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;

import org.w3c.dom.*;

import java.io.IOException;
import java.io.ByteArrayOutputStream;

import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.util.XMLResourceDescriptor;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;


// all these are required for GIF stuffs
//import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
//import java.io.File;
import java.util.Iterator;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
//import javax.imageio.stream.FileImageOutputStream;

/**
 *
 * @author dwimsey
 */
public class CommandEngine implements ICommandEngine {

	public static final String INTERNAL_BUFFERTYPE = "batik/svgdom";
	public static final String MIME_BUFFERTYPE = "image/svg+xml";

	public void init() {
	}

	public void addCommands(ProcessingEngine pEngine) {
		pEngine.addCommandInstruction("replaceImage", new ImageProcessor.ProcessingEngine.CommandEnginePlugins.Batik.Instructions.replaceImage());
		pEngine.addCommandInstruction("setVisibility", new ImageProcessor.ProcessingEngine.CommandEnginePlugins.Batik.Instructions.setVisibility());
		pEngine.addCommandInstruction("substituteVariables", new ImageProcessor.ProcessingEngine.CommandEnginePlugins.Batik.Instructions.substituteVariables());
                pEngine.addCommandInstruction("addWatermark", new ImageProcessor.ProcessingEngine.CommandEnginePlugins.Batik.Instructions.addWatermark());
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
			String uri = "data://" + MIME_BUFFERTYPE + ";base64,";
			uri += ImageProcessor.ProcessingEngine.Base64.encodeBytes(dgsFile.data);
			Document doc = null;

			try {
				String parser = XMLResourceDescriptor.getXMLParserClassName();
				SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
				doc = f.createDocument(uri);
			} catch (IOException ex) {
				workspace.log("An error occurred parsing the SVG file data: " + ex.getMessage());
				return (false);
			}

			buffer = workspace.createImageBuffer(bufferName);
			buffer.height = -1;
			buffer.width = -1;
			buffer.mimeType = INTERNAL_BUFFERTYPE;
			buffer.data = doc;
			return (true);
		} else {
			if (dgsFile.mimeType.equals("image/png") || dgsFile.mimeType.equals("image/gif") || dgsFile.mimeType.equals("image/jpeg") || dgsFile.mimeType.equals("image/tiff")) {
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
			}
		}
		return (false);
	}

	public byte[] getImageData(ProcessingWorkspace workspace, Object svgData, String mimeType, float quality, float snapshotTime, String bufferType) {
		org.apache.batik.transcoder.Transcoder t = null;
		if (mimeType.equals("image/png")) {
			t = new DGSPNGTranscoder(workspace);
		} else if (mimeType.equals("image/jpeg")) {
			t = new DGSJPEGTranscoder(workspace);
            t.addTranscodingHint(DGSJPEGTranscoder.KEY_QUALITY, new Float(quality));
		} else if (mimeType.equals("image/tiff")) {
			t = new DGSTIFFTranscoder(workspace);
		} else if (mimeType.equals("application/pdf")) {
			t = new DGSPDFTranscoder(workspace);
		} else {
			return (null);
		}

		// for now we do our best to completely disable scripting
		t.addTranscodingHint(org.apache.batik.transcoder.SVGAbstractTranscoder.KEY_ALLOWED_SCRIPT_TYPES, "");
		t.addTranscodingHint(org.apache.batik.transcoder.SVGAbstractTranscoder.KEY_CONSTRAIN_SCRIPT_ORIGIN, true);
		if(snapshotTime>0.0f) {
			t.addTranscodingHint(org.apache.batik.transcoder.SVGAbstractTranscoder.KEY_EXECUTE_ONLOAD, true);
		} else {
			t.addTranscodingHint(org.apache.batik.transcoder.SVGAbstractTranscoder.KEY_EXECUTE_ONLOAD, false);
		}
		t.addTranscodingHint(org.apache.batik.transcoder.SVGAbstractTranscoder.KEY_SNAPSHOT_TIME, snapshotTime);
		byte oDat[];
		try {
			TranscoderInput input = null;
			TranscoderOutput output = null;
			if(bufferType.equals(MIME_BUFFERTYPE)) {
				input = new TranscoderInput(new java.io.ByteArrayInputStream((byte[])svgData));
			} else {
				input = new TranscoderInput((Document)svgData);
			}
			java.io.ByteArrayOutputStream outStream = new java.io.ByteArrayOutputStream();
			output = new TranscoderOutput(outStream);

			try {
				t.transcode(input, output);
			} catch (Exception ex) {
				workspace.log("Transcoder Error: " + ex.toString());
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

		return(oDat);
	}

	public boolean save(ProcessingWorkspace workspace, String fileName, ProcessingEngineImageBuffer buffer, String mimeType, NamedNodeMap attributes) {
		int width = 0;
		int height = 0;

		String extension = "";

		if ((!buffer.mimeType.equals(MIME_BUFFERTYPE)) && (!buffer.mimeType.equals(INTERNAL_BUFFERTYPE))) {
			return (false);
		}
		if(buffer.mimeType.equals(INTERNAL_BUFFERTYPE)) {
			// BEGIN HACK
			// this is a hack to deal with the fact
			// that the renderer doesn't seem to work properly
			// if rendering directly off the Document after nodes
			// have been inserted/removed/futzed with
			TransformerFactory tf = TransformerFactory.newInstance();
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			Transformer t = null;
			try {
				t = tf.newTransformer();
				t.transform(new DOMSource((Document)buffer.data), new StreamResult(outStream));
			} catch (Exception ex) {
				workspace.log("An error occurred while reconstructing the XML file after substituteVariables call: " + ex.getMessage());
				return (false);
			}
			buffer.data = outStream.toByteArray();
			
			
			String uri = "data://" + CommandEngine.MIME_BUFFERTYPE + ";base64,";
			uri += ImageProcessor.ProcessingEngine.Base64.encodeBytes((byte[]) buffer.data);

			try {
				SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(org.apache.batik.util.XMLResourceDescriptor.getXMLParserClassName());
				buffer.data = f.createDocument(uri);
			} catch (IOException ex) {
				workspace.log("An error occurred parsing the SVG file data in the substituteVariables command: " + ex.getMessage());
				return (false);
			}
			// END HACK
		}
		if (mimeType.equals("image/png")) {
			extension = ".png";
		} else if (mimeType.equals("image/gif")) {
			extension = ".gif";
			// we use the png transcoder with some special options, and convert it from png to gif
			// afterwords since batik doesn't do it natively, for good reasons (its not part of the
			// svg spec)
		} else if (mimeType.equals("image/jpeg")) {
			extension = ".jpg";
		} else if (mimeType.equals("image/tiff")) {
			extension = ".tif";
		} else if (mimeType.equals("application/pdf")) {
			extension = ".pdf";
		} else {
			return (false);
		}

		int frameCount = 0;
		float timeStep = 0.0f;
		// if its a gif file output, see if animation values are supported.  GIF is the only
		// output type supporting animation at this time
		if (mimeType.equals("image/gif")) {
			String aDurationStr = this.getAttributeValue(attributes, "animationDuration");
			String aFramerateStr = this.getAttributeValue(attributes, "animationFramerate");
			if((aDurationStr != null) && (aFramerateStr != null)) {
				float duration = 0.0f;
				float framerate = 0.0f;
				try {
					duration = Float.parseFloat(aDurationStr);
				} catch(NumberFormatException ex) {
						workspace.log("Could not parse animationDuration attribute: " + ex.getMessage());
				}
				try {
					framerate = Float.parseFloat(aFramerateStr);
				} catch(NumberFormatException ex) {
						workspace.log("Could not parse animationFramerate attribute: " + ex.getMessage());
				}
				if(duration >= Float.MIN_VALUE) {
					if(framerate >= Float.MIN_VALUE) {
						frameCount = (int)(duration*framerate);
						timeStep = 1.0f/framerate;
					} else {
						workspace.log("animationFramerate attribute must be a positive value.");
					}
				} else {
					if(framerate < Float.MIN_VALUE) {
						workspace.log("animationFramerate attribute must be a positive value.");
					}
					workspace.log("animationDuration attribute must be a positive value.");
				}
			}
		}

		BufferedImage image = null;
		byte oDat[] = null;
		if((frameCount > 0) && (timeStep > 0.0f)) {
			// generate the images and combine them
			BufferedImage imgs[] = new BufferedImage[frameCount];
			// we use png as an intermediate because Batik does not support GIF natively, rightly so as GIF
			// is not included in the SVG spec, so we'll have to roll our own for user convience
			int ii = 0; // outputCell
			for(int i = 0; i < frameCount; i++) {
				oDat = this.getImageData(workspace, buffer.data, "image/png", 100.0f, (timeStep * i), INTERNAL_BUFFERTYPE);
				if(oDat == null) {
					if(workspace.requestInfo.continueOnError) {
						workspace.log("Animation sequence contains an invalid frame, it will be ignored.  Frame Number: " + i);
						continue;
					} else {
						workspace.log("Animation sequence contains an invalid frame, creation aborted due to error.  Frame Number: " + i);
						return(false);
					}
				}
				try {
					image = javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(oDat));
					height = image.getHeight();
					width = image.getWidth();
				} catch (IOException ie) {
					workspace.log("Transcoder frame output is corrupt or can't be loaded by the internal image loader.  Frame Number: " + i + "Error: " + ie.getMessage());
					if(workspace.requestInfo.continueOnError) {
						continue;
					} else {
						return(false);
					}
				}
				imgs[ii++] = image;
			}
			if(ii==0) {
				workspace.log("Animation sequence does not contain any invalid frames, Expected Frames: " + frameCount);
				return(false);
			} else if (ii != frameCount) {
				// resize our image buffer
				BufferedImage nimg[] = new BufferedImage[ii];
				for(int iii = 0; iii < ii; iii++) {
					nimg[iii] = imgs[iii];
				}
				imgs = nimg;
			}

			try {
				// we've got our frames, make a GIF now			
				Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("gif");
				ImageWriter writer = writers.next(); // Always assume GIF is available

				// prepare the sequence writer
				ByteArrayOutputStream outStream = new ByteArrayOutputStream();
				writer.setOutput(outStream);

				writer.prepareWriteSequence(null);

				// write the sequence
				IIOImage img;
				for(int i = 0; i < imgs.length; i++) {
					img = new IIOImage(imgs[i], null, null);
					writer.writeToSequence(img, null);
				}

				// terminate the sequence writer
				writer.endWriteSequence();

				// Flush and close the stream.
				outStream.flush();
				outStream.close();
				// oDat is our byte array representing an animated GIF
				oDat = outStream.toByteArray();
			} catch(IOException ex) {
				workspace.log("Could not assembly animation sequence into GIF.  Exception: " + ex.getMessage());
				return(false);
			}
		} else {
			// generate a single frame
			oDat = this.getImageData(workspace, buffer.data, mimeType, 100.0f, 0.0f, INTERNAL_BUFFERTYPE);
			if(oDat == null) {
				workspace.log("Conversion from " + buffer.mimeType + " to " + mimeType + " failed.");
				return(false);
			}
			try {
				image = null;
				image = javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(oDat));
				height = image.getHeight();
				width = image.getWidth();
			} catch (IOException ie) {
				workspace.log("Transcoder output is corrupt or can't be loaded by the internal image loader: " + ie.getMessage());
				if(!workspace.requestInfo.continueOnError) {
					return(false);
				}
			}
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
			dgsFile = new DGSFileInfo();
			dgsFile.name = fname;
			workspace.files.add(dgsFile);
		}

		dgsFile.mimeType = mimeType;
		dgsFile.height = height;
		dgsFile.width = width;
		dgsFile.data = oDat;

		return (true);
	}
}
