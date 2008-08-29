/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dgspreviewer;

import java.io.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.xml.serialize.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import org.w3c.dom.*;

import java.awt.Color;
/**
 *
 * @author dwimsey
 */
public class Options {

	private java.awt.Color BackgroundColor;
	private String MRUTemplateImageFileName;
	private String MRUDGSPackageFileName;
	private int LogLevel;
	private String LogTimeFormatString;

	public Options() {
		super();
		reset();
	}

	private void reset() {
		this.BackgroundColor = new Color(0xFFFFFFFF);
		this.MRUTemplateImageFileName = "";
		this.MRUDGSPackageFileName = "";
		this.LogLevel = 255;
		this.LogTimeFormatString = "[dd/mm/yyyy HH:MM:ss]: ";
	}

	public boolean load() {
		String prefsFileName = System.getenv("APPDATA") + File.separator + "DGSPreviewer" + File.separator + "Prefs.xml";
		File file = null;
		DocumentBuilderFactory dbf = null;
		DocumentBuilder db = null;
		Document doc = null;
		try {
			file = new File(prefsFileName);
			dbf = DocumentBuilderFactory.newInstance();
			db = dbf.newDocumentBuilder();
		} catch (Exception ex) {
//			setStatusMessage(10, "An unexpected error occurred creating the xml document for the Previewer options.  File: " + prefsFileName + "  Error: " + ex.getLocalizedMessage());
			ex.printStackTrace();
			return (false);
		}
		try {
			doc = db.parse(file);
			doc.getDocumentElement().normalize();
		} catch (Exception ex) {
			ex.printStackTrace();
			return (false);
		}
		try {
			NodeList nodeLst = doc.getElementsByTagName("DGSPreviewerOption");
			int nLen = nodeLst.getLength();

			for (int s = 0; s < nLen; s++) {
				Node fstNode = nodeLst.item(s);
				if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
					NamedNodeMap aMap = fstNode.getAttributes();
					// do stuff with the option
					Node nameNode = aMap.getNamedItem("key");
					Node valueNode = aMap.getNamedItem("value");
					String name = null;
					String value = null;
					if (nameNode != null) {
						name = nameNode.getNodeValue();
					}
					if (valueNode != null) {
						value = valueNode.getNodeValue();
					}
					if (name != null && value != null) {
						// handle this name/value pairs

						if (name.equals("BackgroundColor")) {
							if (value != null) {
								java.awt.Color t = this.BackgroundColor;
								try {
									t = Color.decode(value);
									this.BackgroundColor = t;
								} catch (NumberFormatException ex) {
									ex.printStackTrace();
								}
							}
						} else if (name.equals("LogLevel")) {
							if (value != null) {
								try {
									this.LogLevel = Integer.valueOf(value);
								} catch (NumberFormatException ex) {
									ex.printStackTrace();
								}
							}
						} else if (name.equals("LogTimeFormatString")) {
							if (value != null) {
								this.LogTimeFormatString = value;
							}
						} else if (name.equals("MRUTemplateImageFileName")) {
							if (value != null) {
								this.MRUTemplateImageFileName = value;
							}
						} else if (name.equals("MRUDGSPackageFileName")) {
							if (value != null) {
								this.MRUDGSPackageFileName = value;
							}
						}
					}
				}
			}
//                    vars[s] = new DGSVariable(aMap.getNamedItem("name").getNodeValue(), aMap.getNamedItem("value").getNodeValue());
		} catch (Exception ex) {
			ex.printStackTrace();
			return (false);
		}

		return (true);
	}

	private boolean save() {
		String prefsFileName = System.getenv("APPDATA") + File.separator + "DGSPreviewer";
		File pDir = new File(prefsFileName);
		pDir.mkdirs();
		prefsFileName += File.separator + "Prefs.xml";

		FileOutputStream fos;
		try {
			fos = new FileOutputStream(prefsFileName);
			// XERCES 1 or 2 additionnal classes.
			OutputFormat of = new OutputFormat("XML", "UTF-8", true);
			of.setIndent(1);
			of.setIndenting(true);
	//		of.setDoctype(null, "users.dtd");
			XMLSerializer serializer = new XMLSerializer(fos, of);

			// SAX2.0 ContentHandler.
			ContentHandler hd = serializer.asContentHandler();
			hd.startDocument();
			// Processing instruction sample.
			//hd.processingInstruction("xml-stylesheet","type=\"text/xsl\" href=\"users.xsl\"");

			// USER attributes.
			AttributesImpl atts = new AttributesImpl();
			// USERS tag.
			hd.startElement("", "", "DGSPreviewerOptions", atts);
			
			// BackgroundColor
			atts.clear();
			atts.addAttribute("", "", "key", "CDATA", "BackgroundColor");
			atts.addAttribute("", "", "value", "CDATA", "#" + (String.format("%1$X", this.BackgroundColor.getRGB())).substring(2, 8));
			hd.startElement("", "", "DGSPreviewerOption", atts);
//			hd.characters(desc[i].toCharArray(), 0, desc[i].length());
			hd.endElement("", "", "DGSPreviewerOption");

			// LogLevel
			atts.clear();
			atts.addAttribute("", "", "key", "CDATA", "LogLevel");
			atts.addAttribute("", "", "value", "CDATA", "" + this.LogLevel );
			hd.startElement("", "", "DGSPreviewerOption", atts);
			hd.endElement("", "", "DGSPreviewerOption");

			// LogTimeFormatString
			atts.clear();
			atts.addAttribute("", "", "key", "CDATA", "LogTimeFormatString");
			atts.addAttribute("", "", "value", "CDATA", this.LogTimeFormatString);
			hd.startElement("", "", "DGSPreviewerOption", atts);
			hd.endElement("", "", "DGSPreviewerOption");

			// MRUTemplateImageFileName
			atts.clear();
			atts.addAttribute("", "", "key", "CDATA", "MRUTemplateImageFileName");
			atts.addAttribute("", "", "value", "CDATA", this.MRUTemplateImageFileName);
			hd.startElement("", "", "DGSPreviewerOption", atts);
			hd.endElement("", "", "DGSPreviewerOption");

			// MRUDGSPackageFileName
			atts.clear();
			atts.addAttribute("", "", "key", "CDATA", "MRUDGSPackageFileName");
			atts.addAttribute("", "", "value", "CDATA", this.MRUDGSPackageFileName);
			hd.startElement("", "", "DGSPreviewerOption", atts);
			hd.endElement("", "", "DGSPreviewerOption");
		
			hd.endElement("", "", "DGSPreviewerOptions");
			hd.endDocument();
			fos.close();
		} catch (IOException ioex) {
			ioex.printStackTrace();
			return(false);
		} catch (SAXException sex) {
			sex.printStackTrace();
			return(false);
		}
		return (true);
	}

	public Color getBackgroundColor() {
		return (this.BackgroundColor);
	}

	public Color setBackgroundColor(Color newColor) {
		Color oldColor = this.BackgroundColor;
		if (newColor.equals(oldColor)) {
			return (oldColor);
		}
		this.BackgroundColor = newColor;
		this.save();
		return (oldColor);
	}

	public int getLogLevel() {
		return (this.LogLevel);
	}

	public int setLogLevel(int newLevel) {
		if (this.LogLevel == newLevel) {
			return (newLevel);
		}
		int oldLevel = this.LogLevel;
		this.LogLevel = newLevel;
		this.save();
		return (oldLevel);
	}

	public String getLogTimeFormatString() {
		return (this.LogTimeFormatString);
	}

	public String setLogTimeFormatString(String newFormatStr) {
		String oldFormatStr = this.LogTimeFormatString;
		if (newFormatStr == null) {
			newFormatStr = "";
		}
		if (newFormatStr.equals(oldFormatStr)) {
			return (oldFormatStr);
		}

		this.LogTimeFormatString = newFormatStr;
//		this.save();
		return (oldFormatStr);
	}

	public String getMRUTemplateImageFileName() {
		return (this.MRUTemplateImageFileName);
	}

	public String setMRUTemplateImageFileName(String newFileName) {
		String oldName = this.MRUTemplateImageFileName;
		if (newFileName == null) {
			newFileName = "";
		}
		if (newFileName.equals(oldName)) {
			return (oldName);
		}

		this.MRUTemplateImageFileName = newFileName;
		this.save();
		return (oldName);
	}

	public String getMRUDGSPackageFileName() {
		return (this.MRUDGSPackageFileName);
	}

	public String setMRUDGSPackageFileName(String newFileName) {
		String oldName = this.MRUDGSPackageFileName;
		if (newFileName == null) {
			newFileName = "";
		}
		if (newFileName.equals(oldName)) {
			return (oldName);
		}

		this.MRUDGSPackageFileName = newFileName;
		this.save();
		return (oldName);
	}
}
