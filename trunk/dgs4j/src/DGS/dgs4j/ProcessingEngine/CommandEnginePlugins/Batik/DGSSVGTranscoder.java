/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DGS.dgs4j.ProcessingEngine.CommandEnginePlugins.Batik;

import DGS.dgs4j.ProcessingEngine.ProcessingEngineImageBuffer;
import DGS.dgs4j.ProcessingEngine.ProcessingWorkspace;
import DGS.dgs4j.ProcessingEngine.Base64;

import org.apache.batik.transcoder.*;

import org.w3c.dom.*;

import java.io.IOException;
//import java.io.ByteArrayOutputStream;

import org.apache.batik.dom.svg.*;
import org.apache.batik.util.XMLResourceDescriptor;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

/**
 *
 * @author dwimsey
 */
public class DGSSVGTranscoder extends SVGAbstractTranscoder {
	ProcessingWorkspace cWorkspace;
	public DGSSVGTranscoder(ProcessingWorkspace workspace) {
		super();
		cWorkspace = workspace;
		this.userAgent = new DGSUserAgent(this.userAgent, cWorkspace);
	}

	private String convertWorkspace2DataURL(String wsUrl)
	{
		String rValue = "";
		ProcessingEngineImageBuffer ib = cWorkspace.getImageBuffer(wsUrl);
		if (ib == null) {
			cWorkspace.logFatal("ERROR: Workspace file not found: " + wsUrl);
		} else {
			try {
				rValue = "data://" + ib.mimeType + ";base64,";
				rValue += Base64.encodeBytes((byte[])ib.data);
			} catch (Throwable t) {
				cWorkspace.logFatal("ERROR: could not create empty embedded data stream for workspace URL: " + t.getMessage());
				rValue = "data://text;base64,";
				rValue += Base64.encodeBytes(" ".getBytes());
			}
		}

		return(rValue);
	}

	void ReplaceWorkspaceURL(Node cNode)
	{
		org.w3c.dom.NamedNodeMap aList;
		if(cNode.getNodeType() == cNode.PROCESSING_INSTRUCTION_NODE) {
			try {
				boolean foundHref = false;
				org.w3c.dom.ProcessingInstruction p = (org.w3c.dom.ProcessingInstruction)cNode;
				String s = p.getData();
				String s1;
				String s2;
				String ss = null;
				int e;
				int o;
				o = s.indexOf("href=\"workspace");
				if(o > -1) {
					cWorkspace.logFatal("ERROR: URL still contains workspace prefix?");
					s1 = s.substring(0, o) + "href=\"";
					ss = s.substring(o+16);
					
					if(ss.startsWith("//")) {
						ss = ss.substring(2);
					}
					int oo = ss.indexOf("\"");
					if(oo > -1) {
						// only continue if we find the closing quote too
						s2 = ss.substring(oo);
						ss = ss.substring(0, oo);
						s = s1 + convertWorkspace2DataURL(ss) + s2;
						foundHref = true;
					}
				} else {
					o = s.indexOf("href=\"");
					if(o > -1) {
						o += 6;
						s1 = s.substring(0, o);
						ss = s.substring(o);
						e = ss.indexOf("\"");
						if(e > -1) {
							ss = s.substring(o, o+e);
							s2 = s.substring(o+e);
							s = s1 + convertWorkspace2DataURL(ss) + s2;
							foundHref = true;
						}
					}
				}
				
				if(foundHref) {
					p.setData(s);
				}
			} catch(Throwable t) {
				cWorkspace.logFatal("ERROR: Could not patch processing instruction URL");
			}
		} else {
			if(cNode.hasAttributes()) {
				aList = cNode.getAttributes();
				Node aNode = aList.getNamedItemNS(null, "href");
				String aValue;
				if(aNode != null) {
					aValue = aNode.getTextContent();
					if(aValue != null && aValue.startsWith("workspace:")) {
						aValue = aValue.substring(10);
						if(aValue.startsWith(("//"))) {
							aValue = aValue.substring(2);
						}
						aNode.setTextContent(convertWorkspace2DataURL(aValue));

					}
				}

				aNode = aList.getNamedItemNS(null, "src");
				if(aNode != null) {
					aValue = aNode.getTextContent();
					if(aValue != null && aValue.startsWith("workspace:")) {
						aValue = aValue.substring(10);
						if(aValue.startsWith(("//"))) {
							aValue = aValue.substring(2);
						}
						aNode.setTextContent(convertWorkspace2DataURL(aValue));

					}
				}
			}
		}

		Node childNode = cNode.getFirstChild();
		while(childNode != null) {
			ReplaceWorkspaceURL(childNode);
			childNode = childNode.getNextSibling();
		}
	}

	private void transcodeDoc(Document node) {
		// we don't do anything yet, the workspace url must be supported first

		Node cNode = node.getFirstChild();
		while(cNode != null) {
			ReplaceWorkspaceURL(cNode);
			cNode = cNode.getNextSibling();
		}
	}

	public void transcode(TranscoderInput input, TranscoderOutput output) throws TranscoderException {
		java.io.OutputStream outStream = output.getOutputStream();
		// get the input data type
		java.io.InputStream inStream = input.getInputStream();
		Document svgDoc = input.getDocument();
		if (svgDoc == null) {
			if (inStream != null) {
				try {
					byte[] data;
					byte[] tdata;
					int blockSize = 8192;
					int bytesRead = 0;
					int bytesStored = 0;
					data = new byte[blockSize];
					bytesRead = inStream.read(data, 0, blockSize);
					while (bytesRead > 0) {
						bytesStored += bytesRead;
						tdata = new byte[bytesStored + blockSize];
						System.arraycopy(data, 0, tdata, 0, bytesStored);
						data = tdata;
						bytesRead = inStream.read(data, bytesStored, blockSize);
					}
					if (bytesStored > 0) {
						// resize the block one last time, truncating unused space
						tdata = new byte[bytesStored];
						System.arraycopy(data, 0, tdata, 0, bytesStored);
						data = tdata;
					}
					String parser = XMLResourceDescriptor.getXMLParserClassName();
					SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
					svgDoc = f.createSVGDocument(null, new java.io.StringReader((String) new String(data, "UTF8")));
				} catch (IOException ex) {
					throw new TranscoderException("An error occurred parsing the SVG buffer data.", ex);
				}
				if (svgDoc == null) {
					throw new TranscoderException("InputStream DOM creation returned null.");
				}
			} else {
				// no svgDoc or byte stream, can't do anything with this.
				throw new TranscoderException("The TranscoderInput specified does not contain a valid document or input stream.");
			}
		}

		super.transcode(input, output);
		// at this point, svgDoc should always point to a valid SVGDocument object
		// we need to convert any links to the workspace into something useful outside of
		// the workspace, so convert the URLs to use the data protocol instead of the
		// internal workspace.
		transcodeDoc(svgDoc);

		// Now convert the document into an encoded byte stream.
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer t = null;
		try {
			t = tf.newTransformer();
			t.transform(new DOMSource((Document) svgDoc), new StreamResult(outStream));
		} catch (TransformerConfigurationException ex) {
			throw new TranscoderException(ex);
		} catch (TransformerException ex) {
			throw new TranscoderException(ex);
		}
	}
}
// this code just passes the document through as is
/*			int blockSize = 8192;
byte[] data = new byte[blockSize];
try {
int bytesRead = inStream.read(data, 0, blockSize);
while(bytesRead>0) {
outStream.write(data, 0, bytesRead);
bytesRead = inStream.read(data, 0, blockSize);
}
} catch(java.io.IOException ex) {
throw new TranscoderException(ex);
}
 */
