/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.util.cache;

import java.io.Serializable;
import java.util.List;

import ome.model.IObject;

public interface Cache<K extends IObject, V extends Serializable> {

    public abstract Class<K> getType();

    @SuppressWarnings("unchecked")
    public abstract List<Long> getKeys();

    @SuppressWarnings("unchecked")
    public abstract V get(long id);

    public abstract void put(long id, V s);

    public abstract void remove(long id);

    @SuppressWarnings("unchecked")
    public abstract void reap();

}