/*
 *   Copyright (C) 2008-2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.blitz.test.mock;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.jmock.Mock;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.aop.target.HotSwappableTargetSource;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;

public class MockDeclarer implements BeanFactoryPostProcessor {

    private final Map<Class<?>, List<Class<?>>> interfaces;

    public MockDeclarer(Map<Class<?>, List<Class<?>>> interfaces) {
        this.interfaces = interfaces;
    }

    public void postProcessBeanFactory(ConfigurableListableBeanFactory context)
            throws BeansException {

        final BeanDefinitionRegistry registry = ((BeanDefinitionRegistry) context);

        for (Class<?> key : interfaces.keySet()) {
            String mockname = "mock-" + key.getName();
            String hidden = "hidden-" + key.getName();
            String swappable = "swappable-" + key.getName();
            String internal = "internal-" + key.getName();
            String managed = "managed-" + key.getName();

            // Now replace the iface used as name with the subclass if present
            List ifaces;
            if (interfaces.get(key) != null && interfaces.get(key).size() > 0) {
                ifaces = interfaces.get(key);
            } else {
                ifaces = Arrays.asList(key);
            }

            // mock-
            ConstructorArgumentValues ctor = new ConstructorArgumentValues();
            ctor.addGenericArgumentValue(ifaces.get(0));
            BeanDefinition mock = new RootBeanDefinition(Mock.class, ctor, null);
            registry.registerBeanDefinition(mockname, mock);

            // hidden- since mock.proxy() doesn't work in IceMethodInvoker
            BeanDefinition factory = new RootBeanDefinition();
            factory.setFactoryBeanName(mockname);
            factory.setFactoryMethodName("proxy");
            registry.registerBeanDefinition(hidden, factory);

            // Add swappable container for more accurate testing
            ConstructorArgumentValues values = new ConstructorArgumentValues();
            values.addGenericArgumentValue(new RuntimeBeanReference(hidden));
            BeanDefinition swapper = new RootBeanDefinition(HotSwappableTargetSource.class, values, null);
            registry.registerBeanDefinition(swappable, swapper);

            // internal- with a spring proxy
            MutablePropertyValues properties = new MutablePropertyValues();
            properties.addPropertyValue("proxyInterfaces", ifaces);
            properties.addPropertyValue("targetSource", new RuntimeBeanReference(
                    swappable));
            BeanDefinition spring = new RootBeanDefinition(
                    ProxyFactoryBean.class, null, properties);
            registry.registerBeanDefinition(internal, spring);

            // Alias, since internal- and managed- are the same
            registry.registerAlias(internal, managed);
        }

    }

}
