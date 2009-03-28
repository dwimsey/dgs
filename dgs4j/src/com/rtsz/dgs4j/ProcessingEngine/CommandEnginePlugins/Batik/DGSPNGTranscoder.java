/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ImageProcessor.ProcessingEngine.CommandEnginePlugins.Batik;

/**
 *
 * @author dwimsey
 */
public class DGSPNGTranscoder extends org.apache.batik.transcoder.image.PNGTranscoder {

	public DGSPNGTranscoder(ImageProcessor.ProcessingEngine.ProcessingWorkspace workspace) {
		super();
		this.userAgent = new DGSUserAgent(this.getUserAgent(), workspace);
	}
}
