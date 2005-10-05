/*
 * org.openmicroscopy.shoola.agents.hiviewer.clipboard.TreePopupMenu
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
import java.awt.event.KeyEvent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.IconManager;


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
class TreePopupMenu
    extends JPopupMenu
{
    
    static final String  VIEW = "view";
    
    static final String  BROWSE = "browse";
    
    /** The <code>View</code> menu item. */
    JMenuItem           view;
    
    /** The <code>Classify</code> menu item. */
    JMenuItem           classify;
    
    /** The <code>Declassify</code> menu item. */
    JMenuItem           declassify;
    
    /** The <code>properties</code> menu item. */
    JMenuItem           properties;
    
    /** The <code>Annotate</code> menu item. */
    JMenuItem           annotate;  
    

    /** Initializes the UI components. */
    private void initComponents()
    {
        IconManager im = IconManager.getInstance();
        properties = new JMenuItem("Properties", 
                                im.getIcon(IconManager.PROPERTIES));
        annotate = new JMenuItem("Annotate", im.getIcon(IconManager.ANNOTATE));
        classify = new JMenuItem("Add to category");
        declassify = new JMenuItem("Remove from category");
        view = new JMenuItem(VIEW, im.getIcon(IconManager.VIEWER));
    }
    
    /**
     * Helper method to create the Classify submenu.
     * 
     * @return  The Classify submenu.
     */
    private JMenu createClassifySubMenu()
    {
        IconManager im = IconManager.getInstance();
        JMenu menu = new JMenu("Classify");
        menu.setIcon(im.getIcon(IconManager.CLASSIFY));
        menu.setMnemonic(KeyEvent.VK_C);
        menu.add(classify);
        menu.add(declassify);
        return menu;
    }
    
    /** Builds and lays out the UI. */
    private void buildUI()
    {
        add(properties);
        //add(annotate);
        add(createClassifySubMenu());
        add(new JSeparator(SwingConstants.HORIZONTAL));
        add(view);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param clipBoard The {@link ClipBoardUI}. Mustn't be <code>null</code>.
     */
    TreePopupMenu(ClipBoardUI clipBoard)
    {
        if (clipBoard == null)
            throw new IllegalArgumentException("No clipBoard.");
        initComponents();
        buildUI();
        new TreePopupMenuMng(this, clipBoard);
    }

    void setViewText(String txt) { view.setText(txt); }
    
    void setClassifyEnabled(boolean b)
    {
        classify.setEnabled(b);
        declassify.setEnabled(b);
    }
    
}
