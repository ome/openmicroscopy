package ome.util;

import java.text.Collator;
import java.text.RuleBasedCollator;
import java.util.Locale;


/**
 * This class represents an LSID as used by the OME-XML data model.
 * 
 * @author Chris Allan <callan at blackcat dot ca>
 *
 */
public class LSID
{
	/** Concrete Java class which qualifies the type of object. */
	private Class klass;
	
	/** Indexes within the OME-XML data model. */
	private int[] indexes;
	
	/** The LSID as a string. */
	private String asString;
	
	/** Our hash code. */
	private int hashCode;
	
	/** 
	 * The collator that we use to alphabetically sort by class name
	 * within a given level of the OME-XML hierarchy.
	 */
	private final RuleBasedCollator stringComparator = 
		(RuleBasedCollator) Collator.getInstance(Locale.ENGLISH);
	
	/**
	 * Default constructor.
	 * @param klass Concrete Java class which qualifies the type of object
	 * this LSID represents.
	 * @param indexes Indexes for this LSID within the OME-XML data model.
	 */
	public LSID(Class klass, int... indexes)
	{
		this.klass = klass;
		this.indexes = indexes;
        asString = klass.getName();
        for (int index : indexes)
        {
            asString = asString + ":" + index;
        }
        hashCode = asString.hashCode();
	}
	
	/**
	 * Constructor for non-standard LSIDs.
	 * @param asString The LSID as a string.
	 */
	public LSID(String asString)
	{
		this.asString = asString;
		hashCode = asString.hashCode();
	}
	
	/**
	 * Returns the Java class which qualifies the type of object this
	 * LSID represents.
	 * @return See above.
	 */
	public Class getJavaClass()
	{
		return klass;
	}
	
	/**
	 * Returns the indexes for this LSID within the OME-XML data model.
	 * @return See above.
	 */
	public int[] getIndexes()
	{
		return indexes;
	}
	
	@Override
	public String toString()
	{
		return asString;
	}
	
	@Override
	public int hashCode()
	{
		return hashCode;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof LSID)
		{
			LSID comparator = (LSID) obj;
			Class comparatorClass = comparator.getJavaClass();
			if (comparatorClass == null || klass == null)
			{
				return stringComparator.compare(asString, obj.toString()) == 0;
			}
			if (comparatorClass.equals(klass))
			{
				int[] comparatorIndexes = comparator.getIndexes();
				for (int i = 0; i < indexes.length; i++)
				{
					if (indexes[i] != comparatorIndexes[i])
					{
						return false;
					}
				}
				return true;
			}
			return false;
		}
		return super.equals(obj);
	}
}
