/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_SHAREDRESOURCES_ICE
#define OMERO_SHAREDRESOURCES_ICE

#include <omero/Repositories.ice>
#include <omero/Scripts.ice>
#include <omero/Tables.ice>

module omero {

    module grid {

        /**
         * Resource manager provided by each Blitz session for acquiring
         * shared resources in the OmeroGrid. Unlike the other services
         * provided by ServiceFactory instances, it is not guaranteed
         * that a service instance returned from this interface will be
         * returned if that resource happens to be busy. In that case,
         * a null will be returned.
         **/
        interface SharedResources {

            /**
             * Waits up to seconds to acquire a slot in a processor
             * which can handle the given job.
             **/
            omero::grid::InteractiveProcessor*
                acquireProcessor(omero::model::Job job, int seconds)
                throws ServerError;

            /**
             * Registers a [omero::grid::Processor] for Storm notifications
             * so that other sessions can query whether or not a given
             * processor would accept a given task.
             **/
            void
                addProcessor(omero::grid::Processor* proc)
                throws ServerError;

            /**
             * Unregisters a [omero::grid::Processor] from Storm notifications.
             * If the processor was not already registered via [addProcessor]
             * this is a no-op.
             **/
            void
                removeProcessor(omero::grid::Processor* proc)
                throws ServerError;

            /**
             * Returns a map between Repository descriptions (omero::model::OriginalFile
             * instances) and RepositoryPrx instances (possibly null).
             **/
            idempotent
            omero::grid::RepositoryMap
                repositories()
                throws ServerError;

            /**
             * Returns the single (possibly mirrored) script repository which makes
             * all official scripts available.
             **/
            idempotent
            omero::grid::Repository*
                getScriptRepository()
                throws ServerError;

            /**
             * Returns true if a [Tables] service is active in the grid.
             * If this value is false, then all calls to [newTable]
             * or [openTable] will either fail or return null (possibly
             * blocking while waiting for a service to startup)
             **/
            idempotent
             bool
                areTablesEnabled()
                throws ServerError;

            /**
             * Creates a new Format("OMERO.tables") file at the given path
             * on the given repository. The returned Table proxy follows
             * the same semantics as the openTable method.
             */
            omero::grid::Table*
                newTable(long repoId, string path)
                throws ServerError;

            /**
             * Returns a Table instance or null. Table instances are not
             * exclusively owned by the client and may throw an OptimisticLockException
             * if background modifications take place.
             *
             * The file instance must be managed (i.e. have a non-null id) and
             * be of the format "OMERO.tables". Use newTable() to create
             * a new instance.
             */
            idempotent
            omero::grid::Table*
                openTable(omero::model::OriginalFile file)
                throws ServerError;

        };

    };

};

#endif
