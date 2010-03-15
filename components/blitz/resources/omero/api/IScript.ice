/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_ISCRIPT_ICE
#define OMERO_ISCRIPT_ICE

#include <omero/API.ice>
#include <omero/ServerErrors.ice>

module omero {

    module api {

	/**
	 * Utility service for managing scripts for execution by the Processor API.
	 * See <a href="https://trac.openmicroscopy.org.uk/omero/wiki/OmeroScripts">OmeroScripts</a> for more information.
	 **/
	["ami","amd"] interface IScript extends ServiceInterface
	    {

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
		 * @param id see above.
		 * @return see above.
		 * @throws ApiUsageException
		 **/
		idempotent string getScript(long id) throws ServerError;

		/**
		 * Get the script from the server with details from OriginalFile
		 * @param id see above
		 * @return see above
		 * @throws ApiUsageException
		 **/
		idempotent RTypeDict getScriptWithDetails(long id) throws ServerError;

		/**
		 * Get the parameters that the script takes and returns, along with
                 * other metadata available from the script.
		 *
		 * @param id see above.
		 * @return see above.
		 * @throws ApiUsageException
		 **/
		idempotent omero::grid::JobParams getParams(long id) throws ServerError;

		/**
		 * Delete the script on the server with id.
		 * @param id Id of the script to delete.
		 * @throws ApiUsageException
		 * @throws SecurityViolation
		 **/
		void deleteScript(long id) throws ServerError;
	    };

    };

};

#endif
