/*
 * org.openmicroscopy.shoola.agents.util.annotator.view.AnnotatorView 
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
package org.openmicroscopy.shoola.agents.util.annotator.view;

//Java imports
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JSeparator;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.IconManager;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import pojos.DataObject;

/** 
 * The {@link Annotator}'s View. Embeds the <code>AnnotatorUI</code>
 * to let users interact with annotations. Also provides statusBar
 * and a working pane. 
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
class AnnotatorView
	extends JDialog
{

	/** The default size of the window. */
	private static final Dimension	DEFAULT_SIZE = new Dimension(800, 500);
	
	/** The title of the window. */
	private static final String 	TITLE = "Annotate";
	
	/** The subtitle of the window. */
	private static final String		NOTE = "Annotate the selected items.";
  
	/** 
	 * The subtitle of the window when the annotation mode
	 * is {@link Annotator#BULK_ANNOTATE_MODE}.
	 */
	private static final String		NOTE_CHILDREN = "Annotate the images " +
			"contained in the selected folder.";
  
	/** Reference to the Controller. */
	private AnnotatorControl	controller;
  
	/** Reference to the Model. */
	private AnnotatorModel		model;
  
	/** The status bar. */
	private StatusBar			statusBar;
  
	/** The UI component displaying the annotations. */
	private AnnotatorUI			annotatorUI;
  
	/** 
	 * Builds the UI component hosting the controls.
	 * 
	 * @return See above.
	 */
	private JPanel buildToolBar()
	{
		JPanel bar = new JPanel();
		bar.setBorder(null);
		JButton b = new JButton(controller.getAction(AnnotatorControl.FINISH));
		bar.add(b);
		getRootPane().setDefaultButton(b);
		bar.add(Box.createRigidArea(AnnotatorUtil.H_SPACER_SIZE));
		b = new JButton(controller.getAction(AnnotatorControl.CANCEL));
		bar.add(b);
		return UIUtilities.buildComponentPanelRight(bar);
	}
  
	/**
	 * Builds the UI component displaying the annotations.
	 * 
	 * @return See above.
	 */
	private JPanel buildBody()
	{
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.add(annotatorUI);
		p.add(new JSeparator());
		p.add(buildToolBar());
		return p;
	}
  
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		IconManager icons = IconManager.getInstance();
		String note = NOTE;
		if (model.getAnnotationMode() == Annotator.BULK_ANNOTATE_MODE)
			note = NOTE_CHILDREN;
		TitlePanel tp = new TitlePanel(TITLE, note, 
  						icons.getIcon(IconManager.ANNOTATION_48));
  	
		Container c = getContentPane();
		c.setLayout(new BorderLayout(0, 0));
		c.add(tp, BorderLayout.NORTH);
		c.add(buildBody(), BorderLayout.CENTER);
		c.add(statusBar, BorderLayout.SOUTH);
	}
  
	/** Sets the properties of the window. */
	private void setProperties()
	{
		setModal(true);
		setTitle(TITLE);
	}
  
	/** Creates a new instance. */
	AnnotatorView()
	{
		super(AnnotatorFactory.getOwner());
		setProperties();
	}
  
	/**
	 * Links this View to its Controller.
	 * 
	 * @param model 		The Model. Mustn't be <code>null</code>.
	 * @param controller 	The Controller. Mustn't be <code>null</code>.
	 */
	void initialize(AnnotatorModel model, AnnotatorControl controller)
	{
		if (model == null) throw new IllegalArgumentException("No model.");
		if (controller == null) 
			throw new IllegalArgumentException("No control.");
		this.controller = controller;
		this.model = model;
		statusBar = new StatusBar();
		annotatorUI = new AnnotatorUI(model, controller);
		buildGUI();
	}
	
	/** Displays the annotation for the selected images. */
	void showAnnotations() { annotatorUI.showAnnotations(); }
	
	/**
	 * Sets the status of the {@link #statusBar}.
	 * 
	 * @param text 	The status message.
	 * @param hide	Pass <code>true</code> to hide the progress bar, 
	 *              <code>false</otherwise>.
	 */
	void setStatus(String text, boolean hide)
	{
		statusBar.setStatus(text);
		statusBar.setProgress(hide);
	}
	
	/**
	 * Returns the textual annotation.
	 * 
	 * @return See above.
	 */
	String getAnnotationText () { return annotatorUI.getAnnotationText(); }
	
	/**
	 * Returns <code>true</code> if the user has annotation to save.
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean hasDataToSave() { return annotatorUI.hasDataToSave(); }
	
	/**
	 * Returns the name of the selected <code>Dataset</code> or
	 * the partial name if the selected node is an <code>Image</code>.
	 * 
	 * @return See above.
	 */
	String getSelectedObjectName()
	{
		return annotatorUI.getSelectedObjectName();
	}
	
	/** Sets the window on screen. */
	void setOnScreen()
	{
		setSize(DEFAULT_SIZE);
		UIUtilities.centerAndShow(this);
	}

	/**
	 * Returns the currently selected data object if any or <code>null</code>.
	 * 
	 * @return See above.
	 */
	DataObject getSelectedDataObject()
	{
		return annotatorUI.getSelectedDataObject();
	}

}
