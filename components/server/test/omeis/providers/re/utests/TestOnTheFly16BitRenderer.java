/*
 *   Copyright (C) 2009-2011 University of Dundee & Open Microscopy Environment.
 *   All rights reserved.
 *
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package omeis.providers.re.utests;

import omeis.providers.re.data.PlaneDef;
import omeis.providers.re.quantum.QuantumFactory;

import org.perf4j.LoggingStopWatch;
import org.perf4j.StopWatch;
import org.testng.annotations.Test;

public class TestOnTheFly16BitRenderer extends BaseRenderingTest
{
	@Override
	protected QuantumFactory createQuantumFactory()
	{
		TestQuantumFactory qf = new TestQuantumFactory();
		qf.setStratgey(new OnTheFlyStrategy(settings.getQuantization(),
				                            pixels.getType()));
		return qf;
	}

	@Test
	public void testRenderAsPackedInt() throws Exception
	{
		PlaneDef def = new PlaneDef(PlaneDef.XY, 0);
		for (int i = 0; i < RUN_COUNT; i++)
		{
			StopWatch stopWatch = 
				new LoggingStopWatch("testRenderAsPackedIntOnThFly");
			int[] renderedPlane = renderer.renderAsPackedInt(def, pixelBuffer);
			stopWatch.stop();
		}
	}
}
