/*
 * org.openmicroscopy.shoola.env.ui.tdialog.TitlePainter
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

package org.openmicroscopy.shoola.env.ui.tdialog;


//Java imports
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

//Third-party libraries

//Application-internal dependencies

/** 
 * Paints the title string on the {@link TitleBar} of the {@link TinyDialog}.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class TitlePainter
{
    
    /** Color to draw the line 1 in the pattern. */
    private static final Color    ROW_1 = new Color (227, 245, 238);
    
    /** Color to draw the line 2 in the pattern. */
    private static final Color    ROW_2 = new Color(242, 252, 248);
    
    /** Color to draw the line 3 in the pattern. */
    private static final Color    ROW_3 = new Color(253, 251, 251);
    
    /** The string to paint. */
    private String  title;
    
    /** The font to use for painting. */
    private Font    font;
      
    /**
     * Tells if the current {@link #font} will fit into the specified height.
     * That is, if the maximum height that text rendered in the current
     * {@link #font} on the given graphics context would occupy is less or
     * equal to <code>availHeight</code>.
     * 
     * @param availHeight The available height in which the font should fit.
     * @param g2D The graphics context in which the font will be rendered.
     * @return <code>true</code> if the current {@link #font} fits into the
     *          <code>availHeight</code>, <code>false</code> otherwise. 
     */
    private boolean isFontSuitable(int availHeight, Graphics2D g2D)
    {
        FontMetrics fm = g2D.getFontMetrics(font);
        int maxH = fm.getMaxAscent()+fm.getMaxDescent();
        return (maxH <= availHeight);
    }
    
    /**
     * Sets {@link #font} to a derived font such that {@link #title} can be 
     * rendered on the given graphics context within the specified height.
     *  
     * @param availHeight The available height in which {@link #title} should 
     *                      fit.
     * @param g2D The graphics context in which {@link #title} will be rendered.
     */
    private void ensureSuitableFont(int availHeight, Graphics2D g2D)
    {
        float size = font.getSize2D();
        while (1 < size && !isFontSuitable(availHeight, g2D)) {
            size -= 0.5f;
            font = font.deriveFont(size);
        }
    }
    //NOTE: After the first invocation of doPaint, the font won't change.
    //In fact, the available height is always the same -- is the height
    //of the title bar, which is fixed.
    
    /**
     * Creates a substring from {@link #title} such that it'll fit the 
     * <code>availWidth</code>.
     * 
     * @param availWidth The available width in which the returned string 
     *                      should fit.
     * @param g2D The graphics context in which the string will be rendered.
     * @return A substring of title that fits <code>availWidth</code>.  If
     *          no substring would fit, then <code>"..."</code> is returned.
     */
    private String getDisplayTitle(int availWidth, Graphics2D g2D)
    {
        FontMetrics fm = g2D.getFontMetrics();
        int len = title.length(), i;
        for (i = 0; i < len; ++i) {
            Rectangle2D bounds = fm.getStringBounds(title.substring(0, i), g2D);
            if (availWidth < bounds.getWidth()) break;
        }
        if (i == len) return title;  //It all fits. ("" is irrelevant.)
        
        //If we're here, then i<len.  So the loop exited b/c of the break.
        if (i <= 3)  
            //"abc" or less doesn't fit; "..." is the best we can try then.
            //If it doesn't fit, don't care: g2D was clipped to availWidth.
            return "...";
        
        //We have "abcd" at least and not all fits.  We know that title[0,i]  
        //surely fits.  But it's kinda ugly to truncate text like that.  We
        //want to add "..." to make it better looking.  We know 3 < i and so
        //we can do title[0,i-3], which fits because title[0,i] does.  Let's
        //try to append "..." to it.  Even though we can be sure title[0,i-3]
        //+"..." will fit, we don't care: g2D was clipped to availWidth.
        return title.substring(0, i-3)+"...";
    }
    
    /**
     * Creates a new instance to paint the specified <code>title</code>.
     * The <code>fontProto</code> is used to derive a font with the same
     * attributes, but with a size suitable for painting.  That is, a size
     * such that the <code>title</code>'s height won't exceed the height
     * of the available painting area. 
     * 
     * @param fontProto The font prototype to derive the a suitable font for
     *                  painting.  Mustn't be <code>null</code>.
     */
    TitlePainter(Font fontProto)
    {
        if (fontProto == null) throw new NullPointerException("No font proto.");
        font = fontProto;
        title = "";
    }
    
    /**
     * Sets the title string.
     * 
     * @param t The string to paint.  If <code>null</code>, nothing will
     *          be painted.
     */
    void setTitle(String t) 
    { 
        if (t == null) t = "";
        title = t;
    }
    
    /** 
     * Paints the background on <code>area</code>. 
     * 
     * @param g2D The graphics context.
     * @param width The width of the area.
     * @param height The height of the area.
     */
    protected void doPaintBg(Graphics2D g2D, int width, int height)
    {
        //Draw the pattern.
        for(int y = 0; y < height; y += 3) {
            //Row 1.
            g2D.setColor(ROW_1);
            g2D.drawLine(0, y, width, y);
            //Row 2.
            g2D.setColor(ROW_2);
            g2D.drawLine(0, y+1, width, y+1);
            //Row 3.
            g2D.setColor(ROW_3);
            g2D.drawLine(0, y+2, width, y+2);
        }
        //NOTE: If h%3 != 0, the above will draw more lines than h.
        //Don't care: the graphics context has already been clipped 
        //to Rect[0, 0, width, height].
    }
    
    /** 
     * Paints the title on <code>area</code>. 
     * 
     * @param g2D The graphics context.
     * @param width The width of the area.
     * @param height The height of the area.
     */
    protected void doPaintTitle(Graphics2D g2D, int width, int height)
    {
        //Make sure we have a font such that title's height will fit into area.
        ensureSuitableFont(height, g2D);
        
        //Center the title vertically, clip it if too long, and draw it.
        g2D.setColor(Color.BLACK);
        g2D.setFont(font);  //Set current font and *then* get fm.
        FontMetrics fm = g2D.getFontMetrics();  //NB: fm is for font now.
        int baseline = (height+fm.getAscent()-fm.getDescent())/2;
        g2D.drawString(getDisplayTitle(width, g2D), 0, baseline);
    }
    
    /**
     * Paints the title on the given <code>titleArea</code>
     * and the backgroung on the the given <code>bgArea</code>.
     *  
     * @param g2D       The graphics context. Mustn't be <code>null</code>.
     * @param titleArea The title area to paint. Mustn't be <code>null</code>.
     * @param bgArea    The backgroung area to paint. 
     *                  Mustn't be <code>null</code>.
     */
    void paint(Graphics2D g2D, Rectangle titleArea, Rectangle bgArea)
    {
        if (g2D == null) throw new NullPointerException("No graphics context.");
        if (titleArea == null) throw new NullPointerException("No title area.");
        if (bgArea == null) 
            throw new NullPointerException("No background area.");
        Graphics2D scratchGraphics = (Graphics2D) g2D.create(bgArea.x, bgArea.y,
                                        bgArea.width, bgArea.height);
        doPaintBg(scratchGraphics, bgArea.width, bgArea.height);
        scratchGraphics.dispose();
        scratchGraphics = (Graphics2D) g2D.create(titleArea.x, titleArea.y,
                titleArea.width, titleArea.height);
        doPaintTitle(scratchGraphics, titleArea.width, titleArea.height);
        scratchGraphics.dispose();
    }

}
