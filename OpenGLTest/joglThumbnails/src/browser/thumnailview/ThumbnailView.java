/*
* browser.ThumbnailView
*
 *------------------------------------------------------------------------------
*  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
package browser.thumnailview;


//Java imports
import java.io.IOException;
import java.util.Map;

import javax.swing.JPanel;

import browser.thumnailview.model.ThumbnailModel;


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
 * @since 3.0-Beta4
 */
public class ThumbnailView
	extends JPanel
{
	ThumbnailCanvas canvas;
	ThumbnailModel	thumbnailModel;
	
	public ThumbnailView()
	{
		thumbnailModel = new ThumbnailModel();
		buildUI();
	}
	
	public JPanel getView()
	{
		return this;
	}
	
	void buildUI()
	{
		canvas = new ThumbnailCanvas(thumbnailModel);
		add(canvas);
	}
	
	public void setThumbnails(Map<Long, byte[]> thumbnails, int width,
											int height) throws IOException
	{
		thumbnailModel.setThumbnails(thumbnails, width, height);
	}
	
	
}

