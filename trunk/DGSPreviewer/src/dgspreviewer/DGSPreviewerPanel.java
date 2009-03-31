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
	public java.awt.Color BackgroundColor = new java.awt.Color(0x0000FFFF);

	public void paint(java.awt.Graphics g) {
		java.awt.Rectangle r = g.getClipBounds();
		if (BackgroundColor == null) {
			g.clearRect(r.x, r.y, r.width, r.height);
		} else {
			g.setColor(BackgroundColor);
			g.fillRect(r.x, r.y, r.width, r.height);
		}

		if (image != null) {
			g.drawImage(image, 0, 0, null);
		}
	}
}
