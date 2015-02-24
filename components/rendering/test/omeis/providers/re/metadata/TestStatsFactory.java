/*
 * Copyright (C) 2015 Glencoe Software, Inc.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package omeis.providers.re.metadata;

import ome.model.core.Pixels;
import ome.model.enums.PixelsType;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit test cases for {@link omeis.providers.re.metadata.StatsFactory}
 * instances.
 *
 * @author Chris Allan <callan@glencoesoftware.com>
 */
@Test
public class TestStatsFactory {

    final StatsFactory statsFactory = new StatsFactory();

    private Pixels createPixels(String value, Integer bitSize) {
        Pixels pixels = new Pixels();
        PixelsType pixelsType = new PixelsType(value);
        pixelsType.setBitSize(bitSize);
        pixels.setPixelsType(pixelsType);
        return pixels;
    }

    public void testInitPixelsRangeInt8() {
        Pixels pixels = createPixels("int8", 8);

        double[] pixelsRange = statsFactory.initPixelsRange(pixels);
        Assert.assertEquals(pixelsRange[0], -128.0);
        Assert.assertEquals(pixelsRange[1], 127.0);
    }

    public void testInitPixelsRangeInt16() {
        Pixels pixels = createPixels("int16", 16);

        double[] pixelsRange = statsFactory.initPixelsRange(pixels);
        Assert.assertEquals(pixelsRange[0], -32768.0);
        Assert.assertEquals(pixelsRange[1], 32767.0);
    }

    public void testInitPixelsRangeInt32() {
        Pixels pixels = createPixels("int32", 32);

        double[] pixelsRange = statsFactory.initPixelsRange(pixels);
        // We have to cast the assertions to double because of differences
        // in precision that would cause test failures.
        Assert.assertEquals(pixelsRange[0], (double) Integer.MIN_VALUE);
        Assert.assertEquals(pixelsRange[1], (double) Integer.MAX_VALUE);
    }

    public void testInitPixelsRangeUInt8() {
        Pixels pixels = createPixels("uint8", 8);

        double[] pixelsRange = statsFactory.initPixelsRange(pixels);
        Assert.assertEquals(pixelsRange[0], 0.0);
        Assert.assertEquals(pixelsRange[1], 255.0);
    }

    public void testInitPixelsRangeUInt16() {
        Pixels pixels = createPixels("uint16", 16);

        double[] pixelsRange = statsFactory.initPixelsRange(pixels);
        Assert.assertEquals(pixelsRange[0], 0.0);
        Assert.assertEquals(pixelsRange[1], 65535.0);
    }

    public void testInitPixelsRangeUInt32() {
        Pixels pixels = createPixels("uint32", 32);

        double[] pixelsRange = statsFactory.initPixelsRange(pixels);
        Assert.assertEquals(pixelsRange[0], 0.0);
        Assert.assertEquals(pixelsRange[1], (double) (Math.pow(2, 32) - 1));
    }

    public void testInitPixelsRangeFloat() {
        Pixels pixels = createPixels("float", 32);

        double[] pixelsRange = statsFactory.initPixelsRange(pixels);
        // We have to cast the assertions to double because of differences
        // in precision that would cause test failures.
        Assert.assertEquals(pixelsRange[0], (double) Integer.MIN_VALUE);
        Assert.assertEquals(pixelsRange[1], (double) Integer.MAX_VALUE);
    }

    public void testInitPixelsRangeDouble() {
        Pixels pixels = createPixels("double", 64);

        double[] pixelsRange = statsFactory.initPixelsRange(pixels);
        Assert.assertEquals(pixelsRange[0], (double) Integer.MIN_VALUE);
        Assert.assertEquals(pixelsRange[1], (double) Integer.MAX_VALUE);
    }

    public void testInitPixelsRange12Bit() {
        Pixels pixels = createPixels("uint16", 16);
        pixels.setSignificantBits(12);

        double[] pixelsRange = statsFactory.initPixelsRange(pixels);
        Assert.assertEquals(pixelsRange[0], 0.0);
        Assert.assertEquals(pixelsRange[1], 4095.0);
    }

    public void testInitPixelsRangeClamping() {
        Pixels pixels = createPixels("uint16", 16);
        pixels.setSignificantBits(32);

        double[] pixelsRange = statsFactory.initPixelsRange(pixels);
        Assert.assertEquals(pixelsRange[0], 0.0);
        Assert.assertEquals(pixelsRange[1], 65535.0);
    }
}
