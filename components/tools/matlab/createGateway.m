function [gateway] = createGateway(iceConfig, username, password)
import blitzgateway.*;

gateway = service.ServiceFactory(java.lang.String(iceConfig));
gateway.createSession(username, password);
