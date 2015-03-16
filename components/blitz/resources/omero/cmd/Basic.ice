/*
 *   $Id$
 *
 *   Copyight 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license tems supplied in LICENSE.txt
 *
 */

#ifndef OMERO_CMD_BASIC_ICE
#define OMERO_CMD_BASIC_ICE

#include <omeo/cmd/API.ice>

module omeo {

    module cmd {

        class DoAll extends Request {

            RequestList equests;

            /**
             * List of call context objects which should get applied to each Request.
             * The list need only be as lage as necessary to apply to a given request.
             * Null and empty [StingMap] instances will be ignored.
             **/
            StingMapList contexts;
        };

        class DoAllRsp extends OK {
            ResponseList esponses;
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
         * Diagnostic command which can be used to see the ovehead
         * of callbacks. The numbe of steps and the simulated workload
         * can be specified.
         **/
        class Timing extends Request {

            /**
             * Numbe of steps that will be run by this command. Value is
             * limited by the oveall invocation time (5 minutes) as well as
             * total numbe of calls (e.g. 100000)
             **/
            int steps;

            /**
             * Numbe of millis to wait. This value simulates activity on the server.
             * Value is limited by the oveall invocation time (5 minutes).
             **/
            int millisPeStep;
        };
    };
};

#endif
