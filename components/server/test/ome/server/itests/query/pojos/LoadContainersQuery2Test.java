/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests.query.pojos;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import ome.model.annotations.TextAnnotation;
import ome.model.containers.Dataset;
import ome.model.core.Image;
import ome.parameters.Parameters;
import ome.server.itests.AbstractManagedContextTest;
import ome.services.query.PojosLoadHierarchyQueryDefinition;
import ome.util.builders.PojoOptions;

import org.testng.annotations.Test;

/**
 * This class is marked "2" since because the other version is failing due to
 * issues with CreatePojosFixture
 * 
 * @author josh
 * 
 */
@Test
public class LoadContainersQuery2Test extends AbstractManagedContextTest {
    PojosLoadHierarchyQueryDefinition q;

    List list;

    List level2cg;

    List level2p;

    List level1c;

    List level1ds;

    List level0img;

    PojoOptions po10000;

    Parameters filterForUser;

    Parameters noFilter;

    public void testQueryReturnsCounts() throws Exception {
        Dataset d = new Dataset("name");
        Image i = new Image("name");
        TextAnnotation t = new TextAnnotation();
        t.setName("");
        t.setTextValue("t");
        d.linkImage(i);
        d.linkAnnotation(t);
        d = iUpdate.saveAndReturnObject(d);

        long self = iAdmin.getEventContext().getCurrentUserId();

        Set<Dataset> ds = iPojos.loadContainerHierarchy(Dataset.class,
                Collections.singleton(d.getId()), null);
        d = ds.iterator().next();
        assertNotNull(d.getAnnotationLinksCountPerOwner());
        assertTrue(d.getAnnotationLinksCountPerOwner().get(self).longValue() == 1L);
    }
}
