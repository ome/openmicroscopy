/*
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.impl;

import ome.api.IQuery;
import ome.services.blitz.util.BlitzExecutor;
import omero.ServerError;
import omero.api.AMD_IQuery_find;
import omero.api.AMD_IQuery_findAll;
import omero.api.AMD_IQuery_findAllByExample;
import omero.api.AMD_IQuery_findAllByFullText;
import omero.api.AMD_IQuery_findAllByQuery;
import omero.api.AMD_IQuery_findAllByString;
import omero.api.AMD_IQuery_findByExample;
import omero.api.AMD_IQuery_findByQuery;
import omero.api.AMD_IQuery_findByString;
import omero.api.AMD_IQuery_get;
import omero.api.AMD_IQuery_projection;
import omero.api.AMD_IQuery_refresh;
import omero.api._IQueryOperations;
import omero.model.IObject;
import omero.sys.Filter;
import omero.sys.Parameters;
import omero.util.IceMapper;

import Ice.Current;

/**
 * Implementation of the IQuery service.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta4
 * @see ome.api.IQuery
 */
public class QueryI extends AbstractAmdServant implements _IQueryOperations {

    public QueryI(IQuery service, BlitzExecutor be) {
        super(service, be);
    }

    // Interface methods
    // =========================================================================

    public void findAllByExample_async(AMD_IQuery_findAllByExample __cb,
            IObject example, Filter filter, Current __current)
            throws ServerError {
        callInvokerOnRawArgs(__cb, __current, example, filter);

    }

    public void findAllByFullText_async(AMD_IQuery_findAllByFullText __cb,
            String klass, String query, Parameters params, Current __current)
            throws ServerError {
        callInvokerOnRawArgs(__cb, __current, klass, query, params);

    }

    public void findAllByQuery_async(AMD_IQuery_findAllByQuery __cb,
            String query, Parameters params, Current __current)
            throws ServerError {
        callInvokerOnRawArgs(__cb, __current, query, params);

    }

    public void findAllByString_async(AMD_IQuery_findAllByString __cb,
            String klass, String field, String value, boolean caseSensitive,
            Filter filter, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, klass, field, value,
                caseSensitive, filter);

    }

    public void findAll_async(AMD_IQuery_findAll __cb, String klass,
            Filter filter, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, klass, filter);

    }

    public void findByExample_async(AMD_IQuery_findByExample __cb,
            IObject example, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, example);

    }

    public void findByQuery_async(AMD_IQuery_findByQuery __cb, String query,
            Parameters params, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, query, params);

    }

    public void findByString_async(AMD_IQuery_findByString __cb, String klass,
            String field, String value, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, klass, field, value);

    }

    public void find_async(AMD_IQuery_find __cb, String klass, long id,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, klass, id);

    }

    public void get_async(AMD_IQuery_get __cb, String klass, long id,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, klass, id);

    }

    public void refresh_async(AMD_IQuery_refresh __cb, IObject object,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, object);

    }

    public void projection_async(AMD_IQuery_projection __cb, String query,
            Parameters params, Current __current) throws ServerError {
        IceMapper mapper = new IceMapper(IceMapper.LISTOBJECTARRAY_TO_RTYPESEQSEQ);
        ome.parameters.Parameters p = mapper.convert(params);
        callInvokerOnMappedArgs(mapper, __cb, __current, query, p);
    }

}
