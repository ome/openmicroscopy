/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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
package org.openmicroscopy.shoola.agents.metadata.rnd;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import org.apache.commons.collections.CollectionUtils;

import org.openmicroscopy.shoola.agents.util.ViewedByItem;
import org.openmicroscopy.shoola.env.rnd.RndProxyDef;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.WrapLayout;
import org.openmicroscopy.shoola.util.ui.slider.TextualTwoKnobsSlider;
import org.openmicroscopy.shoola.util.ui.slider.TwoKnobsSlider;
import omero.gateway.model.ChannelData;

/** 
 * Component hosting the diagram and the controls to select the pixels intensity
 * interval and the codomain interval.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *         <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *          <a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
class GraphicsPane
    extends JPanel
    implements PropertyChangeListener
{

    /** 
     * Factor used to determine the percentage of the range added 
     * (resp. removed) to (resp. from) the maximum (resp. the minimum).
     */
    static final double RATIO = 0.2;
    
    /** The title of the viewedby taskpane */
    static final String VIEWEDBY_TITLE = "User Settings:";

    /** Slider to select a sub-interval of [0, 255]. */
    private TwoKnobsSlider codomainSlider;

    /** Slider to select the pixels intensity interval. */
    private TextualTwoKnobsSlider domainSlider;

    /** The label displaying the global max. */
    private JLabel maxLabel;

    /** The label displaying the global minimum. */
    private JLabel minLabel;

    /** The component displaying the plane histogram. */
    private GraphicsPaneUI uiDelegate;

    /** Reference to the Model.*/
    protected RendererModel model;

    /** Reference to the Control.*/
    protected RendererControl controller;

    /** Flag indicating to paint a vertical line. */
    private boolean paintVertical;

    /** Flag indicating to paint a vertical line. */
    private boolean paintHorizontal;

    /** The equation the horizontal line. */
    private int horizontalLine = -1;

    /** The equation of the vertical line. */
    private int verticalLine = -1;

    /** Checkbox for switching between greyscale and rgb mode */
    private JCheckBox greyScale;
    
    /** Hosts the sliders controlling the pixels intensity values. */
    private List<ChannelSlider> sliders;

    /** The component displaying the controls. */
    private PreviewControlBar controlsBar;
    
    /** The lower control pane */
    private PreviewControlBar2 controlsBar2;

    /** The Tasks pane, only visible if already viewed by others. */
    private JPanel viewedBy;

    /** The preview tool bar. */
    private PreviewToolBar previewToolBar;

    /** The items shown in the 'saved by' taskpane */
    private List<ViewedByItem> viewedByItems;
    
    /**
     * Formats the specified value.
     * 
     * @param value The value to format.
     * @return See above.
     */
    private String formatValue(double value)
    {
        if (model.isIntegerPixelData())
            return ""+(int) value;
        else
            return UIUtilities.formatToDecimal(value);
    }

    /** Initializes the domain slider. */
    private void initDomainSlider()
    {
        int s = (int) model.getWindowStart();
        int e = (int) model.getWindowEnd();
        int absMin = (int) model.getLowestValue();
        int absMax = (int) model.getHighestValue();
        int min = (int) model.getGlobalMin();
        int max = (int) model.getGlobalMax();
        double range = (max-min)*RATIO;
        int lowestBound = (int) (min-range);
        if (lowestBound < absMin) lowestBound = absMin;
        int highestBound = (int) (max+range);
        if (highestBound > absMax) highestBound = absMax;
        domainSlider.setValues(max, min, highestBound, lowestBound,
                max, min, s, e);
        if (model.getMaxC() > Renderer.MAX_CHANNELS)
            domainSlider.setInterval(min, max);
    }

    /** Initializes the components. */
    private void initComponents()
    {
        viewedBy = new JPanel();
        Font font = viewedBy.getFont();
        viewedBy.setFont(font.deriveFont(font.getSize2D()-2));
        viewedBy.setBackground(UIUtilities.BACKGROUND_COLOR);
        viewedBy.setLayout(new BorderLayout());
        controlsBar = new PreviewControlBar(controller, model);
        controlsBar2 = new PreviewControlBar2(controller);
        uiDelegate = new GraphicsPaneUI(this, model);
        codomainSlider = new TwoKnobsSlider(RendererModel.CD_START,
                RendererModel.CD_END, model.getCodomainStart(),
                model.getCodomainEnd());
        codomainSlider.setBackground(UIUtilities.BACKGROUND_COLOR);
        codomainSlider.setPaintLabels(false);
        codomainSlider.setPaintEndLabels(false);
        codomainSlider.setPaintTicks(false);
        codomainSlider.setColourGradients(Color.BLACK, Color.WHITE);
        codomainSlider.addPropertyChangeListener(this);

        domainSlider = new TextualTwoKnobsSlider();
        domainSlider.setBackground(UIUtilities.BACKGROUND_COLOR);
        initDomainSlider();
        domainSlider.getSlider().setPaintLabels(false);
        domainSlider.getSlider().setPaintEndLabels(false);
        domainSlider.getSlider().setPaintTicks(false);
        domainSlider.addPropertyChangeListener(this);
        maxLabel = new JLabel(formatValue(model.getGlobalMax()));
        minLabel = new JLabel(formatValue(model.getGlobalMin()));
        maxLabel.setBackground(UIUtilities.BACKGROUND_COLOR);
        minLabel.setBackground(UIUtilities.BACKGROUND_COLOR);
        
        greyScale = new JCheckBox("Grayscale");
        greyScale.setSelected(model.isGreyScale());
        greyScale.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                model.setGreyscale(greyScale.isSelected());
            }
        });
        
        sliders = new ArrayList<ChannelSlider>();
        if (model.getModuloT() != null || !model.isLifetimeImage()) {
            List<ChannelData> channels = model.getChannelData();
            Iterator<ChannelData> i = channels.iterator();
            ChannelSlider slider;
            while (i.hasNext()) {
                slider = new ChannelSlider(this, model, controller, i.next());
                sliders.add(slider);
            }
        }
        previewToolBar = new PreviewToolBar(controller, model);
    }

    /** Builds and lays out the GUI. */
    private void buildGUI()
    {
        setBackground(UIUtilities.BACKGROUND_COLOR);
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        if (model.isGeneralIndex()) {
            add(buildGeneralPane(), c);
        } else {
            c.weightx = 0;
            add(buildPane(), c);
            
            c.weightx = 1;
            c.gridx++;
            add(buildGeneralPane(), c);
        }
    }

    /** 
     * Builds hosting the various sliders
     * 
     * @return See above.
     */
    private JPanel buildGeneralPane()
    {
        JPanel content = new JPanel();
        content.setBackground(UIUtilities.BACKGROUND_COLOR);
        content.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        content.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(1, 2, 1, 2);
        c.gridy = 0;
        c.gridx = 0;
        c.weightx = 1;
        c.weighty = 0;
        if (model.isGeneralIndex()) {
            content.add(previewToolBar, c);
            c.gridy++;
            content.add(new JSeparator(), c);
            c.gridy++;
        }
        content.add(controlsBar, c);
        c.gridy++;
        
        content.add(new JSeparator(), c);
        c.gridy++;
        
        content.add(greyScale, c);
        c.gridy++;
        
        Iterator<ChannelSlider> i = sliders.iterator();
        while (i.hasNext())  {
            content.add(i.next(), c);
            c.gridy++;
        }
        
        c.insets = new Insets(3, 1, 3, 1);
        content.add(controlsBar2, c);
        c.gridy++;
        
        c.insets = new Insets(0, 0, 0, 0);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.weighty = 0;
        content.add(new JSeparator(), c);
        c.gridy++;
      
        content.add(new JLabel(VIEWEDBY_TITLE), c);
        c.gridy++;
      
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1;
        content.add(viewedBy, c);
        
        return content;
    }

    /** 
     * Builds and lays out the slider.
     * 
     * @return See above.
     */
    private JPanel buildPane()
    {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(UIUtilities.BACKGROUND_COLOR);
        p.add(codomainSlider);
        return p;
    }

    /**
     * Creates a new instance.
     * 
     * @param model Reference to the model. Mustn't be <code>null</code>.
     * @param controller Reference to the control. Mustn't be <code>null</code>.
     */
    GraphicsPane(RendererModel model, RendererControl controller)
    {
        if (model == null) throw new NullPointerException("No model.");
        if (controller == null) 
            throw new NullPointerException("No controller.");
        this.model = model;
        this.controller = controller;
        initComponents();
        buildGUI();
    }

    /** Sets the value of the selected plane.*/
    void setSelectedPlane()
    {
        if (previewToolBar != null) previewToolBar.setSelectedPlane();
    }

    /** Updates the controls when a new channel is selected. */
    void setSelectedChannel()
    {
        Iterator<ChannelSlider> i = sliders.iterator();
        ChannelSlider slider;
        while (i.hasNext()) {
            slider = i.next();
            slider.setSelectedChannel();
        }
    }

    /** Sets the pixels intensity interval. */
    void setInputInterval()
    {
        double s, e;
        Iterator<ChannelSlider> i = sliders.iterator();
        ChannelSlider slider;
        while (i.hasNext()) {
            slider = i.next();
            s = model.getWindowStart(slider.getIndex());
            e = model.getWindowEnd(slider.getIndex());
            slider.setInterval(s, e);
        }
    }

    /** 
     * Modifies the input range of the channel sliders.
     * 
     *  @param absolute Pass <code>true</code> to set it to the absolute value,
     *                  <code>false</code> to the minimum and maximum.
     */
    void setInputRange(boolean booleanValue)
    {
        if (CollectionUtils.isEmpty(sliders)) return;
        Iterator<ChannelSlider> i = sliders.iterator();
        while (i.hasNext())
            i.next().setInputRange(booleanValue);
    }


    /** Sets the value of the codomain interval. */
    void setCodomainInterval()
    {
        codomainSlider.setInterval(model.getCodomainStart(),
                model.getCodomainEnd());
        onCurveChange();
    }

    /** 
     * Updates the UI when a new curve is selected i.e. when a new family
     * is selected or when a new gamma value is selected.
     */
    void onCurveChange()
    {
        uiDelegate.invalidate();
        uiDelegate.repaint();
    }

    /**
     * Returns <code>true</code> if a vertical or horizontal line has
     * to be painted, <code>false</code> otherwise.
     * 
     * @return See above.
     */
    boolean isPaintLine() { return paintVertical() || paintHorizontal(); }

    /**
     * Returns <code>true</code> if the life update is selected,
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    boolean isLiveUpdate() { return previewToolBar!=null ? previewToolBar.isLiveUpdate() : false; }

    /**
     * Returns <code>true</code> if a vertical line has
     * to be painted, <code>false</code> otherwise.
     * 
     * @return See above.
     */
    boolean paintVertical() { return paintVertical; }

    /**
     * Returns <code>true</code> if a horizontal line has
     * to be painted, <code>false</code> otherwise.
     * 
     * @return See above.
     */
    boolean paintHorizontal() { return paintHorizontal; }

    /**
     * Returns the equation of the horizontal line or <code>-1</code>
     * if no horizontal line defined.
     * 
     * @return See above.
     */
    int getHorizontalLine() { return horizontalLine; }

    /**
     * Returns the equation of the vertical line or <code>-1</code>
     * if no vertical line defined.
     * 
     * @return See above.
     */
    int getVerticalLine() { return verticalLine; }

    /**
     * Returns the value of the partial minimum.
     * 
     * @return See above.
     */
    int getPartialMinimum()
    { 
        return (int)domainSlider.getSlider().getPartialMinimum();
    }

    /**
     * Returns the value of the partial maximum.
     * 
     * @return See above.
     */
    int getPartialMaximum()
    { 
        return (int)domainSlider.getSlider().getPartialMaximum();
    }

    /** 
     * Sets the enabled flag of the UI components.
     * 
     * @param b The value to set.
     */
    void onStateChange(boolean b)
    {
        if (codomainSlider != null) codomainSlider.setEnabled(b);
        if (domainSlider != null) domainSlider.setEnabled(b);
    }

    /** Toggles between color model and Greyscale. */
    void setColorModelChanged() 
    {
        if (CollectionUtils.isEmpty(sliders)) return;
        Iterator<ChannelSlider> i = sliders.iterator();
        while (i.hasNext()) {
            i.next().setColorModelChanged();
        }
    }

    /**
     * Sets the color of the passed channel.
     *  
     * @param index The index of the channel.
     */
    void setChannelColor(int index)
    {
        if (CollectionUtils.isNotEmpty(sliders)) {
            Iterator<ChannelSlider> i = sliders.iterator();
            ChannelSlider slider;
            while (i.hasNext()) {
                slider = i.next();
                if (slider.getIndex() == index) {
                    slider.setChannelColor();
                    break;
                }
            }
        }
        repaint();
    }

    /** 
     * Builds and lays out the images as seen by other experimenters.
     *  
     * @param results The thumbnails to lay out.
     * @param activeRndDef The rendering setting which is currently used
     */
    void displayViewedBy(List<ViewedByItem> results, RndProxyDef activeRndDef)
    {
        if (results == null) {
            viewedBy.removeAll();
            return;
        }
         
        this.viewedByItems = results;
        Collections.sort(this.viewedByItems, new ViewedByItemComparator());
        
        JPanel p = new JPanel();
        p.setLayout(new WrapLayout(WrapLayout.LEFT));
        p.setBackground(UIUtilities.BACKGROUND_COLOR);
        Iterator<ViewedByItem> i = viewedByItems.iterator();
        ViewedByItem item;
        while (i.hasNext()) {
            item = i.next();
            item.addPropertyChangeListener(this);
            p.add(createViewedByPanel(item));       
        }
        
        if(activeRndDef!=null) {
            highlight(activeRndDef);
        }
        
        p.setSize(viewedBy.getSize());
        
        viewedBy.removeAll();
        viewedBy.add(p, BorderLayout.CENTER);
        viewedBy.validate();
    }

    /**
     * Wraps the ViewedByItem in a JPanel with empty border acting as inset
     * @param item The ViewedByItem
     */
    private JPanel createViewedByPanel(ViewedByItem item) {
        JPanel viewedByPanel = new JPanel();
        viewedByPanel.setBackground(UIUtilities.BACKGROUND_COLOR);
        viewedByPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        viewedByPanel.add(item);
        return viewedByPanel;
    }
    
    /**
     * Draws a border around the ViewedByItem whichs represents
     * the given RndProxyDef
     * @param def The RndProxyDef to highlight
     */
    void highlight(RndProxyDef def) {
        for(ViewedByItem item : viewedByItems) {
            if(item.getRndDef().getData().getId().getValue()==def.getData().getId().getValue()) {
                ((JPanel)item.getParent()).setBorder(BorderFactory.createLineBorder(UIUtilities.STEELBLUE, 2));
            }
            else {
                ((JPanel)item.getParent()).setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
            }
        }
    }
    
    /**
     * Returns the slider used to set the codomain interval.
     * 
     * @return See above.
     */
    JComponent getCodomainSlider() { return codomainSlider; }

    /**
     * Reacts to property changes fired by the {@link TwoKnobsSlider}s.
     * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt)
    {
        String name = evt.getPropertyName();
        Object source = evt.getSource();
        if (!previewToolBar.isLiveUpdate()) {
            if (TwoKnobsSlider.KNOB_RELEASED_PROPERTY.equals(name)) {
                paintHorizontal = false;
                paintVertical = false;
                if (source.equals(domainSlider)) {
                    controller.setInputInterval(domainSlider.getStartValue(),
                            domainSlider.getEndValue());
                    onCurveChange();
                } else if (source.equals(codomainSlider)) {
                    int s = codomainSlider.getStartValueAsInt();
                    int e = codomainSlider.getEndValueAsInt();
                    controller.setCodomainInterval(s, e);
                    onCurveChange();
                }
            } else if (TwoKnobsSlider.LEFT_MOVED_PROPERTY.equals(name)){
                if (source.equals(domainSlider)) {
                    verticalLine = (int) (domainSlider.getStartValue());
                    paintHorizontal = false;
                    paintVertical = true;
                    onCurveChange();
                } else if (source.equals(codomainSlider)) {
                    horizontalLine = codomainSlider.getEndValueAsInt();
                    paintHorizontal = true;
                    paintVertical = false;
                    onCurveChange();
                }
            } else if (TwoKnobsSlider.RIGHT_MOVED_PROPERTY.equals(name)) {
                if (source.equals(domainSlider)) {
                    verticalLine = (int) (domainSlider.getEndValue());
                    horizontalLine = -1;
                    paintHorizontal = false;
                    paintVertical = true;
                    onCurveChange();
                } else if (source.equals(codomainSlider)) {
                    horizontalLine = codomainSlider.getStartValueAsInt();
                    verticalLine = -1;
                    paintHorizontal = true;
                    paintVertical = false;
                    onCurveChange();
                }
            }
        } else {
            paintHorizontal = false;
            paintVertical = false;
            if (TwoKnobsSlider.LEFT_MOVED_PROPERTY.equals(name)
                    || TwoKnobsSlider.RIGHT_MOVED_PROPERTY.equals(name)) {
                if (source.equals(domainSlider)) {
                    controller.setInputInterval(domainSlider.getStartValue(),
                            domainSlider.getEndValue());
                    onCurveChange();
                } else if (source.equals(codomainSlider)) {
                    int s = codomainSlider.getStartValueAsInt();
                    int e = codomainSlider.getEndValueAsInt();
                    controller.setCodomainInterval(s, e);
                    onCurveChange();
                }
            } else if (TwoKnobsSlider.KNOB_RELEASED_PROPERTY.equals(name)) {
                if (source.equals(domainSlider)) {
                    controller.setInputInterval(domainSlider.getStartValue(),
                            domainSlider.getEndValue());
                } else if (source.equals(codomainSlider)) {
                    int s = codomainSlider.getStartValueAsInt();
                    int e = codomainSlider.getEndValueAsInt();
                    controller.setCodomainInterval(s, e);
                    onCurveChange();
                }
            }
        }
        if(ViewedByItem.VIEWED_BY_PROPERTY.equals(name)) {
            RndProxyDef def = (RndProxyDef)evt.getNewValue();
            highlight(def);
        }
    }
    
    /**
     * Checks/Unchecks the greyscale checkbox when the color model has changed
     * @param b Pass <code>true</code> if color model is greyscale
     */
    void updateGreyScale(boolean b) {
        greyScale.setSelected(b);
    }

    /**
     * Comparator which sorts the ViewedByItems by its
     * experimenter's last name
     */
    class ViewedByItemComparator implements Comparator<ViewedByItem> {

        @Override
        public int compare(ViewedByItem o1, ViewedByItem o2) {
            String name1 = o1.getExperimenter().getLastName() != null ? o1.getExperimenter().getLastName() : "";
            String name2 = o2.getExperimenter().getLastName() != null ? o2.getExperimenter().getLastName() : "";
            return name1.compareToIgnoreCase(name2);
        }
        
    }
}
