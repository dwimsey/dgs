/*
 * DGSPreviewerApp.java
 */

package dgspreviewer;

import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;

import org.apache.batik.util.ApplicationSecurityEnforcer;

import ImageProcessor.ProcessingEngine.CommandEnginePlugins.Batik.*;

/**
 * The main class of the application.
 */
public class DGSPreviewerApp extends SingleFrameApplication {
	static String args[] = null;
    private DGSPreviewerApp()
    {
        super();
		
		// Apply script security option
        ApplicationSecurityEnforcer securityEnforcer =
            new ApplicationSecurityEnforcer(this.getClass(),
                                            "dgspreviewer/resources/DGSPreviewer.policy");

        securityEnforcer.enforceSecurity(false);

		DGSWorkspaceParsedURLProtocolHandler ph = new DGSWorkspaceParsedURLProtocolHandler();
		org.apache.batik.util.ParsedURL.registerHandler(ph);
		
    }

    /**
     * At startup create and show the main frame of the application.
     */
    @Override protected void startup() {
        show(new DGSPreviewerView(this));
    }

    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     */
    @Override protected void configureWindow(java.awt.Window root) {
    }

    /**
     * A convenient static getter for the application instance.
     * @return the instance of DGSPreviewerApp
     */
    public static DGSPreviewerApp getApplication() {
        return Application.getInstance(DGSPreviewerApp.class);
    }

    /**
     * Main method launching the application.
     */
    public static void main(String[] args) {		
		Application.getInstance(DGSPreviewerApp.class).args = args;
        launch(DGSPreviewerApp.class, args);
    }
}
