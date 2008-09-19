/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.messages;

import java.util.ArrayList;
import java.util.List;

import ome.model.IObject;
import ome.util.messages.InternalMessage;

/**
 * Published when one or more {@link IObject} instances must be re-indexed. This
 * happens especially in {@link org.hibernate.search.bridge.FieldBridge}
 * implementations since they are provided only with a single
 * {@link org.apache.lucene.document.Document} but may want to keep several in
 * sync.
 */
public class ReindexMessage<T extends IObject> extends InternalMessage {

    private static final long serialVersionUID = -4877612115500109919L;

    final public List<T> objects = new ArrayList<T>();

    public ReindexMessage(Object source, List<T> list) {
        super(source);
        objects.addAll(list);
    }

}