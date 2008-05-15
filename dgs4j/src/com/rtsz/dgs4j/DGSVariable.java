/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ImageProcessor;

/**
 *
 * @author dwimsey
 */
public class DGSVariable {

    public String name = "";
    public String data = "";
    public boolean visibility = true;
    public static final int TYPE_TEXT           = 0;
    public static final int TYPE_VISIBILITY     = 1;

    public DGSVariable(String nName, String nValue)
    {
        this.name = nName;
        this.data = nValue;
    }
}
