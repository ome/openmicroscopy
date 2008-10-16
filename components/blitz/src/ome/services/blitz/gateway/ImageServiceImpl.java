/*
 * blitzgateway.service.ImageService 
 *
  *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
package ome.services.blitz.gateway;

import java.awt.Color;
import omero.api.BufferedImage;
import omero.api.IPixelsPrx;
import omero.api.IQueryPrx;
import omero.api.IUpdatePrx;

import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

import omero.RInt;
import omero.model.Image;
import omero.model.Pixels;
import omero.model.PixelsType;

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
 * @since OME3.0
 */
class ImageServiceImpl
	implements ImageService
{	
	
	IQueryPrx iQuery;
	RawPixelsStoreService pixelsStore;
	IPixelsPrx iPixels;
	IUpdatePrx iUpdate;
	RenderingService renderingService;
	ThumbnailService thumbnailService;
	
	/**
	 * Create the ImageService passing the gateway.
	 * @param gateway
	 */
	ImageServiceImpl(RawPixelsStoreService 	pixelsStore,
					RenderingService renderingService,
					ThumbnailService thumbnailService,
					IPixelsPrx iPixels, 
					IQueryPrx iQuery, IUpdatePrx iUpdate) 
	{
		this.iUpdate = iUpdate;
		this.iQuery = iQuery;
		this.iPixels = iPixels;
		this.pixelsStore = pixelsStore;
		this.renderingService = renderingService;
		this.thumbnailService = thumbnailService;
	}

	/* (non-Javadoc)
	 * @see omerojava.service.ImageService#closeRenderingService(long)
	 */
	public void closeRenderingService(long pixelsId) throws omero.ServerError
	{
		renderingService.closeGateway(pixelsId);
	}

	/* (non-Javadoc)
	 * @see omerojava.service.ImageService#closeRawPixelsService(long)
	 */
	public void closeRawPixelsService(long pixelsId) throws omero.ServerError
	{
		pixelsStore.closeGateway(pixelsId);
	}

	/* (non-Javadoc)
	 * @see omerojava.service.ImageService#closeThumbnailService(long)
	 */
	public void closeThumbnailService(long pixelsId) throws omero.ServerError
	{
		thumbnailService.closeGateway(pixelsId);
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.ImageService#getRawPlane(long, int, int, int)
	 */
	public double[][] getPlaneAsDouble(long pixelsId, int z, int c, int t) 
		throws omero.ServerError
	{
		Pixels pixels = getPixels(pixelsId);
		return convertRawPlaneAsDouble(pixels, z, c, t);
	}
	
	/* (non-Javadoc)
	 * @see blitzgateway.service.ImageService#getRawPlane(long, int, int, int)
	 */
	public long[][] getPlaneAsLong(long pixelsId, int z, int c, int t) 
		throws omero.ServerError
	{
		Pixels pixels = getPixels(pixelsId);
		return convertRawPlaneAsLong(pixels, z, c, t);
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.ImageService#getRawPlane(long, int, int, int)
	 */
	public int[][] getPlaneAsInt(long pixelsId, int z, int c, int t) 
		throws omero.ServerError
	{
		Pixels pixels = getPixels(pixelsId);
		return convertRawPlaneAsInt(pixels, z, c, t);
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.ImageService#getRawPlane(long, int, int, int)
	 */
	public short[][] getPlaneAsShort(long pixelsId, int z, int c, int t) 
		throws omero.ServerError
	{
		Pixels pixels = getPixels(pixelsId);
		return convertRawPlaneAsShort(pixels, z, c, t);
	}
	
	/* (non-Javadoc)
	 * @see blitzgateway.service.ImageService#getRawPlane(long, int, int, int)
	 */
	public byte[][] getPlaneAsByte(long pixelsId, int z, int c, int t) 
		throws omero.ServerError
	{
		Pixels pixels = getPixels(pixelsId);
		return convertRawPlaneAsByte(pixels, z, c, t);
	}

	
	/* (non-Javadoc)
	 * @see blitzgateway.service.ImageService#getRawPlane(long, int, int, int)
	 */
	public byte[] getRawPlane(long pixelsId, int z, int c, int t) 
			throws omero.ServerError
	{
		byte[] plane;
		plane = pixelsStore.getPlane(pixelsId, z, c, t);
		return plane;
	}
	
	/**
	 * Convert the rawPlane to doubles depending on the endianess and type of
	 * values in the rawplane.
	 * @param pixels The pixels data of the plane.
	 * @param z The z section.
	 * @param c The channel.
	 * @param t The time point.
	 * @return See above.
	 * @throws DSOutOfServiceException
	 * @throws omero.ServerError
	 */
	private double[][] convertRawPlaneAsDouble(Pixels pixels, int z, int c, int t) 
		throws omero.ServerError 
	{
		DataSink sink = DataSink.makeNew(pixels, this);
		Plane2D plane = sink.getPlane(z, t, c);
		return plane.getPixelsArrayAsDouble();
	}

	/**
	 * Convert the rawPlane to doubles depending on the endianess and type of
	 * values in the rawplane.
	 * @param pixels The pixels data of the plane.
	 * @param z The z section.
	 * @param c The channel.
	 * @param t The time point.
	 * @return See above.
	 * @throws DSOutOfServiceException
	 * @throws omero.ServerError
	 */
	private long[][] convertRawPlaneAsLong(Pixels pixels, int z, int c, int t) 
		throws omero.ServerError 
	{
		DataSink sink = DataSink.makeNew(pixels, this);
		Plane2D plane = sink.getPlane(z, t, c);
		return plane.getPixelsArrayAsLong();
	}
	
	/**
	 * Convert the rawPlane to doubles depending on the endianess and type of
	 * values in the rawplane.
	 * @param pixels The pixels data of the plane.
	 * @param z The z section.
	 * @param c The channel.
	 * @param t The time point.
	 * @return See above.
	 * @throws DSOutOfServiceException
	 * @throws omero.ServerError
	 */
	private int[][] convertRawPlaneAsInt(Pixels pixels, int z, int c, int t) 
		throws omero.ServerError 
	{
		DataSink sink = DataSink.makeNew(pixels, this);
		Plane2D plane = sink.getPlane(z, t, c);
		return plane.getPixelsArrayAsInt();
	}

	/**
	 * Convert the rawPlane to doubles depending on the endianess and type of
	 * values in the rawplane.
	 * @param pixels The pixels data of the plane.
	 * @param z The z section.
	 * @param c The channel.
	 * @param t The time point.
	 * @return See above.
	 * @throws DSOutOfServiceException
	 * @throws omero.ServerError
	 */
	private short[][] convertRawPlaneAsShort(Pixels pixels, int z, int c, int t) 
		throws omero.ServerError 
	{
		DataSink sink = DataSink.makeNew(pixels, this);
		Plane2D plane = sink.getPlane(z, t, c);
		return plane.getPixelsArrayAsShort();
	}
	
	/**
	 * Convert the rawPlane to doubles depending on the endianess and type of
	 * values in the rawplane.
	 * @param pixels The pixels data of the plane.
	 * @param z The z section.
	 * @param c The channel.
	 * @param t The time point.
	 * @return See above.
	 * @throws DSOutOfServiceException
	 * @throws omero.ServerError
	 */
	private byte[][] convertRawPlaneAsByte(Pixels pixels, int z, int c, int t) 
		throws omero.ServerError 
	{
		DataSink sink = DataSink.makeNew(pixels, this);
		Plane2D plane = sink.getPlane(z, t, c);
		return plane.getPixelsArrayAsByte();
	}

	
	/* (non-Javadoc)
	 * @see blitzgateway.service.ImageService#getPixels(long)
	 */
	public Pixels getPixels(long pixelsId) 
		throws omero.ServerError
	{
		return iPixels.retrievePixDescription(pixelsId);
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.ImageService#getImage(long)
	 */
	public Image getImage(long imageID) 
		throws omero.ServerError
	{
		String queryStr = new String("from Image as i left outer join fetch i.pixels as p where i.id= " 
			+ imageID);
		return (Image)iQuery.findByQuery(queryStr, null);
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.ImageService#copyPixels(long, int, int, int, int, java.lang.String)
	 */
	public Long copyPixels(long pixelsID, int x, int y, int t, int z, List<Integer> channelList,
			String methodology) throws omero.ServerError
	{
		Long newID = iPixels.copyAndResizePixels
						(pixelsID, new omero.RInt(x), new omero.RInt(y), new omero.RInt(t), new omero.RInt(z), channelList, methodology, true).val;
		return newID;
	}
	
	/* (non-Javadoc)
	 * @see blitzgateway.service.ImageService#copyPixels(long, java.lang.String)
	 */
	public Long copyPixels(long pixelsID, List<Integer> channelList,
			String methodology) throws omero.ServerError
	{
		Pixels pixels = getPixels(pixelsID);
		Long newID = iPixels.copyAndResizePixels
						(pixelsID, pixels.getSizeX(), pixels.getSizeY(), 
						 pixels.getSizeT(),pixels.getSizeZ(), 
						 channelList, methodology, true).val;
		return newID;
	}
	
	/* (non-Javadoc)
	 * @see blitzgateway.service.ImageService#copyImage(long, int, int, int, int, java.lang.String)
	 */
	public Long copyImage(long imageId, int x, int y, int t, int z, List<Integer> channelList,
			String methodology) throws omero.ServerError
	{
		Long newID = iPixels.copyAndResizeImage
						(imageId, new omero.RInt(x), new omero.RInt(y), new omero.RInt(t), new omero.RInt(z), channelList, methodology, true).val;
		return newID;
	}
	
	/**
	 * Upload the plane to the server, on pixels id with channel and the 
	 * time, + z section. the data is the client 2d data values. This will
	 * be converted to the raw server bytes.
	 * @param pixelsId pixels id to upload to .  
	 * @param z z section. 
	 * @param c channel.
	 * @param t time point.
	 * @param data plane data. 
	 * @throws DSOutOfServiceException
	 * @throws omero.ServerError
	 */
	public void uploadPlane(long pixelsId, int z, int c, int t, 
			double [][] data) throws omero.ServerError
	{
		Pixels pixels = getPixels(pixelsId);
		byte[] convertedData = convertClientToServer(pixels, data);
		pixelsStore.setPlane(pixelsId, convertedData, z,c,t);
	}

	/**
	 * convert the client data pixels to server byte array, also sets the data
	 * pixel size to the size of the pixels in the pixels Id param.
	 * @param pixels the pixels in the server.
	 * @param data the data on the client. 
	 * @return the bytes for server.
	 */
	public byte[] convertClientToServer(Pixels pixels, double [][] data)
	{
		String pixelsType  = pixels.getPixelsType().getValue().val;
		int pixelsSize = getPixelsSize(pixelsType); 
		int sizex = pixels.getSizeX().val;
		int sizey = pixels.getSizeY().val;
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
	private int calcOffset(int pixelSize, int sizex, int x, int y)
	{
		return pixelSize*(y*sizex+x);
	}
	
	/**
	 * Get the pixel size of the pixels.
	 * @param pixelsType see above.
	 * @return the size in bytes. 
	 */
	private int getPixelsSize(String pixelsType)
	{	
		return PixelTypes.pixelMap.get(pixelsType);
	}

	/**
	 * Map the byte data to a byte value 
	 * @param v see above.
	 * @return see above.
	 */
	private byte[] mapToByteArray(byte v) 
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
	private byte[] mapToByteArray(short v) 
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
	private byte[] mapToByteArray(int v) 
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
	private byte[] mapToByteArray(float v) 
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
	private byte[] mapToByteArray(double v) 
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
	private byte[] convertValue(String pixelsType, Double val)
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
	
	/**
	 * Test method to make sure the client converts it's data to the server data
	 * correctly. 
	 * @param pixelsId pixels id to upload to .  
	 * @param c channel.
	 * @param t time point.
	 * @param z z section.
	 * @return the converted data. 
	 * @throws DSOutOfServiceException
	 * @throws omero.ServerError */
	public double[][] testVal(long pixelsId, int z, int c, int t) 
		throws omero.ServerError
	{
	
		Pixels pixels = getPixels(pixelsId);
		double[][] serverPlane = getPlaneAsDouble(pixelsId, z, c, t);
		byte[] clientBytes = convertClientToServer(pixels, serverPlane);
		return DataSink.mapServerToClient(clientBytes, pixels.getSizeX().val, 
			pixels.getSizeY().val, pixels.getPixelsType().getValue().val);
	}
	
	/**
	 * update the pixels object, and return the new pixels.
	 * @param object the pixels object.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws omero.ServerError 
	 */
	public Pixels updatePixels(Pixels object) 
	throws omero.ServerError
	{
	
		return (Pixels)iUpdate.saveAndReturnObject(object);
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.ImageService#getChannelWindowEnd(java.lang.Long, int)
	 */
	public double getChannelWindowEnd(Long pixelsId, int w)
			throws omero.ServerError
	{
		return renderingService.getChannelWindowEnd(pixelsId, w);
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.ImageService#getChannelWindowStart(java.lang.Long, int)
	 */
	public double getChannelWindowStart(Long pixelsId, int w)
			throws omero.ServerError
	{
		return renderingService.getChannelWindowStart(pixelsId, w);
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.ImageService#getDefaultT(java.lang.Long)
	 */
	public int getDefaultT(Long pixelsId) throws 
			omero.ServerError
	{
		return renderingService.getDefaultT(pixelsId);
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.ImageService#getDefaultZ(java.lang.Long)
	 */
	public int getDefaultZ(Long pixelsId) throws 
			omero.ServerError
	{
		return renderingService.getDefaultZ(pixelsId);
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.ImageService#getPixels(java.lang.Long)
	 */
	public Pixels getPixels(Long pixelsId) throws 
			omero.ServerError
	{
		return renderingService.getPixels(pixelsId);
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.ImageService#getRenderedImage(long, int, int)
	 */
	public BufferedImage getRenderedImage(long pixelsId, int z, int t)
			throws omero.ServerError
	{
		return renderingService.getRenderedImage(pixelsId, z, t);
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.ImageService#getRenderedImageMatrix(long, int, int)
	 */
	public int[][][] getRenderedImageMatrix(long pixelsId, int z, int t)
			throws omero.ServerError
	{
		return renderingService.getRenderedImageMatrix(pixelsId, z, t);
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.ImageService#isActive(java.lang.Long, int)
	 */
	public boolean isActive(Long pixelsId, int w)
			throws omero.ServerError
	{
		return renderingService.isActive(pixelsId, w);
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.ImageService#renderAsPackedInt(java.lang.Long, int, int)
	 */
	public int[] renderAsPackedInt(Long pixelsId, int z, int t)
			throws omero.ServerError
	{
		return renderingService.renderAsPackedInt(pixelsId, z, t);
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.ImageService#setActive(java.lang.Long, int, boolean)
	 */
	public void setActive(Long pixelsId, int w, boolean active)
			throws omero.ServerError
	{
		renderingService.setActive(pixelsId, w, active);
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.ImageService#setChannelWindow(java.lang.Long, int, double, double)
	 */
	public void setChannelWindow(Long pixelsId, int w, double start, double end)
			throws omero.ServerError
	{
		renderingService.setChannelWindow(pixelsId, w, start, end);		
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.ImageService#setDefaultT(java.lang.Long, int)
	 */
	public void setDefaultT(Long pixelsId, int t)
			throws omero.ServerError
	{
		renderingService.setDefaultT(pixelsId, t);
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.ImageService#setDefaultZ(java.lang.Long, int)
	 */
	public void setDefaultZ(Long pixelsId, int z)
			throws omero.ServerError
	{
		renderingService.setDefaultZ(pixelsId, z);
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.ImageService#getThumbnail(long, omero.RInt, omero.RInt)
	 */
	public byte[] getThumbnail(long pixelsId, RInt sizeX, RInt sizeY)
			throws omero.ServerError
	{
		return thumbnailService.getThumbnail(pixelsId, sizeX, sizeY);
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.ImageService#getThumbnailByLongestSide(long, omero.RInt)
	 */
	public byte[] getThumbnailByLongestSide(long pixelsId, RInt size)
			throws omero.ServerError
	{
		return thumbnailService.getThumbnailByLongestSide(pixelsId, size);
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.ImageService#getThumbnailByLongestSideSet(omero.RInt, java.util.List)
	 */
	public Map<Long, byte[]> getThumbnailByLongestSideSet(RInt size,
			List<Long> pixelsIds) throws 
			omero.ServerError
	{
		return thumbnailService.getThumbnailByLongestSideSet(size, pixelsIds);
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.ImageService#getThumbnailSet(omero.RInt, omero.RInt, java.util.List)
	 */
	public Map<Long, byte[]> getThumbnailSet(RInt sizeX, RInt sizeY,
			List<Long> pixelsIds) throws 
			omero.ServerError
	{
		return thumbnailService.getThumbnailSet(sizeX, sizeY, pixelsIds);
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.ImageService#setRenderingDefId(long, long)
	 */
	public void setRenderingDefId(long pixelsId, long renderingDefId)
			throws omero.ServerError
	{
		thumbnailService.setRenderingDefId(pixelsId, renderingDefId);
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.ImageService#createImage(int, int, int, int, java.util.List, omero.model.PixelsType, java.lang.String, java.lang.String)
	 */
	public Long createImage(int sizeX, int sizeY, int sizeZ, int sizeT,
			List<Integer> channelList, PixelsType pixelsType, String name,
			String description) throws omero.ServerError
	{
		return iPixels.createImage(sizeX, sizeY, sizeZ, sizeT, channelList, pixelsType, name, description).val;
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.ImageService#keepAlive()
	 */
	public void keepAlive() throws omero.ServerError
	{
		renderingService.keepAlive();
		thumbnailService.keepAlive();
	}

	/* (non-Javadoc)
	 * @see omeroj.service.ImageService#getStack(long, int)
	 */
	public double[][][] getPlaneStack(long pixelsId, int c, int t)
			throws omero.ServerError
	{
		Pixels pixels = getPixels(pixelsId);
		double[][][] stack = new double[pixels.getSizeZ().val][pixels.getSizeX().val][pixels.getSizeY().val];
		for(int z = 0 ; z < pixels.getSizeZ().val ; z++)
			stack[z] = getPlaneAsDouble(pixelsId, z, c, t);
		return stack;
	}


}


