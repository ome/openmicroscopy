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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.IconManager;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.hiviewer.clsf.Classifier;
import org.openmicroscopy.shoola.agents.hiviewer.cmd.AnnotateCmd;
import org.openmicroscopy.shoola.agents.hiviewer.cmd.ClassifyCmd;
import org.openmicroscopy.shoola.agents.hiviewer.cmd.PropertiesCmd;
import org.openmicroscopy.shoola.agents.hiviewer.cmd.ViewCmd;

import pojos.CategoryData;
import pojos.CategoryGroupData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ImageData;
import pojos.ProjectData;


/** 
 * The popup menu displayed when a node is selected in the results tree.
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
    
    /** 
     * The text of the <code>view</code> menu item when an ImageNode is
     * selected.
     */
    static final String  VIEW = "view";
    
    /** 
     * The text of the <code>view</code> menu item when an ImageSet is
     * selected.
     */
    static final String  BROWSE = "browse";
    
    /** The {@link ImageDisplay} node this popup menu is for. */
    private ImageDisplay    selectedNode;
    
    /** The {@link ClipBoardModel} model. */
    private ClipBoardModel  model;
    
    /** The <code>View</code> menu item. */
    JMenuItem               view;
    
    /** The <code>Classify</code> menu item. */
    JMenuItem               classify;
    
    /** The <code>Declassify</code> menu item. */
    JMenuItem               declassify;
    
    /** The <code>properties</code> menu item. */
    JMenuItem               properties;
    
    /** The <code>Annotate</code> menu item. */
    JMenuItem               annotate;  
     
    /** Initializes the UI components. */
    private void initComponents()
    {
        IconManager im = IconManager.getInstance();
        properties = new JMenuItem("Properties", 
                                im.getIcon(IconManager.PROPERTIES));
        properties.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                DataObject object = getDataObject();
                if (object != null) new PropertiesCmd(object).execute();

            }
        });
        annotate = new JMenuItem("Annotate", im.getIcon(IconManager.ANNOTATE));
        annotate.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                DataObject object = getDataObject();
                if (object != null) {
                    AnnotateCmd cmd = 
                        new AnnotateCmd(model.getParentModel(),
                                        selectedNode);
                    cmd.execute();
                }

            }
        });
        classify = new JMenuItem("Add to category");
        classify.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                DataObject object = getDataObject();
                if (object instanceof ImageData) {
                    ClassifyCmd cmd = new ClassifyCmd((ImageData) object, 
                                            Classifier.CLASSIFICATION_MODE, 
                                            model.getParentModel().getUI());
                    cmd.execute();
                }
            }
        });
        declassify = new JMenuItem("Remove from category");
        declassify.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                DataObject object = getDataObject();
                if (object instanceof ImageData) {
                    ClassifyCmd cmd = new ClassifyCmd((ImageData) object, 
                                            Classifier.DECLASSIFICATION_MODE, 
                                            model.getParentModel().getUI());
                    cmd.execute();
                }
            }
        });
        view = new JMenuItem(VIEW, im.getIcon(IconManager.VIEWER));
        view.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                DataObject object = getDataObject();
                if (object != null) new ViewCmd(object).execute();
            }
        });
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
        add(annotate);
        add(createClassifySubMenu());
        add(new JSeparator(SwingConstants.HORIZONTAL));
        add(view);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model The {@link ClipBoardModel}. Mustn't be <code>null</code>.
     */
    TreePopupMenu(ClipBoardModel model)
    {
        if (model == null)
            throw new IllegalArgumentException("No model.");
        this.model = model;
        initComponents();
        buildUI();
    }
    
    
    /**
     * Returns the selected data object.
     * 
     * @return See above.
     */
    DataObject getDataObject()
    {
        DataObject target = null;
        if (selectedNode != null) {
            Object hierarchyObj = selectedNode.getHierarchyObject();
            if (hierarchyObj == null) return null;
            if (hierarchyObj instanceof ProjectData || 
                hierarchyObj instanceof DatasetData ||
                hierarchyObj instanceof ImageData ||
                hierarchyObj instanceof CategoryData ||
                hierarchyObj instanceof CategoryGroupData)
                target = (DataObject) hierarchyObj;
        }
        return target;
    }
    
    /**
     * Returns the selected {@link ImageDisplay} node.
     * 
     * @return See above.
     */
    ImageDisplay getSelectedNode() { return selectedNode; }
    
    /**
     * Brings up the popup menu for the specified {@link ImageDisplay} node.
     * 
     * @param invoker   The component in whose space the popup menu is to appear
     * @param x         The x coordinate in invoker's coordinate space at which 
     *                  the popup menu is to be displayed.
     * @param y         The y coordinate in invoker's coordinate space at which 
     *                  the popup menu is to be displayed
     * @param node      The selected node.
     */
    void showMenuFor(JComponent invoker, int x, int y, ImageDisplay node)
    {
        if (node == null) return;
        selectedNode = node;
        DataObject object = getDataObject();
        if (object == null) return;
        String txt = TreePopupMenu.BROWSE;
        boolean b = false;
        if (object instanceof ImageData) {
            txt = TreePopupMenu.VIEW;
            b = true;
        }
        view.setText(txt);
        classify.setEnabled(b);
        declassify.setEnabled(b);
        show(invoker, x, y);
    }

}
