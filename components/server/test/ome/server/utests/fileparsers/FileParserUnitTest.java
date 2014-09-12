/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.utests.fileparsers;

import java.io.BufferedReader;
import java.io.File;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import ome.services.fulltext.PdfParser;
import ome.services.messages.RegisterServiceCleanupMessage;
import ome.system.OmeroContext;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.util.ResourceUtils;
import org.testng.annotations.Test;

public class FileParserUnitTest extends TestCase {

    static List<RegisterServiceCleanupMessage> list = new ArrayList<RegisterServiceCleanupMessage>();

    @Test
    public void testPdfParse() throws Exception {
        File abc123 = ResourceUtils
                .getFile("classpath:ome/server/utests/fileparsers/ABC123.pdf");
        PdfParser parser = new PdfParser();
        parser.setApplicationContext(new OmeroContext(
                "classpath:ome/server/utests/fileparsers/config.xml"));
        StringBuffer sb = new StringBuffer();
        Iterable<Reader> text = parser.parse(abc123);
        for (Reader reader : text) {
            BufferedReader buffered = new BufferedReader(reader);
            sb.append(buffered.readLine());
        }
        assertEquals("ABC123", sb.toString());
        for (RegisterServiceCleanupMessage cleanup : list) {
            cleanup.close();
        }
        list.clear();
    }

    public static class Closer implements ApplicationListener {

        public void onApplicationEvent(ApplicationEvent arg0) {
            if (arg0 instanceof RegisterServiceCleanupMessage) {
                RegisterServiceCleanupMessage cleanup = (RegisterServiceCleanupMessage) arg0;
                list.add(cleanup);
            }
        }

    }
}
