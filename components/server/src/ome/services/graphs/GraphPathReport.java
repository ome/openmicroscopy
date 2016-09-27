/*
 * Copyright (C) 2014-2016 University of Dundee & Open Microscopy Environment.
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
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.hibernate.type.CustomType;
import org.hibernate.type.EnumType;
import org.hibernate.type.ListType;
import org.hibernate.type.MapType;
import org.hibernate.type.Type;
import org.hibernate.usertype.UserType;

import com.google.common.base.Joiner;

import ome.model.units.GenericEnumType;
import ome.model.units.Unit;
import ome.services.scheduler.ThreadPool;
import ome.system.OmeroContext;
import ome.tools.hibernate.ListAsSQLArrayUserType;

/**
 * A standalone tool for producing a summary of the Hibernate object mapping for our Sphinx documentation. One may invoke it with
 * <code>java -cp lib/server/\* `bin/omero config get | awk '{print"-D"$1}'` ome.services.graphs.GraphPathReport EveryObject.txt</code>.
 * If not using {@code |} prefixes then one may transform the output via {@code fold -sw72}.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.1.0
 *
 */
public class GraphPathReport {

    private static GraphPathBean model;
    private static Writer out;

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
        return "OMERO model class " + className;
    }

    /**
     * @param className a class name
     * @param propertyName a property name
     * @return a Sphinx label for that class property
     */
    @SuppressWarnings("unused")
    private static String labelFor(String className, String propertyName) {
        return "OMERO model property " + className + '.' + propertyName;
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
     * @param className the name of an OMERO model Java class
     * @return a Sphinx link to that class' documentation
     */
    private static String linkToJavadoc(String className) {
        final StringBuffer sb = new StringBuffer();
        sb.append(":javadoc:");
        sb.append('`');
        sb.append(getSimpleName(className));
        sb.append(' ');
        sb.append('<');
        sb.append(className.replace('.', '/'));
        sb.append(".html");
        sb.append('>');
        sb.append('`');
        return sb.toString();
    }

    /**
     * Find a Sphinx representation for a mapped property value type.
     * @param type a Hibernate type
     * @return a reportable representation of that type
     */
    private static String reportType(String className, String propertyName) {
        final Type type = model.getPropertyType(className, propertyName);
        final UserType userType;
        if (type instanceof CustomType) {
            userType = ((CustomType) type).getUserType();
        } else {
            userType = null;
        }
        if (type instanceof EnumType) {
            return "enumeration";
        } else if (userType instanceof GenericEnumType) {
            @SuppressWarnings({"rawtypes", "unchecked"})
            final Class<? extends Unit> unitQuantityClass = ((GenericEnumType) userType).getQuantityClass();
            return "enumeration of " + linkToJavadoc(unitQuantityClass.getName());
        } else if (type instanceof ListType || userType instanceof ListAsSQLArrayUserType) {
            return "list";
        } else if (type instanceof MapType) {
            return "map";
        } else {
            return "``" + type.getName() + "``";
        }
    }

    /**
     * Process the Hibernate domain object model and write a report of the mapped objects.
     * @throws IOException if there was a problem in writing to the output file
     */
    private static void report() throws IOException {
        /* the information is gathered, now write the report */
        out.write("Glossary of all OMERO Model Objects\n");
        out.write("===================================\n\n");
        out.write("Overview\n");
        out.write("--------\n\n");
        out.write(".. include:: EveryObjectOverview.inc\n\n");
        out.write("Reference\n");
        out.write("---------\n\n");
        final SortedMap<String, String> classNames = new TreeMap<String, String>();
        for (final String className : model.getAllClasses()) {
            classNames.put(getSimpleName(className), className);
        }
        for (final Map.Entry<String, String> classNamePair : classNames.entrySet()) {
            final String simpleName = classNamePair.getKey();
            final String className = classNamePair.getValue();
            /* label the class heading */
            out.write(".. _" + labelFor(simpleName) + ":\n\n");
            out.write(simpleName + "\n");
            final char[] underline = new char[simpleName.length()];
            for (int i = 0; i < underline.length; i++) {
                underline[i] = '"';
            }
            out.write(underline);
            out.write("\n\n");
            /* note the class' relationships */
            final SortedSet<String> superclassOf = new TreeSet<String>();
            for (final String subclass : model.getDirectSubclassesOf(className)) {
                superclassOf.add(linkTo(getSimpleName(subclass)));
            }
            final SortedSet<String> linkerText = new TreeSet<String>();
            for (final Map.Entry<String, String> linker : model.getLinkedBy(className)) {
                linkerText.add(linkTo(getSimpleName(linker.getKey()), linker.getValue()));
            }
            if (!(superclassOf.isEmpty() && linkerText.isEmpty())) {
                /* write the class' relationships */
                if (!superclassOf.isEmpty()) {
                    out.write("Subclasses: " + Joiner.on(", ").join(superclassOf) + "\n\n");
                }
                if (!linkerText.isEmpty()) {
                    out.write("Used by: " + Joiner.on(", ").join(linkerText) + "\n\n");
                }
            }
            /* write the class' properties */
            out.write("Properties:\n");
            final SortedMap<String, String> declaredBy = new TreeMap<String, String>();
            final Map<String, String> valueText = new HashMap<String, String>();
            for (final String superclassName : model.getSuperclassesOfReflexive(className)) {
                for (final Map.Entry<String, String> classAndPropertyNames : model.getLinkedTo(superclassName)) {
                    final String valueClassName = classAndPropertyNames.getKey();
                    final String propertyName = classAndPropertyNames.getValue();
                    declaredBy.put(propertyName, superclassName);
                    valueText.put(propertyName, linkTo(getSimpleName(valueClassName)));
                }
                for (final String propertyName : model.getSimpleProperties(superclassName, true)) {
                    declaredBy.put(propertyName, superclassName);
                    valueText.put(propertyName, reportType(superclassName, propertyName));
                }
            }
            for (final Map.Entry<String, String> propertyAndDeclarerNames : declaredBy.entrySet()) {
                final String propertyName = propertyAndDeclarerNames.getKey();
                final String declarerName = propertyAndDeclarerNames.getValue();
                out.write("  | " + propertyName + ": " + valueText.get(propertyName));
                switch (model.getPropertyKind(declarerName, propertyName)) {
                case OPTIONAL:
                    out.write(" (optional)");
                    break;
                case REQUIRED:
                    break;
                case COLLECTION:
                    out.write(" (multiple)");
                    break;
                }
                if (!declarerName.equals(className)) {
                    out.write(" from " + linkTo(getSimpleName(declarerName)));
                } else {
                    final String interfaceName = model.getInterfaceImplemented(className, propertyName);
                    if (interfaceName != null) {
                        out.write(", see " + linkToJavadoc(interfaceName));
                    }
                }
                out.write("\n");
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
        model = context.getBean("graphPathBean", GraphPathBean.class);
        report();
        out.close();
        context.getBean("threadPool", ThreadPool.class).getExecutor().shutdown();
        context.closeAll();
    }
}
