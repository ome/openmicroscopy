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

            /**
             * List of call context objects which should get applied to each Request.
             * The list need only be as large as necessary to apply to a given request.
             * Null and empty {@link StringMap} instances will be ignored.
             **/
            StringMapList contexts;
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

        /**
         * Diagnostic command which can be used to see the overhead
         * of callbacks. The number of steps and the simulated workload
         * can be specified.
         **/
        class Timing extends Request {

            /**
             * Number of steps that will be run by this command. Value is
             * limited by the overall invocation time (5 minutes) as well as
             * total number of calls (e.g. 100000)
             **/
            int steps;

            /**
             * Number of millis to wait. This value simulates activity on the server.
             * Value is limited by the overall invocation time (5 minutes).
             **/
            int millisPerStep;
        };
    };
};

#endif
