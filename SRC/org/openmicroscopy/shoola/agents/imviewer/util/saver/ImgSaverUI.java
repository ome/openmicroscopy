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
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.UIManager;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.IconManager;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

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
{
    
    /** Save the main image. */
    static final int                   IMAGE = 0;
    
    /** Save the grid image. */
    static final int                    GRID_IMAGE = 1;
    
    /** 
     * Save the images and an image of each channel composing the rendered 
     * image. 
     */
    static final int                    IMAGE_AND_COMPONENTS = 2;
    
    /** 
     * Save the images and an image of each channel composing the rendered 
     * image.  Each channel rendered in grey scale mode.
     */
    static final int                    IMAGE_AND_COMPONENTS_GREY = 3;
    
    /** Save the lens image. */
    static final int					LENS_IMAGE = 4;
    
    /** Save the lens image and the split channels. */
    static final int					LENS_IMAGE_AND_COMPONENTS = 5;
    
    
    /** Save the lens image. */
    static final int					LENS_IMAGE_AND_COMPONENTS_GREY = 6;
    
    /** The maximum number of save options. */
    private static final int			MAX = 6;
    
    /** The maximum number of save options if no lens. */
    private static final int			MAX_PARTIAL = 3;
    
    /** Brief description of the action performed by this widget. */
    private static final String         NOTE = "Save the currrent image in " +
            "one of the following formats: TIFF, JPEG, PNG or BMP.";

    /** Description of the type of images we can save. */
    private static final String[]       selections;
    
    /** Description of the type of images we can save. */
    private static final String[]       partialSelections;
    
    /** Reference to the {@link ImgSaver}. */
    private ImgSaver                    model;
    
    /** The possible saving types. */
    private JComboBox                   savingTypes;

    /** Reference to the file chooser. */
    private ImgSaverFileChooser			chooser;
    
    /** Box to save the current directory as default. */
    private JCheckBox					settings;
    
    /** Initializes the static fields. */
    static {
        selections = new String[MAX+1];
        selections[IMAGE] = "image";
        selections[GRID_IMAGE] = "grid view";
        selections[IMAGE_AND_COMPONENTS] = "image and split channels";
        selections[IMAGE_AND_COMPONENTS_GREY] = 
        					"image and split channels in grey";
        selections[LENS_IMAGE] = "lens' image";
        selections[LENS_IMAGE_AND_COMPONENTS] = 
        						"lens' image and split channels";
        selections[LENS_IMAGE_AND_COMPONENTS_GREY] = 
								"lens' image and split channels  in grey";
        partialSelections = new String[MAX_PARTIAL+1];
        partialSelections[IMAGE] = "image";
        partialSelections[GRID_IMAGE] = "grid view";
        partialSelections[IMAGE_AND_COMPONENTS] = "image and split channels";
        partialSelections[IMAGE_AND_COMPONENTS_GREY] = 
        					"image and split channels in grey";
    }
    
    /** Initializes the component composing the display. */
    private void initComponents()
    {
    	switch (model.getSavingType()) {
    		case ImgSaver.PARTIAL:
    			savingTypes = new JComboBox(selections);
			break;
	    	case ImgSaver.FULL:
			default:
				savingTypes = new JComboBox(selections);
				break;
		}
    	
        chooser = new ImgSaverFileChooser(model);
        settings = new JCheckBox();
        settings.setText("Set the current directory as default.");
        settings.setSelected(true);
    }
    
    /**
     * Builds the UI component displaying the saving options.
     * 
     * @return See above.
     */
    private JPanel buildSelectionPane()
    {
        JPanel p = new JPanel();
        JPanel result = new JPanel();
        result.setLayout(new BoxLayout(result, BoxLayout.X_AXIS));
        JLabel l = new JLabel("Images: ");
        result.add(l);
        result.add(UIUtilities.buildComponentPanel(savingTypes));
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(result);
        p.add(UIUtilities.buildComponentPanelRight(settings));
        return p;
    }
    
    /** Builds and lays out the UI. */
    private void buildGUI()
    {
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout(0, 0));
        p.add(chooser, BorderLayout.CENTER);
        p.add(buildSelectionPane(), BorderLayout.SOUTH);
        IconManager im = IconManager.getInstance();
        Container c = model.getContentPane();
        c.setLayout(new BorderLayout(0, 0));
        TitlePanel tp = new TitlePanel(ImgSaver.TITLE, NOTE, 
                                im.getIcon(IconManager.SAVE_BIG));
                    
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
    
}
