/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ImageProcessor.ProcessingEngine.CommandEnginePlugins.Batik;

import org.apache.batik.bridge.*;

/**
 *
 * @author dwimsey
 */
public class DGSPNGTranscoder extends org.apache.batik.transcoder.image.PNGTranscoder {
    public DGSPNGTranscoder()
    {
        super();
        this.hints.put(this.KEY_EXECUTE_ONLOAD, true);
        this.hints.put(this.KEY_ALLOWED_SCRIPT_TYPES, "text/ecmascript");
        this.userAgent = new DGSUserAgent(this.getUserAgent());
    }
}
