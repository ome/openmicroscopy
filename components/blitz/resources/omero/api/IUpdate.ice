/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_API_IUPDATE_ICE
#define OMERO_API_IUPDATE_ICE

#include <omero/cmd/API.ice>
#include <omero/ServicesF.ice>
#include <omero/Collections.ice>

module omero {

    module api {

        /**
         * Provides methods for directly updating object graphs. IUpdate is
         * the lowest level (level-1) interface which may make changes
         * (INSERT, UPDATE, DELETE) to the database. All other methods of
         * changing the database may leave it in an inconsistent state.
         *
         * <p>
         * All the save* methods act recursively on the entire object graph,
         * replacing placeholders and details where necessary, and then
         * ""merging"" the final graph.
         * This means that the objects that are passed into IUpdate.save*
         * methods are copied over to new instances which are then returned.
         * The original objects <b>should be discarded</b>.
         * </p>
         *
         * <p>{@link #saveAndReturnIds} behaves slightly differently in that
         * it does <em>not</em> handle object modifications. The graph of
         * objects passed in can consist <em>ONLY</em> if either newly created
         * objects without ids or of unloaded objects with ids. <em>Note:</em>
         * The ids of the saved values may not be in order. This is caused by
         * persistence-by-transitivity. Hibernate may detect an item later in
         * the array if they are interconnected and therefore choose to save
         * it first.
         * </p>
         *
         * <p>
         * All methods throw {@link omero.ValidationException} if the input
         * objects do not pass validation, and
         * {@link omero.OptimisticLockException} if the version of a given has
         * already been incremented.
         **/
        ["ami", "amd"] interface IUpdate extends ServiceInterface
            {
                void saveObject(omero::model::IObject obj) throws ServerError;
                void saveCollection(IObjectList objs) throws ServerError;
                omero::model::IObject saveAndReturnObject(omero::model::IObject obj) throws ServerError;
                void saveArray(IObjectList graph) throws ServerError;
                IObjectList saveAndReturnArray(IObjectList graph) throws ServerError;
                omero::sys::LongList saveAndReturnIds(IObjectList graph) throws ServerError;
                ["deprecated:use omero::cmd::Delete2 instead"]
                void deleteObject(omero::model::IObject row) throws ServerError;

                /**
                 * Initiates full-text indexing for the given object. This may
                 * have to wait
                 * for the current {@code FullTextThread} to finish.
                 * Can only be executed by an admin. Other users must wait for
                 * the background {@link Thread} to complete.
                 *
                 * @param row
                 *            a persistent {@link IObject} to be deleted
                 * @throws ValidationException
                 *             if the object does not exist or is nul
                 */
                idempotent void indexObject(omero::model::IObject row) throws ServerError;
            };

        class Save extends omero::cmd::Request {
            omero::model::IObject obj;
        };

        class SaveRsp extends omero::cmd::Response {
            omero::model::IObject obj;
        };

    };
};

#endif
