/*
 * org.openmicroscopy.shoola.agents.zoombrowser.data.BrowserImageSummary
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

package org.openmicroscopy.shoola.agents.zoombrowser.data;

//Java imports
import java.awt.Image;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.zoombrowser.piccolo.Thumbnail;
import org.openmicroscopy.shoola.env.data.model.DataObject;
import org.openmicroscopy.shoola.env.data.model.ImageSummary;

/** 
 * An extension of 
 * {@link org.openmicroscopy.shoola.env.data.model.ImageSummary}, 
 * adding some state to track the thumbnails for an image
 * 
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 *
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class BrowserImageSummary  extends ImageSummary
{
	/** A hash for associating thumbnails with BrowserImageSummary objects */
	private static HashMap imageThumbnailMap = new HashMap();
	 
	/** The thumbnail for this image */
	private Image thumb;
	
	
	
	public BrowserImageSummary() {}
	
	
	/** Required by the DataObject interface. */
	public DataObject makeNew() { return new BrowserImageSummary(); }

	public void addThumbnail(Thumbnail thumb) {
		Vector thumbs;
		Integer id = new Integer(getID());
		Object obj = imageThumbnailMap.get(id);
		if (obj == null) {
			 thumbs = new Vector();
		}
		else
			thumbs = (Vector) obj;
		thumbs.add(thumb);
		imageThumbnailMap.put(id,thumbs);
	}
	
	//to prevent re-entrant loops
 	private boolean reentrant=false;
	
 	public void highlightThumbnails(boolean v) {
		
		 if (reentrant == true)
			 return;
		 // so each of the setHighlighted() calls don't lead to a call back here
		 reentrant = true;
			 Vector thumbs = (Vector) imageThumbnailMap.get(new Integer(getID()));
		 if (thumbs == null)
		 	return; // no matching things to higlight.
		 Iterator iter = thumbs.iterator();
		 
		 Thumbnail thumb;
		 while (iter.hasNext()) {
			thumb = (Thumbnail) iter.next();
			thumb.setHighlighted(v);
		 }
		 reentrant = false;
 	}
 	
 	public void setThumbnail(Image thumb) {
 		this.thumb =thumb;
 	}
 
 	public Image getThumbnail() {
 		return thumb;
 	}
}
