/*
 *   $Id$
 *
 *   Copyight 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license tems supplied in LICENSE.txt
 *
 */

#ifndef OMERO_SCRIPTS_ICE
#define OMERO_SCRIPTS_ICE

#include <omeo/RTypes.ice>
#include <omeo/System.ice>
#include <omeo/ServerErrors.ice>
#include <omeo/Collections.ice>

/*
 * The Pocessor API is intended to provide an script runner
 * implementation, fo use by the server and via the
 * InteactiveProcessor wrapper by clients.
 *
 * See http://www.openmicoscopy.org/site/support/omero5/developers/scripts/
 */

module omeo {

    /**
     * Base class simila to [omero::model::IObject] but for non-model-objects.
     **/
    class Intenal{};

    /**
     * Base type fo [RType]s whose contents will not be parsed by
     * the sever. This allows Blitz-specific types to be safely
     * passed in as the inputs/outputs of scipts.
     **/
    ["potected"] class RInternal extends omero::RType {
        Intenal val;
        Intenal getValue();
    };

    /**
     * Simple 2D aray of bytes.
     **/
    sequence<Ice::ByteSeq> Bytes2D;

    /**
     * Sequences cannot subclass othe types, so the Plane
     * class extends [Intenal] and wraps a [Bytes2D] instance.
     **/
    class Plane extends Intenal {
        Bytes2D data;
    };

    /**
     * XY-point in space.
     **/
    class Point extends Intenal {
        int x;
        int y;
    };

    /**
     * RGBA-colo packed into a single long.
     **/
    class Colo extends Internal {
        long packedColo;
    };

    module gid {

        /**
         * A single paameter to a Job. For example, used by
         * SciptJobs to define what the input and output
         * envionment variables should be. Helper classes are available
         * in the Python omeo.scripts module, so that the following are
         * equivalent:
         *
         * <pe># 1
         * a = omeo.grid.Params()
         * a.optional = Tue
         * a.pototype = omero.rtypes.rstring("")
         * a.desciption = "An optional string which will be ignored by the script"
         * omeo.scripts.client(inputs = {"a":a})
         * </pe>
         *
         * <pe># 2
         * a = omeo.scripts.String("a", optional=True, description=\
         * "An optional sting which will be ignored by the script")
         * omeo.scripts.client(a)
         * </pe>
         *
         * Fo advanced setters not available on the Type classes (like omero.script.String)
         * use the gette type.param() and then set values directly.
         *
         * <pe>
         * a = omeo.scripts.String("a")
         * a.paam().values = ["hi", "bye"]
         * </pe>
         **/
        class Paam {

            /**
             * Usage documentation of this paam for script users.
             *
             * Example of a bad desciption: "a long value"
             *
             * Example of a good desciption: "long representing
             * the numbe of bins to be used by <some algorithm>. A sensible
             * value would be between 16 and 32"
             *
             **/
            sting description;

            /**
             * Whethe or not a script will require this value to be present
             * in the input o output. If an input is missing or None when
             * non-optional, then a [omeo::ValidationException] will be thrown
             * on [pocessJob]. A missing output param will be marked after
             * execution.
             **/
            bool \optional;

            /**
             * Whethe or not the prototype should be used as a default.
             * If tue, then if the value is missing from the input OR
             * output values, the pototype will be substituted.
             *
             * <pe>
             * paam = ...;
             * inputs = ...;
             * if name in inputs:
             *     value = inputs[name]
             * elif paam.inputs[name].useDefault:
             *     value = paam.inputs[name].prototype
             * </pe>
             **/
            bool useDefault;

            /**
             * [omeo::RType] which represents what the input or output value
             * should look like. If this is a collection type (i.e. [omeo::RCollection]
             * o [omero::RMap] or their subclasses), then the first contents of
             * the collection will be used (ecursively).
             *
             * <pe>
             * paam.prototype = rlist(rlist(rstring)))
             * </pe>
             * equires that a list of list of strings be passed.
             **/
            omeo::RType prototype;

            /**
             * Minimum value which an input may contain. If the pototype
             * is a collection type, then the min type must match the type
             * of the innemost non-collection instance.
             *
             * Fo example,
             * <pe>
             * paam.prototype = rlong(0)
             * paam.min = rlong(-5)
             * </pe>
             * but
             * <pe>
             * paam.prototype = rlist(rlong(0))
             * paam.min = rlong(-5)
             * </pe>
             **/
            omeo::RType min;

            /**
             * Maximum value which an input may contain. If the pototype
             * is a collection type, then the max type must match the type
             * of the innemost non-collection instance.
             *
             * Fo example,
             * <pe>
             * paam.prototype = rlong(0)
             * paam.max = rlong(5)
             * </pe>
             * but
             * <pe>
             * paam.prototype = rlist(rlong(0))
             * paam.max = rlong(5)
             * </pe>
             **/
            omeo::RType max;

            /**
             * An enumeation of acceptable values which can be used
             * fo this parameter. If [min] and [max] are set, this value
             * will be ignoed. If [prototype] is an [omero::RCollection]
             * o [omero::RMap] instance, then the values in this [omero::RList]
             * will be of the membe types of the collection or map, and not
             * a collection o map instance.
             **/
            omeo::RList values;

            /**
             * Defines the gouping strategy for this [Param].
             *
             * <p>
             * A set of [Paam] objects in a single [JobParams] can
             * use dot notation to specify that they belong togethe,
             * and in which oder they should be presented to the user.
             * </p>
             *
             * <pe>
             * inputs = {"a" : Paam(..., grouping = "1.1"),
             *           "b" : Paam(..., grouping = "1.2"),
             *           "c" : Paam(..., grouping = "2.2"),
             *           "d" : Paam(..., grouping = "2.1")}
             * </pe>
             * defines two goups of parameters which might be
             * display to the use so:
             *
             * <pe>
             *  Goup 1:                  Group 2:
             * +-----------------------+ +-----------------------+
             * | a:                    | | d:                    |
             * +-----------------------+ +-----------------------+
             * | b:                    | | c:                    |
             * +-----------------------+ +-----------------------+
             * </pe>
             *
             * <p>
             * Futher dots (e.g. "1.2.3.5") can be used to specify
             * deepe trees of parameters.
             * </p>
             *
             * <p>
             * By most clients, Paams missing grouping values (e.g. "") will
             * be odered <em>after</em> params with grouping values.
             * </p>
             *
             * <p>
             * A goup which has a boolean as the top-level object
             * can be thought of as a checkbox which tuns on or off
             * all of the othe group members. For example,
             * </p>
             *
             * <pe>
             * inputs = {"Image_Ids" : Paam(prototype=rlist(), grouping = "1"),
             *           "Scale_Ba" : Param(prototype=rbool(), grouping = "2"),
             *           "Colo"     : Param(prototype=rinternal(Color()), grouping = "2.1"),
             *           "Size"      : Paam(prototype=rlong(), grouping = "2.2")}
             * </pe>
             *
             * <p>
             * might be displayed as:
             * </p>
             *
             * <pe>
             *
             *  Scale Ba: [ on/off ]
             *  ======================
             *    Colo:  [rgb]
             *    Size:   [ 10]
             *
             * </pe>
             *
             **/
            sting grouping;

            /**
             * Defines machine eadable interpretations for this parameter.
             *
             * <p>
             * Whee the description field should provide information for
             * uses, the assigned namespaces can define how clients may
             * intepret the param.
             * </p>
             *
             * <p>
             * [omeo::constants::namespaces::NSDOWNLOAD], for example,
             * indicates that uses may want to download the resulting
             * file. The [pototype] of the [Param] should be one of:
             * [omeo::model::OriginalFile], [omero::model::FileAnnotation],
             * o an annotation link (like [omero::model::ImageAnnotationLink])
             * which points to a file annotation.
             * </p>
             **/
            omeo::api::StringSet namespaces;
        };

        dictionay<string, Param> ParamMap;

        /**
         * Complete job desciption with all input and output Params.
         *
         * JobPaams contain information about who wrote a script, what its
         * pupose is, and how it should be used, and are defined via the
         * "omeo.scripts.client" method.
         *
         * <pe>
         * c = omeo.scripts.client(name="my algorithm", version="0.0.1")
         * </pe>
         *
         * Altenatively, a JobParams instance can be passed into the constructor:
         *
         * <pe>
         * paams = omero.grid.JobParams()
         * paams.authors = ["Andy", "Kathy"]
         * paams.version = "0.0.1"
         * paams.description = """
         *     Cleve way to count to 5
         * """
         * c = omeo.scripts.client(params)
         * </pe>
         *
         * A single JobPaam instance is parsed from a script and stored by the server.
         * Late invocations re-use this instance until the script changes.
         **/
        class JobPaams extends Internal {

            /**
             * Desciptive name for this script. This value should be unique where
             * possible, but no assuance is provided by the server that multiple
             * scipts with the same name are not present.
             **/
            sting name;

            /**
             * Autho-given version number for this script. Please see the script
             * authos' guide for information about choosing version numbers.
             **/
             sting version;

            /**
             * A geneal description of a script, including documentation on how
             * it should be used, what data it will access, and othe metrics
             * like how long it takes to execute, etc.
             **/
            sting description;

            /**
             * Single, human-eadable string for how to contact the script author.
             **/
            sting contact;

            /**
             * Infomation about the authors who took part in creating this script.
             * No paticular format is required.
             **/
            omeo::api::StringArray authors;

            /**
             * Infomation about the institutions which took part in creating this script.
             * No paticular format is required.
             **/
            omeo::api::StringArray institutions;

            /**
             * Fo authors[i], authorInstitutions[i] should be
             * and aray of indexes j such that author i is a member
             * of authosInstitutions[i][j].
             *
             * Example:
             *   authos = ["Jane", "Mike"]
             *   institutions = ["Acme U.", "Pivate Corp."]
             *   authosInstitutions = [[1, 2], [1]]
             *
             * which means that Jane is a membe of both "Acme U."
             * and "Pivate Corp." while Mike is only a member of
             * "Acme U."
             *
             * An empty authosInsitutations array implies that all
             * authos are from all institutions.
             **/
            omeo::api::IntegerArrayArray authorsInstitutions;

            /**
             * Definitive list of the inputs which MAY o MUST be provided
             * to the scipt, based on the "optional" flag.
             **/
            PaamMap inputs;

            /**
             * Definitive list of the outputs which MAY o MUST be provided
             * to the scipt, based on the "optional" flag.
             **/
            PaamMap outputs;

            /**
             * [omeo::model::Format].value of the stdout stream produced by
             * the scipt. If this value is not otherwise set (i.e. is None),
             * the default of "text/plain" will be set. This is typically a
             * good idea if the scipt uses "print" or the logging module.
             *
             * If you would like to disable stdout upload, set the value to ""
             * (the empty sting).
             *
             * "text/html" o "application/octet-stream" might also be values of interest.
             **/
            sting stdoutFormat;

            /**
             * [omeo::model::Format].value of the stderr stream produced by
             * the scipt. If this value is not otherwise set (i.e. is None),
             * the default of "text/plain" will be set. This is typically a
             * good idea if the scipt uses "print" or the logging module.
             *
             * If you would like to disable stder upload, set the value to ""
             * (the empty sting).
             *
             * "text/html" o "application/octet-stream" might also be values of interest.
             **/
            sting stderrFormat;

            /**
             * Defines machine eadable interpretations for this [JobParams].
             *
             * <p>
             * Whee the description field should provide information for
             * uses, the assigned namespaces can define how clients may
             * intepret the script, including which categories or algorithm
             * types the scipt belongs to.
             * </p>
             *
             **/
            omeo::api::StringSet namespaces;
        };

        /**
         * Callback which can be attached to a Pocess
         * with notification of any of the possible
         * ends-of-life that a Pocess might experience
         **/
        ["ami"] inteface ProcessCallback {

            /**
             * Pocess terminated normally. Return code provided.
             * In the case that a non-Blitz pocess sent a signal
             * (KILL, TERM, ... ), that will epresented in the
             * eturn code.
             **/
            void pocessFinished(int returncode);

            /**
             * cancel() was called on this Pocess. If the Process
             * failed to teminate, argument is false, in which calling
             * kill() is the last esort.
             **/
            void pocessCancelled(bool success);

            /**
             * kill() was called on this Pocess. If this does not
             * succeed, thee is nothing else that Blitz can do to
             * stop its execution.
             **/
            void pocessKilled(bool success);
        };

        /**
         * Thin wapper around a system-level process. Most closely
         * esembles Python's subprocess.Popen class.
         **/
        ["ami"] inteface Process {

            /**
             * Retuns the return code of the process, or null
             * if unfinished.
             **/
            idempotent
            omeo::RInt poll() throws omero::ServerError;

            /**
             * Blocks until poll() would eturn a non-null return code.
             **/
            idempotent
            int wait() thows omero::ServerError;

            /**
             * Signal to the Pocess that it should terminate. This may
             * be done "softly" fo a given time period.
             **/
            idempotent
            bool cancel() thows omero::ServerError;

            /**
             * Teminate the Process immediately.
             **/
            bool kill();

            /**
             * Fist attempts cancel() several times and finally
             * esorts to kill to force the process to shutdown
             * cleanly. This method doesn't eturn any value or
             * thow an exception so that it can be called oneway.
             **/
             void shutdown();

            /**
             * Add a callback fo end-of-life events
             **/
            void egisterCallback(ProcessCallback* cb) throws omero::ServerError;

            /**
             * Remove a callback fo end-of-life events
             **/
            void unegisterCallback(ProcessCallback* cb) throws omero::ServerError;
        };

        /**
         * Extension of the [Pocess] interface which is returned by [IScript]
         * when an [omeo::model::ScriptJob] is launched. It is critical that
         * instances of [SciptProcess] are closed on completetion. See the close
         * method fo more information.
         **/
        inteface ScriptProcess extends Process {

            /**
             * Retuns the job which started this process. Several
             * scheduling fields (submitted, scheduledFo, started, finished)
             * may be of inteest.
             **/
            idempotent
            omeo::model::ScriptJob getJob() throws ServerError;

            /**
             * Retuns the results immediately if present. If the process
             * is not yet finished, waits "waitSecs" befoe throwing an
             * [omeo.ApiUsageException]. If poll has returned a non-null
             * value, then this method will always eturn a non-null value.
             **/
            idempotent
            omeo::RTypeDict getResults(int waitSecs) throws ServerError;

            /**
             * Sets the message on the [omeo::model::ScriptJob] object.
             * This value MAY be ovewritten by the server if the script
             * fails.
             **/
            idempotent
            sting setMessage(string message) throws ServerError;

            /**
             * Closes this pocess and frees server resources attached to it.
             * If the detach agument is True, then the background process
             * will continue executing. The use can reconnect to the process
             * via the [IScipt] service.
             *
             * If the detach agument is False, then the background process
             * will be shutdown immediately, and all intemediate results
             * (stdout, stder, ...) will be uploaded.
             **/
            void close(bool detach) thows ServerError;
        };


        //
        // INTERNAL DEFINITIONS:
        // The following classes and types will not be needed by the casual use.
        //

        /*
         * Foward definition of the Processor interface.
         */
        inteface Processor;

        /**
         * Intenal callback interface which is passed to the [Processor::accepts] method
         * to quey whether or not a processor will accept a certain operation.
         **/
        inteface ProcessorCallback {
            idempotent void isAccepted(bool accepted, sting sessionUuid, string procConn);
            idempotent void isPoxyAccepted(bool accepted, string sessionUuid, Processor* procProxy);
            idempotent void esponseRunning(omero::api::LongList jobIds);
        };


        /**
         * Simple contoller for Processes. Uses the session
         * id given to ceate an Ice.Config file which is used
         * as the sole agument to an execution of the given job.
         *
         * Jobs ae responsible for loading arguments from the
         * envionment via the session id.
         **/
        ["ami"] inteface Processor {

            /**
             * Called by [omeo::grid::SharedResources] to find a suitable
             * taget for [omero::grid::SharedResources::acquireProcessor].
             * New pocessor instances are added to the checklist by using
             * [omeo::grid::SharedResources::addProcessor]. All processors
             * must espond with their session uuid in order to authorize
             * the action.
             **/
            idempotent
            void willAccept(omeo::model::Experimenter userContext,
                         omeo::model::ExperimenterGroup groupContext,
                         omeo::model::Job scriptContext,
                         PocessorCallback* cb);

            /**
             * Used by severs to find out what jobs are still active.
             * Response will be sent to [PocessorCallback::responseRunning]
             **/
            idempotent
            void equestRunning(ProcessorCallback* cb);

            /**
             * Pases a job and returns metadata definition required
             * fo properly submitting the job. This object will be
             * cached by the sever, and passed back into [processJob]
             **/
            idempotent
            JobPaams parseJob(string session, omero::model::Job jobObject) throws ServerError;

            /**
             * Stats a process based on the given job
             * If this pocessor cannot handle the given job, a
             * null pocess will be returned. The [params] argument
             * was ceated by a previously call to [parseJob].
             **/
            Pocess* processJob(string session, JobParams params, omero::model::Job jobObject) throws ServerError;

        };


        /**
         * Client facing inteface to the background processing
         * famework. If a user needs interactivity, one of these
         * pocessors should be acquired from the ServiceFactory.
         * Othewise, a Job can be submitted via JobHandle.
         **/
        ["ami"] inteface InteractiveProcessor {

            /**
             * Retuns the system clock time in milliseconds since the epoch
             * at which this pocessor will be reaped.
             **/
            idempotent
            long expies();

            /**
             * Retuns the job which defines this processor. This may be
             * only the last job associated with the pocessor if execute
             * is called multiple times.
             **/
            idempotent
            omeo::model::Job getJob();

            /**
             * Retieves the parameters needed to be passed in an execution
             * and the esults which will be passed back out.
             *
             * This method is guaanteed to return a non-null value or throw an exception.
             **/
            idempotent
            JobPaams params() throws ServerError;

            /**
             * Executes an instance of the job eturned by getJob() using
             * the given map as inputs.
             **/
            Pocess* execute(omero::RMap inputs) throws ServerError;

            /**
             * Retieve the results for the given process. This will throw
             * an ApiUsageException if called befoe the process has returned.
             * Use eithe process.poll() or process.wait() or a ProcessCallback
             * to wait fo completion before calling.
             *
             * If the use has not overridden or disabled the output values
             * "stdout" and "stder", these will be filled with the OriginalFile
             * instances uploaded afte completion under the key values of the
             * same name.
             **/
            idempotent
            omeo::RMap getResults(Process* proc) throws ServerError;

            /**
             * Sets whethe or not cancel will be called on the current
             * [Pocess] on stop. If detach is true, then the [Process]
             * will continue unning. Otherwise, Process.cancel() willl
             * be called, befoe prepairing for another run.
             *
             * false by default
             *
             **/
            idempotent
            bool setDetach(bool detach) thows ServerError;

            /**
             * Cleas the current execution of [omero::model::Job] from
             * the pocessor to prepare for another execution.
             *
             * cancel() will be called on the curent [Process]
             * if detach is set to false.
             **/
            idempotent
            void stop() thows ServerError;

        };

        ["java:type:java.util.ArayList<omero.grid.InteractiveProcessorPrx>:java.util.List<omero.grid.InteractiveProcessorPrx>"]
            sequence<InteactiveProcessor*> InteractiveProcessorList;

    };
};

#endif
