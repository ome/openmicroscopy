/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.impl;

import ome.api.ISession;
import ome.services.blitz.util.BlitzExecutor;
import omero.RType;
import omero.ServerError;
import omero.api.AMD_ISession_closeSession;
import omero.api.AMD_ISession_createSession;
import omero.api.AMD_ISession_createSessionWithTimeout;
import omero.api.AMD_ISession_getInput;
import omero.api.AMD_ISession_getInputKeys;
import omero.api.AMD_ISession_getOutput;
import omero.api.AMD_ISession_getOutputKeys;
import omero.api.AMD_ISession_getSession;
import omero.api.AMD_ISession_setInput;
import omero.api.AMD_ISession_setOutput;
import omero.api.AMD_ISession_updateSession;
import omero.api._ISessionOperations;
import omero.model.Session;
import omero.sys.Principal;
import omero.util.IceMapper;
import Ice.Current;

/**
 * Implementation of the ISession service.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta4
 * @see ome.api.ISession
 */
public class SessionI extends AbstractAmdServant implements _ISessionOperations {

    public SessionI(ISession service, BlitzExecutor be) {
        super(service, be);
    }

    // Interface methods
    // =========================================================================

    public void closeSession_async(AMD_ISession_closeSession __cb,
            Session sess, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, sess);

    }

    public void createSessionWithTimeout_async(
            AMD_ISession_createSessionWithTimeout __cb, Principal p,
            long seconds, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, p, seconds);

    }

    public void createSession_async(AMD_ISession_createSession __cb,
            Principal p, String credentials, Current __current)
            throws ServerError {
        callInvokerOnRawArgs(__cb, __current, p, credentials);

    }

    public void getInputKeys_async(AMD_ISession_getInputKeys __cb, String sess,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, sess);

    }

    public void getInput_async(AMD_ISession_getInput __cb, String sess,
            String key, Current __current) throws ServerError {
        IceMapper mapper = new IceMapper(IceMapper.OBJECT_TO_RTYPE);
        callInvokerOnMappedArgs(mapper, __cb, __current, sess, key);

    }

    public void getOutputKeys_async(AMD_ISession_getOutputKeys __cb,
            String sess, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, sess);

    }

    public void getOutput_async(AMD_ISession_getOutput __cb, String sess,
            String key, Current __current) throws ServerError {
        IceMapper mapper = new IceMapper(IceMapper.OBJECT_TO_RTYPE);
        callInvokerOnMappedArgs(mapper, __cb, __current, sess, key);
    }

    public void getSession_async(AMD_ISession_getSession __cb,
            String sessionUuid, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, sessionUuid);

    }

    public void setInput_async(AMD_ISession_setInput __cb, String sess,
            String key, RType value, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, sess, key, value);

    }

    public void setOutput_async(AMD_ISession_setOutput __cb, String sess,
            String key, RType value, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, sess, key, value);

    }

    public void updateSession_async(AMD_ISession_updateSession __cb,
            Session sess, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, sess);

    }

}
