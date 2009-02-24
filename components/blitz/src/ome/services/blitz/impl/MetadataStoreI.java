/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.impl;

import java.util.Map;

import ome.formats.OMEROMetadataStore;
import ome.model.IObject;
import ome.services.blitz.util.BlitzExecutor;
import ome.services.blitz.util.BlitzOnly;
import ome.services.blitz.util.ServiceFactoryAware;
import ome.services.throttling.Adapter;
import ome.services.util.Executor;
import ome.system.OmeroContext;
import ome.system.ServiceFactory;
import ome.tools.spring.InternalServiceFactory;
import omero.RBool;
import omero.RDouble;
import omero.RFloat;
import omero.RInt;
import omero.RLong;
import omero.RString;
import omero.RType;
import omero.ServerError;
import omero.api.*;
import omero.metadatastore.IObjectContainer;
import omero.model.PlaneInfo;
import omero.model.Project;
import omero.util.IceMapper;

import org.hibernate.Session;
import org.springframework.transaction.annotation.Transactional;

import Ice.Current;
import Ice.UserException;

/**
 */
public class MetadataStoreI extends AbstractAmdServant implements
        _MetadataStoreOperations, ServiceFactoryAware, BlitzOnly {

    protected OMEROMetadataStore store;

    protected ServiceFactoryI sf;

    public MetadataStoreI(final BlitzExecutor be) throws Exception {
        super(null, be);
    }

    public void setServiceFactory(ServiceFactoryI sf) throws ServerError {
        this.sf = sf;
    }

    @Override
    public void onSetOmeroContext(final OmeroContext ctx) throws Exception {
        ServiceFactory sf = new InternalServiceFactory(ctx);
        this.store = new OMEROMetadataStore(sf);
    }

    @SuppressWarnings("unchecked")
    private <T extends IObject> T safeReverse(Object o, IceMapper mapper) {
        try {
            return (T) mapper.reverse(o);
        } catch (Exception e) {
            throw new RuntimeException("Failed to safely reverse: " + o);
        }
    }

    // ~ Service methods
    // =========================================================================

    public void createRoot_async(final AMD_MetadataStore_createRoot __cb,
            final Current __current) throws ServerError {

        final IceMapper mapper = new IceMapper(IceMapper.VOID);
        runnableCall(__current, new Adapter(__cb, __current, mapper,
                this.sf.executor, this.sf.principal, new Executor.SimpleWork(
                        this, "createRoot") {
                    @Transactional(readOnly = true)
                    public Object doWork(Session session, ServiceFactory sf) {
                        store.createRoot();
                        return null;
                    }
                }));
    }

    public void populateMinMax_async(
            final AMD_MetadataStore_populateMinMax __cb, final RLong id,
            final RInt i, final Current __current) throws ServerError {

        final IceMapper mapper = new IceMapper(IceMapper.VOID);
        runnableCall(__current, new Adapter(__cb, __current, mapper,
                this.sf.executor, this.sf.principal, new Executor.SimpleWork(
                        this, "populateMinMax") {
                    @Transactional(readOnly = false)
                    public Object doWork(Session session, ServiceFactory sf) {

                        store.populateMinMax(toJavaType(id), toJavaType(i));
                        return null;
                    }
                }));
    }

    public void saveToDB_async(final AMD_MetadataStore_saveToDB __cb,
            final Current __current) throws ServerError {

        final IceMapper mapper = new IceMapper(IceMapper.FILTERABLE_COLLECTION);
        runnableCall(__current, new Adapter(__cb, __current, mapper,
                this.sf.executor, this.sf.principal, new Executor.SimpleWork(
                        this, "saveToDb") {
                    @Transactional(readOnly = false)
                    public Object doWork(Session session, ServiceFactory sf) {

                        return store.saveToDB();
                    }
                }));
    }

    public void setChannelGlobalMinMax_async(
            final AMD_MetadataStore_setChannelGlobalMinMax __cb,
            final int channelIdx, final double globalMin,
            final double globalMax, final int pixelsIndex,
            final Current __current) throws ServerError {

        final IceMapper mapper = new IceMapper(IceMapper.VOID);
        runnableCall(__current, new Adapter(__cb, __current, mapper,
                this.sf.executor, this.sf.principal, new Executor.SimpleWork(
                        this, "setChannelGlobalMinMax") {
                    @Transactional(readOnly = true)
                    public Object doWork(Session session, ServiceFactory sf) {

                        store.setChannelGlobalMinMax(channelIdx, globalMin,
                                globalMax, pixelsIndex);
                        return null;
                    }
                }));
    }

    public void updateObjects_async(AMD_MetadataStore_updateObjects __cb,
            final IObjectContainer[] objects, Current __current)
            throws ServerError {
        final IceMapper mapper = new IceMapper(IceMapper.VOID);
        runnableCall(__current, new Adapter(__cb, __current, mapper,
                this.sf.executor, this.sf.principal, new Executor.SimpleWork(
                        this, "updateObjects") {
                    @Transactional(readOnly = true)
                    public Object doWork(Session session, ServiceFactory sf) {
                        for (IObjectContainer o : objects) {
                            IObject sourceObject;
                            try {
                                sourceObject = (IObject) mapper
                                        .reverse(o.sourceObject);
                            } catch (Exception e) {
                                // TODO: This is **WRONG**; exception handling
                                // here is messed up.
                                throw new RuntimeException(e);
                            }
                            store.updateObject(o.LSID, sourceObject, o.indexes);
                        }
                        return null;
                    }
                }));
    }

    public void updateReferences_async(AMD_MetadataStore_updateReferences __cb,
            final Map<String, String> references, Current __current)
            throws ServerError {
        final IceMapper mapper = new IceMapper(IceMapper.VOID);
        runnableCall(__current, new Adapter(__cb, __current, mapper,
                this.sf.executor, this.sf.principal, new Executor.SimpleWork(
                        this, "updateReferences") {
                    @Transactional(readOnly = true)
                    public Object doWork(Session session, ServiceFactory sf) {
                        store.updateReferences(references);
                        return null;
                    }
                }));
    }

    /**
     * Transforms an OMERO RType into the corresponding Java type.
     * 
     * @param x
     *            OMERO RType value.
     * @return Java type or <code>null</code> if <code>x</code> is
     *         <code>null</code>.
     */
    public Integer toJavaType(RInt x) {
        return x == null ? null : x.getValue();
    }

    /**
     * Transforms an OMERO RType into the corresponding Java type.
     * 
     * @param x
     *            OMERO RType value.
     * @return Java type or <code>null</code> if <code>x</code> is
     *         <code>null</code>.
     */
    public Long toJavaType(RLong x) {
        return x == null ? null : x.getValue();
    }

    /**
     * Transforms an OMERO RType into the corresponding Java type.
     * 
     * @param x
     *            OMERO RType value.
     * @return Java type or <code>null</code> if <code>x</code> is
     *         <code>null</code>.
     */
    public Boolean toJavaType(RBool x) {
        return x == null ? null : x.getValue();
    }

    /**
     * Transforms an OMERO RType into the corresponding Java type.
     * 
     * @param x
     *            OMERO RType value.
     * @return Java type or <code>null</code> if <code>x</code> is
     *         <code>null</code>.
     */
    public Float toJavaType(RFloat x) {
        return x == null ? null : x.getValue();
    }

    /**
     * Transforms an OMERO RType into the corresponding Java type.
     * 
     * @param x
     *            OMERO RType value.
     * @return Java type or <code>null</code> if <code>x</code> is
     *         <code>null</code>.
     */
    public Double toJavaType(RDouble x) {
        return x == null ? null : x.getValue();
    }

    /**
     * Transforms an OMERO RType into the corresponding Java type.
     * 
     * @param x
     *            OMERO RType value.
     * @return Java type or <code>null</code> if <code>x</code> is
     *         <code>null</code>.
     */
    public String toJavaType(RString x) {
        return x == null ? null : x.getValue();
    }

    // Stateful interface methods
    // =========================================================================

    public void activate_async(AMD_StatefulServiceInterface_activate __cb,
            Current __current) {
        final IceMapper mapper = new IceMapper(IceMapper.VOID);
        runnableCall(__current, new Adapter(__cb, __current, mapper,
                this.sf.executor, this.sf.principal, new Executor.SimpleWork(
                        this, "activate") {
                    @Transactional(readOnly = true)
                    public Object doWork(Session session, ServiceFactory sf) {
                        // Do nothing for now.
                        return null;
                    }
                }));

    }

    public void passivate_async(AMD_StatefulServiceInterface_passivate __cb,
            Current __current) {
        final IceMapper mapper = new IceMapper(IceMapper.VOID);
        runnableCall(__current, new Adapter(__cb, __current, mapper,
                this.sf.executor, this.sf.principal, new Executor.SimpleWork(
                        this, "passivate") {
                    @Transactional(readOnly = true)
                    public Object doWork(Session session, ServiceFactory sf) {
                        // Do nothing for now.
                        return null;
                    }
                }));

    }

    public void close_async(final AMD_StatefulServiceInterface_close __cb,
            final Current __current) throws ServerError {

        final IceMapper mapper = new IceMapper(IceMapper.VOID);
        runnableCall(__current, new Adapter(__cb, __current, mapper,
                this.sf.executor, this.sf.principal, new Executor.SimpleWork(
                        this, "close") {
                    @Transactional(readOnly = true)
                    public Object doWork(Session session, ServiceFactory sf) {

                        // Nulling should be sufficient.
                        store = null;
                        return null;
                    }
                }));
    }

    public void getCurrentEventContext_async(
            final AMD_StatefulServiceInterface_getCurrentEventContext __cb,
            final Current __current) throws ServerError {

        final IceMapper mapper = new IceMapper(new IceMapper.ReturnMapping() {

            public Object mapReturnValue(IceMapper mapper, Object value)
                    throws UserException {
                return mapper.convert((ome.system.EventContext) value);
            }
        });

        runnableCall(__current, new Adapter(__cb, __current, mapper,
                this.sf.executor, this.sf.principal, new Executor.SimpleWork(
                        this, "getCurrentEventContext") {
                    @Transactional(readOnly = true)
                    public Object doWork(Session session, ServiceFactory sf) {
                        return null;
                    }
                }));
    }

}
