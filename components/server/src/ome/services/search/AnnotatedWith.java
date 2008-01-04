/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.search;

import ome.model.annotations.Annotation;
import ome.services.SearchBean;

/**
 * Query template used by {@link SearchBean} to store user requests.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public class AnnotatedWith extends SearchAction {

    private static final long serialVersionUID = 1L;

    private final Annotation annotation;

    public AnnotatedWith(SearchValues values, Annotation annotation) {
        super(values);
        this.annotation = annotation;
    }

}
