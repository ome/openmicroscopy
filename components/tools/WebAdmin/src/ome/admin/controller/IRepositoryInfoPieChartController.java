/*
 * ome.admin.controller
 *
 *   Copyright 2007 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.admin.controller;

// Java imports
import java.util.HashMap;

//Third-party libraries
import org.jfree.data.general.DefaultPieDataset;

//Application-internal dependencies
import ome.admin.logic.IRepositoryInfoManagerDelegator;

/**
 * It's the Java bean with fife attributes and setter/getter and actions
 * methods. The bean captures pie chart values. This way the bean provides a
 * bridge between the JSP page and the application logic.
 * 
 * @author Aleksandra Tarkowska &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:A.Tarkowska@dundee.ac.uk">A.Tarkowska@dundee.ac.uk</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Revision$Date: $)</small>
 * @since OME3.0
 */
public class IRepositoryInfoPieChartController {

	/**
	 * {@link org.jfree.data.general.DefaultPieDataset}
	 */
	private DefaultPieDataset pieDataSet = new DefaultPieDataset();

	/**
	 * {@link ome.admin.logic.IRepositoryInfoManagerDelegator}
	 */
	private IRepositoryInfoManagerDelegator iRepository = new IRepositoryInfoManagerDelegator();

	private Long freeSpace = 0L;

	private Long usedSpace = 0L;

	/**
	 * Creates a new instance of IRepositoryInfoPieChartController.
	 */
	public IRepositoryInfoPieChartController() {
		this.freeSpace = iRepository.getSpaceInKilobytes("free");
		this.usedSpace = iRepository.getSpaceInKilobytes("used");
	}

	/**
	 * Gets value of free space
	 * 
	 * @return long freeSpace
	 */
	public Long getFreeSpace() {
		return freeSpace;
	}

	/**
	 * Sets value of free space
	 * 
	 * @param freeSpace
	 */
	public void setFreeSpace(Long freeSpace) {
		this.freeSpace = freeSpace;
	}

	/**
	 * Gets value of used space
	 * 
	 * @return long usedSpace
	 */
	public Long getUsedSpace() {
		return usedSpace;
	}

	/**
	 * Sets value of used space
	 * 
	 * @param usedSpace
	 */
	public void setUsedSpace(Long usedSpace) {
		this.usedSpace = usedSpace;
	}

	/**
	 * Gets data for Pie Chart
	 * 
	 * @return {@link ome.admin.logic.IRepositoryInfoManagerDelegator}
	 */
	public DefaultPieDataset getPieDataSet() {
		try {
			this.pieDataSet.setValue("Free space", iRepository
					.getSpaceInKilobytes("free"));
			//this.pieDataSet.setValue("Used space", iRepository.getSpaceInKilobytes("used"));
			HashMap map = iRepository.getTopTenUserSpace();
			for (Object expid : map.keySet()) {
				this.pieDataSet.setValue(((String) expid), ((Long) map
						.get(expid)) / 1024);
			}
		} catch (Exception e) {
			this.pieDataSet.setValue("No data set", 0L);
		}
		return this.pieDataSet;
	}

}