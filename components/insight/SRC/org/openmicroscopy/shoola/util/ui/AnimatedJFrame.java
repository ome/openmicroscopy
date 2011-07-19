/*
 * org.openmicroscopy.shoola.util.ui.AnimatedJFrame 
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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.border.OneLineBorder;

/** 
 * Animated frame.
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
public class AnimatedJFrame 
	extends JFrame
	implements ActionListener
{

	/** The default value of the duration of the animation. */
	public static final float 	DURATION = 300f;
	
	/** The default value of the animation waiting time. */
	public static final int		SLEEP = 20;
	
	/** Animation direction's constants. */
	private static final int 	INCOMING = 1;
	
	/** Animation direction's constants. */
	public static final int 	OUTGOING = -1;
	
	/** Orientation of the animation .*/
	public static final int		UP_MIDDLE = 0;
	
	/** Orientation of the animation .*/
	public static final int		DOWN = 1;
	
	/** Orientation of the animation .*/
	public static final int		UP_RIGHT = 2;
	
	/** Orientation of the animation .*/
	public static final int		UP_LEFT = 3;
	
	/** The color of the line's border. */
	private static final Color	LINE_COLOR = Color.black;
	
	/** The duration of the animation, default value is {@link #DURATION}. */
	private float 				duration;
	
	/** The value of the animation waiting time. */
	private int					sleep;
	
	/** The glass pane. */
	private JPanel				glass;
	
	/** Flag indicating if the animations is started or stopped. */
	private boolean 			animating;
	
	/** 
	 * The direction of the animation either {@link #INCOMING} or 
	 * {@link #OUTGOING}. 
	 */
	private int 				animationDir;
	
	/** The component displaying the animation. */
	private AnimatedPane 		animatingPane;
	
	/** The time at which the animation started. */
	private long				animationStart;
	
	/** The timer controlling the animation. */
	private Timer 				animationTimer;
	
	/** The content pane of the dialog to show. */
	private JComponent 			sheet;
	
	/** One of the constants orientation. */
	private int					orientation;
	
	/** The partial line border, the value depends on the orientation. */
	private OneLineBorder	border;
	
	/** Flag indicating to close the application after a given time. */
	private boolean				closeAfter;
	
	/** 
	 * The timer used to hide the animation after a given time,
	 * only used if the flag {@link #closeAfter} is <code>true</code>.
	 */
	private Timer 				timer;
	
	/** The extra space to remove. */
	//private int					bottomSpace;
	
	/** Where to show the sheet, default is a <code>(0, 0)</code>. */
	private Point				location;
	
	/** Initializes the components. */
	private void initialize()
	{
		location = new Point(0, 0);
		duration = DURATION;
		sleep = SLEEP;
		animatingPane = new AnimatedPane(this);
		animatingPane.requestFocus();
		glass = (JPanel) getGlassPane();
		glass.setLayout(new GridBagLayout());
		setOrientation(DOWN);
	}
	
	/** Starts the animation. */
	private void startAnimation()
	{
		glass.remove(animatingPane);
		glass.remove(sheet);
		glass.validate();
		glass.repaint();
		animatingPane.setSource(sheet);
		GridBagConstraints c = new GridBagConstraints();
		c.gridy = 0;
		int h, w;
		Dimension d;
		switch (orientation) {
			case DOWN:
				c.anchor = GridBagConstraints.NORTH;
				glass.add(animatingPane, c);
				c.gridy++;
				c.weighty = Integer.MAX_VALUE;
				glass.add(Box.createGlue(), c);
				break;
			case UP_MIDDLE:
				//c.anchor = GridBagConstraints.SOUTHWEST;
				h = glass.getHeight()-sheet.getHeight()-location.y;
				if (glass.getLayout() == null) {
					d = getSize();
					w = (d.width-sheet.getPreferredSize().width)/2;
					animatingPane.setLocation(w, h);
					glass.add(animatingPane);
				} else {
					glass.add(Box.createVerticalStrut(h), c);
					c.weightx = 0.5;
					c.gridy++;
					glass.add(animatingPane, c);
				}
				break;
			case UP_RIGHT:
				c.anchor = GridBagConstraints.SOUTHEAST;
				h = glass.getHeight()-sheet.getHeight()-location.y;
				glass.add(Box.createVerticalStrut(h), c);
				c.weightx = 0.9;
				c.gridy++;
				glass.add(animatingPane, c);
				break;
			case UP_LEFT:
				h = glass.getHeight()-sheet.getHeight()-location.y;
				if (glass.getLayout() == null) {
					animatingPane.setLocation(0, h);
					glass.add(animatingPane);
				} else {
					c.anchor = GridBagConstraints.SOUTHWEST;
					glass.add(Box.createVerticalStrut(h), c);
					c.weightx = 0.1;
					c.gridy++;
					glass.add(animatingPane, c);
				}
		}
		glass.setVisible(true);
		animationStart = System.currentTimeMillis();
		if (animationTimer == null)
			animationTimer = new Timer(sleep, this);
		animating = true;
		animationTimer.start();
	}
	
	/** Stops the animation. */
	private void stopAnimation()
	{
		animationTimer.stop();
		animating = false;
	}
	
	/** Stops the display of the dialog. */
	private void finish()
	{
		glass.remove(animatingPane);
		glass.remove(sheet);
		GridBagConstraints c = new GridBagConstraints();
		c.gridy = 0;
		c.gridx = 0;
		int h, w;
		c.anchor = GridBagConstraints.SOUTHWEST;
		Dimension d;
		switch (orientation) {
			case DOWN:
				c.anchor = GridBagConstraints.NORTH;
				glass.add(sheet, c);
				c.gridy++;
				c.weighty = Integer.MAX_VALUE;
				glass.add(Box.createGlue(), c);
				break;
			case UP_MIDDLE:
				h = glass.getHeight()-sheet.getHeight()-location.y;
				if (glass.getLayout() == null) {
					d = getSize();
					w = (d.width-sheet.getPreferredSize().width)/2;
					sheet.setLocation(w, h);
					glass.add(sheet);
				} else {
					glass.add(Box.createVerticalStrut(h), c);
					c.gridy++;
					glass.add(sheet, c);
				}
				break;
			case UP_RIGHT:
				h = glass.getHeight()-sheet.getHeight()-location.y;
				glass.add(Box.createVerticalStrut(h), c);
				c.gridy++;
				d = getSize();
				w = d.width-sheet.getPreferredSize().width;
				glass.add(Box.createHorizontalStrut(w), c);
				c.gridx++;
				glass.add(sheet, c);
				break;
			case UP_LEFT:
				h = glass.getHeight()-sheet.getHeight()-location.y;
				if (glass.getLayout() == null) {
					sheet.setLocation(0, h);
					glass.add(sheet);
				} else {
					glass.add(Box.createVerticalStrut(h), c);
					c.gridy++;
					c.weightx = 0.1;
					//glass.add(Box.createHorizontalStrut(5), c);
					c.gridx++;
					glass.add(sheet, c);
				}
		}
		glass.revalidate();
		glass.repaint();
		if (closeAfter) {
			if (timer == null) {
				timer = new Timer(500, new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						hideAnimation();
					}
				}); //fire every half second
				timer.setInitialDelay(2000); //first delay 2 seconds
				timer.setRepeats(false);
			}
		    timer.start();
		}
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param title The title of the frame.
	 */
	public AnimatedJFrame(String title)
	{
		super(title);
		initialize();
	}
	
	/**
	 * Indicates to close after a set number of seconds. 
	 * @param closeAfter The value to set.
	 */
	public void setCloseAfter(boolean closeAfter)
	{
		this.closeAfter = closeAfter;
	}
	
	/**
	 * Returns the orientation selected.
	 * 
	 * @return See above.
	 */
	public int getOrientation() { return orientation; }
	
	/**
	 * Sets the duration of the animation.
	 * 
	 * @param duration The value to set.
	 */
	public void setDuration(float duration)
	{ 
		if (duration <= 0) duration = DURATION;
		this.duration = duration; 
	}
	
	/**
	 * Sets the animation waiting time.
	 * 
	 * @param sleep The value to set.
	 */
	public void setSleep(int sleep) { this.sleep = sleep; }

	/**
	 * Sets the orientation and builds the border accordingly.
	 * 
	 * @param orientation 	One of the orientation constants defined by this 
	 * 						class.
	 */
	public void setOrientation(int orientation)
	{
		switch (orientation) {
			case UP_MIDDLE:
			case UP_RIGHT:
			case UP_LEFT:
				this.orientation = orientation;
				border = new OneLineBorder(OneLineBorder.BOTTOM, LINE_COLOR);
				break;
			case DOWN:	
			default:
				this.orientation = DOWN;
				border = new OneLineBorder(OneLineBorder.TOP, LINE_COLOR);
				break;
			
		}
		if (sheet != null) sheet.setBorder(border);
		animatingPane.setBorder(border);
	}
	
	/**
	 * Shows the passed dialog as a sheet.
	 * 
	 * @param dialog 	The dialog to show.
	 * @param location 	The point where to show the sheet.
	 * @return See above.
	 */
	public JComponent showJDialogAsSheet(JDialog dialog, Point location)
	{
		return showJDialogAsSheet(dialog, location, DOWN);
	}
	
	/**
	 * Shows the passed dialog as a sheet.
	 * 
	 * @param dialog 		The dialog to show.
	 * @param location 		The point where to show the sheet.
	 * @param orientation 	One of the orientation constants defined by this 
	 * 						class.
	 * @return See above.
	 */
	public JComponent showJDialogAsSheet(JDialog dialog, Point location, 
			int orientation)
	{
		if (dialog == null) return null;
		this.location = location;
		sheet = (JComponent) dialog.getContentPane();
		if (glass.getLayout() == null) {
			animatingPane.setSize(dialog.getSize());
			animatingPane.setPreferredSize(dialog.getPreferredSize());
			sheet.setSize(dialog.getSize());
			sheet.setPreferredSize(dialog.getPreferredSize());
			glass.remove(animatingPane);
			glass.remove(sheet);
		} else {
			glass.removeAll();
		}
		setOrientation(orientation);
		glass.validate();
		glass.repaint();
		animationDir = INCOMING;
		startAnimation();
		return sheet;
	}
	
	/** Stops the animation and hides the components. 
	 * 
	 * @param visible 	Pass <code>true</code> to keep the glass pane visible, 
	 * 					<code>false</code> otherwise.
	 */
	public void hideAnimation(boolean visible)
	{
		animationDir = OUTGOING;
		//glass.removeAll();
		glass.remove(animatingPane);
		glass.remove(sheet);
		glass.validate();
		glass.repaint();
		stopAnimation();
		glass.setVisible(visible);
		if (timer != null && closeAfter) timer.stop();
	}
	
	/** Stops the animation and hides the components. */
	public void hideAnimation() { hideAnimation(false); }
	
	/**
	 * Starts or ends the animation.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		if (!animating) return;
		if (sheet == null) {
			if (timer != null) timer.stop();
			return;
		}
		float percent = System.currentTimeMillis()-animationStart;
		percent = percent/DURATION;
		percent = Math.min(1.0f, percent);
		int h = 0;
		if (animationDir == INCOMING)
			h = (int) (percent*sheet.getHeight());
		else 
			h = (int) ((1.0f- percent)*sheet.getHeight());
		animatingPane.setAnimatingHeight(h);
		animatingPane.repaint();
		if (percent >= 1.0f) {
			stopAnimation();
			if (animationDir == INCOMING) finish();
			else {
				glass.removeAll();
				glass.setVisible(false);
			}
		}
	}
	
}
