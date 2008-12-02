 /*
 * org.openmicroscopy.shoola.util.ui.ScrollablePane 
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
package org.openmicroscopy.shoola.util.ui;

//Java imports

import java.awt.Dimension;
import java.awt.Rectangle;

import javax.swing.JPanel;
import javax.swing.Scrollable;

//Third-party libraries

//Application-internal dependencies

/** 
 * This is a JPanel that implements the {@link Scrollable} interface in 
 * such a way as to always resize horizontally within a scroll pane. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class ScrollablePanel 
	extends JPanel 
	implements Scrollable {

	/**
	 * Implemented as required by the {@link Scrollable} interface.
	 * Null implementation.
	 */
	public Dimension getPreferredScrollableViewportSize() {
		return null;
	}

	/**
	 * Implemented as required by the {@link Scrollable} interface.
	 * Returns 50;
	 */
	public int getScrollableBlockIncrement(Rectangle visibleRect,
			int orientation, int direction) {
		return 50;
	}

	/**
	 * Implemented as required by the {@link Scrollable} interface.
	 * Returns false'
	 */
	public boolean getScrollableTracksViewportHeight() {
		return false;
	}

	/**
	 * Implemented as specified by the {@link Scrollable} interface.
	 * Returns true, so that the panel stays the same size as 
	 * the view-port in which it is displayed. 
	 */
	public boolean getScrollableTracksViewportWidth() {
		return true;
	}

	/**
	 * Implemented as required by the {@link Scrollable} interface.
	 * Returns 1;
	 */
	public int getScrollableUnitIncrement(Rectangle visibleRect,
			int orientation, int direction) {
		return 1;
	}

}
