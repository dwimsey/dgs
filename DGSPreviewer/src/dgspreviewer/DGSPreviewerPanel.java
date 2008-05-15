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
    
    public void paint(java.awt.Graphics g)
    {
        java.awt.Rectangle r = g.getClipBounds();
        //g.clearRect(r.x, r.y, r.width, r.height);

        java.awt.Color c = new java.awt.Color(255,255,255);
        g.setColor(c);
        g.fillRect(r.x, r.y, r.width, r.height);

        if(image != null) {
            g.drawImage(image, 0, 0, null);
        }
    }
}
