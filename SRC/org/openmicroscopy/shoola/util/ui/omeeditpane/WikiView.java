/*
 * org.openmicroscopy.shoola.util.ui.omeeditpane.WikiView 
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
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.PlainView;
import javax.swing.text.Segment;
import javax.swing.text.Utilities;

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
public class WikiView
	extends PlainView
{ 
	/** The editor pane this view represents. */
	JEditorPane 								editorPane;
	
	/** Map holding the point(start, and end) locations of the regex as value. */
	Map<Position, String>						tokenLocations; 	
	
	/**  The map of regex and the list of positions for that regex */
	Map<String, List<Position>> 				regexMap;
	
	/** Map of the regex vs Formatter. */
	Map<String, FormatSelectionAction>			formatMap;
	
	/**
	 * Get the list of all regex in regex map.
	 * @return see above.
	 */
	public String[] getRegexList()
	{
		return (String[])regexMap.keySet().toArray();
	}
	
	/**
	 * Get the positionList
	 * @param regex see above.
	 * @return see above.
	 */
	public List<Position> getPositionList(String regex)
	{
		return regexMap.get(regex);
	}
	
	/**
	 * Parse the text, and find all the regex, and map the token positions to 
	 * it. 
	 * @param text see above.
	 */
	public void parse(String text)
	{
		createTokenMaps();
		Iterator<String> regexIterator = formatMap.keySet().iterator();
		while(regexIterator.hasNext())
		{
			String regex = regexIterator.next();
			List<Position> positionList = createPositionList(regex);
			try
			{
				findAllExpressions(text, regex, positionList);
				regexMap.put(regex, positionList);
			}
			catch (BadLocationException e)
			{
				// Stupid exception, never going to happen. 
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Find all the regex in the text, add it to the positionList.
	 * @param text see above.
	 * @param regex see above.
	 * @param positionList see above.
	 * @throws BadLocationException stupid exception that cannot occur. 
	 */
	private void findAllExpressions(String text, String regex, 
			List<Position> positionList) 
	throws BadLocationException
	{
		positionList.clear();
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(text);
		while(matcher.find())
		{
			int s = matcher.start();
			int e = matcher.end();
			Position p = new Position(s, e);
			if(alreadyMatched(p))
				continue;
			positionList.add(p);
			tokenLocations.put(p, regex);
		}
	}
	
	/**
	 * has the text in position p been already matched by the another 
	 * regex and been places in tokenLocations.
	 * @param p see above.
	 * @return see above.
	 */
	private boolean alreadyMatched(Position p)
	{
		Iterator<Position> positionIterator = tokenLocations.keySet().iterator();
		
		while(positionIterator.hasNext())
		{
			Position mapPosition = positionIterator.next();
			if(mapPosition.contains(p))
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Get the tokenLocations for the current tokenMaps.
	 * @return see above.
	 */
	public Map<Position, String> getTokenLocations()
	{
		return tokenLocations;
	}
	
	/**
	 * Create the token location maps. 
	 */
	private void createTokenMaps()
	{
		tokenLocations = new TreeMap<Position, String>();
		regexMap = new HashMap<String, List<Position>>();
	}
	
	/**
	 * Create the position list for
	 * @param regex
	 * @return see above
	 */
	private List<Position> createPositionList(String regex)
	{
		List<Position> positionList;
		if(!regexMap.containsKey(regex))
		{
			positionList = new ArrayList<Position>();
			regexMap.put(regex, positionList);
		}
		else
			positionList = regexMap.get(regex);
		return positionList;
	}
	
	/**
	 * Instantiate the wikiview.
	 * @param elem see above.
	 * @param formatMap the map of regex and formatters to apply.
	 * @param editorPane the component this resides in.
	 */
	public WikiView(Element elem,  Map<String, FormatSelectionAction> formatMap, 
							JEditorPane editorPane)
	{
		super(elem);
		this.formatMap = formatMap;
		this.editorPane = editorPane;
	}	
	
	/**
	 * Overridden to control if the passed object equals the current one.
	 * @see javax.swing.text.PlainView#drawUnselectedText(Graphics, 
	 * int, int, int, int)
	 */
	protected int drawUnselectedText(Graphics graphics, int x, int y, int p0,
			int p1) throws BadLocationException 
	{
		TextFormatter.removeHighlights(editorPane);
		Document doc = getDocument();
		String text = doc.getText(p0, p1 - p0);
		
		Segment segment = getLineBuffer();
		
		parse(text);
		
		int i = 0;
		Position p;
		
		Iterator<Position> positionIterator = tokenLocations.keySet().iterator();
		while(positionIterator.hasNext())
		{
			p = positionIterator.next();
			if (i < p.start) 
			{ 
				graphics.setColor(Color.black);
				doc.getText(p0 + i, p.start - i, segment);
				x = Utilities.drawTabbedText(segment, x, y, graphics, this, i);
			}
			
			i = p.end;
			doc.getText(p0 + p.start, i - p.start, segment);
			String regex = tokenLocations.get(p);
			FormatSelectionAction fs = formatMap.get(regex);
			x = fs.getFormatter().formatText(editorPane, segment, x, y, graphics, this, i,
														p.start, p.end);
		}
		
		// 	Paint possible remaining text black
		if (i < text.length()) 
		{
			graphics.setColor(Color.black);
			doc.getText(p0 + i, text.length() - i, segment);
			x = Utilities.drawTabbedText(segment, x, y, graphics, this, i);
		}
		return x;
	}
	
	/**
	 * Overridden to control if the passed object equals the current one.
	 * @see javax.swing.text.PlainView#drawSelectedText(Graphics, 
	 * int, int, int, int)
	 */
	protected int drawSelectedText(Graphics g, int x, int y, int p0, int p1) 
											throws BadLocationException 
	{
		return drawUnselectedText(g, x, y, p0, p1);
	}
	   
	/**
	 * Get the selection action for the token at index. 
	 * @param index see above.
	 * @return the selectionAction.
	 */
	public SelectionAction getSelectionAction(int index)
	{
		Document doc = getDocument();
		try
		{
			parse(doc.getText(0, doc.getLength()));
		}
		catch (BadLocationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Iterator<Position> positionIterator = tokenLocations.keySet().iterator();
		while(positionIterator.hasNext())
		{
			Position p = positionIterator.next();
			if(p.contains(index, index))
			{
				String regex = tokenLocations.get(p);
				FormatSelectionAction fsa = formatMap.get(regex);
				return fsa.getSelectionAction();
			}
		}
		return null;
	}
	
	/**
	 * Get the selected text for the token at index. 
	 * @param index see above.
	 * @return the text.
	 */
	public String getSelectedText(int index)
	{
		Document doc = getDocument();
		Iterator<Position> positionIterator = tokenLocations.keySet().iterator();
		while(positionIterator.hasNext())
		{
			Position p = positionIterator.next();
			if(p.contains(index, index))
			{
				try
				{
					return doc.getText(p.start, p.length());
				}
				catch (BadLocationException e)
				{
					return "";
				}
			}
		}
		try
		{
			return doc.getText(index, index);
		}
		catch (BadLocationException e)
		{
			return "";
		}
	}
	
	
}
    



