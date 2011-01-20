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
import java.util.Collection;
import java.util.Iterator;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JList;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.filter.file.EditorFileFilter;
import org.openmicroscopy.shoola.util.ui.IconManager;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.FileAnnotationData;
import pojos.GroupData;
import pojos.TagAnnotationData;
import pojos.TermAnnotationData;

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
    
	/** The collection of immutable  nodes. */
	private Collection			immutable;
	
	/** Reference to the model. */
	private SelectionWizardUI 	model;
	
    /**
     * Sets the text displayed in the tool tip.
     * 
     * @param exp The experimenter to handle.
     */
    private void createTooltip(ExperimenterData exp)
    {
    	if (exp == null) return;
    	String s = "Created by: "+exp.getFirstName()+" "+exp.getLastName();
    	setToolTipText(s);
    }
    
    /**
     * Returns <code>true</code> if the passed element is immutable.
     * <code>false</code> otherwise.
     * 
     * @param value The element to handle.
     * @return See above.
     */
    private boolean isImmutable(Object value)
    {
    	if (immutable == null || immutable.size() == 0) return false;
    	if (!(value instanceof DataObject)) return false;
    	Iterator i = immutable.iterator();
    	long id = ((DataObject) value).getId();
    	if (id < 0) return false;
    	Object object;
    	while (i.hasNext()) {
			object = i.next();
			if (object.getClass().equals(value.getClass())) {
				if (((DataObject) object).getId() == id) {
					return !model.isAddedNode(value);
				}
			}
		}
    	return false;
    }
	
	/** 
	 * Creates a new instance.
	 * 
	 * @param currentUserID The id of the user currently logged in.
	 * @param model Reference to the UI wizard.  
	 */
	DataObjectListCellRenderer(long currentUserID, SelectionWizardUI model)
	{
		this.model = model;
		this.currentUserID = currentUserID;
		icons = IconManager.getInstance();
        filter = new EditorFileFilter();
	}
	
	/**
	 * Sets the collection of nodes that cannot be removed.
	 * 
	 * @param immutable The collection to set.
	 */
	void setImmutableElements(Collection immutable)
	{
		this.immutable = immutable;
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
			ExperimenterData exp;
			if (TagAnnotationData.INSIGHT_TAGSET_NS.equals(ns)) {
				if (currentUserID >= 0) {
					try {
						exp = tag.getOwner();
						long id = exp.getId();
						if (id == currentUserID) 
							setIcon(icons.getIcon(IconManager.TAG_SET));
						else  {
							createTooltip(exp);
							setIcon(icons.getIcon(
									IconManager.TAG_SET_OTHER_OWNER));
						}
					} catch (Exception e) {
						setIcon(icons.getIcon(IconManager.TAG_SET));
					}
				} else 
					setIcon(icons.getIcon(IconManager.TAG_SET));
			} else {
				if (currentUserID >= 0) {
					try {
						exp = tag.getOwner();
						long id = exp.getId();
						if (id == currentUserID) 
							setIcon(icons.getIcon(IconManager.TAG));
						else {
							createTooltip(exp);
							setIcon(icons.getIcon(IconManager.TAG_OTHER_OWNER));
						}
					} catch (Exception e) {
						setIcon(icons.getIcon(IconManager.TAG));
					}
				} else 
					setIcon(icons.getIcon(IconManager.TAG));
			}
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
		} else if (value instanceof TermAnnotationData) {
			TermAnnotationData term = (TermAnnotationData) value;
			setText(term.getTerm());
			setIcon(icons.getIcon(IconManager.ONTOLOGY));
		} else if (value instanceof DatasetData) {
			DatasetData d = (DatasetData) value;
			setText(d.getName());
			setIcon(icons.getIcon(IconManager.DATASET));
		} else if (value instanceof GroupData) {
			GroupData d = (GroupData) value;
			setText(d.getName());
			setIcon(icons.getIcon(IconManager.GROUP));
		}
		setEnabled(!isImmutable(value));
		return this;
	}
	
}
