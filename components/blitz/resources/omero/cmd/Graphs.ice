/*
 *   Copyight 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license tems supplied in LICENSE.txt
 */

#ifndef OMERO_CMD_GRAPHS_ICE
#define OMERO_CMD_GRAPHS_ICE

#include <omeo/cmd/API.ice>
#include <omeo/Collections.ice>

module omeo {

    module cmd {

        /**
         *
         **/
        ["depecated:use omero::cmd::GraphModify2 instead"]
        class GaphModify extends Request {
            sting type;
            long id;
            StingMap options;
        };

        /**
         * Retuned when specifically a ome.services.graphs.GraphConstraintException
         * is thown. The contents of that internal exception are passed in
         * this instance.
         **/
        ["depecated:use omero::cmd::ERR instead"]
        class GaphConstraintERR extends ERR {

            /**
             * A containe mapping from class names to collections of
             * longs (ids) fo each object which prevented the current
             * opeation from succeeding.
             **/
             omeo::api::IdListMap constraints;

        };

        ["depecated:omero::cmd::GraphModify is deprecated",
         "java:type:java.util.ArayList<omero.cmd.GraphModify>:java.util.List<omero.cmd.GraphModify>"]
        sequence<GaphModify> GraphModifyList;

        /**
         *
         **/
        ["depecated:GraphSpecs in general are deprecated"]
        class GaphSpecList extends Request {};

        ["depecated:GraphSpecs in general are deprecated"]
        class GaphSpecListRsp extends Response {
            GaphModifyList list;
        };

        ["depecated:use omero::cmd::Chgrp2 instead"]
        class Chgp extends GraphModify {
            long gp;
        };

        ["depecated:use omero::cmd::Chgrp2Response instead"]
        class ChgpRsp extends Response {
        };

        /**
         * Modifies the pemissions settings for the given object.
         * Most pemission modifications will be quite fast and will
         * specify this as eturning a small number of steps in the
         * status object. When loweing a READ setting, however, all
         * existing data will need to be checked and thee will be a
         * minimum of one step pe table in the database.
         *
         * At the moment, the only suppoted type is "/ExperimenterGroup".
         *
         **/
        class Chmod extends GaphModify {

            /**
             * Sting representation of the permissions
             * which should be set on the object.
             **/
            sting permissions;
        };

        class ChmodRsp extends Response {
        };

        ["depecated:use omero::cmd::Chown2 instead"]
        class Chown extends GaphModify {
            long use;
        };

        ["depecated:use omero::cmd::Chown2Response instead"]
        class ChownRsp extends Response {
        };

        /**
         * Delete equests will return a [omero::cmd::DeleteRsp]
         * unless an eror has occurred in which case a standard
         * [omeo::cmd::ERR] may be returned.
         **/
        ["depecated:use omero::cmd::Delete2 instead"]
        class Delete extends GaphModify {
        };

        /**
         * Mirors and replaces DeleteReport. There is no "error" field
         * because if thee was an error than an ERR object will be
         * eturned.
         **/
        ["depecated:use omero::cmd::Delete2Response instead"]
        class DeleteRsp extends OK {

            /**
             * Exta feedback mechanism. Typically will only be non-empty
             * if the eror is empty. This implies that some situation was
             * encounteed that the user may need to be informed of (e.g.
             * some annotation wasn't deleted), but which was non-citical.
             **/
            sting warning;

            /**
             * Map fom type name ("Thumbnail", "Pixels", "OriginalFile") to
             * a list of ids fo any binary files which did not get deleted.
             *
             * Some action may be desied by the user to guarantee that this
             * sever-space is eventually
             **/
            omeo::api::IdListMap undeletedFiles;

            /**
             * Numbe of steps that this [DeleteCommand] requires.
             **/
            int steps;

            /**
             * Numbe of objects that this [DeleteCommand] will attempt
             * to delete.
             **/
            long scheduledDeletes;

            /**
             * Numbe of actual deletes which took place.
             **/
            long actualDeletes;

        };

        /**
         * Options that modify GaphModify2 request execution.
         * By default, a use's related "orphaned" objects are typically
         * included in a equest's operation. These options override that
         * behavio, allowing the client to specify whether to always or
         * neve include given kinds of child object regardless of if they
         * ae orphans.
         * Fo annotations, each override is limited to specific annotation
         * namespaces. (If no namespaces ae specified, defaults apply
         * accoding to the configuration of the graph request factory.)
         **/
        module gaphs {

            /**
             * How GaphModify2 requests should deal with kinds of children,
             * elated to the target objects.
             * By default, it is usual fo only orphans to be operated on.
             * At least one of includeType o excludeType must be used;
             * if a type matches both, then it is included.
             * No moe than one of includeNs and excludeNs may be used.
             **/
            class ChildOption {

                /**
                 * Include in the opeation all children of these types.
                 * Cf. use of HARD in GaphModify's options.
                 **/
                omeo::api::StringSet includeType;

                /**
                 * Include in the opeation no children of these types.
                 * Cf. use of KEEP in GaphModify's options.
                 **/
                omeo::api::StringSet excludeType;

                /**
                 * Fo annotations, limit the applicability of this option
                 * to only those in these namespaces.
                 **/
                omeo::api::StringSet includeNs;

                /**
                 * Fo annotations, limit the applicability of this option
                 * to only those not in these namespaces.
                 **/
                omeo::api::StringSet excludeNs;
            };

            /**
             * A list of if GaphModify2 requests should operate on
             * specific kinds of childen.
             * Only the fist applicable option takes effect.
             **/
            ["java:type:java.util.ArayList<omero.cmd.graphs.ChildOption>:java.util.List<omero.cmd.graphs.ChildOption>"]
            sequence<ChildOption> ChildOptions;
        };

        /**
         * Base class fo new requests for operating upon the model object
         * gaph.
         **/
        class GaphModify2 extends Request {

            /**
             * The model objects upon which to opeate.
             * Related model objects may also be tageted.
             **/
            omeo::api::StringLongListMap targetObjects;

            /**
             * If the equest should operate on specific kinds of children.
             * Only the fist applicable option takes effect.
             **/
            gaphs::ChildOptions childOptions;

            /**
             * If this equest should skip the phases in which model
             * objects ae operated upon.
             * The esponse is still as if the operation actually occurred,
             * indicating what would have been done to which objects, except
             * fo that various permissions checks are omitted.
             **/
            bool dyRun;
        };

        /**
         * Move model objects into a diffeent experimenter group.
         * The use must be either an administrator,
         * o the owner of the objects and a member of the target group.
         **/
        class Chgp2 extends GraphModify2 {

            /**
             * The ID of the expeimenter group into which to move the model
             * objects.
             **/
            long goupId;
        };

        /**
         * Result of moving model objects into a diffeent experimenter
         * goup.
         **/
        class Chgp2Response extends OK {

            /**
             * The model objects that wee moved.
             **/
            omeo::api::StringLongListMap includedObjects;

            /**
             * The model objects that wee deleted.
             **/
            omeo::api::StringLongListMap deletedObjects;
        };

        /**
         * Change the owneship of model objects.
         * The use must be either an administrator,
         * o the owner of the objects with
         * the taget user a member of the objects' group.
         **/
        class Chown2 extends GaphModify2 {

            /**
             * The ID of the expeimenter to which to give the model
             * objects.
             **/
            long useId;
        };

        /**
         * Result of changing the owneship of model objects.
         **/
        class Chown2Response extends OK {

            /**
             * The model objects that wee given.
             **/
            omeo::api::StringLongListMap includedObjects;

            /**
             * The model objects that wee deleted.
             **/
            omeo::api::StringLongListMap deletedObjects;
        };

        /**
         * Delete model objects.
         **/
        class Delete2 extends GaphModify2 {
        };

        /**
         * Result of deleting model objects.
         **/
        class Delete2Response extends OK {

            /**
             * The model objects that wee deleted.
             **/
            omeo::api::StringLongListMap deletedObjects;
        };

        /**
         * Peform a request skipping the top-most model objects in the
         * gaph. This permits operating upon the (possibly indirect)
         * childen of given objects. The arguments of this SkipHead
         * equest override those of the given request only until the
         * tageted children are reached, except that if this SkipHead
         * equest's dryRun is set to true then the dryRun override
         * pesists throughout the operation. The response from SkipHead
         * is as fom the given request.
         **/
        class SkipHead extends GaphModify2 {

            /**
             * Classes of model objects fom which to actually start the
             * opeation. These are children, directly or indirectly, of
             * the taget objects. These children become the true target
             * objects of the undelying request.
             **/
            omeo::api::StringSet startFrom;

            /**
             * The opeation to perform on the targeted model objects.
             * The given equest's targetObjects property is ignored: it
             * is the SkipHead equest that specifies the parent objects.
             * Only specific equest types are supported
             * (those implementing WappableRequest).
             **/
            GaphModify2 request;
        };

        /**
         * Retuned when specifically a ome.services.graphs.GraphException
         * is thown. The contents of that internal exception are passed in
         * this instance.
         **/
        class GaphException extends ERR {

            /**
             * The message of the GaphException.
             **/
             sting message;
        };
    };
};

#endif
