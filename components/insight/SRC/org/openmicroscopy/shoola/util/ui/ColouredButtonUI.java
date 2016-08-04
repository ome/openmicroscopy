/*
 * org.openmicroscopy.shoola.util.ui.ColouredButtonUI
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

package org.openmicroscopy.shoola.util.ui;




//Java imports
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.plaf.basic.BasicButtonUI;

//Third-party libraries

//Application-internal dependencies
import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.painter.CompoundPainter;
import org.jdesktop.swingx.painter.MattePainter;
import org.jdesktop.swingx.painter.Painter;
import org.openmicroscopy.shoola.util.ui.colour.HSV;

/** 
 * Basic UI for coloured button.
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
class ColouredButtonUI
	extends BasicButtonUI
{

	/** Identifies the matte painter. */
	private static final int MATTE = 0;
	
	/** Identifies the specular painter. */
	private static final int SPEC = 1;
		
	/** The stroke of the graphics context. */
	private static final Stroke	STROKE = new BasicStroke(1.0f);
	
	/** The default value for width or height. */
	private static final int DEFAULT_SIZE = 32;
	
	/** The default insets for the painter. */
	private static final Insets INSETS = new Insets(3, 3, 3, 3);
	
    /** Current Colour of the button. */
    private Color           colour;
    
    /** The background image */
    private BufferedImage image;

    /** Reference to parent button. */
    private final ColouredButton  button;
    
    /** The button's size, used by paint to draw onto component. */
    private Rectangle       buttonRect;
    
    /** 
     * HSV colour user to determine the start(top) of the gradient in the
     * button.
     */
    private HSV             gradientStartHSV;
    
    /** The Color conversion from HSV used in the paintGradient command.*/
    private Color           gradientStartRGB;
    
    /** 
     * HSV colour user to determine the End(bottom) of the gradient in the
     * button.
     */
    private HSV             gradientEndHSV;
    
    /** The Color conversion from HSV used in the paintGradient command.*/
    private Color           gradientEndRGB;
    
    /** 
     * If true the buttons will have a grey mask painted on top of their
     * button face. 
     */
    private boolean         greyedOut;
    
    /** The index of the derived font. */
    private int             fontIndex;
    
    /** Show the button borders? */
    private boolean 		paintBorderInsets;
    
    /** The button face painter. */
    private Painter 		buttonFacePainter;

    /** The button face painter. */
    private Painter 		selectedButtonFacePainter;

    /** The grey-mask face painter. */
    private Painter 		greyMaskPainter;
    
    /** The grey-mask face painter. */
    private Painter 		selectedGreyMaskPainter;
    
    /**
     * This method calculates the start and end colours for the gradient on
     * the face of the button. 
     */
    private void setGradientColours()
    {
        final HSV col = new HSV(colour);
         // top gradient value from HSV model. 
        float topGradientValue = col.getValue();
        // bottom gradient value from HSV model. 
        float bottomGradientValue = col.getValue();
        // top and bottom gradient saturation from HSV model. 
        float topGradientSaturation, bottomGradientSaturation;
        
        // if colour greyscale(achromatic) don't touch saturation
        if (col.getSaturation() == 0) {
            topGradientSaturation = bottomGradientSaturation = 
                col.getSaturation();
            // A check to see what gives greatest increase, +0.3 or *1.3 
            // and set topGradientValue to that.
            topGradientValue = col.getValue()+0.3f;
            if (col.getValue()*1.3f > topGradientValue)
                topGradientValue = col.getValue()*1.3f;
            if (topGradientValue>1) topGradientValue = 1;
            // Set bottomGradientValue to 75% of colour value.
            bottomGradientValue = col.getValue()*0.75f;      
        } else {
            // We're in a colour space. 
            // Increase topGradientValue to 1.5 * value of colour face.
            topGradientValue = col.getValue()*1.5f;
            if (topGradientValue>1) topGradientValue = 1;
            topGradientSaturation = col.getSaturation()*0.6f;
            bottomGradientSaturation = col.getSaturation();
        }
        
        gradientStartHSV = new HSV(col.getHue(), topGradientSaturation,
                            topGradientValue, 1.0f);
        gradientStartRGB = gradientStartHSV.toColorA();

        gradientEndHSV = new HSV(col.getHue(),
                    bottomGradientSaturation, bottomGradientValue, 1.0f);
        gradientEndRGB = gradientEndHSV.toColorA();
    }
    
    /**
     * This method calculates the size of the button's text. It then
     * renders the text in the centre of the button, it also changes the
     * colour of the text to maximize the contrast between it and the 
     * background. 
     * 
     * @param g Graphics2D drawing context.
     */
    private void drawText(final Graphics2D g)
    {
        final HSV col = new HSV(colour);
        //Font fnt = button.getFont();
        //fnt = fnt.deriveFont(fontIndex, 10);
        //g.setFont(fnt);
        final FontMetrics fm = g.getFontMetrics();
        
        // Using the font metrics, centre the text in the button face.
        final int x = (int) ((buttonRect.width/2.0f)-
        		fm.stringWidth(button.getText())/2.0f);
        final int y = (int) ((buttonRect.height/2.0f) + 
                (fm.getHeight()-fm.getDescent())/2.0f);
        
        // If the button face is dark or does not contrast well with the 
        // black text turn the text white.
        if (col.getValue() < 0.6 || (col.getHue() > 0.6 && 
           col.getSaturation() > 0.7) || greyedOut)
            g.setPaint(Color.white);
        else
            g.setPaint(Color.black);
        g.drawString(button.getText(), x, y);
    }
    
    /**
     * Draws a beveled border for an unselected Button, the top and left 
     * bevels will be lighter than the bottom and Right.
     * 
     * @param g Graphics2D render context.
     */
    private void drawBorder(Graphics2D g)
    {
        Color borderColour;
        borderColour = gradientStartRGB.brighter();
     
        // Set the colour of the top, left bevels to be a lighter colour
        // than the gradient at that same corner.
        g.setPaint(borderColour);
        
        // Draw the bevel, it is drawn as four line from: topleft to 
        // topright, and topleft to bottom left. 
        g.drawLine(0, 0, 0, (int) buttonRect.getHeight());
        g.drawLine(0, 0, (int) buttonRect.getWidth(), 0);
        g.drawLine(1, 1, 1, (int) buttonRect.getHeight()-1);
        g.drawLine(1, 1, (int) buttonRect.getWidth()-1, 1);
        
        borderColour = gradientEndRGB.darker();
        // Set the colour of the top, left bevels to be a lighter colour
        // than the gradient at that same corner.
        g.setPaint(borderColour);
        
        // Draw the bevel, it is drawn as four line from: bottomleft to 
        // bottom right, and bottomright to top left. 
        g.drawLine((int) buttonRect.getWidth()-1, 0, 
            (int) buttonRect.getWidth()-1, (int) buttonRect.getHeight()-1);
        g.drawLine(0, (int) buttonRect.getHeight()-1,
            (int) buttonRect.getWidth()-1, (int) buttonRect.getHeight()-1);
        g.drawLine((int) buttonRect.getWidth()-2, 2,
            (int) buttonRect.getWidth()-2, (int) buttonRect.getHeight()-2);
            g.drawLine(1, (int) buttonRect.getHeight()-2,
            (int) buttonRect.getWidth()-1, (int) buttonRect.getHeight()-2);
    }
    
    /**
     * Draws a bevelled border for a Grey unselected Button, the top and left 
     * bevels will be lighter than the bottom and Right. The Bevels will
     * be the greyscale equivalent of the face colour.
     * 
     * @param g Graphics2D render context.
     */
    private void drawGreyBorder(Graphics2D g)
    {
        Color borderColour;
        HSV borderColourHSV = new HSV(0, 0, gradientStartHSV.getValue(), 0.8f);
        borderColour = borderColourHSV.toColorA();
       
        // Set the colour of the top, left bevels to be a lighter colour
        // than the grey mask of the gradient at that same corner.
        g.setPaint(borderColour);
        g.setStroke(STROKE);
       
        // Draw the bevel, it is drawn as four line from: topleft to 
        // topright, and topleft to bottom left. 
        final int height = (int) buttonRect.getHeight();
        final int width = (int) buttonRect.getWidth();
        g.drawLine(0, 0, 0, height);
        g.drawLine(0, 0, width, 0);
        g.drawLine(1, 1, 1, height-1);
        g.drawLine(1, 1, width-1, 1);
        borderColourHSV = new HSV(0, 0, gradientEndHSV.getValue(), 0.8f);
        borderColour = borderColourHSV.toColorA();
        
        // Set the colour of the bottom, right bevels to be a darker colour
        // than the grey mask of the gradient at that same corner.
        g.setPaint(borderColour.darker().darker().darker());
       
        // Draw the bevel, it is drawn as four line from: bottomleft to 
        // bottom right, and bottomright to top left. 
        g.drawLine(width-1, 0, width-1, height-1);
        g.drawLine(0, height-1, width-1, height-1);
        g.drawLine(width-2, 2, width-2, height-2);
        g.drawLine(1, height-2, width-1, height-2);
    }
    
    /**
     * Draws a bevelled border for a Grey selected Button, the top and left 
     * bevels will be darker than the bottom and Right. The border will be
     * drawn using greyscale equivalent of the face colour. 
     * 
     * @param g Graphics2D render context.
     */
    private void drawGreySelectedBorder(Graphics2D g)
    {
        Color borderColour;
        HSV borderColourHSV = new HSV(0, 0, gradientEndHSV.getValue(), 0.8f);
        borderColour = borderColourHSV.toColorA();
      
        // Set the colour of the top, left bevels to be a lighter colour
        // than the grey mask of the gradient at that same corner.
        g.setPaint(borderColour.darker().darker().darker());
        g.setStroke(STROKE);
        
        // Draw the bevel, it is drawn as four line from: topleft to 
        // topright, and topleft to bottom left. 
        int height = (int) buttonRect.getHeight();
        int width = (int) buttonRect.getWidth();
        g.drawLine(0, 0, 0, height);
        g.drawLine(0, 0, width, 0);
        g.drawLine(1, 1, 1, height-1);
        g.drawLine(1, 1, width-1, 1);
        borderColourHSV = new HSV(0, 0, gradientStartHSV.getValue(), 0.8f);
        borderColour = borderColourHSV.toColorA();
        
        // Set the colour of the bottom, right bevels to be a darker colour
        // than the grey mask of the gradient at that same corner.
        g.setPaint(borderColour.darker());
        
       // Draw the bevel, it is drawn as four line from: bottomleft to 
       // bottom right, and bottomright to top left. 
        g.drawLine(width-1, 0, width-1, height-1);
        g.drawLine(0, height-1, width-1, height-1);
        g.drawLine(width-2, 2, width-2, height-2);
        g.drawLine(1, height-2, width-1, height-2);
    }
    
    /**
     * Draws a bevelled border for a selected Button, the top and left 
     * bevels will be darker than the bottom and Right.
     * 
     * @param g Graphics2D render context.
     */
    private void drawSelectedBorder(Graphics2D g)
    {
        Color borderColour;
        
        borderColour = gradientEndRGB.darker();
        HSV col = new HSV(borderColour);
        g.setPaint(col.toColorA());
        g.setStroke(STROKE);
        
        // Draw the bevel, it is drawn as four line from: topleft to 
        // topright, and topleft to bottom left.
        int height = (int) buttonRect.getHeight();
        int width = (int) buttonRect.getWidth();
        g.drawLine(0, 0, 0, height);
        g.drawLine(0, 0, width, 0);
        g.drawLine(1, 1, 1, height-1);
        g.drawLine(1, 1, width-1, 1);

        borderColour = gradientStartRGB;
        col = new HSV(borderColour);
        col.setSaturation(col.getSaturation()*0.8f);
        borderColour = col.toColorA();

        // Set the colour of the bottom, right bevels to be a darker colour
        // than the grey mask of the gradient at that same corner.
        g.setPaint(borderColour);
        
        // Draw the bevel, it is drawn as four line from: bottomleft to 
            // bottom right, and bottomright to top left. 
        g.drawLine(width-1, 0, width-1, (int) buttonRect.getHeight()-1);
        g.drawLine(0, height-1, width-1, height-1);
        g.drawLine(width-2, 2, width-2, height-2);
        g.drawLine(1, width-2, width-1, height-2);
    }
    
    /**
     * Paints the square, beveled button with gradient fill on to the 
     * Graphics context.
     * 
     * @param g Graphics context.
     */
    private void paintSquareButton(Graphics2D g)
    {
        
        if (this.colour == null) {
            int height = (int) buttonRect.getHeight();
            int width = (int) buttonRect.getWidth();
            
            // Fill
            g.setColor(UIUtilities.BACKGROUND_COLOR);
            g.fillRect(INSETS.left, INSETS.top, width-INSETS.right, height-INSETS.bottom);
            
            // Text
            final FontMetrics fm = g.getFontMetrics();
            final int x = (int) ((buttonRect.width/2.0f)-
                    fm.stringWidth(button.getText())/2.0f);
            final int y = (int) ((buttonRect.height/2.0f) + 
                    (fm.getHeight()-fm.getDescent())/2.0f);
            g.setPaint(Color.BLACK);
            g.drawString(button.getText(), x, y);
            
            return;
        }
        
        if (this.image != null) {
            int height = (int) buttonRect.getHeight();
            int width = (int) buttonRect.getWidth();
            g.drawImage(image, 0, 0, width, height, null);

            // Text
            final FontMetrics fm = g.getFontMetrics();
            final int x = (int) ((buttonRect.width / 2.0f) - fm
                    .stringWidth(button.getText()) / 2.0f);
            final int y = (int) ((buttonRect.height / 2.0f) + (fm.getHeight() - fm
                    .getDescent()) / 2.0f);
            g.setPaint(Color.BLACK);
            g.drawString(button.getText(), x, y);

            return;
        }
        
        // If the button is selected draw selected button face.  
        // Check to see if it's greyed out, if not draw border else
        // draw mask and draw the grey mask selected border. 
        if (button.isSelected()) 
        {
        	if (button.isEnabled()) {
        		if (!greyedOut) 
                {
        			invokePainter(g, selectedButtonFacePainter);
                	drawSelectedBorder(g);
                }
                else
                {
                	invokePainter(g, selectedGreyMaskPainter);
                    drawGreySelectedBorder(g);
                }
        	} else {
        		invokePainter(g, selectedGreyMaskPainter);
                drawGreySelectedBorder(g);
        	}
        } 
        else 
        {
            // If the button is not selected draw unselected button face.  
            // Check to see if it's greyed out, if not draw border else
            // draw mask and draw the grey mask unselected border.
        	if (button.isEnabled()) {
        		if (greyedOut) 
                { 
        			invokePainter(g, greyMaskPainter);
                    drawGreyBorder(g);
                }
                else 
                {
                	invokePainter(g, buttonFacePainter);
                	drawBorder(g);    
                }
        	} else {
        		invokePainter(g, greyMaskPainter);
                drawGreyBorder(g);
        	}
            
        }
         // Draw text in centre of button.
        drawText(g);
    }

    /** Creates painters for the different button options. */
    private void createPainters()
    {
    	buttonFacePainter = getPainter(colour, SPEC);
    	selectedButtonFacePainter = getPainter(colour, MATTE);
    	greyMaskPainter = getPainter(Color.gray, SPEC);
    	selectedGreyMaskPainter = getPainter(Color.gray, MATTE);
    }

    /**
     * Creates the painters for the colour, and adds specular highlight if the
     * passed parameter is <code>true</code>. 
     * 
     * @param colour see above.
     * @param spec see above.
     * @return see above.
     */
    private Painter<JXButton> getPainter(Color colour, int spec) 
	{
		int startX = (int) (getWidth()*0.2);
		int startY = 6;
		int colourStartX = (int) (getWidth()*0.3);
		int colourStartY = 18;
		int matteEndX = 10;
		int matteEndY = 18;
		
		Color c = colour.brighter();
		MattePainter gradientWhite = new MattePainter(
			    new GradientPaint(new Point2D.Double(0.0, 0.0), c,
			    new Point2D.Double(matteEndX, matteEndY), Color.white));
		/*
		MattePainter gradientBrighterColour = new MattePainter(
			    new GradientPaint(new Point2D.Double(0.0, 0.0), c,
			    new Point2D.Double(matteEndX, matteEndY), colour));
		*/
		MattePainter gradientBrighterDarker = new MattePainter(
			    new GradientPaint(new Point2D.Double(0.0, 0.0), c,
			    new Point2D.Double(matteEndX, matteEndY), colour.darker()));
		
		
		//We cannot use this
		/*org.apache.batik.ext.awt.RadialGradientPaint rp =
			new org.apache.batik.ext.awt.RadialGradientPaint(new 
				Point2D.Double(startX,startY), radius, 
				new Point2D.Double(colourStartX, colourStartY),
	                new float[] { 0.0f, 0.5f },
	                new Color[] { new Color(1.0f, 1.0f, 1.0f, 0.4f),
	                    new Color(1.0f, 1.0f, 1.0f, 0.0f) } );*/
		
		HSV newHSV = new HSV(colour);
		float colourAlpha = 0.5f;
		if (newHSV.getHue()>(100.0/360.0) && newHSV.getHue()<(150.0/360.0))
			colourAlpha = 0.7f;
		
		MattePainter gradientLight = new MattePainter(
			    new GradientPaint(new Point2D.Double(startX, startY), 
			    		new Color(1.0f, 1.0f, 1.0f, colourAlpha),
			    new Point2D.Double(colourStartX, colourStartY),  
			    new Color(1.0f, 1.0f, 1.0f, 0.0f) ));
		switch (spec) {
			case SPEC:
				return new CompoundPainter<JXButton>(gradientWhite, 
						gradientBrighterDarker, gradientLight);
			case MATTE:
				return new CompoundPainter<JXButton>( gradientWhite, 
		    			gradientBrighterDarker);
		}
		return null;
	}

    /**
     * Draws the painter on the graphics context.
     * 
     * @param g 	The graphics context.
     * @param painter 	The painter to draw.
     */
    private void invokePainter(Graphics g, Painter painter) 
    {
        if (painter == null) return;
        
        Graphics2D g2d = (Graphics2D) g.create();

        if (!isPaintBorderInsets()) 
        	painter.paint(g2d, this, getWidth(), getHeight());
        else 
        {
        	g2d.translate(INSETS.left, INSETS.top);
        	painter.paint(g2d, this, getWidth()-INSETS.left-INSETS.right,
        			getHeight()-INSETS.top-INSETS.bottom);
        }

    }
    
    /**
     * Creates a new instance.
     * 
     * @param b Reference to parent Button. Mustn't be <code>null</code>.
     * @param c Colour of the button. Mustn't be <code>null</code>.
     */
    ColouredButtonUI(ColouredButton b, Color c)
    {
    	if (b == null) throw new IllegalArgumentException("No button.");
        button = b;
        greyedOut = false;
        fontIndex = Font.PLAIN;
        setColor(c);
        uninstallListeners(b);
    }
    
    /**
     * Buttons can be greyed out, representing that the current model is
     * in greyscale, not RGB, HSV. This is done by adding an alpha blended
     * grey mask over the button. This method sets, unsets buttons to be
     * greyed out.
     * 
     * @param greyedOut Pass <code>true</code> to gray out the button,
     *                  <code>false</code> otherwise.
     */
    void setGrayedOut(boolean greyedOut) { this.greyedOut = greyedOut; }
     
    /**
     * Sets the colour of the button. 
     * 
     * @param c Color to set (can be <code>null</code>, in which case
     * the default background color will be used)
     */
    void setColor(Color c) 
    { 
        this.colour = c; 
        
    	if (c == null) {
    	    return;
    	}
    	    
    	if(c != null) {
    	    setGradientColours();
    	    createPainters();
    	}
    }
    
    /**
     * Set the background image (takes precedence over color!)
     * 
     * @param img
     *            The image
     */
    void setImage(BufferedImage img) {
        this.image = img;
    }
   
    /**
     * Sets the index of the derived font used to paint the text.
     * 
     * @param fontIndex The font index. 
     */
    void setDeriveFont(int fontIndex) { this.fontIndex = fontIndex; }
    
    /**
     * Overridden to paint the button and to renders the text in the centre of
     * the button.
     * @see BasicButtonUI#paint(Graphics, JComponent)
     */
    public void paint(Graphics og, JComponent comp)
    {
        Graphics2D g = (Graphics2D) og;
        buttonRect = new Rectangle(comp.getWidth(), comp.getHeight());
        paintSquareButton(g);
    }
    
    /** 
     * Returns <code>true</code> if the button paints borders, 
     * <code>false</code> otherwise.
     * 
     * @return see above.
     */
    boolean isPaintBorderInsets() { return paintBorderInsets; }

    /**
     * Sets to <code>true</code> if the button paints borders, to
     * <code>false</code> otherwise.
     * 
     * @param pb The value to set.
     */
    void setPaintBorderInsets(boolean pb) { paintBorderInsets = pb; }

    /** 
     * Returns the width of the components.
     * 
     * @return See above.
     */
    protected int getWidth()
    {
    	return Math.max(button.getWidth(), DEFAULT_SIZE);
    }
    
    /** 
     * Returns the height of the components.
     * 
     * @return See above.
     */
    protected int getHeight()
    {
    	return Math.max(button.getHeight(), DEFAULT_SIZE);
    }
    
}
