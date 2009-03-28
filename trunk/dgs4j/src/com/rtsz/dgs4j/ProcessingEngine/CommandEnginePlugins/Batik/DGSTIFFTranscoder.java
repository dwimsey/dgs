/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ImageProcessor.ProcessingEngine.CommandEnginePlugins.Batik;

/**
 *
 * @author dwimsey
 */
public class DGSTIFFTranscoder extends org.apache.batik.transcoder.image.TIFFTranscoder {

	public DGSTIFFTranscoder(ImageProcessor.ProcessingEngine.ProcessingWorkspace workspace) {
		super();
		this.userAgent = new DGSUserAgent(this.getUserAgent(), workspace);
	}
}
