/*
 *   Copyright 2011-2014 Glencoe Software, Inc. All rights reserved.
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
        class GraphConstraintERR extends ERR {

            /**
             * A container mapping from class names to collections of
             * longs (ids) for each object which prevented the current
             * operation from succeeding.
             **/
             omero::api::IdListMap constraints;

        };


        ["java:type:java.util.ArrayList<omero.cmd.GraphModify>:java.util.List<omero.cmd.GraphModify>"]
        sequence<GraphModify> GraphModifyList;

        /**
         *
         **/
        class GraphSpecList extends Request {};

        class GraphSpecListRsp extends Response {
            GraphModifyList list;
        };

        class Chgrp extends GraphModify {
            long grp;
        };

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
        class Chmod extends GraphModify {

            /**
             * String representation of the permissions
             * which should be set on the object.
             **/
            string permissions;
        };

        class ChmodRsp extends Response {
        };

        class Chown extends GraphModify {
            long user;
        };

        class ChownRsp extends Response {
        };

        /**
         * Delete requests will return a [omero::cmd::DeleteRsp]
         * unless an error has occurred in which case a standard
         * [omero::cmd::ERR] may be returned.
         **/
        class Delete extends GraphModify {
        };

        /**
         * Mirrors and replaces DeleteReport. There is no "error" field
         * because if there was an error than an ERR object will be
         * returned.
         **/
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

        class Chgrp2 extends Request {
            long groupId;
            omero::api::IObjectList targetObjects;
        };

        class Chgrp2Response extends OK {
            omero::api::IObjectList includedObjects;
            omero::api::IObjectList deletedObjects;
        };

        class Delete2 extends Request {
            omero::api::IObjectList targetObjects;
        };

        class Delete2Response extends OK {
            omero::api::IObjectList deletedObjects;
        };
    };
};

#endif
