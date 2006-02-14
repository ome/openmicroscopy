/*
 * org.openmicroscopy.shoola.agents.hiviewer.Colors
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

package org.openmicroscopy.shoola.agents.hiviewer;


//Java imports
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplay;
import org.openmicroscopy.shoola.env.config.Registry;


/** 
 * Collection of convenience methods.
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
public class Colors
{
    
    /** Identifies the color used to highlight the title bar. */
    public static final int     TITLE_BAR_HIGHLIGHT = 0;
    
    /** Identifies the color of the title bar. */
    public static final int     TITLE_BAR = 1;
    
    /**
     * Identifies the color used to highlight the title bar when the 
     * regular expression is found in the title and the node is selected.
     */
    public static final int     REGEX_TITLE_HIGHLIGHT = 2;
    
    /**
     * Identifies the color of the title bar when the 
     * regular expression is found in the title.
     */
    public static final int     REGEX_TITLE = 3;
    
    /**
     * Identifies the color used to highlight the title bar when the 
     * regular expression is found in the annotation and the node is selected.
     */
    public static final int     REGEX_ANNOTATION_HIGHLIGHT = 4;
    
    /**
     * Identifies the color of the title bar when the 
     * regular expression is found in the annotation.
     */
    public static final int     REGEX_ANNOTATION = 5;
    
    /**
     * Identifies the color used to highlight the title bar when the 
     * node is selected and annotated.
     */
    public static final int     ANNOTATED_HIGHLIGHT = 6;
    
    /** Identifies the color of the annotated elements. */
    public static final int     ANNOTATED = 7;
    
    /** 
     * Identifies the color of the elements where the regular expression is
     * found. 
     */
    public static final int     REGEX_TITLE_AND_ANNOTATION = 8;
    
    /** The sole instance. */
    private static Colors   singleton;
    
    /** The collection of colors. */
    private Map             colorsMap;
    
    /** Convenience reference. */
    private Registry        registry;
    
    /**
     * Creates a new instance and configures the parameters.
     * 
     * @param registry  Reference to the registry. Mustn't be <code>null</code>.
     */
    private Colors(Registry registry)
    {
        if (registry == null) throw new NullPointerException("No registry.");
        this.registry = registry;
        colorsMap = new HashMap();
        initializeColors();
    }
    
    /** Initializes the colorsMap. */
    private void initializeColors()
    {
        Color c;
        c = (Color) registry.lookup("/resources/colors/TitleBarHighlight");
        colorsMap.put(new Integer(TITLE_BAR_HIGHLIGHT), c);
        c = (Color) registry.lookup("/resources/colors/TitleBar");
        colorsMap.put(new Integer(TITLE_BAR), c);
        c = (Color) registry.lookup("/resources/colors/AnnotatedHighlight");
        colorsMap.put(new Integer(ANNOTATED_HIGHLIGHT), c);
        c = (Color) registry.lookup("/resources/colors/Annotated");
        colorsMap.put(new Integer(ANNOTATED), c);
        c = (Color) 
            registry.lookup("/resources/colors/RegExAnnotationHighlight");
        colorsMap.put(new Integer(REGEX_ANNOTATION_HIGHLIGHT), c);
        c = (Color) registry.lookup("/resources/colors/RegExAnnotation");
        colorsMap.put(new Integer(REGEX_ANNOTATION), c);
        c = (Color) registry.lookup("/resources/colors/RegExTitleHighlight");
        colorsMap.put(new Integer(REGEX_TITLE_HIGHLIGHT), c);
        c = (Color) registry.lookup("/resources/colors/RegExTitle");
        colorsMap.put(new Integer(REGEX_TITLE), c);
        c = (Color) registry.lookup(
                "/resources/colors/RegExTitleAndAnnotationHighlight");
        colorsMap.put(new Integer(REGEX_TITLE_AND_ANNOTATION), c);
    }
    
    /**
     * Returns an instance of this class.
     * 
     * @return See above.
     */
    public static Colors getInstance()
    { 
        if (singleton == null) 
            singleton = new Colors(HiViewerAgent.getRegistry());
        return singleton;
    }

    /**
     * Returns the color corresponding to the specified id.
     * 
     * @param id The passed color's id.
     * @return See above.
     */
    public Color getColor(int id)
    {
        if (id < 0 || colorsMap.size() <= id) {
            registry.getLogger().error(this, "color id out of range: "+id+".");
            return null;
        }
        return (Color) colorsMap.get(new Integer(id));
    }
    
    /**
     * Returns the color corresponding to the non selection of the 
     * specified node.
     * 
     * @param node The deselected node. 
     * @return See above,
     */
    public Color getDeselectedHighLight(ImageDisplay node)
    {
        if (node.getParentDisplay() == null) return getColor(TITLE_BAR);
        Color c = node.getHighlight();
        if (c == null) return null;
        if (c.equals(getColor(TITLE_BAR_HIGHLIGHT))) c = null;
        else if (c.equals(getColor(ANNOTATED_HIGHLIGHT))) 
            c = getColor(ANNOTATED);
        else if (c.equals(getColor(REGEX_TITLE_HIGHLIGHT))) 
            c = getColor(REGEX_TITLE);
        else if (c.equals(getColor(REGEX_ANNOTATION_HIGHLIGHT))) 
            c = getColor(REGEX_ANNOTATION);
        return c;
    }
    
    /**
     * Returns the color corresponding to the node selection.
     * 
     * @param node The selected node.
     * @return See above.
     */
    public Color getSelectedHighLight(ImageDisplay node)
    {
        if (node.getParentDisplay() == null) return getColor(TITLE_BAR);
        Color c = node.getHighlight();
        if (c == null) c = getColor(TITLE_BAR_HIGHLIGHT);
        else if (c.equals(getColor(ANNOTATED))) 
            c = getColor(ANNOTATED_HIGHLIGHT);
        else if (c.equals(getColor(REGEX_TITLE))) 
            c = getColor(REGEX_TITLE_HIGHLIGHT);
        else if (c.equals(getColor(REGEX_ANNOTATION))) 
            c = getColor(REGEX_ANNOTATION_HIGHLIGHT);
        return c;
    }
    
}
