/*
 *   $Id$
 *
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_CMD_GRAPHS_ICE
#define OMERO_CMD_GRAPHS_ICE

#include <omero/cmd/API.ice>

module omero {

    module cmd {

        /**
         *
         **/
        class GraphModify extends Request {
            string type;
            long id;
            StringMap options;
        };

        ["java:type:java.util.ArrayList<omero.cmd.GraphModify>:java.util.List<omero.cmd.GraphModify>"]
        sequence<GraphModify> GraphModifyList;

        /**
         *
         **/
        class GraphSpecList extends Request {};

        class GraphSpecListRsp extends Response {
            GraphModifyList list;
        };

        class Chgrp extends GraphModify {
            long grp;
        };

        class ChgrpRsp extends Response {
        };

        /**
         * Modifies the permissions settings for the given object.
         * Most permission modifications will be quite fast and will
         * specify this as returning a small number of steps in the
         * status object. When lowering a READ setting, however, all
         * existing data will need to be checked and there will be a
         * minimum of one step per table in the database.
         *
         * At the moment, the only supported type is "/ExperimenterGroup".
         *
         **/
        class Chmod extends GraphModify {

            /**
             * String representation of the permissions
             * which should be set on the object.
             **/
            string permissions;
        };

        class ChmodRsp extends Response {
        };

        class Chown extends GraphModify {
            long user;
        };

        class ChownRsp extends Response {
        };

        class Delete extends GraphModify {
        };

        class DeleteRsp extends Response {
        };

    };
};

#endif
