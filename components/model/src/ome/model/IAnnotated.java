/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.model;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import ome.model.annotations.Annotation;
import ome.util.CBlock;

/**
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public interface IAnnotated extends IObject {

    public void clearAnnotationLinks();

    public <E> List<E> collectAnnotationLinks(CBlock<E> block);

    public <E> List<E> eachLinkedAnnotation(CBlock<E> block);

    public <T> Iterator<T> iterateAnnotationLinks();

    public <T> T linkAnnotation(Annotation addition);

    public Iterator<Annotation> linkedAnnotationIterator();

    public List<Annotation> linkedAnnotationList();

    public int sizeOfAnnotationLinks();

    public void unlinkAnnotation(Annotation removal);

    public <T> Collection<T> unmodifiableAnnotationLinks();

}
