/*
 * org.openmicroscopy.shoola.util.ui.RotationIcon 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2012 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.util.ui;

//Java imports
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.Timer;

//Third-party libraries


import org.jdesktop.swingx.JXBusyLabel;

//Application-internal dependencies

/** 
 * Rotates an icon 
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.4
 */
public class RotationIcon
	implements Icon
{

	/** The default angle used to rotate the icon.*/
	private static final double DEFAULT_ANGLE = 90;
	
	/** The icon to rotate.*/
	private Icon icon;
	
	/** The time used to rotate the image.*/
	private final Timer timer;
	
	/** The angle used to rotate the image.*/
	private double angle;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param component The component of reference.
	 */
	public RotationIcon(final JComponent component)
	{
		this(null, component, false);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param icon The icon to rotate.
	 * @param component The component of reference.
	 * @param start Pass <code>true</code> to start the timer, 
	 * <code>false</code> otherwise.
	 */
	public RotationIcon(Icon icon, final JComponent component, boolean start)
	{
		if (icon == null) {
			JXBusyLabel label = new JXBusyLabel();
			if (start) label.setBusy(true);
			label.setSize(16, 16);
			icon = label.getIcon();
		}
		this.icon = icon;
		angle = DEFAULT_ANGLE;
		//Create the timer.
		timer = new Timer(100, new ActionListener() {

			public void actionPerformed( ActionEvent e ) {
				angle = angle+10;
				if (angle == 360) angle = 0;
				component.repaint();
			}
		} );
		timer.setRepeats(false);
		if (start) timer.start();
	}
	
	/** Stops the timer.*/
	public void stopRotation() { timer.stop(); }
	
	/** Stops the timer.*/
	public void startRotation() { timer.start(); }
	
	/**
	 * Returns the width of the icon to rotate.
	 * @see Icon#getIconWidth()
	 */
	public int getIconWidth() { return icon.getIconWidth(); }

	/**
	 * Returns the height of the icon to rotate.
	 * @see Icon#getIconHeight()
	 */
	public int getIconHeight() { return icon.getIconHeight(); }
	
	/**
	 * Paints the icon.
	 * @see Icon#paintIcon(Component, Graphics, int, int)
	 */
	public void paintIcon(Component c, Graphics g, int x, int y )
	{
		stopRotation();
		Graphics2D g2 = (Graphics2D) g.create();
		int w = icon.getIconWidth()/2;
		int h = icon.getIconHeight()/2;
		Rectangle r = new Rectangle(x, y, getIconWidth(), getIconHeight());
		g2.setClip(r);
		AffineTransform original = g2.getTransform();
		AffineTransform at = new AffineTransform();
		at.concatenate(original);
		at.rotate(Math.toRadians(angle), x+w, y+h);
		g2.setTransform(at);
		icon.paintIcon(c, g2, x, y);
		g2.setTransform(original);
		startRotation();
	}

}
