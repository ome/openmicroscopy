/*
 * org.openmicroscopy.shoola.agents.imviewer.rnd.DomainPane
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

package org.openmicroscopy.shoola.agents.imviewer.rnd;




//Java imports
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.IconManager;
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
class DomainPane
    extends ControlPane
    implements ChangeListener
{
    
    /** Box to select the family used in the mapping process. */
    private JComboBox       familyBox;
    
    /** Slider to select a curve in the family. */
    private JSlider         gammaSlider;
    
    /** Slider to select the bit resolution of the rendered image. */
    private JSlider         bitDepthSlider;
    
    /** Field displaying the <code>Gamma</code> value. */
    private JTextField      gammaLabel;
    
    /** Field displaying the <code>Bit Depth</code> value. */
    private JTextField      bitDepthLabel;
    
    /** Box to select the mapping algorithm. */
    private JCheckBox       noiseReduction;
    
    /** Initializes the components composing the display. */
    private void initComponents()
    {
        familyBox = new JComboBox(model.getFamilies().toArray());
        String family = model.getFamily();
        familyBox.setSelectedItem(family);
        bitDepthSlider = new JSlider();
        gammaSlider = new JSlider();
        gammaSlider.setEnabled(!(family == RendererModel.LINEAR || 
                family == RendererModel.LOGARITHMIC));
        gammaLabel = new JTextField();
        gammaLabel.setEnabled(false);
        gammaLabel.setEditable(false);
        bitDepthLabel = new JTextField();
        bitDepthLabel.setEnabled(false);
        bitDepthLabel.setEditable(false);
    }
    
    private JPanel buildSliderPane(JSlider slider, JTextField field)
    {
        JPanel p = new JPanel();
        p.add(slider);
        p.add(field);
        return UIUtilities.buildComponentPanel(p);
    }
    
    /**
     * Builds the pane hosting the main rendering controls.
     * 
     * @return See above.
     */
    private JPanel buildControlsPane()
    {
        JPanel p = new JPanel();
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        p.setLayout(gridbag);
        c.ipadx = 10;
        c.weightx = 0.5;
        c.fill = GridBagConstraints.HORIZONTAL;
        JLabel label = new JLabel("Map");
        p.add(label, c);
        c.gridx = 1;
        p.add(UIUtilities.buildComponentPanel(familyBox), c);
        label = new JLabel("Gamma");
        c.gridx = 0;
        c.gridy = 1;
        p.add(label, c);
        c.gridx = 1;
        p.add(buildSliderPane(gammaSlider, gammaLabel), c);
        label = new JLabel("Bit Depth");
        c.gridx = 0;
        c.gridy = 2;
        p.add(label, c);
        c.gridx = 1;
        p.add(buildSliderPane(bitDepthSlider, bitDepthLabel), c);
        return p;
    }
    
    /** Builds and lays out the UI. */
    private void buildGUI()
    {
        JPanel p = new JPanel();
        
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model         Reference to the Model.
     *                      Mustn't be <code>null</code>.
     * @param controller    Reference to the Control.
     *                      Mustn't be <code>null</code>.
     */
    DomainPane(RendererModel model, RendererControl controller)
    {
        super(model, controller);
    }
    
    /** 
     * Returns the name of the component. 
     * @see ControlPane#getPaneName()
     */
    protected String getPaneName() { return "Mapping"; }

    /**
     * Returns the icon attached to the component.
     * @see ControlPane#getPaneIcon()
     */
    protected Icon getPaneIcon()
    {
        IconManager icons = IconManager.getInstance();
        return icons.getIcon(IconManager.DOMAIN);
    }

    /**
     * Returns the brief description of the component.
     * @see ControlPane#getPaneDescription()
     */
    protected String getPaneDescription()
    {
        return "Sets the context used during the mapping progress";
    }

    protected void onStateChange()
    {
        // TODO Auto-generated method stub
        
    }

    /**
     * Returns the index of the component.
     * @see ControlPane#getPaneIndex()
     */
    protected int getPaneIndex() { return ControlPane.DOMAIN_PANE_INDEX; }
    
    public void stateChanged(ChangeEvent e)
    {
        Object source = e.getSource();
        if (source instanceof JSlider) {
            
        }
    }



    
}
