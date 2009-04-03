/*
 * org.openmicroscopy.shoola.agents.imviewer.view.HistoryCanvas 
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
package org.openmicroscopy.shoola.agents.imviewer.view;


//Java imports
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JScrollPane;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.IconManager;
import org.openmicroscopy.shoola.agents.imviewer.util.HistoryItem;
import org.openmicroscopy.shoola.util.ui.tpane.TinyPane;

/** 
 * 
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
class HistoryCanvas
	extends TinyPane
{

	/** The Description of the {@link #clearButton}. */
	private static final String CLEAR_DESCRIPTION = "Clear the history.";
	
	/** Reference to the Model. */
	private ImViewerModel model;
	
	/** Adds button to the tool bar. */
	private void addDecoration()
	{
		IconManager icons = IconManager.getInstance();
		JButton clearButton = new JButton(icons.getIcon(
								IconManager.HISTORY_CLEAR_12));
		clearButton.setToolTipText(CLEAR_DESCRIPTION);
		clearButton.setContentAreaFilled(false);
		clearButton.setBorder(BorderFactory.createEmptyBorder()); 
		clearButton.setMargin(new Insets(0, 0, 0, 0));
		clearButton.setOpaque(false);  
		clearButton. setFocusPainted(false);  
		List<JButton> nodes = new ArrayList<JButton>();
		nodes.add(clearButton);
		setDecoration(nodes);
	}
	
	/**
     * Creates a new instance.
     *
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     */
	HistoryCanvas(ImViewerModel model)
	{
		//noDecoration();
		clearDefaultButtons();
		addDecoration();
		//setTitleBarType(SMALL_TITLE_BAR);
		//setTitleBarType(SMALL_BAR);
		setListenToBorder(false);
		this.model = model;
	}
	
	/** 
	 * Lays out the nodes in a grid view. 
	 * 
	 * @param width The width available
	 */
	void doGridLayout(int width)
	{
		List nodes = model.getHistory();
		if (nodes == null || nodes.size() == 0) return;
		
		HistoryItem child = null;
        Dimension maxDim = ((HistoryItem) nodes.get(0)).getPreferredSize();
        int m = width/maxDim.width;
        int n = nodes.size();
        if (m != 0) n = n/m+1;
        /*
        for (int i = 0; i < n; ++i) {
    		for (int j = 0; j < m; ++j) {
                if (!node.hasNext()) //Done, less than n^2 children.
                    return;  //Go to finally.
                child = (HistoryItem) node.next();
                child.setBounds(j*maxDim.width, i*maxDim.height, 
                				maxDim.width, maxDim.height);
                child.validate();
            }
		} 
		*/
        Iterator i = nodes.iterator();
        int j = 0;
        while (i.hasNext()) {
			child = (HistoryItem) i.next();
			child.setBounds(j*maxDim.width, 0, maxDim.width, maxDim.height);
			child.validate();
			j++;
		}
        int finalWidth = nodes.size()*maxDim.width;
        if (finalWidth > width) {
        	//Rectangle bounds = child.getBounds();
        	Rectangle pBounds = getBounds();
        	JScrollPane dskDecorator = getDeskDecorator();
        	Dimension d = new Dimension(finalWidth, pBounds.height);
        	getInternalDesktop().setSize(d);
        	getInternalDesktop().setPreferredSize(d);
        	getInternalDesktop().validate();
        	//if (!viewRect.contains(bounds)) {
        	dskDecorator.getHorizontalScrollBar().setValue(finalWidth);
        }
	}
	
}
