/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rtsz.dgs4j.ProcessingEngine.CommandEnginePlugins.Batik;

import com.rtsz.dgs4j.ProcessingEngine.*;

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
public class DGSSVGTranscoder extends AbstractTranscoder {

	public DGSSVGTranscoder(ProcessingWorkspace workspace) {
		super();
		//this.userAgent = new DGSUserAgent(this.getUserAgent(), workspace);
	}
	
	private void transcodeNode(Object node)
	{
		// we don't do anything yet, the workspace url must be supported first
	}

	public void transcode(TranscoderInput input, TranscoderOutput output) throws TranscoderException
	{
		java.io.OutputStream outStream = output.getOutputStream();
		// get the input data type
		java.io.InputStream inStream = input.getInputStream();
		Document svgDoc = input.getDocument();
		if(svgDoc == null) {
			if(inStream != null) {
				try {
					byte[] data;
					byte[] tdata;
					int blockSize = 8192;
					int bytesRead = 0;
					int bytesStored = 0;
					data = new byte[blockSize];
					bytesRead = inStream.read(data, 0, blockSize);
					while(bytesRead>0) {
						bytesStored += bytesRead;
						tdata = new byte[bytesStored + blockSize];
						System.arraycopy(data, 0, tdata, 0, bytesStored);
						data = tdata;
						bytesRead = inStream.read(data, bytesStored, blockSize);
					}
					if(bytesStored > 0) {
						// resize the block one last time, truncating unused space
						tdata = new byte[bytesStored];
						System.arraycopy(data, 0, tdata, 0, bytesStored);
						data = tdata;
					}
					String parser = XMLResourceDescriptor.getXMLParserClassName();
					SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
					svgDoc = f.createSVGDocument(null, new java.io.StringReader((String)new String(data, "UTF8")));
				} catch (IOException ex) {
					throw new TranscoderException("An error occurred parsing the SVG buffer data.", ex);
				}
				if(svgDoc == null) {
					throw new TranscoderException("InputStream DOM creation returned null.");
				}
			} else {
				// no svgDoc or byte stream, can't do anything with this.
				throw new TranscoderException("The TranscoderInput specified does not contain a valid document or input stream.");
			}
		}

		// at this point, svgDoc should always point to a valid SVGDocument object
		// we need to convert any links to the workspace into something useful outside of 
		// the workspace, so convert the URLs to use the data protocol instead of the
		// internal workspace.
		transcodeNode(svgDoc.getFirstChild());
		
		// Now convert the document into an encoded byte stream.
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer t = null;
		try {
			t = tf.newTransformer();
			t.transform(new DOMSource((Document)svgDoc), new StreamResult(outStream));
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
