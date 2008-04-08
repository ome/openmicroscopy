/*
 * org.openmicroscopy.shoola.agents.dataBrowser.view.DataBrowserUI 
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
package org.openmicroscopy.shoola.agents.dataBrowser.view;



//Java imports
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.dataBrowser.DataBrowserAgent;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.Browser;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageNode;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.search.SearchObject;

/** 
 * The view.
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
class DataBrowserUI
	extends JPanel
{

	/** Reference to the tool bar. */
	private DataBrowserToolBar toolBar;
	
	/** Reference to the model. */
	private DataBrowserModel	model;
	
	/** Reference to the control. */
	private DataBrowserControl	controller;
	
	/** The slide show view. */
	private SlideShowView 		slideShowView;
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		setLayout(new BorderLayout(0, 0));
		add(toolBar, BorderLayout.NORTH);
		add(model.getBrowser().getUI(), BorderLayout.CENTER);
	}
	
	/** Creates a new instance. */
	DataBrowserUI() {}
	
	/**
	 * Links the components composing the MVC triad.
	 * 
	 * @param model			Reference to the model. 
	 * 						Mustn't be <code>null</code>.
	 * @param controller	Reference to the control. 
	 * 						Mustn't be <code>null</code>.
	 */
	void initialize(DataBrowserModel model, DataBrowserControl controller)
	{
		if (model == null)
			throw new IllegalArgumentException("No model.");
		if (controller == null)
			throw new IllegalArgumentException("No control.");
		this.model = model;
		this.controller = controller;
		toolBar = new DataBrowserToolBar(this, controller);
		buildGUI();
	}
	
	/**
	 * Returns the selected object in order to filter the node.
	 * 
	 * @return See above.
	 */
	SearchObject getSelectedFilter() { return toolBar.getSelectedFilter(); }

	/**
	 * Returns the collection of existing tags.
	 * 
	 * @return See above.
	 */
	Collection getExistingTags() { return model.getExistingTags(); }
	
	/**
	 * Updates the UI elements when the tags are loaded.
	 * 
	 * @param tags The collection of tags to display.
	 */
	void setTags(Collection tags) { toolBar.setTags(tags); }
	
	/**
	 * Creates or deletes the slide show view.
	 * 
	 * @param create	Pass <code>true</code> to create a new dialog,
	 * 					<code>false</code> to delete it.
	 * @param images	The images to display.
	 */
	void slideShowView(boolean create, boolean images)
	{
		toolBar.enableSlideShow(!create);
		if (!create) {
			if (slideShowView != null) {
				model.cancelSlideShow();
				slideShowView.close();
			}
			//slideShowView = null; 
			return;
		}
		
		//if (slideShowView != null) return;
		Browser browser = model.getBrowser();
		
		List<ImageNode> nodes;
		Iterator i;
		if (images) nodes = browser.getVisibleImageNodes();
		else {
			Set selection = browser.getSelectedDisplays();
			nodes = new ArrayList<ImageNode>();
			if (selection != null) {
				i = selection.iterator();
				Object n;
				while (i.hasNext()) {
					n = i.next();
					if (n instanceof ImageNode)
						nodes.add((ImageNode) n);
				}
			}
			
		}
		if (nodes == null || nodes.size() == 0) return;
		List<ImageNode> selection = new ArrayList<ImageNode>(nodes.size());
		ImageNode n;
		i = nodes.iterator();
		while (i.hasNext()) {
			n = (ImageNode) i.next();
			selection.add(n.copy());
		}
		Registry reg = DataBrowserAgent.getRegistry();
		slideShowView = new SlideShowView(
										reg.getTaskBar().getFrame(), 
										selection);
		slideShowView.addPropertyChangeListener(controller);
		model.fireFullSizeLoading(selection);
		UIUtilities.centerAndShow(slideShowView);
		if (model.getState() != DataBrowser.LOADING_SLIDE_VIEW)
			setSlideViewStatus(true, -1);
	}
	
	/**
     * Adjusts the status bar according to the specified arguments.
     * 
     * @param hideProgressBar Whether or not to hide the progress bar.
     * @param progressPerc  The percentage value the progress bar should
     *                      display.  If negative, it is iterpreted as
     *                      not available and the progress bar will be
     *                      set to indeterminate mode.  This argument is
     *                      only taken into consideration if the progress
     *                      bar shouldn't be hidden.
     */
    void setSlideViewStatus(boolean hideProgressBar, int progressPerc)
    {
    	if (slideShowView != null) 
    		slideShowView.setProgress(hideProgressBar, progressPerc);
    }
    
    /**
     * Returns <code>true</code> if the roll flag is on, <code>false</code>
     * otherwise.
     * 
     * @return See above.
     */
    boolean isRollOver() { return model.isRollOver(); }
    
}
