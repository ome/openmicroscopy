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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package ome.formats.importer;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.File;

import loci.formats.ChannelSeparator;
import loci.formats.FormatTools;
import loci.formats.Memoizer;
import loci.formats.MinMaxCalculator;
import loci.formats.in.SVSReader;
import loci.formats.meta.DummyMetadata;
import loci.formats.ome.OMEXMLMetadataImpl;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import omero.util.TempFileManager;

/**

 */
public class OMEROWrapperTest {

    class Bad {
        class Store extends DummyMetadata {
            public Store(boolean ThisCantBeDeserialized) {
                /*
                 * no action
                 */
            }
        }
    }

    File fake;

    File png;

    OMEROWrapper wrapper;

    ImportConfig config;

    @BeforeMethod
    public void createPNG() throws Exception {
        fake = TempFileManager.create_path("omerowrappertest.", ".fake");
        png = TempFileManager.create_path("omerowrappertest.", ".png");
        FileUtils.touch(fake);
        FormatTools.convert(fake.getAbsolutePath(), png.getAbsolutePath());
    }

    @AfterMethod
    public void deleteFiles() throws Exception {
        Memoizer m = new Memoizer();
        FileUtils.deleteQuietly(fake);
        FileUtils.deleteQuietly(png);
        FileUtils.deleteQuietly(m.getMemoFile(png.toString()));
        FileUtils.deleteQuietly(m.getMemoFile(fake.toString()));
        m.close();
    }

    @BeforeMethod
    public void setUp() {
        config = new ImportConfig();
        wrapper = new OMEROWrapper(config, 0 /*min */, null);
    }

    @AfterMethod
    public void tearDown() throws Exception {
        wrapper.close();
    }

    @Test
    public void testFake() throws Exception {
        wrapper.setId(fake.getAbsolutePath());
        wrapper.close();
    }

    @Test
    public void testPNG() throws Exception {
        wrapper.setId(png.getAbsolutePath());
        wrapper.close();
    }

    @Test
    public void testReuse() throws Exception {
        wrapper.setId(fake.getAbsolutePath());
        wrapper.close();
        // wrapper.setMetadataStore(null);
        // wrapper.setMinMaxStore(null);
        wrapper.setId(png.getAbsolutePath());
    }

    @Test
    public void testMatchedWrappers() throws Exception {
        Memoizer m = new Memoizer(new ChannelSeparator());
        try {
            m.setId(png.getAbsolutePath());
            m.close();
            assertTrue(m.isSavedToMemo());

            m = new Memoizer(new ChannelSeparator());
            m.setId(png.getAbsolutePath());
            assertTrue(m.isLoadedFromMemo());
        } finally {
            m.close();
        }
    }

    @Test
    public void testMismatchedWrappers() throws Exception {

        Memoizer m = null;
        try {
            m = new Memoizer(0L /* min elapsed */);
            m.setId(fake.getAbsolutePath());
            assertTrue(m.isSavedToMemo());
            assertFalse(m.isLoadedFromMemo());
        } finally {
            m.close();
        }

        for (int i = 0; i < 1; i++) {
        try {
            m = new Memoizer();
            m.setId(fake.getAbsolutePath());
            assertTrue(m.isLoadedFromMemo());
            assertFalse(m.isSavedToMemo());
            m.close();


            //m = new Memoizer(new MinMaxCalculator(), 0L /* min elapsed */);
            //m.setId(fake.getAbsolutePath());
            //assertFalse(m.isLoadedFromMemo());
        } finally {
            //m.close();
        }
        }
    }

    @Test
    public void testRepeated() throws Exception {

        Memoizer m = null;
        try {
            m = new Memoizer(0L /* min elapsed */);
            m.setId(fake.getAbsolutePath());
            assertTrue(m.isSavedToMemo());
            assertFalse(m.isLoadedFromMemo());
        } finally {
            m.close();
        }

        for (int i = 0; i < 100; i++) {
            try {
                m = new Memoizer();
                m.setId(fake.getAbsolutePath());
                assertTrue(m.isLoadedFromMemo());
                assertFalse(m.isSavedToMemo());
            } finally {
                m.close();
            }
        }

    }

    @Test
    public void testOMEXMLMetadataStore() throws Exception {
        wrapper.setMetadataStore(new OMEXMLMetadataImpl());
        wrapper.setId(png.getAbsolutePath());
        assertFalse(((Memoizer)wrapper.getReader()).isLoadedFromMemo());
        assertTrue(((Memoizer)wrapper.getReader()).isSavedToMemo());

        wrapper.setId(png.getAbsolutePath());
        assertTrue(((Memoizer)wrapper.getReader()).isLoadedFromMemo());
        assertFalse(((Memoizer)wrapper.getReader()).isSavedToMemo());
        assertEquals(OMEXMLMetadataImpl.class,
                wrapper.getMetadataStore().getClass());

    }

    @Test
    public void testUnserializable() throws Exception {
        wrapper.setMetadataStore(new Bad().new Store(false));
        wrapper.setId(png.getAbsolutePath());
        wrapper.close();
        wrapper.setId(png.getAbsolutePath());
    }

    @Test
    public void testDirectory() throws Exception {
        Memoizer m = new Memoizer(0L /* min */,
                TempFileManager.create_path("memo.", ".dir", true));
        m.setId(fake.getAbsolutePath());
        assertTrue(m.isSavedToMemo());
        m.close();
    }
}
