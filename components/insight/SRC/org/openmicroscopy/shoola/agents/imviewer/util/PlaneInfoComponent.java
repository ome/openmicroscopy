/*
 * org.openmicroscopy.shoola.agents.imviewer.util.PlaneInfoComponent 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2016 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.imviewer.util;


//Java imports
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

//Third-party libraries


//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.ColouredButton;
import org.openmicroscopy.shoola.util.ui.colourpicker.LookupTableIconUtil;

/** 
 * Basic component hosting the plane information.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class PlaneInfoComponent 
	extends JPanel//JLabel
{

	/** Bound property indicating the plane information. */
	public static final String 		PLANE_INFO_PROPERTY = "planeInfo";
	
	/** The dimension of the icon. */
	private static final Dimension ICON_DIMENSION = new Dimension(12, 12);
	
	/** The icon displaying the channel color. */
	//private ColourIcon 	icon;
	
	/** Component displaying the color associated to the channel. */
	private ColouredButton icon;
	
	/** Component displaying the textual information. */
	private JLabel 			label;
	
	/** 
	 * Component displaying in a dialog when the user presses on the 
	 * component. 
	 */
	private JLabel		content;

	/** Fires a property indicating to display the information. */
	private void showInfo()
	{
		firePropertyChange(PLANE_INFO_PROPERTY, null, this);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param color	The color associated to the channel.
	 */
	public PlaneInfoComponent(Color color)
	{
		content = new JLabel();
		//icon = new ColourIcon(ICON_DIMENSION, color);
		//icon.paintLineBorder(true);
		//setIcon(icon);
		icon = new ColouredButton("", color);
		icon.setPreferredSize(ICON_DIMENSION);
		label = new JLabel();
		label.setVerticalTextPosition(JLabel.CENTER);
		label.setHorizontalTextPosition(JLabel.RIGHT);
		label.addMouseListener(new MouseAdapter() {
		
			/**
			 * Shows information
			 * @see MouseAdapter#mousePressed(MouseEvent)
			 */
			public void mousePressed(MouseEvent e) {
				showInfo();
			}
		});
		//setBorder(BorderFactory.createEmptyBorder());
		icon.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				showInfo();
			}
		});
		setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		add(icon);
		add(Box.createHorizontalStrut(2));
		add(label);
	}
	
	/**
     * Creates a new instance.
     * 
     * @param lut The lookup table associated to the channel.
     */
	public PlaneInfoComponent(String lut)
    {
        content = new JLabel();
        icon = new ColouredButton("", LookupTableIconUtil.getLUTIconImage(lut));
        icon.setPreferredSize(ICON_DIMENSION);
        label = new JLabel();
        label.setVerticalTextPosition(JLabel.CENTER);
        label.setHorizontalTextPosition(JLabel.RIGHT);
        label.addMouseListener(new MouseAdapter() {
        
            /**
             * Shows information
             * @see MouseAdapter#mousePressed(MouseEvent)
             */
            public void mousePressed(MouseEvent e) {
                showInfo();
            }
        });
        //setBorder(BorderFactory.createEmptyBorder());
        icon.addActionListener(new ActionListener() {
            
            public void actionPerformed(ActionEvent e) {
                showInfo();
            }
        });
        setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        add(icon);
        add(Box.createHorizontalStrut(2));
        add(label);
    }
	
	/**
	 * Sets the text associated to the component.
	 * 
	 * @param text The value to set.
	 */
	public void setText(String text) { label.setText(text); }
	
	/** 
	 * Returns the content component.
	 * 
	 * @return See above.
	 */
	public JComponent getContent() { return content; }
	
    /**
     * Sets the color of the icon.
     * 
     * @param color
     *            The value to set.
     */
    public void setColor(Color color) {
        icon.setColor(color);
        icon.setImage(null);
    }

    /**
     * Set the lookup table for the icon
     * 
     * @param lut
     *            The lookup table
     */
    public void setLookupTable(String lut) {
        icon.setImage(LookupTableIconUtil.getLUTIconImage(lut));
    }
	
	/**
	 * Overridden to set the text of the <code>content</code> component.
	 * @see JLabel#setToolTipText(String)
	 */
	public void setToolTipText(String text)
	{
		super.setToolTipText(text);
		content.setText(text);
	}
	
}
