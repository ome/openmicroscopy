/*
 * org.openmicroscopy.shoola.util.ui.colourpicker.ColourSlider
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
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import javax.swing.ImageIcon;
import javax.swing.JSlider;
import javax.swing.plaf.basic.BasicSliderUI;

//Third-party libraries

//Application-internal dependencies

/** 
 * ColourSliderUI is a derived class of BasicSliderUI which replaces the track 
 * with a colour spectrum, either from one RGB value to another or one HSV value 
 * to another. In this case the HSV normally just adjusts the V, to show a 
 * change in value. 
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

public class ColourSliderUI 
    extends BasicSliderUI
{
	/**
	 * Static variable used to determine which colourspace the user wishes to 
	 * use for the  slider. 
	 */
	final static int		HSV_COLOURSPACE = 1;

	/**
	 * Static variable used to determine which colourspace the user wishes to 
	 * use for the  slider.
	 */ 
	final static int		RGB_COLOURSPACE = 0;	
	
	/**
	 * Static variable used to determine which channel the user wishes to 
	 * use for the  gradient of the HSV slider. 
	 */
	final static int		HSV_CHANNEL_HUE = 0;
	
	/**
	 * Static variable used to determine which channel the user wishes to 
	 * use for the  gradient of the HSV slider. 
	 */
	final static int		HSV_CHANNEL_SATURATION = 1;
	
	/**
	 * Static variable used to determine which channel the user wishes to 
	 * use for the  gradient of the HSV slider. 
	 */
	final static int		HSV_CHANNEL_VALUE = 2;
	
	/**
	 * The Colourspace of the gradient used to fill track either 
	 * {@link #RGB_COLOURSPACE} or {@link #HSV_COLOURSPACE}.
	 */
	private int				colourSpace;
	
	/** RGB Colour use to start the gradient fill.  */
	private Color 			RGBStart;
	
	/** RGB Colour use to complete the gradient fill. */
	private Color 			RGBEnd;
	
	/** HSV Colour use to start the gradient fill.  */
	private HSV 			HSVStart;
	
	/** 
	 * Determine which channel of the HSV component the gradient fill will 
	 * use.
	 */
	private int				channel;
	
	/** HSV Colour use to complete the gradient fill. */
	private HSV 			HSVEnd;
	
	/** Colour of the border of the track. This is a medium Gray.  */
	private Color 			trackBorderColour;
	
	/** 
	 * The size of the track is too large in a standard JSlider component, so
	 * we create a smaller track and store it in smallTrack.
	 */
	private Rectangle 		smallTrack;

	/** 
	 * The size of the track is too large in a standard JSlider component, so
	 * we create a smaller track and store it in smallTrack, the filled part of 
	 * the track is stored in smallTrackFilled.
	 */
	private Rectangle 		smallTrackFilled;
	
	/** The user can replace the default JSlider thumb with their own. */
	private ImageIcon		thumbImageRight;
	
	/** The user can replace the default JSlider thumb with their own. */
	private ImageIcon		thumbImageDown;

	/** 
     * Creates the sliderUI delegate based on the JSlider component. The 
	 * Colour range is specifed in the s, and e parameters. 
     * 
	 * @param slider   parent slider.
	 * @param s        start colour used to paint the gradient in the track.
	 * @param e        end colour used to paint the gradient in the track.
	 */
	ColourSliderUI(JSlider slider, Color s, Color e)
	{
		super(slider);
		setColourSpace(RGB_COLOURSPACE);
		RGBStart = s;
		RGBEnd = e;
		trackBorderColour = new Color(128, 128, 128, 255);
		thumbImageRight = new ImageIcon("graphx/thumb.png","Thumb Icon");
		thumbImageDown = new ImageIcon("graphx/thumbhorizontal.png","Thumb Icon");
	}
	
	/** 
     * Creates the sliderUI delegate based on the JSlider component. The 
	 * Colour range is specifed in the s, and e parameters. 
     * 
	 * @param slider   parent slider.
	 * @param s        start HSV colour used to paint the gradient in the track.
	 * @param e        end HSV colour used to paint the gradient in the track.
	 */
	ColourSliderUI(JSlider slider, HSV s, HSV e)
	{
		super(slider);
		setColourSpace(HSV_COLOURSPACE);
		HSVStart = s;
		HSVEnd = e;
		channel = HSV_CHANNEL_VALUE;
		trackBorderColour = new Color(128, 128, 128, 255);
		thumbImageRight = new ImageIcon("graphx/thumb.png","Thumb Icon");
		thumbImageDown = new ImageIcon("graphx/thumbhorizontal.png","Thumb Icon");
	}
	
	/**
	 * Sets the current colourspace of the Slider. 
     * 
	 * @param CS Colourspace, should be either {@link #RGB_COLOURSPACE} or
	 * {@link #HSV_COLOURSPACE}.
	 */
	void setColourSpace(int CS) { colourSpace = CS; }

	/**
	 * Returns the current colourspace of the Slider. 
	 * 
	 * @return Colourspace, should be either {@link #RGB_COLOURSPACE} or
	 * {@link #HSV_COLOURSPACE}.
	 */
	int getColourSpace() { return colourSpace; }
		
	/**
	 * Sets the current start of the HSV Colour for gradient fill to c.
	 * 
	 * @param c HSV colour to be used for start of gradient fill. 
	 */
	void setHSVStart(HSV c) { HSVStart = new HSV(c); }
	
	/**
	 * Sets the current end of the HSV Colour for gradient fill to c.
	 * 
	 * @param c HSV Colour to be used for end of gradient fill. 
	 */
	void setHSVEnd(HSV c) { HSVEnd = new HSV(c); }
	
	/**
	 * Sets the current start of the HSV Colour for gradient fill to c.
	 * 
	 * @param c colour to be used for start of gradient fill. 
	 */
	void setHSVStart(Color c) { HSVStart = new HSV(c); }
	
	/**
	 * Sets the current end of the HSV Colour for gradient fill to c.
	 * 
	 * @param c colour to be used for end of gradient fill. 
	 */
	void setHSVEnd(Color c) { HSVEnd = new HSV(c); }
	
	/**
	 * Sets the channel the component will use for HSV gradient. The default
     * value is v.
     * 
	 * @param c The value to set.
	 */
	void setChannel(int c) { channel = c; }
	
	/**
	 * Sets the current start of the RGB Colour for gradient fill to c.
	 * 
	 * @param s Colour to be used for start of gradient fill. 
	 */
	void setRGBStart(Color s)
	{
		RGBStart = new Color(s.getRed(), s.getGreen(), s.getBlue(), 
				s.getAlpha());
	}
	
	/**
	 * Sets the current end of the RGB Colour for gradient fill to c.
	 * 
	 * @param e Colour to be used for end of gradient fill. 
	 */
	void setRGBEnd(Color e)
	{
		RGBEnd = new Color(e.getRed(), e.getGreen(), e.getBlue(), e.getAlpha());
	}

	/*public void paintThumb(Graphics og)
	{
		Graphics2D g = (Graphics2D)og;
		if( slider.getOrientation() == JSlider.VERTICAL )
			g.drawImage(thumbImageRight.getImage(),(int) smallTrack.getX(), 
				(int) thumbRect.getY(), (int) smallTrack.getWidth(),
				(int) smallTrack.getWidth()*2, null);
		else
		{
			thumbRect.height = (int) (smallTrack.getHeight()*2);
			thumbRect.width = (int) smallTrack.getHeight();
			g.drawImage(thumbImageDown.getImage(),
					(int) (thumbRect.getX()), 
					(int) thumbRect.getY(), (int) thumbImageDown.getIconWidth(),
					(int) thumbImageDown.getIconHeight(), null);
		}
			
		
	}*/
		
	
	/**
	 * Overridden to paint the gradient on the slider track
	 * @see BasicSliderUI#paintTrack(Graphics)
	 */
	public void paintTrack(Graphics og)
	{
		Graphics2D g = (Graphics2D)og;
		smallTrack = new Rectangle(trackRect);
		
		GradientPaint gp;
		if (this.slider.getOrientation() == JSlider.HORIZONTAL)
		{
			smallTrack.y += smallTrack.height/3;
			smallTrack.height = (int) (smallTrack.height*(3.0f/4.0f));
			smallTrackFilled = new Rectangle(smallTrack);
			smallTrackFilled.x +=1;
			smallTrackFilled.y +=1;
			smallTrackFilled.height -=1;
			smallTrackFilled.width -=1;
			
			g.setColor(trackBorderColour);
			g.fillRect(smallTrack.x+1, smallTrack.y+smallTrack.height+1, 1, 8);
			g.fillRect(smallTrack.x+smallTrack.width/2, 
					smallTrack.y+smallTrack.height+1, 1, 8);
			g.fillRect(smallTrack.x+smallTrack.width-2, 
					smallTrack.y+smallTrack.height+1, 1, 8);
			g.setPaint(trackBorderColour);
			g.draw(smallTrack);
		
			if (colourSpace == RGB_COLOURSPACE)
			{	
				gp = new GradientPaint((int) smallTrackFilled.getX(),
					 (int) smallTrackFilled.getY(),  RGBStart,
					 (int) smallTrackFilled.getWidth(),
					 (int) smallTrackFilled.getHeight(),RGBEnd, false);
		
				g.setPaint(gp);
				g.fill(smallTrackFilled);
			}
			else
			{
				g.fill(smallTrackFilled);
	
				float start = slider.getMinimum();
				float end = slider.getMaximum();
				float range = (end-start);
				start = start/255;
				end = end/255;
				range = range/255;
				
				
				float steps = (float) (smallTrackFilled.getWidth()/255.0f);
				for( int x = 0 ; x < 255 ; x++)
				{
					if (channel == HSV_CHANNEL_HUE)
						HSVStart.h = start+((float) x/255)*range;
					if (channel == HSV_CHANNEL_SATURATION)
						HSVStart.s = start+((float) x/255)*range;
					if (channel == HSV_CHANNEL_VALUE)
						HSVStart.v = start+((float) x/255)*range;				
					g.setPaint(HSVStart.toColor());
					g.fillRect((int) (smallTrackFilled.getX()+x*steps),
							(int) smallTrackFilled.getY(),
							(int) Math.ceil(steps),
							(int) smallTrackFilled.getHeight());
				}
			}
		}
		else
		{
			smallTrack.width = (int) (smallTrack.width*(3.0f/4.0f));
			smallTrackFilled = new Rectangle(smallTrack);
			smallTrackFilled.x +=1;
			smallTrackFilled.y +=1;
			smallTrackFilled.height -=1;
			smallTrackFilled.width -=1;
			
			if (colourSpace == RGB_COLOURSPACE)
			{
				gp = new GradientPaint((int) smallTrackFilled.getX(),
					 (int) smallTrackFilled.getY(),  RGBEnd,
					 (int) smallTrackFilled.getWidth(),
					 (int) smallTrackFilled.getHeight(),RGBStart, false);
		
				g.setPaint(gp);
				g.fill(smallTrackFilled);
			}
			else
			{
				g.fill(smallTrackFilled);
				
				float start = slider.getMinimum();
				float end = slider.getMaximum();
				float range = (end-start);
				start = start/255;
				end = end/255;
				range = range/255;
				
				float steps = (float) (smallTrackFilled.getHeight()/255.0f);
				for (int x = 0 ; x < 255 ; x++)
				{
					if(channel == HSV_CHANNEL_HUE)
						HSVStart.h = end-((float) x/255)*range;
					if(channel == HSV_CHANNEL_SATURATION)
						HSVStart.s = end-((float) x/255)*range;
					if(channel == HSV_CHANNEL_VALUE)
						HSVStart.v = end-((float) x/255)*range;
					g.setPaint(HSVStart.toColor());
					
					g.fillRect((int) smallTrackFilled.getX(), 
							(int) (smallTrackFilled.getY()+x*steps),
							(int) smallTrackFilled.getWidth(),
							(int) Math.ceil(steps));
				}
			}
			g.setColor(trackBorderColour);
			g.fillRect(smallTrack.x+smallTrack.width+1, smallTrack.y+1, 8, 1);
			g.fillRect(smallTrack.x+smallTrack.width+1, smallTrack.y+
					    smallTrack.height/2, 8, 1);
			g.fillRect(smallTrack.x+smallTrack.width+1, smallTrack.y+
					    smallTrack.height-2, 8, 1);
			g.setPaint(trackBorderColour);
			g.draw(smallTrack);
		}
	}
	
}
