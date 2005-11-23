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
import java.util.Set;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplay;

/** 
 * The {@link ClipBoard}'s view.
 * This component hosts the different panels implementing the
 * {@link ClipBoardTab} interface.
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
class ClipBoardUI
    extends JScrollPane
{

    /** Position of the search tabbed pane. */
    private static final int    SEARCH_TAB = 0;
    
    /** Position of the annotation tabbed pane. */
    private static final int    ANNOTATE_TAB = 1;
    
    /** The title of the <code>Search</code> pane. */
    private static final String SEARCH = "Search";
    
    /** The title of the <code>Annotate</code> pane. */
    private static final String ANNOTATIONS = "Annotations";
    
    /** The <code>Search</code> pane. */
    private CBSearchTabView     searchView;
    
    /** The <code>Annotate</code> pane. */
    private CBAnnotationTabView annotationView;
    
    /** The popup menu. */
    private TreePopupMenu       popupMenu;
    
    /** Reference to the <code>ClipBoardControl</code> Control. */
    private ClipBoardControl    controller;
    
    /** Reference to the <code>ClipBoardModel</code> Model. */
    private ClipBoardModel      model;
    
    /** The tabbedPane hosting the display. */
    private JTabbedPane         tabPane;
    
    /** Adds a {@link ChangeListener} to the tabbed pane. */
    private void initListener()
    {
        tabPane.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e)
            {
                int index = -1;
                switch (tabPane.getSelectedIndex()) {
                    case SEARCH_TAB:
                        index = ClipBoard.SEARCH_PANEL;
                        break;
                    case ANNOTATE_TAB:
                        index = ClipBoard.ANNOTATION_PANEL;
                        break;
                };
                controller.setPaneIndex(index);
            }
        });
    }
    
    /** Initializes the UI components. */
    private void initComponents()
    {
        popupMenu = new TreePopupMenu(model);
        searchView = new CBSearchTabView(model, this, controller);
        annotationView = new CBAnnotationTabView(model, this, controller);
        tabPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.WRAP_TAB_LAYOUT);
        tabPane.setAlignmentX(LEFT_ALIGNMENT);
    }
    
    /** Builds and lays out the GUI. */
    private void buildUI()
    {
        tabPane.add(searchView, SEARCH_TAB);
        tabPane.setTitleAt(SEARCH_TAB, SEARCH);
        tabPane.add(annotationView, ANNOTATE_TAB);
        tabPane.setTitleAt(ANNOTATE_TAB, ANNOTATIONS); 
        setViewportView(tabPane);
    }
    
    /**
     * Links the MVC triad.
     * 
     * @param controller The {@link ClipBoardControl} control. Mustn't be 
     *                  <code>null</code>.
     * @param model The {@link ClipBoardModel} model. Mustn't be 
     *                  <code>null</code>.
     */
    void initialize(ClipBoardControl controller, ClipBoardModel model)
    {
        if (controller == null) throw new NullPointerException("No control.");
        if (model == null) throw new NullPointerException("No model.");
        this.model = model;
        this.controller = controller;
        initComponents();
        buildUI();
        //Initializes the listener otherwise an event is fired when 
        //components are added to the tabbed pane.
        initListener();
    }
    
    /** Returns the popup menu. */
    TreePopupMenu getPopupMenu() { return popupMenu; }

    
    /** Displays the retrieved annotations. */
    void showAnnotations() { annotationView.showAnnotations(); }
    
    /**
     * Displays the results of a search action.
     * 
     * @param foundNodes The results.
     */
    void setSearchResults(Set foundNodes)
    {
        searchView.setSearchResults(foundNodes);
    }
    
    /**
     * Modifies the search view when a new node is selected in the
     * <code>Browser</code>.
     * 
     * @param selectedDisplay The selected node.
     */
    void onDisplayChange(ImageDisplay selectedDisplay)
    {
        searchView.onDisplayChange(selectedDisplay);
        annotationView.onDisplayChange(selectedDisplay);
    }
    
    /** Updates the annotation tabbed pane. */
    void manageAnnotation() { annotationView.manageAnnotation(); }
    
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
    
}
