/*
 * org.openmicroscopy.shoola.util.ui.ColouredButtonUI
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
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
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
import java.awt.Rectangle;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.plaf.basic.BasicButtonUI;

//Third-party libraries

//Application-internal dependencies
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

    /** Current Colour of the button. */
    private Color       colour;

    /** Reference to parent button. */
    private JButton     button;
    
    /** The button's size, used by paint to draw onto component. */
    private Rectangle   buttonRect;
    
    /** 
     * HSV colour user to determine the start(top) of the gradient in the
     * button.
     */
    private HSV         gradientStartHSV;
    
    /** The Color conversion from HSV used in the paintGradient command.*/
    private Color       gradientStartRGB;
    
    /** 
     * HSV colour user to determine the End(bottom) of the gradient in the
     * button.
     */
    private HSV         gradientEndHSV;
    
    /** The Color conversion from HSV used in the paintGradient command.*/
    private Color       gradientEndRGB;
    
    /** 
     * If true the buttons will have a grey mask painted on top of their
     * button face. 
     */
    private boolean             greyedOut;
    
    /**
     * Buttons can be greyed out, representing that the current model is
     * in greyscale, not RGB, HSV. This is done by adding an alpha blended
     * grey mask over the button. This method adds teh grey mask to the 
     * button.
     *   
     * @param g Graphics2D drawing context. 
     */
    private void drawGreyMask(Graphics2D g)
    {
        GradientPaint gp;
        
        // Set a gradient mask of V=.5 to V = 0.3; this has an alpha
        // value to show the colours of the button underlying the mask.
        Color gradientStart = new Color(0.5f, 0.5f, 0.5f, 1.0f);
        Color gradientEnd = new Color(0.3f, 0.3f, 0.3f, 1.0f);
                
        // Draw the gradient mask.
        gp = new GradientPaint((int) buttonRect.getX(),
                 (int) buttonRect.getY(), gradientStart,
                 (int) buttonRect.getWidth(),
                 (int) buttonRect.getHeight(), gradientEnd, false);
    
        g.setPaint(gp);
        g.fill(buttonRect);
    }
    
    /**
     * This method calculates the size of the button's text. It then
     * renders the text in the centre of the button, it also changes the
     * colour of the text to maximise the contrast between it and the 
     * background. 
     * 
     * @param g Graphics2D drawing context.
     */
    private void drawText(Graphics2D g)
    {
        HSV col = new HSV(colour);
        Font fnt = button.getFont();
        fnt = fnt.deriveFont(Font.PLAIN, 10);
        g.setFont(fnt);
        FontMetrics fm = g.getFontMetrics();
        
        // Using the font metrics, centre the text in the button face.
        int x = (int) ((buttonRect.width/2.0f)-
                fm.stringWidth(button.getText())/2);
        int y = (int) ((buttonRect.height/2.0f) + 
                (fm.getHeight()-fm.getDescent())/2);
        
        // If the button face is dark or does not contrast well with the 
        // black text turn the text white.
        if(col.getValue() < 0.6 || (col.getHue() > 0.6 && 
           col.getSaturation() > 0.7) || greyedOut)
            g.setPaint(Color.white);
        else
            g.setPaint(Color.black);
        g.drawString(button.getText(), x, y);
    }
    
    
    /**
     * This method calculates the start and end colours for the gradient on
     * the face of the button. 
     */
    private void setGradientColours()
    {
        HSV col = new HSV(colour);
         // top gradient value from HSV model. 
            float topGradientValue = col.getValue();
            // bottom gradient value from HSV model. 
            float bottomGradientValue = col.getValue();
            // top and bottom gradient saturation from HSV model. 
            float topGradientSaturation, bottomGradientSaturation;
            
            // if colour greyscale(achromatic) don't touch saturation
            if (col.getSaturation() == 0)
            {
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
            }
            else
            {
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
     * Render the button face, using the colours for the gradient set in
     * {@link #setGradientColours()}.
     * 
     * @param g Graphics2D render context.
     */
    private void drawButtonFace(Graphics2D g)
    {
        GradientPaint gp;
        gp = new GradientPaint((int) buttonRect.getX(),
                 (int) buttonRect.getY(), gradientStartRGB,
                 (int) buttonRect.getWidth(),
                 (int) buttonRect.getHeight(), gradientEndRGB, false);
    
        g.setPaint(gp);
        g.fill(buttonRect);
    }

    /**
     * The button has been selected. Draw the button face, inverting the 
     * gradient and darkening the colours used in the gradeint fill.
     *  
     * @param g Graphics2D render context.
     */
    private void drawSelectedButtonFace(Graphics2D g)
    {
        GradientPaint gp;
    
        gp = new GradientPaint((int) buttonRect.getX(),
                 (int) buttonRect.getY(), gradientEndRGB,
                 (int) buttonRect.getWidth(),
                 (int) buttonRect.getHeight(), gradientEndRGB, false);
    
        g.setPaint(gp);
        g.fill(buttonRect);
    }
        
    /**
     * Draws a bevelled border for an unselected Button, the top and left 
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
        g.setStroke(new BasicStroke(1.0f));
       
        // Draw the bevel, it is drawn as four line from: topleft to 
        // topright, and topleft to bottom left. 
        g.drawLine(0, 0, 0, (int) buttonRect.getHeight());
        g.drawLine(0, 0, (int) buttonRect.getWidth(), 0);
        g.drawLine(1, 1, 1, (int) buttonRect.getHeight()-1);
        g.drawLine(1, 1, (int) buttonRect.getWidth()-1, 1);
        borderColourHSV = new HSV(0, 0, gradientEndHSV.getValue(),0.8f);
        borderColour = borderColourHSV.toColorA();
        
        // Set the colour of the bottom, right bevels to be a darker colour
        // than the grey mask of the gradient at that same corner.
        g.setPaint(borderColour.darker().darker().darker());
       
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
        g.setStroke(new BasicStroke(1.0f));
        
        // Draw the bevel, it is drawn as four line from: topleft to 
        // topright, and topleft to bottom left. 
        g.drawLine(0, 0, 0, (int) buttonRect.getHeight());
        g.drawLine(0, 0, (int) buttonRect.getWidth(), 0);
        g.drawLine(1, 1, 1, (int) buttonRect.getHeight()-1);
        g.drawLine(1, 1, (int) buttonRect.getWidth()-1, 1);
        borderColourHSV = new HSV(0, 0, gradientStartHSV.getValue(), 0.8f);
        borderColour = borderColourHSV.toColorA();
        
        // Set the colour of the bottom, right bevels to be a darker colour
        // than the grey mask of the gradient at that same corner.
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
        g.setStroke(new BasicStroke(1.0f));
        
        // Draw the bevel, it is drawn as four line from: topleft to 
        // topright, and topleft to bottom left. 
        g.drawLine(0, 0, 0, (int) buttonRect.getHeight());
        g.drawLine(0, 0, (int) buttonRect.getWidth(), 0);
        g.drawLine(1, 1, 1, (int) buttonRect.getHeight()-1);
        g.drawLine(1, 1, (int) buttonRect.getWidth()-1, 1);
        borderColour = gradientStartRGB;
        col = new HSV(borderColour);
        col.setSaturation(col.getSaturation()*0.8f);
        borderColour = col.toColorA();

        // Set the colour of the bottom, right bevels to be a darker colour
        // than the grey mask of the gradient at that same corner.
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
     * Paints the square, beveled button with gradient fill on to the 
     * Grpahics context.
     * 
     * @param g Graphics context.
     */
    private void paintSquareButton(Graphics2D g)
    {
        // Calculate the colours to use to the extreme ends of the gradient.
        setGradientColours();
        
        // If the button is selected draw selected button face.  
        // Check to see if it's greyed out, if not draw border else
        // draw mask and draw the grey mask selected border. 
        if (button.isSelected()) 
        {
            drawSelectedButtonFace(g);
            if (!greyedOut) drawSelectedBorder(g);
            else 
            {
                drawGreyMask(g);
                drawGreySelectedBorder(g);
            }
        } 
        else 
        {
            // If the button is not selected draw unselected button face.  
            // Check to see if it's greyed out, if not draw border else
            // draw mask and draw the grey mask unselected border.
            drawButtonFace(g);
            if (!greyedOut) drawBorder(g);
            else 
            {
                drawGreyMask(g);
                drawGreyBorder(g);
            }

        }
        // Draw text in centre of button.
        drawText(g);
    }
    
    /**
     * Constructor for ButtonUI component.
     * 
     * @param b Reference to parent Button.
     * @param c Colour of the button.
     */
    ColouredButtonUI(JButton b, Color c)
    {
        if (b == null)
            throw new IllegalArgumentException("No button.");
        if (c == null) 
            throw new IllegalArgumentException("No color.");
        colour = c;
        button = b;
        greyedOut = false;
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
     * @param c Color to set.
     */
    void setColor(Color c)  { this.colour = c; }
       
    /**
     * Overridden, Paints the button, and Renders the text in the centre of
     * the button.
     * @see BasicButtonUI#paint(Graphics, JComponent)
     */
    public void paint(Graphics og, JComponent comp)
    {
        Graphics2D g = (Graphics2D) og;
        buttonRect = new Rectangle(comp.getWidth(), comp.getHeight());
        paintSquareButton(g);
    }
    
}
