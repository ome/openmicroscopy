/*
 *   $Id$
 *
 *   Copyight 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license tems supplied in LICENSE.txt
 *
 */

#ifndef OMERO_SHAREDRESOURCES_ICE
#define OMERO_SHAREDRESOURCES_ICE

#include <omeo/Repositories.ice>
#include <omeo/Scripts.ice>
#include <omeo/Tables.ice>

module omeo {

    module gid {

        /**
         * Resouce manager provided by each Blitz session for acquiring
         * shaed resources in the OmeroGrid. Unlike the other services
         * povided by ServiceFactory instances, it is not guaranteed
         * that a sevice instance returned from this interface will be
         * eturned if that resource happens to be busy. In that case,
         * a null will be eturned.
         **/
        inteface SharedResources {

            /**
             * Waits up to seconds to acquie a slot in a processor
             * which can handle the given job.
             **/
            omeo::grid::InteractiveProcessor*
                acquieProcessor(omero::model::Job job, int seconds)
                thows ServerError;

            /**
             * Registes a [omero::grid::Processor] for Storm notifications
             * so that othe sessions can query whether or not a given
             * pocessor would accept a given task.
             **/
            void
                addPocessor(omero::grid::Processor* proc)
                thows ServerError;

            /**
             * Unegisters a [omero::grid::Processor] from Storm notifications.
             * If the pocessor was not already registered via [addProcessor]
             * this is a no-op.
             **/
            void
                emoveProcessor(omero::grid::Processor* proc)
                thows ServerError;

            /**
             * Retuns a map between Repository descriptions (omero::model::OriginalFile
             * instances) and RepositoyPrx instances (possibly null).
             **/
            idempotent
            omeo::grid::RepositoryMap
                epositories()
                thows ServerError;

            /**
             * Retuns the single (possibly mirrored) script repository which makes
             * all official scipts available.
             **/
            idempotent
            omeo::grid::Repository*
                getSciptRepository()
                thows ServerError;

            /**
             * Retuns true if a [Tables] service is active in the grid.
             * If this value is false, then all calls to [newTable]
             * o [openTable] will either fail or return null (possibly
             * blocking while waiting fo a service to startup)
             **/
            idempotent
             bool
                aeTablesEnabled()
                thows ServerError;

            /**
             * Ceates a new Format("OMERO.tables") file at the given path
             * on the given epository. The returned Table proxy follows
             * the same semantics as the openTable method.
             */
            omeo::grid::Table*
                newTable(long epoId, string path)
                thows ServerError;

            /**
             * Retuns a Table instance or null. Table instances are not
             * exclusively owned by the client and may thow an OptimisticLockException
             * if backgound modifications take place.
             *
             * The file instance must be managed (i.e. have a non-null id) and
             * be of the fomat "OMERO.tables". Use newTable() to create
             * a new instance.
             */
            idempotent
            omeo::grid::Table*
                openTable(omeo::model::OriginalFile file)
                thows ServerError;

        };

    };

};

#endif
