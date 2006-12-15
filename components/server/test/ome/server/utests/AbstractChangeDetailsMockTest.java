/*
 * ome.server.utests.AbstractChangeDetailsMockTest
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.utests;

// Java imports

// Third-party libraries
import org.springframework.orm.hibernate3.HibernateOperations;
import org.testng.annotations.*;

// Application-internal dependencies
import ome.model.core.Image;
import ome.model.meta.Event;
import ome.tools.hibernate.UpdateFilter;

/**
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since Omero 2.0
 */
@Test(groups = { "ticket:52", "security", "broken" })
public class AbstractChangeDetailsMockTest extends AbstractLoginMockTest {

    protected Image i;

    // Factors:
    // 1. new or managed
    // 2. root or user
    // 3. change to user or root
    // 5. TODO even laterer: allowing changes based on group privileges.

    @Configuration(afterTestMethod = true)
    protected void tearDown() throws Exception {
        super.tearDown();
        i = null;
    }

    boolean _MANAGED = false;

    boolean _NEW = true;

    boolean _ROOT = true;

    boolean _USER = false;

    /** this method should be called first to properly setup CurrentDetails */
    protected void userImageChmod(boolean root_p, boolean new_p, Long targetId) {
        userImage(root_p, new_p);
        chown(i, targetId);

    }

    /** this method should be called first to properly setup CurrentDetails */
    protected void userImageChgrp(boolean root_p, boolean new_p, Long targetId) {
        userImage(root_p, new_p);
        chgrp(i, targetId);

    }

    private void userImage(boolean root_p, boolean new_p) {
        if (root_p) {
            rootLogin();
        } else {
            userLogin();
        }

        if (new_p) {
            i = new Image();
        } else {
            i = managedImage();
        }
    }
}
