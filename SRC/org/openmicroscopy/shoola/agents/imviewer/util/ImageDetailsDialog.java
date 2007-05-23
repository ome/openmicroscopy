/*
 * org.openmicroscopy.shoola.agents.imviewer.util.ImageDetailsDialog 
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
package org.openmicroscopy.shoola.agents.imviewer.util;



//Java imports
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.IconManager;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Modal dialog displaying the image's details. 
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
public class ImageDetailsDialog
	extends JDialog
{

	/** The default title of the window. */
    private static final String     TITLE = "Image's details";
    
    /** Brief description of the dialog purpose. */
    private static final String     TEXT = "Information about the pixels set.";
    
    /** Button to close the window. */
    private JButton         cancelButton;
    
    /** The component hosting the detials. */
    private JPanel			details;
    
    /** Sets the properties of this window. */
    private void setDialogProperties()
    {
        setModal(true);
        setResizable(true);
        setTitle(TITLE);
    }
    
    /** Initializes the component composing the display. */
    private void initComponents()
    {
        cancelButton = new JButton("Close");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { close(); }
        });
        getRootPane().setDefaultButton(cancelButton);
    }
    
    /** Closes and disposes. */
    private void close()
    {
        setVisible(false);
        dispose();
    }
    
    /**
     *  Buils and lays out the tool bar. 
     *  
     * @return See above.
     */
    private JPanel buildToolBar()
    {
        JPanel toolBar = new JPanel();
        //toolBar.setBorder(BorderFactory.createEtchedBorder());
        toolBar.add(cancelButton);
        return toolBar;
    }
    
    /** Builds and lays out the UI. */
    private void buildGUI()
    {
        Container c = getContentPane();
        IconManager icons = IconManager.getInstance();
        TitlePanel tp = new TitlePanel(TITLE, TEXT, 
        							icons.getIcon(IconManager.INFO_48));
        c.add(tp, BorderLayout.NORTH);
        c.add(UIUtilities.buildComponentPanel(details), BorderLayout.CENTER);
        c.add(UIUtilities.buildComponentPanelRight(buildToolBar()),
                BorderLayout.SOUTH);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param owner	The owner of this frame.
     * @param maxX	The number of pixels along the X-axis.
     * @param maxY	The number of pixels along the Y-axis.
     * @param sizeX	The size in microns of a pixel along the X-axis.
     * @param sizeY	The size in microns of a pixel along the Y-axis.
     * @param sizeZ	The size in microns of a pixel along the Z-axis.
     */
    public ImageDetailsDialog(JFrame owner, int maxX, int maxY, float sizeX, 
    						float sizeY, float sizeZ)
    {
    	super(owner);
    	details = new DetailsPane(maxX, maxY, sizeX, sizeY, sizeZ);
    	setDialogProperties();
    	initComponents();
    	buildGUI();
    	pack();
    }
    
}
