/*
 * ome.dao.hibernate.queries.PojosQueryBuilder
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
package ome.dao.hibernate.queries;

//Java imports
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.channels.FileLockInterruptionException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeConstants;

//Third-party libraries

//Application-internal dependencies
import ome.model.Category;
import ome.model.CategoryGroup;
import ome.model.Classification;
import ome.model.Dataset;
import ome.model.DatasetAnnotation;
import ome.model.Image;
import ome.model.ImageAnnotation;
import ome.model.Project;
import ome.tools.StringUtils;
import ome.util.builders.PojoOptions;


public abstract class PojosQueryBuilder {

	private static Log log = LogFactory.getLog(PojosQueryBuilder.class);
	
	final static String base = "ome/dao/hibernate/queries/pojos_";
	final static VelocityEngine ve = new VelocityEngine();
	static {
		ve.setProperty( RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, "org.apache.velocity.runtime.log.SimpleLog4JLogSystem" );
		ve.setProperty("runtime.log.logsystem.log4j.category", "velocity");
    	ve.setProperty("resource.loader","file, class");
    	ve.setProperty("class.resource.loader.class","org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
    	ve.setProperty( RuntimeConstants.VM_LIBRARY,base+"macros.vm");
    	try {
			ve.init();
		} catch (Exception e) {
			throw new RuntimeException("Velocity initialization exception.");
		}
	}

	private static String invoke(VContext vc,String template){
        RuntimeException e = new RuntimeException("Error in creating query.");
		try {
        	StringWriter sw = new StringWriter();
        	FileReader fr = new FileReader(new File(PojosQueryBuilder.class.getClassLoader().getResource(base+template+".vm").getFile()));
        	log.debug(Arrays.asList(vc.getContext().getKeys()));
        	ve.evaluate(vc.getContext(),sw,"Running template: "+template,fr);
        	return sw.toString();
        } catch (FileLockInterruptionException fne){
        	e.initCause(fne);
        	throw e;
        } catch (IOException ioe){
        	e.initCause(ioe);
        	throw e;
        } catch (ParseErrorException pee) {
        	e.initCause(pee);
        	throw e;
		} catch (MethodInvocationException mie) {
        	e.initCause(mie);
        	throw e;
		} catch (ResourceNotFoundException rnfe) {
        	e.initCause(rnfe);
        	throw e;
		}
        
	}

	public static String buildLoadQuery(Class target, boolean nullIds, Map options) {
		VContext vc = new VContext(target,options);
		if (nullIds) vc.noIds(); 
    	return invoke(vc,"load");
	}
	
	public static String buildFindQuery(Class target, Map options) {
		VContext vc = new VContext(target, options);
    	return invoke(vc,"find");
	}
	
	public static String buildPathsQuery(String algorithm, Map options) {
		VContext vc = new VContext(CategoryGroup.class,options);
		vc.doString(algorithm);
    	return invoke(vc,"paths");
	}
	
	public static String buildAnnsQuery(Class target, Map options) {
		VContext vc = new VContext(null,options);		
		vc.doClass(target);
		return invoke(vc,"anns");
	}

	public static String buildGetQuery(Class target, Map options) {
		VContext vc = new VContext(target,options);
		vc.doClassList(VContext.anns); // TODO perhaps doAnnotations()
		if (target.equals(Image.class)){ // FIXME
			vc.noIds();
		}
    	return invoke(vc,"get");
	}

	static class VContext {

		private final static Map<Class,String> abbrevs = new HashMap<Class,String>();
		static {
			abbrevs.put(Project.class,"p");
			abbrevs.put(Dataset.class,"d");
			abbrevs.put(CategoryGroup.class,"cg");
			abbrevs.put(Category.class,"c");
			abbrevs.put(DatasetAnnotation.class,"d_ann");
			abbrevs.put(ImageAnnotation.class,"i_ann");
		}
		
		private final static java.util.List pdi = Arrays.asList(new Class[]{Project.class,Dataset.class, Image.class});
		private final static java.util.List cgci = Arrays.asList(new Class[]{CategoryGroup.class, Category.class, Classification.class, Image.class});
		private final static java.util.List anns = Arrays.asList(new Class[]{ImageAnnotation.class, DatasetAnnotation.class});

		VelocityContext vc = new VelocityContext();
		VContext(Class target, Map options){
			if (null != target){ // TODO this is for Anns. If that moves to another queryBuilder can simplify
				vc.put("class",target.getName());
				vc.put("abbrev",abbrevs.get(target));
				checkList(pdi,target);
				checkList(cgci,target);
			}
			parseOptions(options);
		}
		Context getContext(){return vc;}
		
		// other
		VContext doString(String s) { return turnOn("do"+s);}
		VContext noIds(){ return turnOn("noIds");}
        VContext noLeaves() { return turnOn("noLeaves").turnOff("doImage"); }
		VContext doExperimenter(){ return turnOn("doExperimenter");}
        VContext doGroup(){ return turnOn("doGroup");}
        VContext doAnnotationOwner(){ return turnOn("doAnnotationOwner)");}
		
		// Class based
		VContext doProject(){ return turnOn("doProject");}
		VContext doDataset(){ return turnOn("doDataset");}
		VContext doCategoryGroup(){ return turnOn("doCategoryGroup");}
		VContext doCategory(){ return turnOn("doCategory");}
		VContext doClass(Class c){ return turnOn("do"+StringUtils.getClassName(c));}
		VContext doClassList(List<Class> l) {
			for (Class c: l){
				doClass(c);
			}
			return this;
		}

		VContext turnOn(String s){
			vc.put(s,Boolean.TRUE);
			return this;
		}
        
        VContext turnOff(String s){
            vc.put(s,Boolean.FALSE);
            return this;
        }
		
		void parseOptions(Map options){
			PojoOptions po = new PojoOptions(options);
			if (po.isAnnotation()) doClassList(anns);
			if (! po.isAllAnnotations()) doAnnotationOwner();
			if (po.isExperimenter()) doExperimenter();
            if (po.isGroup()) doGroup();
            if (!po.isLeaves()) noLeaves();
		}

		void checkList(List<Class> list, Class target){
			int index = list.indexOf(target);
			if (index > -1) {
				List<Class> sub = list.subList(index,list.size());
				doClassList(sub);
			}
		}
		
	}
	
}
