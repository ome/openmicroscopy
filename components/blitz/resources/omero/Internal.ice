/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_INTERNAL_ICE
#define OMERO_INTERNAL_ICE

/*
 * These interfaces and classes are used internal to the OMERO
 * server and are not needed by client developers.
 */
module omero {

    module internal {

        /*
         * Callback passed to the discover method of the Cluster
         * interface. Each server instance will have a single Cluster
         * instance reachable via multicast and will respond with
         * its uuid. The uuid is an internal value and does not
         * correspond to a session.
         */
        interface DiscoverCallback {
            void clusterNodeUuid(string uuid);
        };

        /*
         * Interface implemented by each server instance. Instances communicate
         * via a multicast adapter defined in etc/internal.cfg. It should be
         * assumed that communication along the cluster is not completely secure
         * and so all values and operations should be checked.
         */
        interface Cluster {

            /*
             * Method called via multicast to discover all active
             * instances within an OMERO cluster.
             */
            void discover(DiscoverCallback* cb);

            /*
             * Let all cluster nodes know that the instance with this
             * uuid is going down.
             */
            void down(string uuid);
        };

    };

};
#endif
