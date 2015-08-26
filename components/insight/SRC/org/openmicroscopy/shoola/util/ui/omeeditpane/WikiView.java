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

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

/** 
 * The View.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
public class WikiView
    extends PlainView
{ 

    /** The editor pane this view represents. */
    private JEditorPane editorPane;

    /** Map holding the point(start and end) locations of the regex as value. */
    private Map<Position, String> tokenLocations;

    /**  The map of regex and the list of positions for that regex */
    private Map<String, List<Position>> regexMap;

    /** Map of the regex vs Formatter. */
    private Map<String, FormatSelectionAction> formatMap;

    /**
     * Returns the list of all regex in regex map.
     *
     * @return See above.
     */
    String[] getRegexList()
    {
        Set<String> keys = regexMap.keySet();
        String[] array = new String[keys.size()];
        Iterator<String> i = keys.iterator();
        int index = 0;
        while (i.hasNext()) {
            array[index] = i.next();
            index++;
        }
        return array;
    }

    /**
     * Returns the list o position.
     *
     * @param regex The key.
     * @return See above.
     */
    List<Position> getPositionList(String regex)
    {
        return regexMap.get(regex);
    }

    /**
     * Parses the text, and find all the regex, and map the token positions to 
     * it. 
     *
     * @param text The text to parse.
     */
    private void parse(String text)
    {
        createTokenMaps();
        Iterator<String> regexIterator = formatMap.keySet().iterator();
        List<Position> positionList;
        String regex;
        while (regexIterator.hasNext())
        {
            regex = regexIterator.next();
            positionList = createPositionList(regex);
            findAllExpressions(text, regex, positionList);
            regexMap.put(regex, positionList);
        }
    }

    /**
     * Finds all the regex in the text, add it to the positionList.
     *
     * @param text The text to handle.
     * @param regex The regular expression.
     * @param positionList The list of position.
     */
    private void findAllExpressions(String text, String regex,
            List<Position> positionList) 
    {
        positionList.clear();
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        int s, e;
        Position p;
        while (matcher.find())
        {
            s = matcher.start();
            e = matcher.end();
            p = new Position(s, e);
            if (alreadyMatched(p))
                continue;
            positionList.add(p);
            tokenLocations.put(p, regex);
        }
    }

    /**
     * Public method to access regex functionality.
     * Parses the <code>text</code> with the <code>regex</code>, adding
     * any matches (defined by start and end {@link Position}) to the
     * <code>positionList</code>
     *
     * @param text The text to parse
     * @param regex Regex expressions to look for
     * @param positionList A list of the matches found.
     */
    public static void findExpressions(String text, String regex,
            List<Position> positionList) 
    {
        positionList.clear();
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        int s, e;
        Position p;
        while (matcher.find())
        {
            s = matcher.start();
            e = matcher.end();
            p = new Position(s, e);
            positionList.add(p);
        }
    }

    /**
     * Public method to access regular expressions functionality.
     * Parses the <code>text</code> with the <code>regular expression</code>,
     * adding any matches (defined by start and end {@link Position}) to the
     * <code>positionList</code>
     * 
     * @param text The text to parse.
     * @param regex Regular expressions to look for.
     * @param positionMap A map of the matches found.
     */
    public static void findGroups(String text, String regex,
            Map<Position,String> positionMap) 
    {
        positionMap.clear();
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        int s, e;
        String match = null;
        while (matcher.find()) {
            match = matcher.group(matcher.groupCount());
            s = matcher.start();
            e = matcher.end();
            positionMap.put(new Position(s, e), match);
        }
    }

    /**
     * Returns <code>true</code> if the text in position p been already 
     * matched by the another regex and been places in tokenLocations,
     * <code>false</code> otherwise.
     * 
     * @param p The position to handle.
     * @return See above.
     */
    private boolean alreadyMatched(Position p)
    {
        Iterator<Position> positionIterator = tokenLocations.keySet().iterator();
        Position mapPosition;
        while (positionIterator.hasNext())
        {
            mapPosition = positionIterator.next();
            if (mapPosition.contains(p))
                return true;
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

    /** Creates the token location maps. */
    private void createTokenMaps()
    {
        tokenLocations = new TreeMap<Position, String>();
        regexMap = new HashMap<String, List<Position>>();
    }

    /**
     * Creates the position list for.
     *
     * @param regex The regular expression to handle.
     * @return See above
     */
    private List<Position> createPositionList(String regex)
    {
        List<Position> positionList;
        if (!regexMap.containsKey(regex))
        {
            positionList = new ArrayList<Position>();
            regexMap.put(regex, positionList);
        }
        else
            positionList = regexMap.get(regex);
        return positionList;
    }

    /**
     * Creates a new instance.
     * 
     * @param elem see above.
     * @param formatMap the map of regex and formatters to apply.
     * @param editorPane the component this resides in.
     */
    WikiView(Element elem, Map<String, FormatSelectionAction> formatMap,
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
            int p1) 
                    throws BadLocationException 
    {
        TextFormatter.removeHighlights(editorPane);
        Document doc = getDocument();
        String text = doc.getText(p0, p1 - p0);

        Segment segment = getLineBuffer();

        parse(text);

        int i = 0;
        Position p;

        Iterator<Position> positionIterator = tokenLocations.keySet().iterator();
        while (positionIterator.hasNext())
        {
            p = positionIterator.next();
            if (i < p.getStart()) 
            { 
                graphics.setColor(editorPane.getForeground());
                doc.getText(p0+i, p.getStart()-i, segment);
                x = Utilities.drawTabbedText(segment, x, y, graphics, this, i);
            }

            i = p.getEnd();
            doc.getText(p0+p.getStart(), i-p.getStart(), segment);
            String regex = tokenLocations.get(p);
            FormatSelectionAction fs = formatMap.get(regex);
            x = fs.getFormatter().formatText(editorPane, segment, x, y, 
                    graphics, this, i, p.getStart(), p.getEnd());
        }

        // 	Paint possible remaining text black
        if (i < text.length()) 
        {
            graphics.setColor(editorPane.getForeground());
            doc.getText(p0+i, text.length()-i, segment);
            x = Utilities.drawTabbedText(segment, x, y, graphics, this, i);
        }
        return x;
    }

    /**
     * Overridden to control if the passed object equals the current one.
     * @see javax.swing.text.PlainView#drawSelectedText(Graphics, int, int, 
     * int, int)
     */
    protected int drawSelectedText(Graphics g, int x, int y, int p0, int p1) 
            throws BadLocationException 
    {
        return drawUnselectedText(g, x, y, p0, p1);
    }

    /**
     * Returns the selection action for the token at index.
     *
     * @param index see above.
     * @return the selectionAction.
     */
    SelectionAction getSelectionAction(int index)
    {
        Document doc = getDocument();
        try
        {
            parse(doc.getText(0, doc.getLength()));
        }
        catch (BadLocationException e) {}
        Iterator<Position> 
        positionIterator = tokenLocations.keySet().iterator();
        Position p;
        String regex;
        FormatSelectionAction fsa;
        while (positionIterator.hasNext())
        {
            p = positionIterator.next();
            if (p.contains(index, index))
            {
                regex = tokenLocations.get(p);
                fsa = formatMap.get(regex);
                if (fsa == null) return null;
                return fsa.getSelectionAction();
            }
        }
        return null;
    }

    /**
     * Returns the selected text for the token at index.
     *
     * @param index see above.
     * @return the text.
     */
    String getSelectedText(int index)
    {
        Document doc = getDocument();
        Iterator<Position> 
        positionIterator = tokenLocations.keySet().iterator();
        Position p;
        while (positionIterator.hasNext())
        {
            p = positionIterator.next();
            if (p.contains(index, index))
            {
                try
                {
                    return doc.getText(p.getStart(), p.length());
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