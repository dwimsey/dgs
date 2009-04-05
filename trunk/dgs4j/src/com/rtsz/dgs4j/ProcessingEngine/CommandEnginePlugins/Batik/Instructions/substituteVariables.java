/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rtsz.dgs4j.ProcessingEngine.CommandEnginePlugins.Batik.Instructions;

import com.rtsz.dgs4j.ProcessingEngine.*;
import com.rtsz.dgs4j.ProcessingEngine.Instructions.*;
import com.rtsz.dgs4j.ProcessingEngine.CommandEnginePlugins.Batik.*;
import com.rtsz.dgs4j.ProcessingEngine.CommandEnginePlugins.Batik.CommandEngine.*;
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
public class substituteVariables implements IInstruction {

	public boolean process(ProcessingWorkspace workspace, Node instructionNode) {
		try {
		    NamedNodeMap attributes = instructionNode.getAttributes();
		    Node bufferNode = attributes.getNamedItem("buffer");
		    String bufferName = null;

		    if (bufferNode == null) {
			    if (!workspace.requestInfo.continueOnError) {
				    workspace.log("Processing halted because the command does not have a buffer attribute: " + instructionNode.getNodeName());
				    return (false);  // this should throw an exception instead
			    } else {
				    workspace.log("Processing of command skipped because it does not have a buffer attribute: " + instructionNode.getNodeName());
				    return (false);  // this should throw an exception instead
			    }
		    } else {
			    bufferName = bufferNode.getNodeValue();
			    if (bufferName == null || bufferName.length() == 0) {
				    if (!workspace.requestInfo.continueOnError) {
					    workspace.log("Processing halted because the command does not have a value for the buffer attribute: " + instructionNode.getNodeName());
					    return (false);  // this should throw an exception instead
				    } else {
					    workspace.log("Processing of command skipped because it does not have a value for the buffer attribute: " + instructionNode.getNodeName());
					    return (false);  // this should throw an exception instead
				    }
			    }
		    }

		    ProcessingEngineImageBuffer iBuffer = workspace.getImageBuffer(bufferName);
		    if (iBuffer == null) {
			    workspace.log("There is no buffer named '" + bufferName + "' to do a substituteVariables on.");
			    return (false);
		    }

		    if ((!iBuffer.mimeType.equals(CommandEngine.MIME_BUFFERTYPE)) && (!iBuffer.mimeType.equals(CommandEngine.INTERNAL_BUFFERTYPE))){
			    workspace.log("Buffer is not of type '" + CommandEngine.MIME_BUFFERTYPE + "' or '" + CommandEngine.INTERNAL_BUFFERTYPE + "' and no conversion is available for substituteVariables: " + bufferName);
			    return (false);
		    }

		    if (workspace.requestInfo.variables == null || workspace.requestInfo.variables.length == 0) {
			    workspace.log("This request has no variables associated with it, substituteVariables does not need to proceed: " + bufferName);
			    return (true);
		    }

		    Document doc = null;
		    if(iBuffer.mimeType.equals(CommandEngine.MIME_BUFFERTYPE)) {
			    String uri = "data://" + CommandEngine.MIME_BUFFERTYPE + ";base64,";
			    uri += Base64.encodeBytes((byte[]) iBuffer.data);

			    try {
				    String parser = XMLResourceDescriptor.getXMLParserClassName();
				    SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
				    //doc = f.createDocument(uri, new java.io.ByteArrayInputStream((byte[])iBuffer.data));
				    doc = f.createDocument(uri);
			    } catch (IOException ex) {
				    workspace.log("An error occurred parsing the SVG file data in the substituteVariables command: " + ex.getMessage());
				    return (false);
			    }
		    } else {
			    doc = (Document)iBuffer.data;
		    }
		    int i = 0;
		    int ii = 0;
		    int iii = 0;
		    int varStart;
		    int varEnd;
		    NodeList elements = null;
		    Node textNode = null;
		    Node textStringNode = null;
		    String val = null;
		    String lines[] = null;
		    String oStr = null;
		    String varName = null;
		    int varNextStart = 0;
		    String varValueStr = null;
		    boolean hasChanged = false;

		    elements = doc.getElementsByTagName("*");
		    int eSize = elements.getLength();
		    for (i = 0; i < eSize; i++) {
			    textNode = elements.item(i);
			    if (textNode != null) {
				    if((textNode.getNodeType()==textNode.ELEMENT_NODE)) {
					    textStringNode = textNode.getFirstChild();
					    while(textStringNode!=null) {
						    if(textStringNode.getNodeType()==textStringNode.TEXT_NODE) {
							    // check to see if its worth parsing this string
							    oStr = textStringNode.getNodeValue();
							    if(!oStr.replace('\t', ' ').replace('\n', ' ').replace('\r', ' ').trim().isEmpty()) {
								    hasChanged = false;
								    varStart = 0;
								    while(true) {
									    varStart = oStr.indexOf("{", varStart);
									    if(varStart == -1) {
										    // this string does not contain any variables, continue on
										    break;
									    }

									    if(oStr.indexOf("{", varStart+1)==0) {
										    // this character is a escaping {, skip it
										    varStart = varStart+2;
										    continue;
									    }

									    varEnd = oStr.indexOf("}", varStart);
									    if(varEnd == -1) {
										    //  no end of variable name was found, ignore this string
										    break;
									    }
									    varNextStart = oStr.indexOf("{", varStart+1);
									    if(varNextStart > 0) {
										    if(varNextStart < varEnd) {
											    // the end we're looking at is for another variable it seems
											    varStart = varStart+1;
											    continue;
										    }
									    }

									    // so at this point, we in theory have a variable name
									    varName = oStr.substring(varStart+1, varEnd);

									    varValueStr = null;
									    for (ii = 0; ii < workspace.requestInfo.variables.length; ii++) {
										String nvName = workspace.requestInfo.variables[ii].name;
										if(nvName.equals(varName)) {
											varValueStr = workspace.requestInfo.variables[ii].data.toString();
											break;
										}
									    }

									    if(varValueStr == null) {
										    // no variable by that name found, ignore it
										    varStart = varEnd + 1;
										    continue;
									    }

									    // we have a variable that has a replacement, do we have to deal with muliline?
									    if(varValueStr.indexOf("\n") > -1) {
										    // this is multiline, deal with it
										    String nStr;
										    Node ltn = null;
										    Node fl = null;

										    if(textNode.getNodeName().equals("flowPara")) {
											    nStr = oStr.substring(0, varStart);
											    lines = varValueStr.split(java.util.regex.Pattern.quote("\n") + "|" + java.util.regex.Pattern.quote("\r") + "|" + java.util.regex.Pattern.quote("\r\n"));
											    nStr += lines[0];
											    textStringNode.setNodeValue(nStr);
											    ltn = textStringNode;
											    for(iii = 1; iii<lines.length; iii++) {
												    fl = doc.createElement("flowLine");
												    textNode.insertBefore(fl, ltn.getNextSibling());

												    ltn = doc.createTextNode(lines[iii]);
												    textNode.insertBefore(ltn, fl.getNextSibling());
											    }
											    nStr = oStr.substring(varEnd+1);
											    textStringNode = doc.createTextNode(nStr);
											    textNode.insertBefore(textStringNode, ltn.getNextSibling());
											    varStart = 0;
											    oStr = nStr;
											    hasChanged = false;
										    } else {
											    nStr = oStr.substring(0, varStart) + varValueStr + oStr.substring(varEnd+1);
											    varStart = varStart + varValueStr.length();
											    oStr = nStr;
											    hasChanged = true;
										    }
									    } else {
										    String nStr = oStr.substring(0, varStart) + varValueStr + oStr.substring(varEnd+1);
										    varStart = varStart + varValueStr.length();
										    oStr = nStr;
										    hasChanged = true;
									    }
								    }
							    }
						    }
						    if(hasChanged == true) {
							    textStringNode.setNodeValue(oStr);
							    hasChanged = false;
						    }
						    textStringNode = textStringNode.getNextSibling();
					    }
				    }
			    }
		    }

		    if(iBuffer.mimeType.equals(CommandEngine.MIME_BUFFERTYPE)) {
			    TransformerFactory tf = TransformerFactory.newInstance();
			    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			    Transformer t = null;
			    try {
				    t = tf.newTransformer();
				    t.transform(new DOMSource(doc), new StreamResult(outStream));
			    } catch (Exception ex) {
				    workspace.log("An error occurred while reconstructing the XML file after substituteVariables call: " + ex.getMessage());
				    return (false);
			    }
			    iBuffer.data = outStream.toByteArray();
		    } else {
			    iBuffer.data = doc;
		    }
		} catch (Exception ex) {
			workspace.log("An error occurred while doing variable substitutions: " + ex.getMessage());
			return (false);
		}

		return (true);
	}
}
