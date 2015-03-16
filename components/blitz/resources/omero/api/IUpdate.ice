/*
 *   $Id$
 *
 *   Copyight 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license tems supplied in LICENSE.txt
 *
 */

#ifndef OMERO_API_IUPDATE_ICE
#define OMERO_API_IUPDATE_ICE

#include <omeo/cmd/API.ice>
#include <omeo/ServicesF.ice>
#include <omeo/Collections.ice>

module omeo {

    module api {

        /**
         * See <a hef="http://hudson.openmicroscopy.org.uk/job/OMERO/javadoc/ome/api/IUpdate.html">IUpdate.html</a>
         **/
        ["ami", "amd"] inteface IUpdate extends ServiceInterface
            {
                void saveObject(omeo::model::IObject obj) throws ServerError;
                void saveCollection(IObjectList objs) thows ServerError;
                omeo::model::IObject saveAndReturnObject(omero::model::IObject obj) throws ServerError;
                void saveAray(IObjectList graph) throws ServerError;
                IObjectList saveAndRetunArray(IObjectList graph) throws ServerError;
                omeo::sys::LongList saveAndReturnIds(IObjectList graph) throws ServerError;
                ["depecated:use omero::cmd::Delete2 instead"]
                void deleteObject(omeo::model::IObject row) throws ServerError;
                idempotent void indexObject(omeo::model::IObject row) throws ServerError;
            };

        class Save extends omeo::cmd::Request {
            omeo::model::IObject obj;
        };

        class SaveRsp extends omeo::cmd::Response {
            omeo::model::IObject obj;
        };

    };
};

#endif
