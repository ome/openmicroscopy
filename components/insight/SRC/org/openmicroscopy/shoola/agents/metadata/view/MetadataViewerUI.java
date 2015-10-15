/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import org.openmicroscopy.shoola.agents.events.iviewer.RndSettingsChanged;
import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.agents.metadata.MetadataViewerAgent;
import org.openmicroscopy.shoola.agents.metadata.rnd.Renderer;
import org.openmicroscopy.shoola.agents.util.ViewedByItem;
import org.openmicroscopy.shoola.env.rnd.RndProxyDef;
import org.openmicroscopy.shoola.env.ui.TopWindow;
import org.openmicroscopy.shoola.util.ui.TitlePanel;

import omero.gateway.model.DatasetData;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.ImageData;
import omero.gateway.model.ProjectData;

/** 
 * The View.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
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
    @SuppressWarnings("unused")
    private MetadataViewerControl 		controller;

    /** Reference to the Model. */
    private MetadataViewerModel   		model;
	
    /** The component hosting the UI components. */
    private JPanel						uiDelegate;
	
    /** The header of the component. */
    private TitlePanel 					titlePanel;
	
    /** The current ViewedByItems */
    private List<ViewedByItem> viewedByItems = new ArrayList<ViewedByItem>();
	
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
		viewedByItems.clear();
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
         * Creates the ViewedByItems
         */
        void createViewedByItems() {
    
            viewedByItems.clear();
    
            Map m = model.getViewedBy();
            Iterator i = m.keySet().iterator();
            ViewedByItem item;
            ExperimenterData exp;
            while (i.hasNext()) {
                exp = (ExperimenterData) i.next();
                ImageData img = model.getImage();
                if (img != null) {
                    boolean isOwnerSetting = img.getOwner().getId() == exp.getId();
                    item = new ViewedByItem(exp, (RndProxyDef) m.get(exp),
                            isOwnerSetting);
                    item.addPropertyChangeListener(ViewedByItem.VIEWED_BY_PROPERTY,
                            this);
                    viewedByItems.add(item);
                }
            }
            Renderer rnd = model.getEditor().getRenderer();
            if (rnd != null) {
                rnd.loadRndSettings(true, null);
            }
        }
	
	/** 
	 * Sets the thumbnails.
	 * 
	 * @param thumbnails The value to set.
	 */
        void setThumbnails(Map<Long, BufferedImage> thumbnails) {
            if (viewedByItems.isEmpty())
                return;
            for (ViewedByItem item : viewedByItems) {
                BufferedImage img = thumbnails.get(item.getExperimenterID());
                if (img != null) {
                    item.setImage(img);
                }
            }
            Renderer renderer = model.getEditor().getRenderer();
            if (renderer != null) // the renderer might not have been set yet
                model.getEditor().getRenderer().loadRndSettings(false, viewedByItems);
        }
	
	/**
	 * Sets the rendering settings.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		if (ViewedByItem.VIEWED_BY_PROPERTY.equals(
				evt.getPropertyName())) {
			model.applyRenderingSettings(
					(RndProxyDef) evt.getNewValue());
			//post an event
			RndSettingsChanged e = new RndSettingsChanged(
			        model.getImage().getId());
			MetadataViewerAgent.getRegistry().getEventBus().post(e);
		}
	}
	
	/**
	 * Overridden so the pack method is not invoked and the component is
	 * not displayed on screen.
	 */
	public void setOnScreen() {}
}
