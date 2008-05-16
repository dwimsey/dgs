/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ImageProcessor.ProcessingEngine.CommandEnginePlugins.Batik.Instructions;

import ImageProcessor.ProcessingEngine.*;

import org.w3c.dom.*;

import java.io.*;

/**
 *
 * @author dwimsey
 */
public class substituteVariables implements ImageProcessor.ProcessingEngine.Instructions.IInstruction {
    public boolean process(ImageProcessor.ProcessingEngine.ProcessingWorkspace workspace, Node instructionNode)
    {
        NamedNodeMap attributes = instructionNode.getAttributes();
        Node bufferNode = attributes.getNamedItem("buffer");
        String bufferName = null;

        if(bufferNode == null) {
            if(!workspace.requestInfo.continueOnError) {
                workspace.log("Processing halted because the command does not have a buffer attribute: " + instructionNode.getNodeName());
                return(false);  // this should throw an exception instead
            } else {
                workspace.log("Processing of command skipped because it does not have a buffer attributes: " + instructionNode.getNodeName());
                return(false);  // this should throw an exception instead
            }
        } else {
            bufferName = bufferNode.getNodeValue();
            if(bufferName == null || bufferName.length()==0) {
               if(!workspace.requestInfo.continueOnError) {
                    workspace.log("Processing halted because the command does not have a value for the buffer attribute: " + instructionNode.getNodeName());
                    return(false);  // this should throw an exception instead
                } else {
                    workspace.log("Processing of command skipped because it does not have a value for the buffer attributes: " + instructionNode.getNodeName());
                    return(false);  // this should throw an exception instead
                }
            }
        }

        ProcessingEngineImageBuffer iBuffer = workspace.getImageBuffer(bufferName);
        if(iBuffer == null) {
            workspace.log("There is no buffer named '" + bufferName + "' to do a substitution on.");
            return(false);
        }
        if(!iBuffer.mimeType.equals("image/svg+xml")) {
            return(false);
        }

        if(workspace.requestInfo.variables == null || workspace.requestInfo.variables.length == 0) {
            return(true);
        }

        String oStr = null;
        
        try {
            oStr = new String((byte[])iBuffer.data, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            workspace.log("An unexpected encoding error has occurred: " + ex.getMessage());
            return(false);
        }
        for(int i = 0; i < workspace.requestInfo.variables.length; i++) {
            oStr = oStr.replaceAll(java.util.regex.Pattern.quote("{" + workspace.requestInfo.variables[i].name + "}"), java.util.regex.Matcher.quoteReplacement(workspace.requestInfo.variables[i].data));
        }
        oStr = oStr.replaceAll(java.util.regex.Pattern.quote("{{"), java.util.regex.Matcher.quoteReplacement("{"));
        try {
            iBuffer.data = oStr.getBytes("UTF-8");
        } catch (Exception ex) {
            workspace.log("An exception occurred during variable replacement while preparing the SVG result buffer: " + ex.getMessage());
            return(false);
        }
        return(true);
    }
 }
