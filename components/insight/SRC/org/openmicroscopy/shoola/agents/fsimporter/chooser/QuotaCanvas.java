/*
 * org.openmicroscopy.shoola.agents.fsimporter.chooser.QuotaCanvas 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2011 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
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
package org.openmicroscopy.shoola.agents.fsimporter.chooser;


//Java imports
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.model.DiskQuota;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Displays the space free and used.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
class QuotaCanvas
	extends JPanel
{

	/** Text indicating the size of the import. */
	static final String IMPORT_SIZE_TEXT = "Import size: ";
	
	/** The threshold before changing the color.*/
	private static final double		THRESHOLD = 0.8;
	
	/** The dimension of the canvas.*/
	private static final Dimension	DIMENSION = new Dimension(80, 12);
	
	/** The color indicating the used space.*/
	private static final Color		USED_COLOR_DEFAULT = Color.BLUE;
	
	/** The color indicating the used space when greater than the threshold.*/
	private static final Color		USED_COLOR_WARNING = Color.RED;
	
	/** The color indicating the space taken by import.*/
	private static final Color		COLOR_IMPORT = Color.GREEN;
	
	/** The percentage of file to import.*/
	private double percentageToImport;
	
	/** The disk quota.*/
	private DiskQuota quota;
	
	/**
	 * Formats the tool tip.
	 * 
	 * @param size The size of files in queue.
	 */
	private void formatToolTip(long size)
	{
		if (quota == null) return;
		long free = quota.getAvailableSpace();
		//long used = quota.getUsedSpace();
		List<String> tips = new ArrayList<String>();
		//tips.add((double) UIUtilities.round(percentage*100, 3)+"% Used");
		//tips.add("Used Space: "+UIUtilities.formatFileSize(used));
		tips.add("Free Space: "+UIUtilities.formatFileSize(free));
		setToolTipText(UIUtilities.formatToolTipText(tips));
	}
	
	/** Creates a new instance.*/
	QuotaCanvas()
	{
		percentageToImport = 0;
		setPreferredSize(DIMENSION);
		setSize(DIMENSION);
		setBorder(BorderFactory.createLineBorder(Color.BLACK));
		setBackground(UIUtilities.BACKGROUND);
	}
	
	/**
	 * Sets the quota.
	 * 
	 * @param quota The value to set.
	 */
	void setPercentage(DiskQuota quota)
	{
		this.quota = quota;
		if (quota == null) return;
		formatToolTip(0);
		repaint();
	}
	
	/**
	 * Sets the size of the file to add to the queue.
	 * 
	 * @param size The size of the file.
	 */
	void setSizeInQueue(long size)
	{
		formatToolTip(size);
		if (quota == null) return;
		long free = quota.getAvailableSpace();
		if (free != 0) percentageToImport = (double) size/free;
		repaint();
	}
	
	/**
	 * Returns the percentage representing by the import.
	 * 
	 * @return See above.
	 */
	double getPercentageToImport() { return percentageToImport; }
	
    /**
     * Overridden to disk usage.
     * @see javax.swing.JComponent#paintComponent(Graphics)
     */
    public void paintComponent(Graphics g)
    {
    	super.paintComponent(g);
    	Color c = USED_COLOR_DEFAULT;
    	Graphics2D g2D = (Graphics2D) g;
    	//setBackground(UIUtilities.BACKGROUND);
    	Dimension d = getPreferredSize();
    	g2D.setColor(UIUtilities.BACKGROUND);
    	g2D.fillRect(0, 0, d.width, d.height);
    	int w = 0;
    	GradientPaint paint = new GradientPaint(0, 0, c, w, 0, c);
		g2D.setPaint(paint);
    	g2D.fillRect(0, 0, w, d.height);
    	int x = w;
    	if (percentageToImport > 0) {
    		double value = percentageToImport;
    		double v = percentageToImport;
    		if (v < THRESHOLD) c = COLOR_IMPORT;
    		else c = USED_COLOR_WARNING;
    		if (v > 1) value = 1;
    		w = (int) (d.width*value);
    		paint = new GradientPaint(x, 0, c, x+w, 0, c);
    		g2D.setPaint(paint);
    		g2D.fillRect(x, 0, w, d.height);
    	} else { //reset
    		c = UIUtilities.BACKGROUND;
    		paint = new GradientPaint(x, 0, c, x+w, 0, c);
    		g2D.setPaint(paint);
    		g2D.fillRect(x, 0, d.width-w, d.height);
    	}
    }

}
