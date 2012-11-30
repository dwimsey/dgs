/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DGS.dgs4j.ProcessingEngine.CommandEnginePlugins.Batik;

import DGS.dgs4j.ProcessingEngine.ProcessingWorkspace;
import org.apache.batik.transcoder.TranscodingHints;
import org.apache.batik.transcoder.keys.StringKey;
import javax.print.*;
import java.awt.print.*;
/**
 *
 * @author dwimsey
 */
public class DGSPrinterTranscoder extends org.apache.batik.transcoder.print.PrintTranscoder {
    
        public static final TranscodingHints.Key KEY_PRINTER_NAME = new StringKey();
	public DGSPrinterTranscoder(ProcessingWorkspace workspace) {
		super();
		this.userAgent = new DGSUserAgent(this.userAgent, workspace);
	}
}
