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
         * For more information, see <a href="https://trac.openmicroscopy.org.uk/ome/ticket/2665">ticket #2665</a>.
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
            ["deprecated: use omero::cmd::Delete instead"]
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
             * Status object which is returned for each [DeleteCommand] passed to
             * [IDelete::queueCommand].
             **/
            ["deprecated: use omero::cmd::DeleteRsp instead"]
            class DeleteReport {

                /**
                 * The command object itself.
                 **/
                DeleteCommand command;

                /**
                 * Primary feedback mechanism. If this value is non-empty, then
                 * there was an error during processing of this command. This will
                 * cause [DeleteHandle::errors] to return a value higher than 0
                 * and the entire transaction, including all [DeleteCommand]s.
                 **/
                string error;

                /**
                 * Extra feedback mechanism. Typically will only be non-empty
                 * if the error is empty. This implies that some situation was
                 * encountered that the user may need to be informed of (e.g.
                 * some annotation wasn't deleted), but which was non-critical.
                 **/
                string warning;

                /**
                 * Map from type name ("Thumbnail", "Pixels", "OriginalFile") to
                 * a list of ids for any binary files which did not get dieleted.
                 *
                 * Some action may be desired by the user to guarantee that this
                 * server-space is eventually
                 **/
                omero::api::IdListMap undeletedFiles;

                /**
                 * Number of steps that this [DeleteCommand] requires.
                 **/
                int steps;

                /**
                 * Number of objects that this [DeleteCommand] will attempt
                 * to delete.
                 **/
                long scheduledDeletes;

                /**
                 * Number of actual deletes which took place.
                 **/
                long actualDeletes;

                //
                // Timing information
                //

                /**
                 * Server time (in milliseconds since the epoch) at
                 * which processing of [DeleteCommand] began.
                 **/
                long start;

                /**
                 * Server time (in milliseconds since the epoch) at
                 * which processing of this step was started.
                 **/
                omero::api::LongArray stepStarts;

                /**
                 * Server time (in milliseconds since the epoch) at
                 * which processing of this step was finished.
                 **/
                omero::api::LongArray stepStops;

                /**
                 * Server time (in milliseconds since the epoch) at
                 * which processing of [DeleteCommand] was finished.
                 **/
                long stop;

            };

            sequence<DeleteReport> DeleteReports;

            /**
             * Returned by [IDelete] to allow managing of queued delete operations.
             **/
            ["deprecated: use omero::cmd::Handle instead"]
            interface DeleteHandle {

                /**
                 * Returns the [DeleteCommand] instances which were used to create
                 * this handle.
                 */
                DeleteCommands commands() throws ServerError;

                /**
                 * Returns a report of what happened. Any errors will produce output as well
                 * as any warnings, such as "Tag was orphaned" when using the "SOFT" option.
                 * An error can be detected by a non-empty string for the [DeleteReport::error]
                 * field.
                 **/
                DeleteReports report() throws ServerError;

                /**
                 * Returns whether processing of all commands has completed, whether successfully
                 * or not.
                 **/
                bool finished() throws ServerError;

                /**
                 * Returns the number of errors that were encountered. If greater than
                 * zero, then the transaction was rolled back.
                 **/
                int errors() throws ServerError;

                /**
                 * Prevents further [DeleteCommand]s from being processed and rolls back any
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
        ["deprecated: use omero::cmd::Delete instead", "ami", "amd"]
        interface IDelete extends omero::api::ServiceInterface
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
