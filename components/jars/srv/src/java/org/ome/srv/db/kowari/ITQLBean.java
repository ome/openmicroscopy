/*
 * Created on Feb 28, 2005
 */
package org.ome.srv.db.kowari;

import java.sql.*;

//Kowari packages
import org.kowari.itql.ItqlInterpreterBean;
import org.kowari.query.Answer;

/**
 * @author josh
 */
public class ITQLBean {

    protected ItqlInterpreterBean interpreter;
    protected Answer answer;
    protected String modelName;
    
    public void query() {
//TODO SEE: file:///home/josh/lib/kowari-1.0.5/docs/site/2513.htm
        try {

            //     Query to select all subject-predicate-object statements from the
            // model
            String query = "select $s $p $o from <" + modelName
                    + "> where $s $p $o ;";

            //     Do the query
            Answer answer = interpreter.executeQuery(query);

            //     Print out the results

            System.out.println("\nQuery Results:\n");

            while (answer.next()) {

                Object subject = answer.getObject(0);
                Object predicate = answer.getObject(1);
                Object object = answer.getObject(2);

                System.out.println("Subject: " + subject + ", Predicate:"
                        + predicate + ", Object: " + object);
                answer.close();
            }
        } catch (Exception e) {
            System.out.println("\nAn Exception occurred: \n" + e);
        }
    }
}