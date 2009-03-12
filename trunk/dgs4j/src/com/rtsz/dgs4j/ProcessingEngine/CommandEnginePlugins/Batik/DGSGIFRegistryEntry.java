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

        final DeferRable  dr  = new DeferRable();
        final InputStream is  = inIS;
        final boolean     raw = needRawData;
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
	
	
	
	
	public Filter handleStream_ORIG(InputStream inIS,
                               ParsedURL   origURL,
                               boolean needRawData) {

        final DeferRable  dr  = new DeferRable();
        final InputStream is  = inIS;
        final boolean     raw = needRawData;
        final String      errCode;
        final Object []   errParam;
        if (origURL != null) {
            errCode  = ERR_URL_FORMAT_UNREADABLE;
            errParam = new Object[] {"PNG", origURL};
        } else {
            errCode  = ERR_STREAM_FORMAT_UNREADABLE;
            errParam = new Object[] {"PNG"};
        }

        Thread t = new Thread() {
                public void run() {
                    Filter filt;
                    try {
//                        PNGDecodeParam param = new PNGDecodeParam();
//                        param.setExpandPalette(true);

                        if (raw) {
//                            param.setPerformGammaCorrection(false);
						} else {
//                            param.setPerformGammaCorrection(true);
//                            param.setDisplayExponent(2.2f); // sRGB gamma
                        }
//                        
//						CachableRed cr = new PNGRed(is, param);
						BufferedImage bi = javax.imageio.ImageIO.read(is);
						CachableRed cr = GraphicsUtil.wrap(bi);

						dr.setBounds(new Rectangle2D.Double (0, 0, cr.getWidth(), cr.getHeight()));
						cr = new Any2sRGBRed(cr);
						cr = new FormatRed(cr, GraphicsUtil.sRGB_Unpre);
						WritableRaster wr = (WritableRaster)cr.getData();
						ColorModel cm = cr.getColorModel();
						BufferedImage image;
						image = new BufferedImage(cm, wr, cm.isAlphaPremultiplied(), null);
						cr = GraphicsUtil.wrap(image);
						filt = new RedRable(cr);
					} catch (IOException ioe) {
						filt = ImageTagRegistry.getBrokenLinkImage
							(DGSGIFRegistryEntry.this, errCode, errParam);
					} catch (ThreadDeath td) {
						filt = ImageTagRegistry.getBrokenLinkImage
							(DGSGIFRegistryEntry.this, errCode, errParam);
						dr.setSource(filt);
						throw td;
					} catch (Throwable t) {
						filt = ImageTagRegistry.getBrokenLinkImage
							(DGSGIFRegistryEntry.this, errCode, errParam);
					}
					dr.setSource(filt);
				}
            };
        t.start();
        return dr;
    }	
}
