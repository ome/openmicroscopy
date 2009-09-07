/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_API_TABLES_ICE
#define OMERO_API_TABLES_ICE

#include <omero/model/OriginalFile.ice>

module omero {

    /*
     * User-consumable types dealing with
     * measurements/results ("tables").
     */
    module api {

        interface Table {

            //StringSet headers();
            //TypeSet ...

        };

    };

    /*
     * Interfaces and types running the backend.
     * Used by OMERO.blitz to manage the public
     * omero.api types.
     */
    module grid {

        interface Tables {
            
            omero::api::Table* newTable();
            omero::api::Table* getTable(omero::model::OriginalFile file);

        };

    };


};

#endif
