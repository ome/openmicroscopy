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

        /**
         * See <a href="http://downloads.openmicroscopy.org/latest/omero5.2/api/ome/api/IQuery.html">IQuery.html</a>
         **/
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

                /**
                 * Return a sequence of {@link omero.RType} sequences.
                 *
                 * <p>
                 * Each element of the outer sequence is one row in the return value.
                 * Each element of the inner sequence is one column specified in the HQL.
                 * </p>
                 *
                 * <p>
                 * {@link omero.model.IObject} instances are returned wrapped
                 * in an {@link omero.rtype.RObject} instance. Primitives are
                 * mapped to the expected {@link omero.RType} subclass. Types
                 * without an {@link omero.RType} mapper if returned will
                 * throw an exception if present in the select except where a
                 * manual conversion is present on the server. This includes:
                 * </p>
                 *
                 * <ul>
                 * <li>
                 *     {@link omero.model.Permissions} instances are serialized to an {@link omero.RMap}
                 *     containing the keys: perms, canAnnotate, canEdit, canLink, canDelete
                 * </li>
                 * <li>
                 *     The quantity types like {@link omero.model.Length} are serialized
                 *     to an {@link omero.RMap} containing the keys: value, unit, symbol
                 * </li>
                 * </ul>
                 *
                 * <p>
                 * As with SQL, if an aggregation statement is used, a group by clause must be added.
                 * </p>
                 *
                 * <p>
                 * Examples:
                 * <pre>
                 *   select i.name, i.description from Image i where i.name like '%.dv'
                 *
                 *   select tag.textValue, tagset.textValue from TagAnnotation tag join tag.annotationLinks l join l.child tagset
                 *
                 *   select p.pixelsType.value, count(p.id) from Pixel p group by p.pixelsType.value
                 * </pre>
                 * </p>
                 *
                 **/
                idempotent RTypeSeqSeq           projection(string query, omero::sys::Parameters params) throws ServerError;

                idempotent omero::model::IObject refresh(omero::model::IObject iObject) throws ServerError;
            };

    };
};

#endif
