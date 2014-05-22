/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.security.auth;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.DirContextAdapter;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

/**
 * Specialized handling of all the attributes, multi or single valued.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4.2
 */
public class AttributeSet {

    Logger log = LoggerFactory.getLogger(AttributeSet.class);

    private final Map<String, String> singleProperties = new HashMap<String, String>();

    private final SetMultimap<String, String> multiProperties = HashMultimap.create();

    public AttributeSet(DirContextAdapter ctx) {
        NamingEnumeration<String> ids = ctx.getAttributes().getIDs();
        Attributes attrs = ctx.getAttributes();
        while (ids.hasMoreElements()) {
            String id = ids.nextElement();

            Attribute attr = attrs.get(id);
            String key = key(id);
            if (attr.size() > 1) {
                if (size(key) > 0) {
                    throw new IllegalStateException("Duplicate key: " + key);
                }
                Object[] objects = ctx.getObjectAttributes(id);
                for (Object object : objects) {
                    if (object == null) {
                        continue;
                    } else if (object instanceof String) {
                        multiProperties.put(key, (String) object);
                    } else if (object instanceof byte[]) {
                        try {
                            multiProperties.put(key, new String((byte[]) object));
                        } catch (Exception e) {
                            log.warn("Error trying to parse byte[] for {}: length={}",
                                    key, ((byte[]) object).length);
                        }
                    } else {
                        multiProperties.put(key, object.toString());
                    }
                }
            } else {
                if (size(key) > 0) {
                    throw new IllegalStateException("Duplicate key: " + key);
                }
                singleProperties.put(key, ctx.getObjectAttribute(id).toString());
            }
        }
    }

    public String key(String id) {
        return id.toLowerCase();
    }

    public String getFirst(String id) {
        String key = key(id);
        if (singleProperties.containsKey(key)) {
            return singleProperties.get(key);
        } else {
            if (multiProperties.containsKey(key)) {
                return multiProperties.get(key).iterator().next();
            }
        }
        return null;
    }

    public Set<String> getAll(String id) {
        String key = key(id);
        Set<String> rv = new HashSet<String>();
        if (singleProperties.containsKey(key)) {
            rv.add(singleProperties.get(key));
        } else {
            if (multiProperties.containsKey(key)) {
                rv.addAll(multiProperties.get(key));
            }
        }
        return rv;
    }

    public int size(String id) {
        String key = key(id);
        if (singleProperties.containsKey(key)) {
            return 1;
        } else {
            if (multiProperties.containsKey(key)) {
                return multiProperties.get(key).size();
            }
        }
        return 0;
    }

    public String put(String id, String value) {
        String key = key(id);
        if (size(key) > 0) {
            throw new IllegalStateException("Duplicate key: " + key);
        }
        return singleProperties.put(key, value);
    }

}
