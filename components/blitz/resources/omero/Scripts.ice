/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_SCRIPTS_ICE
#define OMERO_SCRIPTS_ICE

#include <omero/RTypes.ice>
#include <omero/System.ice>
#include <omero/ServerErrors.ice>
#include <omero/Collections.ice>

/*
 * The Processor API is intended to provide an script runner
 * implementation, for use by the server and via the
 * InteractiveProcessor wrapper by clients.
 *
 * See https://trac.openmicroscopy.org.uk/omero/wiki/OmeroScripts
 */

module omero {

    class Internal{};

    /**
     * Base type for [RType]s whose contents will not be parsed by
     * the server. This is an intermediate solution while
     * conversion between Blitz/JBoss types is necessary.
     **/
    ["protected"] class RInternal extends omero::RType {
        Internal val;
        Internal getValue();
    };

    /**
     * Types using the "Internal" infrastructure to allow storing
     * useful types in the input/output environments of scripts.
     **/
    sequence<Ice::ByteSeq> Bytes2D;

    class Plane extends Internal {
        Bytes2D data;
    };

    class Point extends Internal {
        int x;
        int y;
    };

    class Color extends Internal {
        int packedColor;
    };

    module grid {

        /**
         * A single parameter to a Job. For example, used by
         * ScriptJobs to define what the input and output
         * environment variables should be.
         **/
        class Param {
            string name;
            string description;
            bool optional;

            /**
             *
             **/
            omero::RType prototype;

            /**
             *
             **/
            omero::RType min;

            /**
             *
             **/
            omero::RType max;

            /**
             * An enumeration of acceptable values which can be used
             * for this parameter. If [min] and [max] are set, this value
             * will be ignored. If [prototype] is an [omero::RCollection]
             * or [omero::RMap] instance, then the values in this [omero::RList]
             * will be of the member types of the collection or map, and not
             * a collection or map instance.
             **/
            omero::RList values;
        };

        dictionary<string, Param> ParamMap;

        /**
         * Complete job description with all input
         * and output Params. See above.
         **/
        class JobParams extends Internal {

            string name;
            string description;
            string contact;

            omero::api::StringArray authors;
            omero::api::StringArray institutions;

            /**
             * For authors[i], authorInstitutions[i] should be
             * and array of indexes j such that author i is a member
             * of authorsInstitutions[i][j].
             *
             * Example:
             *   authors = ["Jane", "Mike"]
             *   institutions = ["Acme U.", "Private Corp."]
             *   authorsInstitutions = [[1, 2], [1]]
             *
             * which means that Jane is a member of both "Acme U."
             * and "Private Corp." while Mike is only a member of
             * "Acme U."
             *
             * An empty authorsInsitutations array implies that all
             * authors are from all institutions.
             **/
            omero::api::IntegerArrayArray authorsInstitutions;

            ParamMap inputs;
            ParamMap outputs;

            string stdoutFormat;
            string stderrFormat;

            omero::api::StringSet namespaces; // FIXME OR USE DIRECTORY NAME
        };

        /**
         * Callback which can be attached to a Process
         * with notification of any of the possible
         * ends-of-life that a Process might experience
         **/
        ["ami"] interface ProcessCallback {

            /**
             * Process terminated normally. Return code provided.
             * In the case that a non-Blitz process sent a signal
             * (KILL, TERM, ... ), that will represented in the
             * return code.
             **/
            void processFinished(int returncode);

            /**
             * cancel() was called on this Process. If the Process
             * failed to terminate, argument is false, in which calling
             * kill() is the last resort.
             **/
            void processCancelled(bool success);

            /**
             * kill() was called on this Process. If this does not
             * succeed, there is nothing else that Blitz can do to
             * stop its execution.
             **/
            void processKilled(bool success);
        };

        /**
         * Thin wrapper around a system-level process. Most closely
         * resembles Python's subprocess.Popen class.
         **/
        ["ami"] interface Process {

            /**
             * Returns the return code of the process, or null
             * if unfinished.
             **/
            omero::RInt poll() throws omero::ServerError;

            /**
             * Blocks until poll() would return a non-null return code.
             **/
            int wait() throws omero::ServerError;

            /**
             * Signal to the Process that it should terminate. This may
             * be done "softly" for a given time period.
             **/
            bool cancel() throws omero::ServerError;

            /**
             * Terminate the Process immediately.
             **/
            bool kill();

            /**
             * First attempts cancel() several times and finally
             * resorts to kill to force the process to shutdown
             * cleanly. This method doesn't return any value or
             * throw an exception so that it can be called oneway.
             **/
             void shutdown();

            /**
             * Add a callback for end-of-life events
             **/
            void registerCallback(ProcessCallback* cb) throws omero::ServerError;

            /**
             * Remove a callback for end-of-life events
             **/
            void unregisterCallback(ProcessCallback* cb) throws omero::ServerError;
        };


        /**
         * Callback interface which is passed to the [Processor::accepts] method
         * to query whether or not a processor will accept a certain operation.
         **/
        interface ProcessorAcceptsCallback {
            void isAccepted(bool accepted, string sessionUuid, string processorConn);
        };


        /**
         * Simple controller for Processes. Uses the session
         * id given to create an Ice.Config file which is used
         * as the sole argument to an execution of the given job.
         *
         * Jobs are responsible for loading arguments from the
         * environment via the session id.
         **/
        ["ami"] interface Processor {

            /**
             * Called by [omero::grid::SharedResources] to find a suitable
             * target for [omero::grid::SharedResources::acquireProcessor].
             * New processor instances are added to the checklist by using
             * [omero::grid::SharedResources::addProcessor]. All processors
             * must respond with their session uuid in order to authorize
             * the action.
             **/
            void willAccept(omero::model::Experimenter userContext,
                         omero::model::ExperimenterGroup groupContext,
                         omero::model::Job scriptContext,
                         ProcessorAcceptsCallback* cb);

            /**
             * Parses a job and returns metadata definition required
             * for properly submitting the job. This object will be
             * cached by the server, and passed back into [processJob]
             **/
            JobParams parseJob(string session, omero::model::Job jobObject) throws ServerError;

            /**
             * Starts a process based on the given job
             * If this processor cannot handle the given job, a
             * null process will be returned. The [params] argument
             * was created by a previously call to [parseJob].
             **/
            Process* processJob(string session, JobParams params, omero::model::Job jobObject) throws ServerError;

        };


        /**
         * Client facing interface to the background processing
         * framework. If a user needs interactivity, one of these
         * processors should be acquired from the ServiceFactory.
         * Otherwise, a Job can be submitted via JobHandle.
         **/
        ["ami"] interface InteractiveProcessor {

            /**
             * Returns the system clock time in milliseconds since the epoch
             * at which this processor will be reaped.
             **/
            long expires();

            /**
             * Returns the job which defines this processor. This may be
             * only the last job associated with the processor if execute
             * is called multiple times.
             **/
            omero::model::Job getJob();

            /**
             * Retrieves the parameters needed to be passed in an execution
             * and the results which will be passed back out.
             *
             * This method is guaranteed to return a non-null value or throw an exception.
             **/
            JobParams params() throws ServerError;

            /**
             * Executes an instance of the job returned by getJob() using
             * the given map as inputs.
             **/
            Process* execute(omero::RMap inputs) throws ServerError;

            /**
             * Retrieve the results for the given process. This will throw
             * an ApiUsageException if called before the process has returned.
             * Use either process.poll() or process.wait() or a ProcessCallback
             * to wait for completion before calling.
             *
             * If the user has not overridden or disabled the output values
             * "stdout" and "stderr", these will be filled with the OriginalFile
             * instances uploaded after completion under the key values of the
             * same name.
             **/
            omero::RMap getResults(Process* proc) throws ServerError;

            /**
             * Sets whether or not cancel will be called on the current
             * [Process] on stop. If detach is true, then the [Process]
             * will continue running. Otherwise, Process.cancel() willl
             * be called, before prepairing for another run.
             *
             * false by default
             *
             **/
            bool setDetach(bool detach) throws ServerError;

            /**
             * Clears the current execution of [omero::model::Job] from
             * the processor to prepare for another execution.
             *
             * cancel() will be called on the current [Process]
             * if detach is set to false.
             **/
            void stop() throws ServerError;

        };

        ["java:type:java.util.ArrayList<omero.grid.InteractiveProcessorPrx>:java.util.List<omero.grid.InteractiveProcessorPrx>"]
            sequence<InteractiveProcessor*> InteractiveProcessorList;
    };
};

#endif
