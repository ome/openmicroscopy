/*
 * org.openmicroscopy.shoola.util.roi.figures.MeasureTextArea 
 *
  *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.util.roi.figures;

//Java imports
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.text.CharacterIterator;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.StringTokenizer;

//Third-party libraries
import org.jhotdraw.draw.AbstractAttributedDecoratedFigure;
import org.jhotdraw.draw.AttributeKeys;
import org.jhotdraw.draw.FontSizeHandle;
import org.jhotdraw.draw.Handle;
import org.jhotdraw.draw.TextAreaTool;
import org.jhotdraw.draw.TextHolderFigure;
import org.jhotdraw.draw.Tool;
import org.jhotdraw.geom.Insets2D;
import org.jhotdraw.xml.DOMInput;
import org.jhotdraw.xml.DOMOutput;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.roi.model.annotation.MeasurementAttributes;

/** 
 * Area with measurement.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class MeasureTextArea  
	extends AbstractAttributedDecoratedFigure 
	implements TextHolderFigure 
{
	
	/** The bounds of the area. */
    private Rectangle2D.Double bounds = new Rectangle2D.Double();
    
    /** Flag indicating if the area can be edited. */
    private boolean editable = true;
    
    /** Copies of the layout. */
    transient private TextLayout textLayout;
    
    /** Creates a new instance. */
    public MeasureTextArea()
    {
        this(ROIFigure.DEFAULT_TEXT);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param text The text to display.
     */
    public MeasureTextArea(String text)
    {
        setText(text);
		setAttribute(MeasurementAttributes.FONT_FACE, ROIFigure.DEFAULT_FONT);
		setAttribute(MeasurementAttributes.FONT_SIZE, 
				new Double(ROIFigure.FONT_SIZE));
    }
    
    /**
     * Overridden to draw the text.
     * @see #drawText(Graphics2D)
     */
    protected void drawText(Graphics2D g)
    {
        if (getText() != null || isEditable()) {
            
            Font font = getFont();
            boolean isUnderlined = MeasurementAttributes.FONT_UNDERLINE.get(
            		this);
            Insets2D.Double insets = getInsets();
            Rectangle2D.Double textRect = new Rectangle2D.Double(
                    bounds.x + insets.left,
                    bounds.y + insets.top,
                    bounds.width - insets.left - insets.right,
                    bounds.height - insets.top - insets.bottom
                    );
            float leftMargin = (float) textRect.x;
            float rightMargin = (float) Math.max(leftMargin + 1, 
            		textRect.x + textRect.width);
            float verticalPos = (float) textRect.y;
            float maxVerticalPos = (float) (textRect.y + textRect.height);
            if (leftMargin < rightMargin) {
                float tabWidth = (float) (getTabSize() * 
                		g.getFontMetrics(font).charWidth('m'));
                float[] tabStops = new float[(int) (textRect.width / tabWidth)];
                for (int i=0; i < tabStops.length; i++) {
                    tabStops[i] = (float) (textRect.x + 
                    		(int) (tabWidth * (i + 1)));
                }
                
                if (getText() != null) {
                    Shape savedClipArea = g.getClip();
                    g.clip(textRect);
                    
                    String[] paragraphs = getText().split("\n");//Strings.split(getText(), '\n');
                    for (int i = 0; i < paragraphs.length; i++) {
                        if (paragraphs[i].length() == 0) paragraphs[i] = " ";
                        AttributedString as = new AttributedString(paragraphs[i]);
                        as.addAttribute(TextAttribute.FONT, font);
                        if (isUnderlined) {
                            as.addAttribute(TextAttribute.UNDERLINE, 
                            		TextAttribute.UNDERLINE_LOW_ONE_PIXEL);
                        }
                        int tabCount = new StringTokenizer(
                        		paragraphs[i], "\t").countTokens() - 1;
                        verticalPos = drawParagraph(g, as.getIterator(), 
                        		verticalPos, maxVerticalPos, leftMargin, 
                        		rightMargin, tabStops, tabCount);
                        if (verticalPos > maxVerticalPos) {
                            break;
                        }
                    }
                    g.setClip(savedClipArea);
                }
            }
            
            if (leftMargin >= rightMargin || verticalPos > 
            textRect.y + textRect.height) {
            //    g.setColor(Color.red);
                //g.draw(new Line2D.Double(textRect.x, textRect.y + textRect.height - 1, textRect.x + textRect.width - 1, textRect.y + textRect.height - 1));
            }
        }
    }
    
    /**
     * Draws a paragraph of text at the specified y location and returns
     * the y position for the next paragraph.
     */
    private float drawParagraph(Graphics2D g, 
    		AttributedCharacterIterator styledText, float verticalPos, 
    		float maxVerticalPos, float leftMargin, float rightMargin, 
    		float[] tabStops, int tabCount)
    {
        
        // assume styledText is an AttributedCharacterIterator, and the number
        // of tabs in styledText is tabCount
        
        int[] tabLocations = new int[tabCount+1];
        
        int i = 0;
        for (char c = styledText.first(); c != CharacterIterator.DONE; 
        c = styledText.next()) {
            if (c == '\t') {
                tabLocations[i++] = styledText.getIndex();
            }
        }
        tabLocations[tabCount] = styledText.getEndIndex() - 1;
        
        // Now tabLocations has an entry for every tab's offset in
        // the text.  For convenience, the last entry is tabLocations
        // is the offset of the last character in the text.
        
        LineBreakMeasurer measurer = new LineBreakMeasurer(styledText, 
        		getFontRenderContext());
        int currentTab = 0;
        
        while (measurer.getPosition() < styledText.getEndIndex() &&
                verticalPos <= maxVerticalPos) {
            
            // Lay out and draw each line.  All segments on a line
            // must be computed before any drawing can occur, since
            // we must know the largest ascent on the line.
            // TextLayouts are computed and stored in a List;
            // their horizontal positions are stored in a parallel
            // List.
            
            // lineContainsText is true after first segment is drawn
            boolean lineContainsText = false;
            boolean lineComplete = false;
            float maxAscent = 0, maxDescent = 0;
            float horizontalPos = leftMargin;
            LinkedList<TextLayout> layouts = new LinkedList<TextLayout>();
            LinkedList<Float> penPositions = new LinkedList<Float>();
            
            while (!lineComplete && verticalPos <= maxVerticalPos) {
                float wrappingWidth = rightMargin - horizontalPos;
                TextLayout layout = null;
                layout =
                        measurer.nextLayout(wrappingWidth,
                        tabLocations[currentTab]+1,
                        lineContainsText);
                
                // layout can be null if lineContainsText is true
                if (layout != null) {
                    layouts.add(layout);
                    penPositions.add(horizontalPos);
                    horizontalPos += layout.getAdvance();
                    maxAscent = Math.max(maxAscent, layout.getAscent());
                    maxDescent = Math.max(maxDescent,
                            layout.getDescent() + layout.getLeading());
                } else {
                    lineComplete = true;
                }
                
                lineContainsText = true;
                
                if (measurer.getPosition() == tabLocations[currentTab]+1) {
                    currentTab++;
                }
                
                if (measurer.getPosition() == styledText.getEndIndex())
                    lineComplete = true;
                else if (tabStops.length == 0 || horizontalPos >= 
                	tabStops[tabStops.length-1])
                    lineComplete = true;
                
                if (!lineComplete) {
                    // move to next tab stop
                    int j;
                    for (j=0; horizontalPos >= tabStops[j]; j++) {}
                    horizontalPos = tabStops[j];
                }
            }
            
            verticalPos += maxAscent;
            
            Iterator<TextLayout> layoutEnum = layouts.iterator();
            Iterator<Float> positionEnum = penPositions.iterator();
            
            // now iterate through layouts and draw them
            while (layoutEnum.hasNext()) {
                TextLayout nextLayout = layoutEnum.next();
                float nextPosition = positionEnum.next();
                nextLayout.draw(g, nextPosition, verticalPos);
            }
            
            verticalPos += maxDescent;
        }
        
        return verticalPos;
    }
    
    
    protected void drawFill(Graphics2D g) {
        g.fill(bounds);
    }
    
    protected void drawStroke(Graphics2D g) {
        //g.draw(bounds);
    }
    
    // SHAPE AND BOUNDS
    public void basicSetBounds(Point2D.Double anchor, Point2D.Double lead) {
        bounds.x = Math.min(anchor.x, lead.x);
        bounds.y = Math.min(anchor.y, lead.y);
        bounds.width = Math.max(1, Math.abs(lead.x - anchor.x));
        bounds.height = Math.max(1, Math.abs(lead.y - anchor.y));
        textLayout = null;
    }
    public void basicTransform(AffineTransform tx) {
        Point2D.Double anchor = getStartPoint();
        Point2D.Double lead = getEndPoint();
        basicSetBounds(
                (Point2D.Double) tx.transform(anchor, anchor),
                (Point2D.Double) tx.transform(lead, lead)
                );
    }
    
    
    public boolean figureContains(Point2D.Double p) {
        return bounds.contains(p);
    }
    
    public Rectangle2D.Double getBounds() {
        return (Rectangle2D.Double) bounds.getBounds2D();
    }
    public void restoreTransformTo(Object geometry) {
        Rectangle2D.Double r = (Rectangle2D.Double) geometry;
        bounds.x = r.x;
        bounds.y = r.y;
        bounds.width = r.width;
        bounds.height = r.height;
    }
    
    public Object getTransformRestoreData() {
        return bounds.clone();
    }
    
    // ATTRIBUTES
    /**
     * Gets the text shown by the text figure.
     */
    public String getText() {
        return (String) getAttribute(MeasurementAttributes.TEXT);
    }
    /**
     * Returns the insets used to draw text.
     */
    public Insets2D.Double getInsets() {
        double sw = Math.ceil(MeasurementAttributes.STROKE_WIDTH.get(this) / 2);
        Insets2D.Double insets = new Insets2D.Double(4,4,4,4);
        return new Insets2D.Double(insets.top+sw,insets.left+sw,insets.bottom+sw,insets.right+sw);
    }
    
    public int getTabSize() {
        return 8;
    }
    
    
    /**
     * Sets the text shown by the text figure.
     */
    public void setText(String newText) {
        setAttribute(MeasurementAttributes.TEXT, newText);
    }
    
    public int getTextColumns() {
        return (getText() == null) ? 4 : Math.max(getText().length(), 4);
    }
    public Font getFont() {
        return AttributeKeys.getFont(this);
    }
    public Color getTextColor() {
        return MeasurementAttributes.TEXT_COLOR.get(this);
    }
    
    public Color getFillColor() {
        return MeasurementAttributes.FILL_COLOR.get(this);
    }
    
    public void setFontSize(float size) {
    	MeasurementAttributes.FONT_SIZE.set(this, new Double(size));
    }
    
    public float getFontSize() {
        return MeasurementAttributes.FONT_SIZE.get(this).floatValue();
    }
    
    // EDITING
    public boolean isEditable() { return editable; }
    
    public void setEditable(boolean b) { this.editable = b; }
    /**
     * Returns a specialized tool for the given coordinate.
     * <p>Returns null, if no specialized tool is available.
     */
    public Tool getTool(Point2D.Double p) {
        return (isEditable() && contains(p)) ? new TextAreaTool(this) : null;
    }
    public TextHolderFigure getLabelFor() { return this; }
    
    
    // CONNECTING
    // COMPOSITE FIGURES
    // CLONING
    public MeasureTextArea clone() {
    	MeasureTextArea that = (MeasureTextArea) super.clone();
        that.bounds = (Rectangle2D.Double) this.bounds.clone();
        return that;
    }
    
    // EVENT HANDLING
    
    public Collection<Handle> createHandles(int detailLevel) {
        LinkedList<Handle> handles = (LinkedList<Handle>) 
        super.createHandles(detailLevel);
        if (detailLevel == 0) {
            handles.add(new FontSizeHandle(this));
        }
        return handles;
    }
    
    protected void validate() {
        super.validate();
        textLayout = null;
    }
    
    
    
    
    protected void readBounds(DOMInput in) throws IOException {
        bounds.x = in.getAttribute("x",0d);
        bounds.y = in.getAttribute("y",0d);
        bounds.width = in.getAttribute("w",0d);
        bounds.height = in.getAttribute("h",0d);
    }
    protected void writeBounds(DOMOutput out) throws IOException {
        out.addAttribute("x",bounds.x);
        out.addAttribute("y",bounds.y);
        out.addAttribute("w",bounds.width);
        out.addAttribute("h",bounds.height);
    }
    public void read(DOMInput in) throws IOException {
        readBounds(in);
        readAttributes(in);
        textLayout = null;
    }
    
    public void write(DOMOutput out) throws IOException {
        writeBounds(out);
        writeAttributes(out);
    }
	/* (non-Javadoc)
	 * @see org.jhotdraw.draw.TextHolderFigure#isTextOverflow()
	 */
	public boolean isTextOverflow() { return false; }
	
	/* (non-Javadoc)
	 * @see org.jhotdraw.draw.Figure#transform(java.awt.geom.AffineTransform)
	 */
	public void transform(AffineTransform tx) { }
}
