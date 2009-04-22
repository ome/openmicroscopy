/*
 * ome.ij.dm.actions.SortAction 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
package ome.ij.dm.actions;


//Java imports
import java.awt.event.ActionEvent;
import javax.swing.Action;

//Third-party libraries

//Application-internal dependencies
import ome.ij.dm.browser.Browser;
import org.openmicroscopy.shoola.util.ui.IconManager;
import org.openmicroscopy.shoola.util.ui.UIUtilities;



/** 
 * Action to sort the nodes alphabeticall or by date depending on the index.
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
public class SortAction 
	extends BrowserAction
{

	/** Indicates to sort by date. */
	public static final int SORT_BY_DATE = Browser.SORT_NODES_BY_DATE;
	
	/** Indicates to sort by name. */
	public static final int SORT_BY_NAME = Browser.SORT_NODES_BY_NAME;
	
	/** Description of the action if the index is {@link #SORT_BY_NAME}. */
    private static final String DESCRIPTION_BY_NAME = "Sort by name.";
    
    /** Description of the action if the index is {@link #SORT_BY_DATE}. */
    private static final String DESCRIPTION_BY_DATE = "Sort by date.";
    
	/** One of the constants defined by this class. */
	private int index;
	
	/**
	 * Controls if the passed index is supported.
	 * 
	 * @param value The value to set.
	 */
	private void setIndex(int value)
	{
    	IconManager im = IconManager.getInstance();
		switch (value) {
			case SORT_BY_DATE:
				putValue(Action.SHORT_DESCRIPTION, 
		    			UIUtilities.formatToolTipText(DESCRIPTION_BY_DATE));
				putValue(Action.SMALL_ICON, 
						im.getIcon(IconManager.SORT_BY_DATE));
				return;
			case SORT_BY_NAME:
				putValue(Action.SHORT_DESCRIPTION, 
		    			UIUtilities.formatToolTipText(DESCRIPTION_BY_NAME));
				putValue(Action.SMALL_ICON, 
						im.getIcon(IconManager.SORT_ALPHABETICALLY));
				return;
			default:
				throw new IllegalArgumentException("Index not supported.");
		}
	}
	
    /** 
     * Reacts to {@link Browser}'s state change.
     *  @see BrowserAction#onStateChange()
     */
    protected void onStateChange()
    {
    	setEnabled(model.getState() == Browser.READY);
    }
    
	/**
	 * Creates a new instance.
	 * 
	 * @param model Reference to the Model. Mustn't be <code>null</code>.
	 * @param index One of the constants defined by this class.
	 */
	public SortAction(Browser model, int index)
	{
		super(model);
		setIndex(index);
		this.index = index;
	}
	
	/**
     * Sorts the nodes of the currently selected <code>Browser</code> by name.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e) { model.sortTreeNodes(index); }
	
}
