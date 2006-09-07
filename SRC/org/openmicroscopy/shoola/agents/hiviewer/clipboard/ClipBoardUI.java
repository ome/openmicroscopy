/*
 * org.openmicroscopy.shoola.agents.hiviewer.clipboard.ClipBoardUI
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
import java.awt.Point;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.hiviewer.clipboard.annotator.AnnotationPane;
import org.openmicroscopy.shoola.agents.hiviewer.clipboard.finder.FindPane;

/** 
 * The {@link ClipBoard}'s view.
 * This component hosts the different panels implementing the
 * {@link ClipBoardPane} interface.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * after code by
 *          Barry Anderson &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:banderson@computing.dundee.ac.uk">
 *              banderson@computing.dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class ClipBoardUI
    extends JScrollPane
{

    /** The position of the <code>Find</code> pane. */
    private static final int    FIND_TAB = 0;
    
    /** The position of the <code>Annotate</code>  pane. */
    private static final int    ANNOTATE_TAB = 1;
    
    /** The position of the <code>Editor</code>  pane. */
    private static final int    EDITOR_TAB = 2;
    
    /** The position of the <code>Info</code>  pane. */
    private static final int    INFO_TAB = 3;
    
    /** Reference to the Control. */
    private ClipBoardControl    controller;
    
    /** Reference to the model. */
    private ClipBoardModel      model;
    
    /** The tabbedPane hosting the display. */
    private JTabbedPane         tabPane;
    
    /** The popup menu. */
    private PopupMenu           popupMenu;
    
    /** Initializes the UI components. */
    private void initComponents()
    {
        tabPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.WRAP_TAB_LAYOUT);
        tabPane.setAlignmentX(LEFT_ALIGNMENT);
        popupMenu = new PopupMenu(model);
    }

    /** Builds and lays out the GUI. */
    private void buildUI()
    {
        ClipBoardPane pane = model.getClipboardPane(ClipBoard.FIND_PANE);
        tabPane.addTab(pane.getPaneName(), pane.getPaneIcon(),
                        pane, pane.getPaneDescription());
        pane = model.getClipboardPane(ClipBoard.ANNOTATION_PANE);
        tabPane.addTab(pane.getPaneName(), pane.getPaneIcon(),
                        pane, pane.getPaneDescription());
        pane = model.getClipboardPane(ClipBoard.EDITOR_PANE);
        tabPane.addTab(pane.getPaneName(), pane.getPaneIcon(),
                        pane, pane.getPaneDescription());
        pane = model.getClipboardPane(ClipBoard.INFO_PANE);
        tabPane.addTab(pane.getPaneName(), pane.getPaneIcon(),
                        pane, pane.getPaneDescription());
        tabPane.setSelectedIndex(model.getPaneIndex());
        setViewportView(tabPane);
    }
    
    /**
     * Links the UI .
     * 
     * @param model         The Model. Mustn't be <code>null</code>.
     * @param controller    The Control. Mustn't be <code>null</code>.
     */
    void initialize(ClipBoardModel model, ClipBoardControl controller)
    {
        if (controller == null) throw new NullPointerException("No control.");
        if (model == null) throw new NullPointerException("No model.");
        this.model = model;
        this.controller = controller;
        initComponents();
        buildUI();
    }
    
    /** Adds listeners. */
    void initListeners()
    {
        tabPane.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e)
            {
                switch (tabPane.getSelectedIndex()) {
                    case FIND_TAB:
                        controller.setSelectedPane(ClipBoard.FIND_PANE);
                        break;
                    case ANNOTATE_TAB:
                        controller.setSelectedPane(ClipBoard.ANNOTATION_PANE);
                        break;
                    case INFO_TAB:
                        controller.setSelectedPane(ClipBoard.INFO_PANE);
                        break;
                    case EDITOR_TAB:
                        controller.setSelectedPane(ClipBoard.EDITOR_PANE);
                        break;
                };
                
            }
        });
        //listener
        ClipBoardPane pane = model.getClipboardPane(ClipBoard.FIND_PANE);
        pane.addPropertyChangeListener(FindPane.SELECTED_PROPERTY, controller);
    }

    /** Displays the retrieved annotations. */
    void showAnnotations()
    { 
        AnnotationPane pane = (AnnotationPane) 
                    model.getClipboardPane(ClipBoard.ANNOTATION_PANE);
        pane.showAnnotations();
    }
    
    /**
     * Displays the results of a search action.
     * 
     * @param foundNodes The results.
     */
    void setSearchResults(Set foundNodes)
    {
        FindPane pane = (FindPane) model.getClipboardPane(ClipBoard.FIND_PANE);
        pane.setResults(foundNodes);
    }
    
    /**
     * Modifies the search view when a new node is selected in the
     * <code>Browser</code>.
     * 
     * @param selectedDisplay The selected node.
     */
    void onDisplayChange(ImageDisplay selectedDisplay)
    {
        Map m = model.getClipBoardPanes();
        Iterator i = m.keySet().iterator();
        while (i.hasNext()) 
            ((ClipBoardPane) m.get(i.next())).onDisplayChange(selectedDisplay);
    }
    
    /** 
     * Returns the height of the tabbed pane. 
     * 
     * @return See above.
     */
    int getTabPaneHeight() { return tabPane.getHeight(); }
    
    /**
     * Sets the selected tabbed pane.
     *  
     * @param index The index of the selected tabbed pane.
     */
    void setSelectedPane(int index) { tabPane.setSelectedIndex(index); }
    
    /**
     * Brings up the popup menu for the specified {@link ImageDisplay} node.
     * 
     * @param invoker   The component in whose space the popup menu is to
     *                  appear.
     * @param p         The coordinate in invoker's coordinate space at which 
     *                  the popup menu is to be displayed.
     * @param node      The {@link ImageDisplay} object.                 
     */
    void showMenu(JComponent invoker, Point p, ImageDisplay node)
    {
        popupMenu.showMenuFor(invoker, p.x, p.y, node);
    }
    
}
