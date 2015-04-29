/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_API_ICONTAINER_ICE
#define OMERO_API_ICONTAINER_ICE

#include <omero/ModelF.ice>
#include <omero/ServicesF.ice>
#include <omero/System.ice>
#include <omero/Collections.ice>

module omero {

    module api {

        /**
         * See <a href="http://hudson.openmicroscopy.org.uk/job/OMERO/javadoc/ome/api/IContainer.html">IContainer.html</a>
         **/
        ["ami", "amd"] interface IContainer extends ServiceInterface
            {
                idempotent IObjectList loadContainerHierarchy(string rootType, omero::sys::LongList rootIds, omero::sys::Parameters options) throws ServerError;
                idempotent IObjectList findContainerHierarchies(string rootType, omero::sys::LongList imageIds, omero::sys::Parameters options) throws ServerError;
                //idempotent AnnotationMap findAnnotations(string rootType, omero::sys::LongList rootIds, omero::sys::LongList annotatorIds, omero::sys::Parameters options) throws ServerError;
                idempotent ImageList getImages(string rootType, omero::sys::LongList rootIds, omero::sys::Parameters options) throws ServerError;
                idempotent ImageList getUserImages(omero::sys::Parameters options) throws ServerError;
                idempotent ImageList getImagesByOptions(omero::sys::Parameters options) throws ServerError;
                /* Warning: following discussion in trac ticket 11019 the return type of getImagesBySplitFilesets may be changed. */
                idempotent IdBooleanLongListMapMap getImagesBySplitFilesets(StringLongListMap included, omero::sys::Parameters options) throws ServerError;
                idempotent omero::sys::CountMap getCollectionCount(string type, string property, omero::sys::LongList ids, omero::sys::Parameters options) throws ServerError;
                idempotent IObjectList retrieveCollection(omero::model::IObject obj, string collectionName, omero::sys::Parameters options) throws ServerError;
                omero::model::IObject createDataObject(omero::model::IObject obj, omero::sys::Parameters options) throws ServerError;
                IObjectList createDataObjects(IObjectList dataObjects, omero::sys::Parameters options) throws ServerError;
                void unlink(IObjectList links, omero::sys::Parameters options) throws ServerError;
                IObjectList link(IObjectList links, omero::sys::Parameters options) throws ServerError;
                omero::model::IObject updateDataObject(omero::model::IObject obj, omero::sys::Parameters options) throws ServerError;
                IObjectList updateDataObjects(IObjectList objs, omero::sys::Parameters options) throws ServerError;
            };

    };
};

#endif
