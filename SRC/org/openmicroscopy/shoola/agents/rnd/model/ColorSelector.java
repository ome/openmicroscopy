/*
 * org.openmicroscopy.shoola.agents.rnd.model.ColorSelection
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
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.ColorPanel;
import org.openmicroscopy.shoola.util.ui.TableComponent;
import org.openmicroscopy.shoola.util.ui.TableComponentCellEditor;
import org.openmicroscopy.shoola.util.ui.TableComponentCellRenderer;
/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class ColorSelector
	extends JDialog
{	
	/** table constants. */
	private static final int			ROW_HEIGHT = 25;
	private static final int			WIDTH_ONE = 80;
	
	private static final int			WIDTH_TWO = 100;
	private static final Dimension		SLIDER_PANEL = new Dimension(WIDTH_TWO,
															ROW_HEIGHT);
	
	private static final int 			HEIGHT_BOX = 20;
	private static final int			BORDER = 10;
	private static final int			VSPACE = 10;
	private static final int			HSPACE = 15;

	private static final int			H_EXTRA = 25;
	
	private static final int			WIDTH = 160;
	private static final int			HEIGHT = 50;
	private static final int			TOP = 15;
	private static final int			WBUTTON = 70;
	
	private static final int 			WIN_W = 250;
	private static final int 			WIN_H = 140;
	
	private static final int			MAX_SLIDER = 100;
	private static final String[]  		selection;
	
	private static final int			DEFAULT_INDEX = 
											ColorSelectorManager.RED;
	static {
		selection = new String[ColorSelectorManager.MAX];
		selection[ColorSelectorManager.BLACK] = "Black";
		selection[ColorSelectorManager.WHITE] = "White";
		selection[ColorSelectorManager.RED] = "Red";
		selection[ColorSelectorManager.GREEN] = "Green";
		selection[ColorSelectorManager.BLUE] = "Blue";
		selection[ColorSelectorManager.CYAN] = "Cyan";
		selection[ColorSelectorManager.MAGENTA] = "Magenta";
		selection[ColorSelectorManager.ORANGE] = "Orange";
		selection[ColorSelectorManager.PINK] = "Pink";
		selection[ColorSelectorManager.YELLOW] = "Yellow";
	}
														
	/** Slider to select the alpha component of the color. */
	private JSlider						alphaSlider;
	
	/** Textfield which displays the value of the alpha component. */
	private JLabel						alphaField;
	
	private JButton						applyButton, cancelButton;
	
	/** List of color available. */
	private JComboBox					colorsList;
	
	private ColorPanel					colorPanel;
	
	private JPanel						contents;
	
	private ColorSelectorManager		manager;
	
	ColorSelector(RGBPaneManager rgbManager, int[] rgba, int index)
	{
		super(rgbManager.getReferenceFrame(), "Color Selector", true);	
		Color c = new Color(rgba[0], rgba[1], rgba[2], rgba[3]);
		manager = new ColorSelectorManager(this, rgbManager, c, index);
		initColorPanel(c);
		initControls((int) (rgba[3]*100/255));
		buildGUI();
		manager.attachListeners();
		Container contentPane = super.getContentPane(); 
		contents.setSize(WIN_W, WIN_H);
		contentPane.add(contents);
		//setResizable(false);
		setSize(WIN_W, WIN_H+H_EXTRA);
	}
	
	ColorPanel getColorPanel()
	{
		return colorPanel;
	}

	JButton getApplyButton()
	{
		return applyButton;
	}
	
	JButton getCancelButton()
	{
		return cancelButton;
	}
	
	public JComboBox getColorsList()
	{
		return colorsList;
	}
	
	public JLabel getAlphaField()
	{
		return alphaField;
	}
	
	public JSlider getAlphaSlider()
	{
		return alphaSlider;
	}
	
	/** Initialize the color preview. */
	private void initColorPanel(Color c)
	{
		colorPanel  = new ColorPanel();
		colorPanel.setLayout(null);
		colorPanel.setBounds(BORDER+WIDTH+HSPACE, 2*TOP, 45, 45);
		colorPanel.setBorder(BorderFactory.createLineBorder(Color.black));
		colorPanel.setColor(c);
	}
	
	/** Initializes the slider and the Combobox. */
	private void initControls(int value)
	{
		applyButton = new JButton("Apply");
		cancelButton = new JButton("Cancel");
		alphaSlider = new JSlider(JSlider.HORIZONTAL, 0, MAX_SLIDER, value);
		alphaField = new JLabel(" Alpha: "+value);
		alphaField.setOpaque(false);
		alphaSlider.setOpaque(false);
		colorsList = new JComboBox(selection);
		colorsList.setSelectedIndex(DEFAULT_INDEX);
	}
	
	/** Build and layout the GUI. */
	private void buildGUI()
	{
		contents = new JPanel();
		contents.setLayout(null);
		contents.add(buildControls());
		contents.add(builButtonsPanel());
		contents.add(buildLabel());
		contents.add(colorPanel);	
	}
	
	/** Builds a JLabel and positions it. */
	private JLabel buildLabel()
	{
		JLabel label = new JLabel("Preview");
		label.setBounds(BORDER+WIDTH+HSPACE, TOP, 45, 10);
		return label;	
	}
	
	/** 
	 * Builds a panel containing the buttons.
	 *
	 * @return	The above mentioned panel.
	 */
	private JPanel builButtonsPanel()
	{
		JPanel p = new JPanel();
		p.setLayout(null);
		applyButton.setBounds(0, 0, WBUTTON, 20);
		cancelButton.setBounds(WBUTTON+3, 0, WBUTTON, 20);
		p.add(applyButton);
		p.add(cancelButton);
		p.setBounds(2*BORDER, HEIGHT+HEIGHT_BOX+2*VSPACE, 2*WIDTH_TWO, 
					HEIGHT_BOX);
				
		return p;
	} 
	
	private JTable buildControls()
	{
		JTable table = new TableComponent(2, 2);
		table.setTableHeader(null);
		table.setOpaque(false);
		table.setRowHeight(ROW_HEIGHT);
		table.setShowGrid(false);		// remove the grid
		TableColumnModel columns = table.getColumnModel();
		TableColumn column = columns.getColumn(0);
		column.setPreferredWidth(WIDTH_ONE);
		column.setWidth(WIDTH_ONE);
		column = columns.getColumn(1);
		column.setPreferredWidth(WIDTH_TWO);
		column.setWidth(WIDTH_TWO);
		JLabel label  = new JLabel(" Color: ");
		//first row.
		table.setValueAt(label, 0, 0);
		table.setValueAt(colorsList, 0, 1);
		
		//second row.
		table.setValueAt(alphaField, 1, 0);
		table.setValueAt(buildSliderPanel(), 1, 1);
		table.setDefaultRenderer(JComponent.class, 
								new TableComponentCellRenderer());
		table.setDefaultEditor(JComponent.class, 
								new TableComponentCellEditor());
		
		table.setBounds(BORDER, 2*TOP, WIDTH, HEIGHT);
		
		return table;
		
	}
	
	/** 
	 * Builds a JPanel with JSlider.
	 */
	private JPanel buildSliderPanel()
	{
		JPanel p = new JPanel();
		p.setLayout(null);
		alphaSlider.setBounds(5, 0, WIDTH_TWO-20, ROW_HEIGHT);
		p.add(alphaSlider);
		p.setPreferredSize(SLIDER_PANEL);
		p.setSize(SLIDER_PANEL);			
		return p;
	}
	
}