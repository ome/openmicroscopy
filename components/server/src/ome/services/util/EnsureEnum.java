/*
 * Copyright (C) 2016-2018 University of Dundee & Open Microscopy Environment.
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

package ome.services.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import ome.model.IEnum;
import ome.model.IGlobal;
import ome.system.Login;
import ome.system.Principal;
import ome.system.Roles;
import ome.system.ServiceFactory;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.google.common.collect.ImmutableMap;

/**
 * Utility bean for ensuring that enumeration values do exist in the database.
 * @author m.t.b.carroll@dundee.ac.uk
 */
public class EnsureEnum {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnsureEnum.class);

    private final Executor executor;
    private final Principal principal;
    private final Map<String, String> callContext;
    private final boolean isReadOnlyDb;

    /**
     * Construct a new enumeration ensurer. Expected to be instantiated via Spring.
     * @param executor the internal task executor
     * @param uuid a UUID suitable for constructing a privileged principal
     * @param roles information about the system roles
     * @param readOnly the read-only status
     */
    public EnsureEnum(Executor executor, String uuid, Roles roles, ReadOnlyStatus readOnly) {
        this.executor = executor;
        this.principal = new Principal(uuid, roles.getSystemGroupName(), "Internal");
        this.callContext = ImmutableMap.of(Login.OMERO_GROUP, Long.toString(roles.getUserGroupId()));
        isReadOnlyDb = readOnly.isReadOnlyDb();
    }

    /**
     * Ensure that the given enumeration exists.
     * @param session the Hibernate session for accessing the current enumerations
     * @param enumClass the model class of the enumeration
     * @param enumValue the name of the enumeration (case-sensitive)
     * @return the ID of the enumeration, or {@code null} if it did not exist and could not be created
     */
    private static <E extends IEnum & IGlobal> Long ensure(Session session, Class<E> enumClass, String enumValue) {
        IEnum instance = (IEnum) session.createCriteria(enumClass).add(Restrictions.eq("value", enumValue)).uniqueResult();
        if (instance != null) {
            return instance.getId();
        }
        final String prettyEnum = enumClass.getSimpleName() + '.' + enumValue;
        try {
            instance = enumClass.getConstructor(String.class).newInstance(enumValue);
        } catch (IllegalArgumentException | ReflectiveOperationException | SecurityException e) {
            LOGGER.error("failed to create enumeration value " + prettyEnum, e);
            return null;
        }
        if (TransactionSynchronizationManager.isCurrentTransactionReadOnly()) {
            LOGGER.warn("no enumeration value {} but database is read-only", prettyEnum);
            return null;
        } else {
            LOGGER.info("adding to database new enumeration value " + prettyEnum);
            return (Long) session.save(instance);
        }
    }

    /**
     * Ensure that the given enumerations exist.
     * @param enumClass the model class of the enumeration
     * @param enumValues the names of the enumerations (case-sensitive)
     * @return the IDs of the enumerations, with {@code null} for any that did not exist and could not be created
     */
    @SuppressWarnings("unchecked")
    public <E extends IEnum & IGlobal> List<Long> ensure(final Class<E> enumClass, final Collection<String> enumValues) {
        if (enumValues.isEmpty()) {
            return Collections.emptyList();
        }
        return isReadOnlyDb ? (List<Long>) executor.execute(callContext, principal, new FetchEnums<E>() {
            @Override
            public String description() {
                return "check enum values (ro)";
            }

            @Override
            @Transactional(readOnly = true)
            public List<Long> doWork(Session session, ServiceFactory sf) {
                return innerWork(session, enumClass, enumValues);
            }
        })
        : (List<Long>) executor.execute(callContext, principal, new FetchEnums<E>() {
            @Override
            public String description() {
                return "ensure enum values (rw)";
            }

            @Override
            @Transactional(readOnly = false)
            public List<Long> doWork(Session session, ServiceFactory sf) {
                return innerWork(session, enumClass, enumValues);
            }
        });
    }

    /**
     * Base class for workers that ensure that enumeration values exist.
     * Perhaps could be refactored away in Java 8.
     * @author m.t.b.carroll@dundee.ac.uk
     * @since 5.4.6
     */
    private static abstract class FetchEnums<E extends IEnum & IGlobal> implements Executor.Work<List<Long>> {

        /**
         * Ensure that the given enumerations exist.
         * @param session the Hibernate session for accessing the current enumerations
         * @param enumClass the model class of the enumeration
         * @param enumValues the names of the enumerations (case-sensitive)
         * @return the IDs of the enumerations, with {@code null} for any that did not exist and could not be created
         */
        protected List<Long> innerWork(Session session, Class<E> enumClass, Collection<String> enumValues) {
            final List<Long> enumIds = new ArrayList<Long>(enumValues.size());
            for (final String enumValue : enumValues) {
                enumIds.add(ensure(session, enumClass, enumValue));
            }
            return enumIds;
        }
    }
}
