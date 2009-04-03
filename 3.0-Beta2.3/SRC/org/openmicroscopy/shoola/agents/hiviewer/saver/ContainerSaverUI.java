/*
 * org.openmicroscopy.shoola.agents.hiviewer.saver.ContainerSaverUI
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

package org.openmicroscopy.shoola.agents.hiviewer.saver;

//Java imports
import java.awt.BorderLayout;
import java.awt.Container;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.UIManager;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.IconManager;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * The UI delegate.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class ContainerSaverUI
{

    /** The title of the widget. */
    private static final String         TITLE = "Save the thumbnails";
    
    /** The summary's text. */
    private static final String         SUMMARY = "Save the selected " +
    									"thumbnails as a single image.";
    
    /** Reference to the file chooser. */
    private FileChooser chooser;
    
    /** Box to save the current directory as default. */
    private JCheckBox	settings; 
    
    /** 
     * Initializes the component composing the display. 
     * 
     * @param saver The component to host.
     */
    private void initComponents(ContainerSaver saver)
    {
        chooser = new FileChooser(saver);
        settings = new JCheckBox();
        settings.setText("Set the current directory as default.");
        settings.setSelected(true);
    }
    
    /** 
     * Builds and lays out the GUI. 
     * 
     * @param saver The component to host.
     */
    private void buildGUI(ContainerSaver saver)
    {
    	JPanel p = new JPanel();
        p.setLayout(new BorderLayout(0, 0));
        p.add(chooser, BorderLayout.CENTER);
        p.add(UIUtilities.buildComponentPanel(settings), BorderLayout.SOUTH);
        Container c = saver.getContentPane();
        IconManager im = IconManager.getInstance();
        TitlePanel tp = new TitlePanel(TITLE, SUMMARY, 
                                im.getIcon(IconManager.SAVE_AS_BIG));  
        c.setLayout(new BorderLayout(0, 0));
        c.add(tp, BorderLayout.NORTH);
        c.add(p, BorderLayout.CENTER);
        if (JDialog.isDefaultLookAndFeelDecorated()) {
            if (UIManager.getLookAndFeel().getSupportsWindowDecorations())
                saver.getRootPane().setWindowDecorationStyle(
                            JRootPane.FILE_CHOOSER_DIALOG);
        }
    }
    
    /**
     * Creates a new instance. 
     * 
     * @param saver The model. Mustn't be <code>null</code>.
     */
    ContainerSaverUI(ContainerSaver saver)
    {
        if (saver == null) throw new IllegalArgumentException("No model.");
        initComponents(saver);
        buildGUI(saver);
    }
    
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
