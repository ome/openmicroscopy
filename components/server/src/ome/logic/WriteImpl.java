/*
 * ome.logic.WriteImpl
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
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

/*------------------------------------------------------------------------------
 *
 * Written by:    Josh Moore <josh.moore@gmx.de>
 *
 *------------------------------------------------------------------------------
 */

package ome.logic;

//Java imports

//Third-party libraries
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//Application-internal dependencies
import ome.api.Write;
import ome.dao.GenericDao;
import ome.model.Dataset;
import ome.model.DatasetAnnotation;
import ome.model.Experimenter;
import ome.model.Module;
import ome.model.ModuleExecution;
import ome.model.SemanticType;
import ome.model.SemanticTypeOutput;

import org.springframework.jdbc.core.JdbcTemplate;


/**
 * implementation a writing service.
 * @author Josh Moore, <a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since OMERO 1.0
 */
public class WriteImpl implements Write {

    private static Log log = LogFactory.getLog(WriteImpl.class);
    
    JdbcTemplate template;
    GenericDao gdao;
    
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate){
    	this.template = jdbcTemplate;
    }
   
    public void setGenericDao(GenericDao genericDao){
    	this.gdao = genericDao;
    }
    
	public void createDatasetAnnotation(Integer datasetId, String content) {
		doHibernate(datasetId,content);
	}
	
	protected void doHibernate(Integer datasetId, String content){
		content = "Hibernate:"+content;
		
		Dataset ds = (Dataset) gdao.getById(Dataset.class, datasetId);
		Module annModule = (Module) gdao.getByName(Module.class, "Annotation");
		SemanticType dannST = (SemanticType) gdao.getByName(SemanticType.class, "DatasetAnnotation");

		//TODO this should be in a AdminService/Dao
		int rootExperimenterId = template.queryForInt(
				"SELECT attribute_id FROM experimenters ORDER BY attribute_id LIMIT 1"			
		);
		Experimenter root = (Experimenter) gdao.getById(Experimenter.class, rootExperimenterId);
		
		String message = getMessage();
		
		ModuleExecution mex = new ModuleExecution();
		mex.setExperimenter(root);
		mex.setStatus("EXECUTING");
		mex.setModule(annModule);
		mex.setDataset(ds);
		mex.setDependence("D");
		mex.setErrorMessage(message);
		mex.setVirtualMex(false);
		
		DatasetAnnotation newAnnotation = new DatasetAnnotation();
		newAnnotation.setModuleExecution(mex);
		newAnnotation.setDataset(ds);
		newAnnotation.setContent(content);

		SemanticTypeOutput stOutput = new SemanticTypeOutput();
		stOutput.setSemanticType(dannST);
		stOutput.setModuleExecution(mex);

		gdao.persist(new Object[]{mex,newAnnotation,stOutput});
		mex.setStatus("FINISHED");
		mex.setErrorMessage(null);
		gdao.persist(new Object[]{mex});
		//Don't actually need to change Error message!
		//Go back to using cacheableMapping ! cacheableMappingDirectoryLocation?
		

		
	}
	
	protected void doJdbc(Integer datasetId, String content){ 
		content = "JDBC:"+content;
		
		int annotationModuleId = WriteImpl.getAnnnotationModuleId(template);	
	
		//TODO this should be any user!
		int rootExperimenterId = template.queryForInt(
				"SELECT attribute_id FROM experimenters ORDER BY attribute_id LIMIT 1"			
		);

		int semanticTypeId = template.queryForInt(
				"SELECT semantic_type_id FROM semantic_types WHERE name = 'DatasetAnnotation'"			
		);
	
		String message = "OMEROW: AddingAnnotation"+this.hashCode()+System.currentTimeMillis();

		template.update(
				"INSERT INTO module_executions " +
				"(experimenter_id, status, module_id, dataset_id, dependence, error_message) " +
				" VALUES " +
				"(?, 'EXECUTING', ?, ?, 'D', ?)",
				new Object[]{
						new Integer(rootExperimenterId),
						new Integer(annotationModuleId),
						new Integer(datasetId),
						message
				}
		);
		
		int mexId = template.queryForInt(
			"SELECT module_execution_id FROM module_executions WHERE error_message = ?",
			new Object[]{message}
		);
		/*
		 * 
	   Column        |  Type   |                    Modifiers                    
---------------------+---------+-------------------------------------------------
 attribute_id        | integer | not null default nextval('attribute_seq'::text)
 content             | text    | 
 module_execution_id | integer | not null
 dataset_id          | integer | not null
 valid               | boolean | 
		 */
		template.update(
			"insert INTO dataset_annotations (module_execution_id, dataset_id, content) " +
			"VALUES (?,?,?)",
			new Object[]{new Integer(mexId), new Integer(datasetId), content}
		);

		
		template.update(
				"INSERT INTO semantic_type_outputs (semantic_type_id, module_execution_id) " +
				"VALUES (?, ?)",
				new Object[]{new Integer(semanticTypeId),new Integer(mexId)}
		);
		
		template.update(
				"UPDATE module_executions SET status = 'FINISHED', error_message = NULL WHERE error_message = ?",
				new Object[]{message}
		);
		
		
	}

	/* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	 *  Static methods for elsewhere!
	 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
		
	 public static int getAnnnotationModuleId(JdbcTemplate template) {
		int annotationModuleId = Integer.parseInt((String) template.queryForObject(
			"SELECT value FROM configuration WHERE name = 'annotation_module_id'", String.class
		));
		return annotationModuleId;
	}
	 
	 public String getMessage(){
		 return "OMEROW: AddingAnnotation"+this.hashCode()+System.currentTimeMillis();
	 }
	
}