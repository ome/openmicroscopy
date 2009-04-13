package ome.ij;

import java.awt.image.ColorModel;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;

import ij.plugin.PlugIn;
import ij.process.ByteProcessor;

import i5d.Image5D;

import omero.client;
import omero.api.IContainerPrx;
import omero.api.IPixelsPrx;
import omero.api.IQueryPrx;
import omero.api.RawPixelsStorePrx;
import omero.api.ServiceFactoryPrx;
import omero.model.Image;
import omero.model.Pixels;
import omero.model.StatsInfo;

public class Omero_Test implements PlugIn {
	public void run(String arg){
		try
		{
			doWork(arg);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public void doWork(String arg) throws Exception
	{
		client client = new client("localhost");
		ServiceFactoryPrx sf = client.createSession("root", "omero");
		IContainerPrx iContainer = sf.getContainerService();
		IQueryPrx iQuery = sf.getQueryService();
		IPixelsPrx iPixels = sf.getPixelsService();
		
		IJ.showStatus("Loading Image metadata...");
		List<Long> ids = new ArrayList<Long>();
		ids.add(1L);
		Image image = (Image) iContainer.getImages("Image", ids, null).get(0);
		long pixelsId = image.getPrimaryPixels().getId().getValue();
		Pixels pixels = iPixels.retrievePixDescription(pixelsId);
		int sizeX = pixels.getSizeX().getValue();
		int sizeY = pixels.getSizeY().getValue();
		int sizeZ = pixels.getSizeZ().getValue();
		int sizeC = pixels.getSizeC().getValue();
		int sizeT = pixels.getSizeT().getValue();
		
		IJ.showStatus("Reading Image...");				
		RawPixelsStorePrx store = sf.createRawPixelsStore();
		store.setPixelsId(pixelsId, false);
		
		Image5D image5d = new Image5D("test", ImagePlus.GRAY16, sizeX, 
				sizeY, sizeC, sizeZ, sizeT, false);
		for (int z = 0; z < sizeZ; z++)
		{
			for (int c = 0; c < sizeC; c++)
			{
				StatsInfo statsInfo = pixels.getChannel(c).getStatsInfo();
				double min = statsInfo.getGlobalMin().getValue();
				double max = statsInfo.getGlobalMax().getValue();
				image5d.setChannelMinMax(c + 1, min, max);
				for (int t = 0; t < sizeT; t++)
				{
					image5d.setCurrentPosition(0, 0, c, z, t);
					image5d.setPixels(asShort(store.getPlane(z, c, t)));
				}
			}
		}

		image5d.setCurrentPosition(0, 0, 0, 0, 0);
		image5d.show();
	}
	
	public short[] asShort(byte[] plane)
	{
		int pixelCount = plane.length / 2;
		short[] toReturn = new short[pixelCount];
		ShortBuffer source = ByteBuffer.wrap(plane).asShortBuffer();
		for (int i = 0; i < pixelCount; i++)
		{
			toReturn[i] = source.get(i);
		}
		return toReturn;
	}
}

