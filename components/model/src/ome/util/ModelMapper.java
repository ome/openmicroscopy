/*
 * ome.util.ModelMapper
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ome.conditions.InternalException;
import ome.model.IObject;
import ome.model.ModelBased;
import ome.model.meta.Event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0
 * @since 1.0
 */
public abstract class ModelMapper extends ContextFilter {

    protected final static Logger log = LoggerFactory.getLogger(ModelMapper.class);

    /**
     * TODO identity versus null mappings
     * 
     * @return a map from {@link IObject} classes {@link ModelBased} classes.
     */
    protected abstract Map c2c();

    protected Map model2target = new IdentityHashMap();

    public ModelBased map(Filterable source) {
        Filterable o = this.filter("MAPPING...", source);
        return (ModelBased) model2target.get(o);
    }

    public Collection map(Collection source) {
        Collection o = this.filter("MAPPING...", source);
        return (Collection) model2target.get(o);
    }

    public Map map(Map source) {
        Map o = this.filter("MAPPING...", source);
        return (Map) model2target.get(o);
    }

    @Override
    public Filterable filter(String fieldId, Filterable source) {
        Filterable o = super.filter(fieldId, source);
        ModelBased target = (ModelBased) findTarget(o);
        fillTarget(source, target);
        return o;
    }

    @Override
    public Collection filter(String fieldId, Collection source) {
        Collection o = super.filter(fieldId, source);
        Collection target = findCollection(o);
        fillCollection(source, target);
        return o;
    }

    @Override
    public Map filter(String fieldId, Map source) {
        Map o = super.filter(fieldId, source);
        Map target = findMap(o);
        fillMap(source, target);
        return o;
    }

    protected Class findClass(Class source) {
        return (Class) c2c().get(Utils.trueClass(source));
    }

    /**
     * extension point which subclasses can override to better map the
     * keys of maps.
     */
    public Object findKeyTarget(Object current) {
        return findTarget(current);
    }

    /**
     * extension point which subclasses can override to better map the
     * values of collections and maps.
     */
    public Object findCollectionTarget(Object current) {
        return findTarget(current);
    }

    /**
     * known immutables are return unchanged.
     * 
     * @param current
     * @return a possibly uninitialized object which will be finalized as the
     *         object graph is walked.
     */
    public Object findTarget(Object current) {

        // IMMUTABLES
        if (null == current || current instanceof Number
                || current instanceof String || current instanceof Boolean
                || current instanceof Timestamp || current instanceof Class) {
            return current;
        }

        Object target = model2target.get(current);
        if (null == target) {
            Class currentType = current.getClass();
            Class targetType = null;

            if (currentType.isArray()) {

                Class componentType = null;
                try {
                    int length = Array.getLength(current);
                    componentType = currentType.getComponentType();
                    target = Array.newInstance(componentType, length);
                    for (int i = 0; i < length; i++) {
                        Object currentValue = Array.get(current, i);
                        Object targetValue = this.filter("ARRAY", currentValue);
                        Array.set(target, i, targetValue);
                    }
                } catch (Exception e) {
                    log.error("Error creating new array of type "
                            + componentType, e);
                    throwOnNewInstanceException(current, componentType, e);
                }

            } else {
                targetType = findClass(currentType);

                if (null == targetType) {
                    throw new InternalException("Cannot handle type:" + current);
                }

                try {
                    target = targetType.newInstance();
                } catch (Exception e) {
                    log.error("Error creating new instance of target type"
                            + current, e);
                    throwOnNewInstanceException(current, targetType, e);
                }

            }
            model2target.put(current, target);
        }
        return target;
    }

    public Collection findCollection(Collection source) {
        if (source == null) {
            return null;
        }

        Collection target = (Collection) model2target.get(source);
        if (null == target) {
            if (Set.class.isAssignableFrom(source.getClass())) {
                target = new HashSet();
            } else if (List.class.isAssignableFrom(source.getClass())) {
                target = new ArrayList();
            } else {
                throw new RuntimeException("Unknown collection type: "
                        + source.getClass());
            }
            model2target.put(source, target);
        }
        return target;
    }

    public Map findMap(Map source) {
        if (source == null) {
            return null;
        }

        Map target = (Map) model2target.get(source);
        if (null == target) {
            try {
                target = source.getClass().newInstance();
                model2target.put(source, target);
            } catch (InstantiationException ie) {
                throw new RuntimeException(ie);
            } catch (IllegalAccessException iae) {
                throw new RuntimeException(iae);
            }
        }
        return target;
    }

    private void fillTarget(Filterable source, ModelBased target) {
        if (source != null && target != null) {
            target.copyObject((source), this);
        }
    }

    private void fillCollection(Collection source, Collection target) {
        if (source != null && target != null) {
            for (Iterator it = source.iterator(); it.hasNext();) {
                Object o = it.next();
                target.add(this.findCollectionTarget(o));
            }
        }
    }

    private void fillMap(Map source, Map target) {
        if (source != null && target != null) {
            for (Iterator it = source.keySet().iterator(); it.hasNext();) {
                Object o = it.next();
                target.put(findKeyTarget(o), findCollectionTarget(source.get(o)));
            }
        }
    }

    public Timestamp event2timestamp(Event event) {
        if (event == null) {
            return null;
        }
        if (!event.isLoaded()) {
            return null;
        }
        if (event.getTime() == null) {
            return null;
        }
        return event.getTime();
    }

    public int nullSafeInt(Integer i) {
        if (i == null) {
            return 0;
        }
        return i.intValue();
    }

    public long nullSafeLong(Long l) {
        if (l == null) {
            return 0;
        }
        return l.longValue();
    }

    public double nullSafeDouble(Double d) {
        if (d == null) {
            return 0.0;
        }
        return d.doubleValue();
    }

    public float nullSafeFloat(Float f) {
        if (f == null) {
            return 0.0F;
        }
        return f.floatValue();
    }

    // Helpers
    // =========================================================================

    private void throwOnNewInstanceException(Object current, Class targetType,
            Exception e) {
        throw new InternalException("Could not instantiate object of type "
                + targetType + " while trying to map " + current + "\n"
                + e.getMessage());
    }

    public static String stackAsString(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        Throwable cause = t.getCause();
        while (cause != null && cause != t) {
            cause.printStackTrace(pw);
            t = cause;
            cause = t.getCause();
        }
        pw.flush();
        pw.close();

        return sw.getBuffer().toString();
    }

}
