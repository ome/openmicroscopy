/*
 * Copyright (C) 2013 Glencoe Software, Inc. All rights reserved.
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
package integration;

import junit.framework.TestCase;

import org.testng.annotations.Test;

public class BlockSizeTest extends TestCase {

    @Test
    public void testBlockSizeDefault() throws Exception {
        omero.client c = new omero.client("localhost");
        assertEquals(5000000, c.getDefaultBlockSize());
    }

    @Test
    public void testBlockSize1MB() throws Exception {
        omero.client c = new omero.client(new String[] {
                "--omero.host=localhost", "--omero.block_size=1000000" });
        assertEquals(1000000, c.getDefaultBlockSize());
    }
}
