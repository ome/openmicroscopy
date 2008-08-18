package ome.services.blitz.test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.jmock.Mock;
import org.springframework.aop.framework.ProxyFactoryBean;
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

    private final Map<Class<?>, Class<?>> interfaces;

    public MockDeclarer(Map<Class<?>, Class<?>> interfaces) {
        this.interfaces = interfaces;
    }

    public void postProcessBeanFactory(ConfigurableListableBeanFactory context)
            throws BeansException {

        final BeanDefinitionRegistry registry = ((BeanDefinitionRegistry) context);

        for (Class<?> iface : interfaces.keySet()) {
            String mockname = "mock-" + iface.getName();
            String hidden = "hidden-" + iface.getName();
            String internal = "internal-" + iface.getName();
            String managed = "managed-" + iface.getName();

            // Now replace the iface used as name with the subclass if present
            if (interfaces.get(iface) != null) {
                iface = interfaces.get(iface);
            }

            // mock-
            ConstructorArgumentValues ctor = new ConstructorArgumentValues();
            ctor.addGenericArgumentValue(iface);
            BeanDefinition mock = new RootBeanDefinition(Mock.class, ctor, null);
            registry.registerBeanDefinition(mockname, mock);

            // hidden- since mock.proxy() doesn't work in IceMethodInvoker
            BeanDefinition factory = new RootBeanDefinition();
            factory.setFactoryBeanName(mockname);
            factory.setFactoryMethodName("proxy");
            registry.registerBeanDefinition(hidden, factory);

            // internal- with a spring proxy
            List i = Arrays.asList(iface);
            MutablePropertyValues properties = new MutablePropertyValues();
            properties.addPropertyValue("proxyInterfaces", i);
            properties.addPropertyValue("target", new RuntimeBeanReference(
                    hidden));
            BeanDefinition spring = new RootBeanDefinition(
                    ProxyFactoryBean.class, null, properties);
            registry.registerBeanDefinition(internal, spring);

            // Alias, since internal- and managed- are the same
            registry.registerAlias(internal, managed);
        }

    }

}