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
public class DGSPDFTranscoder extends org.apache.fop.svg.PDFTranscoder {

	public DGSPDFTranscoder(ProcessingWorkspace workspace) {
		super();
		this.userAgent = new DGSUserAgent(this.userAgent, workspace);
	}
}
