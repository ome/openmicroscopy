/*
 * Copyright (C) 2016 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package ome.services.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.SetMultimap;

import ome.model.IEnum;
import ome.model.IGlobal;
import ome.system.PreferenceContext;

/**
 * When a new version of the OMERO server starts up check that the database
 * includes an enumeration instance for every value mapped for code generation.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.3.0
 */
public class DBMappedEnumCheck extends BaseDBCheck {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBMappedEnumCheck.class);

    private static final Pattern ENUMERATION_PATTERN = Pattern.compile("(ome\\.model\\.enums\\.[A-Za-z\\.]+)\\.\\d+\\:(\\S*)");

    private EnsureEnum ensureEnum;

    protected DBMappedEnumCheck(Executor executor, PreferenceContext preferences, EnsureEnum ensureEnum) {
        super(executor, preferences);
        this.ensureEnum = ensureEnum;
    }

    @Override
    protected void doCheck() {
        final SetMultimap<String, String> enums = getEnums();
        if (enums != null) {
            ensureEnums(enums);
        }
    }

    /**
     * @return the enumeration classes and values from the generated {@code enums.properties} file,
     * or {@code null} if it the file not be read
     */
    private SetMultimap<String, String> getEnums() {
        JarFile jarFile = null;
        InputStream enums = null;
        final SetMultimap<String, String> enumValues = LinkedHashMultimap.create();
        try {
            try {
                final URL file = ResourceUtils.getURL("classpath:enums.properties");
                jarFile = new JarFile(ResourceUtils.extractJarFileURL(file).getPath());
                enums = jarFile.getInputStream(jarFile.getJarEntry("enums.properties"));
            } catch (IOException ioe) {
                LOGGER.warn("could not locate mapped enumerations", ioe);
                return null;
            }
            try (final BufferedReader in = new BufferedReader(new InputStreamReader(enums))) {
                String line;
                while ((line = in.readLine()) != null) {
                    final Matcher matcher = ENUMERATION_PATTERN.matcher(line);
                    if (matcher.matches()) {
                        enumValues.put(matcher.group(1), matcher.group(2));
                    }
                }
            } catch (IOException ioe) {
                LOGGER.warn("could not read mapped enumerations", ioe);
                return null;
            }
        } finally {
            try {
                if (jarFile != null) {
                    jarFile.close();
                }
                if (enums != null) {
                    enums.close();
                }
            } catch (IOException ioe) {
                LOGGER.warn("failed to close input handle", ioe);
            }
        }
        return enumValues;
    }

    /**
     * @param className the name of an enumeration class
     * @return the corresponding class
     * @throws ClassNotFoundException if the class could not be found
     */
    @SuppressWarnings("unchecked")
    private static <C extends IEnum & IGlobal> Class<C> getEnumClassForName(String className) throws ClassNotFoundException {
        return (Class<C>) Class.forName(className);
    }

    /**
     * Ensure that the given enumeration values exist as instances in the database.
     * @param enumValues the enumeration classes and their values
     */
    private void ensureEnums(SetMultimap<String, String> enumValues) {
        for (final Map.Entry<String, Collection<String>> valuesForClass : enumValues.asMap().entrySet()) {
            try {
                ensureEnum.ensure(getEnumClassForName(valuesForClass.getKey()), valuesForClass.getValue());
            } catch (ClassNotFoundException cnfe) {
                LOGGER.warn("cannot find class", cnfe);
                continue;
            }
        }
    }

    @Override
    protected String getCheckDone() {
        return "done for OMERO version " + getOmeroVersion();
    }
}
