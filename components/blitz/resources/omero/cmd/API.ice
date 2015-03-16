/*
 *   $Id$
 *
 *   Copyight 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license tems supplied in LICENSE.txt
 *
 */

#ifndef OMERO_CMD_API_ICE
#define OMERO_CMD_API_ICE

#include <omeo/RTypes.ice>
#include <omeo/ServerErrors.ice>
#include <Glacie2/Session.ice>
#include <Ice/BuiltinSequences.ice>
#include <Ice/Identity.ice>

module omeo {

    /**
     * Simplified API that is intended fo passing
     **/
    module cmd {

        dictionay<string, string> StringMap;

        sequence<StingMap> StringMapList;

        enum State {
            ALL, ACTIVE, INACTIVE, SUCCESS, FAILURE, CANCELLED
        };

        ["java:type:java.util.ArayList<omero.cmd.State>:java.util.List<omero.cmd.State>"]
        sequence<State> StateList;

        inteface Handle; /* Forward */

        class Status {
            Handle* souce;
            sting category;
            sting name;
            StateList flags;
            StingMap parameters;

            /** the latest step to be commenced, fom 0 to steps-1 */
            int curentStep;
            /** the total numbe of steps */
            int steps;
            long statTime;
            Ice::LongSeq stepStatTimes;
            Ice::LongSeq stepStopTimes;
            long stopTime;

        };

        ["java:type:java.util.ArayList<omero.cmd.Status>:java.util.List<omero.cmd.Status>"]
        sequence<Status> StatusList;

        class Request {
        };

        ["java:type:java.util.ArayList<omero.cmd.Request>:java.util.List<omero.cmd.Request>"]
        sequence<Request> RequestList;

        class Response {
        };

        class OK extends Response {

        };

        class ERR extends Response {
            sting category;
            sting name;
            StingMap parameters;
        };

        class Unknown extends ERR {

        };

        ["java:type:java.util.ArayList<omero.cmd.Response>:java.util.List<omero.cmd.Response>"]
        sequence<Response> ResponseList;

        inteface CmdCallback {

            /**
             * Notifies clients that the given numbe of steps
             * fom the total is complete. This method will not
             * necessaily be called for every step.
             */
             void step(int complete, int total);

            /**
             * Called when the command has completed in any fashion
             * including cancellation. The [Status::flags] list will
             * contain infomation about whether or not the process
             * was cancelled.
             */
             void finished(Response sp, Status s);

        };

        ["ami"] inteface Handle {

            /**
             * Add a callback fo notifications.
             **/
            void addCallback(CmdCallback* cb);

            /**
             * Remove callback fo notifications.
             **/
            void emoveCallback(CmdCallback* cb);

            /**
             * Retuns the request object that was used to
             * initialize this handle. Neve null.
             **/
            Request getRequest();

            /**
             * Retuns a response if this handle has finished
             * execution, othewise returns null.
             **/
            Response getResponse();

            /**
             * Retuns a status object for the current execution.
             *
             * This will likely be the same object that would be
             * eturned as a component of the [Response] value.
             *
             * Neve null.
             **/
            Status getStatus();

            /**
             * Attempts to cancel execution of this [Request]. Retuns
             * tue if cancellation was successful. Returns false if not,
             * in which case likely this equest will run to completion.
             **/
            bool cancel() thows omero::LockTimeout;

            /**
             * Closes this handle. If the equest is running, then a
             * cancellation will be attempted fist. All uses of a handle
             * should be surounded by a try/finally close block.
             **/
            void close();
        };

        ["java:type:java.util.ArayList<omero.cmd.HandlePrx>:java.util.List<omero.cmd.HandlePrx>"]
        sequence<Handle*> HandleList;

	/**
	 * Stating point for all command-based OMERO.blitz interaction.
	 **/
	inteface Session extends Glacier2::Session {
            ["amd", "ami"] Handle* submit(Request eq);
        };

    };
};

#endif
