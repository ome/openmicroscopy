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
        /**
         * Provides method to interact with acquisition metadata and
         * annotations.
         **/
        ["ami", "amd"] interface IMetadata extends ServiceInterface
            {
                /**
                 * Loads the <code>logical channels</code> and the acquisition
                 * metadata related to them.
                 *
                 * @param ids The collection of logical channel's ids.
                 * 		      Mustn't be <code>null</code>.
                 * @return The collection of loaded logical channels.
                 **/
                idempotent LogicalChannelList loadChannelAcquisitionData(omero::sys::LongList ids) throws ServerError;

                /**
                 * Loads all the annotations of given types, that have been
                 * attached to the specified <code>rootNodes</code> for the
                 * specified <code>annotatorIds</code>.
                 * If no types specified, all annotations will be loaded.
                 * This method looks for the annotations that have been
                 * attached to each of the specified objects. It then maps
                 * each <code>rootId</code> onto the set of annotations
                 * that were found for that node. If no annotations were found
                 * for that node, then the entry will be <code>null</code>.
                 * Otherwise it will be a <code>Map</code> containing
                 * {@link omero.model.Annotation} objects.
                 *
                 * @param rootType
                 *      The type of the nodes the annotations are linked to.
                 *      Mustn't be <code>null</code>.
                 * @param rootIds
                 *      Ids of the objects of type <code>rootType</code>.
                 * 		Mustn't be <code>null</code>.
                 * @param annotationType
                 *      The types of annotation to retrieve. If
                 *      <code>null</code> all annotations will be loaded.
                 *      String of the type
                 *      <code>omero.model.annotations.*</code>.
                 * @param annotatorIds
                 *      Ids of the users for whom annotations should be
                 *      retrieved. If <code>null</code>, all annotations
                 *      returned.
                 * @param options
                 * @return A map whose key is rootId and value the
                 *         <code>Map</code> of all annotations for that node
                 *         or <code>null</code>.
                 **/
                idempotent LongIObjectListMap loadAnnotations(string rootType, omero::sys::LongList rootIds,
                                                         omero::api::StringSet annotationTypes, omero::sys::LongList annotatorIds,
                                                         omero::sys::Parameters options) throws ServerError;

                /**
                 * Loads all the annotations of a given type.
                 * It is possible to filter the annotations by including or
                 * excluding name spaces set on the annotations.
                 *
                 * @param annotationType The type of annotations to load.
                 * @param include
                 *      Include the annotations with the specified name spaces.
                 * @param exclude
                 *      Exclude the annotations with the specified name spaces.
                 * @param options   The POJO options.
                 * @return          A collection of found annotations.
                 **/
                idempotent AnnotationList loadSpecifiedAnnotations(string annotationType,
                                                                   omero::api::StringSet include,
                                                                   omero::api::StringSet exclude,
                                                                   omero::sys::Parameters options) throws ServerError;
                //idempotent omero::metadata::TagSetContainerList loadTagSets(long id, bool withObjects, omero::sys::Parameters options) throws ServerError;
                //idempotent omero::metadata::TagContainerList loadTags(long id, bool withObjects, omero::sys::Parameters options) throws ServerError;

                /**
                 * Loads the Tag Set if the id is specified otherwise loads
                 * all the Tag Set.
                 *
                 * @param ids The id of the tag to load or <code>-1</code>.
                 * @return Map whose key is a <code>Tag/Tag Set</code> and the
                 *         value either a Map or a list of related
                 *         <code>DataObject</code>.
                 **/
                idempotent LongIObjectListMap loadTagContent(omero::sys::LongList ids, omero::sys::Parameters options) throws ServerError;

                /**
                 * Loads all the tag Sets. Returns a collection of
                 * <code>AnnotationAnnotatioLink</code> objects and, if the
                 * <code>orphan</code> parameters is <code>true</code>, the
                 * <code>TagAnnotation</code> object.
                 * Note that the difference between a Tag Set and a Tag is made
                 * using the NS_INSIGHT_TAG_SET namespace.
                 *
                 * @param options The POJO options.
                 * @return See above.
                 **/
                idempotent IObjectList loadTagSets(omero::sys::Parameters options) throws ServerError;

                /**
                 * Returns a map whose key is a tag id and the value the
                 * number of Projects, Datasets, and Images linked to that tag.
                 *
                 * @param ids The collection of ids.
                 * @param options The POJO options.
                 * @return See above.
                 **/
                idempotent omero::sys::CountMap getTaggedObjectsCount(omero::sys::LongList ids, omero::sys::Parameters options) throws ServerError;

                /**
                 * Counts the number of annotation of a given type.
                 *
                 * @param annotationType The type of annotations to load.
                 * @param include   The collection of name space, one of the
                 *                  constants defined by this class.
                 * @param exclude   The collection of name space, one of the
                 *                  constants defined by this class.
                 * @param options	The POJO options.
                 * @return See above.
                 **/
                omero::RLong countSpecifiedAnnotations(string annotationType,
                                                       omero::api::StringSet include,
                                                       omero::api::StringSet exclude,
                                                       omero::sys::Parameters options) throws ServerError;

                /**
                 * Loads the specified annotations.
                 *
                 * @param annotationIds The collection of annotation ids.
                 * @return              See above.
                 */
                idempotent AnnotationList loadAnnotation(omero::sys::LongList annotationIds) throws ServerError;

                /**
                 * Loads the instrument and its components i.e. detectors,
                 * objectives, etc.
                 *
                 * @param id    The id of the instrument to load.
                 * @return      See above
                 */
                idempotent omero::model::Instrument loadInstrument(long id) throws ServerError;

                /**
                 * Loads the annotations of a given type used by the specified
                 * user but not owned by the user.
                 *
                 * @param annotationType    The type of annotations to load.
                 * @param userID            The identifier of the user.
                 * @return                  See above.
                 */
                idempotent IObjectList loadAnnotationsUsedNotOwned(string annotationType, long userID) throws ServerError;

                /**
                 * Counts the number of annotation of a given type used by the
                 * specified user but not owned by the user.
                 *
                 * @param annotationType    The type of annotations to load.
                 * @param userID            The identifier of the user.
                 * @return                  See above.
                 */
                omero::RLong countAnnotationsUsedNotOwned(string annotationType, long userID) throws ServerError;

                /**
                 * Loads the annotations of a given type linked to the
                 * specified objects. It is possible to filter the annotations
                 * by including or excluding name spaces set on the
                 * annotations.
                 *
                 * This method looks for the annotations that have been
                 * attached to each of the specified objects. It then maps
                 * each <code>rootNodeId</code> onto the set of annotations
                 * that were found for that node. If no annotations were found
                 * for that node, the map will not contain an entry for that
                 * node. Otherwise it will be a <code>Set</code> containing
                 * {@link omero.model.Annotation} objects.
                 * The <code>rootNodeType</code> supported are:
                 * Project, Dataset, Image, Pixels, Screen, Plate,
                 * PlateAcquisition, Well, Fileset.
                 *
                 * @param annotationType  The type of annotations to load.
                 * @param include
                 *      Include the annotations with the specified name spaces.
                 * @param exclude
                 *      Exclude the annotations with the specified name spaces.
                 * @param rootNodeType
                 *      The type of objects the annotations are linked to.
                 * @param rootNodeIds   The identifiers of the objects.
                 * @param options       The POJO options.
                 * @return              A collection of found annotations.
                 */
                idempotent LongAnnotationListMap loadSpecifiedAnnotationsLinkedTo(string annotationType,
                                                                   omero::api::StringSet include,
                                                                   omero::api::StringSet exclude,
                                                                   string rootNodeType,
                                                                   omero::sys::LongList rootNodeIds,
                                                                   omero::sys::Parameters options) throws ServerError;

                /**
                 * Finds the original file IDs for the import logs
                 * corresponding to the given Image or Fileset IDs.
                 *
                 * @param rootNodeType
                 *       the root node type, may be {@link omero.model.Image}
                 *       or {@link omero.model.Fileset}
                 * @param ids
                 *       the IDs of the entities for which the import log
                 *       original file IDs are required
                 * @return the original file IDs of the import logs
                 **/
                idempotent LongIObjectListMap loadLogFiles(string rootType, omero::sys::LongList ids) throws ServerError;
            };

    };
};

#endif
