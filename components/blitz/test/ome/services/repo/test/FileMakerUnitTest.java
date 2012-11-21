/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.repo.test;

import ome.conditions.InternalException;
import ome.services.blitz.repo.FileMaker;

import org.testng.annotations.Test;

/**
 * Tests the simple class responsible for creating, reading, and locking the
 * repository uuid file.
 */
@Test(groups = { "repo" })
public class FileMakerUnitTest extends AbstractRepoUnitTest {

    @Test(expectedExceptions = InternalException.class)
    public void testGetNotInitializedThrows() throws Exception {
        FileMaker fm = new FileMaker(tmpRepo.getAbsolutePath());
        fm.getLine();
    }

    @Test(expectedExceptions = InternalException.class)
    public void testWriteNotInitializedThrows() throws Exception {
        FileMaker fm = new FileMaker(tmpRepo.getAbsolutePath());
        fm.writeLine(null);
    }

    public void testGetDir() throws Exception {
        FileMaker fm = new FileMaker(tmpRepo.getAbsolutePath());
        assertEquals(tmpRepo.getAbsolutePath(), fm.getDir());
    }

    public void testBlank() throws Exception {
        FileMaker fm = new FileMaker(tmpRepo.getAbsolutePath());
        fm.init("blank");
        String line = fm.getLine();
        assertEquals(null, line);
    }

    public void testInitialized() throws Exception {
        FileMaker fm = new FileMaker(tmpRepo.getAbsolutePath());
        fm.init("settofoo");
        fm.writeLine("foo");
        String line = fm.getLine();
        assertEquals("foo", line);
    }


}
