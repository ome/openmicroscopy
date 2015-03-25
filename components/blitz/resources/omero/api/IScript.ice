/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_ISCRIPT_ICE
#define OMERO_ISCRIPT_ICE

#include <omero/ServicesF.ice>
#include <omero/Scripts.ice>

module omero {

    module api {

        /**
         * Utility service for managing and launching scripts for execution by the Processor API.
         *
         * Typical usage might include (PYTHON):
         * <pre>
         *
         * sf = client.createSession()
         * svc = sf.getScriptService()
         * scripts = svc.getScripts()
         *
         * if len(scripts) >= 1:
         *   script_id = svc.keys()[0]
         * else:
         *   script_id = svc.uploadScript('/test/my_script.py', SCRIPT_TEXT)
         *
         * params = svc.getParams(script_id)
         *
         * # You will need to parse the params to create the proper input
         * inputs = {}
         *
         * # The last parameter is how long to wait as an RInt
         * proc = svc.runScript(script_id, inputs, None)
         * try:
         *     cb = omero.scripts.ProcessCallbackI(client, proc)
         *     while not cb.block(1000): # ms.
         *         pass
         *     cb.close()
         *     rv = proc.getResults(0)
         * finally:
         *     proc.close(False)
         *
         * </pre>
         * See <a href="http://www.openmicroscopy.org/site/support/omero5/developers/scripts/">OMERO.scripts</a> for more information.
         **/
        ["ami","amd"] interface IScript extends ServiceInterface
            {

                //
                // Script management
                //

                /**
                 * This method returns official server scripts as a list of [omero::model::OriginalFile] objects.
                 * These scripts will be executed by the server if submitted via [runScript]. The input parameters
                 * necessary for proper functioning can be retrieved via [getParams].
                 *
                 * The [omero::model::OriginalFil::path] value can be used in other official scripts via the
                 * language specific import command, since the script directory will be placed on the appropriate
                 * environment path variable.
                 * <pre>
                 * scripts = scriptService.getScripts()
                 * for script in scripts:
                 *     text = scriptService.getScriptText(script.id.val)
                 *     path = script.path.val[1:] # First symbol is a "/"
                 *     path = path.replace("/",".")
                 *     print "Possible import: %s" % path
                 * </pre>
                 *
                 * @return see above.
                 * @throws ApiUsageException
                 * @throws SecurityViolation
                 **/
                idempotent OriginalFileList getScripts() throws ServerError;

                /**
                 * Returns non-official scripts which have been uploaded by individual users.
                 * These scripts will <em>not</me> be run by the server, though a user can
                 * start a personal "usermode processor" which will allow the scripts to be
                 * executed. This is particularly useful for testing new scripts.
                 */
                idempotent OriginalFileList getUserScripts(IObjectList acceptsList) throws ServerError;

                /**
                 * Get the id of an official script by the script path.
                 * The script service ensures that all script paths are unique.
                 *
                 * Note: there is no similar method for user scripts (e.g. getUserScriptID)
                 * since the path is not guaranteed to be unique.
                 *
                 * @param scriptName The name of the script.
                 * @return see above.
                 * @throws ApiUsageException
                 * @throws SecurityViolation
                 **/
                idempotent long getScriptID(string path) throws  ServerError;

                /**
                 * Get the text from the server for the script with given id.
                 *
                 * @param scriptID see above.
                 * @return see above.
                 * @throws ApiUsageException
                 **/
                idempotent string getScriptText(long scriptID) throws ServerError;

                /**
                 * Upload a user script to the server and return the id. This method checks that
                 * a script with that names does not exist and that the script has parameters
                 * <em>if possible</em>, i.e. a usermode processor is running which for the
                 * current user.
                 *
                 * @param script see above.
                 * @return The new id of the script.
                 * @throws ApiUsageException
                 * @throws SecurityViolation
                 **/
                long uploadScript(string path, string scriptText) throws ServerError;

                /**
                 * Like [uploadScript] but is only callable by administrators. The parameters
                 * for the script are also checked.
                 **/
                long uploadOfficialScript(string path, string scriptText) throws ServerError;

                /**
                 * Modify the text for the given script object.
                 *
                 * @param script see above.
                 * @throws ApiUsageException
                 * @throws SecurityViolation
                 **/
                void editScript(omero::model::OriginalFile fileObject, string scriptText) throws ServerError;

                /**
                 * Get the script from the server with details from OriginalFile
                 * @param scriptID see above
                 * @return see above
                 * @throws ApiUsageException
                 **/
                idempotent RTypeDict getScriptWithDetails(long scriptID) throws ServerError;

                /**
                 * Get the parameters that the script takes and returns, along with
                 * other metadata available from the script.
                 *
                 * @param scriptID see above.
                 * @return see above.
                 * @throws ApiUsageException
                 **/
                idempotent omero::grid::JobParams getParams(long scriptID) throws ServerError;

                /**
                 * Delete the script on the server with id. The file will also be removed from disk.
                 *
                 * @param scriptID Id of the script to delete.
                 * @throws ApiUsageException
                 * @throws SecurityViolation
                 **/
                void deleteScript(long scriptID) throws ServerError;

                // Planned methods
                // ===============
                // idempotent OriginalFileList getImageScripts(Parameters params) throws ServerError;
                // idempotent OriginalFileList getScriptsByName(string name, Parameters params) throws ServerError;
                // idempotent OriginalFileList getScriptsByKeyword(string keyword, Parameters params) throws ServerError;

                //
                // Process/Job Management
                //

                /**
                 * If [ResourceError] is thrown, then no [Processor] is available. Use [scheduleJob]
                 * to create a [omero::model::ScriptJob] in the "Waiting" state. A [Processor] may
                 * become available.
                 *
                 * <pre>
                 * try:
                 *     proc = scriptService.runScript(1, {}, None)
                 * except ResourceError:
                 *     job = scriptService.scheduleScript(1, {}, None)
                 * </pre>
                 *
                 * The [ScriptProcess] proxy MUST be closed before exiting.
                 * If you would like the script execution to continue in the
                 * background, pass "True" as the argument.
                 *
                 * <pre>
                 * try:
                 *     proc.poll()         # See if process is finished
                 * finally:
                 *     proc.close(True)    # Detach and execution can continue
                 *     # proc.close(False) # OR script is immediately stopped.
                 * </pre>
                 **/
                omero::grid::ScriptProcess* runScript(long scriptID, omero::RTypeDict inputs, omero::RInt waitSecs) throws ServerError;

                /**
                 * Returns true if there is a processor which will run the given script.
                 *
                 * <p>
                 * Either the script is an official script and this method will return true
                 * (though an individual invocation may fail with an [omero::ResourceError]
                 * for some reason) <em>or</em> this is a user script, and a usermode processor
                 * must be active which takes the scripts user or group.
                 * </p>
                 *
                 **/
                bool canRunScript(long scriptID) throws ServerError;

                /**
                 * Used internally by processor.py to check if the script attached to the [omero::model::Job]
                 * has a valid script attached, based on the [acceptsList] and the current security context.
                 *
                 * An example of an acceptsList might be <pre>Experimenter(myUserId, False)</pre>, meaning that
                 * only scripts belonging to me should be trusted. An empty list implies that the server should
                 * return what it would by default trust.
                 *
                 * A valid script will be returned if it exists; otherwise null.
                 **/
                omero::model::OriginalFile validateScript(omero::model::Job j, omero::api::IObjectList acceptsList) throws ServerError;

                // Planned methods
                // ===============
                // omero::grid::ScriptProcess* findProcess(long jobID) throws ServerError;
                // omero::model::ScriptJob scheduleScript(long scriptID, omero::RTypeDict inputs, omero::RTime scheduledFor) throws ServerError;
                // omero::api::ScriptJobList getActiveJobs(omero::sys::Parameters params) throws ServerError;
                // omero::api::ScriptJobList getWaitingJobs(omero::sys::Parameters params) throws ServerError;
                // omero::api::ScriptJobList getRunningJobs(omero::sys::Parameters params) throws ServerError;
                // long cancelActiveJobs() throws ServerError;
                // long cancelRunningJobs() throws ServerError;
                // long cancelWaitingJobs() throws ServerError;

            };

    };

};

#endif
