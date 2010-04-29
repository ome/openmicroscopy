/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_API_IMETADATA_ICE
#define OMERO_API_IMETADATA_ICE

#include <omero/ServicesF.ice>
#include <omero/System.ice>


module omero {

    module api {
        /**
         * See <a href="http://hudson.openmicroscopy.org.uk/job/OMERO/javadoc/ome/api/IMetadata.html">IMetadata.html</a>
         **/
        ["ami", "amd"] interface IMetadata extends ServiceInterface
            {
                idempotent LogicalChannelList loadChannelAcquisitionData(omero::sys::LongList ids) throws ServerError;
                idempotent IObjectListMap loadAnnotations(string rootType, omero::sys::LongList rootIds,
                                                         omero::api::StringSet annotationTypes, omero::sys::LongList annotatorIds,
                                                         omero::sys::Parameters options) throws ServerError;
                idempotent IObjectListMap loadSpecifiedAnnotations(string annotationType,
                                                                   omero::api::StringSet include,
                                                                   omero::api::StringSet exclude,
                                                                   omero::sys::Parameters options) throws ServerError;
                //idempotent omero::metadata::TagSetContainerList loadTagSets(long id, bool withObjects, omero::sys::Parameters options) throws ServerError;
                //idempotent omero::metadata::TagContainerList loadTags(long id, bool withObjects, omero::sys::Parameters options) throws ServerError;
                idempotent IObjectListMap loadTagContent(omero::sys::LongList ids, omero::sys::Parameters options) throws ServerError;
                idempotent IObjectList loadTagSets(omero::sys::Parameters options) throws ServerError;
                idempotent omero::sys::CountMap getTaggedObjectsCount(omero::sys::LongList ids, omero::sys::Parameters options) throws ServerError;
                omero::RLong countSpecifiedAnnotations(string annotationType,
                                                       omero::api::StringSet include,
                                                       omero::api::StringSet exclude,
                                                       omero::sys::Parameters options) throws ServerError;
                idempotent IObjectListMap loadAnnotation(omero::sys::LongList annotationIds) throws ServerError;
                idempotent IObjectList loadInstrument(long id) throws ServerError;
            };

    };
};

#endif
