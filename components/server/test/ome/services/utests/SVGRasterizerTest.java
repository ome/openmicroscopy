/*
 * Copyright (C) 2013 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package ome.services.utests;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import ome.services.SVGRasterizer;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests the JPEG rendering logic from {@link SVGRasterizer}. This test should
 * fail if the underlying SVG rasterization implementation uses an unavailable
 * Java API (e.g. Batik 1.7 and com.sun.image.codec.jpeg.JPEGCodec).
 * @see ticket #11438
 * @author Blazej Pindelski, bpindelski at dundee.ac.uk
 *
 */
public class SVGRasterizerTest {

    @Test(groups = {"unit", "ticket:11438"})
    public void testCreateJPEG() {
        InputStream input = IOUtils.toInputStream(
                "<svg xmlns=\"http://www.w3.org/2000/svg\"" +
                " xmlns:xlink=\"http://www.w3.org/1999/xlink\">" +
                    "<rect x=\"10\" y=\"10\" height=\"100\" width=\"100\"" +
                    " style=\"stroke:#ff0000; fill: #0000ff\"/>" +
                "</svg>");
        SVGRasterizer rasterizer = new SVGRasterizer(input);
        rasterizer.setQuality(1);
        OutputStream outputStream = new ByteArrayOutputStream();
        try {
            rasterizer.createJPEG(outputStream);
        } catch (TranscoderException te) {
            Assert.fail("JPEG encoding failed with exception.", te);
        } finally {
           IOUtils.closeQuietly(outputStream);
           IOUtils.closeQuietly(input);
        }
    }

}
