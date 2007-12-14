/*
 * org.openmicroscopy.shoola.agents.util.tagging.view.TaggerView
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.util.tagging.view;


//Java imports
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;

import javax.swing.JDialog;
import javax.swing.JFrame;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.util.tagging.util.TagSaverDef;
import org.openmicroscopy.shoola.util.ui.HistoryDialog;
import org.openmicroscopy.shoola.util.ui.IconManager;
import org.openmicroscopy.shoola.util.ui.TitlePanel;

import pojos.CategoryData;
import pojos.CategoryGroupData;
import pojos.ExperimenterData;
	
/** 
 * The {@link Tagger}'s UI. Embeds the different UI components
 * to tag images.
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
class TaggerView
	extends JDialog
{
	
	/** The title of the dialog. */
	private static final String 	TITLE = "Tagging editor";

	/** Note explaining how to create more than one. */
	private static final String		NOTE = "To add more than one tag, " +
			"separate each tag with a comma.\n Or select one or more " +
			"existing tags from the menu.";
	
	/** Text displayed in the title. */
	private static final String		TEXT = "Add tag to image.";
 
	/** The default size of the window. */
	private static final Dimension	WIN_SIZE = new Dimension(500, 400);
	
	/** The Model. */
	private TaggerModel 	model;
	
	/** The Controller. */
	private TaggerControl	controller;
	
	/** The status bar. */
	private StatusBar		statusBar;
	
	/** The main UI component of the display. */
	private TaggerUI		uiDelegate;
	
	/** Sets the properties of the dialog. */
	private void setProperties()
	{
		setModal(true);
		setTitle(TITLE);
	}
	
	/** Initializes the components. */
	private void initComponents()
	{
		statusBar = new StatusBar();
		uiDelegate = new TaggerUI(model, controller, this);
	}
	
	/** Builds and lays out the GUI. */
	private void buildGUI()
	{
		Container c = getContentPane();
		IconManager icons = IconManager.getInstance();
		TitlePanel tp = new TitlePanel(TITLE, TEXT, NOTE,
								icons.getIcon(IconManager.TAG_BIG));
		c.add(tp, BorderLayout.NORTH);
		c.add(uiDelegate, BorderLayout.CENTER);
		c.add(statusBar, BorderLayout.SOUTH);
	}
	
	/** 
	 * Creates a new instance. 
	 * 
	 * @param owner The owner of the frame.
	 */
	TaggerView(JFrame owner)
	{
		super(owner);
		setProperties();
	}
	
	/**
	 * Links this View to its Controller.
     * 
     * @param controller	The Controller.
     * @param model 		The Model.
	 */
	void initialize(TaggerModel model, TaggerControl controller)
	{
		this.model = model;
		this.controller = controller;
		initComponents();
		buildGUI();
		setSize(WIN_SIZE);
	}
	
	/** Sets the focus on the <code>nameArea</code> field. */
    void requestFocusOnField() { uiDelegate.requestFocusOnField(); }
	
    /**
     * Sets the value of the progress bar.
     * 
     * @param hide  Pass <code>true</code> to hide the progress bar, 
     *              <code>false</otherwise>.
     */
    void setProgress(boolean hide) { statusBar.setProgress(hide); }
    
    /** Displays the available tags if any. */
    void showTags() { uiDelegate.showTags(); }
    
    /**
	 * Handles the selection of a tag set via the {@link HistoryDialog}.
	 * 
	 * @param item The item to handle.
	 */
    void handleTagSetSelection(CategoryGroupData item)
    {
    	uiDelegate.handleTagSetSelection(item);
    }
    
    /**
	 * Handles the selection of a tag via the {@link HistoryDialog}.
	 * 
	 * @param item The item to handle.
	 */
	void handleTagSelection(CategoryData item) 
	{
		uiDelegate.handleTagSelection(item);
	}
	
	TagSaverDef saveTags() { return uiDelegate.saveTags(); }

	ExperimenterData getExperimenter() {
		// TODO Auto-generated method stub
		return model.getExperimenter();
	}
	
}
