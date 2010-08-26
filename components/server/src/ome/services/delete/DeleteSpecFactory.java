/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.delete;

import java.util.Map;

import ome.api.IDelete;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Container which is loaded with stateful {@link DeleteSpec} instances for use
 * in a multi-threaded environment. Each {@link DeleteSpec} will be used within
 * a single thread, but be initialized multiple times sequentially.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4.2.1
 * @see IDelete
 */
public class DeleteSpecFactory implements ApplicationContextAware {

    private/* final */Map<String, DeleteSpec> specs;

    public DeleteSpec get(String type) {
        return specs.get(type);
    }

    public void setApplicationContext(ApplicationContext ctx)
            throws BeansException {

        specs = ctx.getBeansOfType(DeleteSpec.class);

        for (DeleteSpec spec : specs.values()) {
            spec.postProcess(specs);
        }

    }
}
