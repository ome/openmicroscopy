/*   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.icy.fixtures;

import ome.services.util.Executor;
import ome.system.OmeroContext;

import org.jmock.Mock;
import org.jmock.core.matcher.InvokeAtLeastOnceMatcher;
import org.springframework.beans.factory.FactoryBean;

/**
 * Since the {@link OmeroContext} attempts to inject itself into the newly
 * created {@link Executor} it is necessary to first apply an
 * {@link Mock#expects(org.jmock.core.InvocationMatcher) expectation}.
 */
public class ExecutorMockFactory implements FactoryBean {

    Mock executorMock = new Mock(Executor.class);
    {
        executorMock.expects(new InvokeAtLeastOnceMatcher()).method(
                "setApplicationContext");
    }

    public Object getObject() throws Exception {
        return executorMock;
    }

    public Class getObjectType() {
        return Mock.class;
    }

    public boolean isSingleton() {
        return true;
    }

}