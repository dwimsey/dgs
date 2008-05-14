/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ImageProcessor.ProcessingEngine.Instructions;

import ImageProcessor.*;

import java.util.*;

import org.apache.xerces.parsers.*;
import org.xml.sax.*;
import org.w3c.dom.*;

/**
 *
 * @author dwimsey
 */
public interface IInstruction {
    public boolean process(ImageProcessor.ProcessingEngine.ProcessingWorkspace workspace, Node instructionNode);
}
