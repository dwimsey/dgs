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
public class DGSPDFTranscoder extends org.apache.fop.svg.PDFTranscoder {

	public DGSPDFTranscoder(ProcessingWorkspace workspace) {
		super();
		this.userAgent = new DGSUserAgent(this.userAgent, workspace);
	}
}
