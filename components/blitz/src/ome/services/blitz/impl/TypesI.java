/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.impl;

import java.util.List;

import ome.api.ITypes;
import ome.services.blitz.util.BlitzExecutor;
import omero.ServerError;
import omero.api.AMD_ITypes_allEnumerations;
import omero.api.AMD_ITypes_createEnumeration;
import omero.api.AMD_ITypes_deleteEnumeration;
import omero.api.AMD_ITypes_getAnnotationTypes;
import omero.api.AMD_ITypes_getEnumeration;
import omero.api.AMD_ITypes_getEnumerationTypes;
import omero.api.AMD_ITypes_getEnumerationsWithEntries;
import omero.api.AMD_ITypes_getOriginalEnumerations;
import omero.api.AMD_ITypes_resetEnumerations;
import omero.api.AMD_ITypes_updateEnumeration;
import omero.api.AMD_ITypes_updateEnumerations;
import omero.api._ITypesOperations;
import omero.model.IObject;
import Ice.Current;

/**
 * Implementation of the ITypes service.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta4
 * @see ome.api.ITypes
 */
public class TypesI extends AbstractAmdServant implements _ITypesOperations {

    public TypesI(ITypes service, BlitzExecutor be) {
        super(service, be);
    }

    // Interface methods
    // =========================================================================

    public void allEnumerations_async(AMD_ITypes_allEnumerations __cb,
            String type, Current __current) throws ServerError {
        serviceInterfaceCall(__cb, __current, type);

    }

    public void createEnumeration_async(AMD_ITypes_createEnumeration __cb,
            IObject newEnum, Current __current) throws ServerError {
        serviceInterfaceCall(__cb, __current, newEnum);

    }

    public void deleteEnumeration_async(AMD_ITypes_deleteEnumeration __cb,
            IObject oldEnum, Current __current) throws ServerError {
        serviceInterfaceCall(__cb, __current, oldEnum);

    }

    public void getAnnotationTypes_async(AMD_ITypes_getAnnotationTypes __cb,
            Current __current) throws ServerError {
        serviceInterfaceCall(__cb, __current);

    }

    public void getEnumerationTypes_async(AMD_ITypes_getEnumerationTypes __cb,
            Current __current) throws ServerError {
        serviceInterfaceCall(__cb, __current);

    }

    public void getEnumeration_async(AMD_ITypes_getEnumeration __cb,
            String type, String value, Current __current) throws ServerError {
        serviceInterfaceCall(__cb, __current, type, value);

    }

    public void getEnumerationsWithEntries_async(
            AMD_ITypes_getEnumerationsWithEntries __cb, Current __current)
            throws ServerError {
        serviceInterfaceCall(__cb, __current);

    }

    public void getOriginalEnumerations_async(
            AMD_ITypes_getOriginalEnumerations __cb, Current __current)
            throws ServerError {
        serviceInterfaceCall(__cb, __current);

    }

    public void resetEnumerations_async(AMD_ITypes_resetEnumerations __cb,
            String enumClass, Current __current) throws ServerError {
        serviceInterfaceCall(__cb, __current, enumClass);

    }

    public void updateEnumeration_async(AMD_ITypes_updateEnumeration __cb,
            IObject oldEnum, Current __current) throws ServerError {
        serviceInterfaceCall(__cb, __current, oldEnum);

    }

    public void updateEnumerations_async(AMD_ITypes_updateEnumerations __cb,
            List<IObject> oldEnums, Current __current) throws ServerError {
        serviceInterfaceCall(__cb, __current, oldEnums);

    }

}
