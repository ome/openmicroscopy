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
import omero.api.AMD_ISession_createSessionWithTimeouts;
import omero.api.AMD_ISession_createToken;
import omero.api.AMD_ISession_createUserSession;
import omero.api.AMD_ISession_getInput;
import omero.api.AMD_ISession_getInputKeys;
import omero.api.AMD_ISession_getInputs;
import omero.api.AMD_ISession_getMyOpenAgentSessions;
import omero.api.AMD_ISession_getMyOpenClientSessions;
import omero.api.AMD_ISession_getMyOpenSessions;
import omero.api.AMD_ISession_getOutput;
import omero.api.AMD_ISession_getOutputKeys;
import omero.api.AMD_ISession_getOutputs;
import omero.api.AMD_ISession_getReferenceCount;
import omero.api.AMD_ISession_getSession;
import omero.api.AMD_ISession_setInput;
import omero.api.AMD_ISession_setOutput;
import omero.api._ISessionOperations;
import omero.model.Session;
import omero.sys.Principal;
import omero.util.IceMapper;
import omero.util.RTypeMapper;
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
            long ttlMs, Current __current) throws ServerError, Glacier2.CannotCreateSessionException {
        callInvokerOnRawArgs(__cb, __current, p, ttlMs);

    }

    public void createSessionWithTimeouts_async(
            AMD_ISession_createSessionWithTimeouts __cb, Principal p,
            long ttlMs, long ttiMs, Current __current) throws ServerError, Glacier2.CannotCreateSessionException {
        callInvokerOnRawArgs(__cb, __current, p, ttlMs, ttiMs);

    }

    public void createSession_async(AMD_ISession_createSession __cb,
            Principal p, String credentials, Current __current)
            throws ServerError, Glacier2.CannotCreateSessionException {
        callInvokerOnRawArgs(__cb, __current, p, credentials);
    }

    public void createToken_async(AMD_ISession_createToken __cb, long arg0,
            String arg2, Ice.Current __current)
            throws ServerError, Glacier2.CannotCreateSessionException {
        callInvokerOnRawArgs(__cb, __current, arg0, arg2);
    }

    public void createUserSession_async(AMD_ISession_createUserSession __cb, long arg0,
            long arg1, String arg2, Ice.Current __current)
            throws ServerError, Glacier2.CannotCreateSessionException {
        callInvokerOnRawArgs(__cb, __current, arg0, arg1, arg2);
    }

    public void getInputKeys_async(AMD_ISession_getInputKeys __cb, String sess,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, sess);

    }

    public void getInput_async(AMD_ISession_getInput __cb, String sess,
            String key, Current __current) throws ServerError {
        RTypeMapper mapper = new RTypeMapper(IceMapper.OBJECT_TO_RTYPE);
        callInvokerOnMappedArgs(mapper, __cb, __current, sess, key);

    }

    public void getOutputKeys_async(AMD_ISession_getOutputKeys __cb,
            String sess, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, sess);

    }

    public void getOutput_async(AMD_ISession_getOutput __cb, String sess,
            String key, Current __current) throws ServerError {
        RTypeMapper mapper = new RTypeMapper(IceMapper.OBJECT_TO_RTYPE);
        callInvokerOnMappedArgs(mapper, __cb, __current, sess, key);
    }

    public void getSession_async(AMD_ISession_getSession __cb,
            String sessionUuid, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, sessionUuid);

    }

    public void getReferenceCount_async(AMD_ISession_getReferenceCount __cb,
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

    public void getInputs_async(AMD_ISession_getInputs __cb, String sess,
            Current __current) throws ServerError {
        RTypeMapper mapper = new RTypeMapper(IceMapper.RTYPEDICT);
        callInvokerOnMappedArgs(mapper, __cb, __current, sess);
    }
    public void getOutputs_async(AMD_ISession_getOutputs __cb, String sess,
            Current __current) throws ServerError {
        RTypeMapper mapper = new RTypeMapper(IceMapper.RTYPEDICT);
        callInvokerOnMappedArgs(mapper, __cb, __current, sess);
    }

    public void getMyOpenAgentSessions_async(
            AMD_ISession_getMyOpenAgentSessions __cb, String agent,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, agent);
    }

    public void getMyOpenClientSessions_async(
            AMD_ISession_getMyOpenClientSessions __cb, Current __current)
            throws ServerError {
        callInvokerOnRawArgs(__cb, __current);
    }

    public void getMyOpenSessions_async(AMD_ISession_getMyOpenSessions __cb,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current);
    }
}
