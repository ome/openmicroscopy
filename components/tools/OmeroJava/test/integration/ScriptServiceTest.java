/*
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

import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import omero.api.IScriptPrx;
import omero.api.RawFileStorePrx;
import omero.grid.JobParams;
import omero.model.IObject;
import omero.model.OriginalFile;
import omero.model.OriginalFileI;
import omero.sys.ParametersI;

import org.apache.commons.collections.CollectionUtils;
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
        Assert.assertEquals(values.size(), n);
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
        Assert.assertTrue(CollectionUtils.isNotEmpty(scripts));
        Iterator<OriginalFile> i = scripts.iterator();
        OriginalFile f;
        while (i.hasNext()) {
            f = i.next();
            Assert.assertNotNull(f);
            if (LUT_MIMETYPE.equals(f.getMimetype().getValue())) {
                Assert.fail("Lut should not be returned.");
            }
        }
        //do it twice since we had initially a bug in loading
        scripts = svc.getScripts();
        Assert.assertNotNull(scripts);
        Assert.assertTrue(CollectionUtils.isNotEmpty(scripts));
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
        Assert.assertTrue(CollectionUtils.isNotEmpty(scripts));
        Iterator<OriginalFile> i = scripts.iterator();
        OriginalFile f;
        while (i.hasNext()) {
            f = i.next();
            Assert.assertNotNull(f);
            String mimetype = f.getMimetype().getValue();
            Assert.assertEquals(mimetype, LUT_MIMETYPE);
        }
        scripts = svc.getScriptsByMimetype(LUT_MIMETYPE);
        i = scripts.iterator();
        while (i.hasNext()) {
            f = i.next();
            Assert.assertNotNull(f);
            String mimetype = f.getMimetype().getValue();
            Assert.assertEquals(mimetype, LUT_MIMETYPE);
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
        newUserAndGroup("rwr---");
        IScriptPrx svc = factory.getScriptService();
        List<OriginalFile> scripts = svc.getScripts();
        OriginalFile f = scripts.get(0);
        int n = svc.getScripts().size();
        //read the script. This is tested elsewhere
        String str = readScript(f);
        String folder = f.getName().getValue();
        try {
            svc.uploadOfficialScript(folder, str);
            Assert.fail("Only administrators can upload official script.");
        } catch (Exception e) {
        }
        Assert.assertEquals(svc.getScripts().size(), n);
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
        IScriptPrx svc = factory.getScriptService();
        List<OriginalFile> scripts = svc.getScripts();
        OriginalFile f = scripts.get(0);
        //read the script. This is tested elsewhere
        String str = readScript(f);
        String folder = f.getName().getValue();
        long id = svc.uploadOfficialScript(folder, str);
        Assert.assertTrue(id > 0);
        Assert.assertEquals(svc.getScripts().size(), scripts.size()+1);
        deleteScript(id);
    }

    /**
     * Tests to upload an official script by a user who is an administrator,
     * this method uses the <code>deleteScript</code>.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testDeleteScriptAsRoot() throws Exception {
        logRootIntoGroup();
        IScriptPrx svc = factory.getScriptService();
        List<OriginalFile> scripts = svc.getScripts();
        OriginalFile f = scripts.get(0);
        //read the script. This is tested elsewhere
        String str = readScript(f);
        String folder = f.getName().getValue();
        long id = svc.uploadOfficialScript(folder, str);
        deleteScript(id);
        Assert.assertEquals(svc.getScripts().size(), scripts.size());
        //Check that the entry has been removed from DB
        assertDoesNotExist(new OriginalFileI(id, false));
    }

    /**
     * Tests to upload a script, this method uses the <code>uploadScript</code>.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testUploadScript() throws Exception {
        IScriptPrx svc = factory.getScriptService();
        List<OriginalFile> scripts = svc.getScripts();
        OriginalFile f = scripts.get(0);
        //read the script. This is tested elsewhere
        String str = readScript(f);
        String folder = f.getName().getValue();
        long id = svc.uploadScript(folder, str);
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
        IScriptPrx svc = factory.getScriptService();
        List<OriginalFile> scripts = svc.getScriptsByMimetype(LUT_MIMETYPE);
        int n = scripts.size();
        OriginalFile f = scripts.get(0);
        String str = readScript(f);
        String folder = f.getName().getValue();
        long id = svc.uploadOfficialScript(folder, str);
        Assert.assertTrue(id > 0);
        Assert.assertEquals(svc.getScriptsByMimetype(LUT_MIMETYPE).size(),
                n+1);
        deleteScript(id);
    }

    /**
     * Delete the uploaded script.
     *
     * @param id The identifier of the script.
     * @throws Exception Thrown if an error occurred.
     */
    private void deleteScript(long id) throws Exception {
        IScriptPrx svc = factory.getScriptService();
        //currently the deleteScript does not delete the entry in the DB.
        svc.deleteScript(id);
    }


    /**
     * Reads the specified script as a string.
     *
     * @param f The script to read.
     * @return See above.
     * @throws Exception Thrown if an error occurred.
     */
    private String readScript(OriginalFile f) throws Exception {
        RawFileStorePrx store;
        byte[] values;
        store = factory.createRawFileStore();
        store.setFileId(f.getId().getValue());
        values = store.read(0, (int) f.getSize().getValue());
        store.close();
        return new String(values, StandardCharsets.UTF_8);
    }
}
