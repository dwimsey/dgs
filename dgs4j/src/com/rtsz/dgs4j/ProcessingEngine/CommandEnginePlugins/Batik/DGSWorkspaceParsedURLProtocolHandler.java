/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.rtsz.dgs4j.ProcessingEngine.CommandEnginePlugins.Batik;

import org.apache.batik.util.*;
/**
 *
 * @author dwimsey
 */
public class DGSWorkspaceParsedURLProtocolHandler implements ParsedURLProtocolHandler {


	public ParsedURLData parseURL(ParsedURL baseUrl, String url)
	{
		return(new DGSWorkspaceParsedURLData(baseUrl, url));
	}

	public ParsedURLData parseURL(String url)
	{
		return(new DGSWorkspaceParsedURLData(null, url));
	}
	
	public String getProtocolHandled()
	{
		return("workspace");
	}
}
