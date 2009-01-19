/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.fire;

import java.util.Arrays;

import omero.internal.ClusterNodePrx;
import omero.internal.ClusterNodePrxHelper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import IceGrid.PermissionDeniedException;

/**
 * Helper class which makes the {@link IceGrid.RegistryPrx} available within
 * OmeroBlitz. Responsible for properly authenticating to
 * IceGrid.Registry.AdminPermissionsVerifier.
 * 
 *@since 4.0
 */
public class Registry {

    private final static Log log = LogFactory.getLog(Registry.class);

    private final Ice.Communicator ic;

    public Registry(Ice.Communicator ic) {
        this.ic = ic;
    }

    public IceGrid.QueryPrx getGridQuery() {
        Ice.ObjectPrx objectPrx = ic.stringToProxy("IceGrid/Query");
        IceGrid.QueryPrx query = IceGrid.QueryPrxHelper.checkedCast(objectPrx);
        return query;
    }

    /**
     * Create a new {@link IceGrid.AdminSessionPrx} with the {@link IceGrid.RegistryPrx}.
     * Consumers are required to properly {@link IceGrid.AdminSessionPrx#destroy()} the 
     * returned session.
     *
     * @return
     * @throws PermissionDeniedException
     */
    public IceGrid.AdminSessionPrx getAdminSession()
            throws PermissionDeniedException {
        Ice.ObjectPrx objectPrx = ic.stringToProxy("IceGrid/Registry");
        IceGrid.RegistryPrx reg = IceGrid.RegistryPrxHelper
                .checkedCast(objectPrx);
        IceGrid.AdminSessionPrx session = reg.createAdminSession("null", "");
        return session;
    }

    public void addObject(Ice.ObjectPrx obj) throws Exception {
        IceGrid.AdminSessionPrx session = getAdminSession();
        try {
            session.getAdmin().addObject(obj);
            log.info("Added " + ic.identityToString(obj.ice_getIdentity())
                    + " to registry");
        } finally {
            session.destroy();
        }
    }

    public void removeObject(Ice.Identity id) throws Exception {
        IceGrid.AdminSessionPrx session = getAdminSession();
        try {
            session.getAdmin().removeObject(id);
            log.info("Removed " + ic.identityToString(id) + " from registry");
        } finally {
            session.destroy();
        }
    }

    public void removeObjectSafely(Ice.Identity id) {
        try {
            removeObject(id);
        } catch (Exception e) {
            log.error("Failed to remove registry object "
                    + Ice.Util.identityToString(id), e);
        }
    }
    
    public ClusterNodePrx[] lookupClusterNodes() {
        Ice.ObjectPrx[] candidates = getGridQuery().findAllObjectsByType(
                "::omero::internal::ClusterNode");
        ClusterNodePrx[] nodes = new ClusterNodePrx[candidates.length];
        for (int i = 0; i < nodes.length; i++) {
            nodes[i] = ClusterNodePrxHelper.uncheckedCast(candidates[i]);
        }
        log.info("Found " + nodes.length + " cluster node(s) : "
                + Arrays.toString(nodes));
        return nodes;
    }
}