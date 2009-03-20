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
public interface Registry {

    /**
     * Returns an active {@link IceGrid.QueryPrx} or null if none is
     * available.
     */
    public abstract IceGrid.QueryPrx getGridQuery();

    /**
     * Create a new {@link IceGrid.AdminSessionPrx} with the
     * {@link IceGrid.RegistryPrx}. Consumers are required to properly
     * {@link IceGrid.AdminSessionPrx#destroy()} the returned session.
     * 
     * @return
     * @throws PermissionDeniedException
     */
    public abstract IceGrid.AdminSessionPrx getAdminSession()
            throws PermissionDeniedException;

    public abstract void addObject(Ice.ObjectPrx obj) throws Exception;

    public abstract void removeObject(Ice.Identity id) throws Exception;

    public abstract boolean removeObjectSafely(Ice.Identity id);

    /**
     * Returns all found cluster nodes or null if something goes wrong
     * during lookup (null {@link IceGrid.QueryPrx} for example)
     */
    public abstract ClusterNodePrx[] lookupClusterNodes();
    
    public class Impl implements Registry {

        private final static Log log = LogFactory.getLog(Registry.class);

        private final Ice.Communicator ic;

        public Impl(Ice.Communicator ic) {
            this.ic = ic;
        }

        /* (non-Javadoc)
         * @see ome.services.blitz.fire.T#getGridQuery()
         */
        public IceGrid.QueryPrx getGridQuery() {
            try {
                Ice.ObjectPrx objectPrx = ic.stringToProxy("IceGrid/Query");
                IceGrid.QueryPrx query = IceGrid.QueryPrxHelper
                        .checkedCast(objectPrx);
                return query;
            } catch (Exception e) {
                log.warn("Could not find IceGrid/Query: " + e);
                return null;
            }
        }

        /* (non-Javadoc)
         * @see ome.services.blitz.fire.T#getAdminSession()
         */
        public IceGrid.AdminSessionPrx getAdminSession()
                throws PermissionDeniedException {
            Ice.ObjectPrx objectPrx = ic.stringToProxy("IceGrid/Registry");
            IceGrid.RegistryPrx reg = IceGrid.RegistryPrxHelper
                    .checkedCast(objectPrx);
            IceGrid.AdminSessionPrx session = reg
                    .createAdminSession("null", "");
            return session;
        }

        /* (non-Javadoc)
         * @see ome.services.blitz.fire.T#addObject(Ice.ObjectPrx)
         */
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

        /* (non-Javadoc)
         * @see ome.services.blitz.fire.T#removeObject(Ice.Identity)
         */
        public void removeObject(Ice.Identity id) throws Exception {
            IceGrid.AdminSessionPrx session = getAdminSession();
            try {
                session.getAdmin().removeObject(id);
                log.info("Removed " + ic.identityToString(id)
                        + " from registry");
            } finally {
                session.destroy();
            }
        }

        /* (non-Javadoc)
         * @see ome.services.blitz.fire.T#removeObjectSafely(Ice.Identity)
         */
        public boolean removeObjectSafely(Ice.Identity id) {
            try {
                removeObject(id);
                return true;
            } catch (IceGrid.ObjectNotRegisteredException onre) {
                log.debug(Ice.Util.identityToString(id) + " not registered");
            } catch (Exception e) {
                log.error("Failed to remove registry object "
                        + Ice.Util.identityToString(id), e);
            }
            return false;
        }

        /* (non-Javadoc)
         * @see ome.services.blitz.fire.T#lookupClusterNodes()
         */
        public ClusterNodePrx[] lookupClusterNodes() {
            IceGrid.QueryPrx query = getGridQuery();
            if (query == null) {
                return null; // EARLY EXIT
            }
            try {
                Ice.ObjectPrx[] candidates = null;
                candidates = query
                        .findAllObjectsByType("::omero::internal::ClusterNode");
                ClusterNodePrx[] nodes = new ClusterNodePrx[candidates.length];
                for (int i = 0; i < nodes.length; i++) {
                    nodes[i] = ClusterNodePrxHelper
                            .uncheckedCast(candidates[i]);
                }
                log.info("Found " + nodes.length + " cluster node(s) : "
                        + Arrays.toString(nodes));
                return nodes;
            } catch (Exception e) {
                log.warn("Could not query cluster nodes " + e);
                return null;
            }
        }
    }
}