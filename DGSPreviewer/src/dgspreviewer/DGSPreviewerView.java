/*
 * DGSPreviewerView.java
 */

package dgspreviewer;

import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.TaskMonitor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;

import ImageProcessor.*;
import ImageProcessor.ProcessingEngine.*;

import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * The application's main frame.
 */
public class DGSPreviewerView extends FrameView {

    private Options options;
    private static ImageProcessor.ProcessingEngine.ProcessingEngine pEngine;
	private String[] args;

	String cmdLinePackageFile = null;
	String cmdLineImageFile = null;
	boolean cmdLineLoadOptions = true;

	private void parseCommandLineArgs() {
		if(args==null) {
			return;
		}
		for(int i = 0; i<this.args.length; i++) {
			if(args[i].equals("-P")) {
				if((i+1)==this.args.length) {
					// this is the last argument, so we can't possibly have a valid DGS package file as the next argument
					return;
				} else {
					// We have another argument, consider it a file name for a DGS package

					// Increment our counter so the i is pointing at the DGS package file name, this
					// will cause it to be skipped during the next argument pass as well, so it isn't
					// considered a template image name
					i++;

					// remember the file name for later
					cmdLinePackageFile = args[i];
				}
			} else if(args[i].equals("-NoLoadOptions")) {
				// Do not load options file, it will be overwritten with defaults as soon as a options change is made within the application
				cmdLineLoadOptions = false;
			} else {
				// If nothing else matched it, consider it a template image file name
				cmdLineImageFile = args[i];
			}
		}
	}

	public DGSPreviewerView(SingleFrameApplication app) {
        super(app);
		String cmdArgs[] = null;
		this.args = DGSPreviewerApp.args;
	    options = new Options();
		parseCommandLineArgs();

		boolean wereOptionsLoaded = false;
		if(cmdLineLoadOptions) {
			wereOptionsLoaded = options.load();
		}

		pEngine = new ImageProcessor.ProcessingEngine.ProcessingEngine();
        initComponents();
        imagePanel.options = options;

        // status bar initialization - message timeout, idle icon and busy animation, etc
        ResourceMap resourceMap = getResourceMap();
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                statusMessageLabel.setText("");
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        statusAnimationLabel.setIcon(idleIcon);
        progressBar.setVisible(false);

        // connecting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("started".equals(propertyName)) {
                    if (!busyIconTimer.isRunning()) {
                        statusAnimationLabel.setIcon(busyIcons[0]);
                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(true);
                } else if ("done".equals(propertyName)) {
                    busyIconTimer.stop();
                    statusAnimationLabel.setIcon(idleIcon);
                    progressBar.setVisible(false);
                    progressBar.setValue(0);
                } else if ("message".equals(propertyName)) {
                    String text = (String)(evt.getNewValue());
                    statusMessageLabel.setText((text == null) ? "" : text);
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer)(evt.getNewValue());
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(value);
                }
            }
        });

		String olm;

		if((cmdLinePackageFile!=null) && (cmdLinePackageFile.length()>0)) {
			// load the DGS package file specified on the command line if possible
			setStatusMessage(10, "Loading template variables specified : " + cmdLinePackageFile);
			if(!loadVariablesFile(cmdLinePackageFile)) {
				setStatusMessage(0, "The template variables specified could not be loaded: " + cmdLinePackageFile);
			}
		} else {
			String MRUTemplateVariablesFileName = this.options.getMRUTemplateVariablesFileName();
			if(MRUTemplateVariablesFileName.length()>0) {
				setStatusMessage(10, "Loading last used template variables: " + MRUTemplateVariablesFileName);
				if(!loadVariablesFile(MRUTemplateVariablesFileName)) {
					setStatusMessage(0, "The previously loaded template variables could not be loaded: " + MRUTemplateVariablesFileName);
				}
			} else {
			}
		}

		if((cmdLineImageFile!=null) && (cmdLineImageFile.length()>0)) {
			setStatusMessage(10, "Loading template image specified: " + cmdLineImageFile);
			if(!loadImageFile(cmdLineImageFile)) {
				setStatusMessage(0, "The image file specified could not be loaded: " + cmdLineImageFile);
			}
		} else {
			String MRUTemplateImageFileName = this.options.getMRUTemplateImageFileName();
			if(MRUTemplateImageFileName.length()>0) {
				setStatusMessage(10, "Loading last used template image: " + MRUTemplateImageFileName);
				if(!loadImageFile(MRUTemplateImageFileName)) {
					setStatusMessage(0, "Previously loaded image file could not be loaded: " + MRUTemplateImageFileName);
				}
	//		} else {
			}
		}
		olm = this.options.setLogTimeFormatString("");
		setStatusMessage(0, "The DGS Previewer is ready.");
   		this.options.setLogTimeFormatString(olm);
	}

    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = DGSPreviewerApp.getApplication().getMainFrame();
            aboutBox = new DGSPreviewerAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        DGSPreviewerApp.getApplication().show(aboutBox);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        jSplitPane1 = new javax.swing.JSplitPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        imagePanel = new DGSPreviewerPanel();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        openMenuItem = new javax.swing.JMenuItem();
	loadVarsMenuItem = new javax.swing.JMenuItem();
        refreshMenuItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();

        mainPanel.setName("mainPanel"); // NOI18N

        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane1.setName("jSplitPane1"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jTextArea1.setName("logDisplay"); // NOI18N
        jScrollPane1.setViewportView(jTextArea1);

        jSplitPane1.setRightComponent(jScrollPane1);

        imagePanel.setName("imagePanel"); // NOI18N

        javax.swing.GroupLayout imagePanelLayout = new javax.swing.GroupLayout(imagePanel);
        imagePanel.setLayout(imagePanelLayout);
        imagePanelLayout.setHorizontalGroup(
            imagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 398, Short.MAX_VALUE)
        );
        imagePanelLayout.setVerticalGroup(
            imagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        jSplitPane1.setLeftComponent(imagePanel);

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 249, Short.MAX_VALUE)
        );

        menuBar.setName("menuBar"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(dgspreviewer.DGSPreviewerApp.class).getContext().getResourceMap(DGSPreviewerView.class);
        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(dgspreviewer.DGSPreviewerApp.class).getContext().getActionMap(DGSPreviewerView.class, this);

        openMenuItem.setAction(actionMap.get("loadFile")); // NOI18N
        openMenuItem.setName("openMenuItem"); // NOI18N
        fileMenu.add(openMenuItem);

	loadVarsMenuItem.setAction(actionMap.get("loadVarsFile")); // NOI18N
	loadVarsMenuItem.setName("loadVarsMenuItem"); // NOI18N
	fileMenu.add(loadVarsMenuItem);

        refreshMenuItem.setAction(actionMap.get("refreshImage")); // NOI18N
        refreshMenuItem.setName("jMenuItemRefresh"); // NOI18N
        fileMenu.add(refreshMenuItem);

        jSeparator1.setName("jSeparator1"); // NOI18N
        fileMenu.add(jSeparator1);

        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        statusPanel.setName("statusPanel"); // NOI18N

        statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N

        statusMessageLabel.setName("statusMessageLabel"); // NOI18N

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

        progressBar.setName("progressBar"); // NOI18N

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusMessageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 230, Short.MAX_VALUE)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusAnimationLabel)
                .addContainerGap())
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addComponent(statusPanelSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(statusMessageLabel)
                    .addComponent(statusAnimationLabel)
                    .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3))
        );

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
    }// </editor-fold>//GEN-END:initComponents

    @Action
    public void loadFile() {
        JFileChooser fc;
		String MRUTemplateImageFileName = this.options.getMRUTemplateImageFileName();
        if(MRUTemplateImageFileName.length() > 0) {
            fc = new JFileChooser(MRUTemplateImageFileName);
        } else {
            fc = new JFileChooser();
        }

		FileNameExtensionFilter filter = new FileNameExtensionFilter("DGS Image Templates (*.svg)", "svg", "svgz");
		fc.setFileFilter(filter);
        int choice = fc.showOpenDialog(mainPanel);
        if (choice == JFileChooser.APPROVE_OPTION) {
            java.io.File f = fc.getSelectedFile();
            this.setStatusMessage(100, "Loading image: " + f.getPath());
            loadImageFile(f.getPath());
        }
    }

    @Action
    public void loadVarsFile() {
        JFileChooser fc;
		String MRUTemplateVariablesFileName = this.options.getMRUTemplateVariablesFileName();
        if(MRUTemplateVariablesFileName.length() > 0) {
            fc = new JFileChooser(MRUTemplateVariablesFileName);
        } else {
            fc = new JFileChooser();
        }

		FileNameExtensionFilter filter = new FileNameExtensionFilter("DGS Template Package files (*.xml)", "xml");
		fc.setFileFilter(filter);
        int choice = fc.showOpenDialog(mainPanel);
        if (choice == JFileChooser.APPROVE_OPTION) {
            java.io.File f = fc.getSelectedFile();
            this.setStatusMessage(50, "Loading variable package: " + f.getPath());
			if(loadVariablesFile(f.getPath())) {
				this.setStatusMessage(100, "File loaded, refreshing image.");
				refreshImage();
			} else {
				this.setStatusMessage(10, "Variable file could not parsed." + f.getPath());
			}
        }
    }

	@Action
    public void refreshImage() {
		String MRUTemplateImageFileName = this.options.getMRUTemplateImageFileName();
        if(MRUTemplateImageFileName.length() > 0) {
            this.logMessage(100, "Reloading " + MRUTemplateImageFileName + "...");
            if(!loadImageFile(MRUTemplateImageFileName)) {
                this.logMessage(10, "");
                this.setStatusMessage(10, "Reload failed.");
                this.logMessage(10, "");
            } else {
				this.setStatusMessage(100, "Reload completed.");
			}
        }
    }

    private DGSFileInfo loadImageFileData(String fileName)
    {
        byte fDat[] = null;
        java.io.File f = new java.io.File(fileName);
        if(!f.exists()) {
            setStatusMessage(10, "File does not exist: " + fileName);
            return(null);
        }
        try {
            fDat = fileToBytes(fileName);
        } catch (FileNotFoundException fex) {
            setStatusMessage(10, "Could not find the specified file: " + fileName);
            return(null);
        } catch (IOException iex) {
            setStatusMessage(10, "Could not read the specified file: " + fileName + " Error: " + iex.getMessage());
            return(null);
        }
        if(fDat == null) {
            setStatusMessage(5, "An unknown error occurred reading file: " + fileName);
            return(null);
        }
        if(fDat.length == 0) {
            setStatusMessage(10, "The specified file is empty: " + fileName);
            return(null);
        }
        DGSFileInfo fInfo = new DGSFileInfo();
        fInfo.data = fDat;
        fInfo.name = f.getName();
        fInfo.width = -1;
        fInfo.height = -1;
        return(fInfo);
    }

    private boolean loadImageFile(String fileName)
    {
        imagePanel.image = null;
        imagePanel.invalidate();
        imagePanel.repaint();

        String outputMimeType = "image/png";

        DGSRequestInfo dgsRequestInfo = new DGSRequestInfo();
        dgsRequestInfo.continueOnError = true;

        setStatusMessage(200, "Reading image file: " + fileName);
        DGSFileInfo templateFileInfo = loadImageFileData(fileName);
        if(templateFileInfo == null) {
            setStatusMessage(10, "Load aborted due to errors: " + fileName);
            return(false);
        }

		// the file itself exists and could be read, thats good enough to store it in the MRU slot so it can be debugged if there is an error in the svg
		this.options.setMRUTemplateImageFileName(fileName);

		
		DGSFileInfo replacementImages[] = null;
		
		String MRUTemplateVariablesFileName = this.options.getMRUTemplateVariablesFileName();
		if((MRUTemplateVariablesFileName == null) || (MRUTemplateVariablesFileName.length() == 0)) {
			dgsRequestInfo.files = new DGSFileInfo[1];
			dgsRequestInfo.variables = null;
		} else {
			dgsRequestInfo.variables = loadVariables(MRUTemplateVariablesFileName);
			replacementImages = loadImageFiles(MRUTemplateVariablesFileName);
			if(replacementImages == null) {
				setStatusMessage(10, "Could not load the user variables file specified: " + fileName);
				dgsRequestInfo.files = new DGSFileInfo[1];
			} else {
				if(replacementImages.length==0) {
					setStatusMessage(100, "The file specified does not contain any replacement images: " + fileName);
					dgsRequestInfo.files = new DGSFileInfo[1];
				} else {
					setStatusMessage(100, replacementImages.length + " replacement image(s) found.");
					dgsRequestInfo.files = new DGSFileInfo[replacementImages.length + 1];
					for(int i = 0; i < replacementImages.length; i++) {
						dgsRequestInfo.files[i+1] = replacementImages[i];
					}
				}
			}
		}

		dgsRequestInfo.files[0] = templateFileInfo;
        if((dgsRequestInfo.files[0].name == null) || (dgsRequestInfo.files[0].name.length() == 0)) {
			dgsRequestInfo.files[0].name = "input.svg"; // we need this set to Something, so set it ourselves
		}
//        if((dgsRequestInfo.files[0].mimeType == null) || (dgsRequestInfo.files[0].mimeType.length() == 0)) {
			dgsRequestInfo.files[0].mimeType = "image/svg+xml"; // we only process svg for now
//		}


		// Form the instruction xml fragment
        dgsRequestInfo.instructionsXML = "<commands><load filename=\"" + dgsRequestInfo.files[0].name + "\" buffer=\"main\" mimeType=\"image/svg+xml\" />";
        if(dgsRequestInfo.variables != null && dgsRequestInfo.variables.length > 0) {
            dgsRequestInfo.instructionsXML += "<substituteVariables buffer=\"main\" />";
            this.logMessage(100, "Loaded " + dgsRequestInfo.variables.length + " variables.");
        }

        if(dgsRequestInfo.files.length > 1) {
            for(int i = 1; i<dgsRequestInfo.files.length; i++) {
                this.logMessage(100, "Replacing image: " + dgsRequestInfo.files[i].name);
                dgsRequestInfo.instructionsXML += "<load filename=\"" + dgsRequestInfo.files[i].name + "\" buffer=\"" + dgsRequestInfo.files[i].name + "\" mimeType=\"image/" + dgsRequestInfo.files[i].mimeType + "\" /><replaceImage buffer=\"main\" srcImage=\"" + dgsRequestInfo.files[i].name + "\" imageElementId=\"" + dgsRequestInfo.files[i].name + "\" halign=\"center\" valign=\"center\" />";
            }
            this.logMessage(100, "Loaded " + (dgsRequestInfo.files.length-1) + " image files.");
        }
        dgsRequestInfo.instructionsXML += "<save snapshotTime=\"1.0\" filename=\"output.png\" buffer=\"main\" mimeType=\"" + outputMimeType + "\" /></commands>";

        ProcessingWorkspace workspace = new ProcessingWorkspace(dgsRequestInfo);
        setStatusMessage(150, "Performing DGS Request ...");
        DGSResponseInfo dgsResponseInfo = pEngine.processCommandString(workspace);
        setStatusMessage(150, " Request completed.");
        
        this.logMessage(200, "DGS Request Log: ");
        for(int i = 0; i < dgsResponseInfo.processingLog.length; i++) {
            this.logMessage(200, "     " + dgsResponseInfo.processingLog[i]);
        }
        this.logMessage(200, "-- END DGS Request Log --");
        if(dgsResponseInfo.resultFiles.length == 0) {
            String plog[] = new String[dgsResponseInfo.processingLog.length];
            for(int i = 0; i<dgsResponseInfo.processingLog.length; i++) {
                plog[i] = dgsResponseInfo.processingLog[i];
            }
            javax.swing.JOptionPane.showMessageDialog(mainPanel, plog);
            setStatusMessage(10, "No image files were returned by the processing engine, this generally indicates an error in the input file: " + fileName);
            return(false);
        }

        setStatusMessage(200, "Updating display with new image ...");
        BufferedImage image = null;
        try {
            image = ImageIO.read(new java.io.ByteArrayInputStream((byte[])dgsResponseInfo.resultFiles[0].data));
        } catch (IOException ie) {
            setStatusMessage(5, "Error processing output image: " + ie.getMessage());
        }
        imagePanel.image = image;
        this.imagePanel.repaint();
        return(true);
    }

	// Currently this function just verifies that the variables file can be loaded and updates 
	private boolean loadVariablesFile(String fileName)
	{
		DGSFileInfo replacementImages[] = null;
		
		DGSVariable vars[] = loadVariables(fileName);
		if(vars==null) {
			setStatusMessage(10, "Could not load the user variables file specified: " + fileName);
			return(false);
		}
		replacementImages = loadImageFiles(fileName);
		if(replacementImages == null) {
			setStatusMessage(10, "Could not load the user variables file specified, an error occured while parsing the image variables: " + fileName);
			return(false);
		}
		options.setMRUTemplateVariablesFileName(fileName);
        return(true);
	}

	private byte[] fileToBytes(String fileName) throws FileNotFoundException, IOException
    {
        byte fDat[] = new byte[0];

        FileInputStream fs = new FileInputStream(fileName);
        int i = fs.available();
        fDat = new byte[i];
        i = fs.read(fDat);
        fs.close();
        return(fDat);
    }

    private DGSVariable[] loadVariables(String varFileName)
    {
        DGSVariable vars[] = null;
        File file = null;
        DocumentBuilderFactory dbf = null;
        DocumentBuilder db = null;
        Document doc = null;
        try {
            file = new File(varFileName);
            dbf = DocumentBuilderFactory.newInstance();
            db = dbf.newDocumentBuilder();
        } catch (Exception ex) {
            ex.printStackTrace();
            return(null);
        }
        try {
            doc = db.parse(file);
            doc.getDocumentElement().normalize();
        } catch (Exception ex) {
            ex.printStackTrace();
            return(null);
        }
        try {
            NodeList nodeLst = doc.getElementsByTagName("DGSVariable");

            int nLen = nodeLst.getLength();
            vars = new DGSVariable[nLen];
            for (int s = 0; s < nLen; s++) {
                Node fstNode = nodeLst.item(s);
                if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
                    NamedNodeMap aMap = fstNode.getAttributes();
                    vars[s] = new DGSVariable(aMap.getNamedItem("name").getNodeValue(), aMap.getNamedItem("value").getNodeValue());
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return(null);
        }

        return(vars);
    }

    private DGSFileInfo[] loadImageFiles(String varFileName)
    {
        DGSFileInfo vars[] = null;
        File file = null;
        DocumentBuilderFactory dbf = null;
        DocumentBuilder db = null;
        Document doc = null;
        try {
            file = new File(varFileName);
            dbf = DocumentBuilderFactory.newInstance();
            db = dbf.newDocumentBuilder();
        } catch (Exception ex) {
            ex.printStackTrace();
            setStatusMessage(10, "loadImageFiles: An error occurred creating the XML document context: " + ex.getMessage());
            return(null);
        }
        try {
            doc = db.parse(file);
            doc.getDocumentElement().normalize();
        } catch (Exception ex) {
			setStatusMessage(10, "loadImageFiles: An error occurred parsing the XML document (" + varFileName + "): " + ex.getLocalizedMessage());
            ex.printStackTrace();
            return(null);
        }
        try {
            NodeList nodeLst = doc.getElementsByTagName("DGSImageVariable");

            int nLen = nodeLst.getLength();
            vars = new DGSFileInfo[nLen];
            for (int s = 0; s < nLen; s++) {
                Node fstNode = nodeLst.item(s);
                if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
                    NamedNodeMap aMap = fstNode.getAttributes();
                    vars[s] = new DGSFileInfo();
                    vars[s].name = aMap.getNamedItem("name").getNodeValue();
                    vars[s].data = ImageProcessor.ProcessingEngine.Base64.decode(aMap.getNamedItem("data").getNodeValue());
                    if("jpg".equalsIgnoreCase(aMap.getNamedItem("mimeType").getNodeValue())) {
                            vars[s].mimeType = "image/jpeg";
                    } else {
                            vars[s].mimeType = "image/" + aMap.getNamedItem("mimeType").getNodeValue();
                    }
                    vars[s].width = Integer.valueOf(aMap.getNamedItem("width").getNodeValue());
                    vars[s].height = Integer.valueOf(aMap.getNamedItem("height").getNodeValue());
                }
            }
        } catch (Exception ex) {
			setStatusMessage(10, "loadImageFiles: An error occurred parsing the variable data in" + varFileName + "\": " + ex.getLocalizedMessage());
            ex.printStackTrace();
            return(null);
        }

        return(vars);
    }

    private void logMessage(int LogLevel, String Message)
    {
		int cLevel = this.options.getLogLevel();
		if(LogLevel>cLevel) {
			return;
		}
		jTextArea1.setText(jTextArea1.getText() + (new java.text.SimpleDateFormat(this.options.getLogTimeFormatString())).format(Calendar.getInstance().getTime()) + Message + "\r\n");
    }

    private void setStatusMessage(int LogLevel, String Message)
    {
        statusMessageLabel.setText(Message);
        statusMessageLabel.repaint();
		logMessage(LogLevel, Message);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private DGSPreviewerPanel imagePanel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem openMenuItem;
    private javax.swing.JMenuItem loadVarsMenuItem;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JMenuItem refreshMenuItem;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    // End of variables declaration//GEN-END:variables

    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;

    private JDialog aboutBox;
}
