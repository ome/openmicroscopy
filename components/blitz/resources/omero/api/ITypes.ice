/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_API_ITYPES_ICE
#define OMERO_API_ITYPES_ICE

#include <omero/ModelF.ice>
#include <omero/ServicesF.ice>
#include <omero/Collections.ice>


module omero {

    module api {

        ["ami", "amd"] interface ITypes extends ServiceInterface
            {
                omero::model::IObject createEnumeration(omero::model::IObject newEnum) throws ServerError;
                idempotent omero::model::IObject getEnumeration(string type, string value) throws ServerError;
                idempotent IObjectList allEnumerations(string type) throws ServerError;
                omero::model::IObject updateEnumeration(omero::model::IObject oldEnum) throws ServerError;
                void updateEnumerations(IObjectList oldEnums) throws ServerError;
                void deleteEnumeration(omero::model::IObject oldEnum) throws ServerError;
                idempotent StringSet getEnumerationTypes() throws ServerError;
                idempotent StringSet getAnnotationTypes() throws ServerError;
                idempotent IObjectListMap getEnumerationsWithEntries() throws ServerError;
                idempotent IObjectList getOriginalEnumerations() throws ServerError;
                void resetEnumerations(string enumClass) throws ServerError;
            };

    };
};

#endif
