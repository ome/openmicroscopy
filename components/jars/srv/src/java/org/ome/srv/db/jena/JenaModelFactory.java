/*
 * Created on Feb 12, 2005
 */
package org.ome.srv.db.jena;

import java.sql.SQLException;

import javax.sql.DataSource;

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
public class JenaModelFactory {

    protected DataSource dataSource = null;

    protected String databaseType = null;

    protected ModelMaker maker = null;

    protected String modelName = null;

    public Model getModel() {
        // create a spec for the new ont model
        OntModelSpec spec = new OntModelSpec(OntModelSpec.OWL_DL_MEM);

        // create the base model as a persistent model
        Model base = getMaker().createModel(modelName);
        OntModel m = ModelFactory.createOntologyModel(spec, base);

        return m;

    }

    public ModelMaker getMaker() {
        if (null == maker) {
            try {
                // Create database connection
                IDBConnection conn = new DBConnection(dataSource
                        .getConnection(), databaseType);
                // Create a model maker object
                maker = ModelFactory.createModelRDBMaker(conn);

            } catch (SQLException sqle) {
                throw new RuntimeException(
                        "Obtaining connection failed while trying to make model maker");
            }
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
     * @param databaseType The databaseType to set.
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
     * @param dataSource The dataSource to set.
     */
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    /**
     * @return Returns the modelName.
     */
    public String getModelName() {
        return modelName;
    }
    /**
     * @param modelName The modelName to set.
     */
    public void setModelName(String modelName) {
        this.modelName = modelName;
    }
    /**
     * @param maker The maker to set.
     */
    public void setMaker(ModelMaker maker) {
        this.maker = maker;
    }
}