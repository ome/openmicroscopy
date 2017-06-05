/*
 *   Copyright 2009-2014 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.util;

import java.util.ArrayList;
import java.util.List;

import loci.formats.IFormatReader;
import loci.formats.ImageReader;

import ome.model.enums.Format;
import ome.system.PreferenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hook run by the context to guarantee that various enumerations are up to
 * date. This is especially important for changes to bioformats which add
 * readers. Each reader is equivalent to a format ( + "Companion/<reader>").
 * Without such an extension, users are not able to import the latest and
 * greatest without a database upgrade.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @author m.t.b.carroll@dundee.ac.uk
 * @since Beta4.1.1
 */
public class DBEnumCheck extends BaseDBCheck {

    public final static Logger log = LoggerFactory.getLogger(DBEnumCheck.class);

    private final EnsureEnum ensureEnum;

    public DBEnumCheck(Executor executor, PreferenceContext preferences, EnsureEnum ensureEnum) {
        super(executor, preferences);
        this.ensureEnum = ensureEnum;
    }

    @Override
    protected void doCheck() {
        final List<String> formatNames = new ArrayList<String>();
        for (final IFormatReader formatReader : new ImageReader().getReaders()) {
            String name = formatReader.getClass().getSimpleName();
            if (name.endsWith("Reader")) {
                name = name.substring(0, name.length() - 6);
                if ("Fake".equals(name)) {
                    continue;
                }
                formatNames.add(name);
                if (formatReader.hasCompanionFiles()) {
                    formatNames.add("Companion/" + name);
                }
            }
        }
        ensureEnum.ensure(Format.class, formatNames);
    }

    @Override
    protected String getCheckDone() {
        return "done for Bio-Formats revision " + loci.formats.FormatTools.VCS_REVISION;
    }
}
