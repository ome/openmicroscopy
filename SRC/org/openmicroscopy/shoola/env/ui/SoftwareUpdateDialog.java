/*
 * org.openmicroscopy.shoola.env.ui.SoftwareUpdateDialog
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

package org.openmicroscopy.shoola.env.ui;


//Java imports
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Basic modal dialog displaying the last version of the software.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Rev: $ $LastChangedDate: $)
 * </small>
 * @since OME2.2
 */
class SoftwareUpdateDialog
    extends JDialog
{

    /** The text displayed before the version and revision values. */
    private static final String     CLIENT_NAME = "Client";
    
    /** The client's version. */
    private static final String     CLIENT_VERSION = "3.0_M3 ";
   
    /** The text displayed before the revision date. */
    private static final String     REVISION_NAME = "Revision Date ";
    
    /** The text displayed before the release date. */
    private static final String     RELEASE_NAME = "Release Date ";
    
    //tmp
    public static String            REVISION_DATE = "2006-10-30";
    public static String            RELEASE_DATE = "2006-10-30";
    
    /** The close button. */
    private JButton closeButton;
    
    /** Closes and disposes. */
    private void close()
    {
        setVisible(false);
        dispose();
    }
    
    /** Initializes the components composing the display. */
    private void initComponents()
    {
        closeButton = new JButton("Close");
        closeButton.addActionListener(new ActionListener() {
        
            public void actionPerformed(ActionEvent e)
            {
                close();
            }
        
        });
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) { close(); }
        });
    }
    
    /** 
     * Returns the version and the revision.
     * 
     * @return See above.
     */
    private String getVersion()
    {
        String version = "$Rev$";
        String date = "$Date: $";
        Pattern p = Pattern.compile("\\d{1,9}");
        Matcher m = p.matcher(version);
        m.find();
        String s = CLIENT_VERSION;
        s += "(revision "+m.group()+")";
        return s;
    }
    
    /**
     * Builds the panel hosting the various values.
     * 
     * @return See above.
     */
    private JPanel buildContentPanel()
    {
        JPanel content = new JPanel();
        content.setLayout(new GridBagLayout());
        content.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(3, 3, 3, 3);
        JLabel name = UIUtilities.setTextFont(CLIENT_NAME);
        c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
        c.fill = GridBagConstraints.NONE;      //reset to default
        c.weightx = 0.0;  
        content.add(name, c);
        
        JLabel value = new JLabel(getVersion());
        name.setLabelFor(value);
        c.gridx = 1;
        c.gridwidth = GridBagConstraints.REMAINDER;     //end row
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        content.add(value, c);  
        c.gridx = 0;
        c.gridy = 1;
        name = UIUtilities.setTextFont(REVISION_NAME);
        c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
        c.fill = GridBagConstraints.NONE;      //reset to default
        c.weightx = 0.0;  
        content.add(name, c);
        value = new JLabel(REVISION_DATE);
        name.setLabelFor(value);
        c.gridx = 1;
        c.gridwidth = GridBagConstraints.REMAINDER;     //end row
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        content.add(value, c); 
        c.gridx = 0;
        c.gridy = 2;
        name = UIUtilities.setTextFont(RELEASE_NAME);
        c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
        c.fill = GridBagConstraints.NONE;      //reset to default
        c.weightx = 0.0;  
        content.add(name, c);
        value = new JLabel(RELEASE_DATE);
        name.setLabelFor(value);
        c.gridx = 1;
        c.gridwidth = GridBagConstraints.REMAINDER;     //end row
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        content.add(value, c); 
        return content;
    }
    
    /**
     * Builds the tool bar hosting the {@link #closeButton}.
     * 
     * @return See above;
     */
    private JPanel buildToolBar()
    {
        JPanel bar = new JPanel();
        bar.setBorder(null);;
        bar.add(closeButton);
        return bar;
    }
    
    /** Builds and lays out the UI. */
    private void buildGUI()
    {
        getContentPane().add(buildContentPanel(), BorderLayout.CENTER);
        JPanel p = UIUtilities.buildComponentPanelRight(buildToolBar());
        p.setBorder(BorderFactory.createEtchedBorder());
        p.setOpaque(true);
        getContentPane().add(p, BorderLayout.SOUTH);
    }
    
    /**
     * Creates a new intance.
     * 
     * @param owner The owner of the frame.
     */
    SoftwareUpdateDialog(JFrame owner)
    {
        super(owner);
        setModal(true);
        setResizable(false);
        initComponents();
        buildGUI();
        pack();
    }
    
}
