/*
 * org.openmicroscopy.shoola.env.data.NullDataManagementService
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.env.data;

//Java imports
import java.util.List;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.DSAccessException;
import org.openmicroscopy.shoola.env.data.DSOutOfServiceException;
import org.openmicroscopy.shoola.env.data.model.ActualInputData;
import org.openmicroscopy.shoola.env.data.model.AnalysisChainData;
import org.openmicroscopy.shoola.env.data.model.AnalysisLinkData;
import org.openmicroscopy.shoola.env.data.model.AnalysisNodeData;
import org.openmicroscopy.shoola.env.data.model.ChainExecutionData;
import org.openmicroscopy.shoola.env.data.model.DataObject;
import org.openmicroscopy.shoola.env.data.model.DatasetData;
import org.openmicroscopy.shoola.env.data.model.DatasetSummary;
import org.openmicroscopy.shoola.env.data.model.DatasetSummaryLinked;
import org.openmicroscopy.shoola.env.data.model.FormalInputData;
import org.openmicroscopy.shoola.env.data.model.FormalOutputData;
import org.openmicroscopy.shoola.env.data.model.ImageData;
import org.openmicroscopy.shoola.env.data.model.ImageSummary;
import org.openmicroscopy.shoola.env.data.model.ModuleCategoryData;
import org.openmicroscopy.shoola.env.data.model.ModuleData;
import org.openmicroscopy.shoola.env.data.model.ModuleExecutionData;
import org.openmicroscopy.shoola.env.data.model.NodeExecutionData;
import org.openmicroscopy.shoola.env.data.model.ProjectData;
import org.openmicroscopy.shoola.env.data.model.ProjectSummary;
import org.openmicroscopy.shoola.env.data.model.SemanticTypeData;
import org.openmicroscopy.shoola.env.data.model.UserDetails;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class NullDataManagementService
        implements DataManagementService
{

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#getUserDetails()
     */
    public UserDetails getUserDetails()
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#getSessionKey()
     */
    public String getSessionKey()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#retrieveUserProjects(org.openmicroscopy.shoola.env.data.model.ProjectSummary, org.openmicroscopy.shoola.env.data.model.DatasetSummary)
     */
    public List retrieveUserProjects(ProjectSummary pProto, DatasetSummary dProto)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#retrieveUserProjects()
     */
    public List retrieveUserProjects()
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#retrieveUserProjectsWithDAnnotations()
     */
    public List retrieveUserProjectsWithDAnnotations()
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#retrieveUserProjectsWithDAnnotations(org.openmicroscopy.shoola.env.data.model.ProjectSummary, org.openmicroscopy.shoola.env.data.model.DatasetSummary)
     */
    public List retrieveUserProjectsWithDAnnotations(ProjectSummary pProto, DatasetSummary dProto)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#retrieveUserProjectsWithDatasetData(org.openmicroscopy.shoola.env.data.model.ProjectSummary, org.openmicroscopy.shoola.env.data.model.DatasetData)
     */
    public List retrieveUserProjectsWithDatasetData(ProjectSummary pProto, DatasetData dProto)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#retrieveUserProjectsWithDatasetData()
     */
    public List retrieveUserProjectsWithDatasetData()
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#retrieveUserDatasets(org.openmicroscopy.shoola.env.data.model.DatasetSummary)
     */
    public List retrieveUserDatasets(DatasetSummary dProto)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#retrieveUserDatasets()
     */
    public List retrieveUserDatasets()
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#fullRetrieveUserDatasets(org.openmicroscopy.shoola.env.data.model.DatasetData, org.openmicroscopy.shoola.env.data.model.ImageSummary)
     */
    public List fullRetrieveUserDatasets(DatasetData dProto, ImageSummary iProto)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#retrieveUserImages(org.openmicroscopy.shoola.env.data.model.ImageSummary)
     */
    public List retrieveUserImages(ImageSummary iProto)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#retrieveUserImages()
     */
    public List retrieveUserImages()
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#retrieveImagesInUserGroup()
     */
    public List retrieveImagesInUserGroup()
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#retrieveImagesInUserGroup(org.openmicroscopy.shoola.env.data.model.ImageSummary)
     */
    public List retrieveImagesInUserGroup(ImageSummary iProto)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#retrieveImagesInUserDatasets(java.util.List)
     */
    public List retrieveImagesInUserDatasets(List datasetIDs)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#retrieveImagesInUserDatasets(java.util.List, org.openmicroscopy.shoola.env.data.model.ImageSummary)
     */
    public List retrieveImagesInUserDatasets(List datasetIDs, ImageSummary iProto)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#retrieveImagesInUserDatasets()
     */
    public List retrieveImagesInUserDatasets()
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#retrieveImagesInSystem()
     */
    public List retrieveImagesInSystem()
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#retrieveImagesInSystem(org.openmicroscopy.shoola.env.data.model.ImageSummary)
     */
    public List retrieveImagesInSystem(ImageSummary iProto)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#retrieveProject(int, org.openmicroscopy.shoola.env.data.model.ProjectData)
     */
    public ProjectData retrieveProject(int projectID, ProjectData retVal)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#retrieveProject(int)
     */
    public ProjectData retrieveProject(int projectID)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#retrieveDataset(int, org.openmicroscopy.shoola.env.data.model.DatasetData)
     */
    public DatasetData retrieveDataset(int datasetID, DatasetData retVal)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#retrieveDataset(int)
     */
    public DatasetData retrieveDataset(int datasetID)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#retrieveImage(int, org.openmicroscopy.shoola.env.data.model.ImageData)
     */
    public ImageData retrieveImage(int id, ImageData retVal)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#retrieveImage(int)
     */
    public ImageData retrieveImage(int id)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#retrieveImages(int)
     */
    public List retrieveImages(int datasetID)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#retrieveImages(int, org.openmicroscopy.shoola.env.data.model.ImageSummary)
     */
    public List retrieveImages(int datasetID, ImageSummary iProto)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#retrieveImagesWithAnnotations(int)
     */
    public List retrieveImagesWithAnnotations(int datasetID)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#retrieveImagesWithAnnotations(int, org.openmicroscopy.shoola.env.data.model.ImageSummary)
     */
    public List retrieveImagesWithAnnotations(int datasetID, ImageSummary iProto)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#retrieveModules(org.openmicroscopy.shoola.env.data.model.ModuleData, org.openmicroscopy.shoola.env.data.model.ModuleCategoryData, org.openmicroscopy.shoola.env.data.model.FormalInputData, org.openmicroscopy.shoola.env.data.model.FormalOutputData, org.openmicroscopy.shoola.env.data.model.SemanticTypeData)
     */
    public List retrieveModules(ModuleData mProto, ModuleCategoryData mcProto, FormalInputData finProto, FormalOutputData foutProto, SemanticTypeData stProto)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#retrieveModuleCategories()
     */
    public List retrieveModuleCategories()
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#retrieveModuleCategories(org.openmicroscopy.shoola.env.data.model.ModuleCategoryData, org.openmicroscopy.shoola.env.data.model.ModuleData)
     */
    public List retrieveModuleCategories(ModuleCategoryData mcProto, ModuleData mProto)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#retrieveModules()
     */
    public List retrieveModules()
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#retrieveChains(org.openmicroscopy.shoola.env.data.model.AnalysisChainData, org.openmicroscopy.shoola.env.data.model.AnalysisLinkData, org.openmicroscopy.shoola.env.data.model.AnalysisNodeData, org.openmicroscopy.shoola.env.data.model.ModuleData, org.openmicroscopy.shoola.env.data.model.FormalInputData, org.openmicroscopy.shoola.env.data.model.FormalOutputData, org.openmicroscopy.shoola.env.data.model.SemanticTypeData)
     */
    public List retrieveChains(AnalysisChainData acProto, AnalysisLinkData alProto, AnalysisNodeData anProto, ModuleData modProto, FormalInputData finProto, FormalOutputData foutProto, SemanticTypeData stProto)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#retrieveChains()
     */
    public List retrieveChains()
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#retrieveChain(int)
     */
    public AnalysisChainData retrieveChain(int id)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#retrieveChain(int, org.openmicroscopy.shoola.env.data.model.AnalysisChainData, org.openmicroscopy.shoola.env.data.model.AnalysisLinkData, org.openmicroscopy.shoola.env.data.model.AnalysisNodeData, org.openmicroscopy.shoola.env.data.model.ModuleData, org.openmicroscopy.shoola.env.data.model.FormalInputData, org.openmicroscopy.shoola.env.data.model.FormalOutputData, org.openmicroscopy.shoola.env.data.model.SemanticTypeData)
     */
    public AnalysisChainData retrieveChain(int id, AnalysisChainData acProto, AnalysisLinkData alProto, AnalysisNodeData anProto, ModuleData modProto, FormalInputData finProto, FormalOutputData foutProto, SemanticTypeData stProto)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#retrieveChainExecutions(org.openmicroscopy.shoola.env.data.model.ChainExecutionData, org.openmicroscopy.shoola.env.data.model.DatasetData, org.openmicroscopy.shoola.env.data.model.AnalysisChainData, org.openmicroscopy.shoola.env.data.model.NodeExecutionData, org.openmicroscopy.shoola.env.data.model.AnalysisNodeData, org.openmicroscopy.shoola.env.data.model.ModuleData, org.openmicroscopy.shoola.env.data.model.ModuleExecutionData)
     */
    public List retrieveChainExecutions(ChainExecutionData ceProto, DatasetData dsProto, AnalysisChainData acProto, NodeExecutionData neProto, ModuleData mProto, ModuleExecutionData meProto)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#retrieveChainExecutions()
     */
    public List retrieveChainExecutions()
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#createProject(org.openmicroscopy.shoola.env.data.model.ProjectData, org.openmicroscopy.shoola.env.data.model.ProjectSummary)
     */
    public ProjectSummary createProject(ProjectData retVal, ProjectSummary pProto)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#createDataset(java.util.List, java.util.List, org.openmicroscopy.shoola.env.data.model.DatasetData, org.openmicroscopy.shoola.env.data.model.DatasetSummary)
     */
    public DatasetSummary createDataset(List projectSummaries, List imageSummaries, DatasetData retVal, DatasetSummary dProto)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#createProject(org.openmicroscopy.shoola.env.data.model.ProjectData)
     */
    public ProjectSummary createProject(ProjectData retVal)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#createDataset(java.util.List, java.util.List, org.openmicroscopy.shoola.env.data.model.DatasetData)
     */
    public DatasetSummary createDataset(List projectSummaries, List imageSummaries, DatasetData retVal)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#createAnalysisChain(org.openmicroscopy.shoola.env.data.model.AnalysisChainData)
     */
    public void createAnalysisChain(AnalysisChainData retVal)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#updateProject(org.openmicroscopy.shoola.env.data.model.ProjectData, java.util.List, java.util.List)
     */
    public void updateProject(ProjectData retVal, List dsToRemove, List dsToAdd)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#updateDataset(org.openmicroscopy.shoola.env.data.model.DatasetData, java.util.List, java.util.List)
     */
    public void updateDataset(DatasetData retVal, List isToRemove, List isToAdd)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#updateImage(org.openmicroscopy.shoola.env.data.model.DataObject)
     */
    public void updateImage(DataObject retVal)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        
    }

    /*
     *  (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#
     * getMexExecutionHistory()
     */
    public List getMexExecutionHistory(int mexID,ModuleExecutionData mexData,
    			ModuleData modData,ActualInputData inpData,FormalInputData finData,
			FormalOutputData foutData,SemanticTypeData stData) 
		throws DSOutOfServiceException, DSAccessException
	{
        // TODO Auto-generated method stub
        return null;	
	}
    
    /*
     *  (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#
     * getMexExecutionHistory()
     */
    public List getMexExecutionHistory(int mexID) 
		throws DSOutOfServiceException, DSAccessException
	{
	    // TODO Auto-generated method stub
	   return null;	
	}
    
    /*
     *  (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#
     * getChainExecutionHistory()
     */
    public List getChainExecutionHistory(int mexID,ModuleExecutionData mexData,
    			ModuleData modData,ActualInputData inpData,FormalInputData finData,
				FormalOutputData foutData,SemanticTypeData stData) 
		throws DSOutOfServiceException, DSAccessException
	{
	    // TODO Auto-generated method stub
	   return null;	
	}    

    /*
     *  (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#
     * getChainExecutionHistory()
     */    
    public List getChainExecutionHistory(int mexID) 
		throws DSOutOfServiceException, DSAccessException 
	{
	    // TODO Auto-generated method stub
	   return null;	
	}


    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#retrieveUserImages(org.openmicroscopy.shoola.env.data.model.ImageSummary, java.util.Map, java.util.Map)
     */
    public List retrieveUserImages(ImageSummary iProto, Map filters, Map complexFilters)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#retrieveUserImages(java.util.Map, java.util.Map)
     */
    public List retrieveUserImages(Map filters, Map complexFilters)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#retrieveImagesInUserGroup(java.util.Map, java.util.Map)
     */
    public List retrieveImagesInUserGroup(Map filters, Map complexFilters)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#retrieveImagesInUserGroup(org.openmicroscopy.shoola.env.data.model.ImageSummary, java.util.Map, java.util.Map)
     */
    public List retrieveImagesInUserGroup(ImageSummary iProto, Map filters, Map complexFilters)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#retrieveImagesInUserDatasets(java.util.List, org.openmicroscopy.shoola.env.data.model.ImageSummary, java.util.Map, java.util.Map)
     */
    public List retrieveImagesInUserDatasets(List datasetIDs, ImageSummary iProto, Map filters, Map complexFilters)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#retrieveImagesInUserDatasets(java.util.List, java.util.Map, java.util.Map)
     */
    public List retrieveImagesInUserDatasets(List datasetIDs, Map filters, Map complexFilters)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#retrieveImagesInUserDatasets(java.util.Map, java.util.Map)
     */
    public List retrieveImagesInUserDatasets(Map filters, Map complexFilters)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#retrieveImagesInSystem(org.openmicroscopy.shoola.env.data.model.ImageSummary, java.util.Map, java.util.Map)
     */
    public List retrieveImagesInSystem(ImageSummary iProto, Map filters, Map complexFilters)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#retrieveImagesInSystem(java.util.Map, java.util.Map)
     */
    public List retrieveImagesInSystem(Map filters, Map complexFilters)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }


    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#retrieveProjectsTree(java.util.List, boolean)
     */
    public List retrieveProjectsTree(List projectIDs, boolean annotated)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#retrieveProjectTree(int, boolean)
     */
    public ProjectSummary retrieveProjectTree(int projectID, boolean annotated)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#retrieveDatasetsTree(java.util.List, boolean)
     */
    public List retrieveDatasetsTree(List datasetIDs, boolean annotated)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#retrieveDatasetTree(int, boolean)
     */
    public DatasetSummaryLinked retrieveDatasetTree(int datasetID, 
            boolean annotated)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#retrieveIDPHierarchy(java.util.List, boolean)
     */
    public List retrievePDIHierarchy(List imageSummaries, boolean annotated)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }    
		
    
}
