/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.util;

import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.hibernate.search.bridge.FieldBridge;
import org.springframework.context.ApplicationContext;
import org.springframework.context.access.ContextSingletonBeanFactoryLocator;

/**
 * Delegating {@link FieldBridge} which passes the "fieldBridge" bean from the
 * "ome.model" Spring {@link ApplicationContext} all arguments.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public class DetailsFieldBridge implements FieldBridge {

    private final static ApplicationContext CONTEXT = (ApplicationContext) ContextSingletonBeanFactoryLocator
            .getInstance().useBeanFactory("ome.model").getFactory();

    private final static Map BRIDGES = (Map) CONTEXT.getBean("bridges");

    public void set(final String name, final Object value,
            final Document document, final Field.Store store,
            final Field.Index index, final Float boost) {

        FieldBridge bridge = (FieldBridge) BRIDGES.get("fieldBridges");
        bridge.set(name, value, document, store, index, boost);

    }

}
