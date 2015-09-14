/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.fire;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

//import ome.services.blitz.repo.InternalRepositoryI;
import omero.api.ServiceFactoryPrx;
import omero.grid.ClusterNodePrx;
import omero.grid.ClusterNodePrxHelper;
import omero.grid.InternalRepositoryPrx;
import omero.grid.InternalRepositoryPrxHelper;
import omero.grid.ProcessorPrx;
import omero.grid.ProcessorPrxHelper;
import omero.grid.TablesPrx;
import omero.grid.TablesPrxHelper;
import omero.grid._InternalRepositoryDisp;
import omero.grid._ClusterNodeDisp;
import omero.grid._ProcessorDisp;
import omero.grid._TablesDisp;
import omero.grid.monitors.MonitorServerPrx;
import omero.grid.monitors.MonitorServerPrxHelper;
import omero.grid.monitors._MonitorServerDisp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Glacier2.SessionManagerPrx;
import Glacier2.SessionPrx;
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
     * Try to return a ServiceFactory from the grid.
     * 
     * Try a number of times then give up and raise the last exception returned.
     * This method will only work internally to the grid, i.e. behind the
     * Glacier2 firewall. It is intended for internal servers to be able to
     * create sessions for accessing the database.
     * 
     *<pre>
     * communicator := Ice.Communicator used to find the registry
     * user         := Username which should have a session created
     * group        := Group into which the session should be logged
     * retries      := Number of session creation retries before throwing
     * interval     := Seconds between retries
     * client_uuid  := Uuid of the client which should be used
     * </pre>
     */
    ServiceFactoryPrx getInternalServiceFactory(String user, String group,
            int retries, int interval, String client_uuid) throws Exception;

    /**
     * Returns an active {@link IceGrid.QueryPrx} or null if none is available.
     */
    public abstract IceGrid.QueryPrx getGridQuery();

    /**
     * Create a new {@link IceGrid.AdminSessionPrx} with the
     * {@link IceGrid.RegistryPrx}. Consumers are required to properly
     * {@link IceGrid.AdminSessionPrx#destroy()} the returned session.
     * 
     * @return See above.
     * @throws PermissionDeniedException
     */
    public abstract IceGrid.AdminSessionPrx getAdminSession()
            throws PermissionDeniedException;

    public abstract void addObject(Ice.ObjectPrx obj) throws Exception;

    public abstract void removeObject(Ice.Identity id) throws Exception;

    public abstract boolean removeObjectSafely(Ice.Identity id);

    /**
     * Returns all found cluster nodes or null if something goes wrong during
     * lookup (null {@link IceGrid.QueryPrx} for example)
     */
    public abstract ClusterNodePrx[] lookupClusterNodes();

    public abstract ProcessorPrx[] lookupProcessors();

    public abstract InternalRepositoryPrx[] lookupRepositories();

    public abstract TablesPrx[] lookupTables();
    
    public abstract MonitorServerPrx[] lookupMonitorServers();

    public class Impl implements Registry {

        private final static Logger log = LoggerFactory.getLogger(Registry.class);

        private final Ice.Communicator ic;

        public Impl(Ice.Communicator ic) {
            this.ic = ic;
        }

        public ServiceFactoryPrx getInternalServiceFactory(String user,
                String group, int retries, int interval, String client_uuid)
                throws Exception {

            int tryCount = 0;
            Exception excpt = null;
            Ice.ObjectPrx prx = ic.stringToProxy("IceGrid/Query");
            IceGrid.QueryPrx query = IceGrid.QueryPrxHelper.checkedCast(prx);

            if (client_uuid == null || client_uuid == "") {
                client_uuid = UUID.randomUUID().toString();
            }

            while (tryCount < retries) {

                try {
                    Map<String, String> ctx = new HashMap<String, String>();
                    ctx.put("omero.client.uuid", client_uuid);
                    prx = query
                            .findAllObjectsByType("::Glacier2::SessionManager")[0];
                    SessionManagerPrx blitz = Glacier2.SessionManagerPrxHelper
                            .checkedCast(prx);
                    SessionPrx sf = blitz.create(user, null, ctx);
                    // Group currently unused.
                    return omero.api.ServiceFactoryPrxHelper.checkedCast(sf);
                } catch (Ice.ObjectAdapterDeactivatedException oade) {
                    // Server is going down. wait an interval and this may have
                    // been shutdown, too.
                    excpt = oade;
                } catch (Exception e) {
                    log.info("Failed to get session on attempt " + tryCount);
                    tryCount += 1;
                    excpt = e;
                }

                tryCount += 1;

                try {
                    Thread.sleep(interval * 1000);
                } catch (InterruptedException ie) {
                    // pass;
                }

            }

            log.warn("Failed to get internal service factory", excpt);
            throw excpt;

        }

        /*
         * (non-Javadoc)
         * 
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

        /*
         * (non-Javadoc)
         * 
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

        /*
         * (non-Javadoc)
         * 
         * @see ome.services.blitz.fire.T#addObject(Ice.ObjectPrx)
         */
        public void addObject(Ice.ObjectPrx obj) throws Exception {
            IceGrid.AdminSessionPrx session = getAdminSession();
            IceGrid.AdminPrx admin = session.getAdmin();
            String str = ic.identityToString(obj.ice_getIdentity());
            try {
                admin.addObject(obj);
                log.info("Added " + str 
                        + " to registry");
            } catch (IceGrid.ObjectExistsException e) {
                admin.updateObject(obj);
                log.info("Updated " + str + " in registry");
            } finally {
                session.destroy();
            }
        }

        /*
         * (non-Javadoc)
         * 
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

        /*
         * (non-Javadoc)
         * 
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

        public ClusterNodePrx[] lookupClusterNodes() {
            IceGrid.QueryPrx query = getGridQuery();
            if (query == null) {
                return null; // EARLY EXIT
            }
            try {
                Ice.ObjectPrx[] candidates = null;
                candidates = query.findAllObjectsByType(_ClusterNodeDisp
                        .ice_staticId());
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

        public ProcessorPrx[] lookupProcessors() {
            IceGrid.QueryPrx query = getGridQuery();
            if (query == null) {
                return null; // EARLY EXIT
            }
            try {
                Ice.ObjectPrx[] candidates = null;
                candidates = query.findAllObjectsByType(_ProcessorDisp
                        .ice_staticId());
                ProcessorPrx[] procs = new ProcessorPrx[candidates.length];
                for (int i = 0; i < procs.length; i++) {
                    procs[i] = ProcessorPrxHelper.uncheckedCast(candidates[i]);
                }
                log.info("Found " + procs.length + " processor(s) : "
                        + Arrays.toString(procs));
                return procs;
            } catch (Exception e) {
                log.warn("Could not query processors " + e);
                return null;
            }
        }

        public TablesPrx[] lookupTables() {
            IceGrid.QueryPrx query = getGridQuery();
            if (query == null) {
                return null; // EARLY EXIT
            }
            try {
                Ice.ObjectPrx[] candidates = null;
                candidates = query.findAllObjectsByType(_TablesDisp.ice_staticId());
                TablesPrx[] tables = new TablesPrx[candidates.length];
                for (int i = 0; i < tables.length; i++) {
                    tables[i] = TablesPrxHelper.uncheckedCast(candidates[i]);
                }
                log.info("Found " + tables.length + " table services(s) : "
                        + Arrays.toString(tables));
                return tables;
            } catch (Exception e) {
                log.warn("Could not query tables " + e);
                return null;
            }
        }

        public InternalRepositoryPrx[] lookupRepositories() {
            IceGrid.QueryPrx query = getGridQuery();
            if (query == null) {
                return null; // EARLY EXIT
            }
            try {
                Ice.ObjectPrx[] candidates = null;
                candidates = query.findAllObjectsByType(_InternalRepositoryDisp
                        .ice_staticId());
                InternalRepositoryPrx[] repos = new InternalRepositoryPrx[candidates.length];
                for (int i = 0; i < repos.length; i++) {
                    repos[i] = InternalRepositoryPrxHelper
                            .uncheckedCast(candidates[i]);
                }
                log.info("Found " + repos.length + " repo(s) : "
                        + Arrays.toString(repos));
                return repos;
            } catch (Exception e) {
                log.warn("Could not query repositories " + e);
                return null;
            }
        }
        
        public MonitorServerPrx[] lookupMonitorServers() {
            IceGrid.QueryPrx query = getGridQuery();
            if (query == null) {
                return null; // EARLY EXIT
            }
            try {
                Ice.ObjectPrx[] candidates = null;
                candidates = query.findAllObjectsByType(_MonitorServerDisp.ice_staticId());
                MonitorServerPrx[] mss = new MonitorServerPrx[candidates.length];
                for (int i = 0; i < mss.length; i++) {
                    mss[i] = MonitorServerPrxHelper
                            .uncheckedCast(candidates[i]);
                }
                log.info("Found " + mss.length + " monitor server(s) : "
                        + Arrays.toString(mss));
                return mss;
            } catch (Exception e) {
                log.warn("Could not query repositories " + e);
                return null;
            }
        }
    }
}
