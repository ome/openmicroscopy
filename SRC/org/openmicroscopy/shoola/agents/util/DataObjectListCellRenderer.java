/*
 * org.openmicroscopy.shoola.agents.util.DataObjectListCellRenderer 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
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
package org.openmicroscopy.shoola.agents.util;


//Java imports
import java.awt.Color;
import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JList;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.filter.file.EditorFileFilter;
import org.openmicroscopy.shoola.util.ui.IconManager;
import pojos.DatasetData;
import pojos.FileAnnotationData;
import pojos.TagAnnotationData;
import pojos.URLAnnotationData;

/** 
 * Renderer used to display various kind of <code>DataObject</code>s in 
 * a table.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class DataObjectListCellRenderer 
	extends DefaultListCellRenderer
{

	/** The foreground color when the object is a new object. */
	public static final Color	NEW_FOREGROUND_COLOR = Color.red;
	
	/** Helper reference to the icon manager. */
	private IconManager 		icons;
	
	/** The id of the user currently logged in. */
	private long				currentUserID;
	
    /** Filter to identify protocol file. */
    private EditorFileFilter 	filter;
    
	/** Creates a new instance. */
	public DataObjectListCellRenderer()
	{
		this(-1);
	}
	
	/** 
	 * Creates a new instance.
	 * 
	 *  @param currentUserID The id of the user currently logged in.
	 */
	public DataObjectListCellRenderer(long currentUserID)
	{
		this.currentUserID = currentUserID;
		icons = IconManager.getInstance();
        filter = new EditorFileFilter();
	}
	
	/**
	 * Overridden to set the text and icon corresponding to the selected object.
	 * @see DefaultListCellRenderer#getListCellRendererComponent(JList, Object,
	 * 								int, boolean, boolean)
	 */
	public Component getListCellRendererComponent (JList list, Object value, 
			int index, boolean isSelected, boolean cellHasFocus)
	{
		super.getListCellRendererComponent(list, value, index, isSelected, 
										cellHasFocus);
		if (value instanceof TagAnnotationData) {
			TagAnnotationData tag = (TagAnnotationData) value;
			setText(tag.getTagValue());
			String ns = tag.getNameSpace();
			if (TagAnnotationData.INSIGHT_TAGSET_NS.equals(ns))
				setIcon(icons.getIcon(IconManager.TAG_SET));
			else setIcon(icons.getIcon(IconManager.TAG));
			if (tag.getId() <= 0)
				setForeground(NEW_FOREGROUND_COLOR);
		} else if (value instanceof FileAnnotationData) {
			FileAnnotationData fad = (FileAnnotationData) value;
			setText(fad.getFileName());
			if (fad.getId() <= 0)
				setForeground(NEW_FOREGROUND_COLOR);
			String format = fad.getFileFormat();
			Icon icon = icons.getIcon(IconManager.FILE);
        	if (FileAnnotationData.PDF.equals(format)) 
        		icon = icons.getIcon(IconManager.FILE_PDF);
        	else if (FileAnnotationData.TEXT.equals(format) ||
        			FileAnnotationData.CSV.equals(format)) 
        		icon = icons.getIcon(IconManager.FILE_TEXT);
        	else if (FileAnnotationData.HTML.equals(format) ||
        			FileAnnotationData.HTM.equals(format)) 
        		icon = icons.getIcon(IconManager.FILE_HTML);
        	else if (FileAnnotationData.MS_POWER_POINT.equals(format) ||
        			FileAnnotationData.MS_POWER_POINT_SHOW.equals(format) ||
        			FileAnnotationData.MS_POWER_POINT_X.equals(format)) 
        		icon = icons.getIcon(IconManager.FILE_PPT);
        	else if (FileAnnotationData.MS_WORD.equals(format) ||
        			FileAnnotationData.MS_WORD_X.equals(format)) 
        		icon = icons.getIcon(IconManager.FILE_WORD);
        	else if (FileAnnotationData.MS_EXCEL.equals(format)) 
        		icon = icons.getIcon(IconManager.FILE_EXCEL);
        	else if (FileAnnotationData.XML.equals(format) ||
        			FileAnnotationData.RTF.equals(format)) {
        		if (filter.accept(fad.getFileName())) {
        			if (FileAnnotationData.EDITOR_EXPERIMENT_NS.equals(
        					fad.getNameSpace()))
        				icon = icons.getIcon(IconManager.EDITOR_EXPERIMENT);
        			else icon = icons.getIcon(IconManager.FILE_EDITOR);
        		} else icon = icons.getIcon(IconManager.FILE_XML);
        	} else icon = icons.getIcon(IconManager.FILE);
			setIcon(icon);
		} else if (value instanceof URLAnnotationData) {
			URLAnnotationData url = (URLAnnotationData) value;
			setText(url.getURL());
			setIcon(icons.getIcon(IconManager.BROWSER));
		} else if (value instanceof DatasetData) {
			DatasetData d = (DatasetData) value;
			setText(d.getName());
			setIcon(icons.getIcon(IconManager.DATASET));
		}
		return this;
	}
	
}
