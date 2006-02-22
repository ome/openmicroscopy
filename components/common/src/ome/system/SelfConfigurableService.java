package ome.system;

import org.springframework.context.ApplicationContextAware;


public interface SelfConfigurableService extends ApplicationContextAware
{

    void selfConfigure();
    
}
