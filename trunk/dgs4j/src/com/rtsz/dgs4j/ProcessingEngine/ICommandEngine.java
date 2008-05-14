/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ImageProcessor.ProcessingEngine;

import ImageProcessor.*;

import java.util.*;

import org.apache.xerces.parsers.*;
import org.xml.sax.*;
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
