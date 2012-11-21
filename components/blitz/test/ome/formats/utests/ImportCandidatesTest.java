/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.formats.utests;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import ome.formats.OMEROMetadataStoreClient;
import ome.formats.importer.IObservable;
import ome.formats.importer.IObserver;
import ome.formats.importer.ImportCandidates;
import ome.formats.importer.ImportConfig;
import ome.formats.importer.ImportContainer;
import ome.formats.importer.ImportEvent;
import ome.formats.importer.ImportFixture;
import ome.formats.importer.ImportLibrary;
import ome.formats.importer.OMEROWrapper;
import ome.formats.importer.ImportCandidates.SCANNING;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.Test;

/**
 * candidate processing tests.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @see ImportFixture
 * @see ImportReader
 * @see ImportConfig
 * @see ImportLibrary
 * @see ImportCandidates
 * @see OMEROWrapper
 * @see OMEROMetadataStoreClient
 * @see IObserver
 *
 * @since Beta4.1
 */
public class ImportCandidatesTest extends TestCase {

    final List<SCANNING> scannings = new ArrayList<SCANNING>();
    Log log = LogFactory.getLog(ImportCandidatesTest.class);
    ImportCandidates c;

    ImportConfig config = new ImportConfig();
    OMEROWrapper w = new OMEROWrapper(config);
    IObserver o = new IObserver() {
        public void update(IObservable importLibrary, ImportEvent event) {
            if (event instanceof ImportCandidates.SCANNING) {
                ImportCandidates.SCANNING s = (ImportCandidates.SCANNING) event;
                scannings.add(s);
            }
        }
    };

    static class Canceler implements IObserver {
        public int count = 0;
        public void update(IObservable importLibrary, ImportEvent event) {
            if (event instanceof ImportCandidates.SCANNING) {
                ImportCandidates.SCANNING s = (ImportCandidates.SCANNING) event;
                count++;
                s.cancel();
            }
        }
    };

    private void basic(IObserver obs) {
        URL url = getClass().getResource(getClass().getSimpleName() + ".class");
        String thisClass = url.getFile();
        File file = new File(thisClass);
        file = file.getParentFile().getParentFile().getParentFile();
        c = new ImportCandidates(w, new String[] { file.getAbsolutePath() },
                obs);
    }

    private ImportContainer container(String...usedFiles) {
        File file = new File("a");
        Long projectID = 0L;
        String imageName = "";
        String reader = "";
        Boolean archive = false;
        Boolean isSPW = false;
        omero.model.IObject target = null;
        return new ImportContainer(file, projectID, target, archive,
                null, reader, usedFiles, isSPW);

    }

    @Test
    public void testTwoPasses() throws Exception {
        basic(o);
        // Nothing valid. assertTrue(c.size() > 0);
        assertTrue(scannings.size() > 0);
    }

    @Test
    public void testCancelFunctions() throws Exception {
        Canceler cancel = new Canceler();
        basic(cancel);
        assertEquals(0, c.size());
        assertEquals(1, cancel.count);
        assertTrue(c.wasCancelled());
    }

    @Test
    public void testOrderedReturns() {
        c = new ImportCandidates(w, new String[]{"a","b"}, o) {
            @Override
            protected ImportContainer singleFile(File file, ImportConfig config) {
                return container(file.getName());
            }
        };
    }

}
