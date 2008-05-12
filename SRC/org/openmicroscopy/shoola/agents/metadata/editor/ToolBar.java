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
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JToolBar;


//Third-party libraries

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
	implements ActionListener
{

	/** 
	 * Index indicating that the tool bar is added to the top of a component, 
	 * useful to set the separator.
	 */
	static final int			TOP = 100;
	
	/** 
	 * Index indicating that the tool bar is added to the bottom of a component, 
	 * useful to set the separator.
	 */
	static final int			BOTTOM = 101;
	
	/** Action ID to save the data. */
	private static final int 	SAVE = 0;
	
	/** Action ID to display info. */
	private static final int 	INFO = 1;
	
	/** Action ID to download archived files. */
	private static final int	DOWNLOAD = 2;
	
	/** Action ID to collapse all extanded nodes. */
	private static final int	COLLAPSE = 3;
	
	/** One of the location constants defined by this class. */
	private int				index;
	
	/** Button to save the annotations. */
	private JButton			saveButton;
	
	/** Button to display the image info. */
	private JButton			infoButton;
	
	/** Button to download the original image. */
	private JButton			downloadButton;
	
	/** Button to collapse all the extanded nodes. */
	private JButton			collapseButton;
	
	/** The decorator. */
	private JPanel			decorator;
	
	/** The decorator. */
	private JPanel			annotation;
	
	/** Reference to the Model. */
	private EditorModel		model;
	
	/** Reference to the View. */
	private EditorUI		view;
	
	/** Reference to the Control. */
	private EditorControl	controller;
	
	/** Initializes the components. */
	private void initComponents()
	{
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		IconManager icons = IconManager.getInstance();
		saveButton = new JButton(icons.getIcon(IconManager.SAVE));
		saveButton.setToolTipText("Save changes.");
		saveButton.addActionListener(this);
		saveButton.setActionCommand(""+SAVE);
		saveButton.setEnabled(false);
		downloadButton = new JButton(icons.getIcon(IconManager.DOWNLOAD));
		downloadButton.setToolTipText("Download the archived files.");
		downloadButton.addActionListener(this);
		downloadButton.setActionCommand(""+DOWNLOAD);
		downloadButton.setEnabled(false);
		infoButton = new JButton(icons.getIcon(IconManager.INFO));
		infoButton.setToolTipText("View image's info.");
		infoButton.addActionListener(this);
		infoButton.setActionCommand(""+INFO);
		
		collapseButton = new JButton(icons.getIcon(IconManager.COLLAPSE));
		collapseButton.setToolTipText("Collapse all nodes.");
		collapseButton.addActionListener(this);
		collapseButton.setActionCommand(""+COLLAPSE);
		
		UIUtilities.unifiedButtonLookAndFeel(downloadButton);
		UIUtilities.unifiedButtonLookAndFeel(infoButton);
		UIUtilities.unifiedButtonLookAndFeel(collapseButton);
		
		decorator = new JPanel();
    	decorator.setLayout(new BoxLayout(decorator, BoxLayout.X_AXIS));
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
    	//bar.add(infoButton);
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
    
    /** 
     * Builds the controls bar.
     * 
     * @return See above.
     */
    private JComponent buildControlsBar()
    {
    	JToolBar bar = new JToolBar();
    	bar.setFloatable(false);
    	bar.setRollover(true);
    	bar.setBorder(null);
    	bar.add(collapseButton);
    	return bar;
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model 		Reference to the model. 
     * 						Mustn't be <code>null</code>.
     * @param view 			Reference to the view. Mustn't be <code>null</code>.
     * @param controller 	Reference to the view. Mustn't be <code>null</code>.
     * @param index			 One of the location constants defined by this 
     * 						class.	
     */
    ToolBar(EditorModel model, EditorUI view, EditorControl controller, 
    		int index)
    {
    	if (model == null)
    		throw new IllegalArgumentException("No model.");
    	if (view == null)
    		throw new IllegalArgumentException("No view.");
    	if (controller == null)
    		throw new IllegalArgumentException("No control.");
    	this.model = model;
    	this.view = view;
    	this.controller = controller;
    	this.index = index;
    	initComponents();
    	buildGUI();
    }
    
    /** Builds and lays out the UI. */
    void buildGUI()
    {
    	removeAll();
    	decorator.removeAll();
    	decorator.add(buildGeneralBar());
    	if (model.getRefObject() instanceof ImageData) {
    		decorator.add(Box.createHorizontalStrut(2));
    		decorator.add(new JSeparator(JSeparator.VERTICAL));
    		decorator.add(buildImageToolBar());
    	}
    	
    	switch (index) {
			case BOTTOM:
	    		add(new JSeparator(JSeparator.HORIZONTAL));
	    		add(UIUtilities.buildComponentPanel(decorator));
				break;
			case TOP:
			default:
				decorator.add(Box.createHorizontalStrut(2));
				decorator.add(new JSeparator(JSeparator.VERTICAL));
				decorator.add(buildControlsBar());
				add(UIUtilities.buildComponentPanel(decorator));
	    		add(new JSeparator(JSeparator.HORIZONTAL));
				break;
		}
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
     * Indicates the types of annotations attached to the item
     * using icons. 
     */
    void setDecorator()
    {
    	IconManager icons = IconManager.getInstance();
    	if (annotation == null) {
    		annotation = new JPanel();
        	annotation.setLayout(new FlowLayout(FlowLayout.LEFT));
    	} else annotation.removeAll();
    	JLabel label;
    	int n = model.getTextualAnnotationCount();
    	if (n > 0) {
    		label = UIUtilities.setTextFont(AnnotationUI.LEFT
    										+n+AnnotationUI.RIGHT);
    		label.setIcon(icons.getIcon(IconManager.ANNOTATION));
    		annotation.add(label);
    		annotation.add(Box.createHorizontalStrut(5));
    	}
    	n = model.getTagsCount();
    	if (n > 0) {
    		label = UIUtilities.setTextFont(AnnotationUI.LEFT
    										+n+AnnotationUI.RIGHT);
    		label.setIcon(icons.getIcon(IconManager.TAG));
    		annotation.add(label);
    		annotation.add(Box.createHorizontalStrut(5));
    	}
    	n = model.getUrlsCount();
    	if (n > 0) {
    		label = UIUtilities.setTextFont(AnnotationUI.LEFT
    										+n+AnnotationUI.RIGHT);
    		label.setIcon(icons.getIcon(IconManager.URL));
    		annotation.add(label);
    		annotation.add(Box.createHorizontalStrut(5));
    	}
    	n = model.getAttachmentsCount();
    	if (n > 0) {
    		label = UIUtilities.setTextFont(AnnotationUI.LEFT
    										+n+AnnotationUI.RIGHT);
    		label.setIcon(icons.getIcon(IconManager.DOC));
    		annotation.add(label);
    		annotation.add(Box.createHorizontalStrut(5));
    	}
    	decorator.add(Box.createHorizontalStrut(10));
    	//decorator.add(new JSeparator(JSeparator.VERTICAL));
    	decorator.add(annotation);
    	decorator.revalidate();
    	decorator.repaint();
    	//add(decorator);
    }
    
    /**
     * 
     * @see ActionListener#actionPerformed(ActionEvent)
     */
	public void actionPerformed(ActionEvent e)
	{
		int index = Integer.parseInt(e.getActionCommand());
		switch (index) {
			case SAVE:
				view.saveData();
				break;
			case DOWNLOAD:
				model.download();
				break;
			case INFO:
				controller.showImageInfo();
				break;
			case COLLAPSE:
				view.collapseAllNodes();
		}
		
	}
	
}
