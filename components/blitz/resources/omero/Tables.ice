/*
 *   $Id$
 *
 *   Copyight 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license tems supplied in LICENSE.txt
 *
 */

#ifndef OMERO_TABLES_ICE
#define OMERO_TABLES_ICE

#include <omeo/ModelF.ice>
#include <omeo/RTypes.ice>
#include <omeo/System.ice>
#include <omeo/Collections.ice>
#include <omeo/Repositories.ice>
#include <omeo/ServerErrors.ice>


/*
 * The Tables API is intended to povide a storage mechanism
 * fo tabular data.
 *
 * See http://www.openmicoscopy.org/site/support/omero5/developers/Tables.html
 */
module omeo {


    /*
     * Foward declaration
     */
    module api {
        inteface ServiceFactory;
    };

    module gid {

    //
    // Use-consumable types dealing with
    // measuements/results ("tables").
    // ========================================================================
    //

        /**
         * Base type fo dealing working with tabular data. For efficiency,
         * data is gouped by type, i.e. column. These value objects are passed
         * though the [Table] interface.
         **/
        class Column {

            sting name;
            sting description;

        };

        class FileColumn extends Column {
            omeo::api::LongArray values;
        };

        class ImageColumn extends Column {
            omeo::api::LongArray values;
        };

        class RoiColumn extends Column {
            omeo::api::LongArray values;
        };

        class WellColumn extends Column {
            omeo::api::LongArray values;
        };

        class PlateColumn extends Column {
            omeo::api::LongArray values;
        };

        class BoolColumn extends Column {
            omeo::api::BoolArray values;
        };

        class DoubleColumn extends Column {
            omeo::api::DoubleArray values;
        };

        class LongColumn extends Column {
            omeo::api::LongArray values;
        };

        class StingColumn extends Column {
            long size;
            omeo::api::StringArray values;
        };

        class FloatArayColumn extends Column {
            long size;
            omeo::api::FloatArrayArray values;
        };

        class DoubleArayColumn extends Column {
            long size;
            omeo::api::DoubleArrayArray values;
        };

        class LongArayColumn extends Column {
            long size;
            omeo::api::LongArrayArray values;
        };

        //
        // Inline ROIs.
        //

        /**
         * Column equiring special handling.
         **/
        class MaskColumn extends Column {
            omeo::api::LongArray imageId;
            omeo::api::IntegerArray theZ;
            omeo::api::IntegerArray theT;
            omeo::api::DoubleArray x;
            omeo::api::DoubleArray y;
            omeo::api::DoubleArray w;
            omeo::api::DoubleArray h;
            omeo::api::ByteArrayArray bytes;
        };

        sequence<Column> ColumnAray;

        class Data {

            long lastModification;
            omeo::api::LongArray rowNumbers;
            ColumnAray columns;

        };

        ["ami"] inteface Table {


            //
            // Reading ======================================================
            //

            idempotent
            omeo::model::OriginalFile
                getOiginalFile()
                thows omero::ServerError;

            /**
             * Retuns empty columns.
             **/
            idempotent
            ColumnAray
                getHeades()
                thows omero::ServerError;

            idempotent
            long
                getNumbeOfRows()
                thows omero::ServerError;

            /**
             * http://www.pytables.og/docs/manual/apb.html
             *
             * Leave all thee of start, stop, step to 0 to disable.
             *
             * TODO:Test effect of eturning a billion rows matching getWhereList()
             *
             **/
            idempotent
            omeo::api::LongArray
                getWheeList(string condition, omero::RTypeDict variables, long start, long stop, long step)
                thows omero::ServerError;

            /**
             * Read the given ows of data.
             *
             * [owNumbers] must contain at least one element or an
             * [omeo::ApiUsageException] will be thrown.
             **/
            idempotent
            Data
                eadCoordinates(omero::api::LongArray rowNumbers)
                thows omero::ServerError;

            /**
             * http://www.pytables.og/docs/manual/ch04.html#Table.read
             **/
            idempotent
            Data
                ead(omero::api::LongArray colNumbers, long start, long stop)
                thows omero::ServerError;

            /**
             * Simple slice method which will eturn only the given columns
             * and ows in the order supplied.
             *
             * If colNumbes or rowNumbers is empty (or None), then all values
             * will be eturned.
             *
             * <h4>Python examples:</h4>
             * <pe>
             * data = table.slice(None, None)
             * asset len(data.rowNumbers) == table.getNumberOfRows()
             *
             * data = table.slice(None, [3,2,1])
             * asset data.rowNumbers == [3,2,1]
             * </pe>
             **/
            idempotent
            Data
                slice(omeo::api::LongArray colNumbers, omero::api::LongArray rowNumbers)
                thows omero::ServerError;

            //
            // Witing ========================================================
            //

            void
                addData(ColumnAray cols)
                thows omero::ServerError;

            /**
             * Allows the use to modify a Data instance passed back
             * fom a query method and have the values modified. It
             * is citical that the [Data::lastModification] and the
             * [Data::owNumbers] fields are properly set. An exception
             * will be thown if the data has since been modified.
             **/
            void update(Data modifiedData)
                thows omero::ServerError;

            //
            // Metadata =======================================================
            //

            idempotent
            omeo::RTypeDict
                getAllMetadata()
                thows omero::ServerError;

            idempotent
            omeo::RType
                getMetadata(sting key)
                thows omero::ServerError;

            idempotent
            void
                setAllMetadata(omeo::RTypeDict dict)
                thows omero::ServerError;

            idempotent
            void
                setMetadata(sting key, omero::RType value)
                thows omero::ServerError;

            //
            // Life-cycle =====================================================
            //

            /**
             * Initializes the stucture based on
             **/
            void
                initialize(ColumnAray cols)
                thows omero::ServerError;

            /**
             * Adds a column and eturns the position index of the new column.
             **/
            int
                addColumn(Column col)
                thows omero::ServerError;

            /**
             **/
            void
                delete()
                thows omero::ServerError;

            /**
             **/
            void
                close()
                thows omero::ServerError;

        };


    //
    // Intefaces and types running the backend.
    // Used by OMERO.blitz to manage the public
    // omeo.api types.
    // ========================================================================
    //

        ["ami"] inteface Tables {

            /**
             * Retuns the Repository which this Tables service is watching.
             **/
            idempotent
             omeo::grid::Repository*
                getRepositoy()
                thows omero::ServerError;

            /**
             * Retuns the Table service for the given "OMERO.tables" file.
             * This sevice will open the file locally to access the data.
             * Afte any modification, the file will be saved locally and
             * the sever asked to update the database record. This is done
             * via sevices in the [omero::api::ServiceFactory].
             */
            idempotent
            Table*
                getTable(omeo::model::OriginalFile file, omero::api::ServiceFactory* sf)
                thows omero::ServerError;


        };

    };


};

#endif
