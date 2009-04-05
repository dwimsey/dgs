/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rtsz.dgs4j.ProcessingEngine.CommandEnginePlugins.Batik;

import com.rtsz.dgs4j.ProcessingEngine.*;

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
