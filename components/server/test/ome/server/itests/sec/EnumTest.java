/*
 *   $Id$
 *
 *   Copyright 2006-2014 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests.sec;

import java.util.UUID;

import ome.api.ITypes;
import ome.model.core.Pixels;
import ome.model.enums.DimensionOrder;
import ome.model.enums.EventType;
import ome.model.enums.Format;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.server.itests.AbstractManagedContextTest;
import ome.testing.ObjectFactory;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test(groups = { "ticket:140", "ticket:145", "ticket:226", "security", "filter" })
public class EnumTest extends AbstractManagedContextTest {

    Experimenter e = new Experimenter();

    @BeforeClass
    public void createData() throws Exception {
        setUp();

        loginRoot();

        String gid = uuid();
        ExperimenterGroup g = new ExperimenterGroup();
        g.setName(gid);
        g.setLdap(false);
        iAdmin.createGroup(g);

        e = new Experimenter();
        e.setOmeName(UUID.randomUUID().toString());
        e.setFirstName("enums");
        e.setLastName("enums");
        e.setLdap(false);
        e = factory.getAdminService().getExperimenter(
                factory.getAdminService().createUser(e, gid));

        tearDown();
    }

    @Test
    public void testEnumsAreReloaded() throws Exception {

        loginUser(e.getOmeName());

        DimensionOrder test = new DimensionOrder();
        test.setValue("XYZCT");
        factory.getUpdateService().saveObject(test);

    }

    @Test
    public void testEvenWhenInAGraph() throws Exception {

        loginUser(e.getOmeName());

        Pixels test = ObjectFactory.createPixelGraph(null);
        factory.getUpdateService().saveObject(test.getImage());

    }

    @Test(groups = "ticket:226")
    public void testStringConstructor() throws Exception {

        loginUser(e.getOmeName());

        EventType type = new EventType(uuid());
        factory.getTypesService().createEnumeration(type);
    }
    
    @Test(groups = "ticket:1204")
    public void testFormatsAreReadable() {
        
        ITypes types = factory.getTypesService();
        Experimenter e = loginNewUser();
        
        String uuid = uuid();
        assertWorldReadable(types.createEnumeration(new Format(uuid)));
        
        Format f = types.getEnumeration(Format.class, "image/png");
        assertNotNull(f);
        
    }

}