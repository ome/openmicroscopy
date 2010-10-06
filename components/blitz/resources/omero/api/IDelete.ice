/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_API_IDELETE_ICE
#define OMERO_API_IDELETE_ICE

#include <omero/ModelF.ice>
#include <omero/ServicesF.ice>
#include <omero/Collections.ice>


module omero {

    module api {

        /**
         * Exploratory Delete API for background processing of deletes.
         *
         * For more information, see <a href="https://trac.openmicroscopy.org.uk/omero/ticket/2665">ticket #2665</a>.
         **/
        module delete {

            /**
             * String constant used as the value of an option key/value pair, implying
             * that the given section of the graph should be orphaned rather than be
             * deleted.
             **/
            const string ORPHAN = "ORPHAN";

            /**
             * String constant used as the value of an option key/value pair, implying
             * that the given section of the graph should be deleted if it would otherwise
             * be orphaned.
             **/
            const string REAP = "REAP";

            /**
             * String constant used as the value of an option key/value pair, implying
             * that the given section of the graph should be deleted, but if not possible
             * the transaction should suceed anyway.
             **/
            const string SOFT = "SOFT";

            /**
             * String constant used as the value of an option key/value pair, implying
             * that the given section of the graph must be deleted, otherwise the transaction
             * should be rolled back.
             **/
            const string HARD = "HARD";

            /**
             * Command object which represents a single delete request. Several of these
             * can be passed to the [IDelete::queueDelete] method at once. All will occur
             * in the same transation.
             **/
            struct DeleteCommand {

                /**
                 * Describes a type which will be deleted. Type strings may either be OMERO classes like
                 * "Image" or "Dataset", but may also represent larger graphs of objects which should be
                 * deleted, e.g. "/Image/Acquisition" which would remove all of the acquisition data
                 * associated to the image with the given id.
                 **/
                string type;

                /**
                 * Id of the object object which is either the type or the root of the graph given by [type].
                 **/
                long id;

                /**
                 * Key/value pairs which represent options on how to properly walk the graph provided by [type].
                 *
                 * <pre>
                 * deleteCommand.options = {"/Image/Tags":REAP}
                 * </pre>
                 * would only delete the tags associated with the image if they would otherwise be orphaned.
                 **/
                StringStringMap options;

            };

            sequence<DeleteCommand> DeleteCommands;

            /**
             * Returned by [IDelete] to allow managing of queued delete operations.
             **/
            interface DeleteHandle {

                /**
                 * Returns the [DeleteCommand] instances which were used to create
                 * this handle.
                 */
                DeleteCommands commands() throws ServerError;

                /**
                 * Returns when processing of all commands has completed, whether successfully
                 * or not.
                 **/
                bool finished() throws ServerError;

                /**
                 * Returns the number of errors that were encountered. If greater than
                 * zero, then the transaction was rolled back. The [report] method may
                 * still return non-empty messages if there were any warnings.
                 **/
                int errors() throws ServerError;

                /**
                 * Returns a report of what happened. Any errors will produce output as well
                 * as any warnings, such as "Tag was orphaned" when using the "SOFT" option.
                 **/
                StringSet report() throws ServerError;

                /**
                 * Starts further [DeleteCommand]s from being processed and rolls back any
                 * changes already made by the transaction. If all commands were already
                 * processed, this method returns false to signal that the rollback was not
                 * possible.
                 **/
                bool cancel() throws ServerError;

                /**
                 * Removes the proxy from the server's adapter. Any calls on this
                 * proxy after close will receive an [Ice::ObjectNotExistException].
                 **/
                void close() throws ServerError;
            };

        };

        /**
         * See <a href="http://hudson.openmicroscopy.org.uk/job/OMERO/javadoc/ome/api/IDelete.html">IDelete.html</a>
         **/
        ["ami", "amd"] interface IDelete extends omero::api::ServiceInterface
            {
                omero::api::IObjectList checkImageDelete(long id, bool force) throws ServerError;
                omero::api::IObjectList previewImageDelete(long id, bool force) throws ServerError;
                void deleteImage(long id, bool force) throws ApiUsageException, ValidationException, SecurityViolation, ServerError;
                void deleteImages(LongList ids, bool force) throws ApiUsageException, ValidationException, SecurityViolation, ServerError;
                void deleteImagesByDataset(long datasetId, bool force) throws ApiUsageException, ValidationException, SecurityViolation, ServerError;
                void deleteSettings(long imageId) throws ServerError;
                void deletePlate(long plateId) throws ServerError;

                /**
                 * Returns a list of [DeleteCommand] instances with type and options filled,
                 * but whose id value can be ignored.
                 **/
                omero::api::delete::DeleteCommands availableCommands() throws ServerError;

                /**
                 * Queue multiple deletes for later execution. The [DeleteHandle] instance can
                 * be queried for the state of the delete.
                 **/
                omero::api::delete::DeleteHandle* queueDelete(omero::api::delete::DeleteCommands commands) throws ApiUsageException, ServerError;
            };
    };
};

#endif
