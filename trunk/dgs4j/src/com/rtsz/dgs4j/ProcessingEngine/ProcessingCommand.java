/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ImageProcessor.ProcessingEngine;

import ImageProcessor.*;
import ImageProcessor.ProcessingEngine.Instructions.*;

import java.io.*;
import java.util.*;

import org.apache.xerces.parsers.*;
import org.xml.sax.*;
import org.w3c.dom.*;

/**
 *
 * @author dwimsey
 */
public class ProcessingCommand {
    private List<IInstruction> instructions;
    private String commandName;
    public ProcessingCommand(String name)
    {
        commandName = name;
        instructions = new ArrayList<IInstruction>();
    }

    public void addInstruction(IInstruction instruction)  
    throws Exception {
        if(instructions.indexOf(instruction) == -1) {
            instructions.add(instruction);
        } else {
            throw new Exception("An attempt to made to add an instruction to a command multiple times.  Command: " + this.commandName);
        }
    }
    
    public boolean process(ProcessingWorkspace workspace, Node commandNode)
    {
        if(instructions.size() ==0) {
            workspace.log("Command does not have any command processors installed.  Command: " + this.commandName);
            return(false);
        }
        for(int i = 0; i<instructions.size(); i++) {
            if(instructions.get(i).process(workspace, commandNode)) {
                return(true);
            }
        }
        workspace.log("Command was not completed by any of the installed command processors.  Command: " + this.commandName);
        return(false);
    }
}