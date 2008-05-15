/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ImageProcessor.ProcessingEngine.CommandEnginePlugins.Batik;

import ImageProcessor.ProcessingEngine.*;
import ImageProcessor.DGSFileInfo;

import org.apache.batik.transcoder.image.*;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;

import org.w3c.dom.*;

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
            return(false);
        }

        if(dgsFile.mimeType.equals("image/svg+xml")) {
            ProcessingEngineImageBuffer buffer = workspace.createImageBuffer(bufferName);
            buffer.height = -1;
            buffer.width = -1;
            buffer.mimeType = mimeType.intern();
            buffer.data = dgsFile.data.clone();
            return(true);
        }
        return(false);
    }

    public boolean save(ProcessingWorkspace workspace, String fileName, ProcessingEngineImageBuffer buffer, String mimeType, NamedNodeMap attributes)
    {
        org.apache.batik.transcoder.Transcoder t = null;
        String extension = "";

        if(!buffer.mimeType.equals("image/svg+xml")) {
            return(false);
        }
        if(mimeType.equals("image/png")) {
            extension = ".png";
            t = new DGSPNGTranscoder();
        } else if(mimeType.equals("image/jpeg")) {
            extension = ".jpg";
            t = new DGSJPEGTranscoder();
//            t.addTranscodingHint(DGSJPEGTranscoder.KEY_QUALITY, new Float(100));
        } else if(mimeType.equals("image/tiff")) {
            extension = ".tif";
            t = new DGSTIFFTranscoder();
        } else if(mimeType.equals("application/pdf")) {
            extension = ".pdf";
            t = new DGSPDFTranscoder();
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
        dgsFile.height = 0;
        dgsFile.width = 0;
        dgsFile.data = oDat;

        return(true);
    }
}