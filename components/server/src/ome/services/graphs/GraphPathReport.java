/*
 * Copyright (C) 2014-2015 University of Dundee & Open Microscopy Environment.
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

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.type.AssociationType;
import org.hibernate.type.CollectionType;
import org.hibernate.type.ComponentType;
import org.hibernate.type.CustomType;
import org.hibernate.type.EnumType;
import org.hibernate.type.ListType;
import org.hibernate.type.MapType;
import org.hibernate.type.Type;
import org.hibernate.usertype.UserType;
import org.springframework.beans.BeanUtils;

import com.google.common.base.Joiner;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.SortedSetMultimap;
import com.google.common.collect.TreeMultimap;

import ome.model.IObject;
import ome.model.units.GenericEnumType;
import ome.services.graphs.GraphPathBean.PropertyDetails;
import ome.services.scheduler.ThreadPool;
import ome.system.OmeroContext;
import ome.tools.hibernate.ListAsSQLArrayUserType;

/**
 * A standalone tool for producing a summary of the Hibernate object mapping for our Sphinx documentation. One may invoke it with
 * <code>java -cp lib/server/\* `bin/omero config get | awk '{print"-D"$1}'` ome.services.graphs.GraphPathReport EveryObject.txt</code>.
 * Comments in code indicate different formatting possibilities for the output.
 * If not using {@code |} prefixes then one may transform the output via {@code fold -sw72}.
 * This class is heavily based on the {@link GraphPathBean}.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.1.0
 *
 */
public class GraphPathReport {

    // TODO: Add enough information to the GraphPathBean that this class may use that instead of the session factory,
    // and remove code from here that resembles that from the bean.

    private static SessionFactoryImplementor sessionFactory;
    private static Writer out;

    /**
     * If the given property of the given class is actually declared by an interface that it implements,
     * find the name of the interface that first declares the property.
     * @param className the name of an {@link IObject} class
     * @param propertyName the name of a property of the class
     * @return the interface declaring the property, or {@code null} if none
     */
    private static Class<? extends IObject> getInterfaceForProperty(String className, String propertyName) {
        Class<? extends IObject> interfaceForProperty = null;
        Set<Class<? extends IObject>> interfacesFrom, interfacesTo;
        try {
            interfacesFrom = ImmutableSet.<Class<? extends IObject>>of(Class.forName(className).asSubclass(IObject.class));
        } catch (ClassNotFoundException e) {
            /* does not log error as in GraphPathBean */
            System.err.println("error: could not load " + IObject.class.getName() + " subclass " + className);
            return null;
        }
        while (!interfacesFrom.isEmpty()) {
            interfacesTo = new HashSet<Class<? extends IObject>>();
            for (final Class<? extends IObject> interfaceFrom : interfacesFrom) {
                if (interfaceFrom.isInterface() && BeanUtils.getPropertyDescriptor(interfaceFrom, propertyName) != null) {
                    interfaceForProperty = interfaceFrom;
                }
                for (final Class<?> newInterface : interfaceFrom.getInterfaces()) {
                    if (newInterface != IObject.class && IObject.class.isAssignableFrom(newInterface)) {
                        interfacesTo.add(newInterface.asSubclass(IObject.class));
                    }
                }
            }
            interfacesFrom = interfacesTo;
        }
        return interfaceForProperty == null ? null : interfaceForProperty;
    }

    /**
     * Trim the package name off a full class name.
     * @param fullName the full class name
     * @return the class' simple name
     */
    private static String getSimpleName(String fullName) {
        return fullName.substring(1 + fullName.lastIndexOf('.'));
    }

    /**
     * @param className a class name
     * @return a Sphinx label for that class
     */
    private static String labelFor(String className) {
        return Joiner.on('.').join("Hibernate version of class omero.model", className);
    }

    /**
     * @param className a class name
     * @param propertyName a property name
     * @return a Sphinx label for that class property
     */
    private static String labelFor(String className, String propertyName) {
        return Joiner.on('.').join("Hibernate version of property omero.model", className, propertyName);
    }

    /**
     * @param className a class name
     * @return a Sphinx link to that class
     */
    private static String linkTo(String className) {
        final StringBuffer sb = new StringBuffer();
        sb.append(":ref:");
        sb.append('`');
        sb.append(className);
        sb.append(' ');
        sb.append('<');
        sb.append(labelFor(className));
        sb.append('>');
        sb.append('`');
        return sb.toString();
    }

    /**
     * @param className a class name
     * @param propertyName a property name
     * @return a Sphinx link to that class property
     */
    private static String linkTo(String className, String propertyName) {
        final StringBuffer sb = new StringBuffer();
        sb.append(":ref:");
        sb.append('`');
        sb.append(className);
        sb.append('.');
        sb.append(propertyName);
        sb.append(' ');
        sb.append('<');
        sb.append(labelFor(className/*, propertyName*/));
        sb.append('>');
        sb.append('`');
        return sb.toString();
    }

    /**
     * @param interfaceName the name of an OMERO model Java interface
     * @return a Sphinx link to that interface's source code
     */
    private static String interfaceSource(String interfaceName) {
        final StringBuffer sb = new StringBuffer();
        sb.append(":source:");
        sb.append('`');
        sb.append(getSimpleName(interfaceName));
        sb.append(' ');
        sb.append('<');
        sb.append("components/model/src/");
        sb.append(interfaceName.replace('.', '/'));
        sb.append(".java");
        sb.append('>');
        sb.append('`');
        return sb.toString();
    }

    /**
     * @param name the name of an object property
     * @return if the property should be ignored
     */
    private static boolean ignoreProperty(String name) {
        return name.startsWith("_") || name.endsWith("CountPerOwner");
    }

    /**
     * Process the Hibernate domain object model and write a report of the mapped objects.
     * @throws IOException if there was a problem in writing to the output file
     */
    private static void report() throws IOException {
        /* note all the direct superclasses and subclasses */
        final Map<String, String> superclasses = new HashMap<String, String>();
        final SortedSetMultimap<String, String> subclasses = TreeMultimap.create();
        @SuppressWarnings("unchecked")
        final Map<String, ClassMetadata> classesMetadata = sessionFactory.getAllClassMetadata();
        for (final String className : classesMetadata.keySet()) {
            try {
                final Class<?> actualClass = Class.forName(className);
                if (IObject.class.isAssignableFrom(actualClass)) {
                    @SuppressWarnings("unchecked")
                    final Set<String> subclassNames =
                            sessionFactory.getEntityPersister(className).getEntityMetamodel().getSubclassEntityNames();
                    for (final String subclassName : subclassNames) {
                        if (!subclassName.equals(className)) {
                            final Class<?> actualSubclass = Class.forName(subclassName);
                            if (actualSubclass.getSuperclass() == actualClass) {
                                superclasses.put(subclassName, className);
                                subclasses.put(getSimpleName(className), getSimpleName(subclassName));
                            }
                        }
                    }
                } else {
                    System.err.println("error: mapped class " + className + " is not a " + IObject.class.getName());
                }
            } catch (ClassNotFoundException e) {
                System.err.println("error: could not instantiate class: " + e);
            }
        }
        /* queue for processing all the properties of all the mapped entities: name, type, nullability */
        final Queue<PropertyDetails> propertyQueue = new LinkedList<PropertyDetails>();
        final Map<String, Set<String>> allPropertyNames = new HashMap<String, Set<String>>();
        for (final Map.Entry<String, ClassMetadata> classMetadata : classesMetadata.entrySet()) {
            final String className = classMetadata.getKey();
            final ClassMetadata metadata = classMetadata.getValue();
            final String[] propertyNames = metadata.getPropertyNames();
            final Type[] propertyTypes = metadata.getPropertyTypes();
            final boolean[] propertyNullabilities = metadata.getPropertyNullability();
            for (int i = 0; i < propertyNames.length; i++) {
                if (!ignoreProperty(propertyNames[i])) {
                    final List<String> propertyPath = Collections.singletonList(propertyNames[i]);
                    propertyQueue.add(new PropertyDetails(className, propertyPath, propertyTypes[i], propertyNullabilities[i]));
                }
            }
            final Set<String> propertyNamesSet = new HashSet<String>(propertyNames.length);
            propertyNamesSet.addAll(Arrays.asList(propertyNames));
            allPropertyNames.put(className, propertyNamesSet);
        }
        /* for linkedBy, X -> Y, Z: class X is linked to by class Y with Y's property Z */
        final SetMultimap<String, Map.Entry<String, String>> linkedBy = HashMultimap.create();
        final SetMultimap<String, String> linkers = HashMultimap.create();
        final SortedMap<String, SortedMap<String, String>> classPropertyReports = new TreeMap<String, SortedMap<String, String>>();
        /* process each property to note entity linkages */
        while (!propertyQueue.isEmpty()) {
            final PropertyDetails property = propertyQueue.remove();
            /* if the property has a component type, queue the parts for processing */
            if (property.type instanceof ComponentType) {
                final ComponentType componentType = (ComponentType) property.type;
                final String[] componentPropertyNames = componentType.getPropertyNames();
                final Type[] componentPropertyTypes = componentType.getSubtypes();
                final boolean[] componentPropertyNullabilities = componentType.getPropertyNullability();
                for (int i = 0; i < componentPropertyNames.length; i++) {
                    if (!ignoreProperty(componentPropertyNames[i])) {
                        final List<String> componentPropertyPath = new ArrayList<String>(property.path.size() + 1);
                        componentPropertyPath.addAll(property.path);
                        componentPropertyPath.add(componentPropertyNames[i]);
                        propertyQueue.add(new PropertyDetails(property.holder, componentPropertyPath, componentPropertyTypes[i],
                                componentPropertyNullabilities[i]));
                    }
                }
            } else {
                /* determine if this property links to another entity */
                final boolean isAssociatedEntity;
                if (property.type instanceof CollectionType) {
                    final CollectionType ct = (CollectionType) property.type;
                    isAssociatedEntity = sessionFactory.getCollectionPersister(ct.getRole()).getElementType().isEntityType();
                } else {
                    isAssociatedEntity = property.type instanceof AssociationType;
                }
                /* determine the class and property name for reporting */
                final String holderSimpleName = getSimpleName(property.holder);
                final String propertyPath = Joiner.on('.').join(property.path);
                /* build a report line for this property */
                final StringBuffer sb = new StringBuffer();
                final String valueClassName;
                if (isAssociatedEntity) {
                    /* entity linkages by non-inherited properties are recorded */
                    final String valueName = ((AssociationType) property.type).getAssociatedEntityName(sessionFactory);
                    final String valueSimpleName = getSimpleName(valueName);
                    final Map.Entry<String, String> classPropertyName = Maps.immutableEntry(holderSimpleName, propertyPath);
                    linkers.put(holderSimpleName, propertyPath);
                    linkedBy.put(valueSimpleName, classPropertyName);
                    valueClassName = linkTo(valueSimpleName);
                } else {
                    /* find a Sphinx representation for this property value type */
                    final UserType userType;
                    if (property.type instanceof CustomType) {
                        userType = ((CustomType) property.type).getUserType();
                    } else {
                        userType = null;
                    }
                    if (property.type instanceof EnumType || userType instanceof GenericEnumType) {
                        valueClassName = "enumeration";
                    } else if (property.type instanceof ListType || userType instanceof ListAsSQLArrayUserType) {
                        valueClassName = "list";
                    } else if (property.type instanceof MapType) {
                        valueClassName = "map";
                    } else {
                        valueClassName = "``" + property.type.getName() + "``";
                    }
                }
                sb.append(valueClassName);
                if (property.type.isCollectionType()) {
                    sb.append(" (multiple)");
                } else if (property.isNullable) {
                    sb.append(" (optional)");
                }
                /* determine from which class the property is inherited, if at all */
                String superclassWithProperty = null;
                String currentClass = property.holder;
                while (true) {
                    currentClass = superclasses.get(currentClass);
                    if (currentClass == null) {
                        break;
                    } else if (allPropertyNames.get(currentClass).contains(property.path.get(0))) {
                        superclassWithProperty = currentClass;
                    }
                }
                /* check if the property actually comes from an interface */
                final String declaringClassName = superclassWithProperty == null ? property.holder : superclassWithProperty;
                final Class<? extends IObject> interfaceForProperty =
                        getInterfaceForProperty(declaringClassName, property.path.get(0));
                /* report where the property is declared */
                if (superclassWithProperty != null) {
                    sb.append(" from ");
                    sb.append(linkTo(getSimpleName(superclassWithProperty)));
                } else {
                    if (interfaceForProperty != null) {
                        sb.append(", see ");
                        sb.append(interfaceSource(interfaceForProperty.getName()));
                    }
                }
                SortedMap<String, String> byProperty = classPropertyReports.get(holderSimpleName);
                if (byProperty == null) {
                    byProperty = new TreeMap<String, String>();
                    classPropertyReports.put(holderSimpleName, byProperty);
                }
                byProperty.put(propertyPath, sb.toString());
            }
        }
        /* the information is gathered, now write the report */
        out.write("Every OMERO model object\n");
        out.write("========================\n\n");
        out.write("Overview\n");
        out.write("--------\n\n");
        out.write("Reference\n");
        out.write("---------\n\n");
        for (final Map.Entry<String, SortedMap<String, String>> byClass : classPropertyReports.entrySet()) {
            /* label the class heading */
            final String className = byClass.getKey();
            out.write(".. _" + labelFor(className) + ":\n\n");
            out.write(className + "\n");
            final char[] underline = new char[className.length()];
            for (int i = 0; i < underline.length; i++) {
                underline[i] = '"';
            }
            out.write(underline);
            out.write("\n\n");
            /* note the class' relationships */
            final SortedSet<String> superclassOf = new TreeSet<String>();
            for (final String subclass : subclasses.get(className)) {
                superclassOf.add(linkTo(subclass));
            }
            final SortedSet<String> linkerText = new TreeSet<String>();
            for (final Map.Entry<String, String> linker : linkedBy.get(className)) {
                linkerText.add(linkTo(linker.getKey(), linker.getValue()));
            }
            if (!(superclassOf.isEmpty() && linkerText.isEmpty())) {
                /* write the class' relationships */
                /*
                out.write("Relationships\n");
                out.write("^^^^^^^^^^^^^\n\n");
                */
                if (!superclassOf.isEmpty()) {
                    out.write("Subclasses: " + Joiner.on(", ").join(superclassOf) + "\n\n");
                }
                if (!linkerText.isEmpty()) {
                    out.write("Used by: " + Joiner.on(", ").join(linkerText) + "\n\n");
                }
            }
            /* write the class' properties */
            /*
            out.write("Properties\n");
            out.write("^^^^^^^^^^\n\n");
            */
            out.write("Properties:\n");
            for (final Map.Entry<String, String> byProperty : byClass.getValue().entrySet()) {
                final String propertyName = byProperty.getKey();
                // if (linkers.containsEntry(className, propertyName)) {
                //     /* label properties that have other entities as values */
                //     out.write(".. _" + labelFor(className, propertyName) + ":\n\n");
                // }
                out.write("  | " + propertyName + ": " + byProperty.getValue() + "\n" /* \n */);
            }
            out.write("\n");
        }
    }

    /**
     * Generate a Sphinx report of OMERO Hibernate entities.
     * @param argv the output filename
     * @throws IOException if the report cannot be written to the file
     */
    public static void main(String[] argv) throws IOException {
        if (argv.length != 1) {
            System.err.println("must give output filename as single argument");
            System.exit(1);
        }
        out = new FileWriter(argv[0]);
        final OmeroContext context = OmeroContext.getManagedServerContext();
        sessionFactory = context.getBean("sessionFactory", SessionFactoryImplementor.class);
        report();
        out.close();
        context.getBean("threadPool", ThreadPool.class).getExecutor().shutdown();
        context.closeAll();
    }
}
