/*
 * org.openmicroscopy.shoola.agents.imviewer.view.ControlPane
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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.IconManager;
import org.openmicroscopy.shoola.agents.imviewer.actions.ViewerAction;
import org.openmicroscopy.shoola.agents.imviewer.util.ChannelButton;
import org.openmicroscopy.shoola.agents.imviewer.util.InfoButton;
import org.openmicroscopy.shoola.env.rnd.metadata.ChannelMetadata;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Presents variable controls.
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
class ControlPane
    extends JPanel
    implements ActionListener, ChangeListener
{

    /** The description of the {@link #zSlider}. */
    private static final String Z_SLIDER_DESCRIPTION = "Use the slider to " +
                            "select a z-section.";

    /** The description of the {@link #zSlider}. */
    private static final String T_SLIDER_DESCRIPTION = "Use the slider to " +
                            "select a timepoint.";
    
    /** Reference to the Control. */
    private ImViewerControl controller;
    
    /** Reference to the Model. */
    private ImViewerModel   model;
    
    /** Reference to the View. */
    private ImViewerUI      view;
    
    /** The box displaying the supported rating level. */
    private JComboBox       ratingBox;
    
    /** The box displaying the supported zooming level. */
    private JComboBox       zoomingBox;
    
    /** Slider to select the z-section. */
    private JSlider         zSlider;
    
    /** Slider to select the timepoint. */
    private JSlider         tSlider;

    /** The group hosting the color button. */
    private ButtonGroup     colorModelGroup;
    
    /** One  {@link ChannelButton} per channel. */
    private HashSet         channelButtons;
    
    /** Button to play movie across channel. */
    private JButton         channelMovie;
    
    /** Initializes the components composing the display. */
    private void initComponents()
    {
        ViewerAction[] zoomingActions = new ViewerAction[12];
        zoomingActions[0] = controller.getAction(ImViewerControl.ZOOM_25);
        zoomingActions[1] = controller.getAction(ImViewerControl.ZOOM_50);
        zoomingActions[2] = controller.getAction(ImViewerControl.ZOOM_75);
        zoomingActions[3] = controller.getAction(ImViewerControl.ZOOM_100);
        zoomingActions[4] = controller.getAction(ImViewerControl.ZOOM_125);
        zoomingActions[5] = controller.getAction(ImViewerControl.ZOOM_150);
        zoomingActions[6] = controller.getAction(ImViewerControl.ZOOM_175);
        zoomingActions[7] = controller.getAction(ImViewerControl.ZOOM_200);
        zoomingActions[8] = controller.getAction(ImViewerControl.ZOOM_225);
        zoomingActions[9] = controller.getAction(ImViewerControl.ZOOM_250);
        zoomingActions[10] = controller.getAction(ImViewerControl.ZOOM_275);
        zoomingActions[11] = controller.getAction(ImViewerControl.ZOOM_300);
        zoomingBox =  new JComboBox(zoomingActions);
        zoomingBox.setEnabled(false);
        ViewerAction[] ratingActions = new ViewerAction[5];
        ratingActions[0] = controller.getAction(ImViewerControl.RATING_ONE);
        ratingActions[1] = controller.getAction(ImViewerControl.RATING_TWO);
        ratingActions[2] = controller.getAction(ImViewerControl.RATING_THREE);
        ratingActions[3] = controller.getAction(ImViewerControl.RATING_FOUR);
        ratingActions[4] = controller.getAction(ImViewerControl.RATING_FIVE);
        ratingBox =  new JComboBox(ratingActions);
        ratingBox.setEnabled(false);

        zSlider = new JSlider(JSlider.VERTICAL, 0, 1, 0);
        zSlider.setEnabled(false);
        zSlider.setToolTipText(Z_SLIDER_DESCRIPTION);
        tSlider = new JSlider(JSlider.VERTICAL, 0, 1, 0);
        tSlider.setEnabled(false);
        tSlider.setToolTipText(T_SLIDER_DESCRIPTION);
        //zSlider.addChangeListener(this);
        //tSlider.addChangeListener(this);
        channelMovie = new JButton(
                controller.getAction(ImViewerControl.CHANNEL_MOVIE));
    }
    
    /**
     * Initializes the value of the components displaying the currently selected
     * z-section and timepoint.
     */
    private void initializeValues()
    {
        zoomingBox.addActionListener(this);
        zoomingBox.setSelectedItem(
                controller.getAction(ImViewerControl.ZOOM_100));
        switch (model.getRatingLevel()) {
            case ImViewerModel.RATING_ONE:
                ratingBox.setSelectedIndex(0);
                break;
            case ImViewerModel.RATING_TWO:
                ratingBox.setSelectedIndex(1);
                break;
            case ImViewerModel.RATING_THREE:
                ratingBox.setSelectedIndex(2);
                break;
            case ImViewerModel.RATING_FOUR:
                ratingBox.setSelectedIndex(3);
                break;
            case ImViewerModel.RATING_FIVE:
                ratingBox.setSelectedIndex(4);
        }
        ratingBox.addActionListener(this);
        zSlider.setMaximum(model.getMaxZ());
        zSlider.setValue(model.getDefaultZ());
        tSlider.setMaximum(model.getMaxT());
        tSlider.setValue(model.getDefaultT());
        zSlider.addChangeListener(this);
        tSlider.addChangeListener(this);
    }
    
    /**
     * Helper method to create a panel hosting the passed slider.
     * 
     * @param txt       The label related to the passed slider.
     * @param slider    The slider to host.
     * @return See above.
     */
    private JPanel createSliderPane(String txt, JSlider slider)
    {
        JPanel pane = new JPanel();
        pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
        JLabel label = new JLabel(txt);
        label.setLabelFor(slider);
        pane.add(label);
        pane.add(slider);
        return pane;
    }
    
    /**
     * Helper method to create a panel hosting the passed slider.
     * 
     * @return See above.
     */
    private JPanel createSliderPanes()
    {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(createSliderPane("Z ", zSlider));
        p.add(createSliderPane("T ", tSlider));
        return p;
    }
    
    /**
     * Helper method to create a UI component hosting the {@link #ratingBox} and
     * {@link #zoomingBox}.
     * 
     * @return See above.
     */
    private JPanel createBoxPanes()
    {
        JPanel p = new JPanel();
        GridBagConstraints c = new GridBagConstraints();  
        p.setLayout(new GridBagLayout());
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.NONE;
        JLabel label = new JLabel("Zoom");
        p.add(label, c);
        c.gridx = 1;
        p.add(UIUtilities.buildComponentPanel(zoomingBox), c);
        c.gridx = 0;
        c.gridy = 1;
        label = new JLabel("Rate Image");
        p.add(label, c);
        c.gridx = 1;
        p.add(UIUtilities.buildComponentPanel(ratingBox), c);
        return p;
    }

    
    /**
     * Helper method to create the UI component hosting the color model
     * selection.
     * 
     * @return See above.
     */
    private JPanel createColorModelPane()
    {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        colorModelGroup = new ButtonGroup();
        ViewerAction action = 
            controller.getAction(ImViewerControl.GREY_SCALE_MODEL);
        JRadioButton button = new JRadioButton();
        String cm = model.getColorModel();
        button.setSelected(cm.equals(ImViewer.GREY_SCALE_MODEL));
        button.setAction(action);
        colorModelGroup.add(button);
        p.add(button);
        action = controller.getAction(ImViewerControl.RGB_MODEL);
        button = new JRadioButton();
        button.setSelected(cm.equals(ImViewer.RGB_MODEL));
        button.setAction(action);
        colorModelGroup.add(button);
        p.add(button);
        action = controller.getAction(ImViewerControl.HSB_MODEL);
        button = new JRadioButton();
        button.setSelected(cm.equals(ImViewer.HSB_MODEL));
        button.setAction(action);
        colorModelGroup.add(button);
        p.add(button);
        return p;
    }

    /**
     * Creates a pane hosting the {@link ChannelButton} and the corresponding
     * {@link InfoButton}.
     * 
     * @param b The {@link ChannelButton} to display.
     * @param d The metadata associated.
     * @return See above.
     */
    private JPanel createButtonPane(ChannelButton b, ChannelMetadata d)
    {
        JPanel p = new JPanel();
        IconManager im = IconManager.getInstance();
        JButton button = new InfoButton(im.getIcon(IconManager.TINY_INFO), 
                                d);
        GridBagConstraints c = new GridBagConstraints();  
        p.setLayout(new GridBagLayout());
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.NONE;
        c.gridx = 1;
        p.add(button, c);
        c.gridx = 0;
        c.gridy = 1;
        p.add(b, c); 
        return p;
    }
    
    /**
     * Creates a UI component hosting the {@link ChannelButton}s.
     * 
     * @return See above.
     */
    private JPanel createChannelsPane()
    {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        ChannelMetadata[] data = model.getChannelData();
        ChannelButton button;
        ChannelMetadata d;
        for (int k = 0; k < data.length; k++) {
            d = data[k];
            button = new ChannelButton(""+d.getEmissionWavelength(), 
                    model.getChannelColor(k), k, model.isChannelActive(k));
            button.addPropertyChangeListener(controller);
            channelButtons.add(button);
            p.add(createButtonPane(button, d));
        }
        p.add(channelMovie);
        return p;
    }
    
    /** Builds and lays out the UI. */
    private void buildGUI()
    {
        JPanel controls = new JPanel();
        controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
        controls.add(createBoxPanes());
        controls.add(createColorModelPane());
        controls.add(createChannelsPane());
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        add(controls);
        add(createSliderPanes());
    }
    
    /**
     * Sets the value of the specified slider when a propertyChange has been
     * fired.
     * 
     * @param slider The slider to update.
     * @param v The selected value.
     */
    private void updateSlider(JSlider slider, int v)
    {
        slider.removeChangeListener(this);
        slider.setValue(v);
        slider.addChangeListener(this);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param controller    Reference to the Control.
     *                      Mustn't be <code>null</code>.
     * @param model         Reference to the Model.
     *                      Mustn't be <code>null</code>.
     * @param view          Reference to the View.
     *                      Mustn't be <code>null</code>.                    
     */
    ControlPane(ImViewerControl controller, ImViewerModel model,
                ImViewerUI view)
    {
        if (controller == null) throw new NullPointerException("No control.");
        if (model == null) throw new NullPointerException("No model.");
        if (view == null) throw new NullPointerException("No view.");
        this.controller = controller;
        this.model = model;
        this.view = view;
        channelButtons = new HashSet();
        initComponents();
    }
    
    /** 
     * This method should be called straight after the metadata and the
     * rendering settings are loaded.
     */
    void buildComponent()
    {
        initializeValues();
        buildGUI();
    }
    
    /**
     * Updates UI components when a new timepoint is selected.
     * 
     * @param t The selected timepoint.
     */
    void setTimepoint(int t) { updateSlider(tSlider, t); }
    
    /**
     * Updates UI components when a new z-section is selected.
     * 
     * @param z The selected z-section.
     */
    void setZSection(int z) { updateSlider(zSlider, z); }
    
    /**
     * Updates UI components when a zooming factor is selected.
     * 
     * @param action    The selected action embedding the zooming factor
     *                  information.
     */
    void setZoomFactor(ViewerAction action)
    {
        zoomingBox.removeActionListener(this);
        zoomingBox.setSelectedItem(action);
        zoomingBox.addActionListener(this);
    }
    
    /**
     * Updates UI components when a rating factor is selected.
     * 
     * @param action    The selected action embedding the rating factor
     *                  information.
     */
    void setRatingFactor(ViewerAction action)
    {
        ratingBox.removeActionListener(this);
        ratingBox.setSelectedItem(action);
        ratingBox.addActionListener(this);
    }
    
    /**
     * Updates UI components when a new color model is selected.
     * 
     * @param action    The selected action embedding the color model
     *                  information.
     */
    void setColorModel(ViewerAction action)
    {
        AbstractButton b;
        for (Enumeration e = colorModelGroup.getElements(); 
            e.hasMoreElements();) {
            b = (AbstractButton) e.nextElement();
            if ((b.getAction()).equals(action)) {
                b.removeActionListener(action);
                b.setSelected(true);
                b.setAction(action);
            }
        }
    }
    
    /** 
     * Reacts to {@link ImViewer} change events.
     * 
     * @param b Pass <code>true</code> to enable the UI components, 
     *          <code>false</code> otherwise.
     */
    void onStateChange(boolean b)
    {
        if (b) {
            zSlider.setEnabled(model.getMaxZ() != 0);
            tSlider.setEnabled(model.getMaxT() != 0);
        } else {
            zSlider.setEnabled(b);
            tSlider.setEnabled(b);
        } 
        zoomingBox.setEnabled(b);
        ratingBox.setEnabled(b);
        Iterator i = channelButtons.iterator();
        while (i.hasNext())
            ((ChannelButton) i.next()).setEnabled(b);
    }
    
    /**
     * Reacts to the selection of an item in the {@link #zoomingBox} or
     * {@link #ratingBox}.
     * 
     * @see ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        JComboBox cb = (JComboBox) e.getSource();
        ViewerAction va = (ViewerAction) cb.getSelectedItem();
        va.actionPerformed(e);
    }

    /**
     *  to selection of a new 
     * plane.
     * @see ChangeListener#stateChanged(ChangeEvent)
     */
    public void stateChanged(ChangeEvent e)
    {
        Object object = e.getSource();
        if (object instanceof JSlider) {
            controller.setSelectedXYPlane(zSlider.getValue(), 
                    tSlider.getValue());
        }
    }

    /** 
     * Updates the {@link ChannelButton}s when a new one is selected or 
     * deselected.
     */
    void setChannelsSelection()
    {
        Iterator i = channelButtons.iterator();
        ChannelButton button;
        while (i.hasNext()) {
            button = (ChannelButton) i.next();
            button.setSelected(model.isChannelActive(button.getChannelIndex()));
        }
    }
    
}
