/*
 *   $Id$
 *
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_CMD_GRAPHS_ICE
#define OMERO_CMD_GRAPHS_ICE

#include <omero/cmd/Commands.ice>

module omero {

    module cmd {

        class ActionClass {
        };

        /**
         *
         **/
        class GraphCommand extends Command {
            ActionClass action;
            string type;
            long id;
        };

    };
};

#endif
