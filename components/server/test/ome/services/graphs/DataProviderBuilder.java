/*
 * Copyright (C) 2017 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package ome.services.graphs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import ome.model.IObject;

import org.testng.annotations.DataProvider;

/**
 * Utility class for building return values for {@link DataProvider} methods. Instances of this builder can still be used even after
 * {@link DataProviderBuilder#build()} has been called.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.4.1
 */
class DataProviderBuilder {

    private List<List<Object>> args = Collections.singletonList(Collections.emptyList());

    /**
     * Provide test methods with an additional argument that iterates through every value of an {@link java.lang.Enum}.
     * @param enumClass the enumeration class whose values are to be iterated through
     * @return this builder with the additional argument noted
     */
    DataProviderBuilder add(Class<? extends Enum<?>> enumClass) {
        final Enum<?>[] enums = enumClass.getEnumConstants();
        final List<List<Object>> newArgs = new ArrayList<>(args.size() * enums.length);
        for (final List<Object> arg : args) {
            for (final Enum<?> e : enums) {
                final List<Object> newArg = new ArrayList<>(arg.size() + 1);
                newArg.addAll(arg);
                newArg.add(e);
                newArgs.add(newArg);
            }
        }
        args = newArgs;
        return this;
    }

    /**
     * Provide test methods with an additional argument that iterates through <em>an instance of</em> every given kind of model
     * object.
     * @param objectClasses the kinds of model object that are to be iterated through
     * @return this builder with the additional argument noted
     * @throws ReflectiveOperationException if the model object could not be instantiated
     */
    DataProviderBuilder add(Collection<Class<? extends IObject>> objectClasses)
            throws ReflectiveOperationException {
        final List<IObject> objects = new ArrayList<>(objectClasses.size());
        for (final Class<? extends IObject> objectClass : objectClasses) {
            objects.add(objectClass.newInstance());
        }
        final List<List<Object>> newArgs = new ArrayList<>(args.size() * objects.size());
        for (final List<Object> arg : args) {
            for (final IObject o : objects) {
                final List<Object> newArg = new ArrayList<>(arg.size() + 1);
                newArg.addAll(arg);
                newArg.add(o);
                newArgs.add(newArg);
            }
        }
        args = newArgs;
        return this;
    }

    /**
     * Provide test methods with an additional argument that iterates through Boolean values.
     * @param isNullable if {@code null} should be included along with {@code true} and {@code false}
     * @return this builder with the additional argument noted
     */
    DataProviderBuilder addBoolean(boolean isNullable) {
        final List<List<Object>> newArgs = new ArrayList<>(args.size() * (isNullable ? 3 : 2));
        for (final List<Object> arg : args) {
            List<Object> newArg;
            newArg = new ArrayList<>(arg.size() + 1);
            newArg.addAll(arg);
            newArg.add(Boolean.FALSE);
            newArgs.add(newArg);
            newArg = new ArrayList<>(arg.size() + 1);
            newArg.addAll(arg);
            newArg.add(Boolean.TRUE);
            newArgs.add(newArg);
            if (isNullable) {
                newArg = new ArrayList<>(arg.size() + 1);
                newArg.addAll(arg);
                newArg.add(null);
                newArgs.add(newArg);
            }
        }
        args = newArgs;
        return this;
    }

    /**
     * @return the return value of a {@link DataProvider} that provides the arguments noted by this builder
     */
    Object[][] build() {
        final Object[][] argsArray = new Object[args.size()][];
        int index = 0;
        for (final List<Object> arg : args) {
            argsArray[index++] = arg.toArray();
        }
        return argsArray;
    }
}
