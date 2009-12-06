/*
 * DGSPreviewCanvas.java
 *
 * Created on March 30, 2009, 3:35 PM
 */
package dgspreviewer;

import java.awt.*;
import java.beans.*;
import com.rtsz.dgs4j.ProcessingEngine.*;
import dgspreviewer.DGSPreviewCanvasLoaderWorker;
import org.apache.batik.swing.svg.*;

/**
 *
 * @author  dwimsey
 */
public class DGSPreviewCanvas extends javax.swing.JPanel implements java.awt.event.ComponentListener {

	public interface NotificationMethods {
		public void logEvent(int LogLevel, String Message);
		public void statusMessage(int LogLevel, String Message);
		public void propertyChangeNotification(PropertyChangeEvent evt);
	}

	public enum DisplayMode {
		Draft,
		PNG,
		GIF,
		JPEG,
		TIFF,
		PDF,
		Printer;
	};
	private static SVGUserAgent defaultUserAgent = null;
	private SVGUserAgent userAgent = null;
	private DisplayMode displayMode;
	private Color backgroundColor;
	private String lastLoadedURI = null;
	private String lastLoadedDGSPackageURI = null;
	protected NotificationMethods notificationMethods = null;
	private java.beans.PropertyChangeListener pcListener = null;
	private DGSPreviewCanvasLoaderWorker workerThread = null;
	public ProcessingEngine pEngine = null;

	/** Creates new form DGSPreviewCanvas */
	public DGSPreviewCanvas() {
		this.displayMode = DisplayMode.GIF;
		this.backgroundColor = new Color(0xFFFFFFFF);
		if(defaultUserAgent == null) {
			// create the single default instance of the user agent
			defaultUserAgent = new DGSPreviewCanvasUserAgent(this);
			//defaultUserAgent = new org.apache.batik.swing.svg.SVGUserAgentGUIAdapter(this);
		}
		userAgent = defaultUserAgent;
		this.initComponents();
		this.addComponentListener(this);
		// do the initial layer ordering
		this.updateLayers();
	}

	private void updateLayers() {
		switch (displayMode) {
			case Draft:
				this.layers.setLayer(renderedCanvas, 0);
				this.layers.setLayer(draftCanvas, 1);
				break;
			case PNG:
			case GIF:
			case JPEG:
			case TIFF:
				this.layers.setLayer(draftCanvas, 0);
				this.layers.setLayer(renderedCanvas, 1);
				break;
			case PDF:
			case Printer:
				break;
		}
	}

	private synchronized boolean cancelWorker(boolean mayInterruptIfNeeded) {
		if (workerThread != null) {
			if (!workerThread.isDone()) {
				workerThread.cancel(mayInterruptIfNeeded);
				return (true);
			}
			workerThread = null;
		}
		return (false);
	}

	public void loadUri(String fileUri, String dgsPackageFile, NotificationMethods newMethods) {
		this.cancelWorker(true);
		lastLoadedURI = fileUri;
		lastLoadedDGSPackageURI = dgsPackageFile;
		if(newMethods == null) {
			notificationMethods = new dgspreviewer.DGSPreviewCanvas.NotificationMethods() {
					@Override
					public void logEvent(int LogLevel, String Message)
					{
					}
					@Override
					public void statusMessage(int LogLevel, String Message)
					{
					}
					@Override
					public void propertyChangeNotification(PropertyChangeEvent evt)
					{
					}
				};
		} else {
			notificationMethods = newMethods;
		}
		if (lastLoadedURI == null) {
			renderedCanvas.image = null;
			renderedCanvas.repaint();
			draftCanvas.setURI(null);
			return;
		}
		workerThread = new DGSPreviewCanvasLoaderWorker(this, this.notificationMethods, pEngine, fileUri, dgsPackageFile, this.getDisplayMode(), null);
		if (pcListener == null) {
			pcListener = new java.beans.PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					notificationMethods.propertyChangeNotification(evt);
				}
			};
		}
		workerThread.addPropertyChangeListener(pcListener);
		workerThread.execute();
	}

	public static void printUri(String fileUri, String dgsPackageFile, ProcessingEngine pEngine, NotificationMethods newMethods) {
		//fileUri;
		//dgsPackageFile;
		if(newMethods == null) {
			newMethods = new dgspreviewer.DGSPreviewCanvas.NotificationMethods() {
				@Override
				public void logEvent(int LogLevel, String Message)
				{
				}
				@Override
				public void statusMessage(int LogLevel, String Message)
				{
				}
				@Override
				public void propertyChangeNotification(PropertyChangeEvent evt)
				{
				}
			};
		}
		final dgspreviewer.DGSPreviewCanvas.NotificationMethods notificationMethods = newMethods;
		DGSPreviewCanvasLoaderWorker wThread = null;
		wThread = new DGSPreviewCanvasLoaderWorker(null, notificationMethods, pEngine, fileUri, dgsPackageFile, DisplayMode.Printer, null);
		java.beans.PropertyChangeListener printerPcListener = null;
		if (printerPcListener == null) {
			printerPcListener = new java.beans.PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					notificationMethods.propertyChangeNotification(evt);
				}
			};
		}
		wThread.addPropertyChangeListener(printerPcListener);
		wThread.execute();
	}

	public static void exportUriAs(String fileUri, String dgsPackageFile, ProcessingEngine pEngine, NotificationMethods newMethods, DisplayMode exportType, String outputFile) {
		if(exportType == DisplayMode.Draft) {			
			throw new java.lang.IllegalArgumentException("OutputFormat can not be draft when exporting to a file.");
		}
		if(exportType == DisplayMode.Printer) {			
			throw new java.lang.IllegalArgumentException("OutputFormat can not be draft when exporting to a file.");
		}
		if(outputFile == null) {
			throw new java.lang.IllegalArgumentException("outputFilename is null.  You must specify a filename to export the file to.");
		}
		if(outputFile.length() == 0) {
			throw new java.lang.IllegalArgumentException("outputFilename is blank.  You must specify a filename to export the file to.");
		}
		if(newMethods == null) {
			newMethods = new dgspreviewer.DGSPreviewCanvas.NotificationMethods() {
				@Override
				public void logEvent(int LogLevel, String Message)
				{
				}
				@Override
				public void statusMessage(int LogLevel, String Message)
				{
				}
				@Override
				public void propertyChangeNotification(PropertyChangeEvent evt)
				{
				}
			};
		}
		final dgspreviewer.DGSPreviewCanvas.NotificationMethods notificationMethods = newMethods;
		DGSPreviewCanvasLoaderWorker wThread = null;
		wThread = new DGSPreviewCanvasLoaderWorker(null, notificationMethods, pEngine, fileUri, dgsPackageFile, exportType, outputFile);
		java.beans.PropertyChangeListener printerPcListener = null;
		if (printerPcListener == null) {
			printerPcListener = new java.beans.PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					notificationMethods.propertyChangeNotification(evt);
				}
			};
		}
		wThread.addPropertyChangeListener(printerPcListener);
		wThread.execute();
	}

	public DisplayMode getDisplayMode() {
		return (this.displayMode);
	}

	public void setDisplayMode(DisplayMode newMode) {
		if(newMode == DisplayMode.Printer) {
			throw new java.lang.IllegalArgumentException("DisplayMode.Printer is only used when printing a document.");
		}
		if (newMode != this.displayMode) {
			this.cancelWorker(true);
			this.displayMode = newMode;
			this.updateLayers();
			renderedCanvas.image = null;
			draftCanvas.setDocument(null);
			if (lastLoadedURI != null) {
				// refresh the loaded image
				this.loadUri(lastLoadedURI, lastLoadedDGSPackageURI, notificationMethods);
			}
		}
	}

	public java.awt.Color getBackgroundColor() {
		return (this.backgroundColor);
	}

	public void setBackgroundColor(java.awt.Color newColor) {
		this.backgroundColor = newColor;
		if(this.backgroundColor == null) {
			this.backgroundColor = new java.awt.Color(0xFFFFFFFF);
		}
		this.renderedCanvas.setBackgroundColor(this.backgroundColor);
		this.draftCanvas.setBackground(this.backgroundColor);
		if(this.displayMode == DisplayMode.Draft) {
			// we hide and redisplay the canvas to force the color update
			this.draftCanvas.setVisible(false);
			this.draftCanvas.setVisible(true);
		}
	}

	public void setUserAgent(SVGUserAgent newUa)
	{
		
	}

	public SVGUserAgent getUserAgent()
	{
		return(null);
	}

	@Override
	public void componentHidden(java.awt.event.ComponentEvent e) {
	}

	@Override
	public void componentMoved(java.awt.event.ComponentEvent e) {
	}

	@Override
	public void componentResized(java.awt.event.ComponentEvent e) {
		if(e.getSource() == this) {
			java.awt.Rectangle r = this.getBounds();
			draftCanvas.setBounds(r);
			renderedCanvas.setBounds(r);
		}
	}

	@Override
	public void componentShown(java.awt.event.ComponentEvent e) {
		
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        renderedCanvas = new dgspreviewer.DGSPreviewerPanel();
        layers = new javax.swing.JLayeredPane();
        draftCanvas = new org.apache.batik.swing.JSVGCanvas(userAgent, true, true);

        renderedCanvas.setName("renderedCanvas"); // NOI18N
        renderedCanvas.setPreferredSize(new java.awt.Dimension(32676, 32676));

        javax.swing.GroupLayout renderedCanvasLayout = new javax.swing.GroupLayout(renderedCanvas);
        renderedCanvas.setLayout(renderedCanvasLayout);
        renderedCanvasLayout.setHorizontalGroup(
            renderedCanvasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 32676, Short.MAX_VALUE)
        );
        renderedCanvasLayout.setVerticalGroup(
            renderedCanvasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 32676, Short.MAX_VALUE)
        );

        setName("DGSPreviewCanvas"); // NOI18N
        setPreferredSize(new java.awt.Dimension(32676, 32676));

        layers.setName("layers"); // NOI18N
        layers.setPreferredSize(new java.awt.Dimension(32676, 32676));

        draftCanvas.setName("draftCanvas"); // NOI18N

        javax.swing.GroupLayout draftCanvasLayout = new javax.swing.GroupLayout(draftCanvas);
        draftCanvas.setLayout(draftCanvasLayout);
        draftCanvasLayout.setHorizontalGroup(
            draftCanvasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 200, Short.MAX_VALUE)
        );
        draftCanvasLayout.setVerticalGroup(
            draftCanvasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 200, Short.MAX_VALUE)
        );

        renderedCanvas.setBounds(0, 0, -1, -1);
        layers.add(renderedCanvas, javax.swing.JLayeredPane.DEFAULT_LAYER);
        draftCanvas.setBounds(0, 0, -1, -1);
        layers.add(draftCanvas, javax.swing.JLayeredPane.DEFAULT_LAYER);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(layers, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(layers, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    protected org.apache.batik.swing.JSVGCanvas draftCanvas;
    private javax.swing.JLayeredPane layers;
    protected dgspreviewer.DGSPreviewerPanel renderedCanvas;
    // End of variables declaration//GEN-END:variables
}
