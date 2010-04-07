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
         *   script_id = svc.uploadScript(SCRIPT_TEXT)
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
	 * See <a href="https://trac.openmicroscopy.org.uk/omero/wiki/OmeroScripts">OmeroScripts</a> for more information.
	 **/
	["ami","amd"] interface IScript extends ServiceInterface
	    {

                //
                // Script management
                //

		/**
		 * This method returns the scripts on the server as by id and name.
		 *
		 * @return see above.
		 * @throws ApiUsageException
		 * @throws SecurityViolation
		 **/
		idempotent ScriptIDNameMap getScripts() throws ServerError;

		/**
		 * Get the id of the script with name, scriptName, the script service
		 * ensures that all script names are unique.
		 * @param scriptName The name of the script.
		 * @return see above.
		 * @throws ApiUsageException
		 * @throws SecurityViolation
		 **/
		idempotent long getScriptID(string name) throws  ServerError;

		/**
		 * Upload the script to the server and get the id. This method checks that
		 * a script with that names does not exist and that the script has parameters.
		 * @param script see above.
		 * @return The new id of the script.
		 * @throws ApiUsageException
		 * @throws SecurityViolation
		 **/
		long uploadScript(string scriptText) throws ServerError;

		/**
		 * Modify the text for the given script object.
                 *
		 * @param script see above.
		 * @throws ApiUsageException
		 * @throws SecurityViolation
		 **/
		void editScript(omero::model::OriginalFile fileObject, string scriptText) throws ServerError;

		/**
		 * Get the script from the server with id.
		 * @param scriptID see above.
		 * @return see above.
		 * @throws ApiUsageException
		 **/
		idempotent string getScript(long scriptID) throws ServerError;

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
		 * Delete the script on the server with id.
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
