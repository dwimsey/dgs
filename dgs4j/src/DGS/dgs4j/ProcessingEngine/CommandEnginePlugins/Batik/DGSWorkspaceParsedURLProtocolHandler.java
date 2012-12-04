/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DGS.dgs4j.ProcessingEngine.CommandEnginePlugins.Batik;

import DGS.dgs4j.ProcessingEngine.ProcessingEngineImageBuffer;
import DGS.dgs4j.ProcessingEngine.ProcessingWorkspace;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import org.apache.batik.util.ParsedURL;
import org.apache.batik.util.ParsedURLData;
import org.apache.batik.util.AbstractParsedURLProtocolHandler;

public class DGSWorkspaceParsedURLProtocolHandler extends AbstractParsedURLProtocolHandler {

//    static final String BASE64 = "base64";
    static final String CHARSET = "charset";

    public DGSWorkspaceParsedURLProtocolHandler() {
        super("workspace");
    }

    public ParsedURLData parseURL(String urlStr) {
        return parseURL(null, urlStr);
    }

    public ParsedURLData parseURL(ParsedURL baseURL, String urlStr) {
        DGSWorkspaceParsedURLData ret = new DGSWorkspaceParsedURLData();

        int pidx=0, idx;
        int len = urlStr.length();

        // Pull fragment id off first...
        idx = urlStr.indexOf('#');
        ret.ref = null;
        if (idx != -1) {
            if (idx + 1 < len) {
                ret.ref = urlStr.substring(idx + 1);
            }
            urlStr = urlStr.substring(0, idx);
            len = urlStr.length();
        }

        idx = urlStr.indexOf(':');
        if (idx != -1) {
            // May have a protocol spec...
            ret.protocol = urlStr.substring(pidx, idx);
            if (ret.protocol.indexOf('/') == -1) {
                pidx = idx+1;
				idx = urlStr.indexOf("/", pidx);
				if((urlStr.indexOf("/", pidx) == pidx) && (urlStr.indexOf("/", (pidx+1)) == (pidx+1))) {
					pidx += 2;
				}
			} else {
                // Got a slash in protocol probably means 
                // no protocol given, (host and port?)
                ret.protocol = null;
                pidx = 0;
            }
        }
/*
		idx = urlStr.indexOf(',',pidx);
        if ((idx != -1) && (idx != pidx)) {
            ret.host = urlStr.substring(pidx, idx);
            pidx = idx+1;

            int aidx = ret.host.lastIndexOf(';');
            if ((aidx == -1) || (aidx==ret.host.length())) {
                ret.contentType = ret.host;
            } else {
                String enc = ret.host.substring(aidx+1);
                idx = enc.indexOf('=');
                if (idx == -1) {
                    // It is an encoding.
                    ret.contentEncoding = enc;
                    ret.contentType = ret.host.substring(0, aidx);
                } else {
                    ret.contentType = ret.host;
                }
                // if theres a charset pull it out.
                aidx = 0;
                idx = ret.contentType.indexOf(';', aidx);
                if (idx != -1) {
                    aidx = idx+1;
                    while (aidx < ret.contentType.length()) {
                        idx = ret.contentType.indexOf(';', aidx);
                        if (idx == -1) idx = ret.contentType.length();
                        String param = ret.contentType.substring(aidx, idx);
                        int eqIdx = param.indexOf('=');
                        if ((eqIdx != -1) &&
                            (CHARSET.equals(param.substring(0,eqIdx)))) 
                            ret.charset = param.substring(eqIdx+1);
                        aidx = idx+1;
                    }
                }
            }
        }
*/        
        if (pidx < urlStr.length()) {
            ret.path = urlStr.substring(pidx);
        }

        return ret;
    }

    /**
     * Overrides some of the methods to support data protocol weirdness
     */
    static class DGSWorkspaceParsedURLData extends ParsedURLData {

		private ProcessingWorkspace workspace = null;
		private String url = null;
		String charset;

		public boolean complete() {
            return path != null;
        }

        public String getPortStr() {
            String portStr = "workspace://";
            if (host != null) {
                portStr += host;
				portStr += ",";
            }
            return portStr;
        }
                
        public String toString() {
            String ret = getPortStr();
            if (path != null) {
                ret += path;
            }
            if (ref != null) {
                ret += '#' + ref;
            }
            return ret;
        }

        /**
         * Returns the content type if available.  This is only available
         * for some protocols.
         */
        public String getContentType(String userAgent) {
            return contentType;
        }

        /**
         * Returns the content encoding if available.  This is only available
         * for some protocols.
         */
        public String getContentEncoding(String userAgent) {
            return contentEncoding;
        }
		
		protected InputStream openStreamInternal(String userAgent, Iterator mimeTypes, Iterator encodingTypes) {
			InputStream inStream = null;
			if (workspace == null) {
				return (inStream);
			}

			ProcessingEngineImageBuffer ib = workspace.getImageBuffer(this.path);
			if (ib == null) {
				return (inStream);
			}

			inStream = new java.io.ByteArrayInputStream((byte[]) ib.data);
			return (inStream);
		}
    }
}
