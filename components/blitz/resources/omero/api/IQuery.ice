/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_API_IQUERY_ICE
#define OMERO_API_IQUERY_ICE

#include <omero/ModelF.ice>
#include <omero/ServicesF.ice>
#include <omero/System.ice>
#include <omero/Collections.ice>


module omero {

    module api {

        ["ami", "amd"] interface IQuery extends ServiceInterface
            {
                idempotent omero::model::IObject get(string klass, long id) throws ServerError;
                idempotent omero::model::IObject find(string klass, long id) throws ServerError;
                idempotent IObjectList           findAll(string klass, omero::sys::Filter filter) throws ServerError;
                idempotent omero::model::IObject findByExample(omero::model::IObject example) throws ServerError;
                idempotent IObjectList           findAllByExample(omero::model::IObject example, omero::sys::Filter filter) throws ServerError;
                idempotent omero::model::IObject findByString(string klass, string field, string value) throws ServerError;
                idempotent IObjectList           findAllByString(string klass, string field, string value, bool caseSensitive, omero::sys::Filter filter) throws ServerError;
                idempotent omero::model::IObject findByQuery(string query, omero::sys::Parameters params) throws ServerError;
                idempotent IObjectList           findAllByQuery(string query, omero::sys::Parameters params) throws ServerError;
                idempotent IObjectList           findAllByFullText(string klass, string query, omero::sys::Parameters params) throws ServerError;
                idempotent RTypeSeqSeq           projection(string query, omero::sys::Parameters params) throws ServerError;
                idempotent omero::model::IObject refresh(omero::model::IObject iObject) throws ServerError;
            };

    };
};

#endif
