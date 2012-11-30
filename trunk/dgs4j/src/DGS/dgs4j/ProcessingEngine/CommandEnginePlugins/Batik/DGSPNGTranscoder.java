/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DGS.dgs4j.ProcessingEngine.CommandEnginePlugins.Batik;

import DGS.dgs4j.ProcessingEngine.ProcessingWorkspace;

/**
 *
 * @author dwimsey
 */
public class DGSPNGTranscoder extends org.apache.batik.transcoder.image.PNGTranscoder {

	public DGSPNGTranscoder(ProcessingWorkspace workspace) {
		super();
		this.userAgent = new DGSUserAgent(this.getUserAgent(), workspace);
	}
}
