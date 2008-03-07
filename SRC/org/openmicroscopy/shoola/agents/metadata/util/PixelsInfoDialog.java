/*
 * org.openmicroscopy.shoola.agents.metadata.util.PixelsInfoDialog 
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
package org.openmicroscopy.shoola.agents.metadata.util;




//Java imports
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * 
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
public class PixelsInfoDialog 
	extends JDialog
{

	/** The default title of the window. */
    private static final String     TITLE = "Image's details";
    
    /** Brief description of the dialog purpose. */
    private static final String     TEXT = "Information about the pixels set.";
    
    /** Button to close the window. */
    private JButton         cancelButton;
    
    /** Sets the properties of this window. */
    private void setDialogProperties()
    {
        setModal(true);
        setResizable(true);
        setTitle(TITLE);
    }
    
    /**
     * Builds the panel hosting the information
     * 
     * @param details The information to display.
     * @return See above.
     */
    private JPanel buildContentPanel(Map details)
    {
        JPanel content = new JPanel();
        content.setLayout(new GridBagLayout());
        content.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(3, 3, 3, 3);
        Iterator i = details.keySet().iterator();
        JLabel label;
        JTextField area;
        String key, value;
        while (i.hasNext()) {
            ++c.gridy;
            c.gridx = 0;
            key = (String) i.next();
            value = (String) details.get(key);
            label = UIUtilities.setTextFont(key);
            c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
            //c.fill = GridBagConstraints.NONE;      //reset to default
            c.weightx = 0.0;  
            content.add(label, c);
            area = new JTextField(value);
            area.setEditable(false);
            area.setEnabled(false);
            label.setLabelFor(area);
            c.gridx = 1;
            c.gridwidth = GridBagConstraints.REMAINDER;     //end row
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1.0;
            content.add(area, c);  
        }
        return content;
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
    
    /** 
     * Builds and lays out the UI. 
     * 
     * @param details The pixels set details.
     */
    private void buildGUI(Map<String, String> details)
    {
        Container c = getContentPane();
        IconManager icons = IconManager.getInstance();
        TitlePanel tp = new TitlePanel(TITLE, TEXT, 
        							icons.getIcon(IconManager.INFO_48));
        c.add(tp, BorderLayout.NORTH);
        c.add(buildContentPanel(details), BorderLayout.CENTER);
        c.add(UIUtilities.buildComponentPanelRight(buildToolBar()),
                BorderLayout.SOUTH);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param owner		The owner of this frame.
     * @param details 	The details of the pixels set.
     */
    public PixelsInfoDialog(JFrame owner, Map<String, String> details)
    {
    	super(owner);
    	
    	setDialogProperties();
    	initComponents();
    	buildGUI(details);
    	pack();
    }
    
}
