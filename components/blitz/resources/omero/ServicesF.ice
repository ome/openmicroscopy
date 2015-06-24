/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_SERVICESF_ICE
#define OMERO_SERVICESF_ICE

#include <omero/ServerErrors.ice>
#include <omero/System.ice>

module omero {

    module api {

       // Interface types

       /**
        * Service marker similar to ome.api.ServiceInterface. Any object which
        * IS-A ServiceInterface but IS-NOT-A StatefulServiceInterface (below)
        * is be definition a "stateless service"
        **/
       interface ServiceInterface
       {
       };

       sequence<ServiceInterface*> ServiceList;

       /**
        * Service marker for stateful services which permits the closing
        * of a particular service before the destruction of the session.
        **/
       ["ami", "amd"] interface StatefulServiceInterface extends ServiceInterface
       {
           /**
            * Causes the blitz server to store the service implementation to disk
            * to free memory. This is typically done automatically by the server
            * when a pre-defined memory limit is reached, but can be used by the
            * client if it clear that a stateful service will not be used for some
            * time.
            *
            * Activation will happen automatically whether passivation was done
            * manually or automatically.
            **/
           void passivate() throws ServerError;

           /**
            * Load a service implementation from disk if it was previously
            * passivated. It is unnecessary to call this method since activation
            * happens automatically, but calling this may prevent a short
            * lapse when the service is first accessed after passivation.
            *
            * It is safe to call this method at any time, even when the service
            * is not passivated.
            **/
           void activate() throws ServerError;

           /**
            * Frees all resources -- passivated or active -- for the given
            * stateful service and removes its name from the object adapter.
            * Any further method calls will fail with a Ice::NoSuchObjectException.
            *
            * Note: with JavaEE, the close method was called publically,
            * and internally this called destroy(). As of the OmeroBlitz
            * migration, this functionality has been combined.
            **/
            void close() throws ServerError;

           /**
            * To free clients from tracking the mapping from session to stateful
            * service, each stateful service can returns its own context information.
            **/
            idempotent omero::sys::EventContext getCurrentEventContext() throws ServerError;

        };

        // Forward definition of SSI
        interface StatefulServiceInterface;

        // Stateless

        interface IAdmin;
        interface IConfig;
        interface IContainer;
        interface ILdap;
        interface IMetadata;
        interface IPixels;
        interface IProjection;
        interface IQuery;
        interface IRoi;
        interface IScript;
        interface ISession;
        interface IShare;
        interface ITypes;
        interface IUpdate;
        interface IRenderingSettings;
        interface IRepositoryInfo;
        interface ITimeline;

        // Stateful

        interface Exporter;
        interface JobHandle;
        interface RawFileStore;
        interface RawPixelsStore;
        interface RenderingEngine;
        interface Search;
        interface ThumbnailStore;
    };

    module grid {
        interface ManagedRepository;
        interface ScriptProcessor;
        interface SharedResources;
        interface Table;
    };
};

#endif
