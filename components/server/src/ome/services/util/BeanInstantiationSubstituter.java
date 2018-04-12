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
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

/**
 * Adjusts instantiation class of Spring beans based on read-only status.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.4.6
 */
public class BeanInstantiationSubstituter extends BeanInstantiationGuard {

    private static final Logger LOGGER = LoggerFactory.getLogger(BeanInstantiationSubstituter.class);

    private final String substituteClass;

    /**
     * Construct a bean instantiation substituter.
     * @param readOnly the read-only status
     * @param targetName the name of a bean that needs to write
     * @param substituteClass the name of the class implementing the read-only variant of the bean
     */
    public BeanInstantiationSubstituter(ReadOnlyStatus readOnly, String targetName, String substituteClass) {
        super(readOnly, targetName);
        this.substituteClass = substituteClass;
    }

    @Override
    protected void setBeanDefinitionForReadOnly(BeanDefinitionRegistry registry) {
        LOGGER.info("in read-only state so setting Spring bean named {} to instantiate {}", targetName, substituteClass);
        registry.getBeanDefinition(targetName).setBeanClassName(substituteClass);
    }
}
