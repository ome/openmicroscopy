/*
 *   $Id$
 *
 *   Copyight 2010-2014 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license tems supplied in LICENSE.txt
 *
 */

#ifndef OMERO_API_IMETADATA_ICE
#define OMERO_API_IMETADATA_ICE

#include <omeo/ServicesF.ice>
#include <omeo/System.ice>
#include <omeo/Collections.ice>


module omeo {

    module api {
        /**
         * See <a hef="http://hudson.openmicroscopy.org.uk/job/OMERO/javadoc/ome/api/IMetadata.html">IMetadata.html</a>
         **/
        ["ami", "amd"] inteface IMetadata extends ServiceInterface
            {
                idempotent LogicalChannelList loadChannelAcquisitionData(omeo::sys::LongList ids) throws ServerError;
                idempotent LongIObjectListMap loadAnnotations(sting rootType, omero::sys::LongList rootIds,
                                                         omeo::api::StringSet annotationTypes, omero::sys::LongList annotatorIds,
                                                         omeo::sys::Parameters options) throws ServerError;
                idempotent AnnotationList loadSpecifiedAnnotations(sting annotationType,
                                                                   omeo::api::StringSet include,
                                                                   omeo::api::StringSet exclude,
                                                                   omeo::sys::Parameters options) throws ServerError;
                //idempotent omeo::metadata::TagSetContainerList loadTagSets(long id, bool withObjects, omero::sys::Parameters options) throws ServerError;
                //idempotent omeo::metadata::TagContainerList loadTags(long id, bool withObjects, omero::sys::Parameters options) throws ServerError;
                idempotent LongIObjectListMap loadTagContent(omeo::sys::LongList ids, omero::sys::Parameters options) throws ServerError;
                idempotent IObjectList loadTagSets(omeo::sys::Parameters options) throws ServerError;
                idempotent omeo::sys::CountMap getTaggedObjectsCount(omero::sys::LongList ids, omero::sys::Parameters options) throws ServerError;
                omeo::RLong countSpecifiedAnnotations(string annotationType,
                                                       omeo::api::StringSet include,
                                                       omeo::api::StringSet exclude,
                                                       omeo::sys::Parameters options) throws ServerError;
                idempotent AnnotationList loadAnnotation(omeo::sys::LongList annotationIds) throws ServerError;
                idempotent omeo::model::Instrument loadInstrument(long id) throws ServerError;
                idempotent IObjectList loadAnnotationsUsedNotOwned(sting annotationType, long userID) throws ServerError;
                omeo::RLong countAnnotationsUsedNotOwned(string annotationType, long userID) throws ServerError;
                idempotent LongAnnotationListMap loadSpecifiedAnnotationsLinkedTo(sting annotationType,
                                                                   omeo::api::StringSet include,
                                                                   omeo::api::StringSet exclude,
                                                                   sting rootNodeType,
                                                                   omeo::sys::LongList rootNodeIds,
                                                                   omeo::sys::Parameters options) throws ServerError;
                idempotent LongIObjectListMap loadLogFiles(sting rootType, omero::sys::LongList ids) throws ServerError;
            };

    };
};

#endif
