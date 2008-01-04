/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.search;

import java.io.Serializable;

import ome.services.SearchBean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Serializable builder used by {@link SearchBean} to generate
 * {@link QueryTemplate} instances for search.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public class QueryBuilder implements Serializable {

    private static final long serialVersionUID = 3314502886822080888L;

    private static Log log = LogFactory.getLog(QueryBuilder.class);

}
