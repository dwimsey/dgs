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
public class DGSJPEGTranscoder extends org.apache.batik.transcoder.image.JPEGTranscoder {

	public DGSJPEGTranscoder(ProcessingWorkspace workspace, float quality) {
		super();
		this.hints.put(KEY_QUALITY, quality);

		this.userAgent = new DGSUserAgent(this.userAgent, workspace);
	}
}
