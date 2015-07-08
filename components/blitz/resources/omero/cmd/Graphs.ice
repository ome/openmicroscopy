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
         *
         **/
        ["deprecated:use omero::cmd::GraphModify2 instead"]
        class GraphModify extends Request {
            string type;
            long id;
            StringMap options;
        };

        /**
         * Returned when specifically a ome.services.graphs.GraphConstraintException
         * is thrown. The contents of that internal exception are passed in
         * this instance.
         **/
        ["deprecated:use omero::cmd::ERR instead"]
        class GraphConstraintERR extends ERR {

            /**
             * A container mapping from class names to collections of
             * longs (ids) for each object which prevented the current
             * operation from succeeding.
             **/
             omero::api::IdListMap constraints;

        };

        ["deprecated:omero::cmd::GraphModify is deprecated",
         "java:type:java.util.ArrayList<omero.cmd.GraphModify>:java.util.List<omero.cmd.GraphModify>"]
        sequence<GraphModify> GraphModifyList;

        /**
         *
         **/
        ["deprecated:GraphSpecs in general are deprecated"]
        class GraphSpecList extends Request {};

        ["deprecated:GraphSpecs in general are deprecated"]
        class GraphSpecListRsp extends Response {
            GraphModifyList list;
        };

        ["deprecated:use omero::cmd::Chgrp2 instead"]
        class Chgrp extends GraphModify {
            long grp;
        };

        ["deprecated:use omero::cmd::Chgrp2Response instead"]
        class ChgrpRsp extends Response {
        };

        /**
         * Modifies the permissions settings for the given object.
         * Most permission modifications will be quite fast and will
         * specify this as returning a small number of steps in the
         * status object. When lowering a READ setting, however, all
         * existing data will need to be checked and there will be a
         * minimum of one step per table in the database.
         *
         * At the moment, the only supported type is "/ExperimenterGroup".
         *
         **/
        ["deprecated:use omero::cmd::Chmod2 instead"]
        class Chmod extends GraphModify {

            /**
             * String representation of the permissions
             * which should be set on the object.
             **/
            string permissions;
        };

        ["deprecated:use omero::cmd::Chmod2Response instead"]
        class ChmodRsp extends Response {
        };

        ["deprecated:use omero::cmd::Chown2 instead"]
        class Chown extends GraphModify {
            long user;
        };

        ["deprecated:use omero::cmd::Chown2Response instead"]
        class ChownRsp extends Response {
        };

        /**
         * Delete requests will return a [omero::cmd::DeleteRsp]
         * unless an error has occurred in which case a standard
         * [omero::cmd::ERR] may be returned.
         **/
        ["deprecated:use omero::cmd::Delete2 instead"]
        class Delete extends GraphModify {
        };

        /**
         * Mirrors and replaces DeleteReport. There is no "error" field
         * because if there was an error than an ERR object will be
         * returned.
         **/
        ["deprecated:use omero::cmd::Delete2Response instead"]
        class DeleteRsp extends OK {

            /**
             * Extra feedback mechanism. Typically will only be non-empty
             * if the error is empty. This implies that some situation was
             * encountered that the user may need to be informed of (e.g.
             * some annotation wasn't deleted), but which was non-critical.
             **/
            string warning;

            /**
             * Map from type name ("Thumbnail", "Pixels", "OriginalFile") to
             * a list of ids for any binary files which did not get deleted.
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

        };

        /**
         * Options that modify GraphModify2 request execution.
         * By default, a user's related "orphaned" objects are typically
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
         * Base class for new requests for operating upon the model object
         * graph.
         **/
        class GraphModify2 extends Request {

            /**
             * The model objects upon which to operate.
             * Related model objects may also be targeted.
             **/
            omero::api::StringLongListMap targetObjects;

            /**
             * If the request should operate on specific kinds of children.
             * Only the first applicable option takes effect.
             **/
            graphs::ChildOptions childOptions;

            /**
             * If this request should skip the phases in which model
             * objects are operated upon.
             * The response is still as if the operation actually occurred,
             * indicating what would have been done to which objects, except
             * for that various permissions checks are omitted.
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
         * The user must be an administrator, or
         * they must be the owner of the objects
         * or an owner of the objects' group, with
         * the target user a member of the objects' group.
         **/
        class Chown2 extends GraphModify2 {

            /**
             * The ID of the experimenter to which to give the model
             * objects.
             **/
            long userId;
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
