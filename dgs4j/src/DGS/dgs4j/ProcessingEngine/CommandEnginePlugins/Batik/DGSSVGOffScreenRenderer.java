package DGS.dgs4j.ProcessingEngine.CommandEnginePlugins.Batik;

import java.awt.geom.AffineTransform;
import java.awt.Rectangle;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.svg12.SVG12BridgeContext;
import org.apache.batik.bridge.BridgeException;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UpdateManager;
import org.apache.batik.bridge.UpdateManagerAdapter;
import org.apache.batik.bridge.UpdateManagerEvent;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.util.RunnableQueue;

import org.apache.batik.gvt.CanvasGraphicsNode;
import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.renderer.ConcreteImageRendererFactory;
import org.apache.batik.gvt.renderer.ImageRenderer;
import org.apache.batik.gvt.renderer.ImageRendererFactory;

import org.apache.batik.anim.*;

import org.apache.batik.dom.svg12.*;
import org.apache.batik.dom.svg.*;

import org.w3c.dom.Document;
import java.awt.image.*;
import java.util.*;

public class DGSSVGOffScreenRenderer {

	static final String SVGNS = "http://www.w3.org/2000/svg";
	Document document;
	UserAgent userAgent;
	GVTBuilder builder;
	BridgeContext ctx;
	ImageRenderer renderer;
	AffineTransform curTxf;
	UpdateManager manager;
	GraphicsNode gvtRoot;
	int DISPLAY_WIDTH = 1280;
	int DISPLAY_HEIGHT = 1024;
	ArrayList<BufferedImage> imageList = null;

	public DGSSVGOffScreenRenderer(Document doc, UserAgent ua) {
		userAgent = ua;
		// Create the bridge context based on the DOM model presented to us
		if ((doc instanceof SVG12OMDocument)) {
			ctx = new SVG12BridgeContext(userAgent);
		} else if ((doc instanceof SVGOMDocument)) {
			ctx = new BridgeContext(userAgent);
		} else {
			// We should throw an exception here since we probably are going to
			// break, but we'll try anyway
			ctx = new BridgeContext(userAgent);
		}
		builder = new GVTBuilder();
		document = doc;
		imageList = new java.util.ArrayList();
	}
	private AnimationEngine ae = null;

	public void init() {
		GraphicsNode gvtRoot = null;

		try {
			ctx.setDynamicState(BridgeContext.DYNAMIC);
			gvtRoot = builder.build(ctx, document);
			ae = ctx.getAnimationEngine();
		} catch (BridgeException e) {
			e.printStackTrace();
			System.exit(1);
		}

		ImageRendererFactory rendererFactory;
		rendererFactory = new ConcreteImageRendererFactory();
		renderer = rendererFactory.createDynamicImageRenderer();
		renderer.setDoubleBuffered(false);

		float docWidth = (float) ctx.getDocumentSize().getWidth();
		float docHeight = (float) ctx.getDocumentSize().getHeight();
		int iWidth = new Float(docWidth).intValue();
		int iHeight = new Float(docHeight).intValue();

//		float xscale = DISPLAY_WIDTH / docWidth;
//		float yscale = DISPLAY_HEIGHT / docHeight;
//		float scale = Math.min(xscale, yscale);

//		AffineTransform px = AffineTransform.getScaleInstance(scale, scale);

//		double tx = -0 + (DISPLAY_WIDTH / scale - docWidth) / 2;
//		double ty = -0 + (DISPLAY_WIDTH / scale - docHeight) / 2;
//		px.translate(tx, ty);
		CanvasGraphicsNode cgn = getGraphicsNode(gvtRoot);
//		if (cgn != null) {
//			cgn.setViewingTransform(px);
//			curTxf = new AffineTransform();
//		} else {
//			curTxf = px;
//		}
		manager = new UpdateManager(ctx, gvtRoot, document);
		// 'setMinRepaintTime' was added to SVN.  This isn't
		// essential but prevents 'frame skipping' (useful
		// for "recording" content, not needed for live display).
		manager.setMinRepaintTime(-1);

		try {
			manager.dispatchSVGLoadEvent();
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		}

		renderer.updateOffScreen(iWidth, iHeight);
		renderer.setTree(gvtRoot);
//		renderer.setTransform(curTxf);
		renderer.clearOffScreen();
		renderer.repaint(new Rectangle(0, 0, iWidth, iHeight));
		manager.addUpdateManagerListener(new UpdateManagerAdapter() {

			@Override
			public void updateStarted(UpdateManagerEvent e) {
				//render(e.getImage());
				int i = 0;
				i++;
			}

			@Override
			public void updateCompleted(UpdateManagerEvent e) {
				render(e.getImage());
			}

			@Override
			public void updateFailed(UpdateManagerEvent e) {
//				render(e.getImage());
				int i = 0;
				i++;
			}

			@Override
			public void managerSuspended(UpdateManagerEvent e) {
				// Make sure pending updates are completed.
				//System.exit(0);
//				this.
			}
		});
		manager.manageUpdates(renderer);
		this.gvtRoot = gvtRoot;
	}

	private CanvasGraphicsNode getGraphicsNode(GraphicsNode gn) {
		if (!(gn instanceof CompositeGraphicsNode)) {
			return null;
		}
		CompositeGraphicsNode cgn = (CompositeGraphicsNode) gn;
		List children = cgn.getChildren();
		if (children.size() == 0) {
			return null;
		}
		gn = (GraphicsNode) children.get(0);
		if (!(gn instanceof CanvasGraphicsNode)) {
			return null;
		}
		return (CanvasGraphicsNode) gn;
	}

	public void render(BufferedImage img) {
		if (imageList != null) {
			imageList.add(img);
		}
	}

	public static BufferedImage[] buildFrames2(Document svgDoc, UserAgent ua, float startTime, float timeStep, float frameCount) {
		DGSSVGOffScreenRenderer render = new DGSSVGOffScreenRenderer(svgDoc, ua);
		render.init();
//		RunnableQueue rq = render.manager.getUpdateRunnableQueue();
		final AnimationEngine aniEng = render.ae;
		aniEng.pause();
		aniEng.setCurrentTime(0);
		render.renderer.setDoubleBuffered(true);
		for (int i = 1; i < frameCount; i++) {
			final float snapshotTime = (startTime + (i * timeStep));
//			try {
			aniEng.setCurrentTime(snapshotTime);
			render.renderer.updateOffScreen(300, 300);
			render.imageList.add(render.renderer.getOffScreen());
//			} catch (InterruptedException ie) {
//				ie.printStackTrace();
//			}
		}

		BufferedImage[] oBuf = new BufferedImage[render.imageList.size()];
		render.imageList.toArray((BufferedImage[]) oBuf);
		return (oBuf);
	}

	public static BufferedImage[] buildFrames(Document svgDoc, UserAgent ua, float startTime, float timeStep, float frameCount) {
		DGSSVGOffScreenRenderer render = new DGSSVGOffScreenRenderer(svgDoc, ua);
		render.init();
		RunnableQueue rq = render.manager.getUpdateRunnableQueue();

		final AnimationEngine aniEng = render.ae;
		aniEng.pause();
		aniEng.setCurrentTime(0);
		float tt = aniEng.getCurrentTime();
		tt = tt + 0.0f;
		for (int i = 1; i < frameCount; i++) {
			final float snapshotTime = (startTime + (i * timeStep));
			//final int offsetX = (300 - (i*20));
			try {
				rq.invokeAndWait(new Runnable() {

					@Override
					public void run() {
						// set the new snapshot time
//						rect.setAttributeNS(null, "x", "" + offsetX);
						aniEng.setCurrentTime(snapshotTime);
//						rn.repaint(new Rectangle(0, 0, 300, 300));
						try {
//						java.lang.Thread.currentThread().sleep((long)100);
						} catch (Exception ex) {
						}
					}
				});
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
		}
//		render.manager.suspend();

		BufferedImage[] oBuf = new BufferedImage[render.imageList.size()];
		render.imageList.toArray((BufferedImage[]) oBuf);
		return (oBuf);
	}
}

