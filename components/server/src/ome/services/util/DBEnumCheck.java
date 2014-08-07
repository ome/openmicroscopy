/*
 *   Copyright 2009-2014 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import loci.formats.IFormatReader;
import loci.formats.ImageReader;
import ome.conditions.InternalException;
import ome.system.PreferenceContext;
import ome.util.SqlAction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

/**
 * Hook run by the context to guarantee that various enumerations are up to
 * date. This is especially important for changes to bioformats which add
 * readers. Each reader is equivalent to a format ( + "Companion/<reader>").
 * Without such an extension, users are not able to import the latest and
 * greatest without a database upgrade.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4.1.1
 */
public class DBEnumCheck extends BaseDBCheck {

    public final static Logger log = LoggerFactory.getLogger(DBEnumCheck.class);

    public final static Pattern readerClass = Pattern
            .compile("^.*?[.]?([^.]+)Reader$");

    /**
     * Hard-coded list of formats missing from 4.1 which should have a
     * "Companion/<reader>" value in the database. Temporary until bio-formats
     * provides inspection on this information.
     */
    final private static List<String> companionful = Arrays.asList("Analyze",
            "APL", "L2D", "Nifti", "TillVision", "Scanr");

    /**
     * Hard-coded list of formats missing from 4.1 which should NOT have a
     * "Companion/<reader>" value in the database. Temporary until bio-formats
     * provides inspection on this information.
     */
    final private static List<String> companionless = Arrays.asList("Zip",
            "APNG", "PCX", "Ivision", "FEI", "NAF", "MINC", "MRW", "ARF",
            "Cellomics", "LiFlim", "Amira");

    /**
     * Hard-coded list of formats missing from 4.1 which should NOT have a
     * "Companion/<reader>" NOR a format at all. Temporary until bio-formats
     * provides inspection on this information.
     */
    final private static List<String> omitlist = Arrays.asList("Fake");

    public static List<String> getReaderNames() {
        List<String> rv = new ArrayList<String>();
        IFormatReader[] readers = new ImageReader().getReaders();
        for (IFormatReader formatReader : readers) {

            String name = formatReader.getClass().getSimpleName();
            Matcher matcher = readerClass.matcher(name);
            if (!matcher.matches()) {
                log.warn("Reader doesn't match: " + name);
                continue;
            }

            rv.add(matcher.group(1));
        }
        return rv;
    }

    public static boolean requiresCompanion(String name) {
        return companionful.contains(name);
    }

    public static boolean shouldBeOmitted(String name) {
        return omitlist.contains(name);
    }

    public DBEnumCheck(Executor executor, PreferenceContext preferences) {
        super(executor, preferences);
    }

    @Override
    protected void doCheck() {
        try {
            executor.executeSql(new Executor.SimpleSqlWork(this,
                    "DBEnumCheck") {
                @Transactional(readOnly = false)
                public Object doWork(SqlAction sql) {
                    for (String name : getReaderNames()) {
                        addFormat(sql, name);
                        if (requiresCompanion(name)) {
                            addFormat(sql, "Companion/" + name);
                        }
                    }
                    return null;
                }

            });
        } catch (Exception e) {
            final String msg = "Error synchronizing enumerations";
            log.error(msg, e); // slf4j migration: fatal() to error()
            InternalException ie = new InternalException(msg);
            throw ie;
        }
    }

    /**
     * Adds an ome.model.enums.Format object if 1) the format value is not
     * omitted, 2) it doesn't already exist.
     * 
     * @param jdbc
     * @param name
     * @return true if the format was added.
     */
    private boolean addFormat(SqlAction sql, String name) {

        if (shouldBeOmitted(name)) {
            log.debug("Omitting: " + name);
            return false;
        }

        long count = sql.countFormat(name);

        if (count > 0) {
            log.debug("Found reader: " + name);
            return false;
        }

        int inserts = sql.insertFormat(name);
        if (inserts != 1) {
            throw new InternalException("Expected 1 insert. Found " + inserts
                    + " while adding: " + name);
        }

        log.info("Added format: " + name);
        return true;

    }

    @Override
    protected String getCheckDone() {
        return "done for Bio-Formats revision " + loci.formats.FormatTools.VCS_REVISION;
    }
}
