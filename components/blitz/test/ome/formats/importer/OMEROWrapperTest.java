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

import java.io.File;

import loci.formats.FormatTools;
import loci.formats.IFormatReader;
import loci.formats.Memoizer;
import loci.formats.MinMaxCalculator;
import loci.formats.in.FakeReader;
import loci.formats.meta.DummyMetadata;
import loci.formats.ome.OMEXMLMetadataImpl;

import org.apache.commons.io.FileUtils;
import org.testng.Assert;
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
        wrapper = new OMEROWrapper(config, 0, null);
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
        wrapper.setId(png.getAbsolutePath());
    }

    @Test(timeOut=3000)
    public void testMatchedWrappers() throws Exception {
        try (FakeReader r = new FakeReader();
             Memoizer m = new Memoizer(r, 0)) {
            m.setId(fake.getAbsolutePath());
            m.close();
            Assert.assertTrue(m.isSavedToMemo());
            m.setId(fake.getAbsolutePath());
            Assert.assertTrue(m.isLoadedFromMemo());
            m.close();
        }
    }

    @Test(invocationCount = 10, timeOut=3000)
    public void testMismatchedWrappers() throws Exception {
        try (IFormatReader r = new MinMaxCalculator();
                Memoizer m = new Memoizer(0L);
                Memoizer m1 = new Memoizer(r, 0L)) {
            m.setId(fake.getAbsolutePath());
            m.close();
            Assert.assertTrue(m.isSavedToMemo());
            m1.setId(fake.getAbsolutePath());
            Assert.assertFalse(m1.isLoadedFromMemo());
            m1.close();
        }
    }

    @Test
    public void testOMEXMLMetadataStore() throws Exception {
        wrapper.setMetadataStore(new OMEXMLMetadataImpl());
        wrapper.setId(fake.getAbsolutePath());
        wrapper.close();
        Memoizer m = new Memoizer(wrapper.getImageReader(), 0L);
        m.setId(fake.getAbsolutePath());
        Assert.assertFalse(m.isLoadedFromMemo());
        Assert.assertTrue(m.isSavedToMemo());
        m.close();
        wrapper.setId(fake.getAbsolutePath());
        wrapper.close();
        m = new Memoizer(wrapper.getImageReader(), 0L);
        m.setId(fake.getAbsolutePath());
        Assert.assertTrue(m.isLoadedFromMemo());
        Assert.assertFalse(m.isSavedToMemo());
        Assert.assertEquals(wrapper.getMetadataStore().getClass(),
                OMEXMLMetadataImpl.class);
        m.close();
    }

    @Test
    public void testUnserializable() throws Exception {
        wrapper.setMetadataStore(new Bad().new Store(false));
        wrapper.setId(png.getAbsolutePath());
        wrapper.close();
        wrapper.setId(png.getAbsolutePath());
    }

}
