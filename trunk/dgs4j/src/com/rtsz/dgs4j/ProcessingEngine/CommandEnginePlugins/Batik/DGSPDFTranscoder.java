/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ImageProcessor.ProcessingEngine.CommandEnginePlugins.Batik;

/**
 *
 * @author dwimsey
 */
public class DGSPDFTranscoder extends org.apache.fop.svg.PDFTranscoder {

	public DGSPDFTranscoder(ImageProcessor.ProcessingEngine.ProcessingWorkspace workspace) {
		super();
		this.userAgent = new DGSUserAgent(this.userAgent, workspace);
	}
}
