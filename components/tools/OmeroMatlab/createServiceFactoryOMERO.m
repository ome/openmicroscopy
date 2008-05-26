function [serviceFactory] = createServiceFactoryOMERO(iceConfig, username, password)


serviceFactory = blitzgateway.service.ServiceFactory(java.lang.String(iceConfig));
serviceFactory.createSession(username, password);
