/*
 * org.openmicroscopy.shoola.agents.rnd.model.ColorChooser
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

package org.openmicroscopy.shoola.agents.rnd.model;

//Java imports
import java.awt.Color;
import java.awt.Container;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.rnd.defs.ChannelBindings;
import org.openmicroscopy.shoola.util.ui.ColorPanel;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * <br><b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class ColorChooser
	extends JDialog
 {
 	
 	static final int					RED = 0;
	static final int					GREEN = 1;
	static final int					BLUE = 2;
	static final int					ALPHA = 3;
	
	private static final int			WIDTH_BOX = 30;
	private static final int 			HEIGHT_BOX = 20;
	private static final int			BOX_SPACE = 2; 
	private static final int			BORDER = 10;
	private static final int			VSPACE = 10;
	private static final int			HSPACE = 15;
	private static final int 			WIN_W = 240;
	private static final int 			WIN_H = 180;
	private static final int			W_EXTRA = 30;
	private static final int			H_EXTRA = 25;
	private static final int			WLABEL = 40;
	
	private static final int			WBUTTON = 70;
	
	/** Slider controlling the Alpha component. */
	private JSlider				alphaSlider;
	
	private JTextField			alphaTextField;
	
	/** Textfield displaying the value of the red component of the color.*/
	private JTextField			rArea;
	
	/** Textfield displaying the value of the green component of the color.*/
	private JTextField			gArea;
	
	/** Textfield displaying the value of the blue component of the color.*/
	private JTextField			bArea;
	
	private JButton				applyButton;
	private JButton				saveButton;
	private JButton 			cancelButton;
	private ColorPanel			colorPanel;
	private ColorChooserManager ccManager;
	private ColorPalette		cp;
	
	private JPanel				contents;
	
	/**
	 * Creates a new ColorChooser instance.
	 * 
	 * @param wcd		colorData.
	 */
	public ColorChooser(ChannelBindings cb)
	{
		int[] rgba = cb.rgba;
		int v = (int) (rgba[ALPHA]*100/255);
		Color c = new Color(rgba[RED], rgba[GREEN], rgba[BLUE], rgba[ALPHA]);
		ccManager = new ColorChooserManager(this, rgba, v);
		cp = new ColorPalette(ccManager);
		Container contentPane = super.getContentPane(); 
		initButtons();
		initSlider(v);
		initTextBoxes(rgba, v);
		initColorPanel(c);
		buildGUI();
		contents.setSize(WIN_W, WIN_H);
		contentPane.add(contents);
		
		ccManager.attachListeners();
		setResizable(false);
		setSize(WIN_W+W_EXTRA, WIN_H+H_EXTRA);
	}
	
	/** Returns the colorPalette containing the three color bars. */
	ColorPalette getColorPalette()
	{
		return cp;
	}
	
	/** Returns the selected color panel. */
	ColorPanel getColorPanel()
	{
		return colorPanel;
	}
	
	/** Returns the Save button. */
	JButton getSaveButton()
	{
		return saveButton;
	}
	
	/** Returns the Apply button. */
	JButton getApplyButton()
	{
		return applyButton;
	}
		
	/** Returns the Cancel button. */
	JButton getCancelButton()
	{
		return cancelButton;
	}
	
	/** Returns the Red textArea. */
	JTextField getRArea()
	{
		return rArea;
	}
	
	/** Returns the green textArea. */
	JTextField getGArea()
	{
		return gArea;
	}
	
	/** Returns the blue textArea. */
	JTextField getBArea()
	{
		return bArea;
	}
	
	/** Returns the alpha slider. */
	JSlider getAlphaSlider()
	{
		return alphaSlider;
	}
	
	/** Returns the alpha textField. */
	JTextField getAlphaTextField()
	{
		return alphaTextField;
	}
	
	/** Initializes the Color selected panel and positions it. */
	private void initColorPanel(Color c)
	{
		colorPanel  = new ColorPanel();
		colorPanel.setLayout(null);
		colorPanel.setBounds(2*BORDER+ColorPalette.WIDTH_PANEL+HSPACE, 30, 45, 
							45);
		colorPanel.setBorder(BorderFactory.createLineBorder(Color.black));
		colorPanel.setColor(c);
	}
	
	/** Initializes the buttons. */
	private void initButtons()
	{
		saveButton = new JButton("Save");
		applyButton = new JButton("Apply");
		applyButton.setEnabled(false);
		cancelButton = new JButton("Cancel");
	}
	
	/** Initializes the slider. */
	private void initSlider(int v)
	{
		alphaSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, v);
		alphaSlider.setOpaque(false);
	}
	
	/** Initializes the textBoxes. */
	private void initTextBoxes(int[] rgba, int v)
	{
		rArea = new JTextField();
		gArea = new JTextField(); 
		bArea = new JTextField();
		rArea.setEditable(true);
		gArea.setEditable(true);
		bArea.setEditable(true);
		rArea.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		gArea.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		bArea.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		rArea.setText(""+rgba[RED]);
		gArea.setText(""+rgba[GREEN]);
		bArea.setText(""+rgba[BLUE]);
		// init alpha textField
		alphaTextField = new JTextField(""+v);
		alphaTextField.setEditable(false);
		alphaTextField.setOpaque(false);
		alphaTextField.setBorder(null);
	}
	
	/** Builds the widget. */
	private void buildGUI()
	{
		contents = new JPanel();
		contents.setLayout(null);
		contents.add(buildLayeredPane());
		contents.add(buildHSBPanel());
		contents.add(buildAlphaPanel());
		contents.add(builButtonPanel());
		contents.add(buildLabel());
		contents.add(colorPanel);
	}
	
	/** Builds a JLabel and positions it. */
	private JLabel buildLabel()
	{
		JLabel label = new JLabel("Preview");
		label.setBounds(2*BORDER+ColorPalette.WIDTH_PANEL+HSPACE, 15, 45, 10);
		return label;	
	}
	
	/** Builds and initializes the ColorPalette. */
	private JLayeredPane buildLayeredPane()
	{		
		
		cp.setBounds(2*BORDER, 0, ColorPalette.WIDTH_PANEL, 
					ColorPalette.HEIGHT_LP);
		return cp;
	}
	
	/** Builds the panel with the HBS TextField. */
	private JPanel buildHSBPanel()
	{
		JPanel p = new JPanel();
		p.setLayout(null);
		JLabel label = new JLabel("RGB ");
		label.setBounds(0, 0, WIDTH_BOX, HEIGHT_BOX);
		rArea.setBounds(WIDTH_BOX+BOX_SPACE, 0, WIDTH_BOX, HEIGHT_BOX);
		gArea.setBounds(2*(WIDTH_BOX+BOX_SPACE), 0, WIDTH_BOX, HEIGHT_BOX);
		bArea.setBounds(3*(WIDTH_BOX+BOX_SPACE), 0, WIDTH_BOX, HEIGHT_BOX);
		p.add(label);
		p.add(rArea);
		p.add(gArea);
		p.add(bArea);
		p.setBounds(3*BORDER, ColorPalette.HEIGHT_LP, ColorPalette.WIDTH_PANEL,
					HEIGHT_BOX);
		return p;
	}
	
	/** 
	 * Builds a JPanel with JLabel, JTextField and JSlider.
	 */
	private JPanel buildAlphaPanel()
	{
		JPanel p = new JPanel();
		JLabel label = new JLabel("Alpha ");
		p.setLayout(null);
		int size = ColorPalette.WIDTH_PANEL-2*WIDTH_BOX;
		label.setBounds(0, 0, WLABEL, HEIGHT_BOX);
		alphaTextField.setBounds(WLABEL, 0, WIDTH_BOX, HEIGHT_BOX);
		alphaSlider.setBounds(WLABEL+WIDTH_BOX+2*BOX_SPACE, 0, size, 
							HEIGHT_BOX);
		p.add(label);
		p.add(alphaTextField);
		p.add(alphaSlider);
		p.setBounds(3*BORDER, ColorPalette.HEIGHT_LP+HEIGHT_BOX+VSPACE, 
					ColorPalette.WIDTH_PANEL+2*HSPACE, HEIGHT_BOX);
					
		return p;
	}
	
	/** 
	 * Builds a panel containing the buttons.
	 *
	 * @return       The above mentioned panel.
	 */
	private JPanel builButtonPanel()
	{
		JPanel p = new JPanel();
		p.setLayout(null);
		applyButton.setBounds(0, 0, WBUTTON, 20);
		saveButton.setBounds(WBUTTON+3, 0, WBUTTON, 20);
		cancelButton.setBounds(2*(WBUTTON+3), 0, WBUTTON, 20);
		p.add(applyButton);
		p.add(saveButton);
		p.add(cancelButton);
		p.setBounds(BORDER, ColorPalette.HEIGHT_LP+2*(HEIGHT_BOX+VSPACE),
					220, HEIGHT_BOX);
					
		return p;
	} 
		
}

