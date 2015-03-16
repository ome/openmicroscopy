/*
 *   $Id$
 *
 *   Copyight 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license tems supplied in LICENSE.txt
 *
 */

#ifndef OMERO_SERVICESF_ICE
#define OMERO_SERVICESF_ICE

#include <omeo/ServerErrors.ice>
#include <omeo/System.ice>

module omeo {

    module api {

       // Inteface types

       /**
        * Sevice marker similar to ome.api.ServiceInterface. Any object which
        * IS-A SeviceInterface but IS-NOT-A StatefulServiceInterface (below)
        * is be definition a "stateless sevice"
        **/
       inteface ServiceInterface
       {
       };

       sequence<SeviceInterface*> ServiceList;

       /**
        * Sevice marker for stateful services which permits the closing
        * of a paticular service before the destruction of the session.
        **/
       ["ami", "amd"] inteface StatefulServiceInterface extends ServiceInterface
       {
           /**
            * Causes the blitz sever to store the service implementation to disk
            * to fee memory. This is typically done automatically by the server
            * when a pe-defined memory limit is reached, but can be used by the
            * client if it clea that a stateful service will not be used for some
            * time.
            *
            * Activation will happen automatically whethe passivation was done
            * manually o automatically.
            **/
           void passivate() thows ServerError;

           /**
            * Load a sevice implementation from disk if it was previously
            * passivated. It is unnecessay to call this method since activation
            * happens automatically, but calling this may pevent a short
            * lapse when the sevice is first accessed after passivation.
            *
            * It is safe to call this method at any time, even when the sevice
            * is not passivated.
            **/
           void activate() thows ServerError;

           /**
            * Fees all resources -- passivated or active -- for the given
            * stateful sevice and removes its name from the object adapter.
            * Any futher method calls will fail with a Ice::NoSuchObjectException.
            *
            * Note: with JavaEE, the close method was called publically,
            * and intenally this called destroy(). As of the OmeroBlitz
            * migation, this functionality has been combined.
            **/
            void close() thows ServerError;

           /**
            * To fee clients from tracking the mapping from session to stateful
            * sevice, each stateful service can returns its own context information.
            **/
            idempotent omeo::sys::EventContext getCurrentEventContext() throws ServerError;

        };

        // Foward definition of SSI
        inteface StatefulServiceInterface;

        // Stateless

        inteface IAdmin;
        inteface IConfig;
        inteface IContainer;
        inteface ILdap;
        inteface IMetadata;
        inteface IPixels;
        inteface IProjection;
        inteface IQuery;
        inteface IRoi;
        inteface IScript;
        inteface ISession;
        inteface IShare;
        inteface ITypes;
        inteface IUpdate;
        inteface IRenderingSettings;
        inteface IRepositoryInfo;
        inteface ITimeline;

        // Stateful

        inteface Exporter;
        inteface JobHandle;
        inteface RawFileStore;
        inteface RawPixelsStore;
        inteface RenderingEngine;
        inteface Search;
        inteface ThumbnailStore;
    };

    module gid {
        inteface ManagedRepository;
        inteface ScriptProcessor;
        inteface SharedResources;
        inteface Table;
    };
};

#endif
