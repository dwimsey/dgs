/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rtsz.dgs4j.ProcessingEngine.Instructions;

import com.rtsz.dgs4j.ProcessingEngine.*;
import org.w3c.dom.*;

/**
 *
 * @author dwimsey
 */
public interface IInstruction {

	public boolean process(ProcessingWorkspace workspace, Node instructionNode);
}
