/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dgspreviewer;

import java.awt.image.BufferedImage;

/**
 *
 * @author dwimsey
 */
public class DGSPreviewerPanel extends javax.swing.JPanel {
    public BufferedImage image = null;
    public dgspreviewer.Options options = null;

    public void paint(java.awt.Graphics g)
    {
        if(options==null) {
            super.paint(g);
            return;
        }
        java.awt.Rectangle r = g.getClipBounds();
        if(options.backgroundColor == null) {
            g.clearRect(r.x, r.y, r.width, r.height);
        } else {
            g.setColor(options.backgroundColor);
            g.fillRect(r.x, r.y, r.width, r.height);
        }
        
        if(image != null) {
            g.drawImage(image, 0, 0, null);
        }
    }
}
