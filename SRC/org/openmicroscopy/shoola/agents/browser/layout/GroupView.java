/*
 * org.openmicroscopy.shoola.agents.browser.layout.GroupView
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

/*------------------------------------------------------------------------------
 *
 * Written by:    Jeff Mellen <jeffm@alum.mit.edu>
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.browser.layout;

import java.awt.Color;
import java.awt.Font;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;

import org.openmicroscopy.shoola.agents.browser.UIConstants;

/**
 * The visual representation of a group.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since 2.2
 */
public class GroupView
{
    /**
     * The layout method for the group.
     */
    protected LayoutMethod layoutMethod;

    /**
     * The shape bounds of the group (0,0 is always the top-left; this is
     * relative; a top-level browser canvas will control the real position of
     * the group)
     */
    protected Shape bounds;

    /**
     * The background color of the group.
     */
    protected Color backgroundColor;

    /**
     * The font of the group name.
     */
    protected Font nameFont;

    /**
     * The color of the group font/foreground information.
     */
    protected Color foregroundColor;

    /**
     * The backing model of the group view.
     */
    protected GroupModel model;

    /**
     * Constructs a group model with the default color scheme and layout method.
     * Bounds will be interpolated according to the default layout method.
     * 
     * @param model The model to base the group view on.
     */
    public GroupView(GroupModel model)
    {
        if (model != null)
        {
            this.model = model;
        }
        // TODO: specify default layout method, create bounds shape
        backgroundColor = UIConstants.BUI_GROUPAREA_COLOR;
        foregroundColor = UIConstants.BUI_GROUPTEXT_COLOR;
        nameFont = UIConstants.BUI_GROUPTEXT_FONT;
    }

    /**
     * Constructs a group model with the default color scheme and specified
     * layout method.  Bounds will be interpolated according to the default
     * layout method.
     * 
     * @param model The model to base the group view on.
     * @param method The layout method to organize the thumbnails by (in the
     *               group).
     */
    public GroupView(GroupModel model, LayoutMethod method)
    {
        if (model != null)
        {
            this.model = model;
        }

        if (method != null)
        {
            this.layoutMethod = method;
        }
        else
        {
            // TODO: specify default layout method, create bounds shape
        }

        backgroundColor = UIConstants.BUI_GROUPAREA_COLOR;
        foregroundColor = UIConstants.BUI_GROUPTEXT_COLOR;
        nameFont = UIConstants.BUI_GROUPTEXT_FONT;
    }

    /**
     * Constructs a group view with the specified backing model, layout method,
     * and maximum bounds constraints.
     * 
     * @param model The group model to base the view on.
     * @param method The layout method to order the thumbnails by.
     * @param bounds The maximum bounds of the group.
     */
    public GroupView(GroupModel model, LayoutMethod method, Shape bounds)
    {
        if (model != null)
        {
            this.model = model;
        }

        if (method != null)
        {
            this.layoutMethod = method;
        }
        else
        {
            // TODO: specify default layout method
        }

        if (bounds != null)
        {
            // TODO: set bounds
        }
        else
        {
            // TODO: interpolate based on layout method
        }
    }

    /**
     * Returns the backing group model.
     * @return The backing group model.
     */
    public GroupModel getModel()
    {
        return this.model;
    }

    /**
     * Returns the current layout method.
     * @return The layout method.
     */
    public LayoutMethod getLayoutMethod()
    {
        return this.layoutMethod;
    }

    /**
     * Sets the layout method to the specified value, unless null.
     * @param method The method to use to layout this group's thumbnails.
     */
    public void setLayoutMethod(LayoutMethod method)
    {
        if (method != null)
        {
            this.layoutMethod = method;
        }
    }

    /**
     * Gets the (relative) shape of the area.
     * @return The bounds of the area.
     */
    public Shape getBounds()
    {
        // create a copy.
        AffineTransform dummyTransform = new AffineTransform();
        return dummyTransform.createTransformedShape(bounds);
    }

    /**
     * Sets the (relative) shape of the area to the specified shape.  Will do
     * nothing if the shape is null or is an instance of a line.
     */
    public void setBounds(Shape s)
    {
        if (s == null || s instanceof Line2D)
        {
            return;
        }
        AffineTransform dummyTransform = new AffineTransform();
        bounds = dummyTransform.createTransformedShape(s);

        // do recalculation of layout here?
    }

    /**
     * Returns the background color.
     * 
     * @return The background color.
     */
    public Color getBackgroundColor()
    {
        return backgroundColor;
    }

    /**
     * Returns the foreground color.
     * 
     * @return The foreground color.
     */
    public Color getForegroundColor()
    {
        return foregroundColor;
    }

    /**
     * Returns the font of the group name.
     * 
     * @return The group name font.
     */
    public Font getNameFont()
    {
        return nameFont;
    }

    /**
     * Sets the background color to the specified value.
     * 
     * @param color The color value to change the background to (does nothing
     *              if null)
     */
    public void setBackgroundColor(Color color)
    {
        if (color != null)
        {
            backgroundColor = color;
        }
    }

    /**
     * Sets the text/foreground color to the specified value.
     * 
     * @param color The color value to change the foreground to (does nothing
     *              if null)
     */
    public void setForegroundColor(Color color)
    {
        if (color != null)
        {
            foregroundColor = color;
        }
    }

    /**
     * Sets the font to the specified value.
     * 
     * @param font The font to display the group name, unless null.
     */
    public void setNameFont(Font font)
    {
        if (font != null)
        {
            nameFont = font;
        }
    }

}
