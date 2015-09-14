/*
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.test.fixtures;

import org.hibernate.SessionFactory;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.jmock.core.matcher.InvokeAtLeastOnceMatcher;
import org.springframework.beans.factory.FactoryBean;

/**
 * Since the {@link OmeroContext} attempts to inject itself into the newly
 * created {@link Executor} it is necessary to first apply an
 * {@link Mock#expects(org.jmock.core.InvocationMatcher) expectation}.
 */
public class SessionFactoryMockFactory implements FactoryBean {

    Mock sfMock = new Mock(SessionFactory.class);
    {
        sfMock.expects(new InvokeAtLeastOnceMatcher()).method(
                "getAllClassMetadata").will(new MockObjectTestCase(){}.returnValue(
                    new java.util.HashMap()));
    }

    public Object getObject() throws Exception {
        return sfMock;
    }

    public Class getObjectType() {
        return Mock.class;
    }

    public boolean isSingleton() {
        return true;
    }

}
