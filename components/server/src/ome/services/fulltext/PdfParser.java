/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.fulltext;

import java.io.File;
import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.Reader;

import ome.services.messages.ParserOpenFileMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.util.PDFTextStripper;

/**
 * {@link FileParser} for "application/pdf" files using <a
 * href="http://pdfbox.org/">PDFBox</a>.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 */
public class PdfParser extends FileParser {

    private final static Logger log = LoggerFactory.getLogger(PdfParser.class);

    @Override
    public Iterable<Reader> doParse(File file) throws Exception {

        final PdfThread pdfThread = new PdfThread(file);
        this.context.publishEvent(new ParserOpenFileMessage(this,
                pdfThread) {
            @Override
            public void close() {
                try {
                    pdfThread.close();
                } catch (Exception e) {
                    log.warn("Error closing PdfThread " + pdfThread, e);
                }
            }

        });

        pdfThread.start();
        return wrap(pdfThread.getReader());
    }
}

class PdfThread extends Thread {

    private final static Logger log = LoggerFactory.getLogger(PdfThread.class);

    final File file;
    final PipedWriter writer;
    final PipedReader reader;
    PDDocument document = null;

    PdfThread(File file) throws IOException {
        this.file = file;
        reader = new PipedReader();
        writer = new PipedWriter(reader);
    }

    Reader getReader() {
        return reader;
    }

    @Override
    public void run() {

        try {
            document = PDDocument.load(file);
        } catch (IOException e) {
            log.warn("Could not load Pdf " + file, e);
            try {
                writer.close();
            } catch (IOException ioe) {
                // What can we do?
            }
        }

        try {
            if (document != null && !document.isEncrypted()) {
                try {
                    PDFTextStripper stripper = null;
                    stripper = new PDFTextStripper();
                    stripper.writeText(document, writer);
                } finally {
                    close();
                }
            }
        } catch (IOException e) {
            log.warn("Error reading pdf file", e);
        }
    }

    public void close() {
        if (writer != null) {
            try {
                writer.close();
            } catch (Exception e) {
                log.warn("Error closing writer", e);
            }
        }
        if (document != null) {
            try {
                document.close();
            } catch (Exception e) {
                log.warn("Error closing PDF document", e);
            }
        }

    }

}
