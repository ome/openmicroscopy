/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_API_IUPDATE_ICE
#define OMERO_API_IUPDATE_ICE

#include <omero/cmd/API.ice>
#include <omero/ServicesF.ice>
#include <omero/Collections.ice>

module omero {

    module api {

        /**
         * See <a href="http://downloads.openmicroscopy.org/latest/omero5.1/api/ome/api/IUpdate.html">IUpdate.html</a>
         **/
        ["ami", "amd"] interface IUpdate extends ServiceInterface
            {
                void saveObject(omero::model::IObject obj) throws ServerError;
                void saveCollection(IObjectList objs) throws ServerError;
                omero::model::IObject saveAndReturnObject(omero::model::IObject obj) throws ServerError;
                void saveArray(IObjectList graph) throws ServerError;
                IObjectList saveAndReturnArray(IObjectList graph) throws ServerError;
                omero::sys::LongList saveAndReturnIds(IObjectList graph) throws ServerError;
                ["deprecated:use omero::cmd::Delete2 instead"]
                void deleteObject(omero::model::IObject row) throws ServerError;
                idempotent void indexObject(omero::model::IObject row) throws ServerError;
            };

        class Save extends omero::cmd::Request {
            omero::model::IObject obj;
        };

        class SaveRsp extends omero::cmd::Response {
            omero::model::IObject obj;
        };

    };
};

#endif
