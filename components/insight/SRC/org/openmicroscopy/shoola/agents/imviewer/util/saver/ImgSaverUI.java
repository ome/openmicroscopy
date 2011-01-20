/*
 * org.openmicroscopy.shoola.agents.imviewer.util.saver.ImgSaverUI
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

package org.openmicroscopy.shoola.agents.imviewer.util.saver;




//Java imports
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.UIManager;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.IconManager;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.filechooser.CreateFolderDialog;

/** 
 * The UI delegate.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
class ImgSaverUI
	implements ActionListener, PropertyChangeListener
{
    
	 /** The tool tip of the <code>Approve</code> button. */
    static final String 			SAVE_AS = "Save the current image in" +
    											" the specified format.";
    
    /** Save the main image. */
    static final int				IMAGE = 0;
    
    /** Save the grid image. */
    static final int				GRID_IMAGE = 1;
    
    /** 
     * Save the images and an image of each channel composing the rendered 
     * image. 
     */
    static final int				IMAGE_AND_COMPONENTS = 2;
    
    /** 
     * Save the images and an image of each channel composing the rendered 
     * image.  Each channel rendered in grey scale mode.
     */
    static final int				IMAGE_AND_COMPONENTS_GREY = 3;
    
    /** Save the lens image. */
    static final int				LENS_IMAGE = 4;
    
    /** Save the lens image and the split channels. */
    static final int				LENS_IMAGE_AND_COMPONENTS = 5;
    
    
    /** Save the lens image. */
    static final int				LENS_IMAGE_AND_COMPONENTS_GREY = 6;
    
    /** The maximum number of save options. */
    private static final int		MAX = 6;
    
    /** The maximum number of save options if no lens. */
    private static final int		MAX_PARTIAL = 3;
    
    /** Brief description of the action performed by this widget. */
    private static final String     NOTE = "Save the currrent image in " +
    										"one of the following formats:" +
    										" TIFF, JPEG, PNG or BMP.";

    /** The tool tip of the <code>Preview</code> button. */
    private static final String		PREVIEW_TEXT = "Preview the image to save.";
    
    /** 
     * The size of the invisible components used to separate buttons
     * horizontally.
     */
    private static final Dimension	H_SPACER_SIZE = new Dimension(3, 10);
    
    /** Action ID for the {@link #cancelButton}. */
    private static final int		CANCEL = 0;
    
    /** Action ID for the {@link #saveButton}. */
    private static final int		SAVE = 1;
    
    /** Action ID for the {@link #newFolderButton}. */
    private static final int		NEWFOLDER = 2;
    
    /** Action ID for the {@link #previewButton}. */
    private static final int		PREVIEW = 3;
    
    /** Description of the type of images we can save. */
    private static final String[]       selections;
    
    /** Description of the type of images we can save. */
    private static final String[]       partialSelections;
    
    /** Description of the type of images we can save. */
    private static final String[]       basicSelections;
    
    /** Reference to the {@link ImgSaver}. */
    private ImgSaver                    model;
    
    /** The possible saving types. */
    private JComboBox                   savingTypes;

    /** Reference to the file chooser. */
    private ImgSaverFileChooser			chooser;
    
    /** Box to save the current directory as default. */
    private JCheckBox					settings;
    
    /** 
     * Replaces the <code>CancelButton</code> provided by the 
     * {@link JFileChooser} class. 
     */
    private JButton						cancelButton;
    
    /** 
     * Replaces the <code>New Folder</code> provided by the 
     * {@link JFileChooser} class. 
     */
    private JButton						newFolderButton;
    
    /** 
     * Replaces the <code>ApproveButton</code> provided by the 
     * {@link JFileChooser} class. 
     */
    private JButton						saveButton;
    
    /** Button to launch the preview window. */
    private JButton						previewButton;
    
    /** Check box indicating to the save each channel in a separated file. */
    private JCheckBox					separateFiles;
    
    /** Initializes the static fields. */
    static {
        selections = new String[MAX+1];
        selections[IMAGE] = "image";
        selections[GRID_IMAGE] = "split view";
        selections[IMAGE_AND_COMPONENTS] = "image and channels panorama";
        selections[IMAGE_AND_COMPONENTS_GREY] = 
        					"image and (grey scale) channels panorama";
        selections[LENS_IMAGE] = "lens' image";
        selections[LENS_IMAGE_AND_COMPONENTS] = 
        						"lens' image and channels panorama";
        selections[LENS_IMAGE_AND_COMPONENTS_GREY] = 
							"lens' image and (grey scale) channels panorama";
        partialSelections = new String[MAX_PARTIAL+1];
        partialSelections[IMAGE] = "image";
        partialSelections[GRID_IMAGE] = "split view";
        partialSelections[IMAGE_AND_COMPONENTS] = "image and channels panorama";
        partialSelections[IMAGE_AND_COMPONENTS_GREY] = 
        					"image and (grey scale) channels panorama";
        basicSelections = new String[1];
        basicSelections[IMAGE] = "image";
    }
    
    /** Initializes the component composing the display. */
    private void initComponents()
    {
    	switch (model.getSavingType()) {
    		case ImgSaver.BASIC:
				savingTypes = new JComboBox(basicSelections);
				break;
    		case ImgSaver.PARTIAL:
    			savingTypes = new JComboBox(partialSelections);
    			break;
	    	case ImgSaver.FULL:
			default:
				savingTypes = new JComboBox(selections);
		}
    	savingTypes.addActionListener(this);
    	separateFiles = new JCheckBox("Save each channel in a separate file.");
    	separateFiles.setSelected(true);
    	separateFiles.setVisible(false);
        chooser = new ImgSaverFileChooser(model, this);
        settings = new JCheckBox();
        settings.setText("Set the current directory as default.");
        settings.setSelected(true);
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this);
        cancelButton.setActionCommand(""+CANCEL);
    	saveButton = new JButton("Save as");
    	saveButton.setToolTipText(UIUtilities.formatToolTipText(SAVE_AS));
    	saveButton.addActionListener(this);
    	saveButton.setActionCommand(""+SAVE);
    	//saveButton.setEnabled(false);
    	previewButton = new JButton("Preview...");
    	previewButton.addActionListener(this);
    	previewButton.setActionCommand(""+PREVIEW);
    	previewButton.setToolTipText(
    			UIUtilities.formatToolTipText(PREVIEW_TEXT));
    	//previewButton.setEnabled(false);
    	newFolderButton = new JButton("New Folder...");
    	newFolderButton.addActionListener(this);
    	newFolderButton.setActionCommand(""+NEWFOLDER);
    	newFolderButton.setToolTipText(
    			UIUtilities.formatToolTipText("Create a new folder"));
    	model.getRootPane().setDefaultButton(saveButton);
    }
    
    /**
     * Builds the tool bar.
     * 
     * @return See above
     */
    private JPanel buildToolbar()
    {
    	JPanel bar = new JPanel();
    	bar.setBorder(null);
    	bar.add(cancelButton);
    	bar.add(Box.createRigidArea(H_SPACER_SIZE));
    	/*
    	if (!ImViewerAgent.hasOpenGLSupport()) {
    		bar.add(previewButton);
        	bar.add(Box.createRigidArea(H_SPACER_SIZE));
    	}
    	*/
    	bar.add(saveButton);
    	JPanel controls = new JPanel();
    	controls.setLayout(new BoxLayout(controls, BoxLayout.X_AXIS));
    	controls.add(Box.createRigidArea(new Dimension(20, 5)));
    	controls.add(newFolderButton);
    	JPanel p = UIUtilities.buildComponentPanelRight(bar);
        p.setOpaque(true);
        controls.add(p);
        return controls;
    }
    
    /**
     * Builds the UI component displaying the saving options.
     * 
     * @return See above.
     */
    private JPanel buildImagePanel()
    {
        //JPanel p = new JPanel();
        JPanel result = new JPanel();
        result.setLayout(new BoxLayout(result, BoxLayout.X_AXIS));
        JLabel l = new JLabel("Saving Types: ");
        result.add(l);
        result.add(UIUtilities.buildComponentPanel(savingTypes));
        //p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        //p.add(result);
        //p.add(UIUtilities.buildComponentPanelRight(settings));
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(UIUtilities.buildComponentPanelCenter(result));
        p.add(UIUtilities.buildComponentPanel(separateFiles));
        return UIUtilities.buildComponentPanelCenter(p);
    }
     
    /** Builds and lays out the UI. */
    private void buildGUI()
    {
    	JPanel controls = new JPanel();
    	controls.setLayout(new BorderLayout(0, 0));
    	controls.add(buildImagePanel(), BorderLayout.NORTH);
    	controls.add(buildToolbar(), BorderLayout.CENTER);
    	controls.add(UIUtilities.buildComponentPanel(settings), 
    							BorderLayout.SOUTH);
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout(0, 0));
        p.add(chooser, BorderLayout.CENTER);
        p.add(controls, BorderLayout.SOUTH);
        IconManager im = IconManager.getInstance();
        Container c = model.getContentPane();
        c.setLayout(new BorderLayout(0, 0));
        TitlePanel tp = new TitlePanel(ImgSaver.TITLE, NOTE, 
                                im.getIcon(IconManager.SAVE_48));
                    
        c.add(tp, BorderLayout.NORTH);
        c.add(p, BorderLayout.CENTER);
        if (JDialog.isDefaultLookAndFeelDecorated()) {
            boolean supportsWindowDecorations = 
            UIManager.getLookAndFeel().getSupportsWindowDecorations();
            if (supportsWindowDecorations)
                model.getRootPane().setWindowDecorationStyle(
                            JRootPane.FILE_CHOOSER_DIALOG);
        }
    }
    
    /** 
     * Sets the {@link #separateFiles} check box visible or not
     * depending on the selected saving types.
     */
    private void handleSavingTypesSelection()
	{
		switch (savingTypes.getSelectedIndex()) {
			case IMAGE_AND_COMPONENTS:
			case IMAGE_AND_COMPONENTS_GREY:
			case LENS_IMAGE_AND_COMPONENTS:
			case LENS_IMAGE_AND_COMPONENTS_GREY:
				separateFiles.setVisible(true);
				break;
	
			default:
				separateFiles.setVisible(false);
				break;
		}
	}
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     */
    ImgSaverUI(ImgSaver model)
    { 
        if (model == null) throw new IllegalArgumentException("No model.");
        this.model = model;
        initComponents();
        buildGUI();
    }
    
    /**
     * Returns the type of saving selected.
     * 
     * @return See above.
     */
    int getSavingType() { return savingTypes.getSelectedIndex(); }
    
    /**
    * Returns the pathname string of the current directory.
    *
    * @return  The string form of this abstract pathname.
    */
    String getCurrentDirectory()
    { 
    	return chooser.getCurrentDirectory().toString(); 
    }
    
    /**
     * Returns <code>true</code> if the default folder is set when
     * saving the image, <code>false</code> toherwise.
     * 
     * @return See above.
     */
    boolean isSetDefaultFolder() { return settings.isSelected(); }
    
    /**
     * Sets the <code>enabled</code> flag of not the <code>Save</code> and
     * <code>Preview</code> options.
     * 
     * @param b The value to set.
     */
	void setControlsEnabled(boolean b)
	{
		saveButton.setEnabled(b);
    	previewButton.setEnabled(b);
	}

	/**
	 * Returns <code>true</code> if each image composing the 
	 * display has to be save in a separated file, <code>false</code>
	 * otherwise.
	 * 
	 * @return See above.
	 */
	boolean isSaveImagesInSeparatedFiles()
	{
		return (separateFiles.isSelected() && separateFiles.isVisible());
	}
	
	/** 
	 * Reacts to click on the button replacing the ones usually provided by the
	 * file chooser.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() instanceof JComboBox) {
			handleSavingTypesSelection();
			return;
		}
		int index = Integer.parseInt(e.getActionCommand());
		switch (index) {
			case CANCEL:
				chooser.cancelSelection();
				break;
			case SAVE:
				chooser.approveSelection();
				break;
			case PREVIEW:
				chooser.previewSelection();
				break;
			case NEWFOLDER:
				CreateFolderDialog d = new CreateFolderDialog(model);
				d.addPropertyChangeListener(
						CreateFolderDialog.CREATE_FOLDER_PROPERTY, this);
				d.pack();
				UIUtilities.centerAndShow(model, d);
		}
	}

	/**
	 * Listens to the property fired by the folder dialog.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = (String) evt.getNewValue();
		chooser.createFolder(name);
	}

}
