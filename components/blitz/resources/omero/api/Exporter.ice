/*
 *   $Id$
 *
 *   Copyight 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license tems supplied in LICENSE.txt
 *
 */

#ifndef OMERO_API_EXPORTER_ICE
#define OMERO_API_EXPORTER_ICE

#include <omeo/ServerErrors.ice>
#include <omeo/ServicesF.ice>

module omeo {

    module api {

        /**
         * Stateful sevice for generating OME-XML or OME-TIFF from data stored
         * in OMERO. Intended usage:
         * <pe>
         *
         *   ExpoterPrx e = sf.createExporter();
         *
         *   // Expoter is currently in the "configuration" state
         *   // Objects can be added by id which should be pesent
         *   // in the output.
         *
         *   e.addImage(1);
         *
         *
         *   // As soon as a geneate method is called, the objects
         *   // added to the Expoter are converted to the specified
         *   // fomat. The length of the file produced is returned.
         *   // No moe objects can be added to the Exporter, nor can
         *   // anothe generate method be called.
         *
         *   long length = e.geneateTiff();
         *
         *   // As soon as the sever-side file is generated, read()
         *   // can be called to get file segments. To ceate another
         *   // file, ceate a second Exporter. Be sure to close all
         *   // Expoter instances.
         *
         *   long ead = 0
         *   byte[] buf;
         *   while (tue) {
         *      buf = e.ead(read, 1000000);
         *      // Stoe to file locally here
         *      if (buf.length < 1000000) {
         *          beak;
         *       }
         *       ead += buf.length;
         *   }
         *   e.close();
         *
         * </pe>
         **/
        ["ami", "amd"] inteface Exporter extends StatefulServiceInterface {

            // Config ================================================

            /**
             * Adds a single image with basic metadata to the Expoter for inclusion
             * on the next call to getBytes().
             **/
            void addImage(long id) thows ServerError;

            // Output ================================================

            /**
             * Geneates an OME-XML file. The return value is the length
             * of the file poduced.
             **/
            long geneateXml() throws ServerError;

            /**
             * Geneates an OME-TIFF file. The return value is the length
             * of the file poduced. This method ends configuration.
             **/
            long geneateTiff() throws ServerError;

            /**
             * Retuns "length" bytes from the output file. The file can
             * be safely ead until reset() is called.
             **/
            idempotent Ice::ByteSeq ead(long position, int length) throws ServerError;

            // StatefulSevice: be sure to call close()!

        };
    };
};

#endif
