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
package omerojava.util;

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
import omero.client;
import omero.model.Dataset;
import omero.model.DatasetI;
import omero.model.DatasetImageLink;
import omero.model.Image;
import omero.model.Pixels;
import omero.model.PixelsI;
import omero.model.Project;
import omero.model.ProjectDatasetLink;
import omero.model.ProjectI;
import omero.model.SessionPrx;

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
	
	/**
	 * Convert the raw integer(4byte colour data) to a bufferedImage, for
	 * pixels object pixels.
	 * @param pixels see above.
	 * @param rawImage see above.
	 * @return
	 */
	static public BufferedImage toBufferedImage(Pixels pixels, int[] rawImage)
	{
		if (pixels == null)
			throw new NullPointerException("pixels is null");
		if (rawImage == null)
			throw new NullPointerException("rawImage is null");
		return ome.util.ImageUtil.createBufferedImage(rawImage, 
				pixels.getSizeX().getValue(), 
				pixels.getSizeX().getValue());
	}
	
	/**
	 * Extracts a 2D plane from the pixels set this object is working for.
	 * 
	 * @param pixels	The pixels object from which the rawPlane was retrieved.
	 * @param rawPlane	The raw bytes of the plane (z,c,t)
	 * @return A plane 2D object that encapsulates the actual plane pixels.
	 * @throws DSAccessException 
	 * @throws DSOutOfServiceException 
	 */
	static public Plane2D getPlane2D(Pixels pixels, byte[] rawPlane)
		throws omero.ServerError
	{
		if (pixels == null)
			throw new NullPointerException("pixels is null");
		if (rawPlane == null)
			throw new NullPointerException("rawPlane is null");
		if (pixels.getPixelsType().getValue().getValue() == null)
			throw new NullPointerException("pixels.getPixelsType().getValue() is null");
		String type = pixels.getPixelsType().getValue().getValue();
		int bytesPerPixels = getBytesPerPixels(type);
		BytesConverter strategy = BytesConverter.getConverter(type);
		return createPlane2D(pixels, rawPlane, bytesPerPixels, strategy);
	}
	
	
	/**
	 * Extracts a 1D plane from the pixels set this object is working for.
	 * 
	 * @param pixels	The pixels object from which the rawPlane was retrieved.
	 * @param rawPlane	The raw bytes of the plane (z,c,t)
	 * @return A plane 1D object that encapsulates the actual plane pixels.
	 * @throws DSAccessException 
	 * @throws DSOutOfServiceException 
	 */
	static public Plane1D getPlane1D(Pixels pixels, byte[] rawPlane)
		throws omero.ServerError
	{
		if (pixels == null)
			throw new NullPointerException("pixels is null");
		if (rawPlane == null)
			throw new NullPointerException("rawPlane is null");
		if (pixels.getPixelsType().getValue().getValue() == null)
			throw new NullPointerException("pixels.getPixelsType().getValue() is null");
		String type = pixels.getPixelsType().getValue().getValue();
		int bytesPerPixels = getBytesPerPixels(type);
		BytesConverter strategy = BytesConverter.getConverter(type);
		return createPlane1D(pixels, rawPlane, bytesPerPixels, strategy);
	}
	
	/**
	 * Get the datasets from the project, the project must have the datasets
	 * loaded already.
	 * This method makes no calls to the server.
	 * @param project
	 * @return see above.
	 * @throws ServerError
	 */
	static public List<Dataset> getDatasetsFromProject(Project project)
			throws ServerError
	{
		if (project == null)
			throw new NullPointerException("project is null");
		if (!project.isLoaded())
			throw new IllegalArgumentException("project not loaded.");
		List<Dataset> datasets = new ArrayList<Dataset>();
		Iterator<ProjectDatasetLink> iterator = ((ProjectI)project).iterateDatasetLinks();
		while (iterator.hasNext())
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
		if (project == null)
			throw new NullPointerException("project is null");
		if (!project.isLoaded())
			throw new IllegalArgumentException("project not loaded.");
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
		for (Image image : images)
			for (Pixels pixels : image.copyPixels())
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
		for (Image image : images)
			for (Pixels pixels : image.copyPixels())
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
		if (dataset == null)
			throw new NullPointerException("dataset is null");
		if (!dataset.isLoaded())
			throw new IllegalArgumentException("dataset not loaded.");
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
		if (dataset == null)
			throw new NullPointerException("dataset is null");
		if (!dataset.isLoaded())
			throw new IllegalArgumentException("dataset not loaded.");
		List<Image> images = new ArrayList<Image>();
		Iterator<DatasetImageLink> iterator = ((DatasetI)dataset).iterateImageLinks(); 
		while (iterator.hasNext())
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
		if (project == null)
			throw new NullPointerException("project is null");
		if (!project.isLoaded())
			throw new IllegalArgumentException("project not loaded.");
		List<Image> images = new ArrayList<Image>();
		List<Dataset> datasets = getDatasetsFromProject(project);
		for (Dataset dataset : datasets)
		{
			List<Image> datasetImages = getImagesFromDataset(dataset);
			for (Image image : datasetImages)
				images.add(image);
		}
		return images;
	}
	
	/**
	 * Returns the number of bytes per pixel depending on the pixel type.
	 * 
	 * @param pixelsType The pixels Type.
	 * @return See above.
	 */
	static public int getBytesPerPixels(String pixelsType)
	{
		if (!PixelTypes.pixelMap.containsKey(pixelsType))
			throw new IllegalArgumentException(pixelsType + " is not a valid PixelsType.");
		return PixelTypes.pixelMap.get(pixelsType);
	}
	
	/**
	 * Convert the rawPlane data to a Plane2D object which can then convert 
	 * that raw byte data to anytype the caller wants.
	 *
	 * @param pixels	The pixels object representing the raw data of the plane 
	 * @param rawPlane	The raw bytes of the plane (z,c,t)
	 * @param bytesPerPixel The number of bytes per pixel.
	 * @param strategy	To transform bytes into pixels values.
	 * @return A plane 2D object that encapsulates the actual plane pixels.
	 * @throws DSAccessException 
	 * @throws DSOutOfServiceException 
	 * @throws DataSourceException If an error occurs while retrieving the
	 *                              plane data from the pixels source.
	 */
	static private Plane2D createPlane2D(Pixels pixels, byte[] rawPlane,
						int bytesPerPixels, BytesConverter strategy) 
		throws omero.ServerError
	{
		ReadOnlyByteArray array = new ReadOnlyByteArray(rawPlane, 0, rawPlane.length);
		return new Plane2D(array, pixels.getSizeX().getValue(), 
							pixels.getSizeY().getValue(), bytesPerPixels, 
							strategy);
	}
	
	/**
	 * Convert the rawPlane data to a Plane1D object which can then convert 
	 * that raw byte data to anytype the caller wants.
	 *
	 * @param pixels	The pixels object representing the raw data of the plane 
	 * @param rawPlane	The raw bytes of the plane (z,c,t)
	 * @param bytesPerPixel The number of bytes per pixel.
	 * @param strategy	To transform bytes into pixels values.
	 * @return A plane 2D object that encapsulates the actual plane pixels.
	 * @throws DSAccessException 
	 * @throws DSOutOfServiceException 
	 * @throws DataSourceException If an error occurs while retrieving the
	 *                              plane data from the pixels source.
	 */
	static private Plane1D createPlane1D(Pixels pixels, byte[] rawPlane,
						int bytesPerPixels, BytesConverter strategy) 
		throws omero.ServerError
	{
		ReadOnlyByteArray array = new ReadOnlyByteArray(rawPlane, 0, rawPlane.length);
		return new Plane1D(array, pixels.getSizeX().getValue(), 
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
		if (pixels == null)
			throw new NullPointerException("pixels is null");
		int sizex = pixels.getSizeX().getValue();
		int sizey = pixels.getSizeY().getValue();
		if (data == null)
			throw new NullPointerException("data is null");
		if (pixels.getPixelsType().getValue().getValue() == null)
			throw new NullPointerException("pixels.getPixelsType() is null");
		if(data.length*data[0].length!=sizex*sizey)
			throw new IllegalArgumentException("data[][] does not match pixels.getSizeX()*pixels.getSizeY()");
		String pixelsType  = pixels.getPixelsType().getValue().getValue();
		int pixelsSize = getPixelsSize(pixelsType); 
			byte[] rawbytes =  new byte[sizex*sizey*pixelsSize];
		for ( int x = 0 ; x < sizex ; x++)
			for ( int y = 0 ; y < sizey ; y++)
			{
				int offset = calcOffset(pixelsSize, sizex, x, y);
				byte[] newBytes = convertValue(pixelsType, data[x][y]);
				for (int offsetLength = 0 ; offsetLength < newBytes.length ; offsetLength++)
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
		if (pixelsType.equals(PixelTypes.INT_8) || pixelsType.equals(PixelTypes.UINT_8))
			return mapToByteArray(val.byteValue());
		else if (pixelsType.equals(PixelTypes.INT_16) || pixelsType.equals(PixelTypes.UINT_16))
			return mapToByteArray(val.shortValue());
		else if (pixelsType.equals(PixelTypes.INT_32) || pixelsType.equals(PixelTypes.UINT_32))
			return mapToByteArray(val.intValue());
		else if (pixelsType.equals(PixelTypes.FLOAT))
			return mapToByteArray(val.floatValue());
		else
			return mapToByteArray(val.doubleValue());
	}

	
}

