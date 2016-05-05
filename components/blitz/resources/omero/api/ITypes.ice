/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_API_ITYPES_ICE
#define OMERO_API_ITYPES_ICE

#include <omero/ModelF.ice>
#include <omero/ServicesF.ice>
#include <omero/Collections.ice>


module omero {

    module api {

        /**
         * Access to reflective type information. Also provides simplified
         * access to special types like enumerations.
         **/
        ["ami", "amd"] interface ITypes extends ServiceInterface
            {
                omero::model::IObject createEnumeration(omero::model::IObject newEnum) throws ServerError;

                /**
                 * Lookup an enumeration value. As with the get-methods of
                 * {@link omero.api.IQuery} queries returning no results
                 * will through an exception.
                 *
                 * @param type An enumeration class which should be searched.
                 * @param value The value for which an enumeration should be
                 *              found.
                 * @return A managed enumeration. Never null.
                 * @throws ApiUsageException
                 *         if {@link omero.model.IEnum} is not found.
                 */
                idempotent omero::model::IObject getEnumeration(string type, string value) throws ServerError;
                idempotent IObjectList allEnumerations(string type) throws ServerError;

                /**
                 * Updates enumeration value specified by object.
                 *
                 * @param oldEnum An enumeration object which should be
                 *                searched.
                 * @return A managed enumeration. Never null.
                 */
                omero::model::IObject updateEnumeration(omero::model::IObject oldEnum) throws ServerError;

                /**
                 * Updates enumeration value specified by object.
                 *
                 * @param oldEnums An enumeration collection of objects which
                 *                 should be searched.
                 */
                void updateEnumerations(IObjectList oldEnums) throws ServerError;

                /**
                 * Deletes enumeration value specified by object.
                 *
                 * @param oldEnum An enumeration object which should be
                 *                searched.
                 */
                void deleteEnumeration(omero::model::IObject oldEnum) throws ServerError;

                /**
                 * Gets all metadata classes which are IEnum type.
                 *
                 * @return set of classes that extend IEnum
                 * @throws RuntimeException if Class not found.
                 */
                idempotent StringSet getEnumerationTypes() throws ServerError;

                /**
                 * Returns a list of classes which implement
                 * {@link omero.model.IAnnotated}. These can
                 * be used in combination with {@link omero.api.Search}.
                 *
                 * @return a {@link StringSet} of
                 *         {@link omero.model.IAnnotated} implementations
                 */
                idempotent StringSet getAnnotationTypes() throws ServerError;

                /**
                 * Gets all metadata classes which are IEnum type with
                 * contained objects.
                 *
                 * @return map of classes that extend IEnum
                 * @throws RuntimeExceptionif xml parsing failure.
                 */
                idempotent IObjectListMap getEnumerationsWithEntries() throws ServerError;

                /**
                 * Gets all original values.
                 *
                 * @return A list of managed enumerations.
                 * @throws RuntimeException if xml parsing failure.
                 */
                idempotent IObjectList getOriginalEnumerations() throws ServerError;
                void resetEnumerations(string enumClass) throws ServerError;
            };

    };
};

#endif
