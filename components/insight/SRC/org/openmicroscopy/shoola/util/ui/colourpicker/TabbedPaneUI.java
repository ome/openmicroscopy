/*
 * org.openmicroscopy.shoola.util.ui.colourpicker.TabbedPaneUI
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2016 University of Dundee. All rights reserved.
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

package org.openmicroscopy.shoola.util.ui.colourpicker;

//Java imports
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import info.clearthought.layout.TableLayout; 


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.CommonsLangUtils;
import org.openmicroscopy.shoola.util.ui.IconManager;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * The TabbedPaneUI controls the allows the switching between the different 
 * views in the colour picker.  
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
	implements ActionListener, ChangeListener, DocumentListener
{
	
	/** The number of column of the label displaying the alpha value. */
	static final int			TEXTBOX_COLUMN = 2;
	
	/** Used by card layout to select colour wheel panel. */
	private static final String COLOURWHEELPANE = "Color Wheel Pane";
	
	/** Used by card layout to select swatch panel. */
	private static final String SWATCHPANE = "Swatch Pane";
    
	/** Action id to preview the color changes.*/
	private static final int	PREVIEW = 0;
	
	/** Action id to cancel and close the dialog.*/
	private static final int	CANCEL = 1;
	
	/** Action id to accept the color changes.*/
	private static final int	ACCEPT = 2;
	
	/** Action id to revert the color changes.*/
	private static final int	REVERT = 3;
	
	/**
	 * Toolbar contains the buttons to select the HSVWheelUI, RGB Selector
	 * or Colour Swatch.
	 */
	private JToolBar 			toolbar;
	
	/**
	 * Actionbar contains the buttons to accept, cancel or revert to the 
	 * original colour selection.
	 */
	private JPanel 				userActionPanel;
	
	/** Button to choose HSVColourWheelPanel. */
	private JToggleButton 		colourWheelButton;
	
	/** Button to choose colour swatch panel. */
	private JToggleButton		colourSwatchButton;
	
	/** Accept the current colour choice. */
	private JButton				acceptButton;
	
	/** Accept the current colour choice. */
	private JButton				previewButton;

	/** Revert to the original colour chosen by the user. */
	private JButton				revertButton;
	
	/** Cancel the colour panel. */
	private JButton				cancelButton;
	
	/** The deescrption of the color. */
	private JTextField			fieldDescription;
	
	/** ColourWheel panel, containing the HSVPickerUI. */
	private HSVColourWheelUI	colourWheelPane;
	
	/** Containing the Swatch UI. */
	private ColourSwatchUI 		swatchPane;

	/** Layout manager for the colourwheel, slider and swatch panels. */
	private CardLayout 			tabPaneLayout;
	
	/** 
	 * Container for the layout manager above. Containers colourwheel, slider 
	 * and swatch panels.
	 */
	private JPanel				tabPanel;
	
	/**
	 * Paintpot pane will be displayed at the top of the window, above selected
	 * pane and below toolbar.
	 */
	private PaintPotUI  		paintPotPane;

    /** Model which will be changed when user adjusts sliders/textfield. */
    private RGBControl      	control;
    
    /** The owner of this component. */
    private ColourPicker    	parent;
    
    /** The original description of the color. */
    private String				originalDescription;
    
    /** Flag indicating that the preview button was clicked.*/
    private boolean preview;
    
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
        icons.getIcon(IconManager.COLOUR_WHEEL_24));
        UIUtilities.unifiedButtonLookAndFeel(colourWheelButton);
        colourWheelButton.setBorderPainted(true);
        colourWheelButton.setToolTipText("Show HSV Color Wheel.");
        AbstractAction action = new AbstractAction("HSV Wheel Color Button") {
            public void actionPerformed(ActionEvent evt) 
            {
                clearToggleButtons();
                pickWheelPane();
            }
        };
        
        colourWheelButton.addActionListener(action);

        colourSwatchButton = new JToggleButton(
        icons.getIcon(IconManager.COLOUR_SWATCH_24));
        colourSwatchButton.setToolTipText("Show Color List.");
        UIUtilities.unifiedButtonLookAndFeel(colourSwatchButton);
        colourSwatchButton.setBorderPainted(true);
        
        action = new AbstractAction("Color Swatch Button")
        {
            public void actionPerformed(ActionEvent evt) 
            {
                clearToggleButtons();
                pickSwatchPane();
            }
        };
        colourSwatchButton.addActionListener(action);
                
        toolbar.setFloatable(false);
        toolbar.setRollover(true);
        toolbar.add(colourSwatchButton);
        toolbar.add(colourWheelButton);
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
        userActionPanel = new JPanel();
        userActionPanel.setLayout(new FlowLayout());
        
        //accept
        acceptButton = new JButton("Accept");
        acceptButton.setToolTipText("Accept the selected color.");
        acceptButton.setActionCommand(""+ACCEPT);
        acceptButton.addActionListener(this);
        
        //revert
        revertButton = new JButton("Revert");
        revertButton.setToolTipText("Revert to the original color.");
        revertButton.setActionCommand(""+REVERT);
        revertButton.addActionListener(this);
        
        //cancel
        cancelButton = new JButton("Cancel");
        cancelButton.setToolTipText("Close the Color Picker.");
        cancelButton.setActionCommand(""+CANCEL);
        cancelButton.addActionListener(this);
        
        //preview
        previewButton = new JButton("Preview");
        previewButton.setToolTipText("Preview the color change.");
        previewButton.setActionCommand(""+PREVIEW);
        previewButton.addActionListener(this);
        previewButton.setVisible(false);
        
        userActionPanel.add(previewButton);
        userActionPanel.add(acceptButton);
        userActionPanel.add(revertButton);
        userActionPanel.add(cancelButton);
        setButtonsEnabled(false);
        parent.getRootPane().setDefaultButton(cancelButton);
    }

    /**
     * Creates PaintPotUI, RGB slider panel and HSVWheel panel and Colour
     * Swatch panel.
     */
    private void createPanels()
    {
        colourWheelPane = new HSVColourWheelUI(control);
        swatchPane = new ColourSwatchUI(control);
        paintPotPane = new PaintPotUI(control.getColour(), control.getLUT(), control);
    }
    
    /** 
     * Creates all the UI elements and displays the HSVWheel as active. 
     * 
     * @param field	  Pass <code>true</code> to add a field, 
	 * 				  <code>false</code> otherwise. 
	 */
    private void createUI(boolean field)
    {
        createToolbar();
        createActionbar();
        createPanels();
                
        JPanel container = new JPanel();
        container.setLayout(new BorderLayout());
        container.add(toolbar, BorderLayout.WEST);
          
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(container);
        paintPotPane.setPreferredSize(new Dimension(260, 24));
        add(Box.createVerticalStrut(5));
        add(paintPotPane);
        add(Box.createVerticalStrut(5));
        tabPanel = new JPanel();
        tabPaneLayout = new CardLayout();
        tabPanel.setLayout(tabPaneLayout);
        tabPanel.add(swatchPane, SWATCHPANE);
        tabPanel.add(colourWheelPane, COLOURWHEELPANE);
        add(tabPanel);
        if (field) {
        	add(new JSeparator());
        	JLabel label = UIUtilities.setTextFont("Description: ");
        	JPanel p = new JPanel();
        	double[][] size = {{TableLayout.PREFERRED, TableLayout.FILL}, 
        			{TableLayout.PREFERRED}};
        	p.setLayout(new TableLayout(size));
        	p.add(label, "0, 0");
        	fieldDescription = new JTextField();
        	p.add(fieldDescription, "1, 0");
        	add(p);
        }
        add(userActionPanel);
        
        if(control.isCustomColor())
            pickWheelPane();
        else
            pickSwatchPane();
    }
    
    /** Clears all buttons. */
    private void clearToggleButtons()
    {
        colourWheelButton.setSelected(false);
        colourSwatchButton.setSelected(false);
    }
    
    /** Sets Wheelbutton as picked and makes it visible. */
    private void pickWheelPane()
    {   
        colourWheelButton.setSelected(true);
        colourWheelPane.setActive(true);
        tabPaneLayout.show(tabPanel,COLOURWHEELPANE);
        swatchPane.setActive(false);
        colourWheelPane.findPuck();
        colourWheelPane.refresh();
        colourWheelPane.repaint();
    }
    
    /** Sets swatch as picked and makes it visible. */
    private void pickSwatchPane()
    {   
        tabPaneLayout.show(tabPanel,SWATCHPANE);
        colourSwatchButton.setSelected(true);
        swatchPane.setActive(true);
        colourWheelPane.setActive(false);
        this.doLayout();
        swatchPane.refresh();
    }
    
	/**
	 * Instantiates the tabbed pane, creates the UI and sets the control.
	 * 
     * @param parent  The parent of this component. Mustn't be <code>null</code>.
	 * @param control Reference to the control. Mustn't be <code>null</code>.
	 * @param luts The available lookup tables
	 * @param selectedLUT The selected lookup table
	 * @param field	  Pass <code>true</code> to add a field, 
	 * 				  <code>false</code> otherwise. 
	 */
	TabbedPaneUI(ColourPicker parent, RGBControl control, boolean field)
	{
        if (parent == null)
            throw new NullPointerException("No parent.");
        if (control == null)
            throw new NullPointerException("No control.");
        this.parent = parent;
		this.control = control;
		createUI(field);
		this.control.addListener(this);
	}
	
	/**
	 * Sets the enabled flag of the {@link #acceptButton} and 
	 * {@link #revertButton}.
	 * 
	 * @param enabled The value to set.
	 */
	void setButtonsEnabled(boolean enabled)
	{
		acceptButton.setEnabled(enabled);
		revertButton.setEnabled(enabled);
		previewButton.setEnabled(enabled);
		if (enabled) parent.getRootPane().setDefaultButton(acceptButton);
		else parent.getRootPane().setDefaultButton(cancelButton);
	}
	
	/** 
	 * Reverts current colour to the original colour choice passed to 
	 * Colourpicker.
	 */
	void revertAction()
	{ 
		control.revert(); 
		swatchPane.revert();
		//Check if preview was 
		if (preview) {
			preview = false;
			parent.reset();
		}
	}

	/** 
	 * Returns the description entered if any.
	 * 
	 * @return See above.
	 */
	String getDescription()
	{
		if (fieldDescription == null) return null;
		String text = fieldDescription.getText();
		if (text == null) return null;
		return text.trim();
	}
	
	/**
	 * Sets the description associated to the color.
	 * 
	 * @param description The value to set.
	 */
	void setColorDescription(String description)
	{
		if (fieldDescription == null || description == null) return;
		originalDescription = description;
		fieldDescription.setText(description);
		fieldDescription.getDocument().addDocumentListener(this);
	}
	
	/**
	 * Shows or hides the preview button.
	 * 
	 * @param visible Pass <code>true</code> to show the preview button,
	 * 				  <code>false</code> otherwise.
	 */
	void setPreviewVisible(boolean visible)
	{
		previewButton.setVisible(visible);
	}
	
	/** 
	 * Listens to ChangeEvent. 
	 * @see ChangeListener#stateChanged(ChangeEvent)
	 */
	public void stateChanged(ChangeEvent evt) 
	{
		if (colourWheelPane != null && colourWheelPane.isVisible())
			colourWheelPane.refresh();
		if (swatchPane != null && swatchPane.isVisible())
			swatchPane.refresh();
		if (fieldDescription == null)
			setButtonsEnabled(!control.isOriginalColour()
			        || !control.isOriginalLut());
		else {
			String text = fieldDescription.getText();
			setButtonsEnabled(!text.equals(originalDescription) 
					|| !control.isOriginalColour() 
					|| !control.isOriginalLut());
		}
	}

	/**
	 * Implemented as specified by {@link DocumentListener} I/F.
	 * @see DocumentListener#insertUpdate(DocumentEvent)
	 */
	public void insertUpdate(DocumentEvent e)
	{
		if (fieldDescription == null) return;
		String text = fieldDescription.getText();
		setButtonsEnabled(!text.equals(originalDescription) 
				|| !control.isOriginalColour() 
				|| !control.isOriginalLut());
	}

	/**
	 * Implemented as specified by {@link DocumentListener} I/F.
	 * @see DocumentListener#removeUpdate(DocumentEvent)
	 */
	public void removeUpdate(DocumentEvent e)
	{
		if (fieldDescription == null) return;
		String text = fieldDescription.getText();
		setButtonsEnabled(!text.equals(originalDescription) ||
				!control.isOriginalColour()
				|| !control.isOriginalLut());
	}

	/**
	 * Reacts to button clicks.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		int v = Integer.parseInt(e.getActionCommand());
		switch (v) {
			case CANCEL:
				parent.cancel();
				break;
			case REVERT:
				revertAction();
				break;
			case ACCEPT:
				parent.accept();
				break;
			case PREVIEW:
				preview = true;
				parent.preview();
		}
	}
	
	/**
	 * Required by the {@link DocumentListener} I/F but no-operation
	 * implementation in our case.
	 * @see DocumentListener#changedUpdate(DocumentEvent)
	 */
	public void changedUpdate(DocumentEvent e) {}
	
}
