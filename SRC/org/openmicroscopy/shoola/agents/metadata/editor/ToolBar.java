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
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToolBar;


//Third-party libraries
import org.jdesktop.swingx.JXBusyLabel;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.ImageData;

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

	/** Indicates the loading progress. */
	private JXBusyLabel		busyLabel;

	/** 
	 * The component hosting the control only used when an <code>Image</code>
	 * is selected.
	 */
	private JComponent		imageBar;

	/** Reference to the Control. */
	private EditorControl	controller;
	
	/** Reference to the Model. */
	private EditorModel 	model;
	
	/** Initializes the components. */
	private void initComponents()
	{
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		IconManager icons = IconManager.getInstance();
		int h = UIUtilities.DEFAULT_ICON_HEIGHT;
		int w = UIUtilities.DEFAULT_ICON_WIDTH;
		Icon icon = icons.getIcon(IconManager.SAVE);
		if (icon != null) {
			if (icon.getIconHeight() > h) h = icon.getIconHeight();
			if (icon.getIconWidth() > w) w = icon.getIconWidth();
		}
		saveButton = new JButton(icon);
		saveButton.setToolTipText("Save changes back to the server.");
		saveButton.addActionListener(controller);
		saveButton.setActionCommand(""+EditorControl.SAVE);
		saveButton.setEnabled(false);
		icon = icons.getIcon(IconManager.DOWNLOAD);
		if (icon != null) {
			if (icon.getIconHeight() > h) h = icon.getIconHeight();
			if (icon.getIconWidth() > w) w = icon.getIconWidth();
		}
		downloadButton = new JButton(icon);
		downloadButton.setToolTipText("Download the Archived File(s).");
		downloadButton.addActionListener(controller);
		downloadButton.setActionCommand(""+EditorControl.DOWNLOAD);
		downloadButton.setEnabled(false);
		
		UIUtilities.unifiedButtonLookAndFeel(saveButton);
		UIUtilities.unifiedButtonLookAndFeel(downloadButton);
		
    	busyLabel = new JXBusyLabel(new Dimension(w, h));
    	busyLabel.setEnabled(true);
    	busyLabel.setVisible(false);
	}
    
    /** 
     * Builds the tool bar displaying the controls related to 
     * an image.
     * 
     * @return See above.
     */
    private JComponent buildImageToolBar()
    {
    	JToolBar bar = new JToolBar();
    	bar.setFloatable(false);
    	bar.setRollover(true);
    	bar.setBorder(null);
    	bar.add(downloadButton);
    	return bar;
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
    	return bar;
    }
    
    /** Builds and lays out the UI. */
    private void buildGUI()
    {
    	JPanel bars = new JPanel();
    	bars.setLayout(new BoxLayout(bars, BoxLayout.X_AXIS));
    	bars.add(buildGeneralBar());
    	bars.add(Box.createHorizontalStrut(2));
    	imageBar = buildImageToolBar();
    	imageBar.setVisible(false);
    	bars.add(imageBar);
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
    void setControls() { downloadButton.setEnabled(model.isArchived()); }
    
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
    
    /**
     * Updates the UI when a new object is selected.
     */
    void buildUI()
    {
    	if ((model.getRefObject() instanceof ImageData))
    		imageBar.setVisible(!model.isMultiSelection());
    	else imageBar.setVisible(false);
    	revalidate();
    	repaint();
    }
	
}
