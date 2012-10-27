/*
 * org.openmicroscopy.shoola.env.ui.SoftwareUpdateDialog
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

package org.openmicroscopy.shoola.env.ui;


//Java imports
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.WindowConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

//Third-party libraries
import info.clearthought.layout.TableLayout; 

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
 * (<b>Internal version:</b> $Rev$ $LastChangedDate$)
 * </small>
 * @since OME2.2
 */
class SoftwareUpdateDialog
    extends JDialog
{

	/** Bounds property indicating to launch the web site. */
	static final String		OPEN_URL_PROPERTY = "openURL";
	
    /** The close button. */
    private JButton closeButton;

    /** Sets the properties of the window. */
    private void setWindowProperties()
    {
        setModal(true);
        setResizable(false);
    }
    
    /** Initializes the components composing the display. */
    private void initComponents()
    {
        closeButton = new JButton("OK");
        closeButton.addActionListener(new ActionListener() {
        
            public void actionPerformed(ActionEvent e) { close(); }
        
        });
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) { close(); }
        });
        getRootPane().setDefaultButton(closeButton);
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
    
    /**
     * Builds a panel hosting the passed message.
     * 
     * @param aboutMessage The message to display
     * @return See above.
     */
    private JPanel buildAbout(String aboutMessage)
    {
    	JPanel content = new JPanel();
    	double[][] size = {{TableLayout.PREFERRED, 5, TableLayout.FILL}, 
    						{TableLayout.FILL}}; 
        content.setLayout(new TableLayout(size));
        Icon icon = IconManager.getLogoAbout();
        JEditorPane p = UIUtilities.buildTextEditorPane(aboutMessage);
        p.setEditable(false);
        p.setOpaque(false);
        p.addHyperlinkListener(new HyperlinkListener() {
        	public void hyperlinkUpdate(HyperlinkEvent e) {
        		if (HyperlinkEvent.EventType.ACTIVATED.equals(
        				e.getEventType())) {
        			String url = e.getURL().toString();
    				firePropertyChange(OPEN_URL_PROPERTY, null, url);
        		}
        	}
        });
        p.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        p.setBackground(Color.WHITE);
        p.setOpaque(true);
        JLabel l = new JLabel(icon);
        l.setBackground(Color.WHITE);
        content.setBackground(Color.WHITE);
        content.add(l, "0, 0");
        content.add(p, "1, 0, 2, 0");
       
        JPanel c = new JPanel();
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        c.add(content);
        c.add(new JSeparator());
        c.setOpaque(true);
    	return c;
    }
    
    /** 
     * Builds and lays out the UI. 
     * 
     * @param aboutMessage	The message retrieved from the About file.
     */
    private void buildGUI(String aboutMessage)
    {
    	Container c = getContentPane();
    	c.add(buildAbout(aboutMessage), BorderLayout.NORTH);
        JPanel p = UIUtilities.buildComponentPanelRight(buildToolBar());
        p.setOpaque(true);
        c.add(p, BorderLayout.SOUTH);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param owner 		The owner of the frame.
     * @param aboutMessage	The message retrieved from the About file.
     */
    SoftwareUpdateDialog(JFrame owner, String aboutMessage)
    {
        super(owner);
        setWindowProperties();
        initComponents();
        buildGUI(aboutMessage);
        pack();
    }
    
    /** Closes and disposes. */
    void close()
    {
        setVisible(false);
        dispose();
    }
    
}
