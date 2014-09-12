/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.utests;

import ome.api.IQuery;
import ome.api.ITypes;
import ome.api.IUpdate;
import ome.services.procs.Processor;
import ome.services.procs.ProcessorSkeleton;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta2
 */
@Test(groups = "jobs")
public class ProcessorSkeletonTest extends MockObjectTestCase {

    private Processor processor;

    private Mock mockQuery, mockUpdate, mockTypes;
    private IQuery query;
    private IUpdate update;
    private ITypes types;

    @Override
    @BeforeMethod
    protected void setUp() throws Exception {
        super.setUp();
        mockQuery = mock(IQuery.class);
        mockUpdate = mock(IUpdate.class);
        mockTypes = mock(ITypes.class);
        query = (IQuery) mockQuery.proxy();
        update = (IUpdate) mockUpdate.proxy();
        types = (ITypes) mockTypes.proxy();

        ProcessorSkeleton skel = new ProcessorSkeleton();
        skel.setQueryService(query);
        skel.setUpdateService(update);
        skel.setTypesService(types);
        processor = skel;
    }

    @Test
    public void testNoJobFound() throws Exception {
        mockQuery.expects(once()).method("find");
        // if iquery doesn't return anything, it has to return null
        assertTrue(null == processor.process(1L));
    }

}
