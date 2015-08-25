/*
 *   Copyright (C) 2011 University of Dundee & Open Microscopy Environment.
 *   All rights reserved.
 *
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.formats;

import java.text.Collator;
import java.text.RuleBasedCollator;
import java.util.Comparator;
import java.util.Locale;

import com.esotericsoftware.kryo.serializers.FieldSerializer;

import ome.util.LSID;
import omero.model.DetectorSettings;
import omero.model.IObject;
import omero.model.LightPath;
import omero.model.LightSettings;
import omero.model.ObjectiveSettings;
import omero.model.Pixels;

/**
 * This comparator takes into account the OME-XML data model hierarchy
 * and uses that to define equivalence.
 *
 * @author Chris Allan <callan at blackcat dot ca>
 *
 */
public class OMEXMLModelComparator implements Comparator<LSID>
{
    /**
     * The collator that we use to alphabetically sort by class name
     * within a given level of the OME-XML hierarchy.
     */
    @FieldSerializer.Optional("OMEXmlModelComparator.stringComparator")
    private RuleBasedCollator stringComparator =
        (RuleBasedCollator) Collator.getInstance(Locale.ENGLISH);

    public int compare(LSID x, LSID y)
    {
        // Handle identical LSIDs
        if (x.equals(y))
        {
            return 0;
        }

        // Parse the LSID for hierarchical equivalence tests.
        Class<? extends IObject> xClass = x.getJavaClass();
        Class<? extends IObject> yClass = y.getJavaClass();
        int[] xIndexes = x.getIndexes();
        int[] yIndexes = y.getIndexes();

        // Handle the null class (one or more unparsable internal
        // references) case.
        if (xClass == null || yClass == null)
        {
            return stringComparator.compare(x.toString(), y.toString());
        }

        // Assign values to the classes
        int xVal = getValue(xClass, xIndexes.length);
        int yVal = getValue(yClass, yIndexes.length);

        int retval = xVal - yVal;
        if (retval == 0)
        {
            // Handle different classes at the same level in the hierarchy
            // by string difference. They need to still be different.
            if (!xClass.equals(yClass))
            {
                return stringComparator.compare(x.toString(), y.toString());
            }
            // Handle the same classes at the same level in the hierarchy with
            // differing numbers of indexes by string difference. They also
            // need to still be different.
            if (xIndexes.length != yIndexes.length)
            {
                return stringComparator.compare(x.toString(), y.toString());
            }
            for (int i = 0; i < xIndexes.length; i++)
            {
                int difference = xIndexes[i] - yIndexes[i];
                if (difference != 0)
                {
                    return difference;
                }
            }
            return 0;
        }
        return retval;
    }

    /**
     * Assigns a value to a particular class based on its location in the
     * OME-XML hierarchy.
     * @param klass Class to assign a value to.
     * @param indexes Number of class indexes that were present in its LSID.
     * @return The value.
     */
    public int getValue(Class<? extends IObject> klass, int indexes)
    {
        // Top-level (Pixels is a special case due to Channel and
        // LogicalChannel containership weirdness).
        if (klass.equals(Pixels.class))
        {
            return 1;
        }

        if (klass.equals(ObjectiveSettings.class)
            || klass.equals(DetectorSettings.class)
            || klass.equals(LightPath.class))
        {
            return 3;
        }

        return indexes;
    }
}
