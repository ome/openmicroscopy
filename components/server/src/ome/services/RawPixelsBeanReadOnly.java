/*
 *   Copyright 2006-2018 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services;

import ome.annotations.RolesAllowed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of the RawPixelsStore stateful service.
 *
 * @author <br>
 *         Josh Moore&nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de"> josh.moore@gmx.de</a>
 * @see RawPixelsBean
 */
@Transactional(readOnly = true)
public class RawPixelsBeanReadOnly extends RawPixelsBean {

    /** The logger for this particular class */
    private static Logger log = LoggerFactory.getLogger(RawPixelsBeanReadOnly.class);

    private static final long serialVersionUID = -5121611405890224414L;

    /**
     * default constructor
     */
    public RawPixelsBeanReadOnly() {
    }

    /**
     * overridden to allow Spring to set boolean
     */
    public RawPixelsBeanReadOnly(boolean checking, String omeroDataDir) {
        super(checking, omeroDataDir);
    }

    @RolesAllowed("user")
    @Override
    public void close() {
        /* omits save() */
        super.clean();
    }
}
