/*
 * Created on Feb 14, 2005
 */
package org.ome.srv.db.jena;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.pool.KeyedObjectPool;
import org.ome.model.AbstractLSObject;
import org.ome.model.Factory;
import org.ome.model.LSID;
import org.ome.model.LSObject;
import org.ome.model.SemanticType;
import org.ome.model.Vocabulary;
import org.ome.interfaces.GenericService;
import org.ome.model.FollowGroup;
import org.ome.srv.db.GenericStore;
import org.ome.srv.db.NamedQuery;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdql.Query;
import com.hp.hpl.jena.rdql.QueryEngine;
import com.hp.hpl.jena.rdql.QueryResults;
import com.hp.hpl.jena.rdql.ResultBinding;
import com.hp.hpl.jena.rdql.ResultBindingImpl;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * @author josh
 */
public class JenaGenericStore implements GenericService, GenericStore {

    protected KeyedObjectPool pool;

    protected String defaultModelName;

    // TODO should such strings be runtime changeable

    /**
     * calls evaluateNamedQuery with the default model
     */
    public List evaluateNamedQuery(NamedQuery nq) {
        return this.evaluateNamedQuery(nq, defaultModelName);
    }

    /**
     * main querying mechanism. Returns non-resolved objects (i.e. no data)
     * simply pointers to that data which have to be manually resolved with the
     * other methods on this class.
     */
    public List evaluateNamedQuery(NamedQuery nq, String modelName) {

        List l = null;
        Model m = getModel(modelName);

        try {
            Map map = parseMap(m, nq);
            QueryResults results = queryWithBinding(m, nq.getQueryString(), map);
            l = lsObjectFromQueryResult(results, nq.getTarget());
            results.close();
        } finally {
            returnModel(modelName, m);
        }

        return l;
    }

    private Map parseMap(Model m, NamedQuery nq) {
        Map map = new HashMap();

        for (Iterator iter = nq.getBindingMap().keySet().iterator(); iter
                .hasNext();) {
            String key = (String) iter.next();
            LSID value = (LSID) nq.getBindingMap().get(key);
            if (null != value) {
                map.put(key, m.getResource(value.getURI()));//FIXME Assumption
                // also need
                // literals!
                // (Values!)
                //TODO
                // file:///home/josh/lib/jena/doc/ontology/examples/describe-class/DescribeClass.java.html
                //also an assumption that null is the same as a wildcard in the
                // query though the triple _must_ exist!
            }
        }
        return map;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ome.interfaces.GenericService#setLSOjbect(org.ome.LSObject)
     */
    public void setLSObject(LSObject lsobj) {
        this.setLSObject(lsobj, defaultModelName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ome.srv.db.GenericStore#setLSObject(org.ome.model.LSObject,
     *      java.lang.String)
     */
    public void setLSObject(LSObject lsobj, String modelName) {

        Model m = getModel(modelName);

        try {
            Resource subj = m.createResource(lsobj.getLSID().getURI());
            subj.removeProperties();

            Map map = lsobj.getMap();
            for (Iterator iter1 = map.keySet().iterator(); iter1.hasNext();) {
                String key = (String) iter1.next();
                Property prop = m.getProperty(key);

                Object newValue = map.get(key);
                List nodes = getAddableResourcesFromObject(m, newValue);

                for (Iterator iter2 = nodes.iterator(); iter2.hasNext();) {
                    RDFNode node = (RDFNode) iter2.next();
                    subj.addProperty(prop, node);
                }
            }
        } finally {
            returnModel(modelName, m);
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ome.interfaces.GenericService#updateLSObject(org.ome.LSObject)
     */
    public void updateLSObject(LSObject lsobj) {
        this.updateLSObject(lsobj, defaultModelName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ome.srv.db.GenericStore#updateLSObject(org.ome.model.LSObject,
     *      java.lang.String)
     */
    public void updateLSObject(LSObject lsobj, String modelName) {

        Model m = getModel(modelName);
        try {
            Resource subj = m.getResource(lsobj.getLSID().getURI());

            if (!m.contains(subj, null)) {
                throw new RuntimeException("Object " + lsobj + "doesn't exist");
                // FIXME check other models
            }

            Map map = lsobj.getMap();
            m.begin();
            try {
                for (Iterator i = map.keySet().iterator(); i.hasNext();) {
                    String key = (String) i.next();
                    Property prop = m.getProperty(key);

                    /* the existing values */
                    StmtIterator stmts = subj.listProperties(prop);
                    List objects = new ArrayList();
                    for (Iterator j = stmts; j.hasNext();) {
                        objects.add(((Statement) j.next()).getObject());
                    }

                    /* the new values */
                    // TODO This list must be kept in sync with AbstractLSObject
                    Object newValue = map.get(key);
                    List nodes = getAddableResourcesFromObject(m, newValue);

                    /* mapping the changes *///FIXME TRANSACTIONS!!!
                    subj.removeAll(prop);
                    for (Iterator iter = nodes.iterator(); iter.hasNext();) {
                        RDFNode value = (RDFNode) iter.next();
                        m.add(subj, prop, value);
                    }
                }
            } catch (Exception e) {
                m.abort();
                throw new RuntimeException(e); // TODO
            } finally {
                m.commit();
            }
        } finally {
            returnModel(modelName, m);//TODO please remember to return your
            // models!!!
        }
    }

    private List getAddableResourcesFromObject(Model m, Object newValue) {
        List nodes = new ArrayList();
        if (newValue instanceof List) {
            for (Iterator iter = ((List) newValue).iterator(); iter.hasNext();) {
                Object element = iter.next();
                nodes.add(nodeFromObject(m, element));
            }
        } else {
            RDFNode node = nodeFromObject(m, newValue);
            if (null != node) {
                nodes.add(node);
            }

        }
        return nodes;
    }

    /**
     * @param element
     * @return
     */
    private RDFNode nodeFromObject(Model m, Object newValue) {
        RDFNode node = null;
        if (null == newValue) {
            return null;
        }

        if (newValue instanceof AbstractLSObject) {
            node = m.createResource(((LSObject) newValue).getLSID().getURI());
        } else if (newValue instanceof Float || newValue instanceof Double
                || newValue instanceof String || newValue instanceof Integer) {
            node = m.createTypedLiteral(newValue);
        } else {
            throw new RuntimeException("Unallowed type");//TODO
        }
        return node;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ome.interfaces.GenericService#getLSObject(org.ome.LSID)
     */
    public LSObject getLSObject(LSID lsid) {
        return this.getLSObject(lsid, defaultModelName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ome.srv.db.GenericStore#getLSObject(org.ome.model.LSID,
     *      java.lang.String)
     */
    public LSObject getLSObject(LSID lsid, String modelName) {
        Model m = getModel(modelName);
        LSObject lsobj = null;
        
        try {
        Resource subj = m.getResource(lsid.getURI());
        lsobj = Factory.make(lsid.getURI(), getType(subj));

        if (!m.contains(subj, null)) {
            return null;
        }

        for (StmtIterator iter = subj.listProperties(); iter.hasNext();) {
            Property pred = iter.nextStatement().getPredicate();

            List values = getValuesForSubjPred(subj, pred);

            if (values.size() > 0) {
                Integer max = Vocabulary.getMax(pred.getURI());
                if (null != max && max.intValue() == 1) {
                    // undefined which for values>1
                    lsobj.put(pred.getURI(), values.get(0));
                } else {
                    lsobj.put(pred.getURI(), values);
                }
            } else {
                //No values available.
                Integer min = Vocabulary.getMin(pred.getURI());
                if (min.intValue() >= 1) {
                    System.err
                            .println("Problem: no values where needed in creating object!"); //FIXME
                }

            }

        }
        } finally {
            returnModel(modelName, m);    
        }
        
        return lsobj;

    }

    private List getValuesForSubjPred(Resource subj, Property pred) {
        List values = new ArrayList();

        for (StmtIterator iter2 = subj.listProperties(pred); iter2.hasNext();) {

            RDFNode obj = iter2.nextStatement().getObject();

            /*
             * Ok. now have (sub,pred,obj) in RDF. Now we need a representation
             * in _Java_ of the object
             */

            Object value = null;

            if (obj.canAs(Resource.class)) {
                LSObject child = null;
                Resource r = (Resource) obj;
                if (pred.getURI().equals(RDF.type)) {
                    child = Factory.make(r.getURI(), SemanticType.URI);
                } else {
                    child = Factory.make(r.getURI(), getType(r));
                }

                value = child;

            } else if (obj.canAs(Literal.class)) {
                value = ((Literal) obj).getValue();
            }
            values.add(value);
        }
        return values;
    }

    /**
     * @param subj
     * @return
     */
    private String getType(Resource subj) {
        // TODO perhaps get statement method with this as the subject
        //TODO refactor where should this code be.
        List types = new ArrayList();
        for (StmtIterator i = subj.listProperties(RDF.type); i.hasNext();) {
            Statement s = (Statement) i.next();
            RDFNode o = s.getObject();
            if (o.canAs(Resource.class)) {
                String t = ((Resource) o).getURI();
                types.add(t);
            }
        }
        //FIXME types
        if (types.size() == 1) {
            return types.get(0).toString();
        } else if (types.size() > 1) {
            System.err.println("Multiple types " + types + " in DB.getType");
            return types.get(0).toString();
        }
        return "";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ome.interfaces.GenericService#getLSObject(org.ome.LSID,
     *      org.ome.texen.srv.PredicateGroup)
     */
    public LSObject getLSObject(LSID lsid, FollowGroup fg) {
        //		 TODO Auto-generated method stub
        throw new RuntimeException("implement me");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ome.interfaces.GenericService#getLSObjectWithFollowGroup(org.ome.model.LSID,
     *      org.ome.model.FollowGroup)
     */
    public LSObject getLSObjectWithFollowGroup(LSID arg0, FollowGroup arg1) {
        // TODO Auto-generated method stub
        /* return null; */
        throw new RuntimeException("implement me");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ome.interfaces.GenericService#getLSObjectsByLSIDType(org.ome.model.LSID)
     */
    public List getLSObjectsByLSIDType(LSID arg0) {
        // TODO Auto-generated method stub
        /* return null; */
        throw new RuntimeException("implement me");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ome.interfaces.GenericService#getLSObjectsByClassType(java.lang.Class)
     */
    public List getLSObjectsByClassType(Class arg0) {
        // TODO Auto-generated method stub
        /* return null; */
        throw new RuntimeException("implement me");
    }

    protected List lsObjectFromQueryResult(QueryResults results, String variable) {
        List l = new ArrayList();
        for (Iterator iter = results; iter.hasNext();) {
            ResultBinding res = (ResultBinding) iter.next();
            Resource p = (Resource) res.get(variable);
            if (null != p) {
                LSObject obj = Factory.make(p.getURI(), "");//TODO this could
                // just return LSIDs
                // if the factory
                // had a method for
                // that. or if lsids
                // and lsobjcts were
                // combined!
                l.add(obj);
            }
        }
        return l;
    }

    protected QueryResults queryWithBinding(Model m, String queryString, Map map) {

        Query query = new Query(queryString);

        // Need to set the source if the query does not.
        query.setSource(m);
        QueryEngine qe = new QueryEngine(query);

        ResultBindingImpl binding = new ResultBindingImpl();
        for (Iterator iter = map.keySet().iterator(); iter.hasNext();) {
            String key = (String) iter.next();
            binding.add(key, (Resource) map.get(key));
        }
        QueryResults results = qe.exec(binding);
        return results;
    }

    private void returnModel(String modelName, Model m) {
        try {
            pool.returnObject(modelName, m);
        } catch (Exception e1) {
            throw new RuntimeException("Failed to return model to pool.", e1);
        }
    }

    private Model getModel(String modelName) {
        Model m;
        try {
            m = (Model) pool.borrowObject(modelName);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get model from pool.", e);
        }
        return m;
    }

    /**
     * @param pool
     *            The pool to set.
     */
    public void setPool(KeyedObjectPool pool) {
        this.pool = pool;
    }

    /**
     * @param defaultModelName
     *            The defaultModelName to set.
     */
    public void setDefaultModelName(String defaultModelName) {
        this.defaultModelName = defaultModelName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ome.srv.db.GenericStore#getLSObjectWithFollowGroup(org.ome.model.LSID,
     *      org.ome.model.FollowGroup, java.lang.String)
     */
    public LSObject getLSObjectWithFollowGroup(LSID lsid, FollowGroup fg,
            String modelName) {
        // TODO Auto-generated method stub
        /* return null; */
        throw new RuntimeException("implement me");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ome.srv.db.GenericStore#getLSObjectsByLSIDType(org.ome.model.LSID,
     *      java.lang.String)
     */
    public List getLSObjectsByLSIDType(LSID type, String modelName) {
        // TODO Auto-generated method stub
        /* return null; */
        throw new RuntimeException("implement me");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ome.srv.db.GenericStore#getLSObjectsByClassType(java.lang.Class,
     *      java.lang.String)
     */
    public List getLSObjectsByClassType(Class klass, String modelName) {
        // TODO Auto-generated method stub
        /* return null; */
        throw new RuntimeException("implement me");
    }
}