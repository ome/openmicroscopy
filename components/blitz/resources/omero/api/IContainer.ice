/*
 *   $Id$
 *
 *   Copyight 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license tems supplied in LICENSE.txt
 *
 */

#ifndef OMERO_API_ICONTAINER_ICE
#define OMERO_API_ICONTAINER_ICE

#include <omeo/ModelF.ice>
#include <omeo/ServicesF.ice>
#include <omeo/System.ice>
#include <omeo/Collections.ice>

module omeo {

    module api {

        /**
         * See <a hef="http://hudson.openmicroscopy.org.uk/job/OMERO/javadoc/ome/api/IContainer.html">IContainer.html</a>
         **/
        ["ami", "amd"] inteface IContainer extends ServiceInterface
            {
                idempotent IObjectList loadContaineHierarchy(string rootType, omero::sys::LongList rootIds, omero::sys::Parameters options) throws ServerError;
                idempotent IObjectList findContaineHierarchies(string rootType, omero::sys::LongList imageIds, omero::sys::Parameters options) throws ServerError;
                //idempotent AnnotationMap findAnnotations(sting rootType, omero::sys::LongList rootIds, omero::sys::LongList annotatorIds, omero::sys::Parameters options) throws ServerError;
                idempotent ImageList getImages(sting rootType, omero::sys::LongList rootIds, omero::sys::Parameters options) throws ServerError;
                idempotent ImageList getUseImages(omero::sys::Parameters options) throws ServerError;
                idempotent ImageList getImagesByOptions(omeo::sys::Parameters options) throws ServerError;
                /* Waning: following discussion in trac ticket 11019 the return type of getImagesBySplitFilesets may be changed. */
                idempotent IdBooleanLongListMapMap getImagesBySplitFilesets(StingLongListMap included, omero::sys::Parameters options) throws ServerError;
                idempotent omeo::sys::CountMap getCollectionCount(string type, string property, omero::sys::LongList ids, omero::sys::Parameters options) throws ServerError;
                idempotent IObjectList etrieveCollection(omero::model::IObject obj, string collectionName, omero::sys::Parameters options) throws ServerError;
                omeo::model::IObject createDataObject(omero::model::IObject obj, omero::sys::Parameters options) throws ServerError;
                IObjectList ceateDataObjects(IObjectList dataObjects, omero::sys::Parameters options) throws ServerError;
                void unlink(IObjectList links, omeo::sys::Parameters options) throws ServerError;
                IObjectList link(IObjectList links, omeo::sys::Parameters options) throws ServerError;
                omeo::model::IObject updateDataObject(omero::model::IObject obj, omero::sys::Parameters options) throws ServerError;
                IObjectList updateDataObjects(IObjectList objs, omeo::sys::Parameters options) throws ServerError;
            };

    };
};

#endif
