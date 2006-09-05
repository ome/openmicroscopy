/*
 * org.openmicroscopy.shoola.agents.imviewer.util.ImgSaverUI
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.agents.imviewer.util;




//Java imports
import java.awt.BorderLayout;
import java.awt.Container;
import javax.swing.BoxLayout;
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
    
    /** Save the images. */
    static final int                    IMAGE = 0;
    
    /** 
     * Save the images and an image of each channel composing the rendered 
     * image. 
     */
    static final int                    IMAGE_AND_COMPONENTS = 1;
    
    
    /** Brief description of the action performed by this widget. */
    private static final String         NOTE = "Save the currrent image in " +
            "one of the following formats: TIFF, JPEG, PNG or BMP.";

    /** Description of the type of images we can save. */
    private static final String[]       selections;
    
    /** Reference to the {@link ImgSaver}. */
    private ImgSaver                    model;
    
    /** The possible saving types. */
    private JComboBox                   savingTypes;

    /** Initializes the static fields. */
    static {
        selections = new String[2];
        selections[IMAGE] = "image";
        selections[IMAGE_AND_COMPONENTS] = "image and its components";
    }
    
    /** Initializes the component composing the display. */
    private void initComponents()
    {
        savingTypes = new JComboBox(selections);
    }
    
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
        return p;
    }
    
    /** Builds and lays out the UI. */
    private void buildGUI()
    {
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout(0, 0));
        p.add(new ImgSaverFileChooser(model), BorderLayout.CENTER);
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
    
}
