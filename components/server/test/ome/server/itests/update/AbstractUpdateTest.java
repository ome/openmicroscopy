/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests.update;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import ome.model.IObject;
import ome.server.itests.AbstractManagedContextTest;

public class AbstractUpdateTest extends AbstractManagedContextTest {

    protected <T extends IObject> boolean equalCollections(
            Collection<T> before, Collection<T> after) {
        Set<Long> beforeIds = new HashSet<Long>();
        for (IObject object : before) {
            beforeIds.add(object.getId());
        }

        Set<Long> afterIds = new HashSet<Long>();
        for (IObject object : after) {
            afterIds.add(object.getId());
        }

        return beforeIds.containsAll(afterIds);
    }

}
