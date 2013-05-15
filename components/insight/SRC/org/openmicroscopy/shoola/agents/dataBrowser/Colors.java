/*
 * org.openmicroscopy.shoola.agents.dataBrowser.Colors 
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
package org.openmicroscopy.shoola.agents.dataBrowser;



//Java imports
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageDisplay;
import org.openmicroscopy.shoola.env.config.Registry;

/** 
 * Collection of convenience methods to determine the color of the
 * nodes depending on its status.
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
public class Colors
{

	/** Identifies the color used to highlight the title bar. */
    public static final int TITLE_BAR_HIGHLIGHT = 0;
    
    /** Identifies the color of the title bar. */
    public static final int TITLE_BAR = 1;
    
    /** Identifies the color used to highlight an unmodified title bar. */
    private static final int TITLE_BAR_UNMODIFIED = 2;
    
    /** Identifies the color of the title bar of the primary node. */
    private static final int TITLE_BAR_PRIMARY = 3;
    
    /** Identifies the color used to highlight an unmodified title bar. */
    private static final int TITLE_BAR_HIGHLIGHT_PRIMARY = 4;

    /** Identifies the secondary title bar highlight color. */
    public static final int TITLE_BAR_HIGHLIGHT_SECONDARY = 5;
    
    /** The default color the title bar. */
    private static final Color COLOR_TITLE_BAR = new Color(189, 210, 230);

    /** The default color the title bar. */
    private static final Color COLOR_TITLE_BAR_HIGHLIGHT_BAR =
    	new Color(58, 116, 215);
    
    /** The sole instance. */
    private static Colors   	singleton;
    
    /** The collection of colors. */
    private Map<Integer, Color>	colorsMap;
    
    /** Convenience reference. */
    private Registry        	registry;
    
    /**
     * Creates a new instance and configures the parameters.
     * 
     * @param registry  Reference to the registry. Mustn't be <code>null</code>.
     */
    private Colors(Registry registry)
    {
        if (registry == null) throw new NullPointerException("No registry.");
        this.registry = registry;
        colorsMap = new HashMap<Integer, Color>();
        initializeColors();
    }
    
    /** Initializes the colorsMap. */
    private void initializeColors()
    {
        Color c;
        c = (Color) registry.lookup("/resources/colors/TitleBarHighlight");
        if (c == null) c = COLOR_TITLE_BAR_HIGHLIGHT_BAR;
        colorsMap.put(TITLE_BAR_HIGHLIGHT, c);
        colorsMap.put(TITLE_BAR_HIGHLIGHT_PRIMARY, c.darker().darker());
        c = (Color) registry.lookup("/resources/colors/TitleBar");
        if (c == null) c = COLOR_TITLE_BAR;
        colorsMap.put(TITLE_BAR, c);
        colorsMap.put(TITLE_BAR_PRIMARY, c.darker().darker());
        c = (Color) registry.lookup("/resources/colors/TitleBarUnmodified");
        colorsMap.put(TITLE_BAR_UNMODIFIED, c);
        c = ((Color) registry.lookup("/resources/colors/TitleBarHighlightSecondary"));
        if (c == null) {
            c = COLOR_TITLE_BAR_HIGHLIGHT_BAR;
        }
        colorsMap.put(TITLE_BAR_HIGHLIGHT_SECONDARY, c);
    }
    
    /**
     * Returns an instance of this class.
     * 
     * @return See above.
     */
    public static Colors getInstance()
    { 
        if (singleton == null) 
            singleton = new Colors(DataBrowserAgent.getRegistry());
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
        return colorsMap.get(id);
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
    	if (node == null) return getColor(TITLE_BAR); 
        if (node.getParentDisplay() == null) return getColor(TITLE_BAR);
        Color c = node.getHighlight();
        if (c == null) return null;
        if (c.equals(getColor(TITLE_BAR_HIGHLIGHT)) ||
        	c.equals(getColor(TITLE_BAR_UNMODIFIED)) ||
        	c.equals(getColor(TITLE_BAR_HIGHLIGHT_PRIMARY))) c = null;	
        //else c = c.brighter();
        return c;
    }
    
    /**
     * Returns the color corresponding to the node selection.
     * 
     * @param node 		The selected node.
     * @param primary 	Pass <code>true</code> if the node is the first node
     * 					selected, <code>false</code> otherwise.
     * @return See above.
     */
    public Color getSelectedHighLight(ImageDisplay node, boolean primary)
    {
    	if (node == null) return getColor(TITLE_BAR); 
        if (node.getParentDisplay() == null) return getColor(TITLE_BAR);
        Color c = node.getHighlight();
        if (c == null || c.equals(getColor(TITLE_BAR_UNMODIFIED)) ||
        		c.equals(getColor(TITLE_BAR_HIGHLIGHT_PRIMARY)) ||
        		c.equals(getColor(TITLE_BAR_HIGHLIGHT))) {
        	if (primary) c = getColor(TITLE_BAR_HIGHLIGHT_PRIMARY);
        	else c = getColor(TITLE_BAR_HIGHLIGHT);
        }
        return c;
    }

    /**
     * Returns the color corresponding to the node selection.
     * 
     * @param node The selected node.
     * @return See above.
     */
    public Color getUnmodifiedHighLight(ImageDisplay node)
    {
    	if (node == null) return getColor(TITLE_BAR); 
        if (node.getParentDisplay() == null) return getColor(TITLE_BAR);
        return getColor(TITLE_BAR_UNMODIFIED);
    }
    
}
