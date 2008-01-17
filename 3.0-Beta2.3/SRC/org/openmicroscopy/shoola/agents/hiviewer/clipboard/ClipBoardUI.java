/*
 * org.openmicroscopy.shoola.agents.hiviewer.clipboard.ClipBoardUI
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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.hiviewer.clipboard.clsf.ClassificationPane;
import org.openmicroscopy.shoola.agents.hiviewer.clipboard.finder.FindPane;
import org.openmicroscopy.shoola.agents.hiviewer.clipboard.info.InfoPane;
import pojos.ImageData;

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
    extends JTabbedPane
{

    /** Reference to the Control. */
    private ClipBoardControl    controller;
    
    /** Reference to the model. */
    private ClipBoardModel      model;
    
    /** The popup menu. */
    private PopupMenu           popupMenu;
    
    /** Initializes the UI components. */
    private void initComponents()
    {
        popupMenu = new PopupMenu(model);
    }

    /** Builds and lays out the GUI. */
    private void buildUI()
    {
    	int h = 0;
    	Dimension dim = model.getComponentPreferredSize();
    	ClipBoardPane pane = model.getClipboardPane(ClipBoard.ANNOTATION_PANE);
    	Icon icon = pane.getPaneIcon();
    	if (icon != null && icon.getIconHeight() > h) h = icon.getIconHeight();
    	Component c = pane;
    	if (ClipBoard.HORIZONTAL_SPLIT) c = pane.getComponent(0);
        insertTab(pane.getPaneName(), icon, c, 
				pane.getPaneDescription(), ClipBoard.ANNOTATION_PANE);
        
        pane = model.getClipboardPane(ClipBoard.CLASSIFICATION_PANE);
        icon = pane.getPaneIcon();
        if (icon != null && icon.getIconHeight() > h) h = icon.getIconHeight();
        insertTab(pane.getPaneName(), icon, pane, 
        			pane.getPaneDescription(), ClipBoard.CLASSIFICATION_PANE);
        
        pane = model.getClipboardPane(ClipBoard.FIND_PANE);
        icon = pane.getPaneIcon();
        if (icon != null && icon.getIconHeight() > h) h = icon.getIconHeight();
        insertTab(pane.getPaneName(), icon, pane, 
        			pane.getPaneDescription(), ClipBoard.FIND_PANE);
        
        pane = model.getClipboardPane(ClipBoard.EDITOR_PANE);
        icon = pane.getPaneIcon();
        if (icon != null && icon.getIconHeight() > h) h = icon.getIconHeight();
        insertTab(pane.getPaneName(), icon, pane, 
        		pane.getPaneDescription(), ClipBoard.EDITOR_PANE);
        
        pane = model.getClipboardPane(ClipBoard.INFO_PANE);
        icon = pane.getPaneIcon();
        if (icon != null && icon.getIconHeight() > h) h = icon.getIconHeight();
        insertTab(pane.getPaneName(), icon, pane, 
        			pane.getPaneDescription(), ClipBoard.INFO_PANE);
        setSelectedIndex(model.getPaneIndex());
        
        Dimension d;
        
        if (ClipBoard.HORIZONTAL_SPLIT) d = new Dimension(dim.width, h+10);
        else d = new Dimension(5, dim.height);
        setPreferredSize(d);
        
        setMinimumSize(new Dimension(5, h));
    }
    
    /** Creates a new instance. */
    ClipBoardUI()
    {
    	super(JTabbedPane.TOP, JTabbedPane.WRAP_TAB_LAYOUT);
    	setAlignmentX(LEFT_ALIGNMENT);
    	
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
    	addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { 
            	controller.removeRollOver(); }
        });
        addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e)
            {
                switch (getSelectedIndex()) {
                    case ClipBoard.FIND_PANE:
                        controller.setSelectedPane(ClipBoard.FIND_PANE);
                        break;
                    case ClipBoard.ANNOTATION_PANE:
                        controller.setSelectedPane(ClipBoard.ANNOTATION_PANE);
                        break;
                    case ClipBoard.INFO_PANE:
                        controller.setSelectedPane(ClipBoard.INFO_PANE);
                        break;
                    case ClipBoard.EDITOR_PANE:
                        controller.setSelectedPane(ClipBoard.EDITOR_PANE);
                        break;
                    case ClipBoard.CLASSIFICATION_PANE:
                        controller.setSelectedPane(
                                ClipBoard.CLASSIFICATION_PANE);
                        break;
                };
            }
        });
        //listener
        ClipBoardPane pane = model.getClipboardPane(ClipBoard.FIND_PANE);
        pane.addPropertyChangeListener(FindPane.SELECTED_PROPERTY, controller);
        Map m = model.getClipBoardPanes();
        Iterator i = m.keySet().iterator();
        while (i.hasNext()) {
			pane = (ClipBoardPane) m.get(i.next());;
			pane.addMouseListener(new MouseAdapter() {
	            public void mouseEntered(MouseEvent e) { 
	            	controller.removeRollOver(); }
	        });
		}
    }
    
    /**
     * Displays the results of a search action.
     * 
     * @param foundNodes The results.
     */
    void setSearchResults(List foundNodes)
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
    int getTabPaneHeight() { return getHeight(); }
    
    /**
     * Sets the selected tabbed pane.
     *  
     * @param index The index of the selected tabbed pane.
     */
    void setSelectedPane(int index) { setSelectedIndex(index); }
    
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

    /**
     * Sets the channels metadata.
     * 
     * @param l 	The value to set.
     * @param image The image linked to metadata.
     * 
     */
    void setChannelMetadata(List l, ImageData image)
    {
        if (model.getPaneIndex() != ClipBoard.INFO_PANE) return;
        InfoPane pane = (InfoPane) model.getClipboardPane(ClipBoard.INFO_PANE);
        pane.setChannelsMetadata(l, image);
    }

    /**
     * Displays the retrieved classifications.
     * 
     * @param paths The classifications to display.
     */
    void showClassifications(Set paths)
    {
        if (model.getPaneIndex() != ClipBoard.CLASSIFICATION_PANE) return;
        ClassificationPane pane = (ClassificationPane) 
                    model.getClipboardPane(ClipBoard.CLASSIFICATION_PANE);
        pane.showClassifications(paths);
    }
    
    /**
	 * Returns <code>true</code> if annotation data to save, 
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
    boolean hasAnnotationToSave()
    {
    	ClipBoardPane pane = model.getClipboardPane(ClipBoard.ANNOTATION_PANE);
        return pane.hasDataToSave();
    }
    
    /**
	 * Returns <code>true</code> if edited data to save, 
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
    boolean hasEditedToSave()
    {
        ClipBoardPane pane = model.getClipboardPane(ClipBoard.EDITOR_PANE);
        return pane.hasDataToSave();
    }
    
}
