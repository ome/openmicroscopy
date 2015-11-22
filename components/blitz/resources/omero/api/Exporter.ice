/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_API_EXPORTER_ICE
#define OMERO_API_EXPORTER_ICE

#include <omero/ServerErrors.ice>
#include <omero/ServicesF.ice>

module omero {

    module api {

        /**
         * Stateful service for generating OME-XML or OME-TIFF from data stored
         * in OMERO. Intended usage:
         * <pre>
         *
         *   ExporterPrx e = sf.createExporter();
         *
         *   // Exporter is currently in the "configuration" state
         *   // Objects can be added by id which should be present
         *   // in the output.
         *
         *   e.addImage(1);
         *
         *
         *   // As soon as a generate method is called, the objects
         *   // added to the Exporter are converted to the specified
         *   // format. The length of the file produced is returned.
         *   // No more objects can be added to the Exporter, nor can
         *   // another generate method be called.
         *
         *   long length = e.generateTiff();
         *
         *   // As soon as the server-side file is generated, read()
         *   // can be called to get file segments. To create another
         *   // file, create a second Exporter. Be sure to close all
         *   // Exporter instances.
         *
         *   long read = 0
         *   byte\[] buf;
         *   while (true) {
         *      buf = e.read(read, 1000000);
         *      // Store to file locally here
         *      if (buf.length < 1000000) {
         *          break;
         *       }
         *       read += buf.length;
         *   }
         *   e.close();
         *
         * </pre>
         **/
        ["ami", "amd"] interface Exporter extends StatefulServiceInterface {

            // Config ================================================

            /**
             * Adds a single image with basic metadata to the Exporter for inclusion
             * on the next call to getBytes().
             **/
            void addImage(long id) throws ServerError;

            // Output ================================================

            /**
             * Generates an OME-XML file. The return value is the length
             * of the file produced.
             **/
            long generateXml() throws ServerError;

            /**
             * Generates an OME-TIFF file. The return value is the length
             * of the file produced. This method ends configuration.
             **/
            long generateTiff() throws ServerError;

            /**
             * Returns "length" bytes from the output file. The file can
             * be safely read until reset() is called.
             **/
            idempotent Ice::ByteSeq read(long position, int length) throws ServerError;

            // StatefulService: be sure to call close()!

        };
    };
};

#endif
