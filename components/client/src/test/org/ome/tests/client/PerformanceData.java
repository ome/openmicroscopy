/*
 * Created on May 22, 2005
*/
package org.ome.tests.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @author josh
 */
public class PerformanceData {

    // Main field
    double percent = 0.05;
    long seed = (new Random()).nextLong();
    Random rnd = new Random(seed);
    DataSource ds = (DataSource)SpringTestHarness.ctx.getBean("dataSource");
    
    public PerformanceData(){
    }
    
    public PerformanceData(double percent) {
        this.percent = percent;
    }
    
    public PerformanceData(double percent, long seed){
        this.percent=percent;
        this.seed = seed;
        this.rnd = new Random(seed);
    }

    // Messages
    String emptyColl = "collections may not be empty";

    // Test data : calculated before to not change times.
    public Set allUsers = getAllIds("experimenters","attribute_id");
    public Set allImgs = getAllIds("images","image_id");
    public Set allDss = getAllIds("datasets","dataset_id");
    public Set allPrjs = getAllIds("projects","project_id");
    public Set allCgs = getAllIds("category_groups","attribute_id");
    public Set allCs = getAllIds("categories","attribute_id");

    public int userId = getOneFromCollection(allUsers); // Perhaps generalize on type HOW OFTEN IS THIS USED! Each ONCE ?
    public int prjId = getOneFromCollection(allPrjs);
    public int dsId = getOneFromCollection(allDss);
    public int cgId = getOneFromCollection(allCgs);
    public int cId = getOneFromCollection(allCs);
    public Set imgsPDI = getPercentOfCollection(allImgs, percent);
    public Set imgsCGCI = getPercentOfCollection(allImgs, percent);
    public Set imgsAnn1 = getPercentOfCollection(allImgs, percent);
    public Set imgsAnn2 = getPercentOfCollection(allImgs, percent);
    public Set dsAnn1 = getPercentOfCollection(allDss, percent);
    public Set dsAnn2 = getPercentOfCollection(allDss, percent);
    
    private Set getAllIds(String table, String field) {
        JdbcTemplate jt = new JdbcTemplate(ds);
        List rows = jt.queryForList("select "+field+" from "+table);
        Set result = new HashSet();
        for (Iterator i = rows.iterator(); i.hasNext();) {
            Map element = (Map) i.next();
            result.add(element.get(field));
        }
        return result;
    }

    private int getOneFromCollection(final Collection ids) {
        
        if (ids.size()==0){
            throw new IllegalArgumentException(emptyColl);
        }
        
        List ordered = new ArrayList(ids);
        int choice = randomChoice(ids.size());
        return ((Integer)ordered.get(choice)).intValue();
    }

    private Set getPercentOfCollection(final Set ids, double percent) {
        
        if (ids.size()==0){
            throw new IllegalArgumentException(emptyColl);
        }
        
        List ordered = new ArrayList(ids);
        Set result = new HashSet();

        while (result.size() < ids.size() * percent){ 
            int choice = randomChoice(ordered.size());
            result.add(ordered.remove(choice));
        }
        
        return result;
    }
    
    private int randomChoice(int size){
        double value = (size-1) * rnd.nextDouble();
        return (new Double(value)).intValue();
    }
    
}
