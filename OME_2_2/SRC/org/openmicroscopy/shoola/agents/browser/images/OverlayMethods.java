/*
 * org.openmicroscopy.shoola.agents.browser.images.OverlayMethods
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

import org.openmicroscopy.shoola.agents.browser.datamodel.AttributeMap;

import edu.umd.cs.piccolo.util.PPaintContext;

/**
 * Collection of overlay methods to use in displaying overlays that respond
 * to some UI events.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class OverlayMethods
{
    public static final OverlayMethod ANNOTATION_METHOD =
        new AbstractOverlayMethod(OverlayNodeDictionary.ANNOTATION_NODE)
    {   
        public void display(Thumbnail t, PPaintContext context)
        {
            if(t == null) return;
            if(displayCondition(t,context))
            {
                if(!t.hasActiveOverlay(getDisplayNodeType()))
                {
                    ImageAnnotationOverlay node = new ImageAnnotationOverlay(t);
                    t.addActiveOverlay(node);
                }
            }
            else
            {
                if(t.hasActiveOverlay(getDisplayNodeType()))
                {
                    t.removeActiveOverlay(getDisplayNodeType());
                }
            }
        }
        
        public boolean displayCondition(Thumbnail t, PPaintContext context)
        {
            if(context.getCamera().getViewScale() < 0.5) return false;
            ThumbnailDataModel model = t.getModel();
            AttributeMap map = model.getAttributeMap();
            return (map.getAttribute("ImageAnnotation") != null);
        }
    };
}
