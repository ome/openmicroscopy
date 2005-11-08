/*
 * org.openmicroscopy.shoola.agents.hiviewer.controls.PopupMenu
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

package org.openmicroscopy.shoola.agents.hiviewer.view;


//Java imports
import java.awt.event.KeyEvent;
import javax.swing.BorderFactory;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.IconManager;

/** 
 * Pop-up menu for nodes in the browser display.
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
class PopupMenu
    extends JPopupMenu
{
    
    /** 
     * Button to bring up the property sheet of a hierarchy object &#151; 
     * project, dataset, category group, category, or image.
     */
    private JMenuItem   properties;
    
    /** Button to bring up the widget to annotate a dataset or an image. */
    private JMenuItem   annotate;
    
    /** Button to bring up the widget to classify an image. */
    private JMenuItem   classify;
    
    /** Button to bring up the widget to de classify an image. */
    private JMenuItem   declassify;
    
    /** Button to browse a container or bring up the Viewer for an image. */
    private JMenuItem   view;
    
    /** Button to zoom in all leaf nodes in a container. */
    private JMenuItem   zoomIn;
    
    /** Button to zoom out all leaf nodes in a container. */
    private JMenuItem   zoomOut;
    
    /** Button to resize all leaf nodes in a container. */
    private JMenuItem   zoomFit;
    
    
    /**
     * Creates the menu items with the given actions.
     * 
     * @param controller The Controller.
     */
    private void createMenuItems(HiViewerControl controller)
    {
        properties = new JMenuItem(
                controller.getAction(HiViewer.PROPERTIES));
        annotate = new JMenuItem(
                controller.getAction(HiViewer.ANNOTATE));
        classify = new JMenuItem(
                controller.getAction(HiViewer.CLASSIFY));
        declassify = new JMenuItem(
                controller.getAction(HiViewer.DECLASSIFY));
        view = new JMenuItem(controller.getAction(HiViewer.VIEW));
        zoomIn = new JMenuItem(controller.getAction(HiViewer.ZOOM_IN));
        zoomOut = new JMenuItem(controller.getAction(HiViewer.ZOOM_OUT));
        zoomFit = new JMenuItem(controller.getAction(HiViewer.ZOOM_FIT));
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
        menu.setMnemonic(KeyEvent.VK_C);
        menu.setIcon(im.getIcon(IconManager.CLASSIFY));
        menu.add(classify);
        menu.add(declassify);
        return menu;
    }
    
    /** Builds and lays out the GUI. */
    private void buildGUI() 
    {
        setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        add(properties);
        add(annotate);
        add(createClassifySubMenu());
        add(new JSeparator(SwingConstants.HORIZONTAL));
        add(view);
        add(zoomIn);
        add(zoomOut);
        add(zoomFit);
    }
    
    /** 
     * Creates a new instance.
     *
     * @param controller The Controller. Mustn't be <code>null</code>.
     */
    PopupMenu(HiViewerControl controller) 
    {
        if (controller == null) 
            throw new IllegalArgumentException("No control.");
        createMenuItems(controller);
        buildGUI() ;
    }
    
}
