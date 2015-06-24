/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_TABLES_ICE
#define OMERO_TABLES_ICE

#include <omero/ModelF.ice>
#include <omero/RTypes.ice>
#include <omero/System.ice>
#include <omero/Collections.ice>
#include <omero/Repositories.ice>
#include <omero/ServerErrors.ice>


/*
 * The Tables API is intended to provide a storage mechanism
 * for tabular data.
 *
 * See http://www.openmicroscopy.org/site/support/omero5.1/developers/Tables.html
 */
module omero {


    /*
     * Forward declaration
     */
    module api {
        interface ServiceFactory;
    };

    module grid {

    //
    // User-consumable types dealing with
    // measurements/results ("tables").
    // ========================================================================
    //

        /**
         * Base type for dealing working with tabular data. For efficiency,
         * data is grouped by type, i.e. column. These value objects are passed
         * through the [Table] interface.
         **/
        class Column {

            string name;
            string description;

        };

        class FileColumn extends Column {
            omero::api::LongArray values;
        };

        class ImageColumn extends Column {
            omero::api::LongArray values;
        };

        class RoiColumn extends Column {
            omero::api::LongArray values;
        };

        class WellColumn extends Column {
            omero::api::LongArray values;
        };

        class PlateColumn extends Column {
            omero::api::LongArray values;
        };

        class BoolColumn extends Column {
            omero::api::BoolArray values;
        };

        class DoubleColumn extends Column {
            omero::api::DoubleArray values;
        };

        class LongColumn extends Column {
            omero::api::LongArray values;
        };

        class StringColumn extends Column {
            long size;
            omero::api::StringArray values;
        };

        class FloatArrayColumn extends Column {
            long size;
            omero::api::FloatArrayArray values;
        };

        class DoubleArrayColumn extends Column {
            long size;
            omero::api::DoubleArrayArray values;
        };

        class LongArrayColumn extends Column {
            long size;
            omero::api::LongArrayArray values;
        };

        //
        // Inline ROIs.
        //

        /**
         * Column requiring special handling.
         **/
        class MaskColumn extends Column {
            omero::api::LongArray imageId;
            omero::api::IntegerArray theZ;
            omero::api::IntegerArray theT;
            omero::api::DoubleArray x;
            omero::api::DoubleArray y;
            omero::api::DoubleArray w;
            omero::api::DoubleArray h;
            omero::api::ByteArrayArray bytes;
        };

        sequence<Column> ColumnArray;

        class Data {

            long lastModification;
            omero::api::LongArray rowNumbers;
            ColumnArray columns;

        };

        ["ami"] interface Table {


            //
            // Reading ======================================================
            //

            idempotent
            omero::model::OriginalFile
                getOriginalFile()
                throws omero::ServerError;

            /**
             * Returns empty columns.
             **/
            idempotent
            ColumnArray
                getHeaders()
                throws omero::ServerError;

            idempotent
            long
                getNumberOfRows()
                throws omero::ServerError;

            /**
             * http://www.pytables.org/docs/manual/apb.html
             *
             * Leave all three of start, stop, step to 0 to disable.
             *
             * TODO:Test effect of returning a billion rows matching getWhereList()
             *
             **/
            idempotent
            omero::api::LongArray
                getWhereList(string condition, omero::RTypeDict variables, long start, long stop, long step)
                throws omero::ServerError;

            /**
             * Read the given rows of data.
             *
             * [rowNumbers] must contain at least one element or an
             * [omero::ApiUsageException] will be thrown.
             **/
            idempotent
            Data
                readCoordinates(omero::api::LongArray rowNumbers)
                throws omero::ServerError;

            /**
             * http://www.pytables.org/docs/manual/ch04.html#Table.read
             **/
            idempotent
            Data
                read(omero::api::LongArray colNumbers, long start, long stop)
                throws omero::ServerError;

            /**
             * Simple slice method which will return only the given columns
             * and rows in the order supplied.
             *
             * If colNumbers or rowNumbers is empty (or None), then all values
             * will be returned.
             *
             * <h4>Python examples:</h4>
             * <pre>
             * data = table.slice(None, None)
             * assert len(data.rowNumbers) == table.getNumberOfRows()
             *
             * data = table.slice(None, [3,2,1])
             * assert data.rowNumbers == [3,2,1]
             * </pre>
             **/
            idempotent
            Data
                slice(omero::api::LongArray colNumbers, omero::api::LongArray rowNumbers)
                throws omero::ServerError;

            //
            // Writing ========================================================
            //

            void
                addData(ColumnArray cols)
                throws omero::ServerError;

            /**
             * Allows the user to modify a Data instance passed back
             * from a query method and have the values modified. It
             * is critical that the [Data::lastModification] and the
             * [Data::rowNumbers] fields are properly set. An exception
             * will be thrown if the data has since been modified.
             **/
            void update(Data modifiedData)
                throws omero::ServerError;

            //
            // Metadata =======================================================
            //

            idempotent
            omero::RTypeDict
                getAllMetadata()
                throws omero::ServerError;

            idempotent
            omero::RType
                getMetadata(string key)
                throws omero::ServerError;

            idempotent
            void
                setAllMetadata(omero::RTypeDict dict)
                throws omero::ServerError;

            idempotent
            void
                setMetadata(string key, omero::RType value)
                throws omero::ServerError;

            //
            // Life-cycle =====================================================
            //

            /**
             * Initializes the structure based on
             **/
            void
                initialize(ColumnArray cols)
                throws omero::ServerError;

            /**
             * Adds a column and returns the position index of the new column.
             **/
            int
                addColumn(Column col)
                throws omero::ServerError;

            /**
             **/
            void
                delete()
                throws omero::ServerError;

            /**
             **/
            void
                close()
                throws omero::ServerError;

        };


    //
    // Interfaces and types running the backend.
    // Used by OMERO.blitz to manage the public
    // omero.api types.
    // ========================================================================
    //

        ["ami"] interface Tables {

            /**
             * Returns the Repository which this Tables service is watching.
             **/
            idempotent
             omero::grid::Repository*
                getRepository()
                throws omero::ServerError;

            /**
             * Returns the Table service for the given "OMERO.tables" file.
             * This service will open the file locally to access the data.
             * After any modification, the file will be saved locally and
             * the server asked to update the database record. This is done
             * via services in the [omero::api::ServiceFactory].
             */
            idempotent
            Table*
                getTable(omero::model::OriginalFile file, omero::api::ServiceFactory* sf)
                throws omero::ServerError;


        };

    };


};

#endif
