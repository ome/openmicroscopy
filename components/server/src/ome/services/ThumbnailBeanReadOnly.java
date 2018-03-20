/*
 *   Copyright 2006-2018 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services;

import java.util.Map;
import java.util.Set;

import ome.annotations.RolesAllowed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

/**
 * Provides methods for directly querying object graphs. This read-only variant of the service
 * does not support rendering engine lazy object creation where rendering settings are missing.
 *
 * @author Chris Allan &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:callan@blackcat.ca">callan@blackcat.ca</a>
 * @see ThumbnailBean
 */
@Transactional(readOnly = true)
public class ThumbnailBeanReadOnly extends ThumbnailBean {

    private static final long serialVersionUID = 7297115245954897138L;

    /** The logger for this class. */
    private transient static Logger log = LoggerFactory.getLogger(ThumbnailBeanReadOnly.class);

    /**
     * overridden to allow Spring to set boolean
     */
    public ThumbnailBeanReadOnly(boolean checking) {
        super(checking);
    }

    /*
     * (non-Javadoc)
     *
     * @see ome.api.ThumbnailStore#setPixelsId(long)
     */
    @RolesAllowed("user")
    @Override
    public boolean setPixelsId(long id)
    {
        return super.setPixelsId(id);
    }

    @RolesAllowed("user")
    @Override
    public Map<Long, byte[]> getThumbnailByLongestSideSet(Integer size, Set<Long> pixelsIds)
    {
        return super.getThumbnailByLongestSideSet(size, pixelsIds);
    }

    /*
     * (non-Javadoc)
     *
     * @see ome.api.ThumbnailStore#getThumbnail(ome.model.core.Pixels,
     *      ome.model.display.RenderingDef, java.lang.Integer,
     *      java.lang.Integer)
     */
    @RolesAllowed("user")
    @Override
    public byte[] getThumbnail(Integer sizeX, Integer sizeY) {
        return super.getThumbnail(sizeX, sizeY);
    }

    /*
     * (non-Javadoc)
     *
     * @see ome.api.ThumbnailStore#getThumbnailByLongestSide(ome.model.core.Pixels,
     *      ome.model.display.RenderingDef, java.lang.Integer)
     */
    @RolesAllowed("user")
    @Override
    public byte[] getThumbnailByLongestSide(Integer size) {
        return super.getThumbnailByLongestSide(size);
    }
}
