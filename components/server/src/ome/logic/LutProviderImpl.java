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
import org.springframework.transaction.annotation.Transactional;

import ome.annotations.RolesAllowed;
import ome.api.ServiceInterface;
import ome.model.display.ChannelBinding;
import ome.services.scripts.ScriptRepoHelper;
import ome.util.SqlAction;
import omeis.providers.re.lut.LutProvider;
import omeis.providers.re.lut.LutReader;
import omeis.providers.re.lut.LutReaderFactory;


/**
 * Lookup table provider implementation.
 * @author Chris Allan <callan@glencoesoftware.com>
 * @since 5.4.1
 */
@Transactional
public class LutProviderImpl
        extends AbstractLevel2Service implements LutProvider {

    /** The logger for this class. */
    private static Logger log =
            LoggerFactory.getLogger(LutProviderImpl.class);

    /** Script repository root. */
    private final String root;

    /** Available readers, keyed off name. Should be an unmodifiable map. */
    protected Map<String, LutReader> lutReaders;

    private transient SqlAction sqlAction;

    public LutProviderImpl() {
        root = ScriptRepoHelper.getDefaultScriptDir();
    }

    /**
     * {@link SqlAction} setter for dependency injection.
     * 
     * @param sqlAction the SQL action instance
     * @see ome.services.util.BeanHelper#throwIfAlreadySet(Object, Object)
     */
    public final void setSqlAction(SqlAction sqlAction) {
        getBeanHelper().throwIfAlreadySet(this.sqlAction, sqlAction);
        this.sqlAction = sqlAction;
        initLutReaders();
    }

    @Override
    public Class<? extends ServiceInterface> getServiceInterface() {
        return LutProvider.class;
    }

    /**
     * Finds the lookup table readers supported and updates them on this
     * instance.
     */
    protected void initLutReaders() {
        List<String> paths = sqlAction.findLuts();
        Map<String, LutReader> lutReaders = new HashMap<String, LutReader>();
        for (String path : paths) {
            Path lut = Paths.get(root, path);
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
    @RolesAllowed("user")
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
