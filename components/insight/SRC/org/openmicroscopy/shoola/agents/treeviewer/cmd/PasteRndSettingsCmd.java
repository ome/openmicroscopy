/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.treeviewer.cmd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageTimeSet;
import org.openmicroscopy.shoola.env.data.model.TimeRefObject;
import omero.gateway.model.DataObject;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.ImageData;
import omero.gateway.model.WellSampleData;

/** 
 * Pastes the rendering settings across the collection of images.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
public class PasteRndSettingsCmd 
	implements ActionCmd
{

	/** Indicates to paste the rendering settings. */
	public static final int PASTE = 0;
	
	/** Indicates to reset the rendering settings. */
	public static final int RESET = 1;
	
	/** Indicates to set the min/max for each channel. */
	public static final int SET_MIN_MAX = 2;
	
	/** Indicates to reset the rendering settings, used the owner's one. */
	public static final int SET_OWNER = 3;
	
	/** Reference to the model. */
    private TreeViewer				model;
    
    /** One of the constants defined by this class. */
    private int						index;
    
    /** The collection of selected items if specified. */
    private Collection<DataObject> 	selection;
    
    /**
     * Controls if the passed index is supported.
     * 
     * @param i The value to check.
     */
    private void checkIndex(int i)
    {
    	switch (i) {
			case PASTE:
			case RESET:
			case SET_MIN_MAX:
			case SET_OWNER:
				break;
			default:
				throw new IllegalArgumentException("Index not supported.");
		}
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the model. Mustn't be <code>null</code>.
     * @param index One of the constants defined by this class.
     */
    public PasteRndSettingsCmd(TreeViewer model, int index)
    {
        if (model == null) throw new IllegalArgumentException("No model.");
        checkIndex(index);
        this.index = index;
        this.model = model;
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model 	Reference to the model. Mustn't be <code>null</code>.
     * @param index 	One of the constants defined by this class.
     * @param selection The collection of data objects to handle.
     */
    public PasteRndSettingsCmd(TreeViewer model, int index, 
    		Collection<DataObject> selection)
    {
        if (model == null) throw new IllegalArgumentException("No model.");
        checkIndex(index);
        this.index = index;
        this.model = model;
        this.selection = selection;
    }
    
    /** Implemented as specified by {@link ActionCmd}. */
    public void execute()
    {
    	List<Long> ids = new ArrayList<Long>();
    	if (selection != null) {
    		Iterator<DataObject> o = selection.iterator();
    		DataObject ho;
    		Class klass = null;
    		while (o.hasNext()) {
				ho = o.next();
				if (ho instanceof WellSampleData) {
					klass = ImageData.class;
					ids.add(((WellSampleData) ho).getImage().getId());
				} else {
					klass = ho.getClass();
					ids.add(ho.getId());
				}
			}
    		switch (index) {
				case PASTE:
					if (model.hasRndSettings()) 
						model.pasteRndSettings(ids, klass);
					break;
				case RESET:
					model.resetRndSettings(ids, klass);
					break;
				case SET_MIN_MAX:
					model.setMinMax(ids, klass);
					break;
				case SET_OWNER:
					model.setOwnerRndSettings(ids, klass);
			}
    		return;
    	}
    	Browser b = model.getSelectedBrowser();
		if (b == null) return;
		TreeImageDisplay[] nodes = b.getSelectedDisplays();
		if (nodes.length == 0) return; 
		TreeImageDisplay node;
		TreeImageTimeSet time;
		
		Class klass = null;
		Object ho;
		Iterator j;
		ExperimenterData exp;
		TimeRefObject ref = null;
		for (int i = 0; i < nodes.length; i++) {
			node = nodes[i];
			if (node instanceof TreeImageTimeSet) {
				if (node.containsImages()) {
					klass = ImageData.class;
					j = ViewCmd.getImageNodeIDs(node, b).iterator();
					while (j.hasNext())
						ids.add((Long) j.next());
				} else {
					time = (TreeImageTimeSet) node;
            		exp = model.getUserDetails();
            		ref = new TimeRefObject(exp.getId(), TimeRefObject.TIME);
        			ref.setTimeInterval(time.getStartTime(), time.getEndTime());
				}
			} else {
				ho = node.getUserObject();
				klass = ho.getClass();
				if (ho instanceof DataObject) {
					if (ho instanceof WellSampleData) {
						klass = ImageData.class;
						ids.add(((WellSampleData) ho).getImage().getId());
					} else
						ids.add(((DataObject) ho).getId());
				}
			}
		}
		switch (index) {
			case PASTE:
				if (model.hasRndSettings()) {
					if (ref != null) model.pasteRndSettings(ref);
					else model.pasteRndSettings(ids, klass);
				}
				break;
			case RESET:
				if (ref != null) model.resetRndSettings(ref);
				else model.resetRndSettings(ids, klass);
				break;
			case SET_MIN_MAX:
				if (ref != null) model.setOriginalRndSettings(ref);
				else model.setMinMax(ids, klass);
				break;
			case SET_OWNER:
				if (ref != null) model.setOwnerRndSettings(ref);
				else model.setOwnerRndSettings(ids, klass);
		}
    }
    
}
