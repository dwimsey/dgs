/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ImageProcessor.ProcessingEngine.CommandEnginePlugins.Batik;

/**
 *
 * @author dwimsey
 */
public class DGSJPEGTranscoder extends org.apache.batik.transcoder.image.JPEGTranscoder {

	public DGSJPEGTranscoder(ImageProcessor.ProcessingEngine.ProcessingWorkspace workspace, float quality) {
		super();
		this.hints.put(this.KEY_QUALITY, quality);

		this.userAgent = new DGSUserAgent(this.userAgent, workspace);
	}
}
