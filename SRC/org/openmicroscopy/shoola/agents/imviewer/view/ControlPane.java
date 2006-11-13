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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.HashSet;
import java.util.Iterator;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.IconManager;
import org.openmicroscopy.shoola.agents.imviewer.actions.ViewerAction;
import org.openmicroscopy.shoola.agents.imviewer.util.ChannelButton;
import org.openmicroscopy.shoola.env.data.model.ChannelMetadata;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.slider.OneKnobSlider;

/** 
 * Presents variable controls.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 			<a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
class ControlPane
    extends JPanel
    implements ActionListener, ChangeListener, MouseWheelListener
{

    /** The description of the {@link #zSlider}. */
    private static final String Z_SLIDER_DESCRIPTION = "Use the slider to " +
                            "select a z-section.";

    /** The description of the {@link #tSlider}. */
    private static final String T_SLIDER_DESCRIPTION = "Use the slider to " +
                            "select a timepoint.";

    /** The tipString of the {@link #zSlider}. */
    private static final String Z_SLIDER_TIPSTRING = "Z";

    /** The tipString of the {@link #tSlider}. */
    private static final String T_SLIDER_TIPSTRING = "T";
    
    /** Dimension of the box between the channel buttons. */
    private static final Dimension VBOX = new Dimension(1, 10);
    
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
    private OneKnobSlider         zSlider;
    
    /** Slider to select the timepoint. */
    private OneKnobSlider         tSlider;
    
    /** One  {@link ChannelButton} per channel. */
    private HashSet         channelButtons;
    
    /** Button to play movie across channel. */
    private JButton         channelMovieButton;
    
    /** Button to select the color model. */
    private JButton         colorModelButton;
    
    /** Helper reference. */
    private IconManager     icons;
    
    /**
     * Handles the event when the wheel is moved over the {@link #zSlider}.
     * 
     * @param e The event to handle.
     */
    private void mouseWheelMovedZ(MouseWheelEvent e)
    {
        boolean up = true;
        if (e.getWheelRotation() > 0) up = false;
        if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
            int v = model.getDefaultZ()-e.getWheelRotation();
            if (up) {
                if (v <= model.getMaxZ())
                    controller.setSelectedXYPlane(v,  model.getDefaultT());
            } else { //moving down
                if (v >= 0)
                    controller.setSelectedXYPlane(v,  model.getDefaultT());
            }
        } else {
     
        }
    }
    
    /**
     * Handles the event when the wheel is moved over the {@link #tSlider}.
     * 
     * @param e The event to handle.
     */
    private void mouseWheelMovedT(MouseWheelEvent e)
    {
        boolean up = true;
        if (e.getWheelRotation() > 0) up = false;
        if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
            int v = model.getDefaultT()-e.getWheelRotation();
            if (up) {
                if (v <= model.getMaxT())
                    controller.setSelectedXYPlane(model.getDefaultZ(), v);
            } else { //moving down
                if (v >= 0)
                    controller.setSelectedXYPlane(model.getDefaultZ(), v);
            }
        } else {
            
        }
    }
    
    /**
     * Returns the icon corresponding to the current color model.
     * 
     * @param model The color model.
     * @return See above.
     */
    private Icon getColorModelIcon(String model)
    {
        if (model.equals(ImViewer.GREY_SCALE_MODEL))
            return icons.getIcon(IconManager.GRAYSCALE);
        else if (model.equals(ImViewer.RGB_MODEL))
            return icons.getIcon(IconManager.RGB);
        else if (model.equals(ImViewer.HSB_MODEL))
            return icons.getIcon(IconManager.RGB);
        return null;
    }

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

        zSlider = new OneKnobSlider(JSlider.VERTICAL, 0, 1, 0);
        zSlider.setEnabled(false);
        tSlider = new OneKnobSlider(JSlider.VERTICAL, 0, 1, 0);
        tSlider.setEnabled(false);
        //zSlider.addChangeListener(this);
        //tSlider.addChangeListener(this);
        channelMovieButton = new JButton(
                controller.getAction(ImViewerControl.CHANNEL_MOVIE));
        UIUtilities.unifiedButtonLookAndFeel(channelMovieButton);
        colorModelButton = new JButton();
        UIUtilities.unifiedButtonLookAndFeel(colorModelButton);
        colorModelButton.addActionListener(new ActionListener() {
        
            public void actionPerformed(ActionEvent e)
            {
                String m = model.getColorModel();
                ViewerAction a = null;
                if (m.equals(ImViewer.RGB_MODEL) || 
                   m.equals(ImViewer.HSB_MODEL)) {
                     a = controller.getAction(ImViewerControl.GREY_SCALE_MODEL);
                } else if (m.equals(ImViewer.GREY_SCALE_MODEL)) {
                     a = controller.getAction(ImViewerControl.RGB_MODEL);
                }    
                a.actionPerformed(e);
            }
        
        });
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
        zSlider.addMouseWheelListener(this);
        tSlider.addMouseWheelListener(this);
        zSlider.setToolTipText(Z_SLIDER_DESCRIPTION);
        zSlider.setEndLabel(Z_SLIDER_TIPSTRING);
        zSlider.setShowEndLabel(true);
        zSlider.setShowTipLabel(true);
        tSlider.setToolTipText(T_SLIDER_DESCRIPTION);
        tSlider.setEndLabel(T_SLIDER_TIPSTRING);
        tSlider.setShowEndLabel(true);
        tSlider.setShowTipLabel(true);
        colorModelButton.setIcon(getColorModelIcon(model.getColorModel()));
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
     * Builds the tool bar displayed on the left side of the image.
     * 
     * @return See above.
     */
    private JToolBar buildToolBar()
    {
        JToolBar bar = new JToolBar(JToolBar.VERTICAL);
        bar.setFloatable(false);
        bar.setRollover(true);
        bar.setBorder(null);
        bar.add(colorModelButton);
        bar.add(Box.createRigidArea(VBOX));
        bar.add(channelMovieButton);
        return bar;
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
        p.add(Box.createRigidArea(VBOX));
        boolean gs = model.getColorModel().equals(ImViewer.GREY_SCALE_MODEL);
        for (int k = 0; k < data.length; k++) {
            d = data[k];
            button = new ChannelButton(""+d.getEmissionWavelength(), 
                    model.getChannelColor(k), k, model.isChannelActive(k));
            if (gs) button.setGrayedOut(gs);
            button.addPropertyChangeListener(controller);
            button.setPreferredSize(new Dimension(30, 30));
            channelButtons.add(button);
            p.add(button);
            p.add(Box.createRigidArea(VBOX));
        }
        
        JPanel controls = new JPanel();
        controls.setLayout(new BoxLayout(controls,BoxLayout.Y_AXIS));
        controls.add(Box.createVerticalStrut(20));
        controls.add(buildToolBar());
        controls.add(p);
        return UIUtilities.buildComponentPanel(controls);
    }
    
    /** Builds and lays out the UI. */
    private void buildGUI()
    {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        add(createChannelsPane());
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
        icons = IconManager.getInstance();
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
    
    /** Updates UI components when a new color model is selected. */
    void setColorModel()
    {
        boolean gs = (model.getColorModel().equals(ImViewer.GREY_SCALE_MODEL));
        Iterator i = channelButtons.iterator();
        ChannelButton button;
        while (i.hasNext()) {
            button = (ChannelButton) i.next();
            button.setSelected(
                    model.isChannelActive(button.getChannelIndex()));
            button.setGrayedOut(gs);
        }
        colorModelButton.setIcon(getColorModelIcon(model.getColorModel()));
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
            button.setSelected(
                    model.isChannelActive(button.getChannelIndex()));
        }
    }
    
    /**
     * Sets the color of selected channel.
     * 
     * @param index The channel index.
     * @param c     The color to set.
     */
    void setChannelColor(int index, Color c)
    {
        Iterator i = channelButtons.iterator();
        ChannelButton button;
        while (i.hasNext()) {
            button = (ChannelButton) i.next();
            if (index == button.getChannelIndex()) {
                button.setColor(c);
            }
        }
    }
    
    /** Resets the default. */
    void resetDefaults()
    {
        boolean gs = (model.getColorModel().equals(ImViewer.GREY_SCALE_MODEL));
        Iterator i = channelButtons.iterator();
        ChannelButton button;
        int index;
        while (i.hasNext()) {
            button = (ChannelButton) i.next();
            index = button.getChannelIndex();
            button.setSelected(model.isChannelActive(index));
            button.setColor(model.getChannelColor(index)); 
            button.setGrayedOut(gs);
        }
        colorModelButton.setIcon(getColorModelIcon(model.getColorModel()));
        setZSection(model.getDefaultZ());
        setTimepoint(model.getDefaultT());
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
        colorModelButton.setEnabled(b);
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
     * Reacts to selection of a new plane.
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
     * Reacts to wheels moved event related to the {@link #zSlider} and
     * {@link #tSlider}.
     * @see MouseWheelListener#mouseWheelMoved(MouseWheelEvent)
     */
    public void mouseWheelMoved(MouseWheelEvent e)
    {
        Object source = e.getSource();
        if (source == zSlider && zSlider.isEnabled()) {
            mouseWheelMovedZ(e);
        } else if (source == tSlider && tSlider.isEnabled()) {
            mouseWheelMovedT(e);
        }
    }
    
}
