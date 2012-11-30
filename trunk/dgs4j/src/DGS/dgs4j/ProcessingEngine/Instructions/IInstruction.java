/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DGS.dgs4j.ProcessingEngine.Instructions;

import DGS.dgs4j.ProcessingEngine.ProcessingWorkspace;
import org.w3c.dom.*;

/**
 *
 * @author dwimsey
 */
public interface IInstruction {

	public boolean process(ProcessingWorkspace workspace, Node instructionNode);
}
