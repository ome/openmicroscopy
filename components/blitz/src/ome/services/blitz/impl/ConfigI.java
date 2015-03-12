/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.impl;

// Java imports
import ome.api.IConfig;
import ome.services.blitz.util.BlitzExecutor;
import omero.ServerError;
import omero.api.AMD_IConfig_getClientConfigDefaults;
import omero.api.AMD_IConfig_getClientConfigValues;
import omero.api.AMD_IConfig_getConfigDefaults;
import omero.api.AMD_IConfig_getConfigValue;
import omero.api.AMD_IConfig_getConfigValues;
import omero.api.AMD_IConfig_getDatabaseTime;
import omero.api.AMD_IConfig_getDatabaseUuid;
import omero.api.AMD_IConfig_getServerTime;
import omero.api.AMD_IConfig_getVersion;
import omero.api.AMD_IConfig_setConfigValue;
import omero.api.AMD_IConfig_setConfigValueIfEquals;
import omero.api._IConfigOperations;
import Ice.Current;

/**
 * Implementation of the IConfig service.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta4
 * @see ome.api.IConfig
 */
public class ConfigI extends AbstractAmdServant implements _IConfigOperations {

    public ConfigI(IConfig service, BlitzExecutor be) {
        super(service, be);
    }

    // Interface methods
    // =========================================================================

    public void getConfigValue_async(AMD_IConfig_getConfigValue __cb,
            String key, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, key);
    }

    public void getConfigValues_async(AMD_IConfig_getConfigValues __cb,
            String keyRegex, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, keyRegex);
    }

    public void getConfigDefaults_async(AMD_IConfig_getConfigDefaults __cb,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current);
    }

    public void getClientConfigValues_async(AMD_IConfig_getClientConfigValues __cb,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current);
    }

    public void getClientConfigDefaults_async(AMD_IConfig_getClientConfigDefaults __cb,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current);
    }

    public void getDatabaseTime_async(AMD_IConfig_getDatabaseTime __cb,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current);
    }
    
    public void getDatabaseUuid_async(AMD_IConfig_getDatabaseUuid __cb,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current);
    }

    public void getServerTime_async(AMD_IConfig_getServerTime __cb,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current);
    }

    public void getVersion_async(AMD_IConfig_getVersion __cb, Current __current)
            throws ServerError {
        callInvokerOnRawArgs(__cb, __current);
    }

    public void setConfigValue_async(AMD_IConfig_setConfigValue __cb,
            String key, String value, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, key, value);
    }

    public void setConfigValueIfEquals_async(
            AMD_IConfig_setConfigValueIfEquals __cb, String key, String value,
            String test, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, key, value, test);
    }

}
