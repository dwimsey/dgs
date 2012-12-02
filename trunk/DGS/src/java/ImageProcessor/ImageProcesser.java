/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ImageProcessor;

import DGS.dgs4j.ProcessingEngine.ProcessingEngine;
import DGS.dgs4j.ProcessingEngine.ProcessingWorkspace;
import DGS.dgs4j.DGSRequestInfo;
import DGS.dgs4j.DGSProcessingException;
import DGS.dgs4j.DGSResponseInfo;

import java.util.*;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

/**
 *
 * @author dwimsey
 */
@WebService()
public class ImageProcesser {

	private static ProcessingEngine pEngine;

	/**
	 * Web service operation
	 */
	@WebMethod(operationName = "ProcessImage")
	public DGSResponseInfo ProcessImage(@WebParam(name = "RequestInfo") DGSRequestInfo RequestInfo) throws DGSProcessingException {
		if (RequestInfo == null) {
			throw new DGSProcessingException("NULL input is not allowed.");
		}
		synchronized (this) {
			if (pEngine == null) {
				pEngine = new ProcessingEngine();
			}
		}
		Date now = new Date();
		ProcessingWorkspace workspace = new ProcessingWorkspace(RequestInfo);
		workspace.logDebug("Processing Starting at " + now.toString());
		return (pEngine.processCommandString(workspace));
	}
}
