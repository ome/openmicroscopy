package omeis.providers.re.utests;

import omeis.providers.re.data.PlaneDef;

import org.perf4j.LoggingStopWatch;
import org.perf4j.StopWatch;
import org.testng.annotations.Test;

public class TestStandard16BitRenderer extends BaseRenderingTest
{
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
