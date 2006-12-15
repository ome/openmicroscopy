/*
 * org.openmicroscopy.shoola.agents.hiviewer.clipboard.ToolBar
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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

package org.openmicroscopy.shoola.agents.hiviewer.clipboard;

//Java imports
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;


//Third-party libraries

//Application-internal dependencies

/** 
 * 
 *
 * @author  Barry Anderson &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:banderson@computing.dundee.ac.uk">
 *              banderson@comnputing.dundee.ac.uk
 *              </a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class ToolBar
    extends JPanel
{

    
    private  static final Dimension     TOOLBAR_SIZE = new Dimension(25, 25);
    
    private ClipBoardModel  model;
    
    private void buildGUI()
    {
        setSize(TOOLBAR_SIZE);
        setBorder(new LineBorder(Color.LIGHT_GRAY));
        setLayout(new FlowLayout(FlowLayout.LEFT));
        
        //view button
        //JButton button = new JButton(model.getAction(ClipBoard.VIEW));
        //add(button);
    }
    
    ToolBar(ClipBoardModel  model)
    {
        this.model = model;
        buildGUI();
    }
    
}
