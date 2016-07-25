/*
 * integration.ScriptServiceTest
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
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
package integration;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import omero.api.IScriptPrx;
import omero.grid.JobParams;
import omero.model.IObject;
import omero.model.OriginalFile;
import omero.sys.ParametersI;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Collections of tests for the <code>Script</code> service.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:donald@lifesci.dundee.ac.uk"
 *         >donald@lifesci.dundee.ac.uk</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Revision: $Date: $) </small>
 * @since 3.0-Beta4
 */
public class ScriptServiceTest extends AbstractServerTest {

    /** The mimetype of the lookup table files.*/
    private static final String LUT_MIMETYPE = "text/x-lut";

    /**
     * Tests to make sure that a new entry for the same file is not added
     * to the originalFile table.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testDuplicateEntries() throws Exception {
        IScriptPrx svc = factory.getScriptService();
        List<OriginalFile> scripts = svc.getScriptsByMimetype(LUT_MIMETYPE);
        Assert.assertNotNull(scripts);
        int n = scripts.size();
        ParametersI param = new ParametersI();
        param.add("m", omero.rtypes.rstring(LUT_MIMETYPE));
        String sql = "select f from OriginalFile as f "
                + "where f.mimetype = :m";
        List<IObject> values = iQuery.findAllByQuery(sql, param);
        Assert.assertEquals(n, values.size());
    }

    /**
     * Tests the retrieval of the scripts using the <code>getScripts</code>
     * method.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testGetScripts() throws Exception {
        IScriptPrx svc = factory.getScriptService();
        List<OriginalFile> scripts = svc.getScripts();
        Assert.assertNotNull(scripts);
        Assert.assertTrue(scripts.size() > 0);
        Iterator<OriginalFile> i = scripts.iterator();
        OriginalFile f;
        while (i.hasNext()) {
            f = i.next();
            Assert.assertNotNull(f);
            if (LUT_MIMETYPE.equals(f.getMimetype().getValue())) {
                Assert.fail("Lut should not be returned.");
            }
        }
        scripts = svc.getScripts();
        Assert.assertNotNull(scripts);
        Assert.assertTrue(scripts.size() > 0);
        i = scripts.iterator();
        while (i.hasNext()) {
            f = i.next();
            Assert.assertNotNull(f);
            if (LUT_MIMETYPE.equals(f.getMimetype().getValue())) {
                Assert.fail("Lut should not be returned.");
            }
        }
    }

    /**
     * Tests the retrieval of the scripts using the <code>getScriptsByMimetype</code>
     * method.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testGetScriptsByMimetype() throws Exception {
        IScriptPrx svc = factory.getScriptService();
        List<OriginalFile> scripts = svc.getScriptsByMimetype(LUT_MIMETYPE);
        Assert.assertNotNull(scripts);
        Assert.assertTrue(scripts.size() > 0);
        Iterator<OriginalFile> i = scripts.iterator();
        OriginalFile f;
        while (i.hasNext()) {
            f = i.next();
            Assert.assertNotNull(f);
            String mimetype = f.getMimetype().getValue();
            Assert.assertEquals(LUT_MIMETYPE, mimetype);
        }
        scripts = svc.getScriptsByMimetype(LUT_MIMETYPE);
        i = scripts.iterator();
        while (i.hasNext()) {
            f = i.next();
            Assert.assertNotNull(f);
            String mimetype = f.getMimetype().getValue();
            Assert.assertEquals(LUT_MIMETYPE, mimetype);
        }
    }
    
    /**
     * Tests the retrieval of the parameters associated to a script using the
     * <code>getParams</code> method.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     * @see #testGetScripts()
     */
    @Test
    public void testGetParams() throws Exception {
        IScriptPrx svc = factory.getScriptService();
        List<OriginalFile> scripts = svc.getScripts();
        Iterator<OriginalFile> i = scripts.iterator();
        OriginalFile f;
        JobParams params;
        while (i.hasNext()) {
            f = i.next();
            params = svc.getParams(f.getId().getValue());
            Assert.assertNotNull(params);
        }
    }

    /**
     * Tests to upload an official script by a user who is not an administrator,
     * this method uses the <code>uploadOfficialScript</code>.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testUploadOfficialScript() throws Exception {
        StringBuffer buf = new StringBuffer("");
        String[] values = { "a", "b", "c" };
        for (int i = 0; i < values.length; i++) {
            buf.append(values[i].charAt(0));
        }
        String folder = "officialTestFolder";
        IScriptPrx svc = factory.getScriptService();
        int n = svc.getScripts().size();
        try {
            svc.uploadOfficialScript(folder, buf.toString());
            Assert.fail("Only administrators can upload official script.");
        } catch (Exception e) {
        }
        Assert.assertEquals(n, svc.getScripts().size());
    }

    /**
     * Tests to upload an official script by a user who is an administrator,
     * this method uses the <code>uploadOfficialScript</code>.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testUploadOfficialScriptAsRoot() throws Exception {
        logRootIntoGroup();
        StringBuffer buf = new StringBuffer("");
        String[] values = { "a", "b", "c" };
        for (int i = 0; i < values.length; i++) {
            buf.append(values[i].charAt(0));
        }
        String folder = "officialTestFolder";
        IScriptPrx svc = factory.getScriptService();
        try {
            long id = svc.uploadOfficialScript(folder, buf.toString());
            Assert.assertTrue(id > 0);
        } catch (Exception e) {
        }
    }

    /**
     * Tests to upload a script, this method uses the <code>uploadScript</code>.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testUploadScript() throws Exception {
        StringBuffer buf = new StringBuffer("");
        String[] values = { "a", "b", "c" };
        for (int i = 0; i < values.length; i++) {
            buf.append(values[i].charAt(0));
        }
        String folder = "scriptTestFolder";
        IScriptPrx svc = factory.getScriptService();
        long id = svc.uploadScript(folder, buf.toString());
        Assert.assertTrue(id > 0);
    }

    /**
     * Tests to upload an official lut by a user who is an administrator,
     * this method uses the <code>uploadOfficialScript</code>.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testUploadOfficialLUTAsRoot() throws Exception {
        logRootIntoGroup();
        StringBuffer buf = new StringBuffer("");
        String[] values = { "a", "b", "c" };
        for (int i = 0; i < values.length; i++) {
            buf.append(values[i].charAt(0));
        }
        String uuid = UUID.randomUUID().toString();
        String folder = "officialTestFolder"+uuid+".lut";
        IScriptPrx svc = factory.getScriptService();
        List<OriginalFile> scripts = svc.getScriptsByMimetype(LUT_MIMETYPE);
        int n = scripts.size();
        try {
            long id = svc.uploadOfficialScript(folder, buf.toString());
            Assert.assertTrue(id > 0);
        } catch (Exception e) {
        }
        Assert.assertEquals(n+1, svc.getScriptsByMimetype(LUT_MIMETYPE).size());
    }

}
