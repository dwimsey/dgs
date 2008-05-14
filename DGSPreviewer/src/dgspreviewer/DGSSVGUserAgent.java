/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dgspreviewer;

import org.apache.batik.swing.svg.*;
import org.apache.batik.util.*;
import org.apache.batik.bridge.*;
/**
 *
 * @author dwimsey
 */
// this class prevents the use of scripts or externally referenced entities of any type.  Internal references via the data: protocol are allowed
public class DGSSVGUserAgent extends SVGUserAgentAdapter {
    public ScriptSecurity getScriptSecurity2(java.lang.String scriptType, ParsedURL scriptURL, ParsedURL docURL)
    {
        ScriptSecurity ss = null;
        ss = new org.apache.batik.bridge.NoLoadScriptSecurity(scriptType);
        return(ss);
    }

    public ExternalResourceSecurity getExternalResourceSecurity(ParsedURL resourceURL, ParsedURL docURL)
    {
        ExternalResourceSecurity rs = null;
        rs = new org.apache.batik.bridge.EmbededExternalResourceSecurity(docURL);
        //rs = new NoLoadExternalResourceSecurity();
        return(rs);
    }

    public void displayError(String arg0)
    {
    }

    public void displayError(Exception arg0)
    {
    }

    public void displayMessage(String arg0)
    {
    }
}
