/*
 * org.openmicroscopy.shoola.util.ui.omeeditpane.OMEEditPane 
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
package org.openmicroscopy.shoola.util.ui.omeeditpane;


//Java imports
import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.Highlighter;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;

//Third-party libraries

//Application-internal dependencies

/** 
 * 
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
public class OMEEditPane
	extends JTextPane
{
	/** Regex expression defining Dataset [Dataset: id 30]. */
	public static String DATASETREGEX = "\\[(Dataset|dataset):[ ]*(id|ID|name|Name)[ ]*[a-zA-Z0-9]+[ ]*\\]";

	/** Regex expression defining Project [Project: id 30]. */
	public static String PROJECTREGEX = "\\[(Project|project):[ ]*(id|ID|name|Name)[ ]*[a-zA-Z0-9]+[ ]*\\]";
	
	/** Regex expression defining Image [Image: id 30]. */
	public static String IMAGEREGEX = "\\[(Image|image):[ ]*(id|ID|name|Name)[ ]*[a-zA-Z0-9]+[ ]*\\]";
	
	/** Regex expression defining url. */
	public static String URLREGEX = "\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
	
	/** Regex expression defining Thumbnail [Thumbnail: id 30]. */
	public static String THUMBNAILREGEX = "\\[(Thumbnail|thumbnail):[ ]*(id|ID|name|Name)[ ]*[a-zA-Z0-9]+[ ]*\\]";
	
	/** Map containing Regex, and formatters. */
	Map<String, EditFormatter> 	formatters;
	
	/** Map holding the point(start, and end) locations of the regex as value. */
	Map<Point, String>			elementLocations; 	
	
	/** The defaultStyle for text in the document. */
	Style 						defaultStyle;
	
	/**
	 * Define the OMEEditPane, create the default maps, and styles. 
	 */
	public OMEEditPane()
	{
		formatters = new HashMap<String, EditFormatter>();
		elementLocations = new HashMap<Point, String>();
		defaultStyle = addStyle("OMEDefaultStyle", null);
		StyleConstants.setForeground(defaultStyle, Color.black);
	}
	
	/**
	 * Get the default style for text in the document.
	 * @return see above.
	 */
	public Style getDefaultStyle()
	{
		return defaultStyle;
	}
	
	/**
	 * Build the format map when the text change, (elements are split)
	 */	
	private void buildFormatMap()
	{
		elementLocations = new HashMap<Point, String>();
	}
	
	/** 
	 * Add formatter for regex to the OMEEditPane.
	 * @param regex see above.
	 * @param formatter see above.
	 */
	public void addFormatter(String regex, EditFormatter formatter)
	{
		formatters.put(regex, formatter);
	}
	
	/** 
	 * Remove formatter for regex to the OMEEditPane.
	 * @param regex see above.
	 */
	public void removeFormatter(String regex)
	{
		formatters.remove(regex);
	}

	/**
	 * Remove the formatting for any elements, that have changed when the user 
	 * types in the editor pane.
	 */
	private void removeFormatting()
	{
		Highlighter.Highlight[] highlights = this.getHighlighter().getHighlights();
	    for (int i = 0; i < highlights.length; i++) {
	      Highlighter.Highlight h = highlights[i];
	        this.getHighlighter().removeHighlight(h);
	    }
	    
	    Element sectionElem = getDocument().getDefaultRootElement();
	    
        // Get number of paragraphs.
        int paraCount = sectionElem.getElementCount();
        int caretPosition = getCaretPosition();
    
        for (int i=0; i<paraCount; i++) 
        {
            Element paraElem = sectionElem.getElement(i);
            AttributeSet attr = paraElem.getAttributes();
    
            // Get the name of the style applied to this paragraph element; may be null
            String sn = (String)attr.getAttribute(StyleConstants.NameAttribute);
    
            int rangeStart = paraElem.getStartOffset();
            int rangeEnd = paraElem.getEndOffset();
            if(caretPosition < rangeStart || caretPosition>rangeEnd)
            	continue;
    
            // Enumerate the content elements
            if(paraElem.getElementCount()==0)
            	return;
            
            for (int j=0; j<paraElem.getElementCount(); j++) 
            {
                Element contentElem = paraElem.getElement(j);
                attr = contentElem.getAttributes();
    
                // Get the name of the style applied to this content element; may be null
                sn = (String)attr.getAttribute(StyleConstants.NameAttribute);
    
                // Reapply the content style
                rangeStart = contentElem.getStartOffset();
                rangeEnd = contentElem.getEndOffset();
                if(caretPosition >= rangeStart && caretPosition<=rangeEnd)
                {	
                	Point p = getElementLocation(caretPosition);
                	int offset = 10;
                	if(p!=null)
                		offset = caretPosition-p.x;
                	int elemStart = Math.max(j-offset,0);
                	int elemEnd = Math.min(j+offset, paraElem.getElementCount());
                	
                	for(int elemIndex = elemStart ; elemIndex < elemEnd ; elemIndex++)
                	{
                		Element resetElement = paraElem.getElement(elemIndex);
                		rangeStart = resetElement.getStartOffset();
                		rangeEnd = resetElement.getEndOffset();
                		String elementString;
						try
						{
							elementString=getDocument().getText(rangeStart, rangeEnd-rangeStart);
							String regex = noMatch(elementString); 
							if(regex.equals(""))
	                		{
								
	                			getStyledDocument().setCharacterAttributes(
	                				rangeStart, rangeEnd-rangeStart, getDefaultStyle(), true);
	                		}
						}
						catch (BadLocationException e)
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
                		
                     	
                	}
                	break;
                }
            }
        }
	}

	/**
	 * Remove the formatting on the changed elements, and reformat all elements, 
	 * that match the regex in the formatters.
	 */
	public void matchFormatters()
	{
			SwingUtilities.invokeLater(new Runnable() 
			{
			        public void run() 
			        {
			        	removeFormatting();
			        	buildFormatMap();
			        	Iterator<String> regexIterator = formatters.keySet().iterator();
			        	while(regexIterator.hasNext())
			        	{
			        		String regex = regexIterator.next();
			        		EditFormatter formatter = formatters.get(regex);
			        		try
							{
								formatText(regex, formatter);
							}
							catch (BadLocationException e)
							{
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
			        	}
			        }
			});
	}
	
	/**
	 * Called when the mouse is pressed and calls the actionPerformed event for
	 * the appropriate SelectionAction of the formatter.value.
	 * @param e mouse event.
	 * @throws BadLocationException if the mouse is outside text.
	 */
	public void onSelection(MouseEvent e) throws BadLocationException
	{
		int index = viewToModel(new Point(e.getX(), e.getY()));
		String elementType = getElementType(index);
		Point point = getElementLocation(index);
		if(elementType.equals(""))
			return;
		EditFormatter formatter = formatters.get(elementType);
		formatter.onSelection(this, point.x, point.y);
	}
	
	/**
	 * Return the regex matching the string or empty string ""
	 * @param str see above.
	 * @return see above.
	 */
	private String noMatch(String str)
	{
    	Iterator<String> regexIterator = formatters.keySet().iterator();
    	while(regexIterator.hasNext())
    	{
    		String regex = regexIterator.next();
    		Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(str);
			if(matcher.find())
			{
				return regex;
			}
    	}
    	return "";
			
	}
	
	/**
	 * Format the text for the regex using the formatter. 
	 * @param regex see above.
	 * @param formatter see above.
	 * @throws BadLocationException 
	 */
	private void formatText(String regex, EditFormatter formatter) throws BadLocationException
	{
		int matchCount = 0;
		Pattern pattern = Pattern.compile(regex);
		String fragment = this.getDocument().getText(0, this.getDocument().getLength());
		Matcher matcher = pattern.matcher(fragment);
		while (matcher.find()) 
		{
			elementLocations.put(new Point(matcher.start(), matcher.end()), regex);
			formatter.onPatternMatch(this, matcher.start(),
		                                matcher.end());
			++matchCount;
		}
	}
	
	/** 
	 * Get the location of the element spanning position i or null if none.
	 * @param i see above.
	 * @return see above.
	 */
	private Point getElementLocation(int i)
	{
		Iterator<Point> pointIterator = elementLocations.keySet().iterator();
		while(pointIterator.hasNext())
		{
			Point p = pointIterator.next();
			if(i>=p.x && i<=p.y)
				return p;
		}
		return null;
	}

	/**
	 * Get the regex for the element at i or return ""
	 * @param i position in document.
	 * @return see above.
	 */
	public String getElementType(int i)
	{
		Iterator<Point> pointIterator = elementLocations.keySet().iterator();
		while(pointIterator.hasNext())
		{
			Point p = pointIterator.next();
			if(i>=p.x && i<=p.y)
				return elementLocations.get(p);
		}
		return "";
	}
	
}



