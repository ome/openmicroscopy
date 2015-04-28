/*
 *   $Id$
 *
 *   Copyright 2010-2014 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_API_IMETADATA_ICE
#define OMERO_API_IMETADATA_ICE

#include <omero/ServicesF.ice>
#include <omero/System.ice>
#include <omero/Collections.ice>


module omero {

    module api {

        ["ami", "amd"] interface IMetadata extends ServiceInterface
            {
                idempotent LogicalChannelList loadChannelAcquisitionData(omero::sys::LongList ids) throws ServerError;
                idempotent LongIObjectListMap loadAnnotations(string rootType, omero::sys::LongList rootIds,
                                                         omero::api::StringSet annotationTypes, omero::sys::LongList annotatorIds,
                                                         omero::sys::Parameters options) throws ServerError;
                idempotent AnnotationList loadSpecifiedAnnotations(string annotationType,
                                                                   omero::api::StringSet include,
                                                                   omero::api::StringSet exclude,
                                                                   omero::sys::Parameters options) throws ServerError;
                //idempotent omero::metadata::TagSetContainerList loadTagSets(long id, bool withObjects, omero::sys::Parameters options) throws ServerError;
                //idempotent omero::metadata::TagContainerList loadTags(long id, bool withObjects, omero::sys::Parameters options) throws ServerError;
                idempotent LongIObjectListMap loadTagContent(omero::sys::LongList ids, omero::sys::Parameters options) throws ServerError;
                idempotent IObjectList loadTagSets(omero::sys::Parameters options) throws ServerError;
                idempotent omero::sys::CountMap getTaggedObjectsCount(omero::sys::LongList ids, omero::sys::Parameters options) throws ServerError;
                omero::RLong countSpecifiedAnnotations(string annotationType,
                                                       omero::api::StringSet include,
                                                       omero::api::StringSet exclude,
                                                       omero::sys::Parameters options) throws ServerError;
                idempotent AnnotationList loadAnnotation(omero::sys::LongList annotationIds) throws ServerError;
                idempotent omero::model::Instrument loadInstrument(long id) throws ServerError;
                idempotent IObjectList loadAnnotationsUsedNotOwned(string annotationType, long userID) throws ServerError;
                omero::RLong countAnnotationsUsedNotOwned(string annotationType, long userID) throws ServerError;
                idempotent LongAnnotationListMap loadSpecifiedAnnotationsLinkedTo(string annotationType,
                                                                   omero::api::StringSet include,
                                                                   omero::api::StringSet exclude,
                                                                   string rootNodeType,
                                                                   omero::sys::LongList rootNodeIds,
                                                                   omero::sys::Parameters options) throws ServerError;
                idempotent LongIObjectListMap loadLogFiles(string rootType, omero::sys::LongList ids) throws ServerError;
            };

    };
};

#endif
