/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.procs;

import ome.formats.OMEROMetadataStore;
import ome.formats.importer.ImportLibrary;
import ome.formats.importer.OMEROWrapper;
import ome.system.OmeroContext;
import ome.system.ServiceFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

    private final static Log log = LogFactory.getLog(ImportProcessor.class);

    private OmeroContext context;
    private OMEROMetadataStore store;
    private OMEROWrapper reader;
    private ImportLibrary library;

    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        try {
            this.context = (OmeroContext) applicationContext;
            this.store = new OMEROMetadataStore(new ServiceFactory(context));
            this.reader = null; // new OMEROWrapper(); WORKAROUND for NPE
        } catch (Exception e) {
            throw new FatalBeanException("Error creating ImportProcessor", e);
        }
    }

    public Process process(long id) {
        try {
            log.warn("NO-OP");
            // this.library = new ImportLibrary(store, reader);
            // for (ImportContainer c : library.getFilesAndDatasets()) {
            // library.setDataset(c.getDataset());
            // String filename = c.file.getAbsolutePath();
            // library.open(filename);
            // Needs synchronization with importer library
            // library.calculateImageCount(filename);
            // long pixId = library.importMetadata(filename);
            // library.importData(pixId, filename, null);//step);
            // }
        } catch (Exception e) {
            log.error(e);
        }
        return null;

    }

}
