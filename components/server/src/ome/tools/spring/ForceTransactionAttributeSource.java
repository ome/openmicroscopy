/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.tools.spring;

import java.lang.reflect.Method;

import ome.conditions.InternalException;

import org.springframework.transaction.interceptor.TransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAttributeSource;

/**
 * Requires that all method calls have some other form of attribute source by
 * throwing an exception if accessed;
 */
public class ForceTransactionAttributeSource implements
        TransactionAttributeSource {

    public TransactionAttribute getTransactionAttribute(Method method,
            Class targetClass) {
        final String fmt = "%s.%s should have a @Transactional annotation!";
        throw new InternalException(String.format(fmt, targetClass
                .getName(), method.getName()));
    }
}
