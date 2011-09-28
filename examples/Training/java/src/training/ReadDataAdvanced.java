/*
 * training.ReadDataAdvanced
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2011 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package training;


//Java imports
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import static omero.rtypes.rint;
import static omero.rtypes.rstring;
import omero.api.IContainerPrx;
import omero.api.IQueryPrx;
import omero.model.Dataset;
import omero.model.DatasetI;
import omero.model.IObject;
import omero.model.Image;
import omero.model.Project;
import omero.model.Screen;
import omero.model.TagAnnotation;
import omero.model.TagAnnotationI;
import omero.model.Well;
import omero.sys.Filter;
import omero.sys.ParametersI;

/**
 * More advanced code for how to load data from an OMERO server.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.3.2
 */
public class ReadDataAdvanced
	extends ConnectToOMERO
{

	/** The name of a Dataset.*/
	private String datasetName = "MyDataset";

	/** The name of a Tag.*/
	private String tagName = "MyTag";

	/**
	 * Creates 3 Datasets with the name defined by {@link #datasetName}.
	 */
	private void createDatasets()
		throws Exception
	{
                List<IObject> datasets = new ArrayList<IObject>();
                for (int i = 0; i < 3; i ++)
                {
                    Dataset d = new DatasetI();
                    d.setName(rstring(datasetName));
                    datasets.add(d);
                }
                entryUnencrypted.getUpdateService().saveArray(datasets);
	}

	/**
	 * Creates 3 Tags with the namespace value defined by {@link #tagName}.
	 */
	private void createTags()
		throws Exception
	{
                List<IObject> tags = new ArrayList<IObject>();
                for (int i = 0; i < 3; i ++)
                {
                    TagAnnotation t = new TagAnnotationI();
                    t.setNs(rstring(tagName));
                    t.setDescription(rstring(String.format("%s %s", tagName, i)));
                    tags.add(t);
                }
                entryUnencrypted.getUpdateService().saveArray(tags);
	}


	/**
	 * Retrieve the Datasets that match the name value.
	 */
	private void loadDatasetsByName()
		throws Exception
	{
                final boolean caseSensitive = true;
                final Filter filter = new Filter();
                // Return the first 10 hits or less.
                filter.limit = rint(10);
                filter.offset = rint(0);

                IQueryPrx proxy = entryUnencrypted.getQueryService();
                List<IObject> datasets = (List<IObject>)
                    proxy.findAllByString("Dataset", "name", datasetName, caseSensitive, filter);
                System.out.println("\nList Datasets:");
                for (IObject obj : datasets)
                {
                    Dataset d = (Dataset) obj;
                    System.out.println("ID: " + d.getId().getValue() + " Name: " + d.getName().getValue());

                }
	}

	/**
	 * Retrieve the Tags that match the ns value.
	 */
	private void loadTagsByNS()
		throws Exception
	{
                final boolean caseSensitive = true;
                final Filter filter = new Filter();
                // Return the first 10 hits or less.
                filter.limit = rint(10);
                filter.offset = rint(0);

                IQueryPrx proxy = entryUnencrypted.getQueryService();
                List<IObject> tags = (List<IObject>)
                    proxy.findAllByString("TagAnnotation", "ns", tagName, caseSensitive, filter);
                System.out.println("\nList Tags:");
                for (IObject obj : tags)
                {
                    TagAnnotation t = (TagAnnotation) obj;
                    System.out.println("ID: " + t.getId().getValue() + " NS: " + t.getNs().getValue());

                }
	}

	/**
	 * Connects and invokes the various methods.
	 */
	ReadDataAdvanced()
	{
		try {
			connect(); // First connect.
                        createDatasets();
                        createTags();
			loadDatasetsByName();
			loadTagsByNS();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				disconnect(); // Be sure to disconnect
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args)
	{
		new ReadDataAdvanced();
		System.exit(0);
	}

}
