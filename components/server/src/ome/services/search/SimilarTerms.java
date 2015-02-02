/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ome.conditions.ApiUsageException;
import ome.conditions.InternalException;
import ome.model.annotations.CommentAnnotation;
import ome.model.annotations.TextAnnotation;
import ome.system.ServiceFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.FuzzyTermEnum;
import org.hibernate.Session;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.search.SearchFactory;
import org.hibernate.search.indexes.IndexReaderAccessor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * Search to find similar terms to some given terms.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4
 */
public class SimilarTerms extends SearchAction {

    private static final Logger log = LoggerFactory.getLogger(SimilarTerms.class);

    private static final long serialVersionUID = 1L;

    private final String[] terms;

    public SimilarTerms(SearchValues values, String...terms) {
        super(values);
        this.terms = terms;
    }

    @Transactional(readOnly = true)
    public Object doWork(Session s, ServiceFactory sf) {

        if (values.onlyTypes == null || values.onlyTypes.size() != 1) {
            throw new ApiUsageException(
                    "Searches by similar terms are currently limited to a single type.\n"
                            + "Plese use Search.onlyType()");
        }
        final Class<?> cls = values.onlyTypes.get(0);

        final FullTextSession session = Search.getFullTextSession(s);
        final SearchFactory factory = session.getSearchFactory();
        final IndexReaderAccessor ra = factory.getIndexReaderAccessor();
        final IndexReader reader = ra.open(cls);

        final List<TextAnnotation> rv = new ArrayList<TextAnnotation>();

        FuzzyTermEnum fuzzy = null;
        try {
            fuzzy = new FuzzyTermEnum(reader, new Term("combined_fields", terms[0]));
            while (fuzzy.next()) {
                CommentAnnotation text = new CommentAnnotation();
                text.setNs(terms[0]);
                text.setTextValue(fuzzy.term().text());
                rv.add(text);
            }
            return rv;
        } catch (IOException e) {
            throw new InternalException("Error reading from index: "+e.getMessage());
        } finally {
            if (fuzzy != null) {
                fuzzy.endEnum();
            }
        }
        
    }
}
