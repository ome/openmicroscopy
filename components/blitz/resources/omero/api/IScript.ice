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
	 * Utility service around the Processor API.
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
		long uploadScript(string script) throws ServerError;

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
		 * Get the parameters that the script takes. This is a key-value pair map,
		 * the key being the variable name, and the value the type of the variable.
		 * @param id see above.
		 * @return see above.
		 * @throws ApiUsageException
		 **/
		idempotent RTypeDict getParams(long id) throws ServerError;

		/**
		 * Run the script on the server with id, and using the parameters, paramMap.
		 * The server checks that all the parameters expected by the script are
		 * supplied in the paramMap and that their types match. Once executed the
		 * script then returns a resultMap which is a key-value pair map, the
		 * key being the result variable name and the value being the value of the
		 * variable.
		 * @param id see above.
		 * @param paramMap see above.
		 * @return see above.
		 * @throws ApiUsageException
		 * @throws SecurityViolation
		 **/
		RTypeDict runScript(long id, RTypeDict map) throws ServerError;

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
