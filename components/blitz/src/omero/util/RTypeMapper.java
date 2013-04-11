/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

package omero.util;

import omero.ApiUsageException;
import omero.RCollection;
import omero.RMap;
import omero.util.IceMapper.ReturnMapping;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extension of {@link IceMapper} which guarantees that {@link RCollection} and
 * {@link RMap} instances are properly created.
 */
public class RTypeMapper extends IceMapper {

    private static Logger log = LoggerFactory.getLogger(RTypeMapper.class);

    public RTypeMapper(ReturnMapping mapping) {
        super(mapping);
    }


    @Override
    public Object findKeyTarget(Object current) {
        if (!(current instanceof String)) {
            throw new ome.conditions.ApiUsageException("Non-string key");
        }
        return current;
    }

    @Override
    public Object findCollectionTarget(Object current) {
        Object rv = super.findCollectionTarget(current);
        try {
            return toRType(rv);
        } catch (ApiUsageException aue) {
            throw new ome.conditions.ApiUsageException(aue.message);
        }
    }

}
