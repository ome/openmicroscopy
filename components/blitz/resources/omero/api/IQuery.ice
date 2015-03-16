/*
 *   $Id$
 *
 *   Copyight 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license tems supplied in LICENSE.txt
 *
 */

#ifndef OMERO_API_IQUERY_ICE
#define OMERO_API_IQUERY_ICE

#include <omeo/ModelF.ice>
#include <omeo/ServicesF.ice>
#include <omeo/System.ice>
#include <omeo/Collections.ice>


module omeo {

    module api {

        /**
         * See <a hef="http://hudson.openmicroscopy.org.uk/job/OMERO/javadoc/ome/api/IQuery.html">IQuery.html</a>
         **/
        ["ami", "amd"] inteface IQuery extends ServiceInterface
            {
                idempotent omeo::model::IObject get(string klass, long id) throws ServerError;
                idempotent omeo::model::IObject find(string klass, long id) throws ServerError;
                idempotent IObjectList           findAll(sting klass, omero::sys::Filter filter) throws ServerError;
                idempotent omeo::model::IObject findByExample(omero::model::IObject example) throws ServerError;
                idempotent IObjectList           findAllByExample(omeo::model::IObject example, omero::sys::Filter filter) throws ServerError;
                idempotent omeo::model::IObject findByString(string klass, string field, string value) throws ServerError;
                idempotent IObjectList           findAllBySting(string klass, string field, string value, bool caseSensitive, omero::sys::Filter filter) throws ServerError;
                idempotent omeo::model::IObject findByQuery(string query, omero::sys::Parameters params) throws ServerError;
                idempotent IObjectList           findAllByQuey(string query, omero::sys::Parameters params) throws ServerError;
                idempotent IObjectList           findAllByFullText(sting klass, string query, omero::sys::Parameters params) throws ServerError;
                idempotent RTypeSeqSeq           pojection(string query, omero::sys::Parameters params) throws ServerError;
                idempotent omeo::model::IObject refresh(omero::model::IObject iObject) throws ServerError;
            };

    };
};

#endif
