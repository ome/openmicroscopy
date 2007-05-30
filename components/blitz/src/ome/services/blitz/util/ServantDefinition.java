package ome.services.blitz.util;

import ome.api.ServiceInterface;

import org.springframework.beans.factory.InitializingBean;

/**
 * Provides helper methods so that servant implementations need not extend a
 * particular {@link Class}.
 * 
 * @author josh
 * 
 */
public class ServantDefinition implements InitializingBean {

    private Class tieClass;

    private Class<ServiceInterface> serviceClass;

    private Class<Ice.Object> operationsClass;

    public void afterPropertiesSet() throws Exception {
        if (tieClass == null || serviceClass == null || operationsClass == null) {
            throw new IllegalStateException(
                    "All fields on a ServantDefinition must be filled.");
        }
    }

    /**
     * @return the operationsClass
     */
    public Class<Ice.Object> getOperationsClass() {
        return operationsClass;
    }

    /**
     * @return the serviceClass
     */
    public Class<ServiceInterface> getServiceClass() {
        return serviceClass;
    }

    /**
     * @return the tieClass
     */
    public Class getTieClass() {
        return tieClass;
    }

    /**
     * @param operationsClass the operationsClass to set
     */
    public void setOperationsClass(Class<Ice.Object> operationsClass) {
        this.operationsClass = operationsClass;
    }

    /**
     * @param serviceClass the serviceClass to set
     */
    public void setServiceClass(Class<ServiceInterface> serviceClass) {
        this.serviceClass = serviceClass;
    }

    /**
     * @param tieClass the tieClass to set
     */
    public void setTieClass(Class tieClass) {
        this.tieClass = tieClass;
    }

}