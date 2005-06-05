/*
 * Created on Jun 5, 2005
 */
package org.openmicroscopy.omero.logic;

import java.util.List;
import java.util.Set;

/**
 * @author josh
 */
public interface AnnotationDao {
    public List findImageAnnotations(final Set ids);

    public List findImageAnnotationsForExperimenter(final Set ids, final int exp);

    public List findDataListAnnotations(final Set ids);

    public List findDataListAnnotationForExperimenter(final Set ids,
            final int exp);
}