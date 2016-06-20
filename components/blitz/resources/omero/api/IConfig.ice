/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_API_ICONFIG_ICE
#define OMERO_API_ICONFIG_ICE

#include <omero/RTypes.ice>
#include <omero/ServicesF.ice>

module omero {

    module api {

        /**
         * Access to server configuration. These methods provide access to the
         * state and configuration of the server and its components (e.g. the
         * database). However, it should not be assumed that two subsequent
         * calls to a proxy for this service will go to the same server due to
         * clustering.
         *
         * Not all possible server configuration is available through this
         * API. Some values (such as DB connection info, ports, etc.) must
         * naturally be set before this service is accessible.
         *
         * Manages synchronization of the various configuration sources
         * internally. It is therefore important that as far as possible all
         * configuration changes take place via this interface and not, for
         * example, directly via java.util.prefs.Preferences.
         *
         * Also used as the main developer example for developing (stateless)
         * ome.api interfaces. See source code documentation for more.
         */
        ["ami", "amd"] interface IConfig extends ServiceInterface
            {
                /**
                 * Provides the release version. OMERO-internal values will be
                 * in the form Major.minor.patch, starting with the value
                 * 4.0.0 for the 4.0 release, Spring 2009.
                 *
                 * Customized values should begin with a alphabetic sequence
                 * followed by a hyphen: ACME-0.0.1 and any build information
                 * should follow the patch number also with a hyphen:
                 * 4.0.0-RC1. These values will be removed by
                 * {@link #getVersion}
                 */
                idempotent string getVersion() throws ServerError;

                /**
                 * Retrieves a configuration value from the backend store.
                 * Permissions applied to the configuration value may cause a
                 * {@link SecurityViolation} to be thrown.
                 *
                 * @param key The non-null name of the desired configuration
                 *        value
                 * @return The string value linked to this key, possibly null
                 *         if not set.
                 * @throws ApiUsageException if the key is null or invalid.
                 * @throws SecurityViolation if the value for the key is not
                 *         readable.
                 */
                idempotent string getConfigValue(string key) throws ServerError;

                /**
                 * Retrieves configuration values from the backend store which
                 * match the given regex. Any configuration value which would
                 * throw an exception on being loaded is omitted.
                 *
                 * @param keyRegex The non-null regex of the desired
                 *        configuration values
                 * @return a map from the found keys to the linked values.
                 */
                idempotent omero::api::StringStringMap getConfigValues(string keyRegex) throws ServerError;

                /**
                 * Reads the etc/omero.properties file and returns all the
                 * key/value pairs that are found there. Since this file is
                 * not to be edited its assumed that these values are in the
                 * public domain and so there's no need to protect them.
                 *
                 * @return a map from the found keys to the linked values.
                 */
                idempotent omero::api::StringStringMap getConfigDefaults() throws ServerError;

                /**
                 * Retrieves configuration values like {@link #getConfigValues}
                 * but only those with the prefix <i>omero.client</i>.
                 *
                 * @return a map from the found keys to the linked values.
                 */
                idempotent omero::api::StringStringMap getClientConfigValues() throws ServerError;

                /**
                 * Reads the etc/omero.properties file and returns all the
                 * key/value pairs that are found there which match the prefix
                 * <i>omero.client</i>.
                 *
                 * @return a map from the found keys to the linked values.
                 */
                idempotent omero::api::StringStringMap getClientConfigDefaults() throws ServerError;

                /**
                 * Sets a configuration value in the backend store.
                 * Permissions applied to the configuration value may cause a
                 * {@link SecurityViolation} to be thrown. If the value is
                 * null or empty, then the configuration will be removed in
                 * all writable configuration sources. If the configuration is
                 * set in a non-modifiable source (e.g. in a property file on
                 * the classpath), then a subsequent call to
                 * {@link #getConfigValue} will return that value.
                 *
                 * @param key The non-null name of the desired configuration
                 *        value
                 * @param value The string value to assign to the given key.
                 * @throws ApiUsageException if the key is null or invalid.
                 * @throws SecurityViolation if the value is not writable.
                 */
                idempotent void setConfigValue(string key, string value) throws ServerError;

                /**
                 * Calls {@link #setConfigValue} if and only if the
                 * configuration property is currently equal to the test
                 * argument. If the test is null or empty, then the
                 * configuration property will be set only if missing.
                 *
                 * @param key
                 * @param value
                 * @throws ApiUsageException
                 * @throws SecurityViolation
                 * @see #setConfigValue
                 */
                idempotent bool setConfigValueIfEquals(string key, string value, string test) throws ServerError;

                /**
                 * Provides the UUID for this OMERO (database) instance. To
                 * make imports and exports function properly, only one
                 * physical database should be active with a given instance
                 * UUID. All other copies of the database with that UUID are
                 * invalid as soon as one modification is made.
                 *
                 * This value is stored in the configuration table under the
                 * key <i>omero.db.uuid</i>.
                 *
                 * @return String not null.
                 */
                idempotent string getDatabaseUuid() throws ServerError;

                /**
                 * Checks the database for it's time using a SELECT statement.
                 *
                 * @return Non-null {@link RTime} representation of the
                 *         database time.
                 * @throws InternalException though any call can throw an
                 *         InternalException it is more likely that this can
                 *         occur while contacting the DB. An exception here
                 *         most likely means (A) a temporary issue with the DB
                 *         or (B) a SQL dialect issue which must be corrected
                 *         by the Omero team.
                 */
                idempotent omero::RTime getDatabaseTime() throws ServerError;

                /**
                 * Checks the current server for its time. This value may be
                 * variant depending on whether the service is clustered or
                 * not.
                 *
                 * @return Non-null {@link RTime} representation of the
                 *         server's own time.
                 */
                idempotent omero::RTime getServerTime() throws ServerError;
            };

    };
};

#endif
