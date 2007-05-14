/*
* ome.admin.controller
*
*   Copyright 2007 University of Dundee. All rights reserved.
*   Use is subject to license terms supplied in LICENSE.txt
*/

package ome.admin.controller;

//Java imports
import java.util.HashMap;

import org.jfree.data.general.DefaultPieDataset;

// Third-party libraries

// Application-internal dependencies
import ome.admin.logic.IRepositoryInfoManagerDelegator;

/**
 * It's the Java bean with fife attributes and setter/getter and actions methods. The bean captures pie chart values. This way the bean provides a bridge between the JSP page and the application logic.
 * @author Aleksandra Tarkowska &nbsp;&nbsp;&nbsp;&nbsp; <a href="mailto:A.Tarkowska@dundee.ac.uk">A.Tarkowska@dundee.ac.uk</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Revision$Date: $)</small>
 * @since OME3.0
 */
public class IRepositoryInfoPieChartController implements java.io.Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

    /**
	 * {@link org.jfree.data.general.DefaultPieDataset}
	 */
	private DefaultPieDataset pieDataSet = new DefaultPieDataset();

	/**
	 * {@link ome.admin.logic.IRepositoryInfoManagerDelegator}
	 */
    private IRepositoryInfoManagerDelegator iRepository = new IRepositoryInfoManagerDelegator();

    /** 
     * Creates a new instance of IRepositoryInfoPieChartController.
     */
    public IRepositoryInfoPieChartController () {    
    }

    /**
     * Gets data for Pie Chart
	 * @return {@link ome.admin.logic.IRepositoryInfoManagerDelegator}
	 */
    public DefaultPieDataset getPieDataSet() {
    	try {
	        this.pieDataSet.setValue("Free space", iRepository.getSpaceInKilobytes("free"));
	        //this.pieDataSet.setValue("Used space", iRepository.getSpaceInKilobytes("used")); 
	        HashMap map = iRepository.getTopTenUserSpace();      
	        for (Object expid : map.keySet()) {
	        	this.pieDataSet.setValue(((String) expid), ((Long) map.get(expid))/1024);
	        }
    	} catch (Exception e) {
			this.pieDataSet.setValue("No data set", 0L);
    	}
        return this.pieDataSet;
    }


}