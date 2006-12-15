/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.tools.hibernate;

import java.util.Properties;

import org.hibernate.MappingException;
import org.hibernate.dialect.Dialect;
import org.hibernate.id.PersistentIdentifierGenerator;
import org.hibernate.id.SequenceGenerator;
import org.hibernate.type.Type;

/**
 * http://www.hibernate.org/296.html
 * 
 * @author josh
 * 
 */
public class MySeqGenerator extends SequenceGenerator {

    /**
     * If the parameters do not contain a {@link SequenceGenerator#SEQUENCE}
     * name, we assign one based on the table name.
     */
    @Override
    public void configure(Type type, Properties params, Dialect dialect)
            throws MappingException {
        if (params.getProperty(SEQUENCE) == null
                || params.getProperty(SEQUENCE).length() == 0) {
            String tableName = params
                    .getProperty(PersistentIdentifierGenerator.TABLE);
            if (tableName != null) {
                String seqName = "seq_" + tableName;
                params.setProperty(SEQUENCE, seqName);
            }
        }
        super.configure(type, params, dialect);
    }
}
