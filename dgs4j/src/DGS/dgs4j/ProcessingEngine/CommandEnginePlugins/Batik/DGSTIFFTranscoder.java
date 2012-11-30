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
public class DGSTIFFTranscoder extends org.apache.batik.transcoder.image.TIFFTranscoder {

	public DGSTIFFTranscoder(ProcessingWorkspace workspace) {
		super();
		this.userAgent = new DGSUserAgent(this.getUserAgent(), workspace);
	}
}
