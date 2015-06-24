/*
 * org.openmicroscopy.shoola.agents.metadata.util.ScriptMenuItem 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.util.ui;


//Java imports
import java.util.ArrayList;
import java.util.List;
import javax.swing.JMenuItem;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.agents.metadata.MetadataViewerAgent;
import org.openmicroscopy.shoola.env.data.model.AnalysisParam;
import org.openmicroscopy.shoola.env.data.model.FigureParam;
import org.openmicroscopy.shoola.env.data.model.MovieExportParam;
import org.openmicroscopy.shoola.env.data.model.ScriptObject;

/** 
 * Displays the script.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class ScriptMenuItem 
	extends JMenuItem
{
	
	/** Identifies the ROI figure script. */
	public static final int ROI_FIGURE_SCRIPT = 0;
	
	/** Identifies the Thumbnail figure script. */
	public static final int THUMBNAIL_FIGURE_SCRIPT = 1;
	
	/** Identifies the movie figure script. */
	public static final int MOVIE_FIGURE_SCRIPT = 2;
	
	/** Identifies the split view figure script. */
	public static final int SPLIT_VIEW_FIGURE_SCRIPT = 3;
	
	/** Identifies the movie export script. */
	public static final int MOVIE_EXPORT_SCRIPT = 4;
	
	/** Identifies the FLIM script. */
	public static final int FLIM_SCRIPT = 5;
	
	/** The collection of scripts that have a UI available. */
	private static final List<String>		SCRIPTS_UI_AVAILABLE;
	
	static {
		SCRIPTS_UI_AVAILABLE = new ArrayList<String>();
		SCRIPTS_UI_AVAILABLE.add(FigureParam.ROI_SCRIPT);
		SCRIPTS_UI_AVAILABLE.add(FigureParam.THUMBNAIL_SCRIPT);
		SCRIPTS_UI_AVAILABLE.add(FigureParam.MOVIE_SCRIPT);
		SCRIPTS_UI_AVAILABLE.add(FigureParam.SPLIT_VIEW_SCRIPT);
		SCRIPTS_UI_AVAILABLE.add(MovieExportParam.MOVIE_SCRIPT);
		SCRIPTS_UI_AVAILABLE.add(MovieExportParam.MOVIE_SCRIPT);
		SCRIPTS_UI_AVAILABLE.add(AnalysisParam.FLIM_SCRIPT);
	}
	
	/** The script to handle. */
	private ScriptObject script;
	
	/** Flag indicating if the script has a built-in UI. */
	private boolean		scriptWithUI;
	
	/**
	 * Returns <code>true</code> if the script has a UI, <code>false</code>
	 * otherwise.
	 * 
	 * @param path The path to check.
	 * @return See above.
	 */
	public static boolean isScriptWithUI(String path)
	{
		return SCRIPTS_UI_AVAILABLE.contains(path);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param script The script to set.
	 */
	public ScriptMenuItem(ScriptObject script)
	{
		if (script == null)
			throw new IllegalArgumentException("A script cannot be null.");
		this.script = script;
		String text = "";
		if (script.getScriptID() >= 0 && !script.isOfficialScript())
			text = "ID: "+script.getScriptID()+" ";
		text += script.getName();
		setText(script.getDisplayedName()+"...");
		setToolTipText(text);
		String path = script.getScriptLabel();
		scriptWithUI = SCRIPTS_UI_AVAILABLE.contains(path);
		if (scriptWithUI) {//reset the icon associated the script.
			IconManager icons = IconManager.getInstance();
			script.setIcon(icons.getIcon(IconManager.SCRIPT_WITH_UI));
			script.setIconLarge(icons.getIcon(IconManager.SCRIPT_WITH_UI_22));
		}
		setIcon(script.getIcon());
	}
	
	/**
	 * Returns the index or <code>-1</code> if the script does not 
	 * have a built-in UI.
	 * 
	 * @return See above.
	 */
	public int getIndex()
	{
		if (!scriptWithUI) return -1;
		if (!MetadataViewerAgent.isBinaryAvailable()) return -1;
		String path = script.getScriptLabel();
		if (FigureParam.ROI_SCRIPT.equals(path))
			return ROI_FIGURE_SCRIPT;
		else if (FigureParam.THUMBNAIL_SCRIPT.equals(path))
			return THUMBNAIL_FIGURE_SCRIPT;
		else if (FigureParam.MOVIE_SCRIPT.equals(path))
			return MOVIE_FIGURE_SCRIPT;
		else if (FigureParam.SPLIT_VIEW_SCRIPT.equals(path))
			return SPLIT_VIEW_FIGURE_SCRIPT;
		else if (MovieExportParam.MOVIE_SCRIPT.equals(path))
			return MOVIE_EXPORT_SCRIPT;
		else if (AnalysisParam.FLIM_SCRIPT.equals(path))
			return FLIM_SCRIPT;
		return -1;
	}
	
	/**
	 * Returns <code>true</code> if the script has already a UI.
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isScriptWithUI()
	{ 
		if (!MetadataViewerAgent.isBinaryAvailable()) return false;
		return scriptWithUI;
	}
	
	/**
	 * Returns the script.
	 * 
	 * @return See above.
	 */
	public ScriptObject getScript() { return script; }

}
