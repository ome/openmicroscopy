/*
 * org.openmicroscopy.shoola.agents.roi.pane.SlidersPane
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

package org.openmicroscopy.shoola.agents.roi.pane;


//Java imports
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.roi.ROIAgt;
import org.openmicroscopy.shoola.agents.roi.defs.ROISettings;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.util.ui.GraphicSlider;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">
 *                  a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class SlidersPane
    extends JPanel
{
    
    private static final Dimension  VBOX = new Dimension(0, 10);
    
    private GraphicSlider           sliderT, sliderZ;
    
    /** TextField with the start (resp. end) value. */
    private JTextField              startT, endT;
    
    /** TextField with the start (resp. end) value. */
    private JTextField              startZ, endZ;
    
    private JCheckBox               zSelection, tSelection, ztSelection;
    
    /** Width of a caracter. */
    private int                     txtWidth;
    
    private SlidersPaneMng          manager;
    
    public SlidersPane(Registry reg, int maxT, int maxZ, ROISettings settings)
    {
        manager = new SlidersPaneMng(this, reg, maxT, maxZ, settings);
        initTxtWidth();
        initFields(maxT, maxZ, settings);
        initComponents(maxT, maxZ, settings);
        manager.attachListeners();
        buildGUI(maxT, maxZ);
    }
    
    GraphicSlider getSliderZ() { return sliderZ; }
    
    GraphicSlider getSliderT() { return sliderT; }
    
    JTextField getStartT() { return startT; }

    JTextField getEndT() { return endT; }
    
    JTextField getStartZ() { return startZ; }

    JTextField getEndZ() { return endZ; }
    
    JCheckBox getZSelection() { return zSelection; }
    
    JCheckBox getTSelection() { return tSelection; }
    
    JCheckBox getZTSelection() { return ztSelection; }
    
    /** Initializes the slider. */
    private void initComponents(int maxT, int maxZ, ROISettings settings)
    {
        sliderT = new GraphicSlider(maxT, settings.getStartT(), 
                                    settings.getEndT());
        sliderT.setBackground(getBackground());
        sliderZ = new GraphicSlider(maxZ, settings.getStartZ(),
                                    settings.getEndZ());
        sliderZ.setBackground(getBackground());
        sliderZ.setKnobColor(new Color(0, 255, 0, 90));
        
        zSelection = new JCheckBox(" Z ");
        tSelection = new JCheckBox(" T ");
        ztSelection = new JCheckBox(" Current plane ");
        
        tSelection.setSelected(settings.isTSelected());
        zSelection.setSelected(settings.isZSelected());
        ztSelection.setSelected(settings.isZTSelected());
        if (!settings.isZTSelected()) { // To be on the save side.
            if (maxZ != 0 && settings.isZSelected()) 
                sliderZ.attachMouseListeners();
            if (maxT != 0 && settings.isZSelected()) 
                sliderT.attachMouseListeners();
        }
    }
    
    /** Initializes the text Fields displaying the current values. */
    private void initFields(int maxT, int maxZ, ROISettings settings)
    {
        startT = new JTextField(""+settings.getStartT(), 
                                    (""+maxT).length());
        startT.setForeground(ROIAgt.STEELBLUE);
        startT.setToolTipText(
                UIUtilities.formatToolTipText("Enter the starting value."));
        endT = new JTextField(""+settings.getEndT(), (""+maxT).length());
        endT.setForeground(ROIAgt.STEELBLUE);
        endT.setToolTipText(
                UIUtilities.formatToolTipText("Enter the end value."));
        
        startZ = new JTextField(""+settings.getStartZ(), 
                                    (""+maxZ).length());
        startZ.setForeground(ROIAgt.STEELBLUE);
        startZ.setToolTipText(
                UIUtilities.formatToolTipText("Enter the starting value."));
        endZ = new JTextField(""+settings.getEndZ(), (""+maxZ).length());
        endZ.setForeground(ROIAgt.STEELBLUE);
        endZ.setToolTipText(
                UIUtilities.formatToolTipText("Enter the end value."));
        if (maxZ != 0) {
            startZ.setEnabled(settings.isZSelected());
            endZ.setEnabled(settings.isZSelected());
        }
        if (maxT != 0) {
            startT.setEnabled(settings.isTSelected());
            endT.setEnabled(settings.isTSelected());
        }  
    }
    
    private JPanel buildControlsMoviePanel(int length, JTextField start, 
                                            JTextField end)
    {
        JPanel p = new JPanel();
        JLabel l = new JLabel(" Start ");
        int x = length*txtWidth;
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        p.setLayout(gridbag);
        c.weightx = 0;        
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        gridbag.setConstraints(l, c);
        p.add(l);
        c.gridx = 1;
        c.ipadx = x;
        c.weightx = 0.5;
        JPanel msp = UIUtilities.buildComponentPanel(start);
        gridbag.setConstraints(msp, c);
        p.add(msp);
        c.gridx = 2;
        c.ipadx = 0;
        c.weightx = 0;
        l = new JLabel(" End ");
        gridbag.setConstraints(l, c);
        p.add(l);
        c.gridx = 3;
        c.ipadx = x;
        c.weightx = 0.5;
        msp = UIUtilities.buildComponentPanel(end);
        gridbag.setConstraints(msp, c);
        p.add(msp);
        return p; 
    }
    
    /** Initializes the width of the text. */
    private void initTxtWidth()
    {
        FontMetrics metrics = getFontMetrics(getFont());
        txtWidth = metrics.charWidth('m');
    }
    
    private JPanel buildGroupPanel(GraphicSlider slider, JCheckBox box,
                                    JPanel controls)
    {
        JPanel p = new JPanel(), group = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        if (slider != null) p.add(slider.getUI());
        if (controls != null) p.add(controls);
        group.setLayout(new BoxLayout(group, BoxLayout.X_AXIS));
        group.add(box);
        group.add(p);
        return group;
    }
    
    private JPanel buildBoxPanel(JCheckBox box)
    {
        JPanel p = new JPanel();
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        p.setLayout(gridbag);
        c.weightx = 0.5;        
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        gridbag.setConstraints(box, c);
        p.add(box);
        return p;
    }
    
    /** Build and lay out the GUI. */
    private void buildGUI(int maxT, int maxZ)
    {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(UIUtilities.buildComponentPanel(
                UIUtilities.setTextFont(" Analysis context")));
        JPanel p = buildGroupPanel(sliderZ, zSelection, 
                    buildControlsMoviePanel((""+maxZ).length(), startZ, endZ));
        add(p);
        p = buildGroupPanel(sliderT, tSelection, 
                buildControlsMoviePanel((""+maxT).length(), startT, endT));
        add(p);
        add(Box.createRigidArea(VBOX));
        p = buildBoxPanel(ztSelection);
        add(p);
        add(new JSeparator());
    }
    
}



