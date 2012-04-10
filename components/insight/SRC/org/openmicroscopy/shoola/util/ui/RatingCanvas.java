/*
 * org.openmicroscopy.shoola.util.ui
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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JPanel;


//Third-party libraries

//Application-internal dependencies

/** 
 * The component where the rating levels are painted.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
class RatingCanvas
	extends JPanel
	implements MouseMotionListener
{

	/** The number of pixels between two stars. */
	static final int	SPACE = 2;

	/** Reference to the model. */
	private RatingComponent	model;

	/** Collection of rectangles where the selected stars are located. */
	private List<Rectangle>	stars;
	
	/** Handles the mouse release. */
	private MouseAdapter	handler;
	
	/** Flag indicating an on-going dragging or not.*/
	private boolean dragging;
	
	/** Flag indicating the mouse has been pressed.*/
	private boolean pressed;
	
	/** 
	 * Sets the number of selected stars depending on the location 
	 * of the mouse click.
	 * 
	 * @param p The location of the mouse click.
	 */
	private void handleClick(Point p)
	{
		if (!isEnabled()) return;
		boolean found = false;
		Rectangle r;
		for (int i = 0; i < stars.size(); i++) {
			r = stars.get(i);
			if (r.contains(p)) {
				found = true;
				model.setValue(i+1);
				break;
			}	
		}
		if (found) return;
		r = stars.get(0);
		if (p.x < r.x) {
			model.setValue(RatingComponent.MIN_VALUE);
			return;
		}
		r = stars.get(stars.size()-1);
		if (p.x > (r.x+r.width)) model.setValue(RatingComponent.MAX_VALUE);
	}
	
	/** Installs the listeners. */
	private void installListeners()
	{
		addMouseListener(handler);
		addMouseMotionListener(this);
	}
	
	/** Removes the listeners. */
	private void uninstallListeners()
	{
		removeMouseListener(handler);
		removeMouseMotionListener(this);
	}

	/**
	 * Invokes when the mouse is released.
	 * 
	 * @param p The mouse location.
	 */
	private void handleMouseReleased(Point p)
	{
		if (dragging || pressed) {
			handleClick(p);
			//model.onMouseReleased();
			dragging = false;
			pressed = false;
		}
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param model Reference to the model. Mustn't be <code>null</code>.
	 */
	RatingCanvas(RatingComponent model)
	{
		if (model == null)
			throw new IllegalArgumentException("No model");
		this.model = model;
		setDoubleBuffered(true);
		stars = new ArrayList<Rectangle>();
		handler = new MouseAdapter() {
		
			/** 
			 * Increases or decreases the number of selected stars
			 * depending on the location of the mouse click.
			 */
			public void mouseReleased(MouseEvent e) {
				//super.mouseReleased(e);
				handleMouseReleased(e.getPoint());
			}

			public void mousePressed(MouseEvent e) {
				//super.mouseReleased(e);
				pressed = true;
				handleMouseReleased(e.getPoint());
			}
		};
		installListeners();
	}
	
	/** 
	 * Overridden to add or remove the listeners
	 * @see javax.swing.JComponent#setEnabled(boolean)
	 */
	public void setEnabled(boolean enabled)
	{
		super.setEnabled(enabled);
		if (enabled) installListeners();
		else uninstallListeners();
	}
	
	/** 
	 * Overridden to paint the stars used to rate the image.
	 * @see javax.swing.JComponent#paintComponent(Graphics)
	 */
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		Graphics2D g2D = (Graphics2D) g;
		g.setColor(getBackground());
		int x = 0;
		int w, h;
		List<Image> l = model.getPlus();
		Iterator<Image> i = l.iterator();
		Image img;
		stars.clear();
		while (i.hasNext()) {
			img = (Image) i.next();
			w = img.getWidth(null);
			h = img.getHeight(null);
			stars.add(new Rectangle(x, 0, w, h));
			g2D.drawImage(img, x, 0, w, h, null);
			x += w+SPACE;
		}
		l = model.getMinus();
		i = l.iterator();
		while (i.hasNext()) {
			img = (Image) i.next();
			w = img.getWidth(null);
			h = img.getHeight(null);
			stars.add(new Rectangle(x, 0, w, h));
			g2D.drawImage(img, x, 0, w, h, null);
			x += w+SPACE;
		}
	}

	/**
	 * Sets the rating values when dragging the mouse over the component.
	 * @see MouseMotionListener#mouseDragged(MouseEvent)
	 */
	public void mouseDragged(MouseEvent e)
	{ 
		//dragging = true;
		//pressed = false;
		//handleClick(e.getPoint());
	}

	/**
	 * Required by the {@link MouseMotionListener} I/F but no-operation 
	 * implementation needed in our case.
	 * @see MouseMotionListener#mouseMoved(MouseEvent)
	 */
	public void mouseMoved(MouseEvent e) {}

}
