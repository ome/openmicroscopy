/*
 * org.openmicroscopy.shoola.agents.browser.images.Thumbnail
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
package org.openmicroscopy.shoola.agents.browser.images;

import java.util.Set;

import org.openmicroscopy.shoola.agents.browser.datamodel.ThumbnailDataModel;
import org.openmicroscopy.shoola.agents.browser.events.MEHChangeListener;
import org.openmicroscopy.shoola.agents.browser.events.ModularEventHandler;

import edu.umd.cs.piccolo.nodes.PImage;

/**
 * A view of a thumbnail/small image within the browser framework.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since 2.2
 */
public class Thumbnail extends PImage implements MEHChangeListener
{
    /**
     * The base model of the thumbnail.
     */
    protected ThumbnailDataModel model;

    /**
     * The current render method.
     */
    protected RenderMethod renderMethod;

    /**
     * Defines the set of paint methods in a self-organizing list
     * (impl: TreeSet)
     */
    protected Set paintMethods;

    /**
     * Defines the union of UI gestures for this thumbnail.
     */
    protected ModularEventHandler eventHandler;

    /**
     * Defines if this thumbnail is mip-mapped.
     */
    protected boolean usesImageSet;

    /**
     * Constructs a thumbnail around this model (no renderer specified yet)
     * @param tdm The data model.
     */
    public Thumbnail(ThumbnailDataModel tdm) // TODO: fix
    {
        this.model = tdm;
    }

    /**
     * Constructs a thumbnail around this model and specifies a first
     * renderer for it.
     */
    public Thumbnail(ThumbnailDataModel tdm, RenderMethod method)
    {
        this.model = tdm;
        this.renderMethod = method;
    }
    // TODO: complete this code skeleton

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.agents.browser.events.MEHChangeListener#eventListenerChanged(int)
     */
    public void eventListenerChanged(int changeType)
    {
        // TODO: rework event handler
    }

    /**
     * Returns the current render method for this thumbnail.
     * @return The current render method.
     */
    public RenderMethod getRenderMethod()
    {
        return renderMethod;
    }

    /**
     * Sets the current render method for this thumbnail.
     * @param method
     */
    public void setRenderMethod(RenderMethod method)
    {
        if (method != null)
        {
            this.renderMethod = method;
        }
    }

    /**
     * Gets the underlying model for the thumbnail.
     * 
     * @return This thumbnail's underlying model.
     */
    public ThumbnailDataModel getModel()
    {
        return model;
    }

    /**
     * Sets the underlying model for the thumbnail.
     * 
     * @param model This thumbnail's new data model.
     */
    public void setModel(ThumbnailDataModel model)
    {
        if (model == null)
        {
            this.model = model;
        }
    }

}
