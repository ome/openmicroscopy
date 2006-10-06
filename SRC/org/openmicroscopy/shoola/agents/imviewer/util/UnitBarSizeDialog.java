/*
 * org.openmicroscopy.shoola.agents.imviewer.util.UnitBarSizeDialog
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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;



//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.ImViewerAgent;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
public class UnitBarSizeDialog
    extends JDialog
{
    
    /** Bounds property indicating that the a new value is entered. */
    public final static String UNIT_BAR_VALUE_PROPERTY = "unitBarValue";
    
    /** The dimension of the text area. */
    private static final Dimension DIM = new Dimension(50, 20);
    
    /** The label used to enter the value. */
    private JTextField label;
    
    /** Fires a property change if the value entered is a valid number. */
    private void handleSelection()
    {
        try {
            double val = Double.parseDouble(label.getText());
            if (val > 0) {
                firePropertyChange(UNIT_BAR_VALUE_PROPERTY, new Double(0), 
                                    new Double(val));
                setVisible(false);
                dispose();
            } else {
                UserNotifier un = ImViewerAgent.getRegistry().getUserNotifier();
                un.notifyInfo("Invalid value", "Please enter a positive value");
            }
        } catch(NumberFormatException nfe) {}
        
        
    }
    
    /** Sets the properties of this window. */
    private void setProperties()
    {
        setModal(true);
        setResizable(false);
    }
    
    /** Builds and lays out the UI. */
    private void buildGUI()
    {
        label = new JTextField();
        label.setEditable(true);
        label.setEnabled(true);
        label.setPreferredSize(DIM);
        label.addActionListener(new ActionListener() {
        
            public void actionPerformed(ActionEvent e) { handleSelection(); }
        
        });
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(new JLabel("Value: "));
        p.add(label);
        getContentPane().add(UIUtilities.buildComponentPanel(p));
    }
    
    /**
     * Creates a new instance.
     * 
     * @param parent The window's parent.
     */
    public UnitBarSizeDialog(JFrame parent)
    {
        super(parent);
        setProperties();
        buildGUI();
        pack();
    }
    
}
