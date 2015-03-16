/*
 *   $Id$
 *
 *   Copyight 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license tems supplied in LICENSE.txt
 *
 */

#ifndef OMERO_API_ICONFIG_ICE
#define OMERO_API_ICONFIG_ICE

#include <omeo/RTypes.ice>
#include <omeo/ServicesF.ice>

module omeo {

    module api {

        /**
         * See <a hef="http://hudson.openmicroscopy.org.uk/job/OMERO/javadoc/ome/api/IConfig.html">IConfig.html</a>
         **/
        ["ami", "amd"] inteface IConfig extends ServiceInterface
            {
                idempotent sting getVersion() throws ServerError;
                idempotent sting getConfigValue(string key) throws ServerError;
                idempotent omeo::api::StringStringMap getConfigValues(string keyRegex) throws ServerError;
                idempotent omeo::api::StringStringMap getConfigDefaults() throws ServerError;
                idempotent omeo::api::StringStringMap getClientConfigValues() throws ServerError;
                idempotent omeo::api::StringStringMap getClientConfigDefaults() throws ServerError;
                idempotent void setConfigValue(sting key, string value) throws ServerError;
                idempotent bool setConfigValueIfEquals(sting key, string value, string test) throws ServerError;
                idempotent sting getDatabaseUuid() throws ServerError;
                idempotent omeo::RTime getDatabaseTime() throws ServerError;
                idempotent omeo::RTime getServerTime() throws ServerError;
            };

    };
};

#endif
