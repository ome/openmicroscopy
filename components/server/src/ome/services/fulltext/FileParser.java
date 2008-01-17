/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.fulltext;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Object which attempts to parse any file given to it. On an exception or
 * empty/missing file, an empty {@link Iterable<String>} should be returned
 * rather than throwing an exception.
 * 
 * Subclases should follow
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public class FileParser {

    private final static Log log = LogFactory.getLog(FileParser.class);

    /**
     * {@link Iterable} which returns an empty {@link Iterator}. This will be
     * used in case
     */
    public final static Iterable<String> EMPTY = new Iterable<String>() {
        public Iterator<String> iterator() {
            return new Iterator<String>() {
                public boolean hasNext() {
                    return false;
                }

                public String next() {
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
    final public Iterable<String> parse(File file) {

        if (file == null) {
            log.warn("Argument null. Returning EMPTY:" + file);
            return EMPTY;
        }

        try {
            Iterable<String> it = doParse(file);
            if (it == null) {
                log.debug("Implementation returned null.");
                return EMPTY;
            } else {
                return it;
            }
        } catch (Exception e) {
            log.debug("Implementation threw an exception.");
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
    public Iterable<String> doParse(File file) throws Exception {
        OverlappingChunkFileIterator it = new OverlappingChunkFileIterator(file);
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
    public Iterable<String> wrap(Iterator<String> it) {
        if (it == null) {
            return EMPTY;
        }
        return new IteratorWrapper(it);
    }

    private static class IteratorWrapper implements Iterable<String> {

        private final Iterator<String> it;

        public IteratorWrapper(Iterator<String> it) {
            this.it = it;
        }

        public Iterator<String> iterator() {
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

        /**
         * Synchronous method which retrieves roughly {@link #size} bytes from
         * the {@link FileReader}. The last read may of course be shorter.
         * 
         * Once the reader has signaled a finish state, the reader will be
         * nulled and all subsequent calls will return null.
         * 
         */
        private String doRead2() {
            if (reader == null) {
                return null;
            }

            int rv = -1;
            try {
                rv = reader.read(buf);
            } catch (Exception e) {
                throw new RuntimeException("Error while parsing file:", e);
            }

            if (rv < 0) {
                closeReader();
            } else {
                next = new String(buf, 0, rv);
                next.lastIndexOf(linesep);
            }

            return null; // FIXME
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