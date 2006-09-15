/*
 * org.openmicroscopy.shoola.util.ui.colourpicker.PaintPotUI
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
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

//Third-party libraries

//Application-internal dependencies

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $$Revision: $$Date: $$)
 * </small>
 * @since OME2.2
 */
class PaintPotUI
	extends JPanel
	implements ChangeListener
{
	
	/** Reference to the control. */
	private RGBControl				control;
    
	/** Matrix stack used to save affinetransform. */
	private MatrixStack				stack;
    
	/** Width  of paint pot. */
	private int 					w;
    
	/** Height of paint pot. */
	private int 					h;
    
	/** Border of paintpot.
	 */
	private Rectangle2D				strokeRect;
    
	/** Area covered by paintpot. */
	private Rectangle2D				whiteRect;
    
	/** Toppoly represents the colour picked without Alpha. */
	private Polygon					topPoly;
    
	/** Bottom represents the colour picked with alpha channel added. */
	private Polygon					bottomPoly;
    
	/** X coordinates of the polygons to represent the colour pots. */
	private int[]					topXPoints; 
    
	/** X coordinates of the polygons to represent the colour pots.  */
	private int[]					bottomXPoints; 
    
	/** Y coordinates of the polygons to represent the colour pots.  */
	private int[]					topYPoints; 
    
	/** Y coordinates of the polygons to represent the colour pots. */
	private int[]					bottomYPoints; 
	
	/**
	 * Creates the UI and attach the component to the control.
	 * 
	 * @param c Reference to the control. Mustn't be <code>null</code>.
	 */
	PaintPotUI(RGBControl c)
	{
        if (c == null)
            throw new NullPointerException("No control.");
		control = c;
		stack = new MatrixStack();
		createUI();
		control.addListener(this);
	}
	
	/**
	 * Creates the UI elements of the paint pot, the two colour parts of the 
	 * white rect, the top half representing the colour without the alpha 
	 * component and the bottom half with the alpha.
	 */
	void createUI()
	{
		w = this.getWidth();
		h = this.getHeight();
		topXPoints = new int[3];
		topYPoints = new int[3];
		bottomXPoints = new int[3];
		bottomYPoints = new int[3];
		strokeRect = 	new Rectangle2D.Double(0, 0, w-1, h-1);
		whiteRect = 	new Rectangle2D.Double(1, 1, w-2, h-2);
		
		topXPoints[0] = (int) whiteRect.getX();
		topXPoints[1] = (int) (whiteRect.getX()+whiteRect.getWidth());
		topXPoints[2] = (int) (whiteRect.getX());
		topYPoints[0] = (int) whiteRect.getY();
		topYPoints[1] = (int) (whiteRect.getY());
		topYPoints[2] = (int) (whiteRect.getY()+whiteRect.getHeight());
		topPoly = new Polygon(topXPoints, topYPoints,3);
		bottomXPoints[0] = (int) strokeRect.getX();
		bottomXPoints[1] = (int) (strokeRect.getX()+strokeRect.getWidth());
		bottomXPoints[2] = (int) (strokeRect.getX()+strokeRect.getWidth());
		bottomYPoints[0] = (int) (strokeRect.getY()+strokeRect.getHeight());
		bottomYPoints[1] = (int) (strokeRect.getY());
		bottomYPoints[2] = (int) (strokeRect.getY()+strokeRect.getHeight());
		bottomPoly = new Polygon(bottomXPoints, bottomYPoints,3);	
	}
	
	/**
	 * Renders is called from {@link #paintComponent(Graphics)} to render all 
     * the graphic elements of the component. 
	 * 
	 * @param og The graphic context.
	 */
	void render(Graphics og)
	{
		Graphics2D g = (Graphics2D)og;
		createUI();
		stack.push(g);
		
			Color c = new Color(control.getRed(), control.getGreen(), 
					control.getBlue());
			g.setColor(c);
			  g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
	                  RenderingHints.VALUE_ANTIALIAS_ON);
			g.fill(topPoly);
			g.setColor(control.getColour());
			g.fill(bottomPoly);
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
	                  RenderingHints.VALUE_ANTIALIAS_OFF);			
			g.setColor(Color.black);
			g.draw(strokeRect);
		
		stack.pop(g);
		
	}
	
	/** 
	 * Calls and invalidate and repaint after colour changes in
	 * colour model.
	 * @see ChangeListener#stateChanged(ChangeEvent)
	 */
	public void stateChanged(ChangeEvent event) 
	{
		invalidate();
		repaint();
	}
    
    /**
     * Overridden, calls render after, super.paintComponent. Which renders the
     * paint pot. 
     * @see javax.swing.JComponent#paintComponent(Graphics)
     */
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        render(g);
    }
    
}
