/*
 * Created on Feb 12, 2005
 */
package org.ome.srv.db.jena;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.pool.BaseKeyedPoolableObjectFactory;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.datasource.DataSourceUtils;

import com.hp.hpl.jena.db.DBConnection;
import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;

/**
 * @author josh
 */
public class JenaModelFactory extends BaseKeyedPoolableObjectFactory {

    private Map makerMap = new HashMap();
    
    protected DataSource dataSource = null;

    protected String databaseType = null;

    public ModelMaker getMaker() {
        ModelMaker maker = null;
        try {
            // Create database connection
            Connection c = DataSourceUtils.getConnection(dataSource);
            IDBConnection conn = new DBConnection(c, databaseType);
            // Create a model maker object
            maker = ModelFactory.createModelRDBMaker(conn);

        } catch (CannotGetJdbcConnectionException sqle) {
            throw new RuntimeException(
                    "Obtaining connection failed while trying to make model maker",
                    sqle);
        }
        return maker;
    }

    /**
     * @return Returns the databaseType.
     */
    public String getDatabaseType() {
        return databaseType;
    }

    /**
     * @param databaseType
     *            The databaseType to set.
     */
    public void setDatabaseType(String databaseType) {
        this.databaseType = databaseType;
    }

    /**
     * @return Returns the dataSource.
     */
    public DataSource getDataSource() {
        return dataSource;
    }

    /**
     * @param dataSource
     *            The dataSource to set.
     */
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.commons.pool.BaseKeyedPoolableObjectFactory#makeObject(java.lang.Object)
     */
    public Object makeObject(Object key) {
        ModelMaker mm = getMaker();

        // create a spec for the new ont model
        OntModelSpec spec = new OntModelSpec(OntModelSpec.OWL_DL_MEM);

        // create the base model as a persistent model
        Model base = mm.createModel((String) key);
        OntModel m = ModelFactory.createOntologyModel(spec, base);
        makerMap.put(base,mm);
        
        return m;
    }
    
    /* (non-Javadoc)
     * @see org.apache.commons.pool.BaseKeyedPoolableObjectFactory#destroyObject(java.lang.Object, java.lang.Object)
     */
    public void destroyObject(Object arg0, Object arg1) throws Exception {
        super.destroyObject(arg0,arg1);
        makerMap.remove(arg0);
    }

}