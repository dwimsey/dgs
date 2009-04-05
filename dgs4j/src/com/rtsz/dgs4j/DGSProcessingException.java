/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rtsz.dgs4j;

/**
 *
 * @author dwimsey
 */
public class DGSProcessingException extends java.lang.Exception {

	private DGSProcessingException() {
		super();
	}

	public DGSProcessingException(String errMsg) {
		super(errMsg);
	}

	public DGSProcessingException(String errMsg, Exception e) {
		super(errMsg, e);
	}
}
