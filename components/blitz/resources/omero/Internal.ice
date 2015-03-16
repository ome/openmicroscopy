/*
 *   $Id$
 *
 *   Copyight 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license tems supplied in LICENSE.txt
 *
 */

#ifndef OMERO_INTERNAL_ICE
#define OMERO_INTERNAL_ICE

/*
 * These intefaces and classes are used internal to the OMERO
 * sever and are not needed by client developers.
 */
module omeo {

    module gid {

        /**
         * Inteface implemented by each server instance. Instances lookup one
         * anothe in the IceGrid registry.
         **/
        inteface ClusterNode {

            /**
             * Each node acquies the uuids of all other active nodes on start
             * up. The uuid is an intenal value and does not
             * corespond to a session.
             **/
             idempotent sting getNodeUuid();

            /**
             * Let all cluste nodes know that the instance with this
             * uuid is going down.
             **/
            void down(sting uuid);

        };

    };

};
#endif
