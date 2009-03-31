/*
 * org.openmicroscopy.shoola.agents.dataBrowser.view.SlideShowUI 
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
package org.openmicroscopy.shoola.agents.dataBrowser.view;


//Java imports
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * The component hosting the canvas or component indicating of the loading
 * state.
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
class SlideShowUI 
	extends JScrollPane
    implements MouseMotionListener
{

	/** The default point location. */
	private static final Point	ORIGIN = new Point(-1, -1);
	
	/** The canvas where the current image is painted. */
	private SlideShowCanvas	canvas;
	
	/** Flag indicating if the experimenter uses the scrollbars. */
    private boolean			adjusting;
    
	 /** The bar notifying the user for the data retrieval progress. */
    private JProgressBar	progressBar;
    
    /** The UI component displaying the status. */
    private JPanel			statusPane;
   
    /** 
     * The component currently displayed, either {@link #statusPane}
     * or {@link #statusPane}
     */
    private JComponent		currentComp;
    
    /** Reference to the parent.*/
    private SlideShowView	model;
    
	/** Initializes the components composing the display. */
    private void initComponents()
    {
        canvas = new SlideShowCanvas();
        canvas.addPropertyChangeListener(model);
        getVerticalScrollBar().addMouseMotionListener(this);
        getHorizontalScrollBar().addMouseMotionListener(this);
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setIndeterminate(false);
        progressBar.setFont(progressBar.getFont().deriveFont(Font.BOLD));
        statusPane = new JPanel();
        statusPane.setOpaque(false);
        statusPane.setLayout(new BoxLayout(statusPane, BoxLayout.Y_AXIS));
        statusPane.add(progressBar);
        statusPane.add(UIUtilities.setTextFont("Loading...", Font.BOLD, 14));
        currentComp = statusPane;
    }
    
    /** Builds and lays out the GUI. */
    private void buildGUI()
    {
    	getViewport().setViewPosition(ORIGIN);
    	getViewport().setLayout(null);
        getViewport().add(statusPane);
        getViewport().setBackground(UIUtilities.BACKGROUND);
    }

	/**
	 * Returns <code>true</code> if the scrollbars are visible,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	private boolean scrollbarsVisible()
	{
		JScrollBar hBar = getHorizontalScrollBar();
		JScrollBar vBar = getVerticalScrollBar();
		if (hBar.isVisible()) return true;
		if (vBar.isVisible()) return true;
		return false;
	}
	
    /** 
     * Creates a new instance. 
     * 
     * @param model Reference to the model. Mustn't be <code>null</code>.
     */
    SlideShowUI(SlideShowView model)
    {
    	if (model == null)
    		throw new IllegalArgumentException("No model.");
    	this.model = model;
    	initComponents();
    	buildGUI();
    }
    
    /**
     * Sets the value of the progress bar.
     * 
     * @param hide  Pass <code>true</code> to remove the progress bar, 
     * 				and replace it by the canvas.
     * @param value  The value to set.
     */
    void setProgress(boolean hide, int value)
	{
		if (hide) {
			getViewport().removeAll();
			getViewport().add(canvas);
			currentComp = canvas;
			getViewport().revalidate();
		} else {
            progressBar.setValue(value);
		}
	}

    /**
     * Paints the passed image.
     * 
     * @param image The image to paint.
     */
    void paintImage(BufferedImage image)
    {
    	canvas.paintImage(image);
    	getViewport().setViewPosition(ORIGIN);
    	canvas.repaint();
    	setBounds(getBounds());
    }
    
	/**
	 * Sets the <code>adjusting</code> flag when the experimenter uses 
	 * the scrollbars..
	 * @see MouseMotionListener#mouseDragged(MouseEvent)
	 */
	public void mouseDragged(MouseEvent e) { adjusting = true; }
	
	/**
	 * Overridden to center the image.
	 * @see JComponent#setBounds(Rectangle)
	 */
	public void setBounds(Rectangle r)
	{
		setBounds(r.x, r.y, r.width, r.height);
	}
	
	/**
	 * Overridden to center the image.
	 * @see JComponent#setBounds(int, int, int, int)
	 */
	public void setBounds(int x, int y, int width, int height)
	{
		super.setBounds(x, y, width, height);
		if (!scrollbarsVisible() && adjusting) adjusting = false;
		if (adjusting) return;
		Rectangle r = getViewport().getViewRect();
		Dimension d = currentComp.getPreferredSize();
		int xLoc = ((r.width-d.width)/2);
		int yLoc = ((r.height-d.height)/2);
		currentComp.setBounds(xLoc, yLoc, d.width, d.height);
	}
	
	/**
	 * Required by the {@link MouseMotionListener} I/F but no-op implementation
	 * in our case.
	 * @see MouseMotionListener#mouseMoved(MouseEvent)
	 */
	public void mouseMoved(MouseEvent e) {}
	
}
