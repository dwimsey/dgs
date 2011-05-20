/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rtsz.dgs4j.dgs4cl;

import org.apache.batik.util.ApplicationSecurityEnforcer;

import java.io.*;
import com.rtsz.dgs4j.*;
import com.rtsz.dgs4j.ProcessingEngine.*;
import com.rtsz.dgs4j.ProcessingEngine.CommandEnginePlugins.Batik.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author dwimsey
 */
public class Main {

	public static void usage() {
		System.out.println("dgs4cl v1.0.1 - DGS command line utility");
		System.out.println("\tUsage: dgs4cl.jar [-d DGSPackage.xml] [-V key=value] input.svg output.png");
		System.out.println("\t-h Show this usage information.");
		System.out.println("\t-l Display request log output.");
		System.out.println("\t-d <DGS Package XML Filename>: Uses the file specified as the DGS package.");
		System.out.println("\t-s <CSS Stylesheet Filename>: Uses the file specified as the default stylesheet.");
		System.out.println("\t\tBuiltin Stylesheets:");
		System.out.println("\t\t\tapl-appstore: Renders the document viewport to a 512x512 png file.");
		System.out.println("\t\t\tapl-iphone-spotlight: Renders the document viewport to a 512x512 png file.");
		System.out.println("\t-W Set output image width in pixels.");
		System.out.println("\t-H Set output image height in pixels.");
		System.out.println("\t-D Set output image dots per inch. (Not currently implemented)");
		System.out.println("\t-V <Key=value>: Sets the variable named 'Key' to 'value'.  Variables specified with the -V option override variables obtained using the -d option.");
	}

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		// TODO code application logic here

		// Apply script security option
		ApplicationSecurityEnforcer securityEnforcer =
				new ApplicationSecurityEnforcer(Main.class.getClass(), "dgspreviewer/resources/DGSPreviewer.policy");

		securityEnforcer.enforceSecurity(false);

		DGSWorkspaceParsedURLProtocolHandler ph = new DGSWorkspaceParsedURLProtocolHandler();
		org.apache.batik.util.ParsedURL.registerHandler(ph);

		com.rtsz.dgs4j.ProcessingEngine.ProcessingEngine pEngine = new com.rtsz.dgs4j.ProcessingEngine.ProcessingEngine();

		int offset = 0;
		int width = 0;
		int height = 0;
		int dpi = 0;
		String dgsTemplateFilename = "";
		String previewPackageFilename = "";
		String altStylesheetFilename = "";
		String outputMimeType = "image/png";
		String outputFilename = "";
		boolean continueOnError = false;
		String current_arg;
		boolean showLog = false;
		java.util.ArrayList<DGSVariable> vars = new java.util.ArrayList<DGSVariable>();

		if(args.length  == 0) {
			System.out.println("ERROR: No arguments specified.");
			usage();
			System.exit(254);
		}

		for (int i = 0; i < args.length; i++) {
			current_arg = args[i];
			if (current_arg.equals("-h")) {
				// we don't do anything after the -h so just bail out
				usage();
				System.exit(0);
			} else if (current_arg.equals("-d")) {
				i++;
				if (args.length > i) {
					previewPackageFilename = args[i];
				} else {
					System.out.println("-d option missing argument");
					System.exit(254);
				}
			} else if (current_arg.equals("-s")) {
				i++;
				if (args.length > i) {
					altStylesheetFilename = args[i];
				} else {
					System.out.println("-s option missing argument");
					System.exit(254);
				}
			} else if (current_arg.equals("-W")) {
				i++;
				if (args.length > i) {
					width = java.lang.Integer.parseInt(args[i]);
				} else {
					System.out.println("-W option missing argument");
					System.exit(254);
				}
			} else if (current_arg.equals("-H")) {
				i++;
				if (args.length > i) {
					height = java.lang.Integer.parseInt(args[i]);
				} else {
					System.out.println("-H option missing argument");
					System.exit(254);
				}
			} else if (current_arg.equals("-D")) {
				i++;
				if (args.length > i) {
					dpi = java.lang.Integer.parseInt(args[i]);
				} else {
					System.out.println("-D option missing argument");
					System.exit(254);
				}
			} else if (current_arg.equals("-l")) {
				showLog = true;
			} else if (current_arg.equals("-V")) {
				i++;
				if (args.length > i) {
					current_arg = args[i];
					offset = current_arg.indexOf("=");
					if (offset > -1) {
						if (offset == (current_arg.length() - 1)) {
							// no value, use null
							vars.add(new DGSVariable(current_arg.substring(0, offset), null));
						} else {
							vars.add(new DGSVariable(current_arg.substring(0, offset), current_arg.substring(offset + 1)));
						}
					} else {
						System.out.println("-V requires a key=value pair.");
					System.exit(254);
					}
				} else {
					System.out.println("-d option missing argument");
					System.exit(254);
				}
			} else {
				if (current_arg.equals("-")) {
					dgsTemplateFilename = "=".intern();
				} else if (current_arg.startsWith("-")) {
					System.out.println("Unexpected option: " + current_arg);
					usage();
					return;
				} else {
					// we assume this is an input file name
					dgsTemplateFilename = current_arg.intern();
				}

				i++;
				if (args.length > i) {
					outputFilename = args[i];
				} else {
					// no output file name, just make one up based on the input file name and type
					if (dgsTemplateFilename.equals("-")) {
						outputFilename = "-".intern();
					} else {
						if (dgsTemplateFilename.endsWith(".svg")) {
							outputFilename = dgsTemplateFilename.substring(0, dgsTemplateFilename.length() - 4) + ".png";
						}
					}
				}

				int rval = processDGSPackage(pEngine, dgsTemplateFilename, previewPackageFilename, altStylesheetFilename, outputMimeType, outputFilename, width, height, dpi, vars, continueOnError, showLog);
				if(rval != 0) {
					System.exit(rval);
				}
			}
		}

	}

	private static int processDGSPackage(String dgsTemplateFilename,
			String previewPackageFilename, String altStylesheetFilename, String outputMimeType,
			String outputFilename, int width, int height, int dpi, boolean continueOnError, boolean showLog) {
		com.rtsz.dgs4j.ProcessingEngine.ProcessingEngine pEngine = new com.rtsz.dgs4j.ProcessingEngine.ProcessingEngine();
		return processDGSPackage(pEngine, dgsTemplateFilename, previewPackageFilename, altStylesheetFilename, outputMimeType, outputFilename, width, height, dpi, new java.util.ArrayList<DGSVariable>(), continueOnError, showLog);
	}

	private static int processDGSPackage(com.rtsz.dgs4j.ProcessingEngine.ProcessingEngine pEngine,
			String dgsTemplateFilename, String previewPackageFilename, String altStylesheetFilename, String outputMimeType,
			String outputFilename, int width, int height, int dpi, java.util.ArrayList<DGSVariable> vars, boolean continueOnError, boolean showLog) {
		DGSRequestInfo dgsRequestInfo = new DGSRequestInfo();
		dgsRequestInfo.continueOnError = continueOnError;

		DGSFileInfo templateFileInfo;
		try {
			templateFileInfo = loadImageFileData(dgsTemplateFilename);
		} catch (Exception e) {
			System.out.println("Could not open input file: " + dgsTemplateFilename + ": " + e.getMessage());
			return(253);
		}

		if (templateFileInfo == null) {
			System.out.println("Could not open input file: " + dgsTemplateFilename);
			return(253);
		}

		DGSPackage dPkg = new DGSPackage();
		if ((previewPackageFilename == null) || (previewPackageFilename.length() == 0)) {
			dgsRequestInfo.files = new DGSFileInfo[1];
			dgsRequestInfo.variables = null;
		} else {
			if (dPkg.loadFile(previewPackageFilename)) {

				if (dPkg.files != null && (dPkg.files.length > 0)) {
					dgsRequestInfo.files = new DGSFileInfo[dPkg.files.length + 1];
					for (int i = 0; i < dPkg.files.length; i++) {
						dgsRequestInfo.files[i + 1] = dPkg.files[i];
					}
				} else {
					dgsRequestInfo.files = new DGSFileInfo[1];
				}
				dgsRequestInfo.variables = dPkg.variables;
			} else {
				dgsRequestInfo.files = new DGSFileInfo[1];
				dgsRequestInfo.variables = null;
			}
		}

		if (!vars.isEmpty()) {
			int voffset = 0;
			// add the vars specified on the command line
			if (dPkg.variables != null && dPkg.variables.length > 0) {
				// we have existing variables from the DGSPackage, allocate a new variable block
				// big enough for both and copy this into it
				com.rtsz.dgs4j.DGSVariable nVars[] = new com.rtsz.dgs4j.DGSVariable[vars.size() + dPkg.variables.length];

				int i;
				for (i = 0; i < vars.size(); i++) {
					nVars[voffset] = vars.get(i);
				}

				for (voffset = 0; voffset < dPkg.variables.length; voffset++) {
					nVars[i] = dPkg.variables[voffset];
				}


				dgsRequestInfo.variables = nVars;
			} else {
				dgsRequestInfo.variables = vars.toArray(new DGSVariable[0]);
			}
		}

		dgsRequestInfo.files[0] = templateFileInfo;
		if ((dgsRequestInfo.files[0].name == null) || (dgsRequestInfo.files[0].name.length() == 0)) {
			dgsRequestInfo.files[0].name = "input.svg"; // we need this set to Something, so set it ourselves
		}
		//if((dgsRequestInfo.files[0].mimeType == null) || (dgsRequestInfo.files[0].mimeType.length() == 0)) {
		dgsRequestInfo.files[0].mimeType = "image/svg+xml"; // we only process svg for now
		//}

		// Form the instruction xml fragment
		dgsRequestInfo.instructionsXML = "<commands><load filename=\"" + dgsRequestInfo.files[0].name + "\" buffer=\"" + dPkg.templateBuffer + "\" mimeType=\"image/svg+xml\" />";
		if (dPkg.commandString != null && dPkg.commandString.length() > 0) {
			dgsRequestInfo.instructionsXML += dPkg.commandString;
		} else {
			if(dgsRequestInfo.variables != null && dgsRequestInfo.variables.length > 0) {
				dgsRequestInfo.instructionsXML += "<substituteVariables buffer=\"main\" />";
			}
		}
		//dgsRequestInfo.instructionsXML += "<addWatermark buffer=\"" + dPkg.templateBuffer + "\" srcImage=\"watermark\" opacity=\"0.05\"/>";
		dgsRequestInfo.instructionsXML += "<save ";
		if ((dPkg.animationDuration > 0.0f) && (dPkg.animationFramerate > 0.0f)) {
			//dgsRequestInfo.instructionsXML += "animationDuration=\"" + dPkg.animationDuration + "\" animationFramerate=\"" + dPkg.animationFramerate + "\" ";
		}

		if(width > 0) {
			dgsRequestInfo.instructionsXML += "width=\"" + width + "\" ";
		}
		if(height > 0) {
			dgsRequestInfo.instructionsXML += "height=\"" + height + "\" ";
		}
		if(dpi > 0) {
			dgsRequestInfo.instructionsXML += "dpi=\"" + dpi + "\" ";
		}

		dgsRequestInfo.instructionsXML += "filename=\"" + outputFilename + "\" buffer=\"" + dPkg.templateBuffer + "\" mimeType=\"" + outputMimeType + "\" /></commands>";

		ProcessingWorkspace workspace = new ProcessingWorkspace(dgsRequestInfo);

		if(altStylesheetFilename.length() > 0) {
			try {
				boolean worked = false;
				byte fDat[] = null;
				fDat = fileToBytes(altStylesheetFilename);
				if (fDat != null) {
					if (fDat.length == 0) {
						System.out.println("The specified file is empty: " + altStylesheetFilename);
					}

					workspace.activeStylesheet = new String(fDat, "UTF8");
				} else {
					System.out.println("An unknown error occurred reading file: " + altStylesheetFilename);
				}
			} catch (Exception e) {
				System.out.println("Could not open input stylesheet: " + altStylesheetFilename + ": " + e.getMessage());
				return(253);
			}
		}
		DGSResponseInfo dgsResponseInfo = pEngine.processCommandString(workspace);




		if(showLog) {
			for (int i = 0; i < dgsResponseInfo.processingLog.length; i++) {
				//this.notificationMethods.logEvent(200, "     " + dgsResponseInfo.processingLog[i]);
				System.out.println("\t"+ dgsResponseInfo.processingLog[i]);
			}
		}

		if (dgsResponseInfo.resultFiles.length == 0) {
			//this.notificationMethods.statusMessage(10, "No image files were returned by the processing engine, this generally indicates an error in the input file: " + this.imageFilename);
			System.out.println("No image files were returned by the processing engine, this generally indicates an error in the input file: " + dgsTemplateFilename);
			return(250);
		}


		// save image data to filename provided
		java.io.FileOutputStream fs = null;
		try {
			fs = new java.io.FileOutputStream(outputFilename);
			try {
				fs.write(((byte[]) dgsResponseInfo.resultFiles[0].data));
			} catch (Throwable t) {
				//this.notificationMethods.statusMessage(0, "Could not save file: " + t.getMessage());
				System.out.println("Could not save output file: " + outputFilename + ": " + t.getMessage());
				return(251);
			} finally {
				fs.close();
			}
		} catch (Throwable t) {
			//this.notificationMethods.statusMessage(0, "Could not open file: " + t.getMessage());
			System.out.println("Could not open output file: " + outputFilename + ": " + t.getMessage());
			return(252);
		}

		return(0);
	}

	private static DGSFileInfo loadImageFileData(String fileName) throws FileNotFoundException, IOException, Exception {
		byte fDat[] = null;
		fDat = fileToBytes(fileName);
		if (fDat == null) {
			//this.notificationMethods.statusMessage(5, "An unknown error occurred reading file: " + fileName);
			return (null);
		}
		if (fDat.length == 0) {
			//this.notificationMethods.statusMessage(10, "The specified file is empty: " + fileName);
			return (null);
		}
		DGSFileInfo fInfo = new DGSFileInfo();
		fInfo.data = fDat;
		fInfo.name = fileName;
		fInfo.width = -1;
		fInfo.height = -1;
		return (fInfo);
	}

	private static byte[] fileToBytes(String fileName) throws FileNotFoundException, IOException {
		byte fDat[] = new byte[0];

		FileInputStream fs = new FileInputStream(fileName);
		int i = fs.available();
		fDat = new byte[i];
		i = fs.read(fDat);
		fs.close();
		return (fDat);
	}
}
