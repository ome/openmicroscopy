/*
 * org.openmicroscopy.shoola.agents.roi.pane.PropagationPopupMenu
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

package org.openmicroscopy.shoola.agents.roi.pane;

//Java imports
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.border.BevelBorder;

import org.openmicroscopy.shoola.agents.datamng.DataManagerUIF;
import org.openmicroscopy.shoola.agents.roi.IconManager;
import org.openmicroscopy.shoola.env.config.Registry;

//Third-party libraries

//Application-internal dependencies

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class PropagationPopupMenu
    extends JPopupMenu
{
    
    /** Button to copy the end cell. */
    JMenuItem               erase;
    
    /** Button to copy the end cell. */
    JMenuItem               copyOne;
    
    /** Button to copy all cell from start to end. */
    JMenuItem               copyAll;
    
    /** Button to cancel op. */
    JMenuItem               cancel;
    
    /** Holds the configuration entries. */
    Registry                registry;
    
    /** This UI component's controller and model. */
    PropagationPopupMenuMng manager;  
    
    PropagationPopupMenu(AssistantDialogMng adm)
    {
        registry = adm.getControl().getRegistry();
        initCopyOne();
        initCopyAll();
        initCancel();
        initErase();
        manager = new PropagationPopupMenuMng(this, adm);
        buildGUI() ;
    }

    /*
    void setDragging(boolean b)
    {
        erase.setEnabled(false);
        copyOne.setEnabled(true);
        copyAll.setEnabled(b);
    }
    */
    
    void setOps(boolean b)
    {
        erase.setEnabled(b);
        copyOne.setEnabled(!b);
        copyAll.setEnabled(!b);
    }

    /** Creates and initializes the copy button. */
    private void initCopyOne()
    {
        IconManager im = IconManager.getInstance(registry);
        copyOne = new JMenuItem("Copy here", im.getIcon(IconManager.COPY));
        copyOne.setBorder(null);
        copyOne.setFont((Font) registry.lookup("/resources/fonts/Labels"));
        copyOne.setForeground(DataManagerUIF.STEELBLUE);  
    }
    
    /** Creates and initializes the copyAll button. */
    private void initCopyAll()
    {
        IconManager im = IconManager.getInstance(registry);
        copyAll = new JMenuItem("Fill to here", 
                                    im.getIcon(IconManager.COPY_SEGMENT));
        copyAll.setBorder(null);
        copyAll.setFont((Font) registry.lookup("/resources/fonts/Labels"));
        copyAll.setForeground(DataManagerUIF.STEELBLUE);  
    }
    
    /** Creates and initializes the cancel button. */
    private void initCancel()
    {
        IconManager im = IconManager.getInstance(registry);
        cancel = new JMenuItem("Cancel",  im.getIcon(IconManager.CLOSE));
        cancel.setBorder(null);
        cancel.setFont((Font) registry.lookup("/resources/fonts/Labels"));
        cancel.setForeground(DataManagerUIF.STEELBLUE);
        cancel.setEnabled(true);    
    }
    
    /** Creates and initializes the cancel button. */
    private void initErase()
    {
        IconManager im = IconManager.getInstance(registry);
        erase = new JMenuItem("Erase",  im.getIcon(IconManager.ERASE));
        erase.setBorder(null);
        erase.setFont((Font) registry.lookup("/resources/fonts/Labels"));
        erase.setForeground(DataManagerUIF.STEELBLUE);   
    }
    
    /** Builds and lays out the GUI. */
    private void buildGUI() 
    {
        setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        add(copyOne);
        add(copyAll);
        add(erase);
        add(cancel);
    }
    
}
