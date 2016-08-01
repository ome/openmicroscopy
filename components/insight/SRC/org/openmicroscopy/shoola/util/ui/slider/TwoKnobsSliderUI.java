/*
 * org.openmicroscopy.shoola.util.ui.slider.TwoKnobsSliderUI
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

package org.openmicroscopy.shoola.util.ui.slider;




//Java imports
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Rectangle;
import java.util.Iterator;
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.UIManager;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.image.geom.Factory;
import org.openmicroscopy.shoola.util.ui.IconManager;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
* The UI delegate for the {@link TwoKnobsSlider}.
* A delegate can't be shared among different instances of
* {@link TwoKnobsSlider} and has a life-time dependency with its owning slider.
*
* @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
* 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
* @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
* 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
* @version 3.0
* <small>
* (<b>Internal version:</b> $Revision: $ $Date: $)
* </small>
* @since OME2.2
*/
class TwoKnobsSliderUI
{
	
	/** Extra space added to the track. */
	static final int            	EXTRA = 3;

	/** Space added to paint the label. */
	static final int            	BUFFER = 1;

	/** The default color of the track. */
	private static final Color  	TRACK_COLOR = Color.LIGHT_GRAY;

	/** Default size of a thumbnail i.e. 16x16. */
	private static final Dimension	DEFAULT_THUMB_SIZE = new Dimension(16, 16);

	/** The component that owns this delegate. */
	private TwoKnobsSlider          component;

	/** Reference to the model. */
	private TwoKnobsSliderModel     model;

	/** The rectangle hosting the track and the two knobs. */
	private Rectangle               trackRect;

	/** The rectangle hosting the ticks. */
	private Rectangle               tickRect;

	/** The rectangle hosting the label. */
	private Rectangle               labelRect;

	/** The rectangle hosting the start value. */
	private Rectangle               startLabelRect;
	
	/** The rectangle hosting the start value. */
	private Rectangle               endLabelRect;
	
	/** The color the track. */
	private Color                   shadowColor;

	/** The color of the font. */
	private Color                   fontColor;

	/** The image used to draw the thumb.  */
	private Image 					thumbImage;
	
	/** The image used to draw the arrow thumb.  */
	private Image 					upArrowImage;

	/** The image used to draw the disabled arrow thumb.  */
	private Image 					disabledUpArrowImage;
	
	/** The image used to draw the thumb when the slider is disabled.  */
	private Image 					disabledThumbImage;

	/** The image used to draw the thumb.  */
	private Image 					thumbImageDarker;
	
	/** The image used to draw the thumb when the slider is disabled.  */
	private Image 					disabledThumbImageDarker;
	
	/** Initializes the components. */
	private void initialize()
	{
		trackRect = new Rectangle();
		tickRect = new Rectangle();
		labelRect = new Rectangle();
		startLabelRect = new Rectangle(); 
		endLabelRect = new Rectangle(); 
		shadowColor = UIManager.getColor("Slider.shadow");
		fontColor = UIUtilities.LINE_COLOR;
	}

	/** Loads the thumb for the two knob slider.*/
	private void createThumbImage()
	{
		// Create the thumb image 
		IconManager icons = IconManager.getInstance();
		ImageIcon icon = icons.getImageIcon(IconManager.THUMB);
		thumbImage = icon.getImage();
		icon = icons.getImageIcon(IconManager.THUMB_DISABLED);
		disabledThumbImage = icon.getImage();
		icon = icons.getImageIcon(IconManager.UP_ARROW_10);
		upArrowImage = icon.getImage();
		icon = icons.getImageIcon(IconManager.UP_ARROW_DISABLED_10);
		disabledUpArrowImage = icon.getImage();
	}

	/**
	 * Paints the ticks.
	 * 
	 * @param g The graphics context.
	 */
	private void paintTicks(Graphics2D g)
	{
		g.setColor(shadowColor);
		g.translate(0, tickRect.y);
		double value = model.getMinimum();
		int xPos = 0;
		double minor = model.getMinorTickSpacing();
		double major = model.getMajorTickSpacing();
		double max = model.getMaximum();
		double min = model.getMinimum();
		if (model.getOrientation() == TwoKnobsSlider.HORIZONTAL) {
			if (minor > 0) {
				while (value <= max) {
					xPos = xPositionForValue(value);
					paintMinorTickForHorizSlider(g, tickRect, xPos);
					value += minor;
				}
			}
			if (major > 0) {
				value = min;
				while (value <= max) {
					xPos = xPositionForValue(value );
					paintMajorTickForHorizSlider(g, tickRect, xPos);
					value += major;
				}
			}
			g.translate(0, -tickRect.y);
		} else {
			g.translate(tickRect.x, 0);
			value = min;
			int yPos = 0;
			if (minor > 0) {
				while (value <= max) {
					yPos = yPositionForValue(value);
					paintMinorTickForVertSlider(g, tickRect, yPos);
					value += minor;
				}
			}
			if (major > 0) {
				value = min;
				while (value <= max) {
					yPos = yPositionForValue(value);
					paintMajorTickForVertSlider( g, tickRect, yPos);
					value += major;
				}
			}
			g.translate(-tickRect.x, 0);
		}    
	}

	/**
	 * Paints the labels.
	 * 
	 * @param g             The graphics context.
	 * @param fontMetrics   Information on how to render the font.
	 */
	private void paintLabels(Graphics2D g, FontMetrics fontMetrics)
	{
		g.setColor(fontColor);
		Map labels = model.getLabels();
		Iterator i = labels.keySet().iterator();
		Double key;
		int value;
		while (i.hasNext()) {
			key = (Double) i.next();
			value = key.intValue();
			if (model.getOrientation() == TwoKnobsSlider.HORIZONTAL) {
				g.translate(0, labelRect.y);
				paintHorizontalLabel(g, fontMetrics, value);
				g.translate(0, -labelRect.y);
			} else {
				g.translate(labelRect.x, 0);
				paintVerticalLabel(g, fontMetrics, value);
				g.translate(-labelRect.x, 0);
			}
		}
	}

	/**
	 * Paints the current values.
	 * 
	 * @param g             The graphics context.
	 * @param fontMetrics   Information on how to render the font.
	 */
	private void paintCurrentValues(Graphics2D g, FontMetrics fontMetrics)
	{
		g.setColor(fontColor);
		String s = ""+model.getStartValue();
		g.drawString(s, startLabelRect.x, fontMetrics.getHeight());
		//g.translate(startLabelRect.x, 0);
		//paintHorizontalLabel(g, fontMetrics, model.getStartValue());
		//g.translate(-startLabelRect.x, 0);
		
		s = ""+model.getEndValue();
		g.drawString(s, startLabelRect.y, fontMetrics.getHeight());
		//g.translate(endLabelRect.x, 0);
		//paintHorizontalLabel(g, fontMetrics, model.getEndValue());
		//g.translate(-endLabelRect.x, 0);
	}
	
	/**
	 * Paints the label for an horizontal slider.
	 * 
	 * @param g             The graphics context.
	 * @param fontMetrics   Information on how to render the font.
	 * @param value         The value to paint.
	 */
	private void paintHorizontalLabel(Graphics2D g, FontMetrics fontMetrics, 
			int value)
	{
		String s = ""+value;
		int labelLeft = xPositionForValue(value)-fontMetrics.stringWidth(s)/2;
		g.translate(labelLeft, 0);
		g.drawString(s, 0, fontMetrics.getHeight());
		g.translate(-labelLeft, 0);
	}

	/**
	 * Paints the label for a vertical slider.
	 * 
	 * @param g             The graphics context.
	 * @param fontMetrics   Information on how to render the font.
	 * @param value         The value to paint.
	 */
	private void paintVerticalLabel(Graphics2D g, FontMetrics fontMetrics,
			int value)
	{
		int v = fontMetrics.getHeight()/2+(fontMetrics.getHeight()/2)%2;
		int labelTop = yPositionForValue(value)-v;
		g.translate(0, labelTop);
		g.drawString(""+value, 0, fontMetrics.getHeight());
		g.translate(0, -labelTop);
	}

	/**
	 * Paints the minor tick for an horizontal slider.
	 * 
	 * @param g         The graphics context.
	 * @param bounds    The bounds of the tick box.
	 * @param x         The x-position.
	 */
	private void paintMinorTickForHorizSlider(Graphics2D g, Rectangle bounds,
			int x)
	{
		g.drawLine(x, 0, x, bounds.height/2-1);
	}

	/**
	 * Paints the major tick for an horizontal slider.
	 * 
	 * @param g         The graphics context.
	 * @param bounds    The bounds of the tick box.
	 * @param x         The x-position.
	 */
	private void paintMajorTickForHorizSlider(Graphics2D g, Rectangle bounds,
											int x)
	{
		g.drawLine(x, 0, x, bounds.height-2);
	}

	/**
	 * Paints the minor tick for an horizontal slider.
	 * 
	 * @param g         The graphics context.
	 * @param bounds    The bounds of the tick box.
	 * @param y         The y-position.
	 */
	private void paintMinorTickForVertSlider(Graphics2D g, Rectangle bounds,
												int y)
	{
		g.drawLine(0, y, bounds.width/2-1, y);
	}

	/**
	 * Paints the major tick for an vertical slider.
	 * 
	 * @param g         The graphics context.
	 * @param bounds    The bounds of the tick box.
	 * @param y         The y-position.
	 */
	private void paintMajorTickForVertSlider(Graphics2D g, Rectangle bounds,
			int y)
	{
		g.drawLine(0, y, bounds.width-2, y);
	}

	/**
	 * Paints the track and the knobs for an horizontal slider.
	 * 
	 * @param g2D The graphic context.
	 */
	private void paintTrackAndKnobsForHorizSlider(Graphics2D g2D)
	{
		int l = xPositionForValue(model.getStartValue());
		int r = xPositionForValue(model.getEndValue());
		if ( component.getImage() != null ){
            g2D.drawImage(component.getImage(), trackRect.x, trackRect.y+3, trackRect.width, trackRect.height-12, null);
            
            g2D.drawRect(trackRect.x, trackRect.y+2, trackRect.width,
                    trackRect.height-11);
		}
		else {
		    if (!component.getColourGradient())
	        {
	            Paint paint = new GradientPaint(0, trackRect.y,
	                UIUtilities.TRACK_GRADIENT_START, 0,
	                trackRect.y+trackRect.height-10,
	                UIUtilities.TRACK_GRADIENT_END, false);
	            g2D.setPaint(paint);
	            g2D.fillRoundRect(trackRect.x, trackRect.y+3, trackRect.width,
	                    trackRect.height-12, trackRect.height/3, 
	                    trackRect.height/3);
	            g2D.setColor(TRACK_COLOR);
	            g2D.drawRoundRect(trackRect.x, trackRect.y+2, trackRect.width,
	                    trackRect.height-11, trackRect.height/3, trackRect.height/3);
	        } else {
	            Color[] colors = component.getGradientColors();
	            Paint paint = new GradientPaint(trackRect.x,
	                    trackRect.y-2,  colors[0], trackRect.width,
	                    trackRect.height+2, colors[1], false);
	            g2D.setPaint(paint);
	            g2D.fillRoundRect(trackRect.x, trackRect.y+2, trackRect.width,
	                    trackRect.height-10, trackRect.height/3, 
	                    trackRect.height/3);
	            g2D.setColor(Color.black);
	            g2D.drawRoundRect(trackRect.x, trackRect.y+2, trackRect.width,
	                    trackRect.height-9, trackRect.height/3, trackRect.height/3);
	        }
		}
		//Draw the knobs
		int w  = component.getKnobWidth();
		int h = component.getKnobHeight();
		Image img;
		int offset = 0;
		if (!component.getColourGradient()) {
			if (model.isEnabled()) img = thumbImage;
			else img = disabledThumbImage;
		} else {
			w = 12;
			h = 12;
			if (model.isEnabled()) img = upArrowImage;
			else img = disabledUpArrowImage;
			offset = 5;	
		}
		if (component.getKnobControl() == TwoKnobsSlider.LEFT) {
			if (model.allowOverlap() && !component.getColourGradient()) {
				img = thumbImageDarker;
				if (!model.isEnabled()) 
					img = disabledThumbImageDarker;
			}
			g2D.drawImage(img, r-w/2, 1+offset, w, h, null);
			if (!component.getColourGradient()) {
				if (model.isEnabled()) img = thumbImage;
				else img = disabledThumbImage;
			}
			g2D.drawImage(img, l-w/2, 1+offset, w, h, null);
		} else  {
			g2D.drawImage(img, l-w/2, 1+offset, w, h, null);
			if (model.allowOverlap() && !component.getColourGradient()) {
				img = thumbImageDarker;
				if (!model.isEnabled()) 
					img = disabledThumbImageDarker;
			}
			g2D.drawImage(img, r-w/2, 1+offset, w, h, null);
		}
	}
	
	/**
	 * Paints the track and the knobs for a vertical slider.
	 * 
	 * @param g2D The graphic context.
	 */
	private void paintTrackAndKnobsForVertSlider(Graphics2D g2D)
	{
		int down = yPositionForValue(model.getStartValue());
		int up = yPositionForValue(model.getEndValue());
		int w = component.getKnobWidth();
		int h = component.getKnobHeight();
		int x = trackRect.x-w/2+(trackRect.width-w)/2;
		Paint paint;
		if (!component.getColourGradient())
		{
			paint = new GradientPaint(trackRect.x+1, trackRect.y+h/2, 
				UIUtilities.TRACK_GRADIENT_START,
				trackRect.x+1+trackRect.width-w-2,
				trackRect.y+h/2, UIUtilities.TRACK_GRADIENT_END, false);

			g2D.setPaint(paint);
			g2D.fillRoundRect(trackRect.x+1, trackRect.y+h/2,
					trackRect.width-w-2, trackRect.height,
					trackRect.width/3, trackRect.width/3);
		} else {
			Color[] colors = component.getGradientColors();
			paint = new GradientPaint(trackRect.x+1, trackRect.y+h/2, 
					colors[0], 
					trackRect.x+1+trackRect.width-w-2, 
					trackRect.y+h/2, colors[1], false);
			g2D.setPaint(paint);
			g2D.fillRoundRect(trackRect.x, trackRect.y+3, trackRect.width,
						trackRect.height-12, trackRect.height/3, 
						trackRect.height/3);
		}
	
		//Draw the knobs
		Image img;
	
		if (!component.getColourGradient()) {
			if (model.isEnabled()) img = thumbImage;
			else img = disabledThumbImage;
		} else {
			w = 10;
			h = 10;
			if (model.isEnabled()) img = upArrowImage;
			else img = disabledUpArrowImage;
		}
		if (component.getKnobControl() == TwoKnobsSlider.LEFT) {
			if (model.allowOverlap() && !component.getColourGradient()) {
				img = thumbImageDarker;
				if (!model.isEnabled()) 
					img = disabledThumbImageDarker;
			}
			g2D.drawImage(img, x, down, w, h, null);
			if (!component.getColourGradient()) {
				if (model.isEnabled()) img = thumbImage;
				else img = disabledThumbImage;
			}
			g2D.drawImage(img, x, up, w, h, null);
		} else {
			g2D.drawImage(img, x, up, w, h, null);
			if (model.allowOverlap() && !component.getColourGradient()) {
				img = thumbImageDarker;
				if (!model.isEnabled()) 
					img = disabledThumbImageDarker;
			}
			g2D.drawImage(img, x, down, w, h, null);
		}
	}

	/**
	 * Determines the boundary of each rectangle composing the slider
	 * according to the font metrics and the dimension of the component.
	 * 
	 * @param fontMetrics   The font metrics.
	 * @param size          The dimension of the component.
	 */
	private void computeRectangles(FontMetrics fontMetrics, Dimension size)
	{ 
		int w = component.getKnobWidth();
		int h = component.getKnobHeight();
		int fontWidth = 
			fontMetrics.stringWidth(model.render(model.getAbsoluteMaximum()));
		int x = 0;
		if (model.getOrientation() == TwoKnobsSlider.HORIZONTAL) {
			x = fontWidth/2;
			//x += w; //06-03
			if (model.isPaintCurrentValues()) {
				int v = fontMetrics.stringWidth(model.render(
						model.getAbsoluteMinimum()));
				v = v/2;
				x += v;
			}
			
			if (model.isPaintEndLabels())
				trackRect.setBounds(x, EXTRA, size.width-2*x, h);
			else
				//trackRect.setBounds(w/2, EXTRA, size.width-2*w, h);
				trackRect.setBounds(w/2, EXTRA, size.width-w, h);

			if (model.isPaintCurrentValues()) {
				int v = fontMetrics.stringWidth(model.render(
						model.getAbsoluteMinimum()));
				v = v/2;
				x += v;
				startLabelRect.setBounds(0, trackRect.y, v, trackRect.y);
				v = fontWidth/2;
				x += v;
				endLabelRect.setBounds(trackRect.x+trackRect.width, 
						trackRect.y, v, trackRect.y);
			}

			if (model.isPaintTicks())
				tickRect = new Rectangle(trackRect.x,
						trackRect.y+trackRect.height,
						trackRect.width, trackRect.height);
			labelRect = new Rectangle(tickRect.x,
					trackRect.y+trackRect.height+tickRect.height,
					trackRect.width, 
					fontMetrics.getHeight()+2*BUFFER);
		} else {
			int y = fontMetrics.getHeight()/2+h;
			if (model.isPaintEndLabels())
				trackRect.setBounds(w/2, y, w+2*EXTRA, size.height-2*y);
			else
				//trackRect.setBounds(x+w-EXTRA, h/2, w+2*EXTRA, 
				//                    size.height-h-h/2);
				trackRect.setBounds(w/2, 0, w+2*EXTRA, size.height-h);
			if (model.isPaintTicks()) 
				tickRect = new Rectangle(trackRect.x+trackRect.width,
						trackRect.y-h, trackRect.width,
						trackRect.height);
			labelRect = new Rectangle(trackRect.x+trackRect.width+
					tickRect.width, trackRect.y, fontWidth+2*BUFFER,
					trackRect.height);
		}
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param component The component that owns this uiDelegate. 
	 *                  Mustn't be <code>null</code>.
	 * @param model     Reference to the model. Mustn't be <code>null</code>.
	 */
	TwoKnobsSliderUI(TwoKnobsSlider component, TwoKnobsSliderModel model)
	{
		if (component == null) throw new NullPointerException("No component");
		if (model == null) throw new NullPointerException("No model");
		this.component = component;
		this.model = model;
		initialize();
		createThumbImage();
	}

	/** Creates darker thumbnails.*/
	void createDarkerImage()
	{
		if (!model.allowOverlap()) return;
		thumbImageDarker = Factory.makeConstrastDecImage(thumbImage);
		disabledThumbImageDarker = Factory.makeConstrastDecImage(disabledThumbImage);
	}
	
	/**
	 * Returns the width of the image knob.
	 * 
	 * @return See above.
	 */
	int getKnobWidth()
	{
		if (thumbImage == null) return DEFAULT_THUMB_SIZE.width;
		return thumbImage.getWidth(null);
	}

	/**
	 * Returns the height of the image knob.
	 * 
	 * @return See above.
	 */
	int getKnobHeight()
	{
		if (thumbImage == null) return DEFAULT_THUMB_SIZE.height;
		return thumbImage.getHeight(null);
	}

	/**
	 * Sets the color of the font.
	 * 
	 * @param c The color to set.
	 */
	void setFontColor(Color c)
	{
		if (c == null) return;
		fontColor = c;
	}

	/**
	 * Determines the x-coordinate of the knob corresponding to the passed
	 * value.
	 * 
	 * @param value The value to map.
	 * @return See above.
	 */
	int xPositionForValue(double value)
	{
	        double min = model.getPartialMinimum();
	        double max = model.getPartialMaximum();
		int trackLength = trackRect.width;
		double valueRange = (double) max-(double) min;
		double pixelsPerValue = (double)trackLength/valueRange;
		int trackLeft = trackRect.x;
		int trackRight = trackRect.x+trackRect.width-1;
		int xPosition = trackLeft;
		xPosition += Math.round(pixelsPerValue*(value-min));
		xPosition = Math.max(trackLeft, xPosition);
		xPosition = Math.min(trackRight, xPosition);
		return xPosition;
	}

	/**
	 * Determines the y-coordinate of the knob corresponding to the passed
	 * value.
	 * 
	 * @param value The value to map.
	 * @return See above.
	 */
	int yPositionForValue(double value)
	{
	        double min = model.getPartialMinimum();
	        double max = model.getPartialMaximum();
		int trackLength = trackRect.height; 
		double valueRange = (double) max-(double) min;
		double pixelsPerValue = (double)trackLength/valueRange;
		int trackTop = trackRect.y;
		int trackBottom = trackRect.y+trackRect.height-1;
		int yPosition= trackTop;
		yPosition += Math.round(pixelsPerValue*((double) max-value));
		yPosition = Math.max(trackTop, yPosition);
		yPosition = Math.min(trackBottom, yPosition);
		return yPosition;
	}

	/**
	 * Determines the value corresponding to the passed x-coordinate.
	 * 
	 * @param xPosition The x-coordinate to map.
	 * @param start Pass <code>true</code> to start calculating from the start,
	 *              <code>false</code> to start from the end.
	 * @return See above.
	 */
	double xValueForPosition(int xPosition, boolean start)
	{
	        double value;
	        double minValue = model.getPartialMinimum();
	        double maxValue = model.getPartialMaximum();
		int trackLength = trackRect.width;
		int trackLeft = trackRect.x; 
		int trackRight = trackRect.x+trackRect.width-1;

		if (xPosition <= trackLeft)  value = minValue;
		else if (xPosition >= trackRight) value = maxValue;
		else {
			int distanceFromTrackLeft = trackRight-xPosition;
			if (start) distanceFromTrackLeft = xPosition-trackLeft;
			double valueRange = (double) maxValue-(double) minValue;
			double valuePerPixel = Math.ceil(valueRange/trackLength*1000)/1000;
			double valueFromTrackLeft = 
			  Math.ceil(distanceFromTrackLeft*valuePerPixel*1000)/1000;
			if (start) value = minValue+valueFromTrackLeft;
			else value = maxValue-valueFromTrackLeft;
		}
		return value;
	}

	/**
	 * Determines the value corresponding to the passed y-coordinate.
	 * 
	 * @param yPosition The y-coordinate to map.
	 * @param start Pass <code>true</code> to start calculating from the start,
	 *              <code>false</code> to start from the end.
	 * @return See above.
	 */
	double yValueForPosition(int yPosition, boolean start)
	{
	        double value;
	        double minValue = model.getPartialMinimum();
	        double maxValue = model.getPartialMaximum();
		int trackLength = trackRect.height;
		int trackTop = trackRect.y;
		int trackBottom = trackRect.y+trackRect.height-1;

		if (yPosition <= trackTop) value = maxValue;
		else if (yPosition >= trackBottom) value = minValue;
		else {
			int distanceFromTrackTop = trackBottom-yPosition;
			if (start) distanceFromTrackTop = yPosition-trackTop;
			double valueRange = (double) maxValue-(double) minValue;
			double valuePerPixel = Math.ceil(valueRange/trackLength*1000)/1000;
			double valueFromTrackTop = 
				Math.ceil(distanceFromTrackTop*valuePerPixel*1000)/1000;
			//value = maxValue-valueFromTrackTop;
			if (!start) value = minValue+valueFromTrackTop;
			else value = maxValue-valueFromTrackTop;
		}
		return value;
	}

	/**
	 * Paints the slider.
	 * 
	 * @param g2D   The graphics context.
	 * @param size  The dimension of the component.
	 */
	void paintComponent(Graphics2D g2D, Dimension size)
	{
		FontMetrics fontMetrics = g2D.getFontMetrics();
		computeRectangles(fontMetrics, size);
		//Draw the track
		g2D.setColor(TRACK_COLOR);
		if (model.getOrientation() == TwoKnobsSlider.HORIZONTAL)
			paintTrackAndKnobsForHorizSlider(g2D);
		else paintTrackAndKnobsForVertSlider(g2D);
		if (model.isPaintTicks()) paintTicks(g2D);
		if (model.isPaintLabels() || model.isPaintEndLabels()) 
			paintLabels(g2D, fontMetrics); 
	}

}