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
