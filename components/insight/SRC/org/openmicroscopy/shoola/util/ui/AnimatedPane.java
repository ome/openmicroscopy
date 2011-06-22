/*
 * org.openmicroscopy.shoola.util.ui.AnimatedPane 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.util.ui;


//Java imports
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies

/** 
 * Component displaying the animation. 
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
class AnimatedPane 
	extends JPanel
{

	/** The size of the component. */
	private Dimension 				animatedSize;
	
	/** The source of the animation. */
	private JComponent 				source;
	
	/** The off-screen image. */
	private BufferedImage			image;
	
	/** The graphics configuration. */
	private GraphicsConfiguration 	config;
	
	/** Reference to the parent. */
	private AnimatedJFrame			parent;
	
	/**
	 * Create an off-screen image for the passed source.
	 * 
	 * @param src The source where to paint the image.
	 */
	private void makeOffscreenImage(JComponent src)
	{
		if (source  == null) return;
		image = config.createCompatibleImage(src.getWidth(), src.getHeight());
		Graphics2D g2D = (Graphics2D) image.getGraphics();
		src.paint(g2D);
	}
	
	/** 
	 * Creates a new instance. 
	 * 
	 * @param parent Reference to the parent.
	 */
	AnimatedPane(AnimatedJFrame parent)
	{
		this.parent = parent;
		animatedSize = new Dimension(1, 1);
		GraphicsEnvironment 
			env = GraphicsEnvironment.getLocalGraphicsEnvironment();
		config = env.getDefaultScreenDevice().getDefaultConfiguration();
		setOpaque(false);
	}
	
	/**
	 * Sets the source.
	 * 
	 * @param source The value to set.
	 */
	void setSource(JComponent source)
	{
		this.source = source;
		if (source == null) return;
		animatedSize.width = source.getWidth();
		if (parent.getOrientation() != AnimatedJFrame.DOWN)
			animatedSize.height = source.getHeight();
		makeOffscreenImage(source);
	}
	
	/** 
	 * Sets the height of the component.
	 * 
	 * @param height The value to set.
	 */
	void setAnimatingHeight(int height)
	{
		animatedSize.height = height;
		if (parent.getOrientation() == AnimatedJFrame.DOWN)
			setSize(animatedSize);
	}

	/**
	 * Overridden to return the default size.
	 * @see JPanel#getPreferredSize()
	 */
	public Dimension getPreferredSize() { return animatedSize; }
	
	/**
	 * Overridden to return the default size.
	 * @see JPanel#getMinimumSize()
	 */
	public Dimension getMinimumSize() { return animatedSize; }
	
	/**
	 * Overridden to return the default size.
	 * @see JPanel#getMaximumSize()
	 */
	public Dimension getMaximumSize() { return animatedSize; }
	
	/**
	 * Overridden to paint the off-screen image.
	 * @see JPanel#paintComponent(Graphics)
	 */
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		if (image == null) return;
		BufferedImage img;
		try {
			switch (parent.getOrientation()) {
				case AnimatedJFrame.UP_MIDDLE:
				case AnimatedJFrame.UP_RIGHT:
				case AnimatedJFrame.UP_LEFT:
					img = image.getSubimage(0, 0, source.getWidth(), 
							animatedSize.height);
					g.drawImage(img, 0, 
							source.getHeight()-animatedSize.height, this);
					break;
				default:
					img = image.getSubimage(0, 
							image.getHeight()-animatedSize.height, 
							source.getWidth(), animatedSize.height);
					g.drawImage(img, 0, 0, this);
			}
		} catch (Exception e) {} //ignore
	}
	
}
