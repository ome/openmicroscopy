/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.procs;

import ome.formats.OMEROMetadataStore;
import ome.formats.importer.ImportContainer;
import ome.formats.importer.ImportLibrary;
import ome.formats.importer.OMEROWrapper;

import ome.services.procs.Processor;
import ome.system.OmeroContext;
import ome.system.ServiceFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


/**
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta2
 */
public class ImportProcessor implements ApplicationContextAware, Processor {

    private OmeroContext context;
    private OMEROMetadataStore store;
    private OMEROWrapper reader;
    private ImportLibrary library;

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
	try {
	    this.context = (OmeroContext) applicationContext;
	    this.store = new OMEROMetadataStore(new ServiceFactory(context));
	    this.reader = new OMEROWrapper(); 
	} catch (Exception e) {
	    throw new FatalBeanException("Error creating ImportProcessor",e);
	}
    }

    public Process process(long id) {
	try {
	    this.library = new ImportLibrary(store, reader, new ImportContainer[]{});	
	    for (ImportContainer c : library.getFilesAndDatasets()) {
		library.setDataset(c.dataset);
		String filename = c.file.getAbsolutePath();
		library.open(filename);
		library.calculateImageCount(filename);
		long pixId = library.importMetadata(filename);
		library.importData(pixId, filename, null);//step);
	    }
	} catch (Exception e) {
	    System.err.println("XXXXXXXXXXXXX << WRITE TO MSG >> XXXXXXXXXXXXXXX");
	}
        return null;

    }

}
