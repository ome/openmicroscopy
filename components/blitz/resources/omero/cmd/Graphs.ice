/*
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

#ifndef OMERO_CMD_GRAPHS_ICE
#define OMERO_CMD_GRAPHS_ICE

#include <omero/cmd/API.ice>
#include <omero/Collections.ice>

module omero {

    module cmd {

        /**
         * Options that modify GraphModify2 request execution.
         * By default, a user's related ""orphaned"" objects are typically
         * included in a request's operation. These options override that
         * behavior, allowing the client to specify whether to always or
         * never include given kinds of child object regardless of if they
         * are orphans.
         * For annotations, each override is limited to specific annotation
         * namespaces. (If no namespaces are specified, defaults apply
         * according to the configuration of the graph request factory.)
         **/
        module graphs {

            /**
             * How GraphModify2 requests should deal with kinds of children,
             * related to the target objects.
             * By default, it is usual for only orphans to be operated on.
             * At least one of includeType or excludeType must be used;
             * if a type matches both, then it is included.
             * No more than one of includeNs and excludeNs may be used.
             **/
            class ChildOption {

                /**
                 * Include in the operation all children of these types.
                 * Cf. use of HARD in GraphModify's options.
                 **/
                omero::api::StringSet includeType;

                /**
                 * Include in the operation no children of these types.
                 * Cf. use of KEEP in GraphModify's options.
                 **/
                omero::api::StringSet excludeType;

                /**
                 * For annotations, limit the applicability of this option
                 * to only those in these namespaces.
                 **/
                omero::api::StringSet includeNs;

                /**
                 * For annotations, limit the applicability of this option
                 * to only those not in these namespaces.
                 **/
                omero::api::StringSet excludeNs;
            };

            /**
             * A list of if GraphModify2 requests should operate on
             * specific kinds of children.
             * Only the first applicable option takes effect.
             **/
            ["java:type:java.util.ArrayList<omero.cmd.graphs.ChildOption>:java.util.List<omero.cmd.graphs.ChildOption>"]
            sequence<ChildOption> ChildOptions;
        };

        /**
         * Base class for new requests for reading the model object graph.
         **/
        class GraphQuery extends Request {

            /**
             * The model objects upon which to operate.
             * Related model objects may also be targeted.
             **/
            omero::api::StringLongListMap targetObjects;
        };

        /**
         * Base class for new requests for modifying the model object graph.
         **/
        class GraphModify2 extends GraphQuery {

            /**
             * If the request should operate on specific kinds of children.
             * Only the first applicable option takes effect.
             **/
            graphs::ChildOptions childOptions;

            /**
             * If this request should skip the actual model object updates.
             * The response is still as if the operation actually occurred,
             * indicating what would have been done to which objects.
             **/
            bool dryRun;
        };

        /**
         * Move model objects into a different experimenter group.
         * The user must be either an administrator,
         * or the owner of the objects and a member of the target group.
         **/
        class Chgrp2 extends GraphModify2 {

            /**
             * The ID of the experimenter group into which to move the model
             * objects.
             **/
            long groupId;
        };

        /**
         * Result of moving model objects into a different experimenter
         * group.
         **/
        class Chgrp2Response extends OK {

            /**
             * The model objects that were moved.
             **/
            omero::api::StringLongListMap includedObjects;

            /**
             * The model objects that were deleted.
             **/
            omero::api::StringLongListMap deletedObjects;
        };

        /**
         * Change the permissions on model objects.
         * The user must be an administrator, the owner of the objects,
         * or an owner of the objects' group.
         * The only permitted target object type is ExperimenterGroup.
         **/
        class Chmod2 extends GraphModify2 {

            /**
             * The permissions to set on the model objects.
             **/
            string permissions;
        };

        /**
         * Result of changing the permissions on model objects.
         **/
        class Chmod2Response extends OK {

            /**
             * The model objects with changed permissions.
             **/
            omero::api::StringLongListMap includedObjects;

            /**
             * The model objects that were deleted.
             **/
            omero::api::StringLongListMap deletedObjects;
        };

        /**
         * Change the ownership of model objects.
         * The user must be an administrator, or they
         * must be an owner of the objects' group, with
         * the target user a member of the objects' group.
         **/
        class Chown2 extends GraphModify2 {

            /**
             * The ID of the experimenter to which to give the model
             * objects.
             **/
            long userId;

            /**
             * The users who should have all their data targeted.
             **/
            omero::api::LongList targetUsers;
        };

        /**
         * Result of changing the ownership of model objects.
         **/
        class Chown2Response extends OK {

            /**
             * The model objects that were given.
             **/
            omero::api::StringLongListMap includedObjects;

            /**
             * The model objects that were deleted.
             **/
            omero::api::StringLongListMap deletedObjects;
        };

        /**
         * Delete model objects.
         **/
        class Delete2 extends GraphModify2 {
        };

        /**
         * Result of deleting model objects.
         **/
        class Delete2Response extends OK {

            /**
             * The model objects that were deleted.
             **/
            omero::api::StringLongListMap deletedObjects;
        };

        /**
         * Perform a request skipping the top-most model objects in the
         * graph. This permits operating upon the (possibly indirect)
         * children of given objects. The arguments of this SkipHead
         * request override those of the given request only until the
         * targeted children are reached, except that if this SkipHead
         * request's dryRun is set to true then the dryRun override
         * persists throughout the operation. The response from SkipHead
         * is as from the given request.
         **/
        class SkipHead extends GraphModify2 {

            /**
             * Classes of model objects from which to actually start the
             * operation. These are children, directly or indirectly, of
             * the target objects. These children become the true target
             * objects of the underlying request.
             **/
            omero::api::StringSet startFrom;

            /**
             * The operation to perform on the targeted model objects.
             * The given request's targetObjects property is ignored: it
             * is the SkipHead request that specifies the parent objects.
             * Only specific request types are supported
             * (those implementing WrappableRequest).
             **/
            GraphModify2 request;
        };

        /**
         * Request to determine the disk usage of the given objects
         * and their contents. File-system paths used by multiple objects
         * are de-duplicated in the total count. Specifying a class is
         * equivalent to specifying all its instances as objects.
         *
         * Permissible classes include:
         *   ExperimenterGroup, Experimenter, Project, Dataset,
         *   Folder, Screen, Plate, Well, WellSample,
         *   Image, Pixels, Annotation, Job, Fileset, OriginalFile.
         **/
        class DiskUsage2 extends GraphQuery {
            omero::api::StringSet targetClasses;
        };

        /**
         * Disk usage report: bytes used and non-empty file counts on the
         * repository file-system for specific objects. The counts from the
         * maps may sum to more than the total if different types of object
         * refer to the same file. Common referers include:
         *   Annotation for file annotations
         *   FilesetEntry for OMERO 5 image files (OMERO.fs)
         *   Job for import logs
         *   Pixels for pyramids and OMERO 4 images and archived files
         *   Thumbnail for the image thumbnails
         * The above map values are broken down by owner-group keys.
         **/
        class DiskUsage2Response extends Response {
            omero::api::LongPairToStringIntMap fileCountByReferer;
            omero::api::LongPairToStringLongMap bytesUsedByReferer;
            omero::api::LongPairIntMap totalFileCount;
            omero::api::LongPairLongMap totalBytesUsed;
        };

        /**
         * Duplicate model objects with some selection of their subgraph.
         * All target model objects must be in the current group context.
         * The extra three data members allow adjustment of the related
         * subgraph. The same type must not be listed in more than one of
         * those data members. Use of a more specific sub-type in a data
         * member always overrides the more general type in another.
         **/
        class Duplicate extends GraphModify2 {

            /**
             * The types of the model objects to actually duplicate.
             **/
            omero::api::StringSet typesToDuplicate;

            /**
             * The types of the model objects that should not be duplicated
             * but that may participate in references involving duplicates.
             **/
            omero::api::StringSet typesToReference;

            /**
             * The types of the model objects that should not be duplicated
             * and that may not participate in references involving duplicates.
             **/
            omero::api::StringSet typesToIgnore;
        };

        /**
         * Result of duplicating model objects.
         **/
        class DuplicateResponse extends OK {

            /**
             * The duplicate model objects created by the request.
             * Note: If dryRun is set to true then this instead lists the model
             * objects that would have been duplicated.
             **/
            omero::api::StringLongListMap duplicates;
        };

        /**
         * Identify the parents or containers of model objects.
         * Traverses the model graph to identify indirect relationships.
         **/
        class FindParents extends GraphQuery {

            /**
             * The types of parents being sought.
             **/
            omero::api::StringSet typesOfParents;

            /**
             * Classes of model objects to exclude from the recursive
             * search. Search does not include or pass such objects.
             * For efficiency the server automatically excludes various
             * classes depending on the other arguments of the request.
             **/
            omero::api::StringSet stopBefore;
        };

        /**
         * Result of identifying the parents or containers of model objects.
         **/
        class FoundParents extends OK {

            /**
             * The parents that were identified.
             **/
            omero::api::StringLongListMap parents;
        };

        /**
         * Identify the children or contents of model objects.
         * Traverses the model graph to identify indirect relationships.
         **/
        class FindChildren extends GraphQuery {

            /**
             * The types of children being sought.
             **/
            omero::api::StringSet typesOfChildren;

            /**
             * Classes of model objects to exclude from the recursive
             * search. Search does not include or pass such objects.
             * For efficiency the server automatically excludes various
             * classes depending on the other arguments of the request.
             **/
            omero::api::StringSet stopBefore;
        };

        /**
         * Result of identifying the children or contents of model objects.
         **/
        class FoundChildren extends OK {

            /**
             * The children that were identified.
             **/
            omero::api::StringLongListMap children;
        };

        /**
         * Graph requests typically allow only specific model object classes
         * to be targeted. This request lists the legal targets for a given
         * request. The request's fields are ignored, only its class matters.
         **/
        class LegalGraphTargets extends Request {

            /**
             * A request of the type being queried.
             **/
            GraphModify2 request;
        };

        /**
         * A list of the legal targets for a graph request.
         **/
        class LegalGraphTargetsResponse extends OK {

            /**
             * The legal targets for the given request's type.
             **/
            omero::api::StringSet targets;
        };

        /**
         * Returned when specifically a ome.services.graphs.GraphException
         * is thrown. The contents of that internal exception are passed in
         * this instance.
         **/
        class GraphException extends ERR {

            /**
             * The message of the GraphException.
             **/
             string message;
        };
    };
};

#endif
