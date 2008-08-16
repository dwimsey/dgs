/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dgspreviewer;

import java.awt.*;
/**
 *
 * @author dwimsey
 */
public class Options {
    private java.awt.Color BackgroundColor;
    private String MRUTemplateImageFileName;
    private String MRUTemplateVariablesFileName;
	private int LogLevel;

	public Options()
    {
        super();
        this.BackgroundColor = new Color(0xFFFFFFFF);
        this.MRUTemplateImageFileName = "";
        this.MRUTemplateVariablesFileName = "";
		this.LogLevel = 255;
    }
    
    public boolean load()
    {
        return(true);
    }
    
    private boolean save()
    {
        return(true);
    }

	public Color getBackgroundColor()
	{
		return(this.BackgroundColor);
	}
	
	public Color setBackgroundColor(Color newColor)
	{
		Color oldColor = this.BackgroundColor;
		if(newColor.equals(oldColor)) {
			return(oldColor);
		}
		this.BackgroundColor = newColor;
		this.save();
		return(oldColor);
	}
	
	public int getLogLevel()
	{
		return(this.LogLevel);
	}
	
	public int setLogLevel(int newLevel)
	{
		if(this.LogLevel==newLevel) {
			return(newLevel);
		}
		int oldLevel = this.LogLevel;
		this.LogLevel = newLevel;
		this.save();
		return(oldLevel);
	}
	public String getMRUTemplateImageFileName()
	{
		return(this.MRUTemplateImageFileName);
	}

	public String setMRUTemplateImageFileName(String newFileName)
	{
		String oldName = this.MRUTemplateImageFileName;
		if(newFileName == null) {
			newFileName = "";
		}
		if(newFileName.equals(oldName)) {
			return(oldName);
		}

		this.MRUTemplateImageFileName = newFileName;
		this.save();
		return(oldName);
	}

	public String getMRUTemplateVariablesFileName()
	{
		return(this.MRUTemplateVariablesFileName);
	}

	public String setMRUTemplateVariablesFileName(String newFileName)
	{
		String oldName = this.MRUTemplateVariablesFileName;
		if(newFileName == null) {
			newFileName = "";
		}
		if(newFileName.equals(oldName)) {
			return(oldName);
		}

		this.MRUTemplateVariablesFileName = newFileName;
		this.save();
		return(oldName);
	}
}
