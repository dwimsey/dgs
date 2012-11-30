/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DGS.DGSPreviewer;

import DGS.dgs4j.ProcessingEngine.ProcessingWorkspace;
import DGS.dgs4j.ProcessingEngine.ProcessingEngine;
import DGS.dgs4j.DGSResponseInfo;
import DGS.dgs4j.DGSPackage;
import DGS.dgs4j.DGSRequestInfo;
import DGS.dgs4j.DGSFileInfo;

import javax.swing.*;

import java.io.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.beans.*;
import DGS.DGSPreviewer.DGSPreviewCanvas.*;

/**
 *
 * @author dwimsey
 */
public class DGSPreviewCanvasLoaderWorker extends SwingWorker<DGSResponseInfo, Void> {

	DGSPreviewCanvas canvas;
	ProcessingEngine pEngine;
	String imageFilename;
	String previewPackageFilename;
	String exportFilename;
	private DisplayMode outputDisplayMode;
	private DGS.DGSPreviewer.DGSPreviewCanvas.NotificationMethods notificationMethods;

	public DGSPreviewCanvasLoaderWorker(DGSPreviewCanvas previewCanvas, NotificationMethods newMethods, ProcessingEngine npEngine, String imageFileName, String packageFileName, DisplayMode displayMode, String exportFile) {
		super();
		exportFilename = exportFile;
		if (exportFilename != null) {
			if (displayMode == DisplayMode.Draft) {
				throw new java.lang.IllegalArgumentException("DisplayMode can not be Draft when providing an export filename,");
			}
			if (displayMode == DisplayMode.Printer) {
				throw new java.lang.IllegalArgumentException("DisplayMode can not be Printer when providing an export filename,");
			}
		}

		canvas = previewCanvas;
		if (npEngine == null) {
			if (canvas != null) {
				if (canvas.pEngine == null) {
					throw new IllegalArgumentException("newMethods and canvas.pEngine arguments can not both be null.");
				}
				pEngine = canvas.pEngine;
			} else {
				throw new IllegalArgumentException("canvas and pEngine arguments can not both be null.");
			}
		} else {
			pEngine = npEngine;
		}
		imageFilename = imageFileName;
		previewPackageFilename = packageFileName;
		outputDisplayMode = displayMode;
		if (newMethods == null) {
			if (canvas != null) {
				notificationMethods = canvas.notificationMethods;
			} else {
				throw new IllegalArgumentException("canvas and newMethods arguments can not both be null.");
			}
		} else {
			notificationMethods = newMethods;
		}
		if (notificationMethods == null) {
			notificationMethods = new DGS.DGSPreviewer.DGSPreviewCanvas.NotificationMethods() {

				@Override
				public void logEvent(int LogLevel, String Message) {
				}

				@Override
				public void statusMessage(int LogLevel, String Message) {
				}

				@Override
				public void propertyChangeNotification(PropertyChangeEvent evt) {
				}
			};
		}
	}

	@Override
	protected DGSResponseInfo doInBackground() throws Exception {
		setProgress(0);
		String outputMimeType = null;
		String outputFileName = null;
		switch (outputDisplayMode) {
			case Printer:
				outputMimeType = "printer/printer";
				outputFileName = "chooser";
				break;
			case Draft:
				outputMimeType = "image/svg+xml";
				outputFileName = "output.svg";
				break;
			case PNG:
				outputMimeType = "image/png";
				outputFileName = "output.png";
				break;
			case GIF:
				outputMimeType = "image/gif";
				outputFileName = "output.gif";
				break;
			case JPEG:
				outputMimeType = "image/jpeg";
				outputFileName = "output.jpg";
				break;
			case TIFF:
				outputMimeType = "image/tiff";
				outputFileName = "output.tif";
				break;
			case PDF:
				outputMimeType = "application/pdf";
				outputFileName = "output.pdf";
				break;
		}

		DGSRequestInfo dgsRequestInfo = new DGSRequestInfo();
		dgsRequestInfo.continueOnError = true;

		this.notificationMethods.statusMessage(200, "Reading image file: " + this.imageFilename);
		DGSFileInfo templateFileInfo = loadImageFileData(this.imageFilename);
		if (this.isCancelled()) { // this check is done after any possibly lengthy operation
			return (null);
		}
		if (templateFileInfo == null) {
			if (outputDisplayMode == DisplayMode.Printer) {
				this.notificationMethods.statusMessage(10, "Printing aborted due to errors: " + this.imageFilename);
			} else {
				this.notificationMethods.statusMessage(10, "Load aborted due to errors: " + this.imageFilename);
			}
			return (null);
		}
		setProgress(5);

		DGSPackage dPkg = new DGSPackage();
		if ((this.previewPackageFilename == null) || (this.previewPackageFilename.length() == 0)) {
			dgsRequestInfo.files = new DGSFileInfo[1];
			dgsRequestInfo.variables = null;
		} else {
			if (dPkg.loadFile(this.previewPackageFilename)) {
				setProgress(10);
				if (this.isCancelled()) { // this check is done after any possibly lengthy operation
					return (null);
				}

				if (dPkg.files != null && (dPkg.files.length > 0)) {
					dgsRequestInfo.files = new DGSFileInfo[dPkg.files.length + 1];
					for (int i = 0; i < dPkg.files.length; i++) {
						dgsRequestInfo.files[i + 1] = dPkg.files[i];
						if (this.isCancelled()) { // this check is done after any possibly lengthy operation
							return (null);
						}
					}
				} else {
					dgsRequestInfo.files = new DGSFileInfo[1];
				}
				dgsRequestInfo.variables = dPkg.variables;
			} else {
				dgsRequestInfo.files = new DGSFileInfo[1];
				dgsRequestInfo.variables = null;
			}
		}
		setProgress(11);
		if (this.isCancelled()) { // this check is done after any possibly lengthy operation
			return (null);
		}

		dgsRequestInfo.files[0] = templateFileInfo;
		if ((dgsRequestInfo.files[0].name == null) || (dgsRequestInfo.files[0].name.length() == 0)) {
			dgsRequestInfo.files[0].name = "input.svg"; // we need this set to Something, so set it ourselves
		}
//       if((dgsRequestInfo.files[0].mimeType == null) || (dgsRequestInfo.files[0].mimeType.length() == 0)) {
		dgsRequestInfo.files[0].mimeType = "image/svg+xml"; // we only process svg for now
//		}

		setProgress(12);
		// Form the instruction xml fragment
		dgsRequestInfo.instructionsXML = "<commands><load filename=\"" + dgsRequestInfo.files[0].name + "\" buffer=\"" + dPkg.templateBuffer + "\" mimeType=\"image/svg+xml\" />";
		if (dPkg.commandString != null && dPkg.commandString.length() > 0) {
			dgsRequestInfo.instructionsXML += dPkg.commandString;
		} else {
			dgsRequestInfo.instructionsXML += "<substituteVariables buffer=\"main\" />";
		}
//		dgsRequestInfo.instructionsXML += "<addWatermark buffer=\"" + dPkg.templateBuffer + "\" srcImage=\"watermark\" opacity=\"0.05\"/>";
		dgsRequestInfo.instructionsXML += "<save ";
		if ((dPkg.animationDuration > 0.0f) && (dPkg.animationFramerate > 0.0f)) {
//			dgsRequestInfo.instructionsXML += "animationDuration=\"" + dPkg.animationDuration + "\" animationFramerate=\"" + dPkg.animationFramerate + "\" ";
		}

		dgsRequestInfo.instructionsXML += "filename=\"" + outputFileName + "\" buffer=\"" + dPkg.templateBuffer + "\" mimeType=\"" + outputMimeType + "\" /></commands>";
		setProgress(13);
		if (this.isCancelled()) { // this check is done after any possibly lengthy operation
			return (null);
		}
		ProcessingWorkspace workspace = new ProcessingWorkspace(dgsRequestInfo);
		this.notificationMethods.statusMessage(150, "Performing DGS Request ...");
		if (this.isCancelled()) { // this check is done after any possibly lengthy operation
			return (null);
		}
		setProgress(15);
		DGSResponseInfo dgsResponseInfo = pEngine.processCommandString(workspace);
		if (this.isCancelled()) { // this check is done after any possibly lengthy operation
			return (null);
		}
		setProgress(95);
		this.notificationMethods.statusMessage(150, " Request completed.");

		this.notificationMethods.logEvent(200, "DGS Request Log: ");
		for (int i = 0; i < dgsResponseInfo.processingLog.length; i++) {
			this.notificationMethods.logEvent(200, "     " + dgsResponseInfo.processingLog[i]);
			if (this.isCancelled()) { // this check is done after any possibly lengthy operation
				return (null);
			}
		}
		this.notificationMethods.logEvent(200, "-- END DGS Request Log --");
		if (outputDisplayMode != DisplayMode.Printer) {
			setProgress(97);
			if (dgsResponseInfo.resultFiles.length == 0) {
				this.notificationMethods.statusMessage(10, "No image files were returned by the processing engine, this generally indicates an error in the input file: " + this.imageFilename);
			}
		}
		setProgress(99);
		return (dgsResponseInfo);
	}

	@Override
	protected void done() {
		if (this.isCancelled()) {
			return;
		}
		DGSResponseInfo dgsResponseInfo;
		try {
			dgsResponseInfo = this.get();
		} catch (InterruptedException ex) {
			return;
		} catch (java.util.concurrent.ExecutionException ex) {
			this.notificationMethods.statusMessage(5, "An exception occurred during processing: " + ex.getMessage());
			return;
		}
		if (dgsResponseInfo == null || dgsResponseInfo.resultFiles == null || dgsResponseInfo.resultFiles.length == 0) {
			// nothing to work with anyway
			return;
		}

		if (outputDisplayMode == DisplayMode.Printer) {
			this.notificationMethods.statusMessage(200, "Image printed.");
		} else {
			this.notificationMethods.statusMessage(200, "Updating display with new image ...");
		}
		setProgress(99);
		org.w3c.dom.Document svgDoc;
		switch (outputDisplayMode) {

			case Printer:
				break;
			case Draft:
				try {
					String parser = org.apache.batik.util.XMLResourceDescriptor.getXMLParserClassName();
					org.apache.batik.dom.svg.SAXSVGDocumentFactory f = new org.apache.batik.dom.svg.SAXSVGDocumentFactory(parser);
					svgDoc = f.createSVGDocument(null, new java.io.StringReader((String) new String(((byte[]) dgsResponseInfo.resultFiles[0].data), "UTF8")));
					// TODO: If this is not set to http:// then the script engines seem to break and refuse to script the svg
					// a real cause and fix needs to be found
					svgDoc.setDocumentURI("http://localhost/workspace.svg");
				} catch (Exception ex) {
					this.notificationMethods.statusMessage(0, "An error occurred parsing the SVG data file data for display: " + ex.getMessage());
					return;
				}
				canvas.draftCanvas.setDocumentState(org.apache.batik.swing.JSVGCanvas.ALWAYS_DYNAMIC);
				canvas.draftCanvas.setDocument(svgDoc);
				canvas.draftCanvas.setVisible(true);
				canvas.draftCanvas.repaint();
				canvas.draftCanvas.setEnabled(true);
				break;
			case PNG:
			case GIF:
			case JPEG:
			case TIFF:
			case PDF:
				if (this.exportFilename == null) {
					// display new image
					if (outputDisplayMode == DisplayMode.PDF) {
						this.notificationMethods.statusMessage(0, "PDF Display output is not supported at this time.");
						setProgress(100);
						return;
					}
					BufferedImage image = null;
					try {
						image = ImageIO.read(new java.io.ByteArrayInputStream((byte[]) dgsResponseInfo.resultFiles[0].data));
					} catch (IOException ie) {
						this.notificationMethods.statusMessage(5, "Error processing output image: " + ie.getMessage());
					}
					canvas.renderedCanvas.image = image;
				} else {
					// save image data to filename provided
					java.io.FileOutputStream fs = null;
					try {
						fs = new java.io.FileOutputStream(this.exportFilename);
						try {
							fs.write(((byte[]) dgsResponseInfo.resultFiles[0].data));
						} catch (Throwable t) {
							this.notificationMethods.statusMessage(0, "Could not save file: " + t.getMessage());
						} finally {
							fs.close();
						}
					} catch (Throwable t) {
						this.notificationMethods.statusMessage(0, "Could not open file: " + t.getMessage());
					}

				}
				break;
		}
		setProgress(100);
		this.notificationMethods.statusMessage(0, "Ready.");
	}

	private DGSFileInfo loadImageFileData(String fileName) {
		// if we've been canceled, is possible that the mainWin is no longer valid, abort to be safe
		if (this.isCancelled()) {
			return (null);
		}
		byte fDat[] = null;
		java.io.File f = new java.io.File(fileName);
		if (!f.exists()) {
			this.notificationMethods.statusMessage(10, "File does not exist: " + fileName);
			return (null);
		}
		try {
			fDat = fileToBytes(fileName);
		} catch (FileNotFoundException fex) {
			this.notificationMethods.statusMessage(10, "Could not find the specified file: " + fileName);
			return (null);
		} catch (IOException iex) {
			this.notificationMethods.statusMessage(10, "Could not read the specified file: " + fileName + " Error: " + iex.getMessage());
			return (null);
		}
		if (fDat == null) {
			this.notificationMethods.statusMessage(5, "An unknown error occurred reading file: " + fileName);
			return (null);
		}
		if (fDat.length == 0) {
			this.notificationMethods.statusMessage(10, "The specified file is empty: " + fileName);
			return (null);
		}
		DGSFileInfo fInfo = new DGSFileInfo();
		fInfo.data = fDat;
		fInfo.name = f.getName();
		fInfo.width = -1;
		fInfo.height = -1;
		return (fInfo);
	}

	private byte[] fileToBytes(String fileName) throws FileNotFoundException, IOException {
		// if we've been canceled, is possible that the mainWin is no longer valid, abort to be safe
		if (this.isCancelled()) {
			return (null);
		}
		byte fDat[] = new byte[0];

		FileInputStream fs = new FileInputStream(fileName);
		int i = fs.available();
		fDat = new byte[i];
		i = fs.read(fDat);
		fs.close();
		return (fDat);
	}
}
