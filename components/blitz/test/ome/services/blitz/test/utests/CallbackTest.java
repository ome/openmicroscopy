/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.blitz.test.utests;

import java.util.List;

import ome.api.IQuery;
import ome.services.blitz.util.IceMethodInvoker;
import ome.services.throttling.Callback;
import omero.api.AMD_IQuery_findAllByQuery;
import omero.model.IObject;
import omero.util.IceMapper;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class CallbackTest extends MockObjectTestCase {

    Mock mock;
    IQuery query;
    IceMethodInvoker invoker;
    Ice.Current current;

    @BeforeMethod
    public void init() {
        mock = mock(IQuery.class);
        query = (IQuery) mock.proxy();
        invoker = new IceMethodInvoker(IQuery.class, null);
        current = new Ice.Current();
        current.operation = "findAllByQuery";
    }

    @Test
    public void testVisibilityOfCalbackMethods() throws Exception {
        _AMD_IQuery_findAllByQuery_expectResponse amd = new _AMD_IQuery_findAllByQuery_expectResponse();
        Callback cb = new Callback(query, invoker, new IceMapper(), amd,
                current, "query", null);
        mock.expects(once()).method("findAllByQuery");
        cb.run(null);
        assertTrue(amd.response);
    }

    private static class _AMD_IQuery_findAllByQuery_expectResponse implements
            AMD_IQuery_findAllByQuery {

        boolean response = false;

        public void ice_exception(Exception ex) {
            throw new RuntimeException("Exception thrown: ", ex);
        }

        public void ice_response(List<IObject> __ret) {
            response = true;
        }

    }
}