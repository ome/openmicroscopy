package omeis.providers.re.utests;

import ome.model.enums.PixelsType;
import omeis.providers.re.data.PlaneDef;

import org.perf4j.LoggingStopWatch;
import org.perf4j.StopWatch;
import org.testng.annotations.Test;

public class TestStandard32BitRenderer extends BaseRenderingTest
{
	@Override
	protected int getSizeX()
	{
		return 4;
	}
	
	@Override
	protected int getSizeY()
	{
		return 4;
	}
	
	@Override
	protected int getBytesPerPixel()
	{
		return 4;
	}
	
	@Override
	protected PixelsType getPixelsType()
	{
		PixelsType pixelsType = new PixelsType();
		pixelsType.setValue("uint32");
		return pixelsType;
	}
	
	@Test
	public void testDumpPixelValues() throws Exception
	{
		for (int i = 0; i < data.size(); i++)
		{
			System.err.println(data.getPixelValue(i));
		}
	}
	
	@Test
	public void testRenderAsPackedInt() throws Exception
	{
		PlaneDef def = new PlaneDef(PlaneDef.XY, 0);
		for (int i = 0; i < RUN_COUNT; i++)
		{
			StopWatch stopWatch = 
				new LoggingStopWatch("testRendererAsPackedInt");
			int[] renderedPlane = renderer.renderAsPackedInt(def, pixelBuffer);
			stopWatch.stop();
		}
	}
}
