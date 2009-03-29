package rts.dgs.plugins.sketsa.dgstools.actions;

import java.awt.EventQueue;
import kiyut.sketsa.actions.AbstractEditorAction;
import kiyut.sketsa.canvas.CanvasModel;
import kiyut.sketsa.canvas.VectorCanvas;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;


/**
 * Action that Show Export As PDF Dialog
 * @author Kiyut
 */
public class ShowDGSOptionsAction extends AbstractEditorAction {
    static DGSGIFRegistryEntry gifRegistryEntry = null;
	public ShowDGSOptionsAction() {
		super();
		if(gifRegistryEntry == null) {
			gifRegistryEntry = new DGSGIFRegistryEntry();
			org.apache.batik.ext.awt.image.spi.ImageTagRegistry ir = org.apache.batik.ext.awt.image.spi.ImageTagRegistry.getRegistry();
			ir.register(gifRegistryEntry);
		}
	}
    /** {@inheritDoc} */
    public void performAction() {
        if (editorCookie == null) {
            return;
        }

		EventQueue.invokeLater(new Runnable() {
            public void run() {
                VectorCanvas canvas = editorCookie.getVectorCanvas();
                
                java.awt.Frame mainFrame = WindowManager.getDefault().getMainWindow();
                
                CanvasModel canvasModel = canvas.getModel();
                java.awt.geom.Rectangle2D rect = new java.awt.geom.Rectangle2D.Double(0,0,canvasModel.getSVGWidth(),canvasModel.getSVGHeight());
                
                ShowDGSOptionsWindow exportDialog = new ShowDGSOptionsWindow(mainFrame,true);
                exportDialog.setSVGDocument(canvas.getSVGDocument());
                exportDialog.setArea(rect);
                exportDialog.pack();
                exportDialog.setLocationRelativeTo(mainFrame);
                exportDialog.setVisible(true);
                exportDialog = null;
            }
        });
    }

    /** {@inheritDoc} 
     * @return {@inheritDoc} 
     */
    public String getName() {
        return NbBundle.getMessage(ShowDGSOptionsAction.class, "CTL_ShowDGSOptionsAction");
    }
    
}
