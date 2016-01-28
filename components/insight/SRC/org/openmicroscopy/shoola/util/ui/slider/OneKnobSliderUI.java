/*
 * org.openmicroscopy.shoola.util.ui.slider.OneKnobSliderUI
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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

import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.plaf.basic.BasicSliderUI;

import org.openmicroscopy.shoola.util.ui.IconManager;
import org.openmicroscopy.shoola.util.ui.UIUtilities;


/** 
* The UI of the <code>OnknobSlider</code>.
*
* @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
* 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
* @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
* 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
* @version 3.0
* @since OME2.2
*/
public class OneKnobSliderUI
	extends BasicSliderUI
{

	/** Spacing between the arrow and the end of the slider track. */
	private static final int 	ARROW_SPACE = 2;

	/** Spacing between the arrow and the end label. */
	private static final int 	TEXT_SPACE = 2;
	
	/** Offset to the left of the mouse used for placing tooltip. */
	//private static final int 	TOOLTIP_OFFSET = 25;

	/** Image used for the thumb. */
	private  Image 				thumbImage;


	/** Image used for the thumb when the component is disabled. */
	private  Image 				disabledThumbImage;

	/** Image used for the up arrow. */
	private  Image 				upArrowImage;

	/** Image used for the up arrow when the component is disabled. */
	private  Image 				upArrowDisabledImage;

	/** Image used for the down arrow. */
	private  Image 				downArrowImage;

	/** Image used for the down arrow when the component is disabled. */
	private  Image 				downArrowDisabledImage;

	/** Image used for the left arrow. */
	private  Image 				leftArrowImage;

	/** Image used for the left arrow when the component is disabled. */
	private  Image 				leftArrowDisabledImage;

	/** Image used for the right arrow. */
	private  Image 				rightArrowImage;

	/** Image used for the right arrow when the component is disabled. */
	private  Image 				rightArrowDisabledImage;

	/** Show arrow at the side of the slider track. */
	private  boolean			showArrows;

	/** Area in which the min arrow will reside. */
	private  Rectangle			minArrowRect;

	/** Area in which the max arrow will reside. */
	private  Rectangle			maxArrowRect;

	/** Dialog used to display tooltip containing the position of the slider. */
	//private  TipDialog			tipDialog;

	/** The end label displayed at top, or left of slider. */
	private  String				endLabel;

	/** Show the end label if set. */
	private  boolean			showEndLabel;

	/** Show the tip label ovet the thumb when slider moved. */
	//private boolean 			showTipLabel;

	/** The rect holding the location of the end label. */
	private Rectangle			endLabelRect;

	/** The height of the end label. */
	private int					labelHeight;

	/** The width of the end label. */
	private	int 				labelWidth;

	/** This variable is set to <code>true</code> if user dragging thumb. */
	protected boolean 			isDragging;

	/** The height of the arrow. */
	private int					arrowHeight;

	/** The width of the arrow. */
	private int					arrowWidth;

	/** The width of the bottom/left arrow. */
	private int					minArrowWidth;

	/** The height of the bottom/left arrow. */
	private int					minArrowHeight;

	/** The width of the thumb. */
	private int					thumbWidth;

	/** The height of the thumb. */
	private int					thumbHeight;
	
	/** Load the thumb and arrow images. */
	private void loadThumbArrowImage()
	{
		IconManager icons = IconManager.getInstance();

		ImageIcon img = icons.getImageIcon(IconManager.THUMB);
		thumbWidth = img.getIconWidth();
		thumbHeight = img.getIconHeight();
		thumbImage = img.getImage();
		img = icons.getImageIcon(IconManager.THUMB_DISABLED);
		disabledThumbImage = img.getImage();

		img = icons.getImageIcon(IconManager.UP_ARROW_DISABLED_10);
		arrowWidth = img.getIconWidth();
		arrowHeight = img.getIconHeight();
		minArrowHeight = arrowHeight;
		minArrowWidth = arrowWidth;
		upArrowDisabledImage = img.getImage();
		img = icons.getImageIcon(IconManager.DOWN_ARROW_DISABLED_10);
		downArrowDisabledImage = img.getImage();
		img = icons.getImageIcon(IconManager.LEFT_ARROW_DISABLED_10);
		leftArrowDisabledImage = img.getImage();
		img = icons.getImageIcon(IconManager.RIGHT_ARROW_DISABLED_10);
		rightArrowDisabledImage = img.getImage();
		img = icons.getImageIcon(IconManager.UP_ARROW_10);
		upArrowImage = img.getImage();
		img = icons.getImageIcon(IconManager.DOWN_ARROW_10);
		downArrowImage = img.getImage();
		img = icons.getImageIcon(IconManager.LEFT_ARROW_10);
		leftArrowImage = img.getImage();
		img = icons.getImageIcon(IconManager.RIGHT_ARROW_10);
		rightArrowImage = img.getImage();
	}	

	/**
	 * This method calculates the size and position of the arrows used displayed
	 * in the trackRect.  
	 */
	private void calculateArrowRect()
	{
		if (slider.getOrientation() == JSlider.HORIZONTAL) {
			int offsetY = (trackRect.height-minArrowHeight)/2-1;
			minArrowRect = new Rectangle(trackRect.x-
					(minArrowWidth+thumbWidth/2+ARROW_SPACE), 
					trackRect.y+offsetY, minArrowWidth, 
					minArrowHeight);
			offsetY = (trackRect.height-arrowHeight)/2-1;
			maxArrowRect = new Rectangle(trackRect.x+trackRect.width+
					ARROW_SPACE+thumbWidth/2, trackRect.y+offsetY, arrowWidth, 
					arrowHeight);
		} else {
			int offsetX = (trackRect.width-arrowWidth)/2;
			if (arrowWidth != minArrowWidth) offsetX +=1;
			maxArrowRect = new Rectangle(trackRect.x+offsetX, trackRect.y-
					(arrowHeight+thumbHeight/2+ARROW_SPACE), arrowWidth, 
					arrowHeight);
			offsetX = (trackRect.width-minArrowWidth)/2;
			if (arrowWidth != minArrowWidth) offsetX +=1;
			minArrowRect = new Rectangle(trackRect.x+offsetX, trackRect.y+
					trackRect.height+ARROW_SPACE+thumbHeight/2, minArrowWidth, 
					minArrowHeight);
		}
	}

	/**
	 * This method calculates the size and position of the end label displayed
	 * in the trackRect.  
	 */
	private void calculateEndLabelRect()
	{
		if (slider.getOrientation() == JSlider.HORIZONTAL) {
			int offsetY = trackRect.height+labelHeight/2+1;
			endLabelRect = new Rectangle(0, offsetY, labelWidth, labelHeight);
		} else {
			int offsetX = trackRect.width/2-labelWidth/2+1;
			endLabelRect = new Rectangle(offsetX, trackRect.y-
					(minArrowRect.height+labelHeight+TEXT_SPACE),
					labelWidth, labelHeight);
		}
	}

	/**
	 * Paints the vertical track, and arrows if selected, this method is called
	 * from the {@link #paintTrack(Graphics)} method. 
	 * 
	 * @param g Graphics context.
	 */
	private void paintVerticalTrack(Graphics2D g)
	{
		Paint paint = new GradientPaint(trackRect.x+trackRect.width/2-2, 
				trackRect.y, UIUtilities.TRACK_GRADIENT_START, 
				trackRect.x+trackRect.width/2+2,
				trackRect.y, UIUtilities.TRACK_GRADIENT_END, false);
		g.setPaint(paint);
		g.fillRoundRect(trackRect.x+trackRect.width/2-2, trackRect.y, 4, 
				trackRect.height, 4, 4);
		g.setPaint(UIUtilities.LINE_COLOR);
		if (showArrows) {
			if (slider.isEnabled()) {
				g.drawImage(downArrowImage, minArrowRect.x, minArrowRect.y, 
						minArrowRect.width, minArrowRect.height, null);
				g.drawImage(upArrowImage, maxArrowRect.x, maxArrowRect.y, 
						maxArrowRect.width, maxArrowRect.height, null);
			} else {
				g.drawImage(downArrowDisabledImage, minArrowRect.x, 
						minArrowRect.y, minArrowRect.width, minArrowRect.height,
						null);
				g.drawImage(upArrowDisabledImage, maxArrowRect.x,
						maxArrowRect.y, maxArrowRect.width, maxArrowRect.height, 
						null);
			}           
		}
		if (showEndLabel && endLabel != null)
			g.drawString(endLabel, endLabelRect.x, endLabelRect.y);
	}

	/**
	 * Paints the Horizontal track, and arrows if selected, this method is 
	 * called from the {@link #paintTrack(Graphics)} method. 
	 * 
	 * @param g Graphics context.
	 */
	private void paintHorizontalTrack(Graphics2D g)
	{
		Paint paint = new GradientPaint(0, trackRect.y+thumbRect.height/2-3, 
				UIUtilities.TRACK_GRADIENT_START, 0, 
				trackRect.y+thumbRect.height/2+2, 
				UIUtilities.TRACK_GRADIENT_END, false);
		g.setPaint(paint);
		g.fillRoundRect(trackRect.x, trackRect.y+thumbRect.height/2-3, 
				trackRect.width, 4, 4, 4);
		g.setPaint(UIUtilities.LINE_COLOR);
		if (showArrows) {
			if (slider.isEnabled()) {
				g.drawImage(leftArrowImage, minArrowRect.x, minArrowRect.y, 
						minArrowRect.width, minArrowRect.height, null);
				g.drawImage(rightArrowImage, maxArrowRect.x, maxArrowRect.y, 
						maxArrowRect.width, maxArrowRect.height, null);
			} else {
				g.drawImage(leftArrowDisabledImage, minArrowRect.x, 
						minArrowRect.y, minArrowRect.width, minArrowRect.height,
						null);
				g.drawImage(rightArrowDisabledImage, maxArrowRect.x,
						maxArrowRect.y, maxArrowRect.width, maxArrowRect.height, 
						null);
			}
		}
		if (showEndLabel && endLabel != null)
			g.drawString(endLabel, endLabelRect.x, endLabelRect.height);
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param slider parent slider component.
	 */
	OneKnobSliderUI(OneKnobSlider slider) 
	{
		super(slider);
		showArrows = true;
		loadThumbArrowImage();
		showEndLabel = false;
		endLabelRect = new Rectangle();
	}

	/** 
	 * Sets the end label. 
	 * 
	 * @param endLabel
	 */
	void setEndLabel(String endLabel)
	{
		this.endLabel = endLabel;
		labelWidth = 6;
		labelHeight = 12;
	}

	/**
	 * Will show the end tip label which hovers over the thumb if set.
	 *  
	 * @param show see above.
	 */
	void setShowTipLabel(boolean show)
	{
		//showTipLabel = show;
		//if (showTipLabel) tipDialog = new TipDialog(endLabel);
	}

	/**
	 * Shows the end label if set.
	 * 
	 * @param show see above.
	 */
	void setShowEndLabel(boolean show) { showEndLabel = show; }

	/**
	 * Shows or hides the arrows on the track.
	 * 
	 * @param isShow See above.
	 */
	void setShowArrows(boolean isShow)
	{
		showArrows = isShow;
		this.calculateGeometry();
	}

	/**
	 * Replaces the arrows icons by the specified one.
	 * 
	 * @param up	The icon displayed at the top of the slider if
	 * 				vertical, at the right of the slider if horizontal.
	 * @param down  The icon displayed at the bottom of the slider if
	 * 				vertical, at the left of the slider if horizontal.
	 */
	void setArrowsImageIcon(ImageIcon up, ImageIcon down)
	{
		setArrowsImageIcon(up, down, null, null);
	}

	/**
	 * Replaces the arrows icons by the specified one.
	 * 
	 * @param up	The icon displayed at the top of the slider if
	 * 				vertical, at the right of the slider if horizontal.
	 * @param down  The icon displayed at the bottom of the slider if
	 * 				vertical, at the left of the slider if horizontal.
	 * @param disabledUp The disabled icon displayed at the top of the slider if
	 * 				vertical, at the right of the slider if horizontal.
	 * @param disabledDown The disabled icon displayed at the bottom of the 
	 * 				slider if vertical, at the left of the slider if horizontal.
	 */
	void setArrowsImageIcon(ImageIcon up, ImageIcon down, 
			ImageIcon disabledUp, ImageIcon disabledDown)
	{
		if (slider.getOrientation() == JSlider.HORIZONTAL) {
			rightArrowImage = up.getImage();
			if (disabledUp != null)
				rightArrowDisabledImage = disabledUp.getImage();
			else rightArrowDisabledImage = up.getImage();
			leftArrowImage = down.getImage();
			if (disabledDown != null)
				leftArrowDisabledImage = disabledDown.getImage();
			else leftArrowDisabledImage = down.getImage();
		} else {
			upArrowImage = up.getImage();
			if (disabledUp != null)
				upArrowDisabledImage = disabledUp.getImage();
			else upArrowDisabledImage = up.getImage();
			downArrowImage = down.getImage();
			if (disabledDown != null)
				downArrowDisabledImage = disabledDown.getImage();
			else downArrowDisabledImage = down.getImage();
		}
		arrowWidth = up.getIconWidth();
		arrowHeight = up.getIconHeight();
		minArrowWidth = down.getIconWidth();
		minArrowHeight = down.getIconHeight();
		this.calculateGeometry();
	}
	
	/**
	 * Returns <code>true</code> if the  arrows on the track, 
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isShowArrows() { return showArrows; }

	/**
	 * Extends the {@link #calculateTrackBuffer()} to allow the extra space 
	 * required to display the arrows on the track. 
	 * @see BasicSliderUI#calculateTrackBuffer()
	 */
	protected void calculateTrackBuffer()
	{
		super.calculateTrackBuffer();
		if (showArrows)
			if (slider.getOrientation() == JSlider.HORIZONTAL) {
				if (arrowWidth > minArrowWidth)
					trackBuffer += arrowWidth+ARROW_SPACE;
				else 
					trackBuffer += minArrowWidth+ARROW_SPACE;
			} else {
				if (arrowHeight > minArrowHeight)
					trackBuffer += arrowHeight+ARROW_SPACE;
				else 
					trackBuffer += minArrowHeight+ARROW_SPACE;
			}

		if	(showEndLabel)
			if (slider.getOrientation() == JSlider.HORIZONTAL)
				trackBuffer += labelWidth+TEXT_SPACE;
			else
				trackBuffer += labelHeight+TEXT_SPACE;
	}

	/**
	 * Overridden to get the size of the thumb.
	 * @see BasicSliderUI#getThumbSize()
	 */
	protected Dimension getThumbSize() 
	{
		return new Dimension(thumbWidth, thumbHeight);
	}

	/**
	 * Overridden to calculate the size of the thumb rectangle. 
	 * @see BasicSliderUI#calculateThumbSize()
	 */
	public void calculateThumbSize()
	{
		this.thumbRect = new Rectangle(0, 0, thumbWidth, thumbHeight);
	}

	/**
	 * Overridden to calculate the size of the thumb rectangle. 
	 * @see BasicSliderUI#paintFocus(Graphics g)
	 */
	public void paintFocus(Graphics g) {}

	/**
	 * Overridden this method will paint the gradient on the slider track
	 * @see BasicSliderUI#paintTrack(Graphics)
	 */
	public void paintTrack(Graphics og)
	{
		if (slider.getOrientation() == JSlider.HORIZONTAL)
			paintHorizontalTrack((Graphics2D) og);
		else
			paintVerticalTrack((Graphics2D) og);
	}

	/**
	 * Overridden to paint thumb on slider. 
	 * @see BasicSliderUI#paintThumb(Graphics)
	 */
	public void paintThumb(Graphics og)
	{
		Graphics2D g = (Graphics2D) og;
		if (slider.isEnabled())
			g.drawImage(thumbImage, thumbRect.x, thumbRect.y, thumbRect.width, 
					thumbRect.height, null);
		else 
			g.drawImage(disabledThumbImage, thumbRect.x, thumbRect.y, 
					thumbRect.width, thumbRect.height, null);
	}

	/**
	 * Overridden to calculate the geometry of the slider, this calls the 
	 * {@link BasicSliderUI#calculateGeometry} and to add extra calculations to 
	 * calculate the <code>ArrowRect</code> if showArrows is <code>true</code>. 
	 * @see BasicSliderUI#calculateGeometry()
	 */
	public void calculateGeometry()
	{
		super.calculateGeometry();
		if (showArrows) calculateArrowRect();
		if (showEndLabel) calculateEndLabelRect();
	}

	/**
	 * Overridden to avoid flicking.
	 * @see BasicSliderUI#paint(Graphics, JComponent)
	 */
	public void paint(Graphics g, JComponent c)   
	{
		recalculateIfInsetsChanged();
		recalculateIfOrientationChanged();
		Rectangle clip = g.getClipBounds();

		if (!clip.intersects(trackRect) && slider.getPaintTrack())
			calculateGeometry();

		if (slider.getPaintTrack() && (clip.intersects(trackRect) || 
				clip.intersects(minArrowRect) || clip.intersects(maxArrowRect)
				|| clip.intersects(endLabelRect))) 
			paintTrack(g);

		if (slider.getPaintTicks() && clip.intersects(tickRect)) 
			paintTicks(g);

		if (slider.getPaintLabels() && clip.intersects(labelRect)) 
			paintLabels(g);

		if (slider.hasFocus() && clip.intersects(focusRect)) 
			paintFocus(g);      

		if (clip.intersects(thumbRect)) 
			paintThumb(g);
	}

	/**
	 * Assign the new overloaded trackListener to the slider. 
	 * 
	 * @param slider Parent slider.
	 * @return TrackListner New listener.
	 * @see BasicSliderUI#createTrackListener(JSlider)
	 */
	protected TrackListener createTrackListener(JSlider slider) 
	{
		return new TrackListener2();
	}

	/** 
	 * Overridden TrackListener class, as we wish to extend the functionality
	 * of the on track click events.  
	 */
	public class TrackListener2 
		extends TrackListener 
	{

		/**
		 * Overridden to determine when a drag event ends. 
		 * This method will also determine when the tool tip Dialog should 
		 * stop showing.
		 */
		public void mouseReleased(MouseEvent event)
		{
			super.mouseReleased(event);
			if (isDragging && slider instanceof OneKnobSlider) {
				isDragging = false;
				((OneKnobSlider) slider).onMouseReleased();
			}
			
			/*
          if (showTipLabel && tipDialog != null)
			{
				if (tipDialog.isVisible()) tipDialog.setVisible(false);
			}	
          slider.repaint();
			 */
		}

		/**
		 * This method will detect a click on the track or min/max arrows and 
		 * behave accordingly.
		 * If the user clicks in the track then the thumb is moved to that 
		 * position and the value updated. If the user clicks the arrows then
		 * the value is incremented or decremented by one depending on which 
		 * slider was clicked. 
		 * 
		 * @param event mouseEvent.
		 */
		public void mouseClicked(MouseEvent event)
		{   
			// Check to see that the slider is enabled before proceeeding.
			if (!slider.isEnabled())
				return;
			isDragging = true;
			// Get mouse x, y positions.
			currentMouseX = event.getX();
			currentMouseY = event.getY();

			int value; 
			scrollTimer.stop();

			if (showArrows)
			{
				if (minArrowRect.contains(currentMouseX, currentMouseY))
				{
					value = slider.getValue();
					isDragging = false;
					if (value > slider.getMinimum())
					{
						slider.setValue(value-1);
						slider.repaint();
					}
					return;
				}
				if (maxArrowRect.contains(currentMouseX, currentMouseY))
				{
					value = slider.getValue();
					isDragging = false;
					if (value < slider.getMaximum())
					{
						slider.setValue(value+1);
						slider.repaint();
					}
					return;
				}
			}

			if (trackRect.contains(currentMouseX, currentMouseY))
			{
				// Depending on the slider orientation lets move the thumb to the 
				// position clicked by the user. 
				switch (slider.getOrientation()) {
					case JSlider.HORIZONTAL:
						value = valueForXPosition(currentMouseX);
						slider.setValue(value);
						break;
					case JSlider.VERTICAL:
						value = valueForYPosition(currentMouseY);
						slider.setValue(value);
				}
			}
		}

		/**
		 * Overloaded the <code>mousePressed</code> event in the TrackListener.
		 */
		public void mousePressed(MouseEvent event) 
		{
			// Check to see that the slider is enabled before proceeeding. 
			if (!slider.isEnabled())
				return;
			isDragging = true;
			// Get mouse x, y positions.
			currentMouseX = event.getX();
			currentMouseY = event.getY();

			// If the slider has {@link #setFocusEnabled} true then 
			// request focus.
			if (slider.isRequestFocusEnabled())
				slider.requestFocus();

			// Check to see if the thumb was clicked. 
			if (thumbRect.contains(currentMouseX, currentMouseY)) 
				super.mousePressed(event);

			
			if (showArrows)
			{
				if (minArrowRect.contains(currentMouseX, currentMouseY))
				{
					int value = slider.getValue();
					isDragging = false;
					if (value > slider.getMinimum())
					{
						//scrollTimer.stop();
						scrollListener.setScrollByBlock(false);
						scrollListener.setDirection(
								OneKnobSliderUI.NEGATIVE_SCROLL);
						//scrollTimer.start();
						slider.repaint();
					}
					return;
				}
				if (maxArrowRect.contains(currentMouseX, currentMouseY))
				{
					int value = slider.getValue();
					isDragging = false;
					if (value < slider.getMaximum())
					{
						//scrollTimer.stop();
						scrollListener.setScrollByBlock(false);
						scrollListener.setDirection(
								OneKnobSliderUI.POSITIVE_SCROLL);
						//scrollTimer.start();
						slider.repaint();
					}
					return;
				}
			}

			slider.repaint();
		}

		/**
		 * Overridden function of the slider track listener method mouseDragged
		 * as this method relied on the private member of isDragged in sliderUI.
		 * Has to override this as we could not set the isDragging variable in
		 * the basicSliderUI. :-( *Why private??!*
		 */
		public void mouseDragged(MouseEvent event) 
		{
			super.mouseDragged(event);
			isDragging = true;
			/*
			if (showTipLabel && tipDialog != null && endLabel != null &&
				slider.isVisible())
			{
				Point location = slider.getLocationOnScreen();
				location.x += thumbRect.x+TOOLTIP_OFFSET;
				location.y += thumbRect.y;

				tipDialog.setTipString(
						endLabel+" : " + slider.getValue());
				tipDialog.setLocation(location);
				tipDialog.setVisible(true);
			}
			 */
		}
	}
	
}
