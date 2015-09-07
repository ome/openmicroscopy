/*
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.server.itests.details;

import ome.conditions.SecurityViolation;
import ome.conditions.ValidationException;
import ome.model.core.Image;
import ome.model.meta.ExternalInfo;
import ome.parameters.Filter;
import ome.parameters.Parameters;
import ome.server.itests.AbstractManagedContextTest;

import org.testng.annotations.Test;

@Test(groups = {"ticket:371","broken"})
public class ExternalInfoTest extends AbstractManagedContextTest {

    Image i;

    ExternalInfo info;

    @Test
    public void testNullIsOk() throws Exception {

        loginNewUser();

        i = createImage();
        assertNull(i.getDetails().getExternalInfo());

        i = iUpdate.saveAndReturnObject(i);

        assertNull(i.getDetails().getExternalInfo());

    }

    @Test(groups = "MAYCHANGE")
    public void testLSIDCurrentlyRequired() throws Exception {

        loginNewUser();

        createImageAndInfo();
        assertNotNull(i.getDetails().getExternalInfo());

        try {
            i = createImage();
            info = createInfo(i);
            info.setLsid(null); // SETTING TO NULL
            i.getDetails().setExternalInfo(info);
            iUpdate.saveAndReturnObject(i);
            fail("invalid!");
        } catch (ValidationException ve) {
            // ok.
        }
    }

    @Test
    public void testImmutableAlsoForRoot() throws Exception {

        loginRoot();

        try {
            createImageAndInfo();
            i.getDetails().setExternalInfo(null); // null
            iUpdate.saveObject(i);
            fail("secvio!");
        } catch (SecurityViolation sv) {
            // ok
        }

        try {
            createImageAndInfo();
            ExternalInfo different = createInfo(i); // unmanaged
            different.setLsid("different");
            i.getDetails().setExternalInfo(different);
            iUpdate.saveObject(i);
            fail("secvio!");
        } catch (SecurityViolation sv) {
            // ok
        }

        try {
            createImageAndInfo();
            ExternalInfo different = iQuery.findByQuery( // managed
                    "from ExternalInfo where id != :id", new Parameters(
                            new Filter().page(0, 1)).addId(info.getId()));
            i.getDetails().setExternalInfo(different);
            iUpdate.saveObject(i);
            fail("secvio!");
        } catch (SecurityViolation sv) {
            // ok
        }

        createImageAndInfo();
        info.setLsid("modified"); // modified
        i.getDetails().setExternalInfo(info);
        iUpdate.saveObject(i);
        info = iQuery.get(info.getClass(), info.getId());
        assertTrue(info.getLsid().equals(LSID));
    }

    @Test
    public void testCascadeDeletes() throws Exception {

        loginRoot();

        createImageAndInfo();

        iUpdate.deleteObject(i);
        info = iQuery.find(info.getClass(), info.getId());
        assertNull(info);

    }

    @Test
    public void testUnique() throws Exception {

        loginRoot();

        createImageAndInfo();

        Image i2 = createImage();
        i2.getDetails().setExternalInfo(info);
        try {
            iUpdate.saveAndReturnObject(i2);
            fail("invalid!");
        } catch (ValidationException ve) {
            // ok
        }
    }

    @Test
    public void testPermissions() throws Exception {

    }

    // ~ Heleprs
    // =========================================================================

    private void createImageAndInfo() {
        i = createImage();
        i = iUpdate.saveAndReturnObject(i);
        info = createInfo(i);
        i.getDetails().setExternalInfo(info);
        i = iUpdate.saveAndReturnObject(i);
        info = i.getDetails().getExternalInfo();
    }

    private Image createImage() {
        Image i = new Image();
        i.setName("ticket:371");
        return i;
    }

    public final static String LSID = "urn:lsid:example.com:image:1";

    private ExternalInfo createInfo(Image i) {
        ExternalInfo info = new ExternalInfo();
        info.setEntityType(i.getClass().getName());
        info.setEntityId(i.getId());
        info.setLsid(LSID);
        return info;
    }
}
