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
public class DGSTIFFTranscoder extends org.apache.batik.transcoder.image.TIFFTranscoder {

	public DGSTIFFTranscoder(ProcessingWorkspace workspace) {
		super();
		this.userAgent = new DGSUserAgent(this.getUserAgent(), workspace);
	}
}
