package omeis.providers.re.utests;

import ome.io.nio.PixelBuffer;
import ome.io.nio.PixelsService;
import ome.model.core.Pixels;

public class TestPixelsService extends PixelsService
{
	private byte[] dummyPlane;
	
	public TestPixelsService()
	{
		super("/tmp");
	}
	
	public void setDummyPlane(byte[] dummyPlane)
	{
		this.dummyPlane = dummyPlane;
	}
	
	@Override
	public PixelBuffer getPixelBuffer(Pixels pixels)
	{
		return new TestPixelBuffer(pixels.getPixelsType(), dummyPlane);
	}
}
