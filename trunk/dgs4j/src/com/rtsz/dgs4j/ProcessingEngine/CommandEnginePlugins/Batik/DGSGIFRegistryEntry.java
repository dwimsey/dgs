/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ImageProcessor.ProcessingEngine.CommandEnginePlugins.Batik;

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.InputStream;

import org.apache.batik.ext.awt.image.GraphicsUtil;
import org.apache.batik.ext.awt.image.renderable.DeferRable;
import org.apache.batik.ext.awt.image.renderable.Filter;
import org.apache.batik.ext.awt.image.renderable.RedRable;
import org.apache.batik.ext.awt.image.rendered.Any2sRGBRed;
import org.apache.batik.ext.awt.image.rendered.CachableRed;
import org.apache.batik.ext.awt.image.rendered.FormatRed;
import org.apache.batik.ext.awt.image.spi.ImageTagRegistry;
import org.apache.batik.ext.awt.image.spi.MagicNumberRegistryEntry;
import org.apache.batik.util.ParsedURL;
/**
 *
 * @author dwimsey
 */
public class DGSGIFRegistryEntry extends MagicNumberRegistryEntry {
    static final byte [] sigGIF87 = {(byte)0x47, 0x49, 0x46, 0x38, 0x37, 0x61};
	static final byte [] sigGIF89 = {(byte)0x47, 0x49, 0x46, 0x38, 0x39, 0x61};

	static MagicNumberRegistryEntry.MagicNumber [] magicNumbers = {
        new MagicNumberRegistryEntry.MagicNumber(0, sigGIF87),
        new MagicNumberRegistryEntry.MagicNumber(0, sigGIF89)
	};

	private boolean entryEnabled = true;
	public synchronized boolean isEnabled()
	{
		return(entryEnabled);
	}
	public synchronized boolean enable()
	{
		boolean old = entryEnabled;
		entryEnabled = true;
		return(old);
	}
	public synchronized boolean disable()
	{
		boolean old = entryEnabled;
		entryEnabled = false;
		return(old);
	}
	public DGSGIFRegistryEntry() {
		super("GIF", "gif", "image/gif", magicNumbers);
    }
/**
     * Decode the Stream into a RenderableImage
     *
     * @param inIS The input stream that contains the image.
     * @param origURL The original URL, if any, for documentation
     *                purposes only.  This may be null.
     * @param needRawData If true the image returned should not have
     *                    any default color correction the file may
     *                    specify applied.  */
    public Filter handleStream(InputStream inIS,
                               ParsedURL   origURL,
                               boolean needRawData) {
		if(!this.isEnabled()) {
			// I can not remember WHERE I saw this documented, but the API takes
			// a null return value to mean that this registry entry was unable to
			// deal with the url provided but not that the data is invalid, which
			// effectively causes this entry to be ignored, as if it were never
			// registered.
			return null;
		}
  
		final DeferRable  dr  = new DeferRable();
        final InputStream is  = inIS;
        final String      errCode;
        final Object []   errParam;
        if (origURL != null) {
            errCode  = ERR_URL_FORMAT_UNREADABLE;
            errParam = new Object[] {"GIF", origURL};
        } else {
            errCode  = ERR_STREAM_FORMAT_UNREADABLE;
            errParam = new Object[] {"GIF"};
        }
      Thread t = new Thread() {
                public void run() {
                    Filter filt;
                    try {
						BufferedImage img = javax.imageio.ImageIO.read(is);
						dr.setBounds(new java.awt.Rectangle(img.getWidth(), img.getHeight()));
						BufferedImage bi = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
						java.awt.Graphics2D g2d = bi.createGraphics();
						g2d.drawImage(img, 0, 0, null);
						g2d.dispose();
						filt = new RedRable(GraphicsUtil.wrap(bi));
					} catch (IOException ioe) {
						filt = ImageTagRegistry.getBrokenLinkImage(DGSGIFRegistryEntry.this, errCode, errParam);
					} catch (ThreadDeath td) {
						filt = ImageTagRegistry.getBrokenLinkImage(DGSGIFRegistryEntry.this, errCode, errParam);
						dr.setSource(filt);
						throw td;
					} catch (Throwable t) {
						filt = ImageTagRegistry.getBrokenLinkImage(DGSGIFRegistryEntry.this, errCode, errParam);
					}
					dr.setSource(filt);
				}
            };
        t.start();
        return dr;
    }	
}
