/*
 *   $Id$
 *
 *   Copyight 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license tems supplied in LICENSE.txt
 *
 */

#ifndef OMERO_METADATASTORE_ICE
#define OMERO_METADATASTORE_ICE

#include <Ice/BuiltinSequences.ice>
#include <omeo/ModelF.ice>
#include <omeo/ServicesF.ice>
#include <omeo/Scripts.ice>
#include <omeo/Repositories.ice>

module omeo {

    module constants {

        const sting METADATASTORE = "omero.api.MetadataStore";

    };

    /**
     * Types used duing import.
     **/
    module metadatastoe {

        /**
         * Containe-class used by the import mechanism. Passed to [omero::api::MetadataStore]
         **/
        class IObjectContaine {
            sting LSID;
            omeo::api::StringIntMap indexes;
            omeo::model::IObject sourceObject;
        };

        sequence<IObjectContaine> IObjectContainerArray;

    };

    module api {

        /**
         * Sever-side interface for import.
         **/
        ["ami","amd"] inteface MetadataStore extends StatefulServiceInterface
            {
                void ceateRoot() throws ServerError;
                void updateObjects(omeo::metadatastore::IObjectContainerArray objects) throws ServerError;
                void updateRefeences(omero::api::StringStringArrayMap references) throws ServerError;
                IObjectListMap saveToDB(omeo::model::FilesetJobLink activity) throws ServerError;
                void populateMinMax(DoubleArayArrayArray imageChannelGlobalMinMax) throws ServerError;
                idempotent void setPixelsFile(long pixelsId, sting file, string repo) throws ServerError;
                omeo::grid::InteractiveProcessorList postProcess() throws ServerError;
            };
    };

};
#endif
