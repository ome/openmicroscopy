/*
 * org.openmicroscopy.shoola.agents.metadata.view.MetadataViewerUI 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.metadata.view;


//Java imports
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.agents.util.ViewedByItem;
import org.openmicroscopy.shoola.agents.util.ViewerSorter;
import org.openmicroscopy.shoola.env.rnd.RndProxyDef;
import org.openmicroscopy.shoola.env.ui.TopWindow;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.ImageData;
import pojos.ProjectData;

/** 
 * The View.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
class MetadataViewerUI 
	extends TopWindow
	implements PropertyChangeListener
{

    /** The text corresponding to the creation of a <code>Project</code>. */
    private static final String     PROJECT_MSG = "Project";
    
    /** The text corresponding to the creation of a <code>Dataset</code>. */
    private static final String     DATASET_MSG = "Dataset";
    
    /** The text corresponding to the creation of a <code>Image</code>. */
    private static final String     IMAGE_MSG = "Image";
    
    /** The title of the dialog. */
    private static final String		TITLE = "Add metadata";
    
    /** The description of the dialog. */
    private static final String		DESCRIPTION = "Add comments, tags, etc., " +
    		"to the selected items.";
    
	/** Reference to the Control. */
	private MetadataViewerControl 		controller;

	/** Reference to the Model. */
	private MetadataViewerModel   		model;
	
	/** The component hosting the UI components. */
	private JPanel						uiDelegate;
	
	/** The header of the component. */
	private TitlePanel 					titlePanel;
	
	/** 
	 * The source invoking the menu displaying the list of users
	 * who viewed the image. 
	 */
	private Component					source;
	
	/** 
	 * The location where to pop up the  menu displaying the list of users
	 * who viewed the image. 
	 */
	private Point						location;
	
	/** The menu displaying the user who viewed the image. */
	private JPopupMenu					viewedByMenu;
	
	/** The item used to display the thumbnails. */
	private JMenuItem					thumbnailsMenuItem;
	
	/** 
     * Returns the message corresponding to the <code>DataObject</code>.
     * 
     * @return See above
     */
    private String getMessage()
    {
        Class nodeType = model.getRefObject().getClass();
        if (nodeType.equals(ProjectData.class)) return PROJECT_MSG;
        else if (nodeType.equals(DatasetData.class)) return DATASET_MSG;
        else if (nodeType.equals(ImageData.class)) return IMAGE_MSG;
        return "";
    }
    
	/** Builds and lays out the GUI. */
    private void buildGUI()
    {
    	IconManager icons = IconManager.getInstance();
		titlePanel = new TitlePanel(TITLE, DESCRIPTION, 
                 icons.getIcon(IconManager.METADATA_48));
		
		
    	Container c = getContentPane();
        c.setLayout(new BorderLayout(0, 0));
        //c.add(model.getEditor().getUI());
        
        JSplitPane pane = new JSplitPane();
        pane.setResizeWeight(1.0);
        pane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        pane.setOneTouchExpandable(true);
        pane.setContinuousLayout(true);
        pane.setLeftComponent(model.getEditor().getUI());
        //pane.setRightComponent(model.getBrowser().getUI());
        uiDelegate = new JPanel();
        uiDelegate.setLayout(new BorderLayout(0, 0));
        uiDelegate.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        //uiDelegate.add(titlePanel, BorderLayout.NORTH);
        uiDelegate.add(model.getEditor().getUI(), BorderLayout.CENTER);
        c.add(titlePanel, BorderLayout.NORTH);
        c.add(uiDelegate, BorderLayout.CENTER);
    }
    
	/**
	 * Creates a new instance.
	 * The 
	 * {@link #initialize(MetadataViewerControl, MetadataViewerModel) initialize} 
	 * method should be called straight after to link this View 
	 * to the Controller.
	 * 
	 * @param title The window title.
	 */
	MetadataViewerUI()
	{
		super("");
	}
	
	/**
	 * Links this View to its Controller and Model.
	 * 
	 * @param controller    Reference to the Control.
	 *                      Mustn't be <code>null</code>.
	 * @param model         Reference to the Model.
	 *                      Mustn't be <code>null</code>.
	 */
	void initialize(MetadataViewerControl controller, MetadataViewerModel model)
	{
		if (controller == null) throw new NullPointerException("No control.");
		if (model == null) throw new NullPointerException("No model.");
		this.controller = controller;
		this.model = model;
		buildGUI();
		setName("metadata viewer window");
	}
	
	/** Updates display when the new root node is set. */
	void setRootObject()
	{
		String message = getMessage();
		titlePanel.setTitle(message);
		titlePanel.setSubtitle("Edit the "+ message.toLowerCase()+": "+
				model.getRefObjectName());
		uiDelegate.revalidate();
		uiDelegate.repaint();
		viewedByMenu = null;
	}
	
	/**
	 * Returns the UI.
	 * 
	 * @return See above.
	 */
	JComponent getUI() { return uiDelegate; }
	
	/**
	 * Indicates that the color of the passed channel has changed.
	 * 
	 * @param index The index of the channel.
	 */
	void onChannelColorChanged(int index)
	{
		model.getEditor().onChannelColorChanged(index);
	}
	
	/**
	 * Sets the location and the source where to pop up the menu.
	 * 
	 * @param source	The source to set.
	 * @param location	The location to set.
	 */
	void setLocationAndSource(Component source, Point location)
	{
		this.source = source;
		this.location = location;
	}
	
	/** 
	 * Displays the menu displaying the list of users who viewed the image.
	 * 
	 * @param source The component invoking the loading.
     * @param location The location of the mouse pressed.
     */
	void viewedBy(Component source, Point location)
	{
		if (viewedByMenu == null) {
			Map m = model.getViewedBy();
			viewedByMenu = new JPopupMenu();
			ViewerSorter sorter = new ViewerSorter();
			List list = sorter.sort(m.keySet());
			Iterator i = list.iterator();
			ViewedByItem item ;
			ExperimenterData exp;
			while (i.hasNext()) {
				exp = (ExperimenterData) i.next();
				item = new ViewedByItem(exp, (RndProxyDef) m.get(exp));
				item.addPropertyChangeListener(
						ViewedByItem.VIEWED_BY_PROPERTY, this);
				viewedByMenu.add(item);
			}
			if (list.size() == 0) {
				thumbnailsMenuItem = new JMenuItem("Not viewed");
				thumbnailsMenuItem.setToolTipText("No other users " +
						"viewed the image.");
			} else {
				IconManager icons = IconManager.getInstance();
				thumbnailsMenuItem = new JMenuItem("Show thumbnails");
				thumbnailsMenuItem.setIcon(icons.getIcon(
						IconManager.PREVIEW_THUMBNAILS_32));
				thumbnailsMenuItem.addActionListener(new ActionListener() {
					
					public void actionPerformed(ActionEvent e)
					{
						showViewedBy();
					}
				});
			}
			
			viewedByMenu.add(thumbnailsMenuItem);
		}
		viewedByMenu.show(source, location.x, location.y);
	}
	
	/** Displays all the thumbnails. */
	private void showViewedBy()
	{
		if (viewedByMenu == null) return;
		ViewedByItem item, itemNew;
		Component comp;
		BufferedImage img;
		Component[] components = viewedByMenu.getComponents();
		List<ViewedByItem> items = new ArrayList<ViewedByItem>();
		for (int i = 0; i < components.length; i++) {
			comp = components[i];
			if (comp instanceof ViewedByItem) {
				item = (ViewedByItem) comp;
				img = item.getImage();
				if (img != null) {
					item.setImage(img);
					itemNew = new ViewedByItem(item.getExperimenter(), 
							item.getRndDef(), false);
					itemNew.setImage(img);
					itemNew.addPropertyChangeListener(
							ViewedByItem.VIEWED_BY_PROPERTY, this);
					items.add(itemNew);
				}
			}
		}
		model.getEditor().getRenderer().loadRndSettings(true, items);
	}
	
	/** 
	 * Sets the thumbnails.
	 * 
	 * @param thumbnails The value to set.
	 */
	void setThumbnails(Map<Long, BufferedImage> thumbnails)
	{
		if (viewedByMenu == null) return;
		Component[] components = viewedByMenu.getComponents();
		Component comp;
		ViewedByItem item, itemNew;
		BufferedImage img;
		List<ViewedByItem> items = new ArrayList<ViewedByItem>();
		for (int i = 0; i < components.length; i++) {
			comp = components[i];
			if (comp instanceof ViewedByItem) {
				item = (ViewedByItem) comp;
				img = thumbnails.get(item.getExperimenterID());
				if (img != null) {
					item.setImage(img);
					itemNew = new ViewedByItem(item.getExperimenter(), 
							item.getRndDef(), false);
					itemNew.setImage(img);
					itemNew.addPropertyChangeListener(
							ViewedByItem.VIEWED_BY_PROPERTY, this);
					items.add(itemNew);
				}
			}
		}
		thumbnailsMenuItem.setEnabled(items.size() > 0);
		model.getEditor().getRenderer().loadRndSettings(true, null);
	}
	
	/**
	 * Sets the rendering settings.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		if (ViewedByItem.VIEWED_BY_PROPERTY.equals(
				evt.getPropertyName()))
			model.applyRenderingSettings(
					(RndProxyDef) evt.getNewValue());

	}
	/**
	 * Overridden so the pack method is not invoked and the component is
	 * not displayed on screen.
	 */
	public void setOnScreen() {}
}
