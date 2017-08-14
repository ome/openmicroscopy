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

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.beanutils.NestedNullException;
import org.apache.commons.beanutils.PropertyUtils;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.type.AssociationType;
import org.hibernate.type.CollectionType;
import org.hibernate.type.ComponentType;
import org.hibernate.type.Type;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;

import com.google.common.base.Joiner;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;

import ome.model.IObject;
import ome.tools.spring.OnContextRefreshedEventListener;

/**
 * The graph path bean holds the Hibernate object mapping and assists navigation thereof.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.1.0
 */
public class GraphPathBean extends OnContextRefreshedEventListener {

    private static final Logger log = LoggerFactory.getLogger(GraphPathBean.class);

    /**
     * How entities may be <q>unlinked</q> from the value of a property.
     * @author m.t.b.carroll@dundee.ac.uk
     * @since 5.1.0
     */
    static enum PropertyKind { OPTIONAL, REQUIRED, COLLECTION };

    /**
     * A tuple used in initialization to note mapping object properties for processing.
     * @author m.t.b.carroll@dundee.ac.uk
     * @since 5.1.0
     */
    private static class PropertyDetails {
        final String holder;
        final List<String> path;
        final Type type;
        final boolean isNullable;

        /**
         * Construct a new property details tuple instance.
         * @param className the name of the class holding the property
         * @param path the HQL path by which the property is referenced
         * @param type the Hibernate type of the property
         * @param isNullable if the property is nullable
         */
        PropertyDetails(String className, List<String> path, Type type, boolean isNullable) {
            this.holder = className;
            this.path = path;
            this.type = type;
            this.isNullable = isNullable;
        }
    }

    /* classes indexed by their simple name */
    private final Map<String, Class<? extends IObject>> classesBySimpleName = new HashMap<String, Class<? extends IObject>>();

    /* only direct superclasses of mapped entities */
    private final Map<String, String> superclasses = new HashMap<String, String>();

    /* only direct subclasses of mapped entities */
    private final SetMultimap<String, String> subclasses = HashMultimap.create();

    /* direct and indirect superclasses of mapped entities */
    private final SetMultimap<String, String> allSuperclasses = HashMultimap.create();

    /* direct and indirect subclasses of mapped entities */
    private final SetMultimap<String, String> allSubclasses = HashMultimap.create();

    /* X -> Y, Z: class X links to class Y with X's property Z */
    private final SetMultimap<String, Entry<String, String>> linkedTo = HashMultimap.create();

    /* X -> Y, Z: class X is linked to by class Y with Y's property Z */
    private final SetMultimap<String, Entry<String, String>> linkedBy = HashMultimap.create();

    /* how nullable properties are */
    private final HashMap<Entry<String, String>, PropertyKind> propertyKinds = new HashMap<Entry<String, String>, PropertyKind>();

    /* the Hibernate type of each property */
    private final Map<Entry<String, String>, Type> propertyTypes = new HashMap<Entry<String, String>, Type>();

    /* the declaring interface of each property */
    private final Map<Entry<String, String>, String> implementedInterfaces = new HashMap<Entry<String, String>, String>();

    /* which properties are accessible */
    private final Set<Entry<String, String>> accessibleProperties = new HashSet<Entry<String, String>>();

    /* the identifier properties of classes */
    private final Map<String, String> classIdProperties = new HashMap<String, String>();

    /* the properties of classes that have simple values, recording full nesting */
    private final SetMultimap<String, String> simplePropertiesNested = HashMultimap.create();

    /* the properties of classes that have simple values, with only the top-level name */
    private final SetMultimap<String, String> simplePropertiesDirect = HashMultimap.create();

    /**
     * The application context after refresh should contain a usable Hibernate session factory.
     * If not already done, process the Hibernate domain object model from that bean.
     * @param event the context refreshed event bearing the new application context
     */
    @Override
    public void handleContextRefreshedEvent(ContextRefreshedEvent event) {
        if (propertyKinds.isEmpty()) {
            final ApplicationContext context = event.getApplicationContext();
            final SessionFactoryImplementor sessionFactory = context.getBean("sessionFactory", SessionFactoryImplementor.class);
            initialize(sessionFactory);
        }
    }

    /**
     * If the given property of the given class is actually declared by an interface that it implements,
     * find the name of the interface that first declares the property.
     * @param className the name of an {@link IObject} class
     * @param propertyName the name of a property of the class
     * @return the interface declaring the property, or {@code null} if none
     */
    private Class<? extends IObject> getInterfaceForProperty(String className, String propertyName) {
        Class<? extends IObject> interfaceForProperty = null;
        Set<Class<? extends IObject>> interfacesFrom, interfacesTo;
        try {
            interfacesFrom = ImmutableSet.<Class<? extends IObject>>of(Class.forName(className).asSubclass(IObject.class));
        } catch (ClassNotFoundException e) {
            log.error("could not load " + IObject.class.getName() + " subclass " + className);
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
                        classesBySimpleName.put(newInterface.getSimpleName(), newInterface.asSubclass(IObject.class));
                    }
                }
            }
            interfacesFrom = interfacesTo;
        }
        return interfaceForProperty == null ? null : interfaceForProperty;
    }

    /**
     * @param name the name of an object property
     * @return if the property should be ignored
     */
    private static boolean ignoreProperty(String name) {
        return "perm1".equals(name) || name.startsWith("_") || name.endsWith("CountPerOwner");
    }

    /**
     * Process the Hibernate domain object model to initialize this class' instance fields.
     * No other method should write to them.
     * @param sessionFactory the Hibernate session factory
     */
    private void initialize(SessionFactoryImplementor sessionFactory) {
        /* note all the direct superclasses */
        final Map<String, ClassMetadata> classesMetadata = sessionFactory.getAllClassMetadata();
        for (final String className : classesMetadata.keySet()) {
            try {
                final Class<?> actualClass = Class.forName(className);
                if (IObject.class.isAssignableFrom(actualClass)) {
                    classesBySimpleName.put(actualClass.getSimpleName(), actualClass.asSubclass(IObject.class));
                    final Set<String> subclassNames =
                            sessionFactory.getEntityPersister(className).getEntityMetamodel().getSubclassEntityNames();
                    for (final String subclassName : subclassNames) {
                        if (!subclassName.equals(className)) {
                            final Class<?> actualSubclass = Class.forName(subclassName);
                            if (actualSubclass.getSuperclass() == actualClass) {
                                superclasses.put(subclassName, className);
                                subclasses.put(className, subclassName);
                            }
                        }
                    }
                } else {
                    log.warn("mapped class " + className + " is not a " + IObject.class.getName());
                }
            } catch (ClassNotFoundException e) {
                log.error("could not instantiate class", e);
            }
        }
        /* note the indirect superclasses and subclasses */
        for (final Entry<String, String> superclassRelationship : superclasses.entrySet()) {
            final String startClass = superclassRelationship.getKey();
            String superclass = superclassRelationship.getValue();
            while (superclass != null) {
                allSuperclasses.put(startClass, superclass);
                allSubclasses.put(superclass, startClass);
                superclass = superclasses.get(superclass);
            }
        }
        /* queue for processing all the properties of all the mapped entities: name, type, nullability */
        final Queue<PropertyDetails> propertyQueue = new LinkedList<PropertyDetails>();
        final Map<String, Set<String>> allPropertyNames = new HashMap<String, Set<String>>();
        for (final Entry<String, ClassMetadata> classMetadata : classesMetadata.entrySet()) {
            final String className = classMetadata.getKey();
            final ClassMetadata metadata = classMetadata.getValue();
            /* note name of identifier property */
            classIdProperties.put(metadata.getEntityName(), metadata.getIdentifierPropertyName());
            /* queue other properties */
            final String[] propertyNames = metadata.getPropertyNames();
            final Type[] propertyTypes = metadata.getPropertyTypes();
            final boolean[] propertyNullabilities = metadata.getPropertyNullability();
            for (int i = 0; i < propertyNames.length; i++) {
                final List<String> propertyPath = Collections.singletonList(propertyNames[i]);
                propertyQueue.add(new PropertyDetails(className, propertyPath, propertyTypes[i], propertyNullabilities[i]));
            }
            final Set<String> propertyNamesSet = new HashSet<String>(propertyNames.length);
            propertyNamesSet.addAll(Arrays.asList(propertyNames));
            allPropertyNames.put(className, propertyNamesSet);
        }
        /* process each property to note entity linkages */
        while (!propertyQueue.isEmpty()) {
            final PropertyDetails property = propertyQueue.remove();
            if (ignoreProperty(property.path.get(property.path.size() - 1))) {
                continue;
            }
            /* if the property has a component type, queue the parts for processing */
            if (property.type instanceof ComponentType) {
                final ComponentType componentType = (ComponentType) property.type;
                final String[] componentPropertyNames = componentType.getPropertyNames();
                final Type[] componentPropertyTypes = componentType.getSubtypes();
                final boolean[] componentPropertyNullabilities = componentType.getPropertyNullability();
                for (int i = 0; i < componentPropertyNames.length; i++) {
                    final List<String> componentPropertyPath = new ArrayList<String>(property.path.size() + 1);
                    componentPropertyPath.addAll(property.path);
                    componentPropertyPath.add(componentPropertyNames[i]);
                    propertyQueue.add(new PropertyDetails(
                            property.holder, componentPropertyPath, componentPropertyTypes[i], componentPropertyNullabilities[i]));
                }
            } else {
                /* determine if another mapped entity class is linked by this property */
                final boolean isAssociatedEntity;
                if (property.type instanceof CollectionType) {
                    final CollectionType ct = (CollectionType) property.type;
                    isAssociatedEntity = sessionFactory.getCollectionPersister(ct.getRole()).getElementType().isEntityType();
                } else {
                    isAssociatedEntity = property.type instanceof AssociationType;
                }
                /* the property can link to entities, so process it further */
                final String fullPropertyPath = Joiner.on('.').join(property.path);
                /* find if the property is accessible (e.g., not protected) */
                boolean propertyIsAccessible = false;
                String classToInstantiateName = property.holder;
                Class<?> classToInstantiate = null;
                try {
                    classToInstantiate = Class.forName(classToInstantiateName);
                    while (Modifier.isAbstract(classToInstantiate.getModifiers())) {
                        classToInstantiateName = allSubclasses.get(classToInstantiateName).iterator().next();
                        classToInstantiate = Class.forName(classToInstantiateName);
                    }
                    try     {
                        PropertyUtils.getNestedProperty(classToInstantiate.newInstance(), fullPropertyPath);
                        propertyIsAccessible = true;
                    } catch (NoSuchMethodException e) {
                        /* expected for collection properties */
                    } catch (NestedNullException e) {
                        log.debug("guessing " + fullPropertyPath + " of " + property.holder + " to be accessible");
                        propertyIsAccessible = true;
                    }
                } catch (ReflectiveOperationException e) {
                    log.error("could not probe property " + fullPropertyPath + " of " + property.holder, e);
                    continue;
                }
                /* build property report line for log */
                final char arrowShaft = property.isNullable ? '-' : '=';
                final StringBuffer sb = new StringBuffer();
                sb.append(property.holder);
                sb.append(' ');
                for (final String propertyName : property.path) {
                    sb.append(arrowShaft);
                    sb.append(arrowShaft);
                    sb.append(propertyName);
                }
                sb.append(arrowShaft);
                sb.append(arrowShaft);
                sb.append("> ");
                final String valueClassName;
                if (isAssociatedEntity) {
                    valueClassName = ((AssociationType) property.type).getAssociatedEntityName(sessionFactory);
                    sb.append(valueClassName);
                } else {
                    valueClassName = null;
                    sb.append("value");
                }
                if (property.type.isCollectionType()) {
                    sb.append("[]");
                }
                if (!propertyIsAccessible) {
                    sb.append(" (inaccessible)");
                }
                /* determine from which class the property is inherited, if at all */
                final String shortPropertyPath = property.path.get(0);
                String superclassWithProperty = null;
                String currentClass = property.holder;
                while (true) {
                    currentClass = superclasses.get(currentClass);
                    if (currentClass == null) {
                        break;
                    } else if (allPropertyNames.get(currentClass).contains(shortPropertyPath)) {
                        superclassWithProperty = currentClass;
                    }
                }
                /* check if the property actually comes from an interface */
                final String declaringClassName = superclassWithProperty == null ? property.holder : superclassWithProperty;
                final Class<? extends IObject> interfaceForProperty =
                        getInterfaceForProperty(declaringClassName, shortPropertyPath);
                /* report where the property is declared */
                if (superclassWithProperty != null) {
                    sb.append(" from ");
                    sb.append(superclassWithProperty);
                } else {
                    final Entry<String, String> classPropertyFullName = Maps.immutableEntry(property.holder, fullPropertyPath);
                    if (interfaceForProperty != null) {
                        sb.append(" see ");
                        final String implementedInterface = interfaceForProperty.getName();
                        sb.append(implementedInterface);
                        implementedInterfaces.put(classPropertyFullName, implementedInterface);
                        /* It would be nice to set PropertyDetails to have the interface as the holder,
                         * but then properties would not be unique by declarer class and instance ID. */
                    }
                    final Entry<String, String> classPropertyShortName = Maps.immutableEntry(property.holder, shortPropertyPath);
                    /* entity linkages by non-inherited properties are recorded */
                    if (valueClassName == null) {
                        simplePropertiesNested.put(property.holder, fullPropertyPath);
                        simplePropertiesDirect.put(property.holder, shortPropertyPath);
                    } else {
                        linkedTo.put(property.holder, Maps.immutableEntry(valueClassName, fullPropertyPath));
                        linkedBy.put(valueClassName, classPropertyFullName);
                    }
                    final PropertyKind propertyKind;
                    if (property.type.isCollectionType()) {
                        propertyKind = PropertyKind.COLLECTION;
                    } else if (property.isNullable) {
                        propertyKind = PropertyKind.OPTIONAL;
                    } else {
                        propertyKind = PropertyKind.REQUIRED;
                    }
                    propertyKinds.put(classPropertyFullName, propertyKind);
                    if (propertyIsAccessible) {
                        accessibleProperties.add(classPropertyFullName);
                    }
                    if (valueClassName == null && !fullPropertyPath.equals(shortPropertyPath)) {
                        /* note simple properties also as non-nested */
                        propertyKinds.put(classPropertyShortName, propertyKind);
                        if (propertyIsAccessible) {
                            accessibleProperties.add(classPropertyShortName);
                        }
                    }
                    propertyTypes.put(classPropertyFullName, property.type);
                }
                if (log.isDebugEnabled()) {
                    log.debug(sb.toString());
                }
            }
        }
        log.info("initialized graph path bean with " + propertyKinds.size() + " properties");
    }

    /**
     * @param simpleName the simple name of a mapped IObject class
     * @return the class with that simple name, or {@code null} if one is not known
     */
    public Class<? extends IObject> getClassForSimpleName(String simpleName) {
        Class<? extends IObject> namedClass = classesBySimpleName.get(simpleName);
        if (namedClass != null) {
            return namedClass;
        }
        /* may have named a subclass, try guessing */
        Class<?> subclass;
        try {
            subclass = Class.forName("omero.model." + simpleName);
        } catch (ClassNotFoundException e) {
            return null;
        }
        while (namedClass == null && subclass != Object.class) {
            subclass = subclass.getSuperclass();
            namedClass = classesBySimpleName.get(subclass.getSimpleName());
        }
        return namedClass;
    }

    /**
     * Get all the classes mapped by Hibernate.
     * @return the classes
     */
    public Collection<String> getAllClasses() {
        return classIdProperties.keySet();
    }

    /**
     * Get the direct superclass of the given class, if any.
     * @param className the name of a class
     * @return the class' direct superclass, may be {@code null}
     */
    public String getDirectSuperclassOf(String className) {
        return superclasses.get(className);
    }

    /**
     * Get the superclasses of the given class, if any.
     * @param className the name of a class
     * @return the class' superclasses, never {@code null}
     */
    public Set<String> getSuperclassesOf(String className) {
        return allSuperclasses.get(className);
    }

    /**
     * Get the name of this class and of its mapped superclasses.
     * @param className the name of a class
     * @return the class and its superclasses, never {@code null}
     */
    public Collection<String> getSuperclassesOfReflexive(String className) {
        final Collection<String> superclasses = getSuperclassesOf(className);
        final Collection<String> superclassesReflexive = new ArrayList<String>(superclasses.size() + 1);
        superclassesReflexive.add(className);
        superclassesReflexive.addAll(superclasses);
        return superclassesReflexive;
    }

    /**
     * Get the direct subclasses of the given class, if any.
     * @param className the name of a class
     * @return the class' direct subclasses, never {@code null}
     */
    public Set<String> getDirectSubclassesOf(String className) {
        return subclasses.get(className);
    }

    /**
     * Get the subclasses of the given class, if any.
     * @param className the name of a class
     * @return the class' subclasses, never {@code null}
     */
    public Set<String> getSubclassesOf(String className) {
        return allSubclasses.get(className);
    }

    /**
     * Get the name of this class and of its mapped subclasses.
     * @param className the name of a class
     * @return the class and its subclasses, never {@code null}
     */
    public Collection<String> getSubclassesOfReflexive(String className) {
        final Collection<String> subclasses = getSubclassesOf(className);
        final Collection<String> subclassesReflexive = new ArrayList<String>(subclasses.size() + 1);
        subclassesReflexive.add(className);
        subclassesReflexive.addAll(subclasses);
        return subclassesReflexive;
    }

    /**
     * Get the classes and properties to which the given class links.
     * @param className the name of a class
     * @return the classes to which the given class links, and by which properties; never {@code null}
     */
    public Set<Entry<String, String>> getLinkedTo(String className) {
        return linkedTo.get(className);
    }

    /**
     * Get the classes and properties that link to the given class.
     * @param className the name of a class
     * @return the classes that link to the given class, and by which properties; never {@code null}
     */
    public Set<Entry<String, String>> getLinkedBy(String className) {
        return linkedBy.get(className);
    }

    /**
     * Get what kind of property a specific class property is.
     * @param className the name of a class
     * @param propertyName the name of a property declared, not just inherited, by that class
     * @return the kind of property it is
     */
    public PropertyKind getPropertyKind(String className, String propertyName) {
        return propertyKinds.get(Maps.immutableEntry(className, propertyName));
    }

    /**
     * Get the Hibernate type of a specific class property.
     * @param className the name of a class
     * @param propertyName the name of a property declared, not just inherited, by that class
     * @return the Hibernate type of the property
     */
    public Type getPropertyType(String className, String propertyName) {
        return propertyTypes.get(Maps.immutableEntry(className, propertyName));
    }

    /**
     * Get the interface whose method is implemented by a specific class property, if any.
     * @param className the name of a class
     * @param propertyName the name of a property declared, not just inherited, by that class
     * @return the implemented interface, may be {@code null}
     */
    public String getInterfaceImplemented(String className, String propertyName) {
        return implementedInterfaces.get(Maps.immutableEntry(className, propertyName));
    }

    /**
     * Find if the given property can be accessed.
     * @param className the name of a class
     * @param propertyName the name of a property declared, not just inherited, by that class
     * @return if the property can be accessed
     */
    public boolean isPropertyAccessible(String className, String propertyName) {
        return accessibleProperties.contains(Maps.immutableEntry(className, propertyName));
    }

    /**
     * Get the identifier property for the given class.
     * @param className the name of a class
     * @return the identifier property, or {@code null} if one is not known
     */
    public String getIdentifierProperty(String className) {
        return classIdProperties.get(className);
    }

    /**
     * Get the <q>simple</q> non-nested properties for the given class, not linking to other mapped classes.
     * @param className the name of a class
     * @return the <q>simple</q> properties of the given class; never {@code null}
     * @deprecated instead use {@link #getSimpleProperties(String, boolean)} setting {@code isNested} to {@code false}
     */
    @Deprecated
    public Set<String> getSimpleProperties(String className) {
        return getSimpleProperties(className, false);
    }

    /**
     * Get the <q>simple</q> properties for the given class, not linking to other mapped classes.
     * @param className the name of a class
     * @param isNested if nested properties should have all components given separately with dot-notation
     * @return the <q>simple</q> properties of the given class; never {@code null}
     */
    public Set<String> getSimpleProperties(String className, boolean isNested) {
        return (isNested ? simplePropertiesNested : simplePropertiesDirect).get(className);
    }
}
