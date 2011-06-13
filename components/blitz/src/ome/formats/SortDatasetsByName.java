/*
 *   Copyright (C) 2006-2011 University of Dundee & Open Microscopy Environment.
 *   All rights reserved.
 *
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.formats;

import java.util.Comparator;

import omero.model.Dataset;

/**
 * @author Brian Loranger brain at lifesci.dundee.ac.uk
 *
 */
public class SortDatasetsByName implements Comparator<Dataset>{
    public int compare(Dataset o1, Dataset o2)
    {
        return o1.getName().getValue().compareTo(o2.getName().getValue());
    }
 }
