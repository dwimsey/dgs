/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rtsz.dgs4j.ProcessingEngine.CommandEnginePlugins.Batik;

import com.rtsz.dgs4j.ProcessingEngine.*;
import org.apache.batik.transcoder.TranscodingHints;
import org.apache.batik.transcoder.keys.StringKey;
/**
 *
 * @author dwimsey
 */
public class DGSPrinterTranscoder extends org.apache.batik.transcoder.print.PrintTranscoder {

	public static final TranscodingHints.Key KEY_PRINTER_TARGET_STR = new StringKey();
	
	public DGSPrinterTranscoder(ProcessingWorkspace workspace) {
		super();
		this.userAgent = new DGSUserAgent(this.userAgent, workspace);
	}
}
