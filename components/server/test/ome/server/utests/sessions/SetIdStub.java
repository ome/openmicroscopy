/*
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.server.utests.sessions;

import ome.model.IObject;

import org.jmock.core.Invocation;
import org.jmock.core.Stub;

// TODO factor out to ome.testing
class SetIdStub implements Stub {

    long id;

    SetIdStub(long id) {
        this.id = id;
    }

    public Object invoke(Invocation arg0) throws Throwable {
        IObject obj = (IObject) arg0.parameterValues.get(0);
        obj.setId(id);
        return obj;
    }

    public StringBuffer describeTo(StringBuffer arg0) {
        return arg0.append(" returns session with id ");
    }

}
