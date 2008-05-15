/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ImageProcessor.ProcessingEngine.CommandEnginePlugins.Batik.Instructions;

import ImageProcessor.*;
import ImageProcessor.ProcessingEngine.*;

import java.util.*;

import org.apache.xerces.parsers.*;
import org.xml.sax.*;
import org.w3c.dom.*;

//import java.io.File;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException; 
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
        if(!iBuffer.mimeType.equals("image/svg+xml")) {
            return(false);
        }

        Document doc = null;
        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            doc = docBuilder.parse(new java.io.ByteArrayInputStream((byte[])iBuffer.data));
        } catch (Exception ex) {
            workspace.log("Unexpected parser error: " + ex.getMessage());
        }

        NodeList t = doc.getElementsByTagName("text");
        int i = t.getLength();
        substituteVars(workspace, doc.getParentNode());
        return(true);
    }
    
    private void substituteVars(ProcessingWorkspace workspace, Node domNode)
    {
        
        if(domNode == null) {
            return;
        }
        String nodeName = domNode.getNodeName();
        NodeList childNodes = domNode.getChildNodes();
        if(childNodes != null) {
            for(int i = 0; i<childNodes.getLength(); i++) {
                substituteVars(workspace, childNodes.item(i));
            }
        }
        
    }
}
