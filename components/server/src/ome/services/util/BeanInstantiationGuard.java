/*
 * Copyright (C) 2018 University of Dundee & Open Microscopy Environment.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

/**
 * Prevents creation of Spring beans based on read-only status.
 * @author m.t.b.carroll@dundee.ac.uk
 */
public class BeanInstantiationGuard implements BeanFactoryPostProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(BeanInstantiationGuard.class);

    protected final String targetName;

    private final ReadOnlyStatus readOnly;

    private boolean isWriteDb = false;

    private boolean isWriteRepo = false;

    /**
     * Construct a bean instantiation guard.
     * @param readOnly the read-only status
     * @param targetName the name of a bean that needs to write
     */
    public BeanInstantiationGuard(ReadOnlyStatus readOnly, String targetName) {
        this.readOnly = readOnly;
        this.targetName = targetName;
    }

    /**
     * @param isWriteDb if the target bean needs to write to the database, defaults to {@code false}
     */
    public void setIsWriteDb(boolean isWriteDb) {
        this.isWriteDb = isWriteDb;
    }

    /**
     * @param isWriteRepo if the target bean needs to write to the repository, defaults to {@code false}
     */
    public void setIsWriteRepo(boolean isWriteRepo) {
        this.isWriteRepo = isWriteRepo;
    }

    /**
     * @return if the changes for read-only should be made
     */
    private boolean isTriggerConditionMet() {
        return isWriteDb && readOnly.isReadOnlyDb() ||
               isWriteRepo && readOnly.isReadOnlyRepo();
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory factory) {
        if (isTriggerConditionMet()) {
            setBeanDefinitionForReadOnly((BeanDefinitionRegistry) factory);
        }
    }

    /**
     * Act on the bean definition registry to make the target bean suitable for read-only mode.
     * @param registry the bean definition registry
     */
    protected void setBeanDefinitionForReadOnly(BeanDefinitionRegistry registry) {
        LOGGER.info("in read-only state so removing Spring bean named {}", targetName);
        registry.removeBeanDefinition(targetName);
    }
}
