/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dgspreviewer;

import ImageProcessor.*;
import ImageProcessor.ProcessingEngine.*;

import javax.swing.*;

import java.io.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

/**
 *
 * @author dwimsey
 */
public class DGSPreviewerLoadImageWorker extends SwingWorker<DGSResponseInfo, Void>{
	DGSPreviewerView mainWin;
	ImageProcessor.ProcessingEngine.ProcessingEngine pEngine;
	String templateImageFileName;
	String dgsPackageFileName;

	public DGSPreviewerLoadImageWorker(DGSPreviewerView mainWindow, ImageProcessor.ProcessingEngine.ProcessingEngine npEngine, String imageFileName, String packageFileName) {
		this.mainWin = mainWindow;
		this.templateImageFileName = imageFileName;
		this.dgsPackageFileName = packageFileName;
		this.pEngine = npEngine;
    }
    
    @Override
    protected DGSResponseInfo doInBackground() throws Exception {
		setProgress(0);
        String outputMimeType = "image/png";

        DGSRequestInfo dgsRequestInfo = new DGSRequestInfo();
        dgsRequestInfo.continueOnError = true;

        setStatusMessage(200, "Reading image file: " + this.templateImageFileName);
        DGSFileInfo templateFileInfo = loadImageFileData(this.templateImageFileName);
		if(this.isCancelled()) { // this check is done after any possibly lengthy operation
			return(null);
		}
        if(templateFileInfo == null) {
            setStatusMessage(10, "Load aborted due to errors: " + this.templateImageFileName);
            return(null);
        }
		setProgress(5);

		DGSFileInfo replacementImages[] = null;

		DGSPackage dPkg = new DGSPackage();
		if((this.dgsPackageFileName == null) || (this.dgsPackageFileName.length() == 0)) {
			dgsRequestInfo.files = new DGSFileInfo[1];
			dgsRequestInfo.variables = null;
		} else {
			if(dPkg.loadFile(this.dgsPackageFileName)) {
				setProgress(10);
				if(this.isCancelled()) { // this check is done after any possibly lengthy operation
					return(null);
				}

				if(dPkg.files!=null && (dPkg.files.length>0)) {
					dgsRequestInfo.files = new DGSFileInfo[dPkg.files.length+1];
					for(int i = 0; i<dPkg.files.length; i++) {
						dgsRequestInfo.files[i+1] = dPkg.files[i];
						if(this.isCancelled()) { // this check is done after any possibly lengthy operation
							return(null);
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
		if(this.isCancelled()) { // this check is done after any possibly lengthy operation
			return(null);
		}

		dgsRequestInfo.files[0] = templateFileInfo;
        if((dgsRequestInfo.files[0].name == null) || (dgsRequestInfo.files[0].name.length() == 0)) {
			dgsRequestInfo.files[0].name = "input.svg"; // we need this set to Something, so set it ourselves
		}
//        if((dgsRequestInfo.files[0].mimeType == null) || (dgsRequestInfo.files[0].mimeType.length() == 0)) {
			dgsRequestInfo.files[0].mimeType = "image/svg+xml"; // we only process svg for now
//		}

		setProgress(12);
		// Form the instruction xml fragment
        //dgsRequestInfo.instructionsXML = "<commands><load filename=\"" + dgsRequestInfo.files[0].name + "\" buffer=\"" + dPkg.templateBuffer + "\" mimeType=\"image/svg+xml\" />";
	dgsRequestInfo.instructionsXML = "<commands><load filename=\"" + dgsRequestInfo.files[0].name + "\" buffer=\"main\" mimeType=\"image/svg+xml\" />";
//		dgsRequestInfo.instructionsXML += dPkg.commandString;
        dgsRequestInfo.instructionsXML += "<save ";
		if((dPkg.animationDuration>0.0f) && (dPkg.animationFramerate>0.0f)) {
//			dgsRequestInfo.instructionsXML += "animationDuration=\"" + dPkg.animationDuration + "\" animationFramerate=\"" + dPkg.animationFramerate + "\" ";
		}
		
		dgsRequestInfo.instructionsXML += "filename=\"output.png\" buffer=\"main\" mimeType=\"" + outputMimeType + "\" /></commands>";

		setProgress(13);
		if(this.isCancelled()) { // this check is done after any possibly lengthy operation
			return(null);
		}
        ProcessingWorkspace workspace = new ProcessingWorkspace(dgsRequestInfo);
		setStatusMessage(150, "Performing DGS Request ...");
        if(this.isCancelled()) { // this check is done after any possibly lengthy operation
			return(null);
		}
        setProgress(15);
        DGSResponseInfo dgsResponseInfo = pEngine.processCommandString(workspace);
        if(this.isCancelled()) { // this check is done after any possibly lengthy operation
			return(null);
		}
        setProgress(95);
        setStatusMessage(150, " Request completed.");
        
        this.logMessage(200, "DGS Request Log: ");
        for(int i = 0; i < dgsResponseInfo.processingLog.length; i++) {
            this.logMessage(200, "     " + dgsResponseInfo.processingLog[i]);
			if(this.isCancelled()) { // this check is done after any possibly lengthy operation
				return(null);
			}
		}
        this.logMessage(200, "-- END DGS Request Log --");
		setProgress(97);
        if(dgsResponseInfo.resultFiles.length == 0) {
            setStatusMessage(10, "No image files were returned by the processing engine, this generally indicates an error in the input file: " + this.templateImageFileName);
        }
		setProgress(99);
        return(dgsResponseInfo);
    }
    
    @Override
    protected void done() {
		if(this.isCancelled()) {
			return;
		}
		DGSResponseInfo dgsResponseInfo;
		try {
			dgsResponseInfo = this.get();
		} catch (InterruptedException ex) {
			return;
		} catch (java.util.concurrent.ExecutionException ex) {
			setStatusMessage(5, "An exception occurred during processing: " + ex.getMessage());
			return;
		}
		if(dgsResponseInfo == null || dgsResponseInfo.resultFiles == null || dgsResponseInfo.resultFiles.length == 0) {
			// nothing to work with anyway
			return;
		}
		setStatusMessage(200, "Updating display with new image ...");
        BufferedImage image = null;
        try {
            image = ImageIO.read(new java.io.ByteArrayInputStream((byte[])dgsResponseInfo.resultFiles[0].data));
        } catch (IOException ie) {
            setStatusMessage(5, "Error processing output image: " + ie.getMessage());
        }

		setProgress(99);
		setDisplayImage(image);
		setProgress(100);
		setStatusMessage(0, "Ready.");
	}
	
	private void setDisplayImage(BufferedImage nImage) {
		// if we've been canceled, is possible that the mainWin is no longer valid, abort to be safe
		if(this.isCancelled()) {
			return;
		}
        final BufferedImage image  = nImage;
		javax.swing.SwingUtilities.invokeLater(new Runnable() { public void run() { mainWin.setDisplayImage(image); } } );
	}

	private void setStatusMessage(int LogLevel, String LogMessage) {
		// if we've been canceled, is possible that the mainWin is no longer valid, abort to be safe
		if(this.isCancelled()) {
			return;
		}
        final String lm  = LogMessage.intern();
		final int ll = LogLevel;
		javax.swing.SwingUtilities.invokeLater(new Runnable() { public void run() { mainWin.setStatusMessage(ll, lm); } } );
	}
	
	private void logMessage(int LogLevel, String LogMessage) {
		// if we've been canceled, is possible that the mainWin is no longer valid, abort to be safe
		if(this.isCancelled()) {
			return;
		}
        final String lm  = LogMessage.intern();
		final int ll = LogLevel;
		javax.swing.SwingUtilities.invokeLater(new Runnable() { public void run() {mainWin.logMessage(ll, lm); } });
	}

	private DGSFileInfo loadImageFileData(String fileName)
    {
        // if we've been canceled, is possible that the mainWin is no longer valid, abort to be safe
		if(this.isCancelled()) {
			return(null);
		}
        byte fDat[] = null;
        java.io.File f = new java.io.File(fileName);
        if(!f.exists()) {
            setStatusMessage(10, "File does not exist: " + fileName);
            return(null);
        }
        try {
            fDat = fileToBytes(fileName);
        } catch (FileNotFoundException fex) {
            setStatusMessage(10, "Could not find the specified file: " + fileName);
            return(null);
        } catch (IOException iex) {
            setStatusMessage(10, "Could not read the specified file: " + fileName + " Error: " + iex.getMessage());
            return(null);
        }
        if(fDat == null) {
            setStatusMessage(5, "An unknown error occurred reading file: " + fileName);
            return(null);
        }
        if(fDat.length == 0) {
            setStatusMessage(10, "The specified file is empty: " + fileName);
            return(null);
        }
        DGSFileInfo fInfo = new DGSFileInfo();
        fInfo.data = fDat;
        fInfo.name = f.getName();
        fInfo.width = -1;
        fInfo.height = -1;
        return(fInfo);
    }

	private byte[] fileToBytes(String fileName) throws FileNotFoundException, IOException
    {
        // if we've been canceled, is possible that the mainWin is no longer valid, abort to be safe
		if(this.isCancelled()) {
			return(null);
		}
        byte fDat[] = new byte[0];

        FileInputStream fs = new FileInputStream(fileName);
        int i = fs.available();
        fDat = new byte[i];
        i = fs.read(fDat);
        fs.close();
        return(fDat);
    }
}
