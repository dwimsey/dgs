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

	//0.47+devel r9492
	private int getInkscapeQuirksVersion(String inkscapeIdStr)
	{
		int o = inkscapeIdStr.indexOf(" r");
		if(o > -1) {
			try {
				return(java.lang.Integer.parseInt(inkscapeIdStr.substring(o+2)));
			} catch(Throwable t) {

			}
		}
		return(1);
	}

        public boolean load(ProcessingWorkspace workspace, String fileName, String bufferName, String mimeType, NamedNodeMap attributes) {
                boolean quirkVersion12UpConvert = false;
		int quirkInkscapeRev = 0;
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
			String parser = XMLResourceDescriptor.getXMLParserClassName();
			SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
			try {
				//TODO: // this always forces the 1.1 version to 1.2 but its a nasty hack, we need to do this properly
                                try {
                                    doc = f.createSVGDocument(null, new java.io.StringReader((String)new String(dgsFile.data, "UTF8")));
                                } catch(Throwable eex) {
                                    doc = f.createSVGDocument(null, new java.io.StringReader((String)new String(dgsFile.data, "UTF8").replaceFirst("version=\"1.1\"", "version=\"1.2\"")));
                                    // we had to upconvert to 1.2 to get something to work, lame, flag it
                                    quirkVersion12UpConvert = true;
                                }
			} catch (Throwable ex) {
				workspace.log("An error occurred parsing the SVG file data: " + ex.getMessage());
				return (false);
			}

			buffer = workspace.createImageBuffer(bufferName);
			SVGElement rootNode = doc.getRootElement();
			if(rootNode != null) {
                                // TODO: Figure out a better way to handle inkscape HACKS
                                // HACK: If this is an inkscape file, we have to do a couple special things
                                String valStr = "";
                                String oStr = "";
                                SVGElement wNode = null;// = (SVGElement)ns.item(0);
                                SVGElement wwNode = null;
                                SVGElement wwwNode = null;
                                int i = 0;
                                int ii = 0;
                                int iii = 0;

                                NodeList ns;
                                NodeList ns2;
                                NodeList ns3;

                                String nodeName;

				// Check for inkscape identifier so we can handle its quirks
                                oStr = rootNode.getAttributeNS("http://www.inkscape.org/namespaces/inkscape", "version");
                                if(oStr != null) {
					quirkInkscapeRev = getInkscapeQuirksVersion(oStr);
                                } else {
                                    ns = doc.getElementsByTagNameNS("http://sodipodi.sourceforge.net/DTD/sodipodi-0.dtd", "namedview");
                                    if(ns!=null) {
						quirkInkscapeRev = 1;
                                    }
                                }
                                oStr = "";

				if(quirkInkscapeRev > 0) {
                                    // Find any flow roots and fix the flow region backgrounds and wrap the
                                    // text in a flowDiv
                                    SVGOMRectElement rNode;
                                    ns = doc.getElementsByTagName("flowRoot");
                                    if(ns != null) {
						String s = "";
						String ss = "";
						String p1 = "";
						String p2 = "";
						int offset1 = 0;
						int offset2 = 0;
                                        wNode = null;// = (SVGElement)ns.item(0);
                                        i = 0;
                                        wNode = (SVGElement)ns.item(i++);
                                        while(wNode != null) {
                                            // fix the rectangles for this flow root
                                            ns2 = wNode.getElementsByTagName("rect");
                                            if(ns2 != null && ns2.getLength()>0) {
                                                ii = 0;
                                                rNode = (SVGOMRectElement)ns2.item(ii++);
                                                while(rNode != null) {
                                                    // change the style for the rectangle to from fill-opacity:1 to fill-opacity:0
									s = rNode.getAttribute(rNode.SVG_STYLE_ATTRIBUTE);
									ss = s;

									offset1 = ss.indexOf("fill:");
									if(offset1 > -1) {
										p1 = ss.substring(0, offset1);
										offset2 = ss.indexOf(";", offset1);
										// check for end with no semi-colon
										if(offset2==-1) {
											offset2 = ss.length();
										}
										if(offset2 > (offset1 + 1)) {
											p2 = ss.substring(offset2);

											ss = ss.substring(offset1, offset2);
											// we have a value to deal with changing possibly
											String b = wNode.getAttribute("style");
											if(b.contains(ss)) {
												// the flowRoot and the rectangle have the same color
												// this is how inkscape stores the coloring so we have
												// to ignore the background color until its fixed
												ss = p1 + "fill:none";
											} else {
												ss = p1 + ss;
											}
											ss += p2;
										}
									}


									offset1 = ss.indexOf("fill-opacity:");
									if(offset1 > -1) {
										p1 = ss.substring(0, offset1);
										offset2 = ss.indexOf(";", offset1);
										// check for end with no semi-colon
										if(offset2==-1) {
											offset2 = ss.length();
										}
										if(offset2 > (offset1 + 1)) {
											p2 = ss.substring(offset2);

											ss = ss.substring(offset1, offset2);
											// we have a value to deal with changing possibly
											String b = wNode.getAttribute("style");
											if(b.contains(ss)) {
												// the flowRoot and the rectangle have the same color
												// this is how inkscape stores the coloring so we have
												// to ignore the background color until its fixed
												ss = p1;
											} else {
												ss = p1 + ss;
											}
											ss += p2;
										}
									}


									if(!s.equals(ss)) {
										rNode.setAttribute(rNode.SVG_STYLE_ATTRIBUTE, ss);
									}
                                                    rNode = (SVGOMRectElement)ns2.item(ii++);
                                                }
                                            }

                                            ns2 = wNode.getElementsByTagName("flowDiv");
                                            if(ns2 == null || ns2.getLength()==0) {
                                                // we don't have any flowDivs, do we have any flowPara's
                                                // that need to be moved?
                                                ns2 = wNode.getElementsByTagName("flowPara");
                                                if(ns2 != null && ns2.getLength()>0) {
                                                    // we need to move the flowPara's into the flowDiv
                                                    ii = 0;
                                                    Element fdNode = doc.createElement("flowDiv");
                                                    wwNode = (SVGElement)ns2.item(ii++);
                                                    while(wwNode != null) {
                                                        wNode.removeChild(wwNode);
                                                        fdNode.appendChild(wwNode);
                                                        wwNode = (SVGElement)ns2.item(ii++);
                                                    }
                                                    if(ii>0) {
                                                        wNode.appendChild(fdNode);
                                                    }
                                                }
                                            
                                            
                                            }

                                            wNode = (SVGElement)ns.item(i++);
                                        }
                                    }
                                }
                                   
                                valStr = rootNode.getAttribute("height");
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
                                valStr = rootNode.getAttribute("width");
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
