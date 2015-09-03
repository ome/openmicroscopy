/*
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.util;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import ome.conditions.InternalException;
import ome.model.IObject;
import ome.model.internal.Details;

public class ShallowCopy {

    public <T extends IObject> T copy(T original) {
        T f = ShallowCopy.reflectiveNewInstance(original);
        SetValues set = new SetValues(f);
        original.acceptFilter(set);
        return f;
    }

    @SuppressWarnings("unchecked")
    public static <T extends IObject> T reflectiveNewInstance(T f) {
        Method m;
        T iobj;
        try {
            Class<T> c = (Class<T>) Utils.trueClass(f.getClass());
            iobj = c.newInstance();
        } catch (Exception e) {
            InternalException ie = new InternalException(e.getMessage());
            ie.setStackTrace(e.getStackTrace());
            throw ie;
        }

        return iobj;
    }

}

class SetValues implements Filter {

    private IObject target;

    public SetValues(IObject target) {
        this.target = target;
    }

    public Filterable filter(String fieldId, Filterable f) {
        if (f == null) {
            return null;
        }

        else if (f instanceof Enum) {
            target.putAt(fieldId, f);
        }

        else if (f instanceof ome.model.internal.Primitive) {
            target.putAt(fieldId, f);
        }

        else if (Details.class.isAssignableFrom(f.getClass())) {
            target.putAt(fieldId, ((Details) f).shallowCopy());
        }

        else if (IObject.class.isAssignableFrom(f.getClass())) {
            IObject old = (IObject) f;
            IObject iobj = ShallowCopy.reflectiveNewInstance(old);
            iobj.setId(old.getId());
            iobj.unload();
            target.putAt(fieldId, iobj);
        }

        else {
            throw new InternalException("Unknown filterable type:"
                    + f.getClass());
        }

        return f;

    }

    public Collection filter(String fieldId, Collection c) {
        target.putAt(fieldId, null);
        return c;
    }

    public Map filter(String fieldId, Map m) {
        target.putAt(fieldId, null);
        return m;
    }

    public Object filter(String fieldId, Object o) {

        if (o == null) {
            target.putAt(fieldId, null);
        }

        // TODO add Object[] filter(Object[]) method to Filterable
        else if (Object[].class.isAssignableFrom(o.getClass())) {
            target.putAt(fieldId, null);
        }

        else {
            target.putAt(fieldId, o);
        }
        return o;
    }

}

class StoreValues implements Filter {

    public Map values = new HashMap();

    public Filterable filter(String fieldId, Filterable f) {
        if (f == null) {
            return null;
        }
        else if (f instanceof Enum) {
            values.put(fieldId, f);
        }
        else if (Details.class.isAssignableFrom(f.getClass())) {
            values.put(fieldId, ((Details) f).shallowCopy());
        }

        else if (IObject.class.isAssignableFrom(f.getClass())) {
            IObject old = (IObject) f;
            IObject iobj = ShallowCopy.reflectiveNewInstance(old);
            iobj.setId(old.getId());
            iobj.unload();
            values.put(fieldId, iobj);
        }

        else {
            throw new InternalException("Unknown filterable type:"
                    + f.getClass());
        }

        return f;

    }

    public Collection filter(String fieldId, Collection c) {
        values.put(fieldId, null);
        return c;
    }

    public Map filter(String fieldId, Map m) {
        values.put(fieldId, null);
        return m;
    }

    public Object filter(String fieldId, Object o) {
        if (o == null) {
            values.put(fieldId, null);
        }
        values.put(fieldId, o);
        return o;
    }

}
