/*
 * ome.api.ITypes
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.api;

// Java imports
import java.util.List;
import java.util.Map;

import ome.annotations.NotNull;
import ome.annotations.Validate;
import ome.conditions.ApiUsageException;
import ome.model.IAnnotated;
import ome.model.IEnum;

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

    /**
     * Returns a list of classes which implement {@link IAnnotated}. These can
     * be used in combination with {@link ome.api.Search}.
     * 
     * @return a {@link List} of {@link IAnnotated} implementations
     */
    List<Class<IAnnotated>> getAnnotationTypes();

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
     * 
     * @param <T>
     *            The type of the enumeration. Must extend {@link IEnum}
     * @param oEnum
     *            An enumeration object which should be searched.
     * @return A managed enumeration. Never null.
     */
    <T extends IEnum> T updateEnumeration(@NotNull
    T oEnum);

    /**
     * updates enumeration value specified by object
     * 
     * @param <T>
     *            The type of the enumeration. Must extend {@link IEnum}
     * @param listEnum
     *            An enumeration collection of objects which should be searched.
     */
    <T extends IEnum> void updateEnumerations(@NotNull
    @Validate(IEnum.class)
    List<T> listEnum);

    /**
     * deletes enumeration value specified by object
     * 
     * @param <T>
     *            The type of the enumeration. Must extend {@link IEnum}
     * @param oEnum
     *            An enumeration object which should be searched.
     */
    <T extends IEnum> void deleteEnumeration(@NotNull
    T oEnum);

    /**
     * Gets all original values.
     * 
     * @param <T>
     *            The type of the enumeration. Must extend {@link IEnum}
     * @return A list of managed enumerations.
     * @throws RuntimeException
     *             if xml parsing failure.
     */
    <T extends IEnum> List<T> getOriginalEnumerations();

    <T extends IEnum> void resetEnumerations(Class<T> klass);

    /**
     * Gets all metadata classes which are IEnum type.
     * 
     * @param <T>
     *            The type of the enumeration. Must extend {@link IEnum}
     * @return list of Class of T extends IEnum
     * @throws RuntimeException
     *             if Class not found.
     */
    <T extends IEnum> List<Class<T>> getEnumerationTypes();

    /**
     * Gets all metadata classes which are IEnum type with contained objects.
     * 
     * @param <T>
     *            The type of the enumeration. Must extend {@link IEnum}
     * @return list of Class of T extends IEnum
     * @throws RuntimeException
     *             if xml parsing failure.
     */
    <T extends IEnum> Map<Class<T>, List<T>> getEnumerationsWithEntries();

}
