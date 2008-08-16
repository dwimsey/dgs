/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ImageProcessor.ProcessingEngine;

import ImageProcessor.*;
import java.util.*;

/**
 *
 * @author dwimsey
 */
public class ProcessingWorkspace {

	public DGSRequestInfo requestInfo;
	private List<ProcessingEngineImageBuffer> images;
	private List<String> processingLog;
	public List<DGSFileInfo> files;

	public ProcessingWorkspace(DGSRequestInfo iRequestInfo) {
		this.requestInfo = iRequestInfo;
		this.processingLog = new ArrayList<String>();
		this.images = new ArrayList<ProcessingEngineImageBuffer>();
		this.files = new ArrayList<DGSFileInfo>();
	}

	public void log(String logString) {
		processingLog.add(logString);
	}

	public String getLog() {
		return ("");
	}

	public DGSResponseInfo generateResultInfo() {
		DGSResponseInfo dgsResponse = new DGSResponseInfo();
		Date now = new Date();
		this.processingLog.add("Workspace results generated at: " + now.toString());
		dgsResponse.processingLog = new String[this.processingLog.size()];
		for (int i = 0; i < this.processingLog.size(); i++) {
			dgsResponse.processingLog[i] = this.processingLog.get(i);
		}
		dgsResponse.resultFiles = new DGSFileInfo[this.files.size()];
		for (int i = 0; i < this.files.size(); i++) {
			dgsResponse.resultFiles[i] = this.files.get(i);
		}
		return (dgsResponse);
	}

	public ProcessingEngineImageBuffer getImageBuffer(String name) {
		for (int i = 0; i < images.size(); i++) {
			if (name.equals(images.get(i).name)) {
				return (images.get(i));
			}
		}
		return (null);
	}

	public ProcessingEngineImageBuffer createImageBuffer(String name) {
		for (int i = 0; i < images.size(); i++) {
			if (name.equals(images.get(i).name)) {
				return (images.get(i));
			}
		}
		Date now = new Date();
		this.log("Creating new image buffer (" + name + ") at  " + now.toString());
		ProcessingEngineImageBuffer buffer = new ProcessingEngineImageBuffer();
		buffer.name = name.intern();
		buffer.height = -1;
		buffer.width = -1;
		buffer.mimeType = "";
		buffer.data = null;
		images.add(buffer);
		return (buffer);
	}
}
