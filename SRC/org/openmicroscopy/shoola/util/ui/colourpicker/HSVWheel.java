/*
 * org.openmicroscopy.shoola.util.ui.colourpicker.HSVWheel
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
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.math.geom2D.PlanePoint;

/** 
 * Creates the HSVColour wheel and provides methods for manipulating it's 
 * settings: Changing alpha and value(HSV). The wheel is created from a lookup
 * table generated in {@link #buildLUT()} which also creates reverse lookup 
 * tables used to determine the position on the wheel based on Hue, Saturation
 *  values. 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 			<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */

class HSVWheel 
		extends JPanel
{
	/**
	 * The colour wheel is created as an bufferedImage which is created
	 * in createColourWheel. 
	 */
	private BufferedImage 			img;
	
	/** Diameter of ColourWheel. */
	private float					wheelwidth;
	
	/** Radius of the wheel. */
	private float					radius;

	/** Position of the puck on ColourWheel. */
	private PlanePoint				puck;
	
	/** Colour of the puck border on the wheel. */
	private Color					puckColour;
	
	/** Colour of the puck fill on the wheel. */
	private Color					puckfillColour;
	
	/** Lookup table used to construct colourwheel. */
	private	int[][][]				lut;

	/**
	 * listener to the control, this will be the UI element which contains 
	 * HSVWheel. The listeners will be notified when the user has change the
	 * puck position.
	 */
	private ArrayList				listeners;
	
    /** Reference to the Control. */
    private RGBControl              control;
    
	/** Mouse listener used to get mouse down and drag events. */
	private HSVWheelListener       mouselistener;

	/**
	 * Maps from 4 ints to 4 byte colour.
	 * 
	 * @param a The alpha (0..255)
	 * @param r The red   (0..255)
	 * @param g The greed (0..255)
	 * @param b The blue  (0..255) 
	 * @return 4 byte int composed of the 4 params, Alpha-Red-Green-Blue.
	 */
	private int makeARGB(int a, int r, int g, int b)
    {
		return a << 24 | r << 16 | g << 8 | b;
	} 

	/**
	 * Builds the colour wheel graphic based on LUT, construct in a 
     * BufferedImage to improve speed. 
	 */
	private void createColourWheelFromLUT()
	{
		int sz = (int) radius;
		float szsz = sz*sz;
		
		float value  = control.getValue(); 
		for (int x =  sz ; x > -sz ; x--)
			for (int y =  sz ; y > -sz ; y--)
				if (x*x+y*y < szsz)
				{
					img.setRGB(sz+x, sz+y, makeARGB((
					(int) (control.getAlpha()*255)),
					(int) (lut[x+sz][y+sz][0]*value),
					(int) (lut[x+sz][y+sz][1]*value),
					(int) (lut[x+sz][y+sz][2]*value )));	
				}
				
	}
	
	/**
	 * Builds the lookup table for the colourwheel. althought the WheelUI
	 * changes all parts of the HSV vector, the wheel only changes the H, S
	 * components. We can then create a colour lookup table which will set these
	 * It is much fast to render the wheel using this.  
	 */
	private void buildComponents()
	{
		int sz = (int) radius;
		
		float f, p, q, t;
		float s, fsz, xd, yd, sd;
		float angle;
		int Hi;
	
		float szsz = sz*sz;
		float value = 1;
		fsz = sz;
		
		for (int x =  sz ; x > -sz ; x--)
		{
			for (int y =  sz ; y > -sz ; y--)
			{
				if (x*x+y*y < szsz)
				{
					s = (float) Math.sqrt(x*x+y*y);
					xd  = x/fsz;
					yd  = y/fsz;
					sd  = (s/fsz);
					if( sd != 0 )
						angle = (float) Math.toDegrees(Math.acos(xd/sd));
					else angle = 90;
					
					if (yd < 0) angle = 360-angle;
					Hi = ((int) angle/60)%6;
					switch(Hi) {
    					case 0:
    						f = ((angle/60.0f)-Hi);
    						p = (value)*(1-sd);
    						t = (value)*(1-(1-f)*sd);
    						lut[x+sz][y+sz][0] = (int) (value*255.0f);
    						lut[x+sz][y+sz][1] = (int) (t*255.0f);
    						lut[x+sz][y+sz][2] = (int) (p*255.0f);
    						break;
    					case 1:
    						f = ((angle/60.0f)-Hi);
    						p = (value)*(1-sd);
    						q = (value)*(1-f*sd);
    						lut[x+sz][y+sz][0] = (int) (q*255.0f);
    						lut[x+sz][y+sz][1] = (int) (value*255.0f);
    						lut[x+sz][y+sz][2] = (int) (p*255.0f);
    						break;
    					case 2:
    						f = ((angle/60.0f)-Hi);
    						p = (value)*(1-sd);
    						t = (value)*(1-(1-f)*sd);
    						lut[x+sz][y+sz][0] = (int) (p*255.0f);
    						lut[x+sz][y+sz][1] = (int) (value*255.0f);
    						lut[x+sz][y+sz][2] = (int) (t*255.0f);
    						break;
    					case 3:
    						f = ((angle/60.0f)-Hi);
    						p = (value)*(1-sd);
    						q = (value)*(1-f*sd);
    						lut[x+sz][y+sz][0] = (int) (p*255.0f);
    						lut[x+sz][y+sz][1] = (int) (q*255.0f);
    						lut[x+sz][y+sz][2] = (int) (value*255.0f);
    						break;
    					case 4:
    						f = ((angle/60.0f)-Hi);
    						p = (value)*(1-sd);
    						t = (value)*(1-(1-f)*sd);
    						lut[x+sz][y+sz][0] = (int) (t*255.0f);
    						lut[x+sz][y+sz][1] = (int) (p*255.0f);
    						lut[x+sz][y+sz][2] = (int) (value*255.0f);
    						break;
    					case 5:
    						f = ((angle/60.0f)-Hi);
    						p = (value)*(1-sd);
    						q = (value)*(1-f*sd);
    						lut[x+sz][y+sz][0] = (int) (value*255.0f);
    						lut[x+sz][y+sz][1] = (int) (p*255.0f);
    						lut[x+sz][y+sz][2] = (int) (q*255.0f);
    						break;
					}
				}
			}
		}
	}
		
	/**
	 * Builds the lookup table of the colourwheel, althought the WheelUI
	 * changes all parts of the HSV vector, the wheel only changes the H, S
	 * components. We can then create a colour lookup table which will set them. 
	 */
	void buildLUT()
	{
		lut = new int[(int) wheelwidth][(int) wheelwidth][3];
		buildComponents();
	}
	
	/**
	 * Overridden, calls the super
	 * @see javax.swing.JComponent#paintComponent(Graphics)
	 */
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		render((Graphics2D) g);
	}
	
	/**
	 * Construct the HSVWheel and reference to the control c.
	 * 
	 * @param c Reference to the control. Mustn't be <code>null</code>.
	 */
	HSVWheel(RGBControl c)
	{
        if (c == null)
            throw new NullPointerException("No control.");
		control = c;
		puck = new PlanePoint(radius, radius);
		mouselistener = new HSVWheelListener(this);
		this.addMouseListener(mouselistener);
		this.addMouseMotionListener(mouselistener);
		listeners = new ArrayList();
	}	
	
	/**
	 * Adds listener to the arraylist of components who will be notified when
	 * the state changes in HSVWheel. 
	 * 
	 * @param listener The listener to add.
	 */
	void addListener(ChangeListener listener) { listeners.add(listener); }

	/**
	 * Fires a Changed event to all listeners stating the HSVWheel has changed.
	 */
	void fireChangeEvent()
	{
        ChangeListener e;
		for (int i = 0 ; i < listeners.size(); i++)
		{
			e = (ChangeListener) listeners.get(i);
			e.stateChanged(new ColourChangedEvent(this));
		}
	}
	
	/** 
	 * Gets the Hue of the colour determined by the position of the puck.
	 * 
	 * @return Returns the value of the hue from the Hue Saturation 
	 *         look up table (HSlut). 
	 */
	float getHue()
	{
		double x = puck.x1-radius;
		double y = puck.x2-radius;
		
		double s = Math.sqrt(x*x+y*y);
		double xd  = x/radius;
		double yd  = y/radius;
		double sd  = (s/radius);
		double angle;
		
		/* Check for achromatic colours */
		if (x == 0 && y == 0) return 0;
		
		if (sd != 0) angle = (float) Math.toDegrees(Math.acos(xd/sd));
		else angle = 90;
		
		if (yd < 0) angle = 360-angle;
		
		return (float) angle/360.0f;
		
	}
	
	/** 
	 * Gets the Saturation of the colour determined by the position of the puck.
	 * 
	 * @return Returns the value of the saturation from the Hue Saturation 
	 *         look up table. 
	 */
	float getSaturation()
	{
		double x = puck.x1-radius;
		double y = puck.x2-radius;
		double s = Math.sqrt(x*x+y*y);
		return (float) s/radius;
	}
	
	/**
	 * Renders the graphics colourwheel. 
	 * 
	 * @param g The graphics context.
	 */
	void render(Graphics2D g)
	{
		wheelwidth = this.getWidth() < this.getHeight() ?
				this.getWidth() : this.getHeight();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                  RenderingHints.VALUE_ANTIALIAS_ON);
		g.drawImage(img, 0, 0, (int) wheelwidth, (int) wheelwidth, null);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                 RenderingHints.VALUE_ANTIALIAS_OFF);
	
		if (puck != null)
		{
			g.setStroke(new BasicStroke(1.0f));
			g.setPaint(puckfillColour);
			g.fillRect((int) puck.x1-2, (int) puck.x2-2, 4, 4);
			g.setPaint(puckColour);
			g.drawRect((int) puck.x1-2, (int) puck.x2-2, 4, 4);
		}  
	}

	/**
	 * Sets the wheel width to the panel size, the puck position to match the 
     * colour specified in the model and creates LUT and colourwheel image.
     * Called when the panel changes size.
	 */
	void changePanelSize()
	{
		wheelwidth = this.getWidth();
		radius = wheelwidth/2;
		puckColour = Color.black;
		puckfillColour = Color.white;
		img = new BufferedImage((int) wheelwidth,(int) wheelwidth, 
				BufferedImage.TYPE_INT_ARGB);
		buildLUT();
		createColourWheelFromLUT();
		findPuck();
	}
	
	/**
	 * Returns <code>true</code> if the wheel has been picked in the 
	 * JPanel component.
	 * 
	 * @param x position(x coord) of the mouse relative to the panel.
	 * @param y position(y coord) of the mouse relative to the panel.
	 * @return Returns <code>true</code> if (x,y) within wheel, 
     *          <code>false</code> otherwise.
	 */
	boolean picked(int x, int y)
	{
		float dx = (x-radius);
		float dx2 = dx*dx;
		float dy = (y-radius);
		float dy2 = dy*dy;
		float dist = dx2+dy2;
		float r2 = radius*radius;
		if (dist<r2)
		{
			puck = new PlanePoint(x,y);
			return true;
		}
		return false;
	}

	/**
	 * Method called on mousedown, checks to see if either the colourwheel,
	 * has been picked, if so set puck to the new x, y position and post 
	 * statechanged event.  
	 * @param x  mouse x position.
	 * @param y  mouse x position.
	 */
	void mouseDown(int x, int y) { if (picked(x, y)) fireChangeEvent(); }
	
	/**
	 * Method called on mousedrag, checks to see if either the colourwheel,
	 * has been picked, if so set puck to the new x, y position and post 
	 * statechanged event. 
	 * 
	 * @param x  mouse x position.
	 * @param y  mouse x position.
	 */
	void mouseDrag(int x, int y) { if (picked(x, y)) fireChangeEvent(); }
	
	/**
	 * Finds the position of the puck on the wheel based on the current colour
	 * selected by the model.
	 */
	void findPuck()
	{
		float h = (control.getHue()*360.0f);
		float s = (control.getSaturation());
		double x =  s*Math.cos(Math.toRadians(h));
		double y =  s*Math.sin(Math.toRadians(h));
		if (h != 0 && s != 0)
		{
			puck.x1 = x*radius+radius;
			puck.x2 = y*radius+radius;
		}
		else
		{
			puck.x1 = radius;
			puck.x2 = radius;
		}
	}
	
	/** Refreshes the display. */
	void refresh() { createColourWheelFromLUT(); }
    
    
    /**
     * Overridden, calls the {@link #changePanelSize()} method, to create
     * a new LUT and wheel to the size of the window. 
     * @see java.awt.Component#setBounds(int, int, int, int)
     */
    public void setBounds(int x, int y, int h, int w)
    {
        super.setBounds(x, y, w, h);
        changePanelSize();      
    }
    
    /** 
     * Overridden, calls the {@link #changePanelSize()} method, to create
     * a new LUT and wheel to the size of the window. 
     * @see java.awt.Component#setBounds(java.awt.Rectangle)
     */
    public void setBounds(Rectangle r)
    {
        super.setBounds(r);
        changePanelSize();
    }
    
}


