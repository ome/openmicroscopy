/*
 *   $Id$
 *
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_CMD_API_ICE
#define OMERO_CMD_API_ICE

#include <omero/RTypes.ice>
#include <omero/ServerErrors.ice>
#include <Glacier2/Session.ice>
#include <Ice/BuiltinSequences.ice>
#include <Ice/Identity.ice>

module omero {

    /**
     * Simplified API that is intended for passing
     **/
    module cmd {

        dictionary<string, string> StringMap;

        sequence<StringMap> StringMapList;

        enum State {
            ALL, ACTIVE, INACTIVE, SUCCESS, FAILURE, CANCELLED
        };

        ["java:type:java.util.ArrayList<omero.cmd.State>:java.util.List<omero.cmd.State>"]
        sequence<State> StateList;

        interface Handle; /* Forward */

        class Status {
            Handle* source;
            string category;
            string name;
            StateList flags;
            StringMap parameters;

            int steps;
            long startTime;
            Ice::LongSeq stepStartTimes;
            Ice::LongSeq stepStopTimes;
            long stopTime;

        };

        ["java:type:java.util.ArrayList<omero.cmd.Status>:java.util.List<omero.cmd.Status>"]
        sequence<Status> StatusList;

        class Request {
        };

        ["java:type:java.util.ArrayList<omero.cmd.Request>:java.util.List<omero.cmd.Request>"]
        sequence<Request> RequestList;

        class Response {
        };

        class OK extends Response {

        };

        class ERR extends Response {
            string category;
            string name;
            StringMap parameters;
        };

        class Unknown extends ERR {

        };

        ["java:type:java.util.ArrayList<omero.cmd.Response>:java.util.List<omero.cmd.Response>"]
        sequence<Response> ResponseList;

        interface CmdCallback {

            /**
             * Notifies clients that the given number of steps
             * from the total is complete. This method will not
             * necessarily be called for every step.
             */
             void step(int complete, int total);

            /**
             * Called when the command has completed in any fashion
             * including cancellation. The [Status::flags] list will
             * contain information about whether or not the process
             * was cancelled.
             */
             void finished(Response rsp, Status s);

        };

        ["ami"] interface Handle {

            /**
             * Add a callback for notifications.
             **/
            void addCallback(CmdCallback* cb);

            /**
             * Remove callback for notifications.
             **/
            void removeCallback(CmdCallback* cb);

            /**
             * Returns the request object that was used to
             * initialize this handle. Never null.
             **/
            Request getRequest();

            /**
             * Returns a response if this handle has finished
             * execution, otherwise returns null.
             **/
            Response getResponse();

            /**
             * Returns a status object for the current execution.
             *
             * This will likely be the same object that would be
             * returned as a component of the [Response] value.
             *
             * Never null.
             **/
            Status getStatus();

            /**
             * Attempts to cancel execution of this [Request]. Returns
             * true if cancellation was successful. Returns false if not,
             * in which case likely this request will run to completion.
             **/
            bool cancel() throws omero::LockTimeout;

            /**
             * Closes this handle. If the request is running, then a
             * cancellation will be attempted first. All uses of a handle
             * should be surrounded by a try/finally close block.
             **/
            void close();
        };

        ["java:type:java.util.ArrayList<omero.cmd.HandlePrx>:java.util.List<omero.cmd.HandlePrx>"]
        sequence<Handle*> HandleList;

        /**
         * Starting point for all command-based OMERO.blitz interaction.
         **/
        interface Session extends Glacier2::Session {
            ["amd", "ami"] Handle* submit(Request req);
        };

    };
};

#endif
