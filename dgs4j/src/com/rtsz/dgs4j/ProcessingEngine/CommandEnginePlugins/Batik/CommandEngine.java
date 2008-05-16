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

/**
 *
 * @author dwimsey
 */
public class CommandEngine implements ICommandEngine {

    public void init()
    {   
    }

    public void addCommands(ProcessingEngine pEngine)
    {
        pEngine.addCommandInstruction("substituteVariables", new ImageProcessor.ProcessingEngine.CommandEnginePlugins.Batik.Instructions.substituteVariables());
    }
    
    private String getAttributeValue(NamedNodeMap attributes, String aName)
    {
        Node cNode = attributes.getNamedItem(aName);
        if(cNode == null) {
            return(null);
        }
        return(cNode.getNodeValue());
    }

    public boolean load(ProcessingWorkspace workspace, String fileName, String bufferName, String mimeType, NamedNodeMap attributes)
    {
        DGSFileInfo dgsFile = null;
        for(int i = 0; i < workspace.requestInfo.files.length; i++) {
            if(workspace.requestInfo.files[i].name.equals(fileName)) {
                dgsFile = workspace.requestInfo.files[i];
                break;
            }
        }
        if(dgsFile == null) {
            workspace.log("Could not find file specified in request info file list: " + fileName);
            return(false);
        }

        if(dgsFile.mimeType.equals("image/svg+xml")) {
            String uri = "data://image/svg+xml;base64,";
            uri += ImageProcessor.ProcessingEngine.Base64.encodeBytes(dgsFile.data);
            Document doc = null;

            try {
                String parser = XMLResourceDescriptor.getXMLParserClassName();
                SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
                //doc = f.createDocument(uri, new java.io.ByteArrayInputStream((byte[])iBuffer.data));
                doc = f.createDocument(uri);
            } catch (IOException ex) {
                workspace.log("An error occurred parsing the SVG file data: " + ex.getMessage());
                return(false);
            }

            TransformerFactory tf = TransformerFactory.newInstance();
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            try {
                Transformer t = tf.newTransformer();
                t.transform(new DOMSource(doc), new StreamResult(outStream));
            } catch (Exception ex) {
                workspace.log("An error occurred while reconstructing the XML file: " + ex.getMessage());
                return(false);
            }
            ProcessingEngineImageBuffer buffer = workspace.createImageBuffer(bufferName);
            buffer.height = -1;
            buffer.width = -1;
            buffer.mimeType = mimeType.intern();
            buffer.data = outStream.toByteArray();
            return(true);
        }
        return(false);
    }

    public boolean save(ProcessingWorkspace workspace, String fileName, ProcessingEngineImageBuffer buffer, String mimeType, NamedNodeMap attributes)
    {
        int width = 0;
        int height = 0;

        org.apache.batik.transcoder.Transcoder t = null;
        String extension = "";

        if(!buffer.mimeType.equals("image/svg+xml")) {
            return(false);
        }
        if(mimeType.equals("image/png")) {
            extension = ".png";
            t = new DGSPNGTranscoder(workspace);
        } else if(mimeType.equals("image/jpeg")) {
            extension = ".jpg";
            t = new DGSJPEGTranscoder(workspace);
//            t.addTranscodingHint(DGSJPEGTranscoder.KEY_QUALITY, new Float(100));
        } else if(mimeType.equals("image/tiff")) {
            extension = ".tif";
            t = new DGSTIFFTranscoder(workspace);
        } else if(mimeType.equals("application/pdf")) {
            extension = ".pdf";
            t = new DGSPDFTranscoder(workspace);
        } else {
            return(false);
        }

//        t.addTranscodingHint(org.apache.batik.transcoder.SVGAbstractTranscoder.KEY_ALLOWED_SCRIPT_TYPES, "text/ecmascript");
        // for now we do our best to completely disable scripting
        t.addTranscodingHint(org.apache.batik.transcoder.SVGAbstractTranscoder.KEY_ALLOWED_SCRIPT_TYPES, "");
        t.addTranscodingHint(org.apache.batik.transcoder.SVGAbstractTranscoder.KEY_CONSTRAIN_SCRIPT_ORIGIN, true);
        t.addTranscodingHint(org.apache.batik.transcoder.SVGAbstractTranscoder.KEY_EXECUTE_ONLOAD, false);

        byte oDat[];
        try {
            TranscoderInput input = null;
            TranscoderOutput output = null;
            java.io.ByteArrayInputStream inStream = new java.io.ByteArrayInputStream((byte[])buffer.data);
            java.io.ByteArrayOutputStream outStream = new java.io.ByteArrayOutputStream();


            input = new TranscoderInput(inStream);
            output = new TranscoderOutput(outStream);

            try {
                t.transcode(input, output);
            } catch (Exception ex) {
                workspace.log("Convertion from " + buffer.mimeType + " to " + mimeType + " failed.  Transcoder Error: " + ex.toString());
                return(false);
            }

            // Flush and close the stream.
            outStream.flush();
            outStream.close();

            oDat  = outStream.toByteArray();
            if(oDat == null || oDat.length == 0) {
                workspace.log("Convertion from " + buffer.mimeType + " to " + mimeType + " failed. Transcoder did not return any data.");
                return(false);
            }
            
            try {
                java.awt.image.BufferedImage image = null;
                image = javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(oDat));
                height = image.getHeight();
                width = image.getWidth();
            } catch (IOException ie) {
                workspace.log("Transcoder output is corrupt or can't be loaded by the internal image loader: " + ie.getMessage());
            }
        } catch (Exception ex) {
            workspace.log("Convertion from " + buffer.mimeType + " to " + mimeType + " failed: " + ex.getMessage());
            return(false);
        }

        String fname = "";
        if(!fileName.endsWith(extension)) {
            fname = fileName.concat(extension);
        }
        DGSFileInfo dgsFile = null;
        for(int i = 0; i < workspace.files.size(); i++) {
            dgsFile = workspace.files.get(i);
            if(dgsFile.name.equals(fname)) {
                break;
            }
            dgsFile = null;
        }
        if(dgsFile == null) {
            dgsFile = new DGSFileInfo();
            dgsFile.name = fname;
            workspace.files.add(dgsFile);
        }

        dgsFile.mimeType = mimeType;
        dgsFile.height = height;
        dgsFile.width = width;
        dgsFile.data = oDat;

        return(true);
    }
}
