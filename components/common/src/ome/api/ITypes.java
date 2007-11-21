/*
 * ome.api.ITypes
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.api;

// Java imports
import java.util.Collection;
import java.util.List;

// Third-party libraries

// Application-internal dependencies
import ome.annotations.NotNull;
import ome.annotations.Validate;
import ome.model.IEnum;
import ome.model.IObject;
import ome.model.internal.Permissions;

/**
 * Access to reflective type information. Also provides simplified access to
 * special types like enumerations.
 * 
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 3.0 <small> (<b>Internal version:</b> $$) </small>
 * @since OMERO3
 */

public interface ITypes extends ServiceInterface {

    <T extends IObject> List<Class<T>> getResultTypes();

    <T extends IObject> List<Class<T>> getAnnotationTypes();

    <T extends IObject> List<Class<T>> getContainerTypes();

    <T extends IObject> List<Class<T>> getPojoTypes();

    <T extends IObject> List<Class<T>> getImportTypes();

    <T extends IEnum> T createEnumeration(T newEnum);

    <T extends IEnum> List<T> allEnumerations(Class<T> k);

    /**
     * lookup an enumeration value. As with the get-methods of {@link IQuery}
     * queries returning no results will through an exception.
     * 
     * @param <T>
     *            The type of the enumeration. Must extend {@link IEnum}
     * @param k
     *            An enumeration class which should be searched.
     * @param string
     *            The value for which an enumeration should be found.
     * @return A managed enumeration. Never null.
     * @throws ApiUsageException
     *             if {@link IEnum} is not found.
     */
    <T extends IEnum> T getEnumeration(Class<T> k, String string);
    
    /**
     * updates enumeration value specified by object
     * @param <T> 
     *            The type of the enumeration. Must extend {@link IEnum}
     * @param 
     *            oEnum An enumeration object which should be searched.
     * @return A managed enumeration. Never null.
     */
    <T extends IEnum> T updateEnumeration(@NotNull T oEnum);

    /**
     * updates enumeration value specified by object
     * @param <T> 
     *            The type of the enumeration. Must extend {@link IEnum}
     * @param listEnum 
     *            An enumeration collection of objects which should be searched.
     * @return A managed enumeration. Never null.
     */
    <T extends IEnum> void updateEnumerations(@NotNull @Validate(IEnum.class) List<T> listEnum);
    
    /**
     * deletes enumeration value specified by object
     * @param <T> 
     *            The type of the enumeration. Must extend {@link IEnum}
     * @param oEnum 
     *            An enumeration object which should be searched.
     */
    <T extends IEnum> void deleteEnumeration(@NotNull T oEnum);
    
    /**
     * Parses files CLASS_PATH/lib/common.jar!/mappings/*.ome.xml and 
     * looks for values for specyfied class of <code><enum></code> tag. 
     * @param <T> 
     *            The type of the enumeration. Must extend {@link IEnum}
     * @param klass 
     *            An enumeration class which should be searched.
     * @return A list of managed enumerations.
     * @throws ApiUsageException
     *             if {@link IEnum} is not found.
     */
    <T extends IEnum> List<T> allOryginalEnumerations(Class<T> klass);
    
    /**
     * Parses files CLASS_PATH/lib/common.jar!/mappings/*.ome.xml and 
     * looks for <code><enum></code> tag. 
     * @param <T> 
     *            The type of the enumeration. Must extend {@link IEnum}
     * @return list of Class of T extends IEnum
     * @throws ApiUsageException
     *             if {@link IEnum} is not found.
     */
    <T extends IEnum> List<Class<? extends IEnum>> getAllEnumerationTypes();

    <T extends IObject> Permissions permissions(Class<T> k);

}
