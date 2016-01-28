/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.util;

import java.util.Map;

import org.springframework.beans.factory.InitializingBean;

/**
 * Simple bean which calls "map.put(key, object)" on
 * {@link InitializingBean#afterPropertiesSet()}. Must adhere to the
 * {@link Map} interface for nulls, etc.
 */
public class MapPut implements InitializingBean {

    private Map<String, Object> map;

    private String key;

    private Object object;

    public void afterPropertiesSet() throws Exception {
        map.put(key, object);
    }

    public void setMap(Map<String, Object> map) {
        this.map = map;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setObject(Object object) {
        this.object = object;
    }

}
