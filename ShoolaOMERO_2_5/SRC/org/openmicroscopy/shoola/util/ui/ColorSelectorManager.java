/*
 * org.openmicroscopy.shoola.agents.rnd.model.ColorSelectionManager
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

package org.openmicroscopy.shoola.util.ui;


//Java imports
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openmicroscopy.shoola.util.ui.IColorChooser;

//Third-party libraries

//Application-internal dependencies

/** 
 * Manager of the {@link ColorSelector} widget.
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
class ColorSelectorManager
	implements ActionListener, ChangeListener
{
	
	/** ID to handle events. */
	private static final int	COLOR = 100;
	private static final int	APPLY = 101;
	private static final int	CANCEL = 102;
	
	/** ID of the available colors.  */
	static final int 			RED = 0;
	static final int 			GREEN = 1;
	static final int 			BLUE = 2;
	static final int 			CYAN = 3;
	static final int 			MAGENTA = 4;
	static final int 			ORANGE = 5;
	static final int 			PINK = 6;
	static final int 			YELLOW = 7;
	static final int 			BLACK = 8;
	static final int 			WHITE = 9;
	
	static final int			MAX = 10;
	
	private Color				curColor;
	private int					curAlpha;
	
	private ColorSelector		view;
	
	private IColorChooser      component;
	
	/** Channel index. */
	private int					index;
	
	ColorSelectorManager(ColorSelector view, IColorChooser component,
						Color c, int index)
	{
		this.view = view;
		this.index = index;
		this.component = component;
		curColor = c;
		curAlpha = c.getAlpha();
	}
	
	/** Attach the listeners. */
	void attachListeners()
	{
		JComboBox list = view.colorsList;
		list.addActionListener(this);
		list.setActionCommand(""+COLOR);
        buttonListener(view.applyButton, APPLY);
        buttonListener(view.cancelButton, CANCEL);
		//Alpha slider.
		view.alphaSlider.addChangeListener(this);
		
	}

    private void buttonListener(JButton button, int id)
    {
        button.addActionListener(this);
        button.setActionCommand(""+id);
    }
    
	/** Handle events fired by Combobox and buttons. */
	public void actionPerformed(ActionEvent e)
	{
		String s = e.getActionCommand();
		int index = Integer.parseInt(s);
		try {
	   		switch (index) {
				case COLOR:
					JComboBox cb = (JComboBox) e.getSource();
					modifyPeview(cb.getSelectedIndex());
					break;
		   		case APPLY:
			   		applySettings();
			   		break;
		   		case CANCEL:
		   			view.setVisible(false);
			   		break;
	   		}// end switch  
		} catch(NumberFormatException nfe) {  
			throw new Error("Invalid Action ID "+index, nfe);
   		}        
	}

	/** Handle event fired by slider */
	public void stateChanged(ChangeEvent e)
	{
		modifyAlpha(view.alphaSlider.getValue());
	}
	
	private void applySettings()
	{
		component.setColor(index, curColor);
		view.setVisible(false);
	}
	
	/** 
	 * Modifiy the color. 
	 * 
	 * @param index		one of the color constants defined above. */
	private void modifyPeview(int index)
	{	
		try {
			switch(index) {
				case RED:
					setColorPreview(Color.RED); break; 
		   		case GREEN:
					setColorPreview(Color.GREEN); break;
		   		case BLUE:
					setColorPreview(Color.BLUE); break;
		   		case CYAN:
					setColorPreview(Color.CYAN); break;
		   		case MAGENTA:
					setColorPreview(Color.MAGENTA); break;
		   		case ORANGE:
					setColorPreview(Color.ORANGE); break;
		   		case PINK:
					setColorPreview(Color.PINK); break;
		   		case YELLOW:
					setColorPreview(Color.YELLOW); break;
		   		case BLACK:
					setColorPreview(Color.BLACK); break;
		   		case WHITE:
					setColorPreview(Color.WHITE); break;   
			}
		} catch(NumberFormatException nfe) {  
			throw new Error("Invalid Action ID "+index, nfe);
		}
	}
	
	/** Set the color. */
	private void setColorPreview(Color c)
	{
		curColor =  new Color(c.getRed(), c.getGreen(), c.getBlue(), curAlpha);	
		view.colorPanel.setColor(curColor);  
		view.repaint();
	}
	
	/** 
	 * Modify the alpha component.
	 * 
	 * @param value		value in the range 0-100.
	 */
	private void modifyAlpha(int value)
	{
		view.alphaField.setText("Transparency: "+value);
		curAlpha = (int) (value*2.55);
		curColor = new Color(curColor.getRed(), curColor.getGreen(), 
							curColor.getBlue(), curAlpha);	
		view.colorPanel.setColor(curColor);
		view.repaint();   	
	}
	
}
