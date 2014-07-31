/*
 * org.openmicroscopy.shoola.agents.metadata.rnd.DomainPane 
 *
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


//Java imports
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

//Third-party libraries
import org.jdesktop.swingx.JXTaskPane;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.agents.util.ViewedByItem;
import org.openmicroscopy.shoola.agents.util.ui.ChannelButton;
import org.openmicroscopy.shoola.env.rnd.RndProxyDef;
import org.openmicroscopy.shoola.util.image.geom.Factory;
import org.openmicroscopy.shoola.util.ui.ColorListRenderer;
import org.openmicroscopy.shoola.util.ui.SeparatorPane;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.slider.OneKnobSlider;
import pojos.ChannelData;

/** 
 * Pane displaying the controls used to define the transformation process
 * of the pixels intensity values.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">
 * donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class DomainPane
	extends ControlPane
    implements ActionListener, ChangeListener, MouseWheelListener
{

    /** 
     * For slider control only. The minimum value for the curve coefficient.
     * The real value is divided by 10.
     */
    static final int MIN_GAMMA = 1;

    /** 
     * For slider control only. The maximum value for the curve coefficient.
     * The real value is divided by 10.
     */
    static final int MAX_GAMMA = 40;

    /** 
     * For slider control only. The default value for the curve coefficient.
     * The real value is divided by 10.
     */
    static final int DEFAULT_GAMMA = 10;
 
    /** The minimum value of the bit resolution. */
    static final int MIN_BIT_DEPTH = 1;

    /** The maximum value of the bit resolution. */
    static final int MAX_BIT_DEPTH = 8;

    /** The default value of the bit resolution. */
    static final int DEFAULT_BIT_DEPTH = 8;

    /** The border of the selected channel. */
    private static final Border SELECTION_BORDER =
            BorderFactory.createLineBorder(Color.BLACK, 3);

    /** The factor .*/
    private static final int FACTOR = 10;

    /** Identifies the <code>Family</code> selection. */
    private static final int FAMILY = 0;

    /** Identifies the <code>Channels</code> selection. */
    private static final int CHANNEL = 1;

    /** Dimension of the box between the channel buttons. */
    private static final Dimension VBOX = new Dimension(1, 10);

    /** Title of the advanced options. */
    private static final String ADVANCED_OPTIONS = "Advanced"; 

    /** The description of a z-sections selection slider. */
    private static final String Z_SLIDER_DESCRIPTION = "Select a z-section.";

    /** The description of a time-point selection slider. */
    private static final String T_SLIDER_DESCRIPTION = "Select a timepoint.";

    /** The description of a bin selection slider. */
    private static final String LITEIME_SLIDER_DESCRIPTION = "Select a bin.";

    /** The tipString of the {@link #zSlider}. */
    private static final String Z_SLIDER_TIPSTRING = "Z";

    /** The tipString of the {@link #tSlider}. */
    private static final String T_SLIDER_TIPSTRING = "T";

    /** The tipString of the {@link #lifetimeSlider}. */
    private static final String LIFETIME_SLIDER_TIPSTRING = "t";

    /** Box to select the family used in the mapping process. */
    private JComboBox familyBox;

    /** 
     * A collection of ColourButtons which represent the channel selected 
     * in the mapping process. 
     */
    private List<ChannelButton> channelList;

    /** A panel containing the channel buttons. */
    private JPanel channelButtonPanel;

    /** Slider to select a curve in the family. */
    private OneKnobSlider gammaSlider;

    /** Slider to select the bit resolution of the rendered image. */
    private OneKnobSlider bitDepthSlider;

    /** Field displaying the <code>Gamma</code> value. */
    private JTextField gammaLabel;

    /** Field displaying the <code>Bit Depth</code> value. */
    private JTextField bitDepthLabel;

    /** Box to select the mapping algorithm. */
    private JCheckBox noiseReduction;

    /** Button to bring up the histogram widget on screen. */
    private JButton histogramButton;

    /** The UI component hosting the interval selections. */
    private GraphicsPane graphicsPane;

    /** The component hosting the various options. */
    private JXTaskPane taskPane;

    /** Button to modify the color model. */
    private JButton colorModel;

    /** Select the lifetime bin. */
    private OneKnobSlider lifetimeSlider;

    /** Selects the z-section. */
    private OneKnobSlider zSlider;

    /** Selects the time-point. */
    private OneKnobSlider tSlider;

    /** The component displaying the preview image. */
    private PreviewCanvas canvas;

    /** The box hosting the channels. */
    private JComboBox channelsBox;

    /**
     * Attaches listener to the passed slider and sets the default values.
     * 
     * @param slider	The slider to handle.
     * @param max		The maximum value.
     * @param v			The default value.
     * @param toolTip	The text displayed in the tool tip.
     * @param endLabel	The text displayed in the tool tip when 
     * 					slider changes value, as well as the label shown at 
     * 					the end of the text. 
     */
    private void initSlider(OneKnobSlider slider, int max, int v,
    						String toolTip, String endLabel)
    {
    	slider.setEnabled(max > 0);
    	slider.setBackground(UIUtilities.BACKGROUND_COLOR);
    	slider.setVisible(max != 0);
    	slider.setMaximum(max);
    	slider.setValue(v);
    	slider.addChangeListener(this);
        slider.addMouseWheelListener(this);
        slider.setToolTipText(toolTip);
        slider.setEndLabel(endLabel);
        slider.setShowEndLabel(true);
        slider.setShowTipLabel(true);
        if (max > 0  && max <= Renderer.MAX_NO_TICKS) {
        	slider.setPaintTicks(true);
        	slider.setMajorTickSpacing(1);
        }
    }
   
    /**
     * Handles the event when the wheel is moved over the {@link #zSlider}
     * or {@link #zSliderGrid}.
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
        }
        graphicsPane.setSelectedPlane();
    }
    
    /**
     * Handles the event when the wheel is moved over the {@link #tSlider}
     * or {@link #tSliderGrid}.
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
        }
        graphicsPane.setSelectedPlane();
    }
    
    /** Initializes the components composing the display. */
    private void initComponents()
    {
    	taskPane = EditorUtil.createTaskPane(ADVANCED_OPTIONS);
        graphicsPane = new GraphicsPane(model, controller);
        familyBox = new JComboBox(model.getFamilies().toArray());
        familyBox.setBackground(UIUtilities.BACKGROUND_COLOR);
        String family = model.getFamily();
        familyBox.setSelectedItem(family);
        familyBox.addActionListener(this);
        familyBox.setActionCommand(""+FAMILY);
        
        boolean gammaEnabled = family.equals(RendererModel.EXPONENTIAL) ||
                family.equals(RendererModel.POLYNOMIAL);
        double k = model.getCurveCoefficient();
        gammaSlider = new OneKnobSlider(JSlider.HORIZONTAL, MIN_GAMMA,
        							MAX_GAMMA, (int) (k*FACTOR));
        gammaSlider.setBackground(UIUtilities.BACKGROUND_COLOR);
        gammaSlider.setShowArrows(false);
        gammaSlider.setEnabled(gammaEnabled);
        gammaSlider.addChangeListener(this);
        gammaSlider.addMouseListener(new MouseAdapter() {

            public void mouseReleased(MouseEvent e) {
                double v = (double) gammaSlider.getValue()/FACTOR;
                gammaLabel.setText(""+v);
                int channel = channelsBox.getSelectedIndex();
                controller.setCurveCoefficient(channel, v);
            }
        });
        gammaLabel = new JTextField(""+k);
        gammaLabel.setBackground(UIUtilities.BACKGROUND_COLOR);
        gammaLabel.setEnabled(gammaEnabled);
        gammaLabel.setEditable(true);
        gammaLabel.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent arg0) {
                try {
                    double value = Double.parseDouble(gammaLabel.getText());
                    gammaSlider.setValue((int)(value*FACTOR));
                    int channel = channelsBox.getSelectedIndex();
                    controller.setCurveCoefficient(channel, value);
                } catch (NumberFormatException e1) {
                    gammaLabel.setText(""+(double) gammaSlider.getValue()/FACTOR);
                }
            }
            
        });
        
        int v = model.getBitResolution();
        bitDepthSlider = new OneKnobSlider(JSlider.HORIZONTAL, MIN_BIT_DEPTH,
                                MAX_BIT_DEPTH, convertBitResolution(v));
        bitDepthSlider.setBackground(UIUtilities.BACKGROUND_COLOR);
        bitDepthSlider.setShowArrows(false);
        bitDepthSlider.addMouseListener(new MouseAdapter() {
		
			public void mouseReleased(MouseEvent e) {
				int v = convertUIBitResolution(bitDepthSlider.getValue());
	            bitDepthLabel.setText(""+v);
	            firePropertyChange(BIT_RESOLUTION_PROPERTY,
	            		Integer.valueOf(model.getBitResolution()),
	            		Integer.valueOf(v));
			}
		
		});
        bitDepthSlider.addChangeListener(this);
        bitDepthLabel = new JTextField(""+v);
        bitDepthLabel.setBackground(UIUtilities.BACKGROUND_COLOR);
        bitDepthLabel.setEnabled(false);
        bitDepthLabel.setEditable(false);
        noiseReduction = new JCheckBox();
        noiseReduction.setBackground(UIUtilities.BACKGROUND_COLOR);
        noiseReduction.setSelected(model.isNoiseReduction());
        noiseReduction.setAction(
                controller.getAction(RendererControl.NOISE_REDUCTION));
        histogramButton = new JButton(
                controller.getAction(RendererControl.HISTOGRAM));
        
        colorModel = new JButton(
        		controller.getAction(RendererControl.COLOR_MODEL));
        colorModel.setBackground(UIUtilities.BACKGROUND_COLOR);
        UIUtilities.unifiedButtonLookAndFeel(colorModel);
        colorModel.setVisible(false);
        channelList = new ArrayList<ChannelButton>();
        
        if (model.isGeneralIndex()) {
        	int maxZ = model.getMaxZ()-1;
        	zSlider = new OneKnobSlider(OneKnobSlider.VERTICAL, 0, 1, 0);
        	zSlider.setEnabled(false);
        	tSlider = new OneKnobSlider(OneKnobSlider.HORIZONTAL, 0, 1, 0);
        	tSlider.setEnabled(false);
        	initSlider(tSlider, model.getRealT()-1, model.getRealSelectedT(),
        			T_SLIDER_DESCRIPTION, T_SLIDER_TIPSTRING);

        	initSlider(zSlider, maxZ, model.getDefaultZ(), 
        			Z_SLIDER_DESCRIPTION, Z_SLIDER_TIPSTRING);
        	canvas = new PreviewCanvas();
        	canvas.addMouseListener(new MouseAdapter() {

        		/**
        		 * Posts an event to open the viewer when double-clicking 
        		 * on the canvas.
        		 */
        		public void mouseReleased(MouseEvent e)
        		{
        			if (e.getClickCount() == 2) {
        				ActionEvent event = new ActionEvent(
        						e.getSource(), e.getID(), "");
        				controller.getAction(
        						RendererControl.VIEW).actionPerformed(event);
        			}
        		}
        	});
        }
        if (model.hasModuloT()) {
            lifetimeSlider = new OneKnobSlider(OneKnobSlider.HORIZONTAL,
                    0, 1, 0);
            lifetimeSlider.setEnabled(false);
            int maxBin = model.getMaxLifetimeBin()-1;
            initSlider(lifetimeSlider, maxBin, model.getSelectedBin(),
                    LITEIME_SLIDER_DESCRIPTION, LIFETIME_SLIDER_TIPSTRING);
            lifetimeSlider.setPaintTicks(false);
            channelButtonPanel = createChannelButtons();
        } else {
            if (model.isLifetimeImage()) {
                lifetimeSlider = new OneKnobSlider(OneKnobSlider.HORIZONTAL,
                        0, 1, 0);
                lifetimeSlider.setEnabled(false);
                int maxBin = model.getMaxLifetimeBin()-1;
                initSlider(lifetimeSlider, maxBin, model.getSelectedBin(),
                        LITEIME_SLIDER_DESCRIPTION, LIFETIME_SLIDER_TIPSTRING);
                lifetimeSlider.setPaintTicks(false);
                channelButtonPanel = new JPanel();
                channelButtonPanel.setBackground(UIUtilities.BACKGROUND_COLOR);
            } else channelButtonPanel = createChannelButtons();
        }
        graphicsPane.setSelectedPlane();
        channelsBox = new JComboBox();
        populateChannels();
        channelsBox.setRenderer(new ColorListRenderer());
        channelsBox.setActionCommand(""+CHANNEL);
        channelsBox.setVisible(model.getMaxC() > 1);
    }
    
    /** Populates the channels. */
    private void populateChannels()
    {
    	if (channelsBox == null) return;
    	List<ChannelData> channels = model.getChannelData();
    	Object[][] channelCols = new Object[channels.size()][2];
 		Iterator<ChannelData> i = channels.iterator();
 		ChannelData data;
 		int index = 0;
 		int selected = 0;
 		while (i.hasNext()) {
 			data = i.next();
 			channelCols[index] = new Object[]{
 					model.getChannelColor(data.getIndex()),
 					data.getChannelLabeling() };
 			if (data.getIndex() == model.getSelectedChannel())
 				selected = index;
 			index++;
 		}
 		channelsBox.setModel(new DefaultComboBoxModel(channelCols));
 		channelsBox.removeActionListener(this);
 		channelsBox.setSelectedIndex(selected);
 		channelsBox.addActionListener(this);
    }
    
    /** Resets the value of the bit resolution. */
    private void resetBitResolution()
    {
        int v = model.getBitResolution();
        bitDepthSlider.removeChangeListener(this);
        bitDepthSlider.setValue(convertBitResolution(v));
        bitDepthSlider.addChangeListener(this);
        bitDepthLabel.setText(""+v);
        bitDepthLabel.repaint();
    }
    
    /**
     * Creates the channel buttons on the left hand side of the histogram.
     * 
     * @return panel containing the buttons.
     */
    private JPanel createChannelButtons()
    {
        JPanel p = new JPanel();
        p.setBackground(UIUtilities.BACKGROUND_COLOR);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        List<ChannelData> data = model.getChannelData();
        boolean gs = model.isGreyScale();
        ChannelData d;
        //ChannelToggleButton item;
        ChannelButton item;
        p.add(Box.createRigidArea(VBOX));
        Dimension dMax = ChannelButton.DEFAULT_MIN_SIZE;
        Dimension dim;
        Iterator<ChannelData> i = data.iterator();
        int j;
        List<Integer> active = model.getActiveChannels();
        while (i.hasNext()) {
			d = i.next();
			j = d.getIndex();
			item = new ChannelButton(""+d.getChannelLabeling(), 
					model.getChannelColor(j), j);
			dim = item.getPreferredSize();
			if (dim.width > dMax.width) 
				dMax = new Dimension(dim.width, dMax.height);
			item.setBackground(UIUtilities.BACKGROUND_COLOR);
			channelList.add(item);
			item.setSelected(active.contains(j));
			item.setGrayedOut(gs);
			item.addPropertyChangeListener(controller);
			p.add(item);
			p.add(Box.createRigidArea(VBOX));
		}
 
        Iterator<ChannelButton> index = channelList.iterator();
        while (index.hasNext()) 
			index.next().setPreferredSize(dMax);

        JPanel controls = new JPanel();
        controls.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.VERTICAL;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(0, 2, 2, 0);
		c.gridy = 0;
		c.gridx = 0;
        controls.setBackground(UIUtilities.BACKGROUND_COLOR);
        if (model.isGeneralIndex()) {
        	colorModel.setVisible(true);
        	JToolBar bar = new JToolBar(JToolBar.VERTICAL);
        	bar.setBackground(colorModel.getBackground());
        	bar.setFloatable(false);
        	bar.setRollover(true);
        	bar.setBorder(null);
        	bar.add(colorModel);
        	c.anchor = GridBagConstraints.CENTER;
        	controls.add(bar, c);
        	c.gridy = c.gridy+2;
        	c.anchor = GridBagConstraints.WEST;
        }
        if (channelList.size() > Renderer.MAX_CHANNELS) 
        	controls.add(new JScrollPane(p), c);
        else controls.add(p, c);
        JPanel content = UIUtilities.buildComponentPanel(controls);
        content.setBackground(UIUtilities.BACKGROUND_COLOR);
        return content;  
    }
    
    /**
     * Creates a panel showing the channel buttons and histogram.
     *  
     * @return See above.
     */
    private JPanel buildChannelGraphicsPanel()
    {
    	JPanel p = new JPanel();
    	p.setBackground(UIUtilities.BACKGROUND_COLOR);
    	p.setLayout(new BorderLayout());
    	if (model.isGeneralIndex()) {
    		p.add(buildViewerPane(), BorderLayout.WEST);
    		p.add(graphicsPane, BorderLayout.SOUTH);
    		JPanel content = new JPanel();
    		content.setLayout(new BorderLayout());
    		content.setBackground(UIUtilities.BACKGROUND_COLOR);
    		content.add(p, BorderLayout.CENTER);
    		return content;
    	} 
    	p.add(graphicsPane, BorderLayout.CENTER);
    	taskPane.add(buildControlsPane());
    	p.add(taskPane, BorderLayout.SOUTH);
    	JPanel content = UIUtilities.buildComponentPanel(p);
    	content.setBackground(UIUtilities.BACKGROUND_COLOR);
    	return p;
    }
    
    /** 
     * Builds and lays out the component displaying the preview.
     * 
     * @return See above.
     */
    private JPanel buildViewerPane()
    {
    	JPanel p = new JPanel();
    	p.setBackground(UIUtilities.BACKGROUND_COLOR);
    	p.setLayout(new GridBagLayout());
    	GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.VERTICAL;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(0, 2, 2, 0);
		c.gridy = 0;
		c.gridx = 0;
		if (channelButtonPanel != null) {
			p.add(channelButtonPanel, c);
			c.gridx++;
		}
		p.add(zSlider, c);
		c.gridx++;
		p.add(canvas, c);
		c.gridy++;
		if (tSlider.isVisible()) p.add(tSlider, c);
		if (lifetimeSlider != null) {
		    c.gridy++;
		    p.add(lifetimeSlider, c);
		}
    	return p;
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
        graphicsPane.setSelectedPlane();
    }
    
    /**
     * Lays out the slider and its corresponding text area.
     * 
     * @param slider    The slider to lay out.
     * @param field     The text area.
     * @return A panel hosting the component.
     */
    private JPanel buildSliderPane(JSlider slider, JTextField field)
    {
        JPanel p = new JPanel();
        p.setBackground(UIUtilities.BACKGROUND_COLOR);
        p.add(slider);
        p.add(field);
        return UIUtilities.buildComponentPanel(p);
    }
    
    /**
     * Adds the specified component to the passed <code>Panel</code>.
     * 
     * @param c The layout constraints for the component to be added.
     * @param l The text corresponding to the component to be added.
     * @param comp The component to be added.
     * @param p The panel the component is added to.
     */
    private void addComponent(GridBagConstraints c, String l, JComponent comp,
    						JPanel p)
    {
    	c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0.0;
		c.gridx = 0;
		
		if (l != null && l.length() > 0) {
			p.add(new JLabel(l), c);
			c.gridx++;
			p.add(Box.createHorizontalStrut(5), c);
			c.gridx++;
		}
		
		c.gridwidth = GridBagConstraints.REMAINDER;     //end row
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;
		p.add(comp, c);
    }
    
    /**
     * Builds the pane hosting the main rendering controls.
     * 
     * @return See above.
     */
    private JPanel buildControlsPane()
    {
        JPanel p = new JPanel();
        p.setBackground(UIUtilities.BACKGROUND_COLOR);
        p.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(0, 2, 2, 0);
		c.gridy = 0;
		JPanel comp;
		comp = buildSliderPane(bitDepthSlider, bitDepthLabel);
		comp.setBackground(UIUtilities.BACKGROUND_COLOR);
		addComponent(c, "Brightness", graphicsPane.getCodomainSlider(), p);
		c.gridy++;
		comp = buildSliderPane(bitDepthSlider, bitDepthLabel);
		comp.setBackground(UIUtilities.BACKGROUND_COLOR);
		addComponent(c, "Bit Depth", comp, p);
		c.gridy++;
		addComponent(c, "", noiseReduction, p);
		c.gridx = 0;
		c.gridy++;
		comp = new SeparatorPane();
		comp.setBackground(UIUtilities.BACKGROUND_COLOR);
		p.add(comp, c);
		c.gridy++;
		
		if (channelsBox.isVisible()) {
			comp = UIUtilities.buildComponentPanel(channelsBox);
			comp.setBackground(UIUtilities.BACKGROUND_COLOR);
			addComponent(c, "Channel", comp, p);
			c.gridy++;
		}
		comp = UIUtilities.buildComponentPanel(familyBox);
		comp.setBackground(UIUtilities.BACKGROUND_COLOR);
		addComponent(c, "Map", comp, p);
		c.gridy++;
		comp = buildSliderPane(gammaSlider, gammaLabel);
		comp.setBackground(UIUtilities.BACKGROUND_COLOR);
		addComponent(c, "Gamma", comp, p);
        return p;
    }
    
    /** Builds and lays out the UI. */
    private void buildGUI()
    {
    	setBackground(UIUtilities.BACKGROUND_COLOR);
    	setLayout(new BorderLayout());
    	add(buildChannelGraphicsPanel(), BorderLayout.NORTH);
    }
    
    /**
     * Returns the bit resolution corresponding to the UI value.
     * 
     * @param uiValue   The UI value to convert.
     * @return See above.
     */
    private int convertUIBitResolution(int uiValue)
    {
        switch (uiValue) {
            case 1: return RendererModel.DEPTH_1BIT;
            case 2: return RendererModel.DEPTH_2BIT;
            case 3: return RendererModel.DEPTH_3BIT;
            case 4: return RendererModel.DEPTH_4BIT;
            case 5: return RendererModel.DEPTH_5BIT;
            case 6: return RendererModel.DEPTH_6BIT;
            case 7: return RendererModel.DEPTH_7BIT;
            case 8: 
            default: return RendererModel.DEPTH_8BIT;
        }
    }
    
    /**
     * Converts the bit resolution value into its corresponding UI value.
     * 
     * @param value The value to convert.
     * @return See above.
     */
    private int convertBitResolution(int value)
    {
        switch (value) {
            case RendererModel.DEPTH_1BIT: return 1;
            case RendererModel.DEPTH_2BIT: return 2;  
            case RendererModel.DEPTH_3BIT: return 3;
            case RendererModel.DEPTH_4BIT: return 4;
            case RendererModel.DEPTH_5BIT: return 5;
            case RendererModel.DEPTH_6BIT: return 6;
            case RendererModel.DEPTH_7BIT: return 7;
            case RendererModel.DEPTH_8BIT: 
            default: return 8;
        }
    }

    /**
     * Resets the value of the gamma slider and the gamma label.
     * 
     * @param k The value to set.
     */
    private void resetGamma(double k)
    {
    	gammaSlider.removeChangeListener(this);
        gammaSlider.setValue((int) (k*FACTOR));
        gammaSlider.addChangeListener(this);
        gammaLabel.setText(""+k);
    }
    
    /**
     * Handles the event when the wheel is moved over the 
     * {@link #lifetimeSlider}.
     * 
     * @param e The event to handle.
     */
    private void mouseWheelMovedLifetime(MouseWheelEvent e)
    {
        boolean up = true;
        if (e.getWheelRotation() > 0) up = false;
        if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
            int v = model.getRealSelectedT()-e.getWheelRotation();
            int bin = lifetimeSlider.getValue();
            if (up) {
                if (v <= model.getMaxLifetimeBin()) {
                	controller.setSelectedXYPlane(model.getDefaultZ(),
                			model.getRealSelectedT(), bin);
                }
            } else { //moving down
                if (v >= 0)
                	controller.setSelectedXYPlane(model.getDefaultZ(),
                			model.getRealSelectedT(), bin);
            }
        } else {
            
        }
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
        return "Define the mapping context for the pixels intensity values.";
    }

    /**
     * Returns the index of the component.
     * @see ControlPane#getPaneIndex()
     */
    protected int getPaneIndex() { return ControlPane.DOMAIN_PANE_INDEX; }
    
    /**
     * Resets the default rendering settings. 
     * @see ControlPane#resetDefaultRndSettings()
     */
    protected void resetDefaultRndSettings()
    {
        setInputInterval();
        setInputRange(false);
        setSelectedChannel();
        setCodomainInterval();
        resetBitResolution();
        int n = model.getMaxC();
        for (int i = 0; i < n; i++) {
        	setChannelColor(i);
		}
        resetGamma(model.getCurveCoefficient());
        setZSection(model.getDefaultZ());
        setTimepoint(model.getDefaultT());
    }
    
    /**
     * Sets the enabled flag of the UI components.
     * @see ControlPane#onStateChange(boolean)
     */
	protected void onStateChange(boolean b)
	{
		if (familyBox != null) familyBox.setEnabled(b);
		if (gammaSlider != null) {
			String family = model.getFamily();
			boolean enabled = family.equals(RendererModel.EXPONENTIAL) || 
	                        family.equals(RendererModel.POLYNOMIAL);
			gammaSlider.setEnabled(enabled);
			gammaLabel.setEnabled(enabled);
		}
		if (bitDepthSlider != null) bitDepthSlider.setEnabled(b);
		if (noiseReduction != null) noiseReduction.setEnabled(b);
		if (channelList != null) {
			Iterator<ChannelButton> i = channelList.iterator();
			while (i.hasNext()) 
				(i.next()).setEnabled(b);
		}
		graphicsPane.onStateChange(b);
	}
	
    /**
     * Resets the value of the various controls when the user selects 
     * a new rendering control
     * @see ControlPane#resetDefaultRndSettings()
     */
    protected void switchRndControl() {}
    
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
        initComponents();
        buildGUI();
    }
    
    /**
     * Modifies the rendering controls depending on the currently selected
     * channel.
     */
    void setSelectedChannel()
    {
        graphicsPane.setSelectedChannel();
        Iterator<ChannelButton> i = channelList.iterator();
        ChannelButton btn;
        List<Integer> active = model.getActiveChannels();
        if (active == null) return;
        int index;
        int c = model.getSelectedChannel();
        boolean gs = model.isGreyScale();
        while (i.hasNext()) {
			btn = i.next();
			index = btn.getChannelIndex();
			btn.setSelected(active.contains(index));
			if (index == c && !model.isGeneralIndex()) 
				btn.setBorder(SELECTION_BORDER);
			btn.setGrayedOut(gs);
			btn.setColor(model.getChannelColor(index));
		}
    }
    
    /** Sets the pixels intensity interval. */
    void setInputInterval() { graphicsPane.setInputInterval(); }
    
    /** 
     * Modifies the input range of the channel sliders. 
     * 
     *  @param absolute Pass <code>true</code> to set it to the absolute value,
	 *  				<code>false</code> to the minimum and maximum.
	 */
	void setInputRange(boolean booleanValue)
	{
		graphicsPane.setInputRange(booleanValue);
	}
	
    /** Sets the value of the codomain interval. */
    void setCodomainInterval() { graphicsPane.setCodomainInterval(); }
    
    /**
     * Sets the color of the passed channel.
     *  
     * @param index The index of the channel.
     */
    void setChannelColor(int index)
    {
    	Iterator<ChannelButton> i = channelList.iterator();
    	ChannelButton btn;
    	boolean gs = model.isGreyScale();
    	while (i.hasNext()) {
			btn = i.next();
			if (index == btn.getChannelIndex()) {
				 btn.setColor(model.getChannelColor(index));
				 if (gs) btn.setGrayedOut(gs);
			}
		}
    	graphicsPane.setChannelColor(index);
    	if (channelsBox != null) populateChannels();
    }
    
    /** Toggles between color model and Greyscale. */
    void setColorModelChanged() 
    {
        ChannelButton btn;
        boolean gs = model.isGreyScale();
        int index;
        int selected = model.getSelectedChannel();
        for (int i = 0 ; i < channelList.size() ; i++) {
            btn = channelList.get(i);
            index = btn.getChannelIndex();
            btn.setColor(model.getChannelColor(index));
            btn.setGrayedOut(gs);
            btn.setSelected(model.isChannelActive(index));
            if (index == selected && !model.isGeneralIndex()) 
            	btn.setBorder(SELECTION_BORDER);
        }
        graphicsPane.setColorModelChanged();
    }
    
    /** 
     * Updates the UI when a new curve is selected i.e. when a new family
     * is selected or when a new gamma value is selected.
     */
    void onCurveChange()
    { 
        String f = model.getFamily();
        boolean b = !(f.equals(RendererModel.LINEAR) || 
        			f.equals(RendererModel.LOGARITHMIC));
        double k = 1;
        if (b) k = model.getCurveCoefficient();
        resetGamma(k);
        gammaSlider.setEnabled(b);
        gammaLabel.setEnabled(b);
        graphicsPane.onCurveChange(); 
    }
    
    /**
     * Updates UI components when a new time-point is selected.
     * 
     * @param t The selected time-point.
     */
    void setTimepoint(int t)
    { 
    	if (tSlider != null) updateSlider(tSlider, t); 
    }
    
    /**
     * Updates UI components when a new z-section is selected.
     * 
     * @param z The selected z-section.
     */
    void setZSection(int z)
    { 
    	if (zSlider != null) updateSlider(zSlider, z);
    }

	/**
	 * Returns <code>true</code> if the passed object is one of the
	 * channel buttons, <code>false</code> otherwise.
	 * 
	 * @param source The object to handle.
	 * @return See above.
	 */
	boolean isSourceDisplayed(Object source)
	{
		Iterator<ChannelButton> i = channelList.iterator();
		while (i.hasNext()) {
			if (i.next() == source) return true;
		}
		return false;
	} 
	
	 /** Renders and displays the rendered image in the preview. */
	void renderPreview()
	{
		if (canvas == null) return;
		BufferedImage img = model.renderImage();
		if (img == null) return;
		Dimension d = model.getPreviewDimension();
		img = Factory.scaleBufferedImage(img, d.width, d.height);
		canvas.setPreferredSize(d);
		canvas.setSize(d);
		canvas.setImage(img);
	}
	
    /** 
     * Builds and lays out the images as seen by other experimenters.
     *  
     * @param results The thumbnails to lay out.
     * @param activeRndDef The rendering setting which is currently used
     */
    void displayViewedBy(List<ViewedByItem> results, RndProxyDef activeRndDef)
    {
    	graphicsPane.displayViewedBy(results, activeRndDef);
    }
    
    /**
     * Updates the component displaying the channels' details after update.
     */
    void onChannelUpdated()
    {
    	populateChannels();
    	Iterator<ChannelButton> i = channelList.iterator();
    	ChannelButton cb;
    	List<ChannelData> channels = model.getChannelData();
    	ChannelData data;
    	while (i.hasNext()) {
			cb = i.next();
			data = channels.get(cb.getChannelIndex());
			cb.setText(data.getChannelLabeling());
		}
    }
    
    /**
     * Depending on the source of the event. Sets the gamma value or
     * the bit resolution.
     * @see ChangeListener#stateChanged(ChangeEvent)
     */
    public void stateChanged(ChangeEvent e)
    {
        Object source = e.getSource();
        if (source.equals(gammaSlider)) {
            gammaLabel.setText(""+(double) gammaSlider.getValue()/FACTOR);
        } else if (source.equals(bitDepthSlider)) {
            bitDepthLabel.setText(""+
            		convertUIBitResolution(bitDepthSlider.getValue()));
        } else if (source.equals(tSlider) || source.equals(zSlider)) {
            if (lifetimeSlider != null && lifetimeSlider.isVisible()) {
                controller.setSelectedXYPlane(model.getDefaultZ(),
                        tSlider.getValue(), lifetimeSlider.getValue());
            } else controller.setSelectedXYPlane(zSlider.getValue(),
                    tSlider.getValue());
        } else if (source.equals(lifetimeSlider)) {
        	controller.setSelectedXYPlane(model.getDefaultZ(),
                    model.getRealSelectedT(), lifetimeSlider.getValue());
        	graphicsPane.setSelectedPlane();
        }
    }
    
    /**
     * Reacts to family or channel selection.
     * @see ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        int index = -1;
        index = Integer.parseInt(e.getActionCommand());
        int v = channelsBox.getSelectedIndex();
        try {
            switch (index) {
            case FAMILY:
                String f = (String) 
                ((JComboBox) e.getSource()).getSelectedItem();
                controller.setChannelFamily(v, f);
                break;
            case CHANNEL:
                //Set the family
                String family = model.getFamily(v);
                double coefficient = model.getCurveCoefficient(v);
                familyBox.removeActionListener(this);
                familyBox.setSelectedItem(family);
                familyBox.addActionListener(this);
                //set the gamma.
                boolean enabled = family.equals(RendererModel.EXPONENTIAL) ||
                        family.equals(RendererModel.POLYNOMIAL);
                gammaSlider.removeChangeListener(this);
                gammaSlider.setValue((int) (coefficient*FACTOR));
                gammaSlider.setEnabled(enabled);
                gammaSlider.addChangeListener(this);
                gammaLabel.setText(""+coefficient);
                gammaLabel.setEnabled(enabled);
                controller.setChannelSelection(v, true);
            }
        } catch(NumberFormatException nfe) {  
            throw new Error("Invalid Action ID "+index, nfe);
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
        if (source == zSlider && zSlider.isEnabled()) mouseWheelMovedZ(e);
        else if (source == tSlider && tSlider.isEnabled())
            mouseWheelMovedT(e);
        else if (source == lifetimeSlider && lifetimeSlider.isEnabled())
            mouseWheelMovedLifetime(e);
	}

}