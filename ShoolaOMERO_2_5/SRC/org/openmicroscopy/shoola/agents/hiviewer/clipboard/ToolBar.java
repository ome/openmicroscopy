/*
 * org.openmicroscopy.shoola.agents.hiviewer.clipboard.ToolBar
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.agents.hiviewer.clipboard;

//Java imports
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.JButton;
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
