/*
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.fulltext;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.Iterator;
import java.util.NoSuchElementException;

import ome.services.messages.ParserOpenFileMessage;
import ome.system.OmeroContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Object which attempts to parse any file given to it. On an exception or
 * empty/missing file, an empty {@link Iterable<String>} should be returned
 * rather than throwing an exception.
 * 
 * Subclasses should follow
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public class FileParser implements ApplicationContextAware {

    private final static Logger log = LoggerFactory.getLogger(FileParser.class);

    protected OmeroContext context;

    protected long maxFileSize = 10000L; // default test is 8.8KB

    public void setApplicationContext(ApplicationContext arg0)
            throws BeansException {
        context = (OmeroContext) arg0;
    }

    public void setMaxFileSize(Long size) {
        if (size.floatValue() / Runtime.getRuntime().maxMemory() > 0.5) {
            log.warn("Indexer maximum file size is set to more than half of "
                    + "total heap size. Excessively large text files may "
                    + "cause search index corruption. Consider decreasing the "
                    + "maximum file size or increasing the Indexer heap size.");
        }
        this.maxFileSize = size;
    }

    /**
     * {@link Iterable} which returns an empty {@link Iterator}. This will be
     * used in case
     */
    public final static Iterable<Reader> EMPTY = new Iterable<Reader>() {
        public Iterator<Reader> iterator() {
            return new Iterator<Reader>() {
                public boolean hasNext() {
                    return false;
                }

                public Reader next() {
                    throw new NoSuchElementException();
                }

                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
    };

    /**
     * Uses {@link #doParse(File)} to create manageable chunks of a file for
     * indexing. If the {@link File} argument is null or unreadable, then the
     * {@link #EMPTY} {@link Iterable} will be returned. The same holds if a
     * null {@link Iterable} is returned or an {@link Exception} is thrown.
     * 
     * The {@link Iterator} returned from the instance should always be
     * completely iterated through so that resources can be released. For
     * example, <code>
     * for (String string : parse(file)) {
     *     /* possibly ignore string *\/
     * }
     * </code>
     * 
     * @param file
     *            Can be null.
     * @return An {@link Iterable} which is never null.
     */
    final public Iterable<Reader> parse(File file) {

        if (file == null) {
            log.warn("Argument null. Returning EMPTY:");
            return EMPTY;
        }

        if (!file.exists() && !file.canRead()) {
            log.debug("empty|unreadable file: " + file.getAbsoluteFile());
            return EMPTY;
        }

        if (file.length() > this.maxFileSize) {
            log.info("File too large for indexing. Skipping: "
                    + file.getAbsoluteFile());
            return EMPTY;
        }

        try {
            Iterable<Reader> it = doParse(file);
            if (it == null) {
                log.debug("Implementation returned null.");
                return EMPTY;
            } else {
                return it;
            }
        } catch (Exception e) {
            log.warn("Implementation threw an exception.", e);
            return EMPTY;
        }

    }

    /**
     * Template method to parse a {@link File} into manageable chunks.
     * 
     * The default implementation reads from the file lazily with chunks
     * overlapping on the final white space. For example a file with:
     * <code>The quick brown fox jumps over the lazy dog</code> might be
     * parsed to: <code>The quick brown fox jumps</code> and
     * <code>jumps over the lazy dog</code>.
     * 
     * Receives a non-null, {@link File#canRead() readable} {@link File}
     * instance from {@link #parse(File)} and can return a possible null
     * {@link Iterable} or throw an {@link Exception}.
     * 
     * In any of the non-successful cases, the {@link #EMPTY} {@link Iterable}
     * will be returned to the consumer.
     */
    public Iterable<Reader> doParse(File file) throws Exception {
        FileReader reader = new FileReader(file);
        BufferedReader buffered = new BufferedReader(reader);
        context.publishEvent(new ParserOpenFileMessage(this, buffered) {
            @Override
            public void close() {
                try {
                    Reader r = (Reader) resource;
                    r.close();
                } catch (Exception e) {
                    log.debug("Error closing " + resource, e);
                }
            }
        });
        Iterator<Reader> it = new SingleIterator(buffered);
        return wrap(it);
    }

    /**
     * Wraps an {@link Iterator} with an {@link Iterable} instance. If the
     * {@link Iterator} is null, the {@link #EMPTY} {@link Iterable} will be
     * returned.
     * 
     * @param it
     *            Can be null.
     * @return Will never be null
     */
    public Iterable<Reader> wrap(Iterator<Reader> it) {
        if (it == null) {
            return EMPTY;
        }
        return new IteratorWrapper(it);
    }

    public Iterable<Reader> wrap(Reader r) {
        if (r == null) {
            return EMPTY;
        }
        return wrap(new SingleIterator(r));
    }

    private static class SingleIterator implements Iterator<Reader> {

        Reader r;

        SingleIterator(Reader r) {
            this.r = r;
        }

        public boolean hasNext() {
            return r != null;
        }

        public Reader next() {
            Reader rv = r;
            r = null;
            return rv;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

    }

    private static class IteratorWrapper implements Iterable<Reader> {

        private final Iterator<Reader> it;

        public IteratorWrapper(Iterator<Reader> it) {
            this.it = it;
        }

        public Iterator<Reader> iterator() {
            return it;
        }
    }

    private static class OverlappingChunkFileIterator implements
            Iterator<String> {

        private static final String linesep = System
                .getProperty("line.separator");

        private static final int size = 10000;

        private final long fileSize;

        private final char[] buf;

        private String next;

        /*
         * will be closed nulled out when finished.
         */
        private BufferedReader reader;

        public OverlappingChunkFileIterator(File file) throws Exception {
            this.fileSize = file.length();
            if (fileSize > Integer.MAX_VALUE) {
                throw new RuntimeException(String.format(
                        "%s file is too large for current implementation: %s",
                        file, fileSize));
            }
            this.reader = new BufferedReader(new FileReader(file), size);
            this.buf = new char[size];
        }

        public boolean hasNext() {

            if (next == null) {
                next = doRead();
            }
            return next != null;
        }

        public String next() {

            if (!hasNext()) { // does doRead()
                throw new NoSuchElementException();
            }
            String rv = next;
            next = null;
            return rv;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        /**
         * Intermediate method which parses whole file into a single String.
         * Please see the restriction in the constructor on filesize.
         */
        private String doRead() {

            if (reader == null) {
                return null;
            }

            StringBuffer sb = new StringBuffer((int) fileSize);

            int rv = -1;
            try {
                while ((rv = reader.read(buf)) != -1) {
                    sb.append(buf, 0, rv);
                }
            } catch (Exception e) {
                throw new RuntimeException("Error while parsing file", e);
            }
            closeReader();
            return sb.toString();

        }

        private void closeReader() {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    // must ignore
                } finally {
                    reader = null;
                }
            }
        }
    }
}
