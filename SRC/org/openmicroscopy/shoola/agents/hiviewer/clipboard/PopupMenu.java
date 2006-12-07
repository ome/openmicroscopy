/*
 * org.openmicroscopy.shoola.agents.hiviewer.clipboard.PopupMenu
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
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.IconManager;
import org.openmicroscopy.shoola.agents.hiviewer.actions.AnnotateAction;
import org.openmicroscopy.shoola.agents.hiviewer.actions.ClassifyAction;
import org.openmicroscopy.shoola.agents.hiviewer.actions.DeclassifyAction;
import org.openmicroscopy.shoola.agents.hiviewer.actions.PropertiesAction;
import org.openmicroscopy.shoola.agents.hiviewer.actions.ViewAction;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplay;
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
 * Pop-up menu for nodes in the clipboard display.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * after code by
 *          Barry Anderson &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:banderson@computing.dundee.ac.uk">
 *              banderson@computing.dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
class PopupMenu
    extends JPopupMenu
{
    
    /** The <code>View</code> menu item. */
    private JMenuItem               view;
    
    /** The <code>Classify</code> menu item. */
    private JMenuItem               classify;
    
    /** The <code>Declassify</code> menu item. */
    private JMenuItem               declassify;
    
    /** The <code>properties</code> menu item. */
    private JMenuItem               properties;
    
    /** The <code>Annotate</code> menu item. */
    private JMenuItem               annotate;  
    
    /** Reference to the Model. */
    private ClipBoardModel          model;
    
    /** The {@link ImageDisplay} node this popup menu is for. */
    private ImageDisplay            selectedNode;
    
    /** Initializes the UI components. */
    private void initComponents()
    {
        IconManager im = IconManager.getInstance();
        properties = new JMenuItem(PropertiesAction.NAME, 
                im.getIcon(IconManager.PROPERTIES));
        properties.setToolTipText(PropertiesAction.DESCRIPTION);
        properties.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                DataObject object = getDataObject();
                if (object != null) {
                    PropertiesCmd cmd = 
                        new PropertiesCmd(model.getParentModel(), object);
                    cmd.execute();
                }
            }
        });
        annotate = new JMenuItem(AnnotateAction.NAME, 
        						im.getIcon(IconManager.ANNOTATE));
        annotate.setToolTipText(AnnotateAction.DESCRIPTION);
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
        classify = new JMenuItem(ClassifyAction.NAME, 
        						im.getIcon(IconManager.CATEGORY));
        classify.setToolTipText(ClassifyAction.DESCRIPTION);
        classify.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                DataObject object = getDataObject();
                if (object instanceof ImageData) {
                    ClassifyCmd cmd = new ClassifyCmd(model.getParentModel(), 
                    						(ImageData) object, 
                    						ClassifyCmd.CLASSIFICATION_MODE);
                    cmd.execute();
                }
            }
        });
        declassify = new JMenuItem(DeclassifyAction.NAME, 
				im.getIcon(IconManager.DECATEGORISE));
        declassify.setToolTipText(DeclassifyAction.DESCRIPTION);
        declassify.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                DataObject object = getDataObject();
                if (object instanceof ImageData) {
                    ClassifyCmd cmd = new ClassifyCmd(model.getParentModel(), 
    												(ImageData) object, 
    										ClassifyCmd.DECLASSIFICATION_MODE);
                    cmd.execute();
                }
            }
        });
        view = new JMenuItem(ViewAction.NAME, im.getIcon(IconManager.VIEWER));
        view.setToolTipText(ViewAction.DESCRIPTION_VIEW);
        view.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                DataObject object = getDataObject();
                if (object != null) {
                    ViewCmd cmd = new ViewCmd(model.getParentModel(), object);
                    cmd.execute();
                }
            }
        });
    }
    
    /**
     * Returns the selected data object.
     * 
     * @return See above.
     */
    private DataObject getDataObject()
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
    
    /** Builds and lays out the GUI. */
    private void buildGUI()
    {
        add(view);
        add(new JSeparator(JSeparator.HORIZONTAL));
        add(classify);
        add(declassify);
        add(annotate);
        add(new JSeparator(JSeparator.HORIZONTAL));
        add(properties);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     */
    PopupMenu(ClipBoardModel model)
    {
        if (model == null) throw new IllegalArgumentException("No model.");
        this.model = model;
        initComponents();
        buildGUI();
    }
    
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
        String txt = ViewAction.BROWSE;
        String toolTip = ViewAction.DESCRIPTION_BROWSE;
        boolean b = false;
        if (object instanceof ImageData) {
            txt = ViewAction.VIEW;
            toolTip = ViewAction.DESCRIPTION_VIEW;
            b = true;
        }
        view.setText(txt);
        view.setToolTipText(toolTip);
        classify.setEnabled(b);
        declassify.setEnabled(b);
        show(invoker, x, y);
    }
    
}
