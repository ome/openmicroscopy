/*
 * org.openmicroscopy.shoola.agents.imviewer.util.PlaneSlicingUI
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
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * 
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
class PlaneSlicingUI
    extends JPanel
{
    
    /** Description associated to the static pane selection. */
    private static final String         TEXT_STATIC = 
                                        "<html>Highlights a range,<br>" +
                                         "preserves others (cf. (2)).</html>";

    /** Description associated to the non static pane selection. */
    private static final String         TEXT_NOT_STATIC = 
                                        "<html>Highlights a range,<br>" +
                                        "reduces others to a constant level" +
                                        "(cf. (1)).</html>";

    /** Vertical gap between UI components. */
    private static final Dimension      VBOX = new Dimension(0, 10);
    
    private static final Dimension      DIM = new Dimension(
                                            PlaneSlicingPane.WIDTH, 
                                            PlaneSlicingPane.HEIGHT);

    private static final Dimension      DIM_ALL = new Dimension(
                                            2*PlaneSlicingPane.WIDTH,
                                            PlaneSlicingPane.HEIGHT);

    /** The bit-planes selection. */
    private static final String[]       BIT_PLANES;
    
    static {
        BIT_PLANES = new String[7];
        BIT_PLANES[PlaneSlicingDialog.B_ONE] = "1-bit plane";
        BIT_PLANES[PlaneSlicingDialog.B_TWO] = "2-bit plane";
        BIT_PLANES[PlaneSlicingDialog.B_THREE] = "3-bit plane";
        BIT_PLANES[PlaneSlicingDialog.B_FOUR] = "4-bit plane";
        BIT_PLANES[PlaneSlicingDialog.B_FIVE] = "5-bit plane";
        BIT_PLANES[PlaneSlicingDialog.B_SIX] = "6-bit plane";
        BIT_PLANES[PlaneSlicingDialog.B_SEVEN] = "7-bit plane";
    }
    
    /** Reference to the non static {@link PlaneSlicingPane pane}. */
    private PlaneSlicingPane        pane;
    
    /** Reference to the static {@link PlaneSlicingPane pane}. */
    private PlaneSlicingPaneStatic  paneStatic;
    
    /** Reference to the model. */
    private PlaneSlicingDialog      model;
    
    /**
     * 
     * @param x
     * @param r
     * @param b
     * @return See above.
     */
    private int convertRealIntoGraphics(int x, int r, int b)
    {
        double a = (double) PlaneSlicingPane.square/r;
        return (int) (a*(x-b));
    }
    
    /** Initializes the components composing the display. */
    private void initComponents()
    {
        pane = new PlaneSlicingPane(model);
        paneStatic = new PlaneSlicingPaneStatic(model);
        int yStart, yEnd; 
        yStart = PlaneSlicingPane.topBorder+
                    convertRealIntoGraphics(model.getLowerLimit(), 
                            model.cdStart-model.cdEnd, model.cdEnd);
        yEnd = PlaneSlicingPane.topBorder+
                convertRealIntoGraphics(model.getUpperLimit(),
                        model.cdStart-model.cdEnd, model.cdEnd);
        pane.initialize(yStart, yEnd);
    }
    
    /**
     * Helper method to create the component hosting the controls.
     * 
     * @return See above.
     */
    private JPanel buildControlsPane()
    {
        JPanel controls = new JPanel();
        GridBagConstraints c = new GridBagConstraints();
        controls.setLayout(new GridBagLayout());
        c.anchor = GridBagConstraints.EAST;
        c.fill = GridBagConstraints.HORIZONTAL;
        ButtonGroup group = new ButtonGroup();
        JRadioButton button = new JRadioButton(TEXT_STATIC);
        button.setSelected(model.isContextConstant());
        group.add(button);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                model.setPaneIndex(PlaneSlicingDialog.STATIC);
            }
        });
        
        controls.add(button, c); //add to panel
        button = new JRadioButton(TEXT_NOT_STATIC);
        button.setSelected(model.isContextConstant());
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                model.setPaneIndex(PlaneSlicingDialog.NON_STATIC);
            }
        });
        group.add(button);
        c.gridy = 1;
        controls.add(button, c); //add to panel
        JComboBox bitPlanes = new JComboBox(BIT_PLANES);
        bitPlanes.setSelectedIndex(model.getBitPlaneIndex());
        bitPlanes.addActionListener(new ActionListener() {
        
            public void actionPerformed(ActionEvent e)
            {
                JComboBox box = (JComboBox) e.getSource();
                model.setBitPlaneIndex(box.getSelectedIndex());
            }
        
        });
        //Combox to panel
        JPanel p = new JPanel();
        p.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel label = new JLabel(" Select a plane: ");
        p.add(label);
        p.add(UIUtilities.buildComponentPanel(bitPlanes));  
        c.gridy = 2;
        controls.add(p, c); //add to panel
        return UIUtilities.buildComponentPanel(controls);
    }
    
    private JPanel buildGraphicsPane()
    {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.setPreferredSize(DIM_ALL);
        p.setSize(DIM_ALL);
        pane.setPreferredSize(DIM);
        pane.setSize(DIM);
        p.add(pane);
        paneStatic.setPreferredSize(DIM);
        paneStatic.setSize(DIM);
        p.add(paneStatic);
        return p;
    }
    
    /** Builds and lays out the GUI. */
    private void buildGUI()
    {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(buildGraphicsPane());
        add(Box.createRigidArea(VBOX));
        add(buildControlsPane());
    }
    
    /**
     * Creates a new intance.
     * 
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     */
    PlaneSlicingUI(PlaneSlicingDialog model)
    {
        if (model == null) throw new NullPointerException("No model.");
        this.model = model;
        initComponents();
        buildGUI();
    }
    
}
