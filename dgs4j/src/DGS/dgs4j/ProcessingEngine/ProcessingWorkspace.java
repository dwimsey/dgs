/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DGS.dgs4j.ProcessingEngine;

import DGS.dgs4j.DGSRequestInfo;
import DGS.dgs4j.DGSFileInfo;
import DGS.dgs4j.DGSResponseInfo;
import java.util.*;

/**
 *
 * @author dwimsey
 */
public class ProcessingWorkspace {

	public static final String DGSNSREF_DC = "http://purl.org/dc/elements/1.1/";
	public static final String DGSNSREF_CC = "http://creativecommons.org/ns#";
	public static final String DGSNSREF_RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	public static final String DGSNSREF_SVG = "http://www.w3.org/2000/svg";
	public static final String DGSNSREF_XMLNS = "http://www.w3.org/2000/svg";
	public static final String DGSNSREF_XLINK = "http://www.w3.org/1999/xlink";
	public static final String DGSNSREF_SODIPODI = "http://sodipodi.sourceforge.net/DTD/sodipodi-0.dtd";
	public static final String DGSNSREF_INKSCAPE = "http://www.inkscape.org/namespaces/inkscape";
	public static final String DGSNSREF_DGSIMAGE = "http://www.rtsz.com/ns/dgs";
	public static final String DGSWORKSPACE_URL_STYLESHEET = "workspace://workspace.css";

	private static ThreadLocal<ProcessingWorkspace> activeWorkspace = new InheritableThreadLocal<ProcessingWorkspace>();

	public static ProcessingWorkspace getCurrentWorkspace() {
		return ((ProcessingWorkspace) activeWorkspace.get());
	}

	public static void setCurrentWorkspace(ProcessingWorkspace workspace) {
		activeWorkspace.set(workspace);
	}
	public DGSRequestInfo requestInfo;
	private List<ProcessingEngineImageBuffer> images;
	private List<String> processingLog;
	public List<DGSFileInfo> files;
	public String activeStylesheet;

	public ProcessingWorkspace(DGSRequestInfo iRequestInfo) {
		this.requestInfo = iRequestInfo;
		this.processingLog = new ArrayList<String>();
		this.images = new ArrayList<ProcessingEngineImageBuffer>();
		this.files = new ArrayList<DGSFileInfo>();
		this.activeStylesheet = null;
	}

	public void logEx(int logValue, String logString) {
		if (logValue < 256) {
			processingLog.add(logString);
		}
	}

	public void logFatal(String logString) {
		logEx(0, logString);
	}

	public void logError(String logString) {
		logEx(50, logString);
	}

	public void logWarning(String logString) {
		logEx(100, logString);
	}

	public void logInfo(String logString) {
		logEx(200, logString);
	}

	public void logDebug(String logString) {
		logEx(256, logString);
	}

	public void logException(String logString, Throwable ex) {
		StackTraceElement[] stack = ex.getStackTrace();
		this.logError(logString);
		for (int i = 0; i < stack.length; i++) {
			this.logError("\t\t" + stack[i].toString());
		}
	}

	public String getLog() {
		return ("");
	}

	public DGSResponseInfo generateResultInfo() {
		DGSResponseInfo dgsResponse = new DGSResponseInfo();
		Date now = new Date();
		this.logInfo("Workspace results generated at: " + now.toString());
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
		this.logDebug("Creating new image buffer (" + name + ") at  " + now.toString());
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
