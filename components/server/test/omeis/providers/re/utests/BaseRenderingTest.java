/*
 *   Copyright (C) 2009-2013 University of Dundee & Open Microscopy Environment.
 *   All rights reserved.
 *
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package omeis.providers.re.utests;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Random;

import org.testng.annotations.BeforeClass;

import ome.api.IPixels;
import ome.io.nio.PixelBuffer;
import ome.util.PixelData;
import ome.logic.RenderingSettingsImpl;
import ome.model.core.Channel;
import ome.model.core.Pixels;
import ome.model.display.RenderingDef;
import ome.model.enums.PixelsType;
import ome.model.enums.RenderingModel;
import ome.model.stats.StatsInfo;
import omeis.providers.re.Renderer;
import omeis.providers.re.quantum.QuantumFactory;

import junit.framework.TestCase;

public class BaseRenderingTest extends TestCase
{
	protected Renderer renderer;
	
	protected Random random = new Random();
	
	protected PixelData data;
	
	protected Pixels pixels;
	
	protected PixelBuffer pixelBuffer;
	
	protected RenderingDef settings;
	
	protected RenderingSettingsImpl settingsService;
	
	protected IPixels pixelsMetadataService;
	
	protected TestPixelsService pixelsService;
	
	protected QuantumFactory quantumFactory;
	
	public static final int RUN_COUNT = 10;
	
	// Configurables
	
	@Override
	@BeforeClass
	protected void setUp()
	{
		PixelsType pixelsType = getPixelsType();
		
		byte[] plane = getPlane();
		data = new PixelData(pixelsType.getValue(), ByteBuffer.wrap(plane));

		pixels = createDummyPixels(pixelsType, data);
		pixelsService = new TestPixelsService(pixels);
		pixelsService.setDummyPlane(plane);
		pixelsMetadataService = new TestPixelsMetadataService();
		settingsService = new RenderingSettingsImpl();
		settingsService.setPixelsMetadata(pixelsMetadataService);
		settingsService.setPixelsData(pixelsService);
		settings = settingsService.createNewRenderingDef(pixels);
		settingsService.resetDefaultsNoSave(settings, pixels);

		pixelBuffer = pixelsService.getPixelBuffer(pixels, false);
		List<RenderingModel> renderingModels =
			pixelsMetadataService.getAllEnumerations(RenderingModel.class);
		quantumFactory = createQuantumFactory();
		renderer = new Renderer(quantumFactory, renderingModels,
				                pixels, settings, pixelBuffer);
	}
	
	protected QuantumFactory createQuantumFactory()
	{
		return new TestQuantumFactory();
	}
	
	protected int getSizeX()
	{
		return 1024;
	}

	protected int getSizeY()
	{
		return 1024;
	}
	
	protected int getSizeZ()
	{
		return 1;
	}
	
	protected int getSizeC()
	{
		return 3;
	}
	
	protected int getSizeT()
	{
		return 1;
	}
	
	protected int getBytesPerPixel()
	{
		return 2;
	}
	
	protected PixelsType getPixelsType()
	{
		PixelsType pixelsType = new PixelsType();
		pixelsType.setValue("uint16");
		pixelsType.setBitSize(16);
		return pixelsType;
	}
	
	protected byte[] getPlane()
	{
		byte[] plane = new byte[getSizeX() * getSizeY() * getBytesPerPixel()];
		random.nextBytes(plane);
		return plane;
	}
	
	private double[] calculateMinMax(PixelData data)
	{
		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;
		double value;
		for (int i = 0; i < data.size(); i++)
		{
			value = data.getPixelValue(i);
			if (value > max)
			{
				max = value;
			}
			if (value < min)
			{
				min = value;
			}
		}
		return new double[] { min, max };
	}
	
	Pixels createDummyPixels(PixelsType pixelsType, PixelData plane)
	{
		Pixels pixels = new Pixels(1L, true);
		pixels.setSizeX(getSizeX());
		pixels.setSizeY(getSizeY());
		pixels.setSizeZ(getSizeZ());
		pixels.setSizeC(getSizeC());
		pixels.setSizeT(getSizeT());
		pixels.setPixelsType(pixelsType);

		double[] minAndMax = calculateMinMax(plane);
		for (int c = 0; c < pixels.getSizeC(); c++)
		{
			StatsInfo si = new StatsInfo();
			si.setGlobalMin(minAndMax[0]);
			si.setGlobalMax(minAndMax[1]);
			Channel channel = new Channel();
			channel.setStatsInfo(si);
			pixels.addChannel(channel);
		}
		return pixels;
	}

}
