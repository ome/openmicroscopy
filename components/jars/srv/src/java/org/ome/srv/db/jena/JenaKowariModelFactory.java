/*
 * Created on Feb 28, 2005
 */
package org.ome.srv.db.jena;

import java.net.URI;

import org.apache.commons.pool.BaseKeyedPoolableObjectFactory;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * @author josh
 */
public class JenaKowariModelFactory extends BaseKeyedPoolableObjectFactory {
    protected URI serverUri;

    /**
     * @return Returns the serverURI.
     */
    public URI getServerUri() {
        return serverUri;
    }

    /**
     * @param serverURI
     *            The serverURI to set.
     */
    public void setServerUri(URI serverURI) {
        this.serverUri = serverURI;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.commons.pool.BaseKeyedPoolableObjectFactory#makeObject(java.lang.Object)
     */
    public Object makeObject(Object key) throws Exception {
            //return AbstractJenaFactory.newModel(serverUri.toString()+"#"+(String)key);
        throw new RuntimeException("implement me if you can"); //TODO
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.commons.pool.BaseKeyedPoolableObjectFactory#destroyObject(java.lang.Object,
     *      java.lang.Object)
     */
    public void destroyObject(Object key, Object model) throws Exception {
        if (model != null) {
            try {
                ((Model)model).close();
            } finally {
                model = null;
            }
        }
    }
}

