/*
 * org.openmicroscopy.shoola.agents.imviewer.view.SplitPanel 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.imviewer.util;



//Java imports
import javax.swing.JComponent;
import javax.swing.JPanel;


//Third-party libraries
import info.clearthought.layout.TableLayout; 

//Application-internal dependencies

/** 
 * Basic panel behaving like a splitpane.
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
public class SplitPanel
	extends JPanel
{

	/** Indicates the split is vertical i.e. left and right component. */
	public static final int VERTICAL = 0;
	
	/** Indicates the split is horizontal i.e. top and bottom component. */
	public static final int HORIZONTAL = 1;
	
	/** The size of the divider. */
	private static final int DIVIDER_SIZE = 0;
		
	/** 
	 * One of the constants defined by this class. Default is 
	 * {@link #VERTICAL}. 
	 */
	private int 	orientation;
	
	/** Initializes the components. */
	private void initialize()
	{
		switch (orientation) {
			case VERTICAL:
				double size[][] =
		        {{TableLayout.FILL, TableLayout.PREFERRED},  // Columns
		         {TableLayout.FILL}}; // Rows
		        setLayout(new TableLayout(size));
				break;
			case HORIZONTAL:
				double s[][] =
		        {{TableLayout.FILL},  // Columns
		         {TableLayout.FILL, TableLayout.PREFERRED}}; // Rows
		        setLayout(new TableLayout(s));
				break;
		}
	}
	
	/** Creates a new instance. */
	public SplitPanel()
	{
		this(VERTICAL);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param orientation The orientation of the split.
	 */
	public SplitPanel(int orientation)
	{
		switch (orientation) {
			case VERTICAL:
			case HORIZONTAL:
				break;
			default:
				orientation = VERTICAL;
				break;
		}
		this.orientation = orientation;
		initialize();
	}
	
	/** 
	 * Sets the top component.
	 * 
	 * @param c The component to add. Mustn't be <code>null</code>.
	 */
	public void setTopComponent(JComponent c)
	{
		if (c == null) 
			throw new IllegalArgumentException("No component to set.");
		add(c, "0, 0");
	}
	
	/** 
	 * Sets the bottom component.
	 * 
	 * @param c The component to add. Mustn't be <code>null</code>.
	 */
	public void setBottomComponent(JComponent c)
	{
		if (c == null) 
			throw new IllegalArgumentException("No component to set.");
		if (orientation == VERTICAL) setRightComponent(c);
		else add(c, "0, 1");
	}
	
	/** 
	 * Sets the left component.
	 * 
	 * @param c The component to add. Mustn't be <code>null</code>.
	 */
	public void setLeftComponent(JComponent c)
	{
		if (c == null) 
			throw new IllegalArgumentException("No component to set.");
		add(c, "0, 0");
	}
	
	/** 
	 * Sets the right component.
	 * 
	 * @param c The component to add. Mustn't be <code>null</code>.
	 */
	public void setRightComponent(JComponent c)
	{
		if (c == null) 
			throw new IllegalArgumentException("No component to set.");
		if (orientation == HORIZONTAL) setBottomComponent(c);
		else add(c, "1, 0");
	}
	
	/**
	 * Returns the size in pixels of the divider.
	 * 
	 * @return See above.
	 */
	public int getDividerSize() { return DIVIDER_SIZE; }
	
}
