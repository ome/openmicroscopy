/*****************************************************************************
 * Copyright (C) 2003 Jean-Daniel Fekete and INRIA, France                   *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the QPL Software License    *
 * a copy of which has been included with this distribution in the           *
 * license-infovis.txt file.                                                
 *  
 * Borrowed and revised by hsh, Feb 2004-July 2004. Turn ints into longs
 *****************************************************************************/
package org.openmicroscopy.shoola.agents.executions.ui;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.JComponent;

import org.openmicroscopy.shoola.agents.executions.ui.model.BoundedLongRangeModel;

/**
 * Implements a Swing-based Range slider, which allows the user to enter a
 * range-based value.
 * 
 * @author Ben B. Bederson, Jon Meyer and Jean-Daniel Fekete
 * @version $Revision$
 */
public class LongRangeSlider extends JComponent implements MouseListener,
	MouseMotionListener, ChangeListener {
	private static int PICK_WIDTH = 6;
	private static int pickWidth = PICK_WIDTH;
	static int SZ = 6; // Size of the cutout corner on the range bar.
	// Event handling
	static final int PICK_NONE = 0;
	// Event handling
	static final int PICK_MIN = 1;
	// Event handling
	static final int PICK_MAX = 2;
	// Event handling
	static final int PICK_MID = 4;
	private BoundedLongRangeModel model;
	private boolean enabled = false;
	// PAINT METHOD
	int[] xPts = new int[7];
	// PAINT METHOD
	int[] yPts = new int[7];
	int pick;
	int pickOffset;
	int mouseX;
	// PUBLIC API
	private static long time;
	private static int count;
	private long start;
	
	private static long paintTime;
	private static long dragTime=0;
	/**
	 * Constructs a new range slider.
	 * 
	 * @param minimum -
	 *            the minimum value of the range.
	 * @param maximum -
	 *            the maximum value of the range.
	 * @param lowValue -
	 *            the current low value shown by the range slider's bar.
	 * @param highValue -
	 *            the current high value shown by the range slider's bar.
	 */
	public LongRangeSlider(long minimum, long maximum, long lowValue, long highValue) {
		this(new BoundedLongRangeModel(lowValue, highValue - lowValue,
				minimum, maximum));
	}
	/**
	 * Creates a new LongRangeSlider object.
	 * 
	 * @param model
	 *            the BoundedLongRangeModel.
	 */
	public LongRangeSlider(BoundedLongRangeModel model) {
		this.model = model;
		model.addChangeListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);
	}
	/**
	 * Returns the current "low" value shown by the range slider's bar. The low
	 * value meets the constraint minimum &lt;= lowValue &lt;= highValue &lt;=
	 * maximum.
	 * 
	 * @return the current "low" value shown by the range slider's bar.
	 */
	public long getLowValue() {
		return model.getValue();
	}
	/**
	 * Returns the current "high" value shown by the range slider's bar. The
	 * high value meets the constraint minimum &lt;= lowValue &lt;= highValue
	 * &lt;= maximum.
	 * 
	 * @return the current "high" value shown by the range slider's bar.
	 */
	public long getHighValue() {
		return model.getValue() + model.getExtent();
	}
	/**
	 * Returns the minimum possible value for either the low value or the high
	 * value.
	 * 
	 * @return the minimum possible value for either the low value or the high
	 *         value.
	 */
	public long getMinimum() {
		return model.getMinimum();
	}
	/**
	 * Returns the maximum possible value for either the low value or the high
	 * value.
	 * 
	 * @return the maximum possible value for either the low value or the high
	 *         value.
	 */
	public long getMaximum() {
		return model.getMaximum();
	}
	/**
	 * Returns true if the specified value is within the range indicated by
	 * this range slider. i.e. lowValue 1 &lt;= v &lt;= highValue.
	 * 
	 * @param v
	 *            value
	 * 
	 * @return true if the specified value is within the range indicated by
	 *         this range slider.
	 */
	public boolean contains(long v) {
		return (v >= getLowValue() && v <= getHighValue());
	}
	/**
	 * Sets the low value shown by this range slider. This causes the range
	 * slider to be repainted and a RangeEvent to be fired.
	 * 
	 * @param lowValue
	 *            the low value shown by this range slider
	 */
	public void setLowValue(long lowValue) {
		long high;
		if ((lowValue + model.getExtent()) > getMaximum()) {
			high = getMaximum();
		} else {
			high = getHighValue();
		}
		long extent = high - lowValue;
		model.setRangeProperties(lowValue, extent, getMinimum(), getMaximum(),
				true);
	}
	/**
	 * Sets the high value shown by this range slider. This causes the range
	 * slider to be repainted and a RangeEvent to be fired.
	 * 
	 * @param highValue
	 *            the high value shown by this range slider
	 */
	public void setHighValue(long highValue) {
		model.setExtent(highValue - getLowValue());
	}
	/**
	 * Sets the minimum value of the sizeModel.
	 * 
	 * @param min
	 *            the minimum value.
	 */
	public void setMinimum(long min) {
		model.setMinimum(min);
	}
	/**
	 * Sets the maximum value of the sizeModel.
	 * 
	 * @param max
	 *            the maximum value.
	 */
	public void setMaximum(long max) {
		model.setMaximum(max);
	}
	Rectangle getInBounds() {
		Dimension sz = getSize();
		Insets insets = getInsets();
		return new Rectangle(insets.left, insets.top, sz.width - insets.left
				- insets.right, sz.height - insets.top - insets.bottom);
	}
	
	
	Dimension getScreenBounds() {
		Dimension sz = getSize();
		Insets insets = getInsets();
		
		return new Dimension((int)sz.getWidth()-insets.left-insets.right,
				(int)sz.getHeight());
	}
	/**
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		long paintStart  = System.currentTimeMillis();
		g.setColor(Color.lightGray);
		Rectangle rect = getInBounds();
		g.fill3DRect(rect.x, rect.y, rect.width, rect.height, false);
		int minX = toScreenX(getLowValue());
		int maxX = toScreenX(getHighValue());
		if ((maxX - minX) > 10) {
			xPts[0] = minX;
			yPts[0] = rect.y + SZ;
			xPts[1] = minX + SZ;
			yPts[1] = rect.y;
			xPts[2] = maxX;
			yPts[2] = rect.y;
			xPts[3] = maxX;
			yPts[3] = rect.y + rect.height - SZ;
			xPts[4] = maxX - SZ;
			yPts[4] = rect.y + rect.height - 3;
			xPts[5] = minX;
			yPts[5] = rect.y + rect.height - 3;
			xPts[6] = minX;
			yPts[6] = rect.y + SZ;
			g.setColor(Color.darkGray);
			g.drawPolygon(xPts, yPts, 7);
			if (enabled == true) {
				g.setColor(Color.lightGray);
			}
			g.fillPolygon(xPts, yPts, 7);
			g.setColor(Color.white);
			g.drawLine(xPts[0], yPts[0], xPts[1], yPts[1]);
			g.drawLine(xPts[1], yPts[1], xPts[2], yPts[2]);
			g.drawLine(xPts[5], yPts[5], xPts[6], yPts[6]);
			if ((maxX - minX) > 12) {
				// Draw the little dot pattern
			for (int y = rect.y + 3; y < (rect.y + rect.height - 3); y += 3) {
					g.setColor(Color.lightGray);
					g.fillRect(minX + 2, y + 2, 1, 1);
					g.fillRect(minX + 5, y, 1, 1);
					g.fillRect(minX + 8, y - 2, 1, 1);
					g.fillRect(maxX - 3, y, 1, 1);
					g.fillRect(maxX - 6, y + 2, 1, 1);
					g.fillRect(maxX - 9, y + 4, 1, 1);
					g.setColor(Color.darkGray);
					g.fillRect(minX + 2, y + 3, 1, 1);
					g.fillRect(minX + 5, y + 1, 1, 1);
					g.fillRect(minX + 8, y - 1, 1, 1);
					g.fillRect(maxX - 3, y + 1, 1, 1);
					g.fillRect(maxX - 6, y + 3, 1, 1);
					g.fillRect(maxX - 9, y + 5, 1, 1);
				}
				g.setColor(Color.gray);
				g.drawLine(minX + 10, rect.y + 2, minX + 10, rect.y
						+ rect.height - 2);
				g.drawLine(maxX - 11, rect.y + 2, maxX - 11, rect.y
						+ rect.height - 2);
				pickWidth = PICK_WIDTH;
			} else {
				// Too small to draw the dot pattern - just draw a line down
				// the center
				g.setColor(Color.gray);
				g.drawLine((minX + maxX) / 3, rect.y + 2, (minX + maxX) / 3,
						rect.y + rect.height - 2);
				g.drawLine((2 * (minX + maxX)) / 3, rect.y + 2,
						(2 * (minX + maxX)) / 3, rect.y + rect.height - 2);
				pickWidth = (int) ((maxX - minX) / 3);
			}
		} else {
			// For very small ranges we just draw a tiny 3D rect
			if (enabled == true) {
				g.setColor(Color.lightGray);
			} else {
				g.setColor(Color.darkGray);
			}
			int w = maxX - minX;
			if (w < 10) {
				w = 10;
			}
			g.fill3DRect(minX, rect.y, w, rect.y + rect.height, true);
			g.setColor(Color.gray);
			g.drawLine((minX + maxX) / 3, rect.y + 2, (minX + maxX) / 3, rect.y
					+ rect.height - 2);
			g.drawLine((2 * (minX + maxX)) / 3, rect.y + 2,
					(2 * (minX + maxX)) / 3, rect.y + rect.height - 2);
			pickWidth = (int) ((maxX - minX) / 3);
		}
		setToolTipText("LongRangeSlider");
		long end = System.currentTimeMillis();
		paintTime+=end-paintStart;
	}
	// Converts from screen coordinates to a range value.
	private long toLocalX(int x) {
		Dimension sz = getScreenBounds();
		double xScale = (sz.width - 3) / (double) (getMaximum() - getMinimum());
		return (long) ((x / xScale) + getMinimum());
	}
	// Converts from a range value to screen coordinates.
	private int toScreenX(long x) {
		Dimension sz = getScreenBounds();
		double xScale = (sz.width - 3) / (double) (getMaximum() - getMinimum());
		return (int) ((x - getMinimum()) * xScale + getInsets().left);
	}
	private int pickHandle(int x) {
		int minX = toScreenX(getLowValue());
		int maxX = toScreenX(getHighValue());
		int pick = 0;
		if (Math.abs(x - minX) < PICK_WIDTH) {
			pick |= PICK_MIN;
			//System.out.println("MIN");
		}
		if (Math.abs(x - maxX) < PICK_WIDTH) {
			pick |= PICK_MAX;
			//System.out.println("MAX");
		}
		if ((pick == 0) && (x > minX) && (x < maxX)) {
			pick = PICK_MID;
			//System.out.println("MID");
		}
		return pick;
	}
	private void offset(long dx) {
		model.setValue(getLowValue() + dx);
	}
	/**
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	public void mousePressed(MouseEvent e) {
		if (enabled == false) {
			return;
		}
	
		pick = pickHandle(e.getX());
		pickOffset = e.getX() - toScreenX(getLowValue());
		mouseX = e.getX();
		model.setValueIsAdjusting(true);
	}
	/**
	 * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
	 */
	public void mouseDragged(MouseEvent e) {
		if (enabled == false) {
			return;
		}
		start = System.currentTimeMillis();
		long dragStart = System.currentTimeMillis();
	
		long x = toLocalX(e.getX());
		if (x < getMinimum()) {
			x = getMinimum();
		}
		if (x > getMaximum()) {
			x = getMaximum();
		}
		if (pick == (PICK_MIN | PICK_MAX)) {
			if ((e.getX() - mouseX) > 2) {
				pick = PICK_MAX;
			} else if ((e.getX() - mouseX) < -2) {
				pick = PICK_MIN;
			} else {
				return;
			}
		}
		switch (pick) {
			case PICK_MIN :
				if (x < getHighValue()) {
					setLowValue(x);
				}
				break;
			case PICK_MAX :
				if (x > getLowValue()) {
					setHighValue(x);
				}
				break;
			case PICK_MID :
				long dx = toLocalX(e.getX() - pickOffset) - getLowValue();
				if ((dx < 0) && ((getLowValue() + dx) < getMinimum())) {
					dx = getMinimum() - getLowValue();
				}
				if ((dx > 0) && ((getHighValue() + dx) > getMaximum())) {
					dx = getMaximum() - getHighValue();
				}
				if (dx != 0) {
					offset(dx);
				}
				break;
		}
		long dragEnd = System.currentTimeMillis();
		dragTime += dragEnd-dragStart;
		count++;
		time += dragEnd - start;
	}
	/**
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	public void mouseReleased(MouseEvent e) {
		model.setValueIsAdjusting(false);
		
	}
	private void setCurs(int c) {
		Cursor cursor = Cursor.getPredefinedCursor(c);
		if (getCursor() != cursor) {
			setCursor(cursor);
		}
	}
	/**
	 * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
	 */
	public void mouseMoved(MouseEvent e) {
		if (enabled == false) {
			return;
		}
		switch (pickHandle(e.getX())) {
			case PICK_MIN :
			case PICK_MIN | PICK_MAX :
				setCurs(Cursor.W_RESIZE_CURSOR);
			break;
			case PICK_MAX :
				setCurs(Cursor.E_RESIZE_CURSOR);
				break;
			case PICK_MID :
				setCurs(Cursor.MOVE_CURSOR);
				break;
			case PICK_NONE :
				setCurs(Cursor.DEFAULT_CURSOR);
				break;
		}
	}
	/**
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {
			model.setValue(model.getMinimum());
			model.setExtent(model.getMaximum() - model.getMinimum());
			repaint();
		}
	}
	/**
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	public void mouseEntered(MouseEvent e) {
	}
	/**
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	public void mouseExited(MouseEvent e) {
	}
	/**
	 * @see javax.swing.JComponent#getPreferredSize()
	 */
	public Dimension getPreferredSize() {
		return new Dimension(0, 20);
	} 
	
	public Dimension getMinimumSize() {
		return new Dimension(0, 20);
	} 
	
	public Dimension getMaximumSize() {
		return new Dimension(1000, 20);
	} 
	/**
	 * @see javax.swing.JComponent#setEnabled(boolean)
	 */
	public void setEnabled(boolean v) {
		enabled = v;
		repaint();
	}
	/**
	 * @see javax.swing.event.ChangeListener#stateChanged(ChangeEvent)
	 */
	public void stateChanged(ChangeEvent e) {
		repaint();
	}
	/**
	 * Returns the sizeModel.
	 * 
	 * @return BoundedLongRangeModel
	 */
	public BoundedLongRangeModel getModel() {
		return model;
	}
	/**
	 * Sets the sizeModel.
	 * 
	 * @param model
	 *            The BoundedLongRangeModel to set
	 */
	public void setModel(BoundedLongRangeModel model) {
		this.model = model;
		repaint();
	}
	/**
	 * @see javax.swing.JComponent#getToolTipText(MouseEvent)
	 */
	public String getToolTipText(MouseEvent event) {
		return "[" + getMinimum() + " [" + getLowValue() + ":" + getHighValue()
		+ "] " + getMaximum() + "]";
	}
	public static void dumpTimes() {
		double ave = (double) time / (double) count;
		System.err.println("in range slider total time " + time + ", count "
				+ count + "ave " + ave);
		System.err.println("paint time "+paintTime);
		System.err.println("drag time "+dragTime);
	}
}
