/*
 * org.openmicroscopy.shoola.util.ui.LoadingWindow
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
package org.openmicroscopy.shoola.util.ui;


//Java imports
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.WindowConstants;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * A modal dialog brings up on screen when the metadata and rendering settings
 * associated to the image to visualize are loaded.
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
public class LoadingWindow
    extends JDialog
{

    /** Bounds property indicating that the window is closed. */
    public static final String		CLOSED_PROPERTY = "closed";
    
    /** Bounds property indicating that the window is closed. */
    public static final String		CANCEL_LOADING_PROPERTY = 
    	"cancelLoadingProperty";
    
    /** The message displayed. */
    public static final String 		LOADING_TXT = "Loaded: ";
    
    /** The title of the window. */
    private static final String 	TITLE = "Loading window";

    /** The default size of the window. */
    private static final Dimension	DEFAULT_SIZE = new Dimension(400, 60);
    
    /** The option set when the dialog has been closed.*/
    private static final int DISCARD = 1;
    
    /** Displays the status message. */
    private JLabel              status;
    
    /** The bar notifying the user for the data retrieval progress. */
    private JProgressBar        progressBar;
    
    /** Cancels the loading of the image.*/
    private JButton				cancelButton;
    
    /** The option set when the dialog is closed.*/
    private int option;
    
    /** Initializes the components. */
    private void initComponents()
    {
        progressBar = new JProgressBar();
        status = new JLabel("Loading...");
        progressBar.setIndeterminate(true);
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				firePropertyChange(CANCEL_LOADING_PROPERTY,
						Boolean.valueOf(false), Boolean.valueOf(true));
			}
		});
        getRootPane().setDefaultButton(cancelButton);
    }
    
    /** Builds and lays out the GUI. */
    private void buildGUI()
    {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(status);
        p.add(progressBar);
        p.add(UIUtilities.buildComponentPanelRight(cancelButton));
        Container c = getContentPane();
        c.add(p, BorderLayout.CENTER);
    }
    
    /** 
     * Attaches a window listener to the view to discard the model when 
     * the user closes the window.
     */
    private void attachWindowListeners()
    {
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        /*
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e)
            { 
                if (isVisible()) {
                    firePropertyChange(CANCEL_LOADING_PROPERTY, 
                    		Boolean.valueOf(false), Boolean.valueOf(true));
                }   
            }
        });
        */
    }
    
    /** Sets the properties of the component. */
    private void setWindowProperties()
    {
        setTitle(TITLE);
        setModal(true);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param owner The owner of this frame.
     */
    public LoadingWindow(JFrame owner)
    {
        super(owner);
        setWindowProperties();
        initComponents();
        buildGUI();
        attachWindowListeners();
        setSize(DEFAULT_SIZE);
        //pack();
        setVisible(false);
    }
    
    /** Disposes of the dialog.*/
    public void close()
    {
    	option = DISCARD;
    	setVisible(false);
    	dispose();
    }
    
    /** 
     * Sets the status message.
     * 
     * @param s The message to display.
     */
    public void setStatus(String s) { status.setText(s); }

    /**
     * Sets the value of the progress bar.
     * 
     * @param perc  The value to set.
     */
    public void setProgress(int perc)
    {
        if (perc < 0) progressBar.setIndeterminate(true);
        else {
            //progressBar.setIndeterminate(false);
            progressBar.setValue(perc);
        }
        progressBar.setIndeterminate(true);
    }
    
    /**
     * Overridden to set the visibility depending on the option
     */
    public void setVisible(boolean visible)
    {
    	if (visible && option == DISCARD) visible = false;
    	super.setVisible(visible);
    }

}
