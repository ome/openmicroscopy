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
	
	/** Reference to the <code>Dataset</code> icon. */
	private static final Icon DATASET_ICON;
	
	/** Reference to the <code>File</code> icon. */
	private static final Icon FILE_ICON;
	
	/** Reference to the <code>Tag</code> icon. */
	private static final Icon TAG_ICON;
	
	/** Reference to the <code>Tag Set</code> icon. */
	private static final Icon TAG_SET_ICON;

	/** Reference to the <code>Owner</code> icon. */
	private static final Icon OWNER_ICON;
	
	/** Reference to the <code>Text File</code> icon. */
	private static final Icon FILE_TEXT_ICON;
	
	/** Reference to the <code>PDF File</code> icon. */
	private static final Icon FILE_PDF_ICON;
	
	/** Reference to the <code>HTML File</code> icon. */
	private static final Icon FILE_HTML_ICON;
	
	/** Reference to the <code>Power Point File</code> icon. */
	private static final Icon FILE_PPT_ICON;
	
	/** Reference to the <code>Word File</code> icon. */
	private static final Icon FILE_WORD_ICON;
	
	/** Reference to the <code>Excel File</code> icon. */
	private static final Icon FILE_EXCEL_ICON;
	
	/** Reference to the <code>XML File</code> icon. */
	private static final Icon FILE_XML_ICON;
	
	/** Reference to the <code>Editor File</code> icon. */
	private static final Icon FILE_EDITOR_ICON;
	
	/** Reference to the <code>Experiment</code> icon. */
	private static final Icon EDITOR_EXPERIMENT_ICON;
	
	/** Reference to the <code>Date</code> icon. */
	private static final Icon GROUP_ICON;
	
	/** Reference to the <code>Date</code> icon. */
	private static final Icon ONTOLOGY_ICON;
	
	/** Reference to the <code>Tag</code> icon. */
	private static final Icon TAG_OTHER_OWNER_ICON;
	
	/** Reference to the <code>Tag set</code> icon. */
	private static final Icon TAG_SET_OTHER_OWNER_ICON;
	
	static { 
		IconManager icons = IconManager.getInstance();
		DATASET_ICON = icons.getIcon(IconManager.DATASET);
		FILE_ICON = icons.getIcon(IconManager.FILE);
		TAG_ICON = icons.getIcon(IconManager.TAG);
		TAG_SET_ICON = icons.getIcon(IconManager.TAG_SET);
		OWNER_ICON = icons.getIcon(IconManager.OWNER);
		FILE_TEXT_ICON = icons.getIcon(IconManager.FILE_TEXT);
		FILE_PDF_ICON = icons.getIcon(IconManager.FILE_PDF);
		FILE_HTML_ICON = icons.getIcon(IconManager.FILE_HTML);
		FILE_PPT_ICON = icons.getIcon(IconManager.FILE_PPT);
		FILE_WORD_ICON = icons.getIcon(IconManager.FILE_WORD);
		FILE_EXCEL_ICON = icons.getIcon(IconManager.FILE_EXCEL);
		FILE_XML_ICON = icons.getIcon(IconManager.FILE_XML);
		FILE_EDITOR_ICON = icons.getIcon(IconManager.FILE_EDITOR);
		EDITOR_EXPERIMENT_ICON = icons.getIcon(IconManager.EDITOR_EXPERIMENT);
		GROUP_ICON = icons.getIcon(IconManager.GROUP);
		ONTOLOGY_ICON = icons.getIcon(IconManager.ONTOLOGY);
		TAG_OTHER_OWNER_ICON = icons.getIcon(IconManager.TAG_OTHER_OWNER);
		TAG_SET_OTHER_OWNER_ICON = 
			icons.getIcon(IconManager.TAG_SET_OTHER_OWNER);
	}
	
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
							setIcon(TAG_SET_ICON);
						else  {
							createTooltip(exp);
							setIcon(TAG_SET_OTHER_OWNER_ICON);
						}
					} catch (Exception e) {
						setIcon(TAG_SET_ICON);
					}
				} else 
					setIcon(TAG_SET_ICON);
			} else {
				if (currentUserID >= 0) {
					try {
						exp = tag.getOwner();
						long id = exp.getId();
						if (id == currentUserID) 
							setIcon(TAG_ICON);
						else {
							createTooltip(exp);
							setIcon(TAG_OTHER_OWNER_ICON);
						}
					} catch (Exception e) {
						setIcon(TAG_ICON);
					}
				} else 
					setIcon(TAG_ICON);
			}
			if (tag.getId() <= 0)
				setForeground(NEW_FOREGROUND_COLOR);
		} else if (value instanceof FileAnnotationData) {
			FileAnnotationData fad = (FileAnnotationData) value;
			setText(fad.getFileName());
			if (fad.getId() <= 0)
				setForeground(NEW_FOREGROUND_COLOR);
			String format = fad.getFileFormat();
			Icon icon = FILE_ICON;
        	if (FileAnnotationData.PDF.equals(format)) 
        		icon = FILE_PDF_ICON;
        	else if (FileAnnotationData.TEXT.equals(format) ||
        			FileAnnotationData.CSV.equals(format)) 
        		icon = FILE_TEXT_ICON;
        	else if (FileAnnotationData.HTML.equals(format) ||
        			FileAnnotationData.HTM.equals(format)) 
        		icon = FILE_HTML_ICON;
        	else if (FileAnnotationData.MS_POWER_POINT.equals(format) ||
        			FileAnnotationData.MS_POWER_POINT_SHOW.equals(format) ||
        			FileAnnotationData.MS_POWER_POINT_X.equals(format)) 
        		icon = FILE_PPT_ICON;
        	else if (FileAnnotationData.MS_WORD.equals(format) ||
        			FileAnnotationData.MS_WORD_X.equals(format)) 
        		icon = FILE_WORD_ICON;
        	else if (FileAnnotationData.MS_EXCEL.equals(format)) 
        		icon = FILE_EXCEL_ICON;
        	else if (FileAnnotationData.XML.equals(format) ||
        			FileAnnotationData.RTF.equals(format)) {
        		if (filter.accept(fad.getFileName())) {
        			if (FileAnnotationData.EDITOR_EXPERIMENT_NS.equals(
        					fad.getNameSpace()))
        				icon = EDITOR_EXPERIMENT_ICON;
        			else icon = FILE_EDITOR_ICON;
        		} else icon = FILE_XML_ICON;
        	} else icon = FILE_ICON;
			setIcon(icon);
		} else if (value instanceof TermAnnotationData) {
			TermAnnotationData term = (TermAnnotationData) value;
			setText(term.getTerm());
			setIcon(ONTOLOGY_ICON);
		} else if (value instanceof DatasetData) {
			DatasetData d = (DatasetData) value;
			setText(d.getName());
			setIcon(DATASET_ICON);
		} else if (value instanceof GroupData) {
			GroupData d = (GroupData) value;
			setText(d.getName());
			setIcon(GROUP_ICON);
		} else if (value instanceof ExperimenterData) {
			ExperimenterData exp = (ExperimenterData) value;
			setText(EditorUtil.getExperimenterName(exp));
			setIcon(OWNER_ICON);
		}
		setEnabled(!isImmutable(value));
		return this;
	}
	
}
