/*
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.blitz.test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jmock.core.Invocation;
import org.jmock.core.Stub;

public class FakeAdapter implements Stub {

    private final Map<String, Object> servants = new HashMap<String, Object>();

    public StringBuffer describeTo(StringBuffer arg0) {
        return arg0;
    }

    public Object invoke(Invocation arg0) throws Throwable {
        if (arg0.invokedMethod.getName().equals("add")) {
            return ice_add(arg0.parameterValues);
        } else if (arg0.invokedMethod.getName().equals("createDirectProxy")) {
            return ice_find(arg0.parameterValues);
        } else if (arg0.invokedMethod.getName().equals("find")) {
            return ice_find(arg0.parameterValues);
        } else {
            throw new RuntimeException("Unknown method: "
                    + arg0.invokedMethod);
        }
    }

    private Object ice_add(List parameterValues) {
        Ice.Object servant = (Ice.Object) parameterValues.get(0);
        Ice.Identity id = (Ice.Identity) parameterValues.get(1);
        String key = Ice.Util.identityToString(id);
        servants.put(key, servant);
        return null;
    }

    private Object ice_createDirectPrixy(List parameterValues) {
        Ice.Identity id = (Ice.Identity) parameterValues.get(0);
        throw new RuntimeException("NYI");
    }

    private Object ice_find(List parameterValues) {
        Ice.Identity id = (Ice.Identity) parameterValues.get(0);
        String key = Ice.Util.identityToString(id);
        return servants.get(key);
    }
}
