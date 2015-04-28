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

        ["ami", "amd"] interface IConfig extends ServiceInterface
            {
                idempotent string getVersion() throws ServerError;
                idempotent string getConfigValue(string key) throws ServerError;
                idempotent omero::api::StringStringMap getConfigValues(string keyRegex) throws ServerError;
                idempotent omero::api::StringStringMap getConfigDefaults() throws ServerError;
                idempotent omero::api::StringStringMap getClientConfigValues() throws ServerError;
                idempotent omero::api::StringStringMap getClientConfigDefaults() throws ServerError;
                idempotent void setConfigValue(string key, string value) throws ServerError;
                idempotent bool setConfigValueIfEquals(string key, string value, string test) throws ServerError;
                idempotent string getDatabaseUuid() throws ServerError;
                idempotent omero::RTime getDatabaseTime() throws ServerError;
                idempotent omero::RTime getServerTime() throws ServerError;
            };

    };
};

#endif
