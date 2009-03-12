/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ImageProcessor.ProcessingEngine.CommandEnginePlugins.Batik;

/**
 *
 * @author dwimsey
 */
public class DGSGIFTranscoder extends org.apache.batik.transcoder.image.PNGTranscoder {
	private ImageProcessor.ProcessingEngine.ProcessingWorkspace workspace;
	public DGSGIFTranscoder(ImageProcessor.ProcessingEngine.ProcessingWorkspace wrkspc) {
		super();
		this.workspace = wrkspc;
		this.hints.put(this.KEY_EXECUTE_ONLOAD, true);
		this.hints.put(this.KEY_ALLOWED_SCRIPT_TYPES, ""); // "text/ecmascript");
		this.userAgent = new DGSUserAgent(this.getUserAgent(), wrkspc);
	}
	
	public void writeImage(java.awt.image.BufferedImage img, org.apache.batik.transcoder.TranscoderOutput output) {
		try {
			if(!javax.imageio.ImageIO.write(img, "gif", output.getOutputStream())) {
				workspace.log("DGSGIFTranscoder: PNG Image could not be converted to GIF format.");
			}
		} catch(java.io.IOException ioex) {
			workspace.log("DGSGIFTranscoder: PNG Image could not be converted to GIF format.  Error: " + ioex.getMessage());
		}
	}
}
