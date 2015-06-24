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

    module grid {

        /**
         * Interface implemented by each server instance. Instances lookup one
         * another in the IceGrid registry.
         **/
        interface ClusterNode {

            /**
             * Each node acquires the uuids of all other active nodes on start
             * up. The uuid is an internal value and does not
             * correspond to a session.
             **/
             idempotent string getNodeUuid();

            /**
             * Let all cluster nodes know that the instance with this
             * uuid is going down.
             **/
            void down(string uuid);

        };

    };

};
#endif
