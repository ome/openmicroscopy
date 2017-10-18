/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2017 Glencoe Software, Inc.
 *  All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package ome.logic;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.ContextRefreshedEvent;

import ome.model.core.OriginalFile;
import ome.model.display.ChannelBinding;
import ome.services.scripts.ScriptRepoHelper;
import ome.tools.spring.OnContextRefreshedEventListener;
import omeis.providers.re.lut.LutProvider;
import omeis.providers.re.lut.LutReader;
import omeis.providers.re.lut.LutReaderFactory;


/**
 * Lookup table provider implementation.
 * @author Chris Allan <callan@glencoesoftware.com>
 * @since 5.4.1
 */
public class LutProviderImpl
        extends OnContextRefreshedEventListener implements LutProvider {

    /** The logger for this class. */
    private static Logger log =
            LoggerFactory.getLogger(LutProviderImpl.class);

    private final ScriptRepoHelper scriptRepoHelper;

    private final String fileRepoSecretKey;

    /** Available readers, keyed off name. Should be an unmodifiable map. */
    protected Map<String, LutReader> lutReaders;

    public LutProviderImpl(ScriptRepoHelper scriptRepoHelper, String uuid) {
        this.scriptRepoHelper = scriptRepoHelper;
        this.fileRepoSecretKey = uuid;
    }

    @Override
    public void handleContextRefreshedEvent(ContextRefreshedEvent event) {
        // As the script repository helper has not been fully initialized with
        // the correct filters until the Spring Application Context has been
        // refreshed we need to defer our reader population until then.
        initLutReaders();
    }

    /**
     * Finds the lookup table readers supported and updates them on this
     * instance.
     */
    protected void initLutReaders() {
        List<OriginalFile> scripts =
                scriptRepoHelper.loadAll(true, "text/x-lut");
        log.debug("Found {} LUT scripts", scripts.size());
        Map<String, LutReader> lutReaders = new HashMap<String, LutReader>();
        String root = scriptRepoHelper.getScriptDir();
        for (OriginalFile script : scripts) {
            String path = script.getPath();
            String name = script.getName();
            // On INSERT the file repository secret key is used in the "name"
            // attribute of an OriginalFile to protect bad actors from
            // manipulating other attributes.  If it is present we need to
            // strip it off.  This *should* only occur in the situation
            // where the database is fresh and the above call to
            // `ScriptRepoHelper.loadAll()` actually creates the OriginalFile
            // rows in the database for the various scripts.
            //
            // Reference:
            //   https://github.com/openmicroscopy/openmicroscopy/pull/5273
            name = name.replace(fileRepoSecretKey,  "");
            Path lut = Paths.get(root, path, name);
            if (Files.exists(lut)) {
                try {
                    String key = lut.getFileName().toString().toLowerCase();
                    lutReaders.put(key, LutReaderFactory.read(lut.toFile()));
                    log.debug("Successfully added LUT '{}'", key);
                    continue;
                } catch (Exception e) {
                    log.warn("Cannot read lookup table: '{}'", lut, e);
                }
            } else {
                log.warn("Cannot find lookup table: '{}'", lut);
            }
        }
        log.info("Successfully added {} LUTs", lutReaders.size());
        this.lutReaders =
                Collections.<String, LutReader>unmodifiableMap(lutReaders);
    }

    /* (non-Javadoc)
     * @see omeis.providers.re.lut.LutProvider#getLutReaders(ome.model.display.ChannelBinding[])
     */
    public List<LutReader> getLutReaders(ChannelBinding[] channelBindings) {
        log.debug("Looking up LUT readers for {} channels from {} LUTs",
                channelBindings.length, lutReaders.size());
        List<LutReader> lutReaders = new ArrayList<LutReader>();
        for (ChannelBinding cb : channelBindings) {
            if (cb.getActive()) {
                LutReader lut = this.lutReaders.get(cb.getLookupTable());
                if (lut != null) {
                    lutReaders.add(lut);
                } else {
                    lutReaders.add(null);
                }
            }
        }
        return lutReaders;
    }

}
