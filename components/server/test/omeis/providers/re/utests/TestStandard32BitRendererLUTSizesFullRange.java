/*
 *   Copyright (C) 2009-2011 University of Dundee & Open Microscopy Environment.
 *   All rights reserved.
 *
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package omeis.providers.re.utests;

import ome.model.enums.PixelsType;
import omeis.providers.re.data.PlaneDef;
import omeis.providers.re.quantum.Quantization_32_bit;
import omeis.providers.re.quantum.QuantumFactory;
import omeis.providers.re.quantum.QuantumStrategy;

import org.perf4j.LoggingStopWatch;
import org.perf4j.StopWatch;
import org.testng.annotations.Test;

public class TestStandard32BitRendererLUTSizesFullRange extends BaseRenderingTest
{

    @Override
    protected QuantumFactory createQuantumFactory()
    {
        TestQuantumFactory qf = new TestQuantumFactory();
        qf.setStrategy(new Quantization_32_bit(settings.getQuantization(),
                pixels));
        return qf;
    }

    @Override
    protected int getSizeX()
    {
        return 2;
    }

    @Override
    protected int getSizeY()
    {
        return 2;
    }

    @Override
    protected byte[] getPlane()
    {
        return new byte[] {
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
        };
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
        pixelsType.setBitSize(32);
        return pixelsType;
    }

    @Test
    public void testPixelValues() throws Exception
    {
        QuantumStrategy qs = quantumFactory.getStrategy(
                settings.getQuantization(), pixels);
        int n = data.size();
        for (int i = 0; i < n/2; i++) {
            assertEquals(0.0, data.getPixelValue(i));
        }
        for (int i = 0; i < n/2; i++) {
            assertEquals(qs.getPixelsTypeMax(), data.getPixelValue(i+n/2));
        }
        try
        {
            assertEquals(0.0, data.getPixelValue(n));
            fail("Should have thrown an IndexOutOfBoundsException.");
        }
        catch (IndexOutOfBoundsException e) { }
    }

    @Test
    public void testPixelValuesRange() throws Exception
    {
        QuantumStrategy qs = quantumFactory.getStrategy(
                settings.getQuantization(), pixels);
        assertEquals(0.0, qs.getPixelsTypeMin());
        assertEquals(Math.pow(2, 32)-1, qs.getPixelsTypeMax());
    }

    @Test(timeOut=30000)
    public void testRenderAsPackedInt() throws Exception
    {
        PlaneDef def = new PlaneDef(PlaneDef.XY, 0);
        for (int i = 0; i < RUN_COUNT; i++)
        {
            StopWatch stopWatch =
                    new LoggingStopWatch("testRendererAsPackedInt");
            renderer.renderAsPackedInt(def, pixelBuffer);
            stopWatch.stop();
        }
    }
}
