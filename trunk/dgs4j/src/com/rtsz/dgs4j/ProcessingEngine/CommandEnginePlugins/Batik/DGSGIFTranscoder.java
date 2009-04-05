/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ImageProcessor.ProcessingEngine.CommandEnginePlugins.Batik;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import org.apache.batik.ext.awt.image.GraphicsUtil;
import org.apache.batik.dom.util.DOMUtilities;
import org.apache.batik.transcoder.*;
import org.apache.batik.util.ParsedURL;
import org.w3c.dom.*;
import org.apache.batik.gvt.renderer.ConcreteImageRendererFactory;
import org.apache.batik.gvt.renderer.ImageRendererFactory;
import org.apache.batik.transcoder.image.ImageTranscoder.*;
import org.apache.batik.transcoder.image.*;
import java.awt.geom.*;
import java.awt.image.*;
import org.apache.batik.gvt.renderer.ImageRenderer;
import org.apache.batik.dom.svg12.*;
import java.awt.geom.AffineTransform;
import org.apache.batik.dom.svg.*;
import org.w3c.dom.Document;

//import gif4free.*;
/**
 *
 * @author dwimsey
 */
public class DGSGIFTranscoder extends org.apache.batik.transcoder.SVGAbstractTranscoder {

	private ImageProcessor.ProcessingEngine.ProcessingWorkspace workspace;

	public DGSGIFTranscoder(ImageProcessor.ProcessingEngine.ProcessingWorkspace wrkspc) {
		super();
		// gifs need a white transparent background to render in IE
		hints.put(ImageTranscoder.KEY_FORCE_TRANSPARENT_WHITE, Boolean.TRUE);
		this.userAgent = new DGSUserAgent(this.userAgent, wrkspc);
		this.workspace = wrkspc;
	}

	/**
	 * Transcodes the specified Document as an image in the specified output.
	 *
	 * @param document the document to transcode
	 * @param uri the uri of the document or null if any
	 * @param output the ouput where to transcode
	 * @exception TranscoderException if an error occured while transcoding
	 */
	protected void transcode(Document document,
			String uri,
			TranscoderOutput output)
			throws TranscoderException {

		try {
			// do we need to generate an animated GIF?
			TranscoderOutput oto = null;
			float animationStartTime = 45.0f;
			float animationRepeatDelay = 2.0f;
			float animationTimeStep = 0.2f;
			int animationFrameCount = 1;
			int animationRepeatCount = 1;

			if (!this.hints.containsKey(CommandEngine.KEY_ANIMATION_ENABLED)) {
				animationRepeatDelay = 0.0f;
				animationTimeStep = 0.0f;
				animationFrameCount = 1;
				animationRepeatCount = 1;
			}

			if (animationFrameCount < 1) {
				// invalid frame count, produce 1 frame instead
				animationRepeatDelay = 0.0f;
				animationTimeStep = 0.0f;
				animationFrameCount = 1;
				animationRepeatCount = 1;
				animationFrameCount = 1;
			}

			// create the gif stream
			java.io.ByteArrayOutputStream gifStream = new java.io.ByteArrayOutputStream();
			com.fmsware.gif4free.AnimatedGifEncoder e = new com.fmsware.gif4free.AnimatedGifEncoder();
			e.start(gifStream);
			if (animationFrameCount > 1) {
				// if the frame count is 1 or 0 then we only display the initial frame
				if (animationRepeatCount != 1) {
					e.setRepeat(animationRepeatCount);
				}
				e.setDelay(new Float(animationTimeStep * 1000).intValue());
			} else {
				animationRepeatCount = 1;
				animationFrameCount = 1;
				animationTimeStep = 0.0f;
			}

			for (int i = 0; i < animationFrameCount; i++) {
				if((i+1) == animationFrameCount) {
					// last frame, are we repeating?
					if(animationRepeatCount != 1) {
						// we're repeating, is the repeat delay different from the standard frame delay?
						if(animationRepeatDelay != animationTimeStep) {
							// the last frame has a different delay, set it now
							e.setDelay(new Float(animationRepeatDelay * 1000).intValue());
						}
					}
				}
				e.addFrame(getBufferedImageForSnapshotTime(document, uri, animationStartTime + (i * animationTimeStep)));
			}

			e.finish();
			gifStream.flush();
			gifStream.close();
			gifStream.writeTo(output.getOutputStream());

//			java.io.FileOutputStream fs = new java.io.FileOutputStream("C:\\test.gif");
//			fs.write(gifStream.toByteArray());
//			fs.flush();
//			fs.close();
//			fs = null;
		} catch (Exception ex) {
			throw new TranscoderException("GIF image creation failed.  Error: " + ex.getMessage(), ex);
		}
	}

	private BufferedImage getBufferedImageForSnapshotTime(Document document, String uri, float snapshotTime) throws TranscoderException {
		float ts = 0.0f;
		if (document != null) {
			DOMImplementation impl;
			if (document instanceof SVG12OMDocument) {
				impl = SVG12DOMImplementation.getDOMImplementation();
			} else if ((document instanceof SVGOMDocument)) {
				impl = SVGDOMImplementation.getDOMImplementation();
			} else {
				// try the standard implementation and hope for the best
				impl = (DOMImplementation) hints.get(KEY_DOM_IMPLEMENTATION);
			}
			document = DOMUtilities.deepCloneDocument(document, impl);
			if (uri != null) {
				ParsedURL url = new ParsedURL(uri);
				((SVGOMDocument) document).setParsedURL(url);
			}
		}
		super.hints.remove(KEY_SNAPSHOT_TIME);
		super.hints.put(KEY_SNAPSHOT_TIME, snapshotTime);
		super.hints.remove(ImageTranscoder.KEY_FORCE_TRANSPARENT_WHITE);
		super.hints.put(ImageTranscoder.KEY_FORCE_TRANSPARENT_WHITE, true);
		super.transcode(document, uri, null); // this transcoder doesn't write anything to output so we pass in null

		// prepare the image to be painted
		int w = (int) (width + 0.5);
		int h = (int) (height + 0.5);

		// paint the SVG document using the bridge package
		// create the appropriate renderer
		ImageRenderer renderer = createRenderer();
		renderer.updateOffScreen(w, h);
		// curTxf.translate(0.5, 0.5);
		renderer.setTransform(curTxf);
		renderer.setTree(this.root);
		this.root = null; // We're done with it...

		try {
			// now we are sure that the aoi is the image size
			Shape raoi = new Rectangle2D.Float(0, 0, width, height);
			// Warning: the renderer's AOI must be in user space
			renderer.repaint(curTxf.createInverse().
					createTransformedShape(raoi));
			BufferedImage rend = renderer.getOffScreen();
			renderer = null; // We're done with it...

			BufferedImage dest = createImage(w, h);

			Graphics2D g2d = GraphicsUtil.createGraphics(dest);
			if (hints.containsKey(ImageTranscoder.KEY_BACKGROUND_COLOR)) {
				Paint bgcolor = (Paint) hints.get(ImageTranscoder.KEY_BACKGROUND_COLOR);
				g2d.setComposite(AlphaComposite.SrcOver);
				g2d.setPaint(bgcolor);
				g2d.fillRect(0, 0, w, h);
			}
			if (rend != null) { // might be null if the svg document is empty
				g2d.drawRenderedImage(rend, new AffineTransform());
			}
			g2d.dispose();
			rend = null; // We're done with it...
			return (dest);
		} catch (Exception ex) {
			throw new TranscoderException(ex);
		}
	}

	/**
	 * Method so subclasses can modify the Renderer used to render document.
	 */
	protected ImageRenderer createRenderer() {
		ImageRendererFactory rendFactory = new ConcreteImageRendererFactory();
		// ImageRenderer renderer = rendFactory.createDynamicImageRenderer();
		return rendFactory.createStaticImageRenderer();
	}

	/**
	 * Creates a new image with the specified dimension.
	 * @param width the image width in pixels
	 * @param height the image height in pixels
	 */
	protected BufferedImage createImage(int width, int height) {
		return (new java.awt.image.BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB));
	}
}