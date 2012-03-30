/*
 *   $Id$
 *
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_CMD_BASIC_ICE
#define OMERO_CMD_BASIC_ICE

#include <omero/cmd/API.ice>

module omero {

    module cmd {

        class DoAll extends Request {
            RequestList requests;
        };

        class DoAllRsp extends OK {
            ResponseList responses;
            StatusList status;
        };

        class ListRequests extends Request {

        };

        class ListRequestsRsp extends OK {
            RequestList list;
        };

        class PopStatus extends Request {
            int limit;
            StateList include;
            StateList exclude;
        };

        class PopStatusRsp extends OK {
            StatusList list;
        };

        class FindHandles extends Request {
            int limit;
            StateList include;
            StateList exclude;
        };

        class FindHandlesRsp extends OK {
            HandleList handles;
        };

    };
};

#endif
