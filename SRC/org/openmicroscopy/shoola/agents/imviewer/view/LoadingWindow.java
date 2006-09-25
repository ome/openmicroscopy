/*
 * org.openmicroscopy.shoola.agents.imviewer.view.LoadingWindow
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

package org.openmicroscopy.shoola.agents.imviewer.view;




//Java imports
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.BoxLayout;
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
class LoadingWindow
    extends JDialog
{

    /** Bounds property indicating that the window is closed. */
    static final String         CLOSED_PROPERTY = "closed";
    
    /** The message displayed. */
    private static final String LOADING_TXT = "Loaded: ";
    
    /** The default size of the window. */
    private static final Dimension DEFAULT_SIZE = new Dimension(400, 50);
    
    /** Displays the status message. */
    private JLabel              status;
    
    /** The bar notifying the user for the data retrieval progress. */
    private JProgressBar        progressBar;
    
    /** Initializes the components. */
    private void initComponents()
    {
        progressBar = new JProgressBar();
        status = new JLabel("Loading...");
    }
    
    /** Builds and lays out the GUI. */
    private void buildGUI()
    {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(status);
        p.add(UIUtilities.buildComponentPanelRight(progressBar));
        getContentPane().add(p);
    }
    
    /** 
     * Attaches a window listener to the view to discard the model when 
     * the user closes the window.
     */
    private void attachWindowListeners()
    {
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e)
            { 
                if (isVisible())
                    firePropertyChange(CLOSED_PROPERTY, Boolean.FALSE, 
                        Boolean.TRUE);
            }
        });
    }
    
    /**
     * Creates a new instance.
     * 
     * @param owner The owner of this frame.
     */
    LoadingWindow(JFrame owner)
    {
        super(owner);
        setTitle("Loading image...");
        setModal(true);
        initComponents();
        buildGUI();
        attachWindowListeners();
        setSize(DEFAULT_SIZE);
        //pack();
        setVisible(false);
    }
    
    /** 
     * Sets the status message.
     * 
     * @param s The message to display.
     */
    void setStatus(String s) { status.setText(LOADING_TXT+s); }
    
    /**
     * Sets the value of the progress bar.
     * 
     * @param perc  The value to set.
     */
    void setProgress(int perc)
    {
        if (perc < 0) progressBar.setIndeterminate(true);
        else {
            //progressBar.setIndeterminate(false);
            progressBar.setValue(perc);
        }
    }
    
}
