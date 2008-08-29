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
		this.hints.put(this.KEY_EXECUTE_ONLOAD, true);
		this.hints.put(this.KEY_ALLOWED_SCRIPT_TYPES, ""); // "text/ecmascript");
		this.userAgent = new DGSUserAgent(this.userAgent, workspace);
	}
}
