/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.graphs;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * post processor which should be included in all Spring context files with
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4.2.3
 * @deprecated will be removed in OMERO 5.2, so use the
 * <a href="http://www.openmicroscopy.org/site/support/omero5.1/developers/Server/ObjectGraphs.html">new graphs implementation</a>
 */
@Deprecated
@SuppressWarnings("deprecation")
public class GraphSpecPostProcessor implements BeanPostProcessor,
        ApplicationContextAware {

    private final Map<String, GraphSpec> specs = new HashMap<String, GraphSpec>();

    private/* final */ApplicationContext ctx;

    public void setApplicationContext(ApplicationContext ctx)
            throws BeansException {
        this.ctx = ctx;
    }

    public Object postProcessBeforeInitialization(Object bean, String name)
            throws BeansException {

        return bean;

    }

    public Object postProcessAfterInitialization(Object bean, String name)
            throws BeansException {

        if (bean instanceof GraphSpec) {
            ((GraphSpec) bean).postProcess(ctx);
        }
        return bean;

    }

}