/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ImageProcessor.ProcessingEngine.CommandEnginePlugins.Batik;

import org.w3c.dom.*;
import org.apache.batik.bridge.*;
import org.apache.batik.util.*;

import org.apache.batik.gvt.text.*;

import java.awt.*;
import java.awt.geom.*;

/**
 *
 * @author dwimsey
 */
public class DGSUserAgent implements UserAgent {
    private UserAgent ua;
    private ImageProcessor.ProcessingEngine.ProcessingWorkspace workspace;

    public DGSUserAgent(UserAgent oldAgent, ImageProcessor.ProcessingEngine.ProcessingWorkspace cWorkspace)
    {
        if(oldAgent==null) {
            throw new java.lang.NullPointerException("oldAgent can not be null when creating a new DGSUserAgent");
        }
        if(cWorkspace==null) {
            throw new java.lang.NullPointerException("cWorkspace can not be null when creating a new DGSUserAgent");
        }
        ua = oldAgent;
        workspace = cWorkspace;
    }
    
    public void checkLoadExternalResource(ParsedURL resourceURL, ParsedURL docURL) throws SecurityException
    {
        if(resourceURL == null) {
            throw new SecurityException("NULL resource URLs can not be handled at this time.");
        }

        String rProto = "";
        if(resourceURL.getProtocol() == null) {
            if(docURL.getProtocol() == null) {
                throw new SecurityException("Could not determine the protocol used to reference this external resource.  Document URL: " + docURL.toString() + " Resource URL: " + resourceURL.toString());
            } else {
                rProto = docURL.getProtocol();
            }
        } else {
            rProto = resourceURL.getProtocol();
        }

        if(!rProto.equals("data")) {
            throw new SecurityException("Only embedded resource links are allowed.");
        }
        ua.checkLoadExternalResource(resourceURL, docURL);
    }

    public void checkLoadScript(String scriptType, ParsedURL scriptURL, ParsedURL docURL) throws SecurityException
    {
        if(scriptURL == null) {
            throw new SecurityException("NULL script resource URLs can not be handled at this time.");
        }

        String rProto = "";
        if(scriptURL.getProtocol() == null) {
            if(docURL.getProtocol() == null) {
                throw new SecurityException("Could not determine the protocol used to reference this script resource.  Document URL: " + docURL.toString() + " Script URL: " + scriptURL.toString());
            } else {
                rProto = docURL.getProtocol();
            }
        } else {
            rProto = scriptURL.getProtocol();
        }

        if(!rProto.equals("data")) {
            throw new SecurityException("Only embedded script resources are allowed.");
        }
        
        throw new SecurityException("Scripts are not allowed in this context.");
//        ua.checkLoadScript(scriptType, scriptURL, docURL);
    }

    public void deselectAll()
    {
        ua.deselectAll();
    }

    public void displayError(Exception ex)
    {
        workspace.log("Processing Error: " + ex.getClass().getCanonicalName() + ": " + ex.getMessage());
        //ua.displayError(ex);
    }

    public void displayMessage(String message)
    {
        workspace.log(message);
        //ua.displayMessage(message);
    }

    public String getAlternateStyleSheet()
    {
        return(ua.getAlternateStyleSheet());
    }

    public float getBolderFontWeight(float f)
    {
        return(ua.getBolderFontWeight(f));
    }

    public org.w3c.dom.svg.SVGDocument getBrokenLinkDocument(Element e, String url, String message)
    {
        return(ua.getBrokenLinkDocument(e, url, message));
    }

    public Point getClientAreaLocationOnScreen()
    {
        return(ua.getClientAreaLocationOnScreen());
    }

    public String getDefaultFontFamily()
    {
        return(ua.getDefaultFontFamily());
    }

    public org.apache.batik.gvt.event.EventDispatcher getEventDispatcher()
    {
        return(ua.getEventDispatcher());
    }

    public ExternalResourceSecurity getExternalResourceSecurity(ParsedURL resourceURL, ParsedURL docURL)
    {
        return(ua.getExternalResourceSecurity(resourceURL, docURL));
    }

    public String getLanguages()
    {
        return(ua.getLanguages());
    }

    public float getLighterFontWeight(float f)
    {
        return(ua.getLighterFontWeight(f));
    }
    
    public String getMedia()
    {
        return(ua.getMedia());
    }

    public float getMediumFontSize()
    {
        return(ua.getMediumFontSize());
    }

    public float getPixelToMM()
    {
        return(ua.getPixelToMM());
    }

    public float getPixelUnitToMillimeter()
    {
        return(ua.getPixelUnitToMillimeter());
    }

    public ScriptSecurity getScriptSecurity(String scriptType, ParsedURL scriptURL, ParsedURL docURL)
    {
        return(ua.getScriptSecurity(scriptType, scriptURL, docURL));
    }

    public AffineTransform getTransform()
    {
        return(ua.getTransform());
    }

    public String getUserStyleSheetURI()
    {
        return(ua.getUserStyleSheetURI());
    }

    public Dimension2D getViewportSize()
    {
        return(ua.getViewportSize());
    }

    public String getXMLParserClassName()
    {
        return(ua.getXMLParserClassName());
    }

    public void handleElement(Element elt, Object data)
    {
        ua.handleElement(elt, data);
    }
    
    public boolean hasFeature(String s)
    {
        return(ua.hasFeature(s));
    }

    public boolean isXMLParserValidating()
    {
        return(ua.isXMLParserValidating());
    }

    public void openLink(org.w3c.dom.svg.SVGAElement elt)
    {
        ua.openLink(elt);
    }

    public void registerExtension(BridgeExtension ext)
    {
        ua.registerExtension(ext);
    }

    public void setSVGCursor(Cursor cursor)
    {
        ua.setSVGCursor(cursor);
    }

    public void setTextSelection(Mark start, Mark end)
    {
        ua.setTextSelection(start, end);
    }

    public void setTransform(AffineTransform at)
    {
        ua.setTransform(at);
    }

    public void showAlert(String message)
    {
        ua.showAlert(message);
    }

    public boolean showConfirm(String message)
    {
        return(ua.showConfirm(message));
    }

    public String showPrompt(String message)
    {
        return(ua.showPrompt(message));
    }

    public String showPrompt(String message, String defaultValue)
    {
        return(ua.showPrompt(message, defaultValue));
    }

    public  boolean supportExtension(String s)
    {
        return(ua.supportExtension(s));
    }
}

