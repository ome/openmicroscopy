/*
* ome.services.blitz.gateway.services.util.GatewayUtils
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
package ome.services.blitz.gateway.services.util;

//Java imports
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

//Third-party libraries

//Application-internal dependencies
import omero.ServerError;
import omero.model.Dataset;
import omero.model.DatasetI;
import omero.model.DatasetImageLink;
import omero.model.Image;
import omero.model.Pixels;
import omero.model.PixelsI;
import omero.model.Project;
import omero.model.ProjectDatasetLink;
import omero.model.ProjectI;

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
public class GatewayUtils 
{
	/** Identifies the type used to store pixel values. */
	static final String INT_8 = "int8";

	/** Identifies the type used to store pixel values. */
	static final String UINT_8 = "uint8";

	/** Identifies the type used to store pixel values. */
	static final String INT_16 = "int16";

	/** Identifies the type used to store pixel values. */
	static final String UINT_16 = "uint16";

	/** Identifies the type used to store pixel values. */
	static final String INT_32 = "int32";

	/** Identifies the type used to store pixel values. */
	static final String UINT_32 = "uint32";

	/** Identifies the type used to store pixel values. */
	static final String FLOAT = "float";

	/** Identifies the type used to store pixel values. */
	static final String DOUBLE = "double";
	
	/**
	 * Convert the raw integer(4byte colour data) to a bufferedImage, for
	 * pixels object pixels.
	 * @param pixels see above.
	 * @param rawImage see above.
	 * @return
	 */
	static public BufferedImage toBufferedImage(PixelsI pixels, int[] rawImage)
	{
		return ome.util.ImageUtil.createBufferedImage(rawImage, 
				pixels.getSizeX().getValue(), 
				pixels.getSizeX().getValue());
	}
	
	/**
	 * Extracts a 2D plane from the pixels set this object is working for.
	 * 
	 * @param z			The z-section at which data is to be fetched.
	 * @param t			The timepoint at which data is to be fetched.
	 * @param w			The wavelength at which data is to be fetched.
	 * @return A plane 2D object that encapsulates the actual plane pixels.
	 * @throws DSAccessException 
	 * @throws DSOutOfServiceException 
	 */
	static public Plane2D getPlane2D(PixelsI pixels, byte[] rawPlane)
		throws omero.ServerError
	{
		String type = pixels.getPixelsType().getValue().getValue();
		int bytesPerPixels = getBytesPerPixels(type);
		BytesConverter strategy = BytesConverter.getConverter(type);
		return createPlane(pixels, rawPlane, bytesPerPixels, strategy);
	}
	
	/**
	 * Get the datasets from the project, the project must have the datasets
	 * loaded already.
	 * This method makes no calls to the server.
	 * @param project
	 * @return
	 * @throws ServerError
	 */
	static public List<Dataset> getDatasetsFromProject(Project project)
			throws ServerError
	{
		List<Dataset> datasets = new ArrayList<Dataset>();
		Iterator<ProjectDatasetLink> iterator = ((ProjectI)project).iterateDatasetLinks();
		while(iterator.hasNext())
			datasets.add(iterator.next().getChild());
		return datasets;
	}
	
	/**
	 * Get the pixels from the Project, the pixels must be loaded in the project
	 * beforehand.
	 * This method makes no calls to the server.
	 * @param project see above.
	 * @return see above.
	 */
	static public List<Pixels> getPixelsFromProject(Project project)
			throws ServerError
	{
		List<Image> images = getImagesFromProject(project);
		return getPixelsFromImageList(images);
	}
	
	/**
	 * Get the pixels from the list of images, these images must have the pixels
	 * loaded beforehand.
	 * This method makes no calls to the server.
	 * @param images see above.
	 * @return see above.
	 */
	static public List<Pixels> getPixelsFromImageList(List<Image> images)
	{
		List<Pixels> pixelsList = new ArrayList<Pixels>();
		for(Image image : images)
			for(Pixels pixels : image.copyPixels())
				pixelsList.add(pixels);
		return pixelsList;
	}
	
	/**
	 * create a map of pixelsId to pixels from the images in the image list.
	 * This method makes no calls to the server and relies on the images having 
	 * the pixels loaded beforehand.
	 * @param images see above.
	 * @return see above.
	 */
	static public Map<Long, Pixels> getPixelsImageMap(List<Image> images)
	{
		Map<Long, Pixels> pixelsList = new TreeMap<Long, Pixels>();
		for(Image image : images)
			for(Pixels pixels : image.copyPixels())
				pixelsList.put(image.getId().getValue(), pixels);
		return pixelsList;
	}

	/**
	 * Get the pixels from the Dataset, the pixels must be loaded in the Dataset
	 * beforehand.
	 * This method makes no calls to the server.
	 * @param dataset see above.
	 * @return see above.
	 */
	static public List<Pixels> getPixelsFromDataset(Dataset dataset)
			throws ServerError
	{
		List<Image> images = getImagesFromDataset(dataset);
		return getPixelsFromImageList(images);
	}
	
	/**
	 * Get the images from the Dataset, the Dataset must be loaded in the project
	 * beforehand.
	 * This method makes no calls to the server.
	 * @param dataset see above.
	 * @return see above.
	 */
	static public List<Image> getImagesFromDataset(Dataset dataset) throws ServerError
	{
		List<Image> images = new ArrayList<Image>();
		Iterator<DatasetImageLink> iterator = ((DatasetI)dataset).iterateImageLinks(); 
		while(iterator.hasNext())
		    images.add(iterator.next().getChild());
		return images;
	}

	/**
	 * Get the images from the Project, the images must be loaded in the project
	 * beforehand.
	 * This method makes no calls to the server.
	 * @param project see above.
	 * @return see above.
	 */
	static public List<Image> getImagesFromProject(Project project) throws ServerError
	{
		List<Image> images = new ArrayList<Image>();
		List<Dataset> datasets = getDatasetsFromProject(project);
		for(Dataset dataset : datasets)
		{
			List<Image> datasetImages = getImagesFromDataset(dataset);
			for(Image image : datasetImages)
				images.add(image);
		}
		return images;
	}
	
	/**
	 * Returns the number of bytes per pixel depending on the pixel type.
	 * 
	 * @param v The pixels Type.
	 * @return See above.
	 */
	static private int getBytesPerPixels(String v)
	{
		if (INT_8.equals(v) || UINT_8.equals(v)) return 1;
		if (INT_16.equals(v) || UINT_16.equals(v)) return 2;
		if (INT_32.equals(v) || UINT_32.equals(v) || FLOAT.equals(v)) 
			return 4;
		if (DOUBLE.equals(v)) return 8;
		return -1;
	}
	
	/**
	 * Factory method to fetch plane data and create an object to access it.
	 * 
	 * @param z			The z-section at which data is to be fetched.
	 * @param t			The timepoint at which data is to be fetched.
	 * @param w			The wavelength at which data is to be fetched.
	 * @param strategy	To transform bytes into pixels values.
	 * @return A plane 2D object that encapsulates the actual plane pixels.
	 * @throws DSAccessException 
	 * @throws DSOutOfServiceException 
	 * @throws DataSourceException If an error occurs while retrieving the
	 *                              plane data from the pixels source.
	 */
	static private Plane2D createPlane(PixelsI pixels, byte[] rawPlane,
						int bytesPerPixels, BytesConverter strategy) 
		throws omero.ServerError
	{
		ReadOnlyByteArray array = new ReadOnlyByteArray(rawPlane, 0, rawPlane.length);
		return new Plane2D(array, pixels.getSizeX().getValue(), 
							pixels.getSizeY().getValue(), bytesPerPixels, 
							strategy);
	}
	/**
	 * convert the client data pixels to server byte array, also sets the data
	 * pixel size to the size of the pixels in the pixels Id param.
	 * @param pixels the pixels in the server.
	 * @param data the data on the client. 
	 * @return the bytes for server.
	 */
	public static byte[] convertClientToServer(Pixels pixels, double [][] data)
	{
		String pixelsType  = pixels.getPixelsType().getValue().getValue();
		int pixelsSize = getPixelsSize(pixelsType); 
		int sizex = pixels.getSizeX().getValue();
		int sizey = pixels.getSizeY().getValue();
		byte[] rawbytes =  new byte[sizex*sizey*pixelsSize];
		for ( int x = 0 ; x < sizex ; x++)
			for ( int y = 0 ; y < sizey ; y++)
			{
				int offset = calcOffset(pixelsSize, sizex, x, y);
				byte[] newBytes = convertValue(pixelsType, data[x][y]);
				for( int offsetLength = 0 ; offsetLength < newBytes.length ; offsetLength++)
					rawbytes[offset+offsetLength] = newBytes[offsetLength];  
			}
		return rawbytes;
	}
	
	/** 
	 * Determines the offset value.
	 * @param pixelSize pixels size in bytes. 
	 * @param x	The x-coordinate.
	 * @param y	The y-coordinate.
	 * @return See above.
	 */
	static private int calcOffset(int pixelSize, int sizex, int x, int y)
	{
		return pixelSize*(y*sizex+x);
	}
	
	/**
	 * Get the pixel size of the pixels.
	 * @param pixelsType see above.
	 * @return the size in bytes. 
	 */
	static private int getPixelsSize(String pixelsType)
	{	
		return PixelTypes.pixelMap.get(pixelsType);
	}


	/**
	 * Map the byte data to a byte value 
	 * @param v see above.
	 * @return see above.
	 */
	static private byte[] mapToByteArray(byte v) 
	{
		ByteBuffer bb = ByteBuffer.allocate(1);
		bb.put(v);
		return bb.array();
	}

	/**
	 * Map the byte data to a byte value 
	 * @param v see above.
	 * @return see above.
	 */
	static private byte[] mapToByteArray(short v) 
	{
		ByteBuffer bb = ByteBuffer.allocate(2);
		bb.putShort(v);
		return bb.array();
	}

	/**
	 * Map the byte data to a byte value 
	 * @param v see above.
	 * @return see above.
	 */
	static private byte[] mapToByteArray(int v) 
	{
		ByteBuffer bb = ByteBuffer.allocate(4);
		bb.putInt(v);
		return bb.array();
	}

	/**
	 * Map the byte data to a byte value 
	 * @param v see above.
	 * @return see above.
	 */
	static private byte[] mapToByteArray(float v) 
	{
		ByteBuffer bb = ByteBuffer.allocate(4);
		bb.putFloat(v);
		return bb.array();
	}
	
	/**
	 * Map the byte data to a byte value 
	 * @param v see above.
	 * @return see above.
	 */
	static private byte[] mapToByteArray(double v) 
	{
		ByteBuffer bb = ByteBuffer.allocate(8);
		bb.putDouble(v);
		return bb.array();
	}

	/**
	 * Convert the value of the pixel in the client to the server byte value.
	 * @param pixelsType PixelType. 
	 * @param val the value to convert.
	 * @return the converted value.
	 */
	static private byte[] convertValue(String pixelsType, Double val)
	{
		if(pixelsType.equals(PixelTypes.INT_8) || pixelsType.equals(PixelTypes.UINT_8))
			return mapToByteArray(val.byteValue());
		else if(pixelsType.equals(PixelTypes.INT_16) || pixelsType.equals(PixelTypes.UINT_16))
			return mapToByteArray(val.shortValue());
		else if(pixelsType.equals(PixelTypes.INT_32) || pixelsType.equals(PixelTypes.UINT_32))
			return mapToByteArray(val.intValue());
		else if(pixelsType.equals(PixelTypes.FLOAT))
			return mapToByteArray(val.floatValue());
		else
			return mapToByteArray(val.doubleValue());
	}

	
}

