/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DGS.dgs4j.ProcessingEngine.CommandEnginePlugins.Batik;

import DGS.dgs4j.ProcessingEngine.ProcessingEngineImageBuffer;
import DGS.dgs4j.ProcessingEngine.ProcessingWorkspace;
import DGS.dgs4j.DGSFileInfo;

import org.apache.batik.util.*;
import java.util.*;
import java.io.*;

/**
 *
 * @author dwimsey
 */
public class DGSWorkspaceParsedURLData extends org.apache.batik.util.ParsedURLData {

	private ProcessingWorkspace workspace = null;
	private String url = null;

	public DGSWorkspaceParsedURLData(ParsedURL baseUrl, String urlStr) {
		url = urlStr;
		this.protocol = "workspace";
		if (urlStr.startsWith("workspace://")) {
			this.path = urlStr.substring(12);
		} else if (urlStr.startsWith("workspace:")) {
			this.path = urlStr.substring(10);
		} else {
			this.path = urlStr;
		}

		workspace = ProcessingWorkspace.getCurrentWorkspace();
		if (workspace != null) {
		}
	}

	public String toString() {
		return("workspace://" + this.path);
	}

	public boolean complete() {
		return (true);
	}
// boolean	complete() // Returns true if the URL looks well formed and complete.
// boolean	equals(Object obj) // Implement Object.equals for ParsedURLData.
//protected  void	extractContentTypeParts(String userAgent) // Extracts the type/subtype and charset parameter from the Content-Type header.
/*
	String	getContentEncoding(String userAgent)
	Returns the content encoding if available.
	String	getContentType(String userAgent)
	Returns the content type if available.
	String	getContentTypeCharset(String userAgent)
	Returns the content type's charset parameter, if available.
	String	getContentTypeMediaType(String userAgent)
	Returns the content type's type/subtype, if available.
	String	getPortStr()
	Returns the URL up to and include the port number on the host.
	String	getPostConnectionURL()
	Returns the URL that was ultimately used to fetch the resource represented by the ParsedURL.
	boolean	hasContentTypeParameter(String userAgent, String param)
	Returns whether the Content-Type header has the given parameter.
	InputStream	openStream(String userAgent, Iterator mimeTypes)
	Open the stream and check for common compression types.
	protected  InputStream	openStreamInternal(String userAgent, Iterator mimeTypes, Iterator encodingTypes)

	InputStream	openStreamRaw(String userAgent, Iterator mimeTypes)
	Open the stream and returns it.
	protected  boolean	sameFile(ParsedURLData other)

	String	toString()				// Return a string representation of the data.
	 */

	public InputStream openStream(String userAgent, Iterator mimeTypes) // Open the stream and check for common compression types.
	{
		return (this.openStreamInternal(userAgent, mimeTypes, null));
	}

	protected InputStream openStreamInternal(String userAgent, Iterator mimeTypes, Iterator encodingTypes) {
		InputStream inStream = null;
		if (workspace == null) {
			return (inStream);
		}


		if (this.path.equals("workspace.css")) {
			if (workspace.activeStylesheet == null) {
				inStream = new java.io.ByteArrayInputStream(new byte[0]);
			} else {
				try {
					inStream = new java.io.ByteArrayInputStream((byte[]) workspace.activeStylesheet.getBytes("utf-8"));
				} catch (Throwable t) {
					workspace.log(("ERROR: could not create input stream for default stylesheet: " + t.getMessage()));
					inStream = new java.io.ByteArrayInputStream(new byte[0]);
				}
			}
		} else {
			ProcessingEngineImageBuffer ib = workspace.getImageBuffer(this.path);
			if (ib == null) {
				return (inStream);
			}

			inStream = new java.io.ByteArrayInputStream((byte[]) ib.data);
		}
		return (inStream);
	}

	protected InputStream openStreamInternal2(String userAgent, Iterator mimeTypes, Iterator encodingTypes) {
		InputStream inStream = null;
		if (workspace == null) {
			return (inStream);
		}
		if (workspace.files == null) {
			return (inStream);
		}
		int count = workspace.files.size();
		if (count == 0) {
			return (null);
		}
		DGSFileInfo fInfo;
		for (int i = 0; i < count; i++) {
			fInfo = workspace.files.get(i);
			if (fInfo != null) {
				if (fInfo.name != null && fInfo.name.length() > 0) {
					if (fInfo.equals(this.path)) {
						// we have a matching file, return it.
						inStream = new java.io.ByteArrayInputStream(fInfo.data);
						return (inStream);
					}
				}
			}
		}
		return (inStream);
	}
}
