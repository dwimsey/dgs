/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rtsz.dgs4j.ProcessingEngine;

import org.w3c.dom.*;

/**
 *
 * @author dwimsey
 */
public interface ICommandEngine {

	public void init();

	public void addCommands(ProcessingEngine pEngine);

	public boolean load(ProcessingWorkspace workspace, String fileName, String bufferName, String outputType, NamedNodeMap attributes);

	public boolean save(ProcessingWorkspace workspace, String fileName, ProcessingEngineImageBuffer buffer, String outputType, NamedNodeMap attributes);
}
