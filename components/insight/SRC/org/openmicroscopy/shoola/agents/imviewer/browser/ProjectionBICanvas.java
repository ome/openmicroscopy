/*
 * org.openmicroscopy.shoola.agents.imviewer.browser.ProjectionBICanvas 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.imviewer.browser;



//Java imports
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.util.ImagePaintingFactory;

/** 
 * Displays the projected image.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * </small>
 * @since 3.0-Beta4
 */
class ProjectionBICanvas 
	extends ImageCanvas
{

	/** The background color. */
	static final Color BACKGROUND_COLOR = Color.BLACK;
	
	/** The Text color. */
	static final Color TEXT_COLOR = Color.WHITE;
	
	/** The default text. */
	static final String DEFAULT_TEXT = "Click here to create\n" +
			" a projection preview.";
	
	/** The text indicating that the preview is on-going. */
	static final String CREATION_TEXT = "Creating preview";
	
    /** The mouse listener. */
    private MouseAdapter	listener;
    
    /** Reference to the UI hosting this canvas. */
    private ProjectionUI	ui;
    
    /** The text displayed when no projection preview. */
    private String          text;
    
    /** Attaches the listener. */
    private void attachListener()
    {
    	if (listener != null) return;
    	listener = new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				model.projectionPreview();
				text = CREATION_TEXT;
		    	removeMouseListener(listener);
		    	repaint();
			}
		};
		addMouseListener(listener);
    }
    
    /**
     * Creates a new instance.
     *
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     * @param view  Reference to the View. Mustn't be <code>null</code>.
     * @param ui  Reference to the View. Mustn't be <code>null</code>.
     */
	ProjectionBICanvas(BrowserModel model, BrowserUI view, ProjectionUI ui)
	{
		super(model, view);
		if (ui == null)
			throw new IllegalArgumentException("No UI specified.");
		this.ui = ui;
		text = DEFAULT_TEXT;
	}
	
	/**
     * Overridden to paint the image.
     * @see javax.swing.JComponent#paintComponent(Graphics)
     */
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        BufferedImage img = model.getDisplayedProjectedImage();
        Graphics2D g2D = (Graphics2D) g;
        ImagePaintingFactory.setGraphicRenderingSettings(g2D);
        if (img == null) {
        	img = model.getDisplayedImage();
        	if (img != null) {
        		attachListener();
        		double f = model.getZoomFactor();
        		int w = img.getWidth()-1;
        		int h = img.getHeight()-1;
        		w *= f;
        		h *= f;
        		g2D.setColor(BACKGROUND_COLOR);
        		g2D.fillRect(0, 0, w, h);
        		FontMetrics fm = g2D.getFontMetrics();
        		g2D.setColor(TEXT_COLOR);
        		int width = fm.stringWidth(text);
        		if (width+(w-width*f)/2 > w) { //need to split the text
        			int l = text.length();
        			String s1 = text.substring(0, l/2);
        			String s2 = text.substring(l/2+1, l);
        			width = (int) (fm.stringWidth(s1));
        			int x = (w-width)/2;
        			g2D.drawString(s1, x, h/2);
        			width = (int) (fm.stringWidth(s2));
        			x = (w-width)/2;
        			g2D.drawString(s2, x, (h+fm.getHeight()+10)/2);
        		} else {
        			g2D.drawString(text, (w-width)/2, h/2);
        		}
        		
        	}
        	return;
        }
        g2D.drawImage(img, 0, 0, null);
        paintScaleBar(g2D, img.getWidth(), img.getHeight(), ui.getViewport());
        g2D.dispose();
    }

}