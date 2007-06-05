/*
 * org.openmicroscopy.shoola.agents.imviewer.util.InfoDialog
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

package org.openmicroscopy.shoola.agents.imviewer.util;




//Java imports
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.IconManager;
import org.openmicroscopy.shoola.env.data.model.ChannelMetadata;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Modal dialog displaying the channel information. 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
public class InfoDialog
    extends JDialog
{

    /** Bound property indicating that an update of the channel information. */ 
    public static final String      UPDATE_PROPERTY = "update";
    
    /** The default title of the window. */
    private static final String     TITLE = "Channel Info";
    
    /** Brief description of the dialog purpose. */
    private static final String     TEXT = "Edit the selected channel";
    
    /** The horizontal space between the buttons. */
    private static final Dimension  H_BOX = new Dimension(10, 0);
    
    /** Button to save the changes. */
    private JButton         finishButton;
    
    /** Button to close the window without saving. */
    private JButton         cancelButton;
    
    /** The metadata to display. */
    private ChannelMetadata metadata;
    
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
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { close(); }
        });
        finishButton = new JButton("Save");
        finishButton.setEnabled(false);
        finishButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { save(); }
        });
    }
    
    /** Closes and disposes. */
    private void close()
    {
        setVisible(false);
        dispose();
    }
    
    /** Saves the changes. */
    private void save()
    {
        firePropertyChange(UPDATE_PROPERTY, null, metadata);
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
        toolBar.add(finishButton);
        toolBar.add(Box.createRigidArea(H_BOX));
        toolBar.add(cancelButton);
        return toolBar;
    }
    
    /**
     * Builds and lays out the body displaying the channel info.
     * 
     * @return See above.
     */
    private JPanel buildBody()
    {
        Map details = EditorUtil.transformChannelData(metadata);
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
            c.fill = GridBagConstraints.NONE;      //reset to default
            c.weightx = 0.0;  
            content.add(label, c);
            area = new JTextField(value);
            area.setEditable(false);
            label.setLabelFor(area);
            c.gridx = 1;
            c.gridwidth = GridBagConstraints.REMAINDER;     //end row
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1.0;
            content.add(area, c);  
        }
        return content;
    }
    
    /** Builds and lays out the UI. */
    private void buildGUI()
    {
        Container c = getContentPane();
        IconManager icons = IconManager.getInstance();
        TitlePanel tp = new TitlePanel(TITLE, TEXT, 
        							icons.getIcon(IconManager.INFO_48));
        c.add(tp, BorderLayout.NORTH);
        c.add(buildBody(), BorderLayout.CENTER);
        c.add(UIUtilities.buildComponentPanelRight(buildToolBar()),
                BorderLayout.SOUTH);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param owner     The owner of the frame.
     * @param metadata  The metadata to display. Mustn't be <code>null</code>.
     */
    public InfoDialog(JFrame owner, ChannelMetadata metadata)
    {
        super(owner);
        if (metadata == null) throw new IllegalArgumentException("No metadata");
        this.metadata = metadata;
        setDialogProperties();
        initComponents();
        buildGUI();
        pack();
    }
    
}
