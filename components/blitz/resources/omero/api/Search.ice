/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_API_SEARCH_ICE
#define OMERO_API_SEARCH_ICE

#include <omero/ServicesF.ice>
#include <omero/System.ice>
#include <omero/Collections.ice>

module omero {

    module api {

        /**
         * See <a href="http://downloads.openmicroscopy.org/latest/omero5.1/api/ome/api/Search.html">Search.html</a>
         **/
        ["ami", "amd"] interface Search extends StatefulServiceInterface
            {

                // Non-query state ~~~~~~~~~~~~~~~~~~~~~~

                idempotent int activeQueries() throws ServerError;
                idempotent void setBatchSize(int size) throws ServerError;
                idempotent int getBatchSize() throws ServerError;
                idempotent void setMergedBatches(bool merge) throws ServerError;
                idempotent bool isMergedBatches() throws ServerError;
                idempotent void setCaseSentivice(bool caseSensitive) throws ServerError;
                idempotent bool isCaseSensitive() throws ServerError;
                idempotent void setUseProjections(bool useProjections) throws ServerError;
                idempotent bool isUseProjections() throws ServerError;
                idempotent void setReturnUnloaded(bool returnUnloaded) throws ServerError;
                idempotent bool isReturnUnloaded() throws ServerError;
                idempotent void setAllowLeadingWildcard(bool allowLeadingWildcard) throws ServerError;
                idempotent bool isAllowLeadingWildcard() throws ServerError;


                // Filters ~~~~~~~~~~~~~~~~~~~~~~

                void onlyType(string klass) throws ServerError;
                void onlyTypes(StringSet classes) throws ServerError;
                void allTypes() throws ServerError;
                void onlyIds(omero::sys::LongList ids) throws ServerError;
                void onlyOwnedBy(omero::model::Details d) throws ServerError;
                void notOwnedBy(omero::model::Details d) throws ServerError;
                void onlyCreatedBetween(omero::RTime start, omero::RTime  stop) throws ServerError;
                void onlyModifiedBetween(omero::RTime start, omero::RTime stop) throws ServerError;
                void onlyAnnotatedBetween(omero::RTime start, omero::RTime stop) throws ServerError;
                void onlyAnnotatedBy(omero::model::Details d) throws ServerError;
                void notAnnotatedBy(omero::model::Details d) throws ServerError;
                void onlyAnnotatedWith(StringSet classes) throws ServerError;


                // Fetches, order, counts, etc ~~~~~~~~~~~~~~~~~~~~~~

                void addOrderByAsc(string path) throws ServerError;
                void addOrderByDesc(string path) throws ServerError;
                void unordered() throws ServerError;
                void fetchAnnotations(StringSet classes) throws ServerError;
                void fetchAlso(StringSet fetches) throws ServerError;


                // Reset ~~~~~~~~~~~~~~~~~~~~~~~~~

                void resetDefaults() throws ServerError;


                // Query state  ~~~~~~~~~~~~~~~~~~~~~~~~~

                void byGroupForTags(string group) throws ServerError;
                void byTagForGroups(string tag) throws ServerError;
                void byFullText(string query) throws ServerError;
                void byLuceneQueryBuilder(string fields, string from, string to, string dateType, string query) throws ServerError;
                void bySimilarTerms(StringSet terms) throws ServerError;
                void byHqlQuery(string query, omero::sys::Parameters params) throws ServerError;
                void bySomeMustNone(StringSet some, StringSet must, StringSet none) throws ServerError;
                void byAnnotatedWith(AnnotationList examples) throws ServerError;
                void clearQueries() throws ServerError;

                void and() throws ServerError;
                void or() throws ServerError;
                void not() throws ServerError;


                // Retrieval  ~~~~~~~~~~~~~~~~~~~~~~~~~

                idempotent bool hasNext() throws ServerError;
                omero::model::IObject next() throws ServerError;
                IObjectList results() throws ServerError;

                // Currently unused
                idempotent SearchMetadata currentMetadata() throws ServerError;
                idempotent SearchMetadataList currentMetadataList() throws ServerError;

                // Unused; Part of Java Iterator interface
                void remove() throws ServerError;
            };

    };
};

#endif
