/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2014 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package omeis.providers.re.utests;

import java.nio.ByteBuffer;

import ome.model.enums.PixelsType;
import omeis.providers.re.data.PlaneDef;
import omeis.providers.re.quantum.Quantization_float;
import omeis.providers.re.quantum.QuantumFactory;
import omeis.providers.re.quantum.QuantumStrategy;

import org.perf4j.LoggingStopWatch;
import org.perf4j.StopWatch;
import org.testng.annotations.Test;

public class TestStandardFloatRenderer extends BaseRenderingTest
{
    @Override
    protected QuantumFactory createQuantumFactory()
    {
        TestQuantumFactory qf = new TestQuantumFactory();
        qf.setStrategy(new Quantization_float(settings.getQuantization(),
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
        return 4;
    }

    @Override
    protected int getBytesPerPixel()
    {
        return 4;
    }

    @Override
    protected byte[] getPlane()
    {
        int n = 16;
        Float[] output = new Float[16];
        for (int i = 0; i < n/2; i++) {
            output[i+n/2] = new Float(Integer.MAX_VALUE);
        }
        for (int i = 0; i < n/2; i++) {
            output[i] = 0.0f;
        }
        ByteBuffer buffer = ByteBuffer.allocate(4 * output.length);

        for (float value : output){
            buffer.putFloat(value);
        }
        return buffer.array();
    }

    @Override
    protected PixelsType getPixelsType()
    {
        PixelsType pixelsType = new PixelsType();
        pixelsType.setValue("float");
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
            assertEquals(new Float(qs.getPixelsTypeMax()),
                    new Float(data.getPixelValue(i+n/2)));
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
        assertTrue(Integer.MIN_VALUE == qs.getPixelsTypeMin());
        assertTrue(Integer.MAX_VALUE == qs.getPixelsTypeMax());
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
