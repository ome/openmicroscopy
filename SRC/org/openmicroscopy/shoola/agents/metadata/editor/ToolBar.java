/*
 * org.openmicroscopy.shoola.agents.metadata.editor.ToolBar 
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
package org.openmicroscopy.shoola.agents.metadata.editor;


//Java imports
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;


//Third-party libraries
import org.jdesktop.swingx.JXBusyLabel;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import pojos.ExperimenterData;
import pojos.ImageData;
import pojos.WellSampleData;

/** 
 * The tool bar of the editor.
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
class ToolBar 
	extends JPanel
{
	
	/** Button to save the annotations. */
	private JButton			saveButton;

	/** Button to download the original image. */
	private JButton			downloadButton;

	/** Button to load the rendering control for the primary select. */
	private JButton			rndButton;
	
	/** Button to refresh the selected tab. */
	private JButton			refreshButton;

	/** Button to bring up the activity panel. */
	private JButton			activityButton;
	
	/** Indicates the loading progress. */
	private JXBusyLabel		busyLabel;

	/** Reference to the Control. */
	private EditorControl	controller;
	
	/** Reference to the Model. */
	private EditorModel 	model;

	/** The option dialog. */
	private OptionsDialog  dialog;
	
	/** Initializes the components. */
	private void initComponents()
	{
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		IconManager icons = IconManager.getInstance();
		saveButton = new JButton(icons.getIcon(IconManager.SAVE));
		saveButton.setToolTipText("Save changes back to the server.");
		saveButton.addActionListener(controller);
		saveButton.setActionCommand(""+EditorControl.SAVE);
		saveButton.setEnabled(false);
		
		downloadButton = new JButton(icons.getIcon(IconManager.DOWNLOAD));
		downloadButton.setToolTipText("Download the Archived File(s).");
		downloadButton.addActionListener(controller);
		downloadButton.setActionCommand(""+EditorControl.DOWNLOAD);
		downloadButton.setEnabled(false);
		
		rndButton = new JButton(icons.getIcon(IconManager.RENDERER));
		rndButton.setToolTipText("Rendering control for the primary selected " +
				"image.");
		rndButton.addActionListener(controller);
		rndButton.setActionCommand(""+EditorControl.RENDERER);
		rndButton.setEnabled(false);
		
		refreshButton = new JButton(icons.getIcon(IconManager.REFRESH));
		refreshButton.setToolTipText("Refresh the selected tab.");
		refreshButton.addActionListener(controller);
		refreshButton.setActionCommand(""+EditorControl.REFRESH);
		
		activityButton = new JButton(icons.getIcon(IconManager.ACTIVITY));
		activityButton.setToolTipText("Display the publishing, " +
				"analysis options.");
		activityButton.setEnabled(false);
		activityButton.addMouseListener(new MouseAdapter() {
			
			/**
			 * Launches the dialog when the user releases the mouse.
			 * MouseAdapter#mouseReleased(MouseEvent)
			 */
			public void mouseReleased(MouseEvent e)
			{
				launchOptions((Component) e.getSource(), e.getPoint());
			}
		});
		refreshButton.addActionListener(controller);
		refreshButton.setActionCommand(""+EditorControl.REFRESH);
		UIUtilities.unifiedButtonLookAndFeel(saveButton);
		UIUtilities.unifiedButtonLookAndFeel(downloadButton);
		UIUtilities.unifiedButtonLookAndFeel(rndButton);
		UIUtilities.unifiedButtonLookAndFeel(refreshButton);

		UIUtilities.unifiedButtonLookAndFeel(activityButton);
		Dimension d = new Dimension(UIUtilities.DEFAULT_ICON_WIDTH, 
				UIUtilities.DEFAULT_ICON_HEIGHT);
    	busyLabel = new JXBusyLabel(d);
    	busyLabel.setEnabled(true);
    	busyLabel.setVisible(false);
	}
	
    /** 
     * Builds the general bar.
     * 
     * @return See above.
     */
    private JComponent buildGeneralBar()
    {
    	JToolBar bar = new JToolBar();
    	bar.setFloatable(false);
    	bar.setRollover(true);
    	bar.setBorder(null);
    	bar.add(saveButton);
    	bar.add(Box.createHorizontalStrut(5));
    	bar.add(refreshButton);
    	bar.add(Box.createHorizontalStrut(5));
    	bar.add(downloadButton);
    	bar.add(Box.createHorizontalStrut(5));
    	bar.add(activityButton);
    	/*
    	JButton b = new JButton("P");
    	b.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				AnalysePalette p = new AnalysePalette(controller);
				UIUtilities.centerAndShow(p);
			}
		});
		bar.add(b);
		*/
    	
    	return bar;
    }
    
    /** Builds and lays out the UI. */
    private void buildGUI()
    {
    	JPanel bars = new JPanel();
    	bars.setLayout(new BoxLayout(bars, BoxLayout.X_AXIS));
    	bars.add(buildGeneralBar());
    	JPanel p = new JPanel();
    	p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    	p.add(UIUtilities.buildComponentPanel(bars));
    	p.add(UIUtilities.buildComponentPanelRight(busyLabel));
    	setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    	add(p);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model 		Reference to the model. 
     * 						Mustn't be <code>null</code>.
     * @param controller 	Reference to the view. Mustn't be <code>null</code>.
     */
    ToolBar(EditorModel model, EditorControl controller)
    {
    	if (model == null)
    		throw new IllegalArgumentException("No model.");
    	if (controller == null)
    		throw new IllegalArgumentException("No control.");
    	this.model = model;
    	this.controller = controller;
    	initComponents();
    	buildGUI();
    }
    
    /** Enables the various controls. */
    void setControls()
    { 
    	
    	downloadButton.setEnabled(model.isArchived()); 
    }
    
    /**
     * Enables the {@link #saveButton} depending on the passed value.
     * 
     * @param b Pass <code>true</code> to save the data,
     * 			<code>false</code> otherwise. 
     */
    void setDataToSave(boolean b) { saveButton.setEnabled(b); }
    
    /**
     * Sets to <code>true</code> if loading data, to <code>false</code>
     * otherwise.
     * 
     * @param busy 	Pass <code>true</code> while loading data, 
     * 				<code>false</code> otherwise.
     */
    void setStatus(boolean busy)
    {
    	busyLabel.setBusy(busy);
    	busyLabel.setVisible(busy);
    }
    
    /** Updates the UI when a new object is selected. */
    void buildUI()
    {
    	Object refObject = model.getRefObject();
    	if ((refObject instanceof ImageData) || 
    			(refObject instanceof WellSampleData)) {
    		rndButton.setEnabled(!model.isRendererLoaded());
    		
    		if (model.isNumerousChannel())
    			rndButton.setEnabled(false);
    		if (model.isMultiSelection() && refObject instanceof ImageData) 
    			downloadButton.setEnabled(model.isArchived());
    	} else {
    		rndButton.setEnabled(false);
    		downloadButton.setEnabled(false);
    	}
    	revalidate();
    	repaint();
    }

    /** Sets the root object. */
	void setRootObject()
	{ 
		if (model.getRefObject() instanceof ExperimenterData) {
			activityButton.setEnabled(false);
			return;
		}
		activityButton.setEnabled(true);
		if (dialog != null) dialog.setRootObject();
	}
	
    
	/**
	 * Launches the Options.
	 * 
	 * @param source The location of the mouse pressed.
	 * @param p 	 The location of the mouse pressed.
	 */
	void launchOptions(Component source, Point p)
	{
		SwingUtilities.convertPointToScreen(p, source);
		if (dialog == null)
			dialog = new OptionsDialog(controller, model);
		
		dialog.setLocation(p);
		dialog.setVisible(true);
	}
}
