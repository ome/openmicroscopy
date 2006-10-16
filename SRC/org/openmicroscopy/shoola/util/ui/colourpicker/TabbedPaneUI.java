/*
 * org.openmicroscopy.shoola.util.ui.colourpicker.TabbedPaneUI
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
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.util.ui.colourpicker;

//Java imports
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.IconManager;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */

class TabbedPaneUI
	extends JPanel
	implements ChangeListener
{
	
	/** Used by card layout to select colour wheel panel. */
	static final String COLOURWHEELPANE = "Colour Wheel Pane"; 	
	
	/** Used by card layout to select RGB Slider panel. */
	static final String RGBSLIDERPANE = "RGB Slider Pane"; 	
	
	/** Used by card layout to select swatch panel. */
	static final String SWATCHPANE = "Swatch Pane"; 	
	
	/**
	 * Toolbar contains the buttons to select the HSVWheelUI, RGB Selector
	 * or Colour Swatch.
	 */
	private JToolBar 		toolbar;
	
	/**
	 * Actionbar contains the buttons to accept, cancel or revert to the 
	 * original colour selection.
	 */
	private JToolBar 		userActionbar;
	
	/** Button to choose HSVColourWheelPanel. */
	private JToggleButton 	colourWheelButton;
	
	/** Button to choose RGB Sliders panel. */
	private JToggleButton	RGBSlidersButton;
	
	/** Button to choose colour swatch panel. */
	private JToggleButton	colourSwatchButton;
	
	/** Accept the current colour choice. */
	private JButton			acceptButton;

	/** Revert to the original colour chosen by the user. */
	private JButton			revertButton;
	
	/** Cancel the colour panel. */
	private JButton			cancelButton;
	
	/** Colourwheel button action when clicked will make colourwheel visible. */
	private Action 			colourWheelAction;
	
	/** Sliders button action when clicked will make RGBsliders visible. */
	private Action 			RGBSlidersAction;
	
	/** Swatch button action when clicked will make ColourSwatch visible. */
	private Action 			colourSwatchAction;
	
	/** Cancel icon displayed on button.  */
	private Action			cancelButtonAction;
	
	/** Accept icon displayed on button. */
	private Action			acceptButtonAction;
	
	/** Revert icon displayed on button. */
	private Action			revertButtonAction;
	
	/** ColourWheel panel, containing the HSVPickerUI. */
	private HSVColourWheelUI colourWheelPane;
	
	/** RGBPanel containing the ColourSlider UI. */
	private RGBSliderUI 	RGBSliderPane;
	
	/** Containing the Swatch UI. */
	private ColourSwatchUI 	swatchPane;
	
	/** Layout manager for the colourwheel, slider and swatch panels. */
	private CardLayout 		tabPaneLayout;
	
	/** 
	 * Container for the layout manager above. Containers colourwheel, slider 
	 * and swatch panels.
	 */
	private JPanel			tabPanel;
	
	/**
	 * Paintpot pane will be displayed at the top of the window, above selected
	 * pane and below toolbar.
	 */
	private PaintPotUI  	paintPotPane;

    /** Model which will be changed when user adjusts sliders/textfield. */
    private RGBControl      control;
    
    /** The owner of this component. */
    private ColourPicker    parent;
    
    /** 
     * The toolbar controls which panel is active, the user has the choice
     * of HSV Colour wheel, RGB Sliders and colour swatches.
     * Create the toolbar and its buttons, add actions to the buttons and load
     * the button icons, attach the buttons to the tool bar. 
     */
    private void createToolbar()
    {
        toolbar = new JToolBar();
        IconManager icons = IconManager.getInstance();
        colourWheelButton = new JToggleButton(
                icons.getIcon(IconManager.COLOUR_WHEEL));
        UIUtilities.unifiedButtonLookAndFeel(colourWheelButton);
        colourWheelButton.setBorderPainted(true);
        colourWheelButton.setToolTipText("Show HSV Colour Wheel");
        colourWheelAction = new AbstractAction("HSV Wheel Colour Button") {
            public void actionPerformed(ActionEvent evt) 
            {
                clearToggleButtons();
                pickWheelPane();
            }
        };
        colourWheelButton.addActionListener(colourWheelAction);
        RGBSlidersButton = new JToggleButton(
                icons.getIcon(IconManager.COLOUR_SLIDER));
        UIUtilities.unifiedButtonLookAndFeel(RGBSlidersButton);
        RGBSlidersButton.setBorderPainted(true);
        RGBSlidersButton.setToolTipText("Show RGB Colour Sliders");
        
    
        RGBSlidersAction = new AbstractAction("RGB Slider Button") 
        {
            public void actionPerformed(ActionEvent evt) 
            {
                clearToggleButtons();
                pickRGBSliderPane();
            }
        };
        RGBSlidersButton.addActionListener(RGBSlidersAction);
        colourSwatchButton = new JToggleButton(
                icons.getIcon(IconManager.COLOUR_SWATCH));
        colourSwatchButton.setToolTipText("Show Colour List");
        UIUtilities.unifiedButtonLookAndFeel(colourSwatchButton);
        colourSwatchButton.setBorderPainted(true);
        
        colourSwatchAction = new AbstractAction("Colour Swatch Button")
        {
            public void actionPerformed(ActionEvent evt) 
            {
                clearToggleButtons();
                pickSwatchPane();
            }
        };
        colourSwatchButton.addActionListener(colourSwatchAction);
        toolbar.setFloatable(false);
        toolbar.setRollover(true);
        toolbar.add(colourWheelButton);
        toolbar.add(RGBSlidersButton);
        toolbar.add(colourSwatchButton);
    }
    
    /** 
     * The action bar controls if the user wishes to as accept current colour
     * choice, close the colour picker window or revert to the original colour
     * selected when the colour picker was loaded. 
     * 
     * Creates action bar, its buttons and load icons. Attach action events
     * to buttons and buttons to bar. 
     */
    private void createActionbar()
    {
        userActionbar = new JToolBar();
        IconManager icons = IconManager.getInstance();
        acceptButton = new JButton(icons.getIcon(IconManager.OK));
        acceptButton.setToolTipText("Accept Current Colour");
        UIUtilities.unifiedButtonLookAndFeel(acceptButton);
        acceptButton.setBorderPainted(true);
        acceptButtonAction = new AbstractAction("Accept Button Action") 
        {
            public void actionPerformed(ActionEvent evt) { parent.accept(); }
        };
        acceptButton.addActionListener(acceptButtonAction);
        
        revertButton = new JButton(icons.getIcon(IconManager.UNDO));
        revertButton.setToolTipText("Revert to Original Colour");
        UIUtilities.unifiedButtonLookAndFeel(revertButton);
        revertButton.setBorderPainted(true);
        revertButtonAction = new AbstractAction("Revert Button Action") 
        {
            public void actionPerformed(ActionEvent evt) 
            {
                revertAction();
            }
        };
        revertButton.addActionListener(revertButtonAction);
        
        cancelButton = new JButton(icons.getIcon(IconManager.CANCEL));
        cancelButton.setToolTipText("Cancel Selection and Close Colour Picker");
        UIUtilities.unifiedButtonLookAndFeel(cancelButton);
        cancelButton.setBorderPainted(true);
        cancelButtonAction = new AbstractAction("Cancel Button Action") 
        {
            public void actionPerformed(ActionEvent evt) 
            {
                parent.cancel();
            }
        };
        cancelButton.addActionListener(cancelButtonAction);
        
        userActionbar.setFloatable(false);
        userActionbar.setRollover(true);
        userActionbar.add(acceptButton);
        userActionbar.add(revertButton);
        userActionbar.add(cancelButton);
    }

    /**
     * Creates PaintPotUI, RGB slider panel and HSVWheel panel + Colour
     * Swatch panel.
     */
    private void createPanels()
    {
        colourWheelPane = new HSVColourWheelUI(control);
        paintPotPane = new PaintPotUI(control);
        RGBSliderPane = new RGBSliderUI(control);
        swatchPane = new ColourSwatchUI(control);
    }
    
    /** Creates all the UI elements and display the HSVWheel as active. */
    private void createUI()
    {
        createToolbar();
        createActionbar();
        createPanels();
                
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.X_AXIS));
        container.add(toolbar);
        container.add(Box.createHorizontalBox());
        container.add(userActionbar, BorderLayout.EAST);
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(container);
        paintPotPane.setPreferredSize(new Dimension(260,24));
        add(Box.createVerticalStrut(5));
        add(paintPotPane);
        add(Box.createVerticalStrut(5));
        tabPanel = new JPanel();
        tabPaneLayout = new CardLayout();
        tabPanel.setLayout(tabPaneLayout);
        tabPanel.add(colourWheelPane,COLOURWHEELPANE);
        tabPanel.add(RGBSliderPane,RGBSLIDERPANE);
        tabPanel.add(swatchPane,SWATCHPANE);
        add(tabPanel);
        colourWheelPane.setActive(true);
        RGBSliderPane.setActive(false); 
        
    }
    
    /** Clear all buttons. */
    private void clearToggleButtons()
    {
        colourWheelButton.setSelected(false);
        RGBSlidersButton.setSelected(false);
        colourSwatchButton.setSelected(false);
    }
    
    
    /** Sets Wheelbutton as picked and make it visible. */
    private void pickWheelPane()
    {   
        colourWheelButton.setSelected(true);
        colourWheelPane.setActive(true);

        tabPaneLayout.show(tabPanel,COLOURWHEELPANE);
        RGBSliderPane.setActive(false);
        colourWheelPane.findPuck();
        colourWheelPane.refresh();
        colourWheelPane.repaint();
    }
    
    /** Sets swatch as picked and makes it visible. */
    private void pickSwatchPane()
    {   
        tabPaneLayout.show(tabPanel,SWATCHPANE);
        colourSwatchButton.setSelected(true);
        RGBSliderPane.setActive(false);
        colourWheelPane.setActive(false);
        this.doLayout();
    }
    
    /** Sets RGBSlider as picked and makes it visible. */
    private void pickRGBSliderPane()
    {
        tabPaneLayout.show(tabPanel,RGBSLIDERPANE);
        RGBSlidersButton.setSelected(true);
        RGBSliderPane.setActive(true);
        colourWheelPane.setActive(false);
        
        this.doLayout();
        RGBSliderPane.refresh();
    }
    
	/**
	 * Instantiates the tabbed pane, create the UI and set the control to c.
	 * 
     * @param parent The parent of this component. Mustn't be <code>null</code>.
	 * @param c      Reference to the control. Mustn't be <code>null</code>.
	 */
	TabbedPaneUI(ColourPicker parent, RGBControl c)
	{
        if (parent == null)
            throw new NullPointerException("No parent.");
        if (c == null)
            throw new NullPointerException("No control.");
        this.parent = parent;
		control = c;
		createUI();
		control.addListener(this);
	}
	
	/** 
	 * User has clicked revert button. Revert current colour to the original 
	 * colour choice passed to Colourpicker.
	 */
	void revertAction()
	{
		control.revert();
	}

	/** 
	 * Listens to ChangeEvent. 
	 * @see ChangeListener#stateChanged(ChangeEvent)
	 */
	public void stateChanged(ChangeEvent arg0) 
	{
		if (RGBSliderPane != null)
			if( RGBSliderPane.isVisible())
				RGBSliderPane.refresh();
		if (colourWheelPane != null)
			if( colourWheelPane.isVisible())
				colourWheelPane.refresh();
	}

}
