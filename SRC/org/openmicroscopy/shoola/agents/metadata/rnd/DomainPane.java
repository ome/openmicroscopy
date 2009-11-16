/*
 * org.openmicroscopy.shoola.agents.metadata.rnd.DomainPane 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

//Third-party libraries
import layout.TableLayout;
import org.jdesktop.swingx.JXTaskPane;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.agents.metadata.actions.NoiseReductionAction;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.agents.util.ui.ChannelButton;
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
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
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
    static final int            	MIN_GAMMA = 1;
    
    /** 
     * For slider control only. The maximum value for the curve coefficient. 
     * The real value is divided by 10.
     */
    static final int            	MAX_GAMMA = 40;
    
    /** 
     * For slider control only. The default value for the curve coefficient. 
     * The real value is divided by 10.
     */
    static final int            	DEFAULT_GAMMA = 10;
 
    /** The minimum value of the bit resolution. */ 
    static final int            	MIN_BIT_DEPTH = 1;
    
    /** The maximum value of the bit resolution. */ 
    static final int            	MAX_BIT_DEPTH = 8;
    
    /** The default value of the bit resolution. */ 
    static final int            	DEFAULT_BIT_DEPTH = 8;
    
    /** The border of the selected channel. */
    private static final Border		SELECTION_BORDER = 
    	BorderFactory.createLineBorder(Color.BLACK, 3);
    
    /** The factor .*/
    private static final int    	FACTOR = 10;
    
    /** Identifies the <code>Family</code> selection. */
    private static final int    	FAMILY = 0;
   
    /** Dimension of the box between the channel buttons. */
    private static final Dimension 	VBOX = new Dimension(1, 10);
       
    /** Title of the advanced options. */
    private static final String		ADVANCED_OPTIONS = "Advanced"; 
    
    /** The description of a z-sections selection slider. */
    private static final String 	Z_SLIDER_DESCRIPTION = 
    								"Select a z-section.";

    /** The description of a time-point selection slider. */
    private static final String 	T_SLIDER_DESCRIPTION = 
    								"Select a timepoint.";
    
    /** The tipString of the {@link #zSlider}. */
    private static final String 	Z_SLIDER_TIPSTRING = "Z";

    /** The tipString of the {@link #tSlider}. */
    private static final String 	T_SLIDER_TIPSTRING = "T";
    
    /** Box to select the family used in the mapping process. */
    private JComboBox       			familyBox;

    /** 
     * A collection of ColourButtons which represent the channel selected 
     * in the mapping process. 
     */
    private List<ChannelButton>			channelList;
    
    /** A panel containing the channel buttons. */
    private JPanel						channelButtonPanel;
    
    /** Slider to select a curve in the family. */
    private OneKnobSlider         		gammaSlider;
    
    /** Slider to select the bit resolution of the rendered image. */
    private OneKnobSlider        	 	bitDepthSlider;
    
    /** Field displaying the <code>Gamma</code> value. */
    private JTextField      			gammaLabel;
    
    /** Field displaying the <code>Bit Depth</code> value. */
    private JTextField      			bitDepthLabel;
    
    /** Box to select the mapping algorithm. */
    private JCheckBox       			noiseReduction;
    
    /** Button to bring up the histogram widget on screen. */
    private JButton         			histogramButton;
    
    /** The UI component hosting the interval selections. */
    private GraphicsPane    			graphicsPane;
      
    /** The component hosting the various options. */
    private JXTaskPane					taskPane;
    
    /** Button to modify the color model. */
    private JButton						colorModel;
    
    /** Selects the z-section. */
    private OneKnobSlider				zSlider;
    
    /** Selects the time-point. */
    private OneKnobSlider				tSlider;
    
    /** The label displaying the selected plane. */
    private JLabel						selectedPlane;
    
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
        } else {
     
        }
        setSelectedPlaneLabel();
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
        } else {
            
        }
        setSelectedPlaneLabel();
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
        
        double k = model.getCurveCoefficient();
        gammaSlider = new OneKnobSlider(JSlider.HORIZONTAL, MIN_GAMMA, 
        							MAX_GAMMA, (int) (k*FACTOR));
        gammaSlider.setBackground(UIUtilities.BACKGROUND_COLOR);
        gammaSlider.setShowArrows(false);
        gammaSlider.setEnabled(family.equals(RendererModel.EXPONENTIAL) || 
                family.equals(RendererModel.POLYNOMIAL));
        gammaSlider.addChangeListener(this);
        gammaSlider.addMouseListener(new MouseAdapter() {
    		
			public void mouseReleased(MouseEvent e) {
				double v = (double) gammaSlider.getValue()/FACTOR;
	            gammaLabel.setText(""+v);
	            firePropertyChange(GAMMA_PROPERTY, 
	            			new Double(model.getCurveCoefficient()), 
	                        new Double(v));
			}
		
		});
        gammaLabel = new JTextField(""+k);
        gammaLabel.setBackground(UIUtilities.BACKGROUND_COLOR);
        gammaLabel.setEnabled(false);
        gammaLabel.setEditable(false);
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
        if (model.getMaxC() < Renderer.MAX_CHANNELS)
        	channelButtonPanel = createChannelButtons();
        else {
        	channelButtonPanel = new JPanel();
        	channelButtonPanel.setBackground(UIUtilities.BACKGROUND_COLOR);
        }
        int maxZ = model.getMaxZ()-1;
        int maxT = model.getMaxT()-1;
        zSlider = new OneKnobSlider(OneKnobSlider.HORIZONTAL, 0, 1, 0);
        zSlider.setEnabled(false);
        tSlider = new OneKnobSlider(OneKnobSlider.HORIZONTAL, 0, 1, 0);
        tSlider.setEnabled(false);
        initSlider(tSlider, maxT, model.getDefaultT(), 
        		T_SLIDER_DESCRIPTION, T_SLIDER_TIPSTRING);
        
        initSlider(zSlider, maxZ, model.getDefaultZ(), 
        			Z_SLIDER_DESCRIPTION, Z_SLIDER_TIPSTRING);
        selectedPlane = new JLabel();
        setSelectedPlaneLabel();
    }
    
    /** Indicates the selected plane. */
    private void setSelectedPlaneLabel()
    {
    	String s = "Z="+(model.getDefaultZ()+1)+"/"+model.getMaxZ();
    	s += " T="+(model.getDefaultT()+1)+"/"+model.getMaxT();
    	selectedPlane.setText(s);
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
        double size[][] = {{TableLayout.PREFERRED}, 
        				{TableLayout.PREFERRED, 5, TableLayout.PREFERRED}};
        controls.setLayout(new TableLayout(size));
        controls.setBackground(UIUtilities.BACKGROUND_COLOR);
        int k = 0;
        if (model.isGeneralIndex()) {
        	 colorModel.setVisible(true);
        	 JToolBar bar = new JToolBar(JToolBar.VERTICAL);
        	 bar.setBackground(colorModel.getBackground());
             bar.setFloatable(false);
             bar.setRollover(true);
             bar.setBorder(null);
             bar.add(colorModel);
        	 controls.add(bar, "0, "+k+", c, c");
             k = k+2;
        }
        if (channelList.size() > Renderer.MAX_CHANNELS) 
        	controls.add(new JScrollPane(p), "0, "+k);
        else controls.add(p, "0, "+k);
        
        
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
    	if (channelButtonPanel != null)
    		p.add(channelButtonPanel, BorderLayout.WEST);
    	p.add(graphicsPane, BorderLayout.CENTER);
    	if (model.isGeneralIndex()) 
    		p.add(buildPlaneSelectionPanel(), BorderLayout.SOUTH);
    		
    	return p;
    }
    
    /**
     * Builds the panel hosting the sliders used to select
     * the 2D-plane.
     * 
     * @return See above.
     */
    private JPanel buildPlaneSelectionPanel()
    {
    	JPanel p = new JPanel();
    	p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
    	p.add(new JSeparator(JSeparator.HORIZONTAL));
    	p.setBackground(UIUtilities.BACKGROUND_COLOR);
    	p.add(zSlider);
    	p.add(tSlider);
    	JPanel l = UIUtilities.buildComponentPanel(selectedPlane);
    	l.setBackground(UIUtilities.BACKGROUND_COLOR);
    	p.add(l);
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
        setSelectedPlaneLabel();
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
     * @param c		The layout contraints for the component to be added.
     * @param l		The text corresponding to the component to be added.
     * @param comp	The component to be added.
     * @param p		The panel the component is added to.
     */
    private void addComponent(GridBagConstraints c, String l, JComponent comp, 
    						JPanel p)
    {
    	c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
		c.fill = GridBagConstraints.NONE;      //reset to default
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
		JPanel comp = UIUtilities.buildComponentPanel(familyBox);
		comp.setBackground(UIUtilities.BACKGROUND_COLOR);
		addComponent(c, "Map", comp, p);
		c.gridy++;
		comp = buildSliderPane(gammaSlider, gammaLabel);
		comp.setBackground(UIUtilities.BACKGROUND_COLOR);
		addComponent(c, "Gamma", comp, p);
		c.gridy++;
		comp = buildSliderPane(bitDepthSlider, bitDepthLabel);
		comp.setBackground(UIUtilities.BACKGROUND_COLOR);
		addComponent(c, "Bit Depth", comp, p);
		c.gridy++;
		c.gridx = 0;
		comp = new SeparatorPane();
		comp.setBackground(UIUtilities.BACKGROUND_COLOR);
		p.add(comp, c);
		c.gridy++;
		addComponent(c, "", noiseReduction, p);
		c.gridy++;
        return p;
    }
    
    /** Builds and lays out the UI. */
    private void buildGUI()
    {
    	setBackground(UIUtilities.BACKGROUND_COLOR);
    	double size[][] = {{TableLayout.FILL},  // Columns
         {TableLayout.PREFERRED, 5, TableLayout.PREFERRED}}; // Rows
    	setLayout(new TableLayout(size));
   
    	taskPane.add(buildControlsPane());
    	add(buildChannelGraphicsPanel(), "0, 0");
		add(taskPane, "0, 2");
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
            default:
                    return RendererModel.DEPTH_8BIT;
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
            default:
                    return 8;
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
        setSelectedChannel();
        setCodomainInterval();
        resetBitResolution();
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
			gammaSlider.setEnabled(b);
			gammaSlider.setEnabled(family.equals(RendererModel.EXPONENTIAL) || 
	                family.equals(RendererModel.POLYNOMIAL));
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
        String f = model.getFamily();
        familyBox.removeActionListener(this);
        familyBox.setSelectedItem(f);
        familyBox.addActionListener(this);
        boolean b = !(f.equals(RendererModel.LINEAR) || 
    			f.equals(RendererModel.LOGARITHMIC));
        double k = 1;
        if (b) k = model.getCurveCoefficient();
        resetGamma(k);
        gammaSlider.setEnabled(b);
        noiseReduction.removeActionListener(
                controller.getAction(RendererControl.NOISE_REDUCTION));
        noiseReduction.setSelected(model.isNoiseReduction());
        noiseReduction.addActionListener(
                controller.getAction(RendererControl.NOISE_REDUCTION));
        noiseReduction.setText(NoiseReductionAction.NAME);
        
        Iterator<ChannelButton> i = channelList.iterator();
        ChannelButton btn;
        List<Integer> active = model.getActiveChannels();
        int index;
        int c = model.getSelectedChannel();
        boolean gs = model.isGreyScale();
        while (i.hasNext()) {
			btn = i.next();
			index = btn.getChannelIndex();
			btn.setSelected(active.contains(index));
			if (index == c) btn.setBorder(SELECTION_BORDER);
			btn.setGrayedOut(gs);
			btn.setColor(model.getChannelColor(index));
		}
    }
    
    /** Sets the pixels intensity interval. */
    void setInputInterval() { graphicsPane.setInputInterval(); }
    
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
    	graphicsPane.repaint();
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
            if (index == selected) btn.setBorder(SELECTION_BORDER);
        }
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
    
    /** Updates the display when the settings have been updated. */
	void onSettingsApplied() { graphicsPane.onSettingsApplied(); }
	
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
        	controller.setSelectedXYPlane(zSlider.getValue(), 
                    tSlider.getValue());
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
        try {
            switch (index) {
                case FAMILY:
                    String f = (String) 
                            ((JComboBox) e.getSource()).getSelectedItem();
                    firePropertyChange(FAMILY_PROPERTY, model.getFamily(), f);
                    break;
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
	}
	
}
