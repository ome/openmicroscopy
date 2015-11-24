/*
 * org.openmicroscopy.shoola.agents.imviewer.util.UnitBarSizeDialog
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
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
import omero.model.LengthI;
import omero.model.enums.UnitsLength;

/** 
 * Sets the value of the scale bar.
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
    
    /** The unit of the scalebar */
    private UnitsLength unit;
    
    /** Fires a property change if the value entered is a valid number. */
    private void handleSelection()
    {
        try {
            final Double val = Double.valueOf(label.getText());
            if (val > 0) {
                firePropertyChange(UNIT_BAR_VALUE_PROPERTY, Double.valueOf(0), 
                                   val);
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
        p.add(new JLabel("Value (in "+LengthI.lookupSymbol(unit)+"): "));
        p.add(label);
        getContentPane().add(UIUtilities.buildComponentPanel(p));
    }
    
    /**
     * Creates a new instance.
     * 
     * @param parent The window's parent.
     * @param unit The unit of the scalebar
     */
    public UnitBarSizeDialog(JFrame parent, UnitsLength unit)
    {
        super(parent);
        this.unit = unit;
        setProperties();
        buildGUI();
        pack();
    }
    
}
