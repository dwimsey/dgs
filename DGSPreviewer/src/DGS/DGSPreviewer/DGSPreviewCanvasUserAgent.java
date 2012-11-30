/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DGS.DGSPreviewer;

import org.apache.batik.util.*;
import org.apache.batik.swing.svg.*;
import org.apache.batik.swing.svg.SVGUserAgent.*;

/**
 *
 * @author dwimsey
 */
public class DGSPreviewCanvasUserAgent extends SVGUserAgentAdapter {

	private DGSPreviewCanvas previewCanvas = null;

	public DGSPreviewCanvasUserAgent(DGSPreviewCanvas targetCanvas) {
		previewCanvas = targetCanvas;
	}

	@Override
	public void displayError(Exception ex) {
		previewCanvas.notificationMethods.logEvent(0, "Processing Error: " + ex.getClass().getCanonicalName() + ": " + ex.getMessage());
		ex.fillInStackTrace();
		StackTraceElement st[] = ex.getStackTrace();

		for (int i = 0; i < st.length; i++) {
			previewCanvas.notificationMethods.logEvent(0, " -> " + st[i].toString());
		}
		previewCanvas.notificationMethods.statusMessage(0, "SVG Display Error: " + ex.getMessage());
	}

	@Override
	public void displayMessage(String message) {
		previewCanvas.notificationMethods.logEvent(5, message);
	}

	@Override
	public void checkLoadExternalResource(ParsedURL arg0, ParsedURL arg1) {
		super.checkLoadExternalResource(arg0, arg1);
	}

	@Override
	public void checkLoadScript(String arg0, ParsedURL arg1, ParsedURL arg2) {
		super.checkLoadScript(arg0, arg1, arg1);
	}
}
