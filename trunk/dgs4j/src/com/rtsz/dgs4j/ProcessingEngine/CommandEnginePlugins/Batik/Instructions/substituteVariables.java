/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ImageProcessor.ProcessingEngine.CommandEnginePlugins.Batik.Instructions;

import ImageProcessor.*;

import java.util.*;

import org.apache.xerces.parsers.*;
import org.xml.sax.*;
import org.w3c.dom.*;

/**
 *
 * @author dwimsey
 */
public class substituteVariables implements ImageProcessor.ProcessingEngine.Instructions.IInstruction {
    public boolean process(ImageProcessor.ProcessingEngine.ProcessingWorkspace workspace, Node instructionNode)
    {
        return(true);
    }
}
