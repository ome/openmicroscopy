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
package omero.cmd.graphs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jmock.MockObjectTestCase;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ome.testing.MockServiceFactory;

import omero.cmd.Chgrp;
import omero.cmd.Delete;
import omero.cmd.DoAll;
import omero.cmd.GraphModify;
import omero.cmd.Helper;
import omero.cmd.Request;
import omero.cmd.Status;

public class PreprocessorTest extends MockObjectTestCase {

    MockServiceFactory msf;

    Helper helper;

    @BeforeMethod
    protected void createHelper() {
        msf = new MockServiceFactory();
        helper = new Helper(new DoAll(), new Status(), null, null, msf);
    }

    private void projection(Object[]... rv) {
        List<Object> list = new ArrayList<Object>();
        for (Object[] obj : rv) {
            list.add(obj);
        }
        msf.mockQuery.expects(once()).method("projection")
                .will(returnValue(list));
    }

    private List<Request> req(Request... r) {
        return new ArrayList<Request>(Arrays.asList(r));
    }

    private Preprocessor proc(Request... r) {
        return new Preprocessor(req(r), helper);
    }

    private Delete delImg(long id) {
        return new Delete("/Image", id, null);
    }

    private Delete delFs(long id) {
        return new Delete("/Fileset", id, null);
    }

    private Chgrp chgrpImg(long id) {
        return new Chgrp("/Image", id, null, -1L);
    }

    private void assertProc(Preprocessor proc, long imageCount,
            long filesetCount) {
        assertEquals(imageCount, proc.getImageCount());
        assertEquals(filesetCount, proc.getFilesetCount());
    }

    private void assertReqs(Preprocessor proc, Request... r) {
        List<Request> procList = proc.getRequests();
        assertEquals(r.length, procList.size());
        for (int i = 0; i < r.length; i++) {
            GraphModify expected = (GraphModify) r[i];
            GraphModify found = (GraphModify) procList.get(i);
            assertEquals(expected.getClass(), found.getClass());
            assertEquals(expected.type, found.type);
            assertEquals(expected.id, found.id);
        }
    }

    @Test
    public void testNothing() {
        Preprocessor proc = proc();
        assertProc(proc, -1, -1);
    }

    @Test
    public void testDeleteOneImageInFileset() {
        projection(new Object[] { 100L, 1L });
        Preprocessor proc = proc(delImg(1));
        assertProc(proc, 0, 0);
        // Nothing is done if there's just one GraphModify
        assertReqs(proc, delImg(1));
    }

    @Test
    public void testDeleteTwoImagesInFileset() {
        projection(new Object[] { 100L, 1L }, new Object[] { 100L, 2L });
        Preprocessor proc = proc(delImg(1), delImg(2));
        assertProc(proc, 2, 1);
        assertReqs(proc, delFs(100));
    }

    @Test
    public void testDeleteChgrpTwoImagesInFileset() {
        projection(new Object[] { 100L, 1L }, new Object[] { 100L, 2L });
        Preprocessor proc = proc(delImg(1), chgrpImg(2));
        assertProc(proc, 2, 1);
        assertReqs(proc, delImg(1), chgrpImg(2));
    }

    private void assertMixed(Delete fs1, Delete fs2, Delete... deletes) {
        projection(new Object[] { 100L, 1L}, new Object[]{100L, 2L});
        projection(new Object[] { 200L, 3L}, new Object[]{200L, 4L });
        Preprocessor proc = proc(deletes);
        assertProc(proc, 4, 2);
        assertReqs(proc, fs1, fs2);
    }

    @Test
    public void testTwoFilesetsMixed() {
        assertMixed(delFs(100), delFs(200), delImg(1), delImg(2), delImg(3), delImg(4));
        assertMixed(delFs(100), delFs(200), delImg(1), delImg(3), delImg(2), delImg(4));
        assertMixed(delFs(200), delFs(100), delImg(1), delImg(4), delImg(3), delImg(2));
        assertMixed(delFs(200), delFs(100), delImg(1), delImg(3), delImg(4), delImg(2));
    }
}
