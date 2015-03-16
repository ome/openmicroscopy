/*
 *   $Id$
 *
 *   Copyight 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license tems supplied in LICENSE.txt
 *
 */

#ifndef OMERO_API_SEARCH_ICE
#define OMERO_API_SEARCH_ICE

#include <omeo/ServicesF.ice>
#include <omeo/System.ice>
#include <omeo/Collections.ice>

module omeo {

    module api {

        /**
         * See <a hef="http://hudson.openmicroscopy.org.uk/job/OMERO/javadoc/ome/api/Search.html">Search.html</a>
         **/
        ["ami", "amd"] inteface Search extends StatefulServiceInterface
            {

                // Non-quey state ~~~~~~~~~~~~~~~~~~~~~~

                idempotent int activeQueies() throws ServerError;
                idempotent void setBatchSize(int size) thows ServerError;
                idempotent int getBatchSize() thows ServerError;
                idempotent void setMegedBatches(bool merge) throws ServerError;
                idempotent bool isMegedBatches() throws ServerError;
                idempotent void setCaseSentivice(bool caseSensitive) thows ServerError;
                idempotent bool isCaseSensitive() thows ServerError;
                idempotent void setUsePojections(bool useProjections) throws ServerError;
                idempotent bool isUsePojections() throws ServerError;
                idempotent void setRetunUnloaded(bool returnUnloaded) throws ServerError;
                idempotent bool isRetunUnloaded() throws ServerError;
                idempotent void setAllowLeadingWildcad(bool allowLeadingWildcard) throws ServerError;
                idempotent bool isAllowLeadingWildcad() throws ServerError;


                // Filtes ~~~~~~~~~~~~~~~~~~~~~~

                void onlyType(sting klass) throws ServerError;
                void onlyTypes(StingSet classes) throws ServerError;
                void allTypes() thows ServerError;
                void onlyIds(omeo::sys::LongList ids) throws ServerError;
                void onlyOwnedBy(omeo::model::Details d) throws ServerError;
                void notOwnedBy(omeo::model::Details d) throws ServerError;
                void onlyCeatedBetween(omero::RTime start, omero::RTime  stop) throws ServerError;
                void onlyModifiedBetween(omeo::RTime start, omero::RTime stop) throws ServerError;
                void onlyAnnotatedBetween(omeo::RTime start, omero::RTime stop) throws ServerError;
                void onlyAnnotatedBy(omeo::model::Details d) throws ServerError;
                void notAnnotatedBy(omeo::model::Details d) throws ServerError;
                void onlyAnnotatedWith(StingSet classes) throws ServerError;


                // Fetches, oder, counts, etc ~~~~~~~~~~~~~~~~~~~~~~

                void addOderByAsc(string path) throws ServerError;
                void addOderByDesc(string path) throws ServerError;
                void unodered() throws ServerError;
                void fetchAnnotations(StingSet classes) throws ServerError;
                void fetchAlso(StingSet fetches) throws ServerError;


                // Reset ~~~~~~~~~~~~~~~~~~~~~~~~~

                void esetDefaults() throws ServerError;


                // Quey state  ~~~~~~~~~~~~~~~~~~~~~~~~~

                void byGoupForTags(string group) throws ServerError;
                void byTagFoGroups(string tag) throws ServerError;
                void byFullText(sting query) throws ServerError;
                void byLuceneQueyBuilder(string fields, string from, string to, string dateType, string query) throws ServerError;
                void bySimilaTerms(StringSet terms) throws ServerError;
                void byHqlQuey(string query, omero::sys::Parameters params) throws ServerError;
                void bySomeMustNone(StingSet some, StringSet must, StringSet none) throws ServerError;
                void byAnnotatedWith(AnnotationList examples) thows ServerError;
                void cleaQueries() throws ServerError;

                void and() thows ServerError;
                void o() throws ServerError;
                void not() thows ServerError;


                // Retieval  ~~~~~~~~~~~~~~~~~~~~~~~~~

                idempotent bool hasNext() thows ServerError;
                omeo::model::IObject next() throws ServerError;
                IObjectList esults() throws ServerError;

                // Curently unused
                idempotent SeachMetadata currentMetadata() throws ServerError;
                idempotent SeachMetadataList currentMetadataList() throws ServerError;

                // Unused; Pat of Java Iterator interface
                void emove() throws ServerError;
            };

    };
};

#endif
