/*
* browser.thumnailview.model.ViewModel
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
package browser.thumnailview.model;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.media.opengl.GL;

import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureData;
import com.sun.opengl.util.texture.TextureIO;

//Java imports

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
public class ThumbnailModel 
{
	HashMap<Long, Rectangle2D> thumbnailPosition;
	HashMap<Long, Texture> thumbnailImage;
	int NUMCOLUMNS = 5;
	
	public ThumbnailModel()
	{
	}
	
	public void setThumbnails(Map<Long, byte[]> thumbnails, int width, 
												int height) throws IOException
	{
		thumbnailPosition = new HashMap<Long, Rectangle2D>();
		thumbnailImage = new HashMap<Long, Texture>();
		createThumbnailTexures(thumbnails);
		setThumbnailPositions(thumbnails, width, height);
	}
	
	private void setThumbnailPositions(Map<Long, byte[]> thumbnails, int width, int height)
	{
		Iterator<Long> thumbnailIterator = thumbnails.keySet().iterator();
		int count = 0;
		while(thumbnailIterator.hasNext())
		{
			Long id = thumbnailIterator.next();
			Rectangle2D rectangle = new Rectangle2D.Float((count%NUMCOLUMNS)*width, 
											  (count/NUMCOLUMNS)*height, 
											  width, height);
			thumbnailPosition.put(id, rectangle);
			count++;
		}
	}

	private void createThumbnailTexures(Map<Long, byte[]> thumbnails) 
															throws IOException
	{
		Iterator<Long> thumbnailIterator = thumbnails.keySet().iterator();
		while(thumbnailIterator.hasNext())
		{
			Long id = thumbnailIterator.next();
			Texture thumbnailTexture = createTextureFromBytes(thumbnails.get(id));
			thumbnailImage.put(id, thumbnailTexture);
		}
	}
	
	private Texture createTextureFromBytes(byte[] data) throws IOException
	{
		ByteArrayInputStream stream = new ByteArrayInputStream(data);
		TextureData textureData  = TextureIO.newTextureData(stream, false,
														TextureIO.JPG);
		Texture texture = TextureIO.newTexture(textureData);
		return texture;
	}
	
	public Map<Long, Rectangle2D> getThumbnailPosition()
	{
		return thumbnailPosition;
	}
	
	public Map<Long,Texture> getThumbnailTexture()
	{
		return thumbnailImage;
	}

	
}

