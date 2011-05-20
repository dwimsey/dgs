/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rtsz.dgs4j.ProcessingEngine;

import com.rtsz.dgs4j.*;
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

	private static class ThreadLocalWorkspace extends ThreadLocal {
	}
	private static ThreadLocalWorkspace activeWorkspace = new ThreadLocalWorkspace();

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

	public void log(String logString) {
		processingLog.add(logString);
	}

	public void logException(String logString, Throwable ex) {
		StackTraceElement[] stack = ex.getStackTrace();
		processingLog.add(logString);
		for(int i = 0; i < stack.length; i++) {
			processingLog.add("\t\t" + stack[i].toString());
		}
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
