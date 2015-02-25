/*
 *   $Id$
 *
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.formats.utests;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import ome.formats.importer.IObservable;
import ome.formats.importer.ImportConfig;
import ome.formats.importer.ImportEvent;
import ome.formats.importer.util.ErrorContainer;
import ome.formats.importer.util.ErrorHandler;
import ome.formats.importer.util.HtmlMessengerException;

import org.testng.annotations.Test;

/**
 * Various configuration workflows
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4.4
 */
@Test
public class ErrorHandlerTest extends TestCase {

    class MyErrorHandler extends ErrorHandler {

        private final boolean _sendFiles;

        private final boolean _sendLog;

        private Integer posts = null;

        private Integer uploads = null;

        public MyErrorHandler(ImportConfig config, boolean sendFiles,
                boolean sendLog) {
            super(config);
            this._sendFiles = sendFiles;
            this._sendLog = sendLog;
        }

        @Override
        public void executePost(String sendUrl, Map<String, String> postList)
                throws HtmlMessengerException {

            List<String> selectedFiles = new ArrayList<String>();
            for (int i = 0; i < postList.size(); i++) {
                String part = postList.get(i);
                if (part.equals("selected_file")) {
                    selectedFiles.add(""+part);
                }
            }
            posts = selectedFiles.size();
        }

        @Override
        public void uploadFile(ErrorContainer errorContainer) {
            uploads = errorContainer.getFiles().length;
        }

        @Override
        protected void onUpdate(IObservable observable, ImportEvent event) {
            if (event instanceof ImportEvent.DEBUG_SEND) {
                ImportEvent.DEBUG_SEND send = (ImportEvent.DEBUG_SEND) event;
                assertEquals(_sendFiles, send.sendFiles);
                assertEquals(_sendLog, send.sendLogs);

                // Copied from cli ErrorHandler.onUpdate.
                sendFiles = send.sendFiles;
                sendLogs = send.sendLogs;

                sendErrors();
            } else if (event instanceof ErrorHandler.UNKNOWN_FORMAT){
                // ignore this one.
            } else {
                fail("Bad event: " + event);
            }
        }

    }

    protected ImportConfig cfg(boolean sendFiles, boolean sendLog) {
        final ImportConfig config = new ImportConfig();
        config.sendLogFile.set(sendLog);
        config.sendFiles.set(sendFiles);
        return config;
    }

    protected ImportEvent err() {
        return new ErrorHandler.UNKNOWN_FORMAT("/tmp/",
                new RuntimeException("test"), null);
    }

    public void testLogsAndFiles() {
        ImportConfig cfg = cfg(true, true);
        MyErrorHandler handler = new MyErrorHandler(cfg, true, true);
        handler.update(null, err());
        handler.update(null, new ImportEvent.DEBUG_SEND(true, true));
        assertEquals(new Integer(2), handler.uploads);
        assertEquals(new Integer(1), handler.posts);
    }

    public void testLogsNotFiles() {
        ImportConfig cfg = cfg(false, true);
        MyErrorHandler handler = new MyErrorHandler(cfg, false, true);
        handler.update(null, err());
        handler.update(null, new ImportEvent.DEBUG_SEND(false, true));
        assertEquals(new Integer(1), handler.uploads);
        assertEquals(new Integer(0), handler.posts);
    }

    public void testFilesNotLogs() {
        ImportConfig cfg = cfg(true, false);
        MyErrorHandler handler = new MyErrorHandler(cfg, true, false);
        handler.update(null, err());
        handler.update(null, new ImportEvent.DEBUG_SEND(true, false));
        assertEquals(new Integer(1), handler.uploads);
        assertEquals(new Integer(1), handler.posts);
    }

    public void testNeitherFilesNorLogs() {
        ImportConfig cfg = cfg(false, false);
        MyErrorHandler handler = new MyErrorHandler(cfg, false, false);
        handler.update(null, err());
        handler.update(null, new ImportEvent.DEBUG_SEND(false, false));
        assertEquals(null, handler.uploads);
        assertEquals(new Integer(0), handler.posts);
    }

}
