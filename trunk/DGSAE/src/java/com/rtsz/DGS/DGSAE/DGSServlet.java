/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rtsz.DGS.DGSAE;

import java.io.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.rtsz.dgs4j.*;
import com.rtsz.dgs4j.DGSVariable;
import com.rtsz.dgs4j.ProcessingEngine.*;

import java.util.*;

import com.oreilly.servlet.multipart.*;

/**
 *
 * @author dwimsey
 */
public class DGSServlet extends HttpServlet {

	public static String xmlEncode(String inStr) {
		String rVal = inStr;
		rVal = rVal.replaceAll("&", "&amp;");
		rVal = rVal.replaceAll("<", "&lt;");
		rVal = rVal.replaceAll(">", "&gt;");
		rVal = rVal.replaceAll("'", "&apos;");
		rVal = rVal.replaceAll("\"", "&apos;");
		return (rVal);
	}

	public static String xmlDecode(String inStr) {
		String rVal = inStr;
		rVal = rVal.replaceAll("&apos;", "\"");
		rVal = rVal.replaceAll("&apos;", "'");
		rVal = rVal.replaceAll("&gt;", ">");
		rVal = rVal.replaceAll("&lt;", "<");
		rVal = rVal.replaceAll("&amp;", "&");
		return (rVal);
	}

	/**
	 * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
	 * @param request servlet request
	 * @param response servlet response
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException if an I/O error occurs
	 */
	protected void processRequest(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html;charset=UTF-8");



		


		PrintWriter out = response.getWriter();
		try {




			MultipartParser mp;
			try {
				mp = new MultipartParser(request, 1024*1024*10);
			} catch(IOException ex) {
				// we should report this error properly but for now we're just rethrowing
				throw ex;
			}

			String pName = null;
			String pValue = null;
			DGSFileInfo nfInfo = null;
			com.rtsz.dgs4j.DGSPackage dPkg = null;
			DGSRequestInfo reqInfo = new DGSRequestInfo();
			reqInfo.files = new DGSFileInfo[0];
			reqInfo.variables = new DGSVariable[0];

			Part p = mp.readNextPart();
			while(p != null) {
				pName = p.getName();
				if(p.isFile()) {
					pValue = null;
					if("svgFile".equals(pName)) {
						java.io.ByteArrayOutputStream os = new java.io.ByteArrayOutputStream();
						((FilePart)p).writeTo(os);
						os.close();
						byte[] fData = os.toByteArray();
						if(fData.length > 0) {
							nfInfo = new DGSFileInfo();
							nfInfo.data = fData;

							//pfPath = ((FilePart)p).getFilePath();
							nfInfo.name = ((FilePart)p).getFileName();
							nfInfo.mimeType = ((FilePart)p).getContentType();
/*
							DGSFileInfo[] newBlock = new DGSFileInfo[reqInfo.files.length+1];
							int i = 0;
							for(i = 0; i < reqInfo.files.length; i++) {
								newBlock[i] = reqInfo.files[i];
							}
							newBlock[i] = nfInfo;
							reqInfo.files = newBlock;
*/						}
					} else if("dgsPackageFile".equals(pName)) {
						dPkg = new com.rtsz.dgs4j.DGSPackage();
						if (dPkg.loadFileStream(((FilePart)p).getInputStream(), ((FilePart)p).getFileName())) {
							if (dPkg.files != null && (dPkg.files.length > 0)) {
								reqInfo.files = new DGSFileInfo[dPkg.files.length + 1];
								for (int i = 0; i < dPkg.files.length; i++) {
									reqInfo.files[i + 1] = dPkg.files[i];
								}
							} else {
								reqInfo.files = new DGSFileInfo[1];
							}
							reqInfo.variables = dPkg.variables;
						} else {
							reqInfo.files = new DGSFileInfo[1];
							reqInfo.variables = null;
						}
					}

				} else {
					if(p.isParam()) {
						pValue = ((ParamPart)p).getStringValue();
					} else {
						pValue = null;
						throw new IOException("Unexpected parameter type.");
					}
				}

				p = mp.readNextPart();
			}

			boolean gotSvg = false;
			boolean gotDgsPackage = false;

			if(nfInfo == null || dPkg == null) {
				throw new IOException("Missing SVG template or DGS package file.");
			} else {
				reqInfo.files[0] = nfInfo;
				if ((reqInfo.files[0].name == null) || (reqInfo.files[0].name.length() == 0)) {
					reqInfo.files[0].name = "input.svg"; // we need this set to Something, so set it ourselves
				}
		// if((reqInfo.files[0].mimeType == null) || (reqInfo.files[0].mimeType.length() == 0)) {
				reqInfo.files[0].mimeType = "image/svg+xml"; // we only process svg for now
		// }

				// Form the instruction xml fragment
				reqInfo.instructionsXML = "<commands><load filename=\"" + reqInfo.files[0].name + "\" buffer=\"" + dPkg.templateBuffer + "\" mimeType=\"image/svg+xml\" />";
				if (dPkg.commandString != null && dPkg.commandString.length() > 0) {
					reqInfo.instructionsXML += dPkg.commandString;
				} else {
					reqInfo.instructionsXML += "<substituteVariables buffer=\"main\" />";
				}
		//		reqInfo.instructionsXML += "<addWatermark buffer=\"" + dPkg.templateBuffer + "\" srcImage=\"watermark\" opacity=\"0.05\"/>";
				reqInfo.instructionsXML += "<save ";
				if ((dPkg.animationDuration > 0.0f) && (dPkg.animationFramerate > 0.0f)) {
		//			reqInfo.instructionsXML += "animationDuration=\"" + dPkg.animationDuration + "\" animationFramerate=\"" + dPkg.animationFramerate + "\" ";
				}

				String outputFileName = "output.png";
				String outputMimeType = "image/png";

				reqInfo.instructionsXML += "filename=\"" + outputFileName + "\" buffer=\"" + dPkg.templateBuffer + "\" mimeType=\"" + outputMimeType + "\" /></commands>";
//				ProcessingWorkspace workspace = new ProcessingWorkspace(reqInfo);
//				DGSResponseInfo dgsResponseInfo = pEngine.processCommandString(workspace);










				DGSResponseInfo respInfo = null;
				try {
					respInfo = ProcessImage(reqInfo);
					out.print("<Response>\r\n");
					if (respInfo.resultFiles != null) {
						out.print("\t<Files>\r\n");
						for (int i = 0; i < respInfo.resultFiles.length; i++) {
							out.print("\t\t<File");
							out.print(" name=\"" + xmlEncode(respInfo.resultFiles[i].name) + "\"");
							out.print(" height=\"" + respInfo.resultFiles[i].height + "\" width=\"" + respInfo.resultFiles[i].width + "\"");
							out.print(" mimetype=\"" + xmlEncode(respInfo.resultFiles[i].mimeType) + "\">");
							out.print(com.oreilly.servlet.Base64Encoder.encode(respInfo.resultFiles[i].data));
							out.print("</File>\r\n");
						}
						out.print("\t</Files>\r\n");
					}
					out.print("\t<Log>\r\n");
					for (int i = 0; i < respInfo.processingLog.length; i++) {
						out.print("\t\t<Message index=\"" + i + "\">" + xmlEncode(respInfo.processingLog[i]) + "</Message>\r\n");
					}
					out.print("\t</Log>\r\n");
					out.print("</Response>");
				} catch (DGSProcessingException ex) {
					out.print(ex);
				}
			}
		} finally {
			out.close();
		}
	}

	// <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
	/**
	 * Handles the HTTP <code>GET</code> method.
	 * @param request servlet request
	 * @param response servlet response
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException if an I/O error occurs
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		processRequest(request, response);
	}

	/**
	 * Handles the HTTP <code>POST</code> method.
	 * @param request servlet request
	 * @param response servlet response
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException if an I/O error occurs
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		processRequest(request, response);
	}

	/**
	 * Returns a short description of the servlet.
	 * @return a String containing servlet description
	 */
	@Override
	public String getServletInfo() {
		return "Short description";
	}// </editor-fold>
	private static ProcessingEngine pEngine;

	/**
	 * Web service operation
	 */
//	@WebMethod(operationName = "ProcessImage")
	public DGSResponseInfo ProcessImage(DGSRequestInfo RequestInfo) throws DGSProcessingException {
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
		workspace.log("Processing Starting at " + now.toString());
		return (pEngine.processCommandString(workspace));
	}

	private DGSFileInfo getFile(HttpServletRequest request, HttpServletResponse response) throws IOException {
		//to get the content type information from JSP Request Header
		String contentType = request.getContentType();

		//here we are checking the content type is not equal to Null and
		// as well as the passed data from mulitpart/form - data
		// is greater than or equal to 0
		if ((contentType != null) && (contentType.indexOf("application/x-www-form-urlencoded") >= 0)) {
//		if ((contentType != null) && (contentType.indexOf("multipart/form-data") >= 0)) {

			DataInputStream in = new DataInputStream(request.getInputStream());
			//we are taking the length of Content type data
			int formDataLength = request.getContentLength();
			byte dataBytes[] = new byte[formDataLength];
			int byteRead = 0;
			int totalBytesRead = 0;
			//this loop converting the uploaded file into byte code
			while (totalBytesRead < formDataLength) {
				byteRead = in.read(dataBytes, totalBytesRead, formDataLength);
				totalBytesRead += byteRead;
			}
			String file = new String(dataBytes);
			//for saving the file name
			String saveFile = file.substring(file.indexOf("filename=\"") + 10);
			saveFile = saveFile.substring(0, saveFile.indexOf("\n"));
			saveFile = saveFile.substring(saveFile.lastIndexOf("\\") + 1, saveFile.indexOf("\""));
			int lastIndex = contentType.lastIndexOf("=");
			String boundary = contentType.substring(lastIndex + 1, contentType.length());
			int pos;
			//extracting the index of file
			pos = file.indexOf("filename=\"");
			pos = file.indexOf("\n", pos) + 1;
			pos = file.indexOf("\n", pos) + 1;
			pos = file.indexOf("\n", pos) + 1;
			int boundaryLocation = file.indexOf(boundary, pos) - 4;
			int startPos = ((file.substring(0, pos)).getBytes()).length;
			int endPos = ((file.substring(0, boundaryLocation)).getBytes()).length;
			// creating a new file with the same name and writing the
			// content in
			FileOutputStream fileOut = new FileOutputStream(saveFile);
			fileOut.write(dataBytes, startPos, (endPos - startPos));
			fileOut.flush();
			fileOut.close();
		}
		return(null);
	}
}
