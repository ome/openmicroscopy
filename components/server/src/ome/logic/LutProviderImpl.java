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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ome.model.display.ChannelBinding;
import ome.services.scripts.RepoFile;
import ome.services.scripts.ScriptFileType;
import ome.services.scripts.ScriptRepoHelper;
import omeis.providers.re.lut.LutProvider;
import omeis.providers.re.lut.LutReader;
import omeis.providers.re.lut.LutReaderFactory;


/**
 * Lookup table provider implementation.
 * @author Chris Allan <callan@glencoesoftware.com>
 * @since 5.4.1
 */
public class LutProviderImpl implements LutProvider {

    /** The logger for this class. */
    private static Logger log =
            LoggerFactory.getLogger(LutProviderImpl.class);

    /**
     * Available readers, keyed off name.  Should be an unmodifiable map.
     */
    protected final Map<String, LutReader> lutReaders =
            new HashMap<String, LutReader>();

    @SuppressWarnings("unchecked")
    public LutProviderImpl(
            ScriptRepoHelper scriptRepoHelper, ScriptFileType lutType) {
        File root = new File(scriptRepoHelper.getScriptDir());
        Iterator<File> scripts = FileUtils.iterateFiles(
                root, lutType.getFileFilter(), TrueFileFilter.TRUE);
        while (scripts.hasNext()) {
            RepoFile script = new RepoFile(root, scripts.next());
            String basename = script.basename();
            try {
                lutReaders.put(
                        basename, LutReaderFactory.read(script.file()));
                log.debug("Successfully added LUT '{}'", basename);
            } catch (Exception e) {
                log.warn("Cannot read lookup table: '{}'",
                        script.fullname(), e);
            }
        }
        log.info("Successfully added {} LUTs", lutReaders.size());
    }

    /* (non-Javadoc)
     * @see omeis.providers.re.lut.LutProvider#getLutReaders(ome.model.display.ChannelBinding[])
     */
    public List<LutReader> getLutReaders(ChannelBinding[] channelBindings) {
        log.debug("Looking up LUT readers for {} channels from {} LUTs",
                channelBindings.length, lutReaders.size());
        List<LutReader> toReturn = new ArrayList<LutReader>();
        for (ChannelBinding cb : channelBindings) {
            if (cb.getActive()) {
                LutReader lut = lutReaders.get(cb.getLookupTable());
                if (lut != null) {
                    toReturn.add(lut);
                } else {
                    toReturn.add(null);
                }
            }
        }
        return toReturn;
    }

}
