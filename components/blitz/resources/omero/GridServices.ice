/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_GRIDSERVICES_ICE
#define OMERO_GRIDSERVICES_ICE

#include <omero/Repositories.ice>
#include <omero/Scripts.ice>
#include <omero/Tables.ice>

module omero {

    module grid {

        /**
         * Stateless service provided by each Blitz server for acquiring
         * shared resources in the OmeroGrid. Unlike the other services
         * provided by ServiceFactory instances, it is not guaranteed
         * that a service instance will be returned if the resource is
         * not free. In that case, a null will be returned.
         **/
        interface GridServices {

            /**
             * Waits up to seconds to acquire a slot in a processor
             * which can handle the given job.
             **/
	    omero::grid::InteractiveProcessor*
		acquireProcessor(omero::model::Job job, int seconds)
		throws ServerError;

            /**
             * Returns a map between Repository descriptions (omero::model::Repository
             * instances) and RepositoryPrx instances (possibly null).
             **/
            omero::grid::RepositoryMap
                acquireRepositories()
		throws ServerError;

            /**
             * Waits up to seconds to acquire an exclusive write lock
             * on the given Table, and returns null if the lock cannot
             * be obtained. After that it is possible to use volatile
             * table in read-only mode.
             *
             * If the OriginalFile does not have an id set (is non-managed),
             * then an OriginalFile object with Format "OMERO.tables" will
             * be created. Only the name and the annotation links of the
             * file object need to be filled out.
             */
            omero::grid::Table*
                acquireWritableTable(omero::model::OriginalFile file, int seconds)
                throws ServerError;

            /**
             * Returns a Table instance which may be read-only. If it is, then
             * any mutators will throw an exception, and if a mutator is called
             * on the same file by another user/thread/process, any read method
             * will throw a ConcurrentModificationException. If the service is
             * writable, it functions exactly like a call to acquireWritableTable
             * which succeeded.
             */
            omero::grid::Table*
                acquireTable(omero::model::OriginalFile file)
                throws ServerError;

        };

    };

};

#endif
