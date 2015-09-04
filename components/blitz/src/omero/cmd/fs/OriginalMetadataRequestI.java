/*
 * Copyright (C) 2013 Glencoe Software, Inc. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package omero.cmd.fs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.TreeMap;

import loci.formats.IFormatReader;
import loci.formats.ImageReader;
import ome.api.IQuery;
import ome.io.nio.PixelsService;
import ome.model.annotations.FileAnnotation;
import ome.model.core.Image;
import ome.model.core.Pixels;
import ome.model.fs.Fileset;
import ome.parameters.Parameters;
import omero.RType;
import omero.cmd.ERR;
import omero.cmd.Helper;
import omero.cmd.IRequest;
import omero.cmd.OriginalMetadataRequest;
import omero.cmd.OriginalMetadataResponse;
import omero.cmd.Response;
import omero.cmd.HandleI.Cancel;
import omero.constants.annotation.file.ORIGINALMETADATA;
import omero.constants.namespaces.NSCOMPANIONFILE;
import omero.util.IceMapper;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

/**
 * Original metadata loader, handling both pre-FS and post-FS data.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 5.0.0
 */
public class OriginalMetadataRequestI extends OriginalMetadataRequest implements
		IRequest {

	private static final long serialVersionUID = -1L;

	private final OriginalMetadataResponse rsp = new OriginalMetadataResponse();

	private final PixelsService pixelsService;

	private Helper helper;

    public OriginalMetadataRequestI(PixelsService pixelsService) {
        this.pixelsService = pixelsService;
    }

	//
	// CMD API
	//

	public Map<String, String> getCallContext() {
		Map<String, String> all = new HashMap<String, String>();
		all.put("omero.group", "-1");
		return all;
	}

	public void init(Helper helper) {
		this.helper = helper;
		this.helper.setSteps(1);
	}

	public Object step(int step) {
		helper.assertStep(step);
		loadFileset();
		if (rsp.filesetId == null) {
			loadFileAnnotation();
		}
		return null;
	}

	@Override
	public void finish() throws Cancel {
		// no-op
	}

	public void buildResponse(int step, Object object) {
		helper.assertResponse(step);
		if (helper.isLast(step)) {
			helper.setResponseIfNull(rsp);
		}
	}

	public Response getResponse() {
		return helper.getResponse();
	}

	//
	// LOADING & PARSING
	//

	/**
	 * Searches for a {@link Fileset} attached to this {@link Image}, and if present,
	 * uses Bio-Formats to parse the metadata into the {@link OriginalMetadataResponse}
	 * instance. If no {@link Fileset} is present, then there <em>may</em> be a
	 * {@link FileAnnotation} present which has a static version of the metadata.
	 */
	protected void loadFileset() {
		rsp.filesetId = firstIdOrNull("select i.fileset.id from Image i where i.id = :id");
		if (rsp.filesetId != null) {
			final Image image = helper.getServiceFactory().getQueryService().get(Image.class, imageId);
			final Pixels pixels = image.getPrimaryPixels();

			try {
				final IFormatReader reader = pixelsService.getBfReader(pixels);
				final Hashtable<String, Object> global = reader.getGlobalMetadata();
				final Hashtable<String, Object> series = reader.getSeriesMetadata();
				rsp.globalMetadata = wrap(global);
				rsp.seriesMetadata = wrap(series);
			} catch (Throwable t) {
				helper.cancel(new ERR(), t, "bf-reader-failure", "pixels", ""+pixels.getId());
			}
		}
	}

    /**
     * Only called if {@link #loadFileset()} finds no {@link Fileset}. If any {@link FileAnnotation}
     * instances with the appropriate namespace and name are found, the first one is taken and
     * parsed into the {@link OriginalMetadataResponse}.
     */
    protected void loadFileAnnotation() {
        rsp.fileAnnotationId = firstIdOrNull(
                "select a.id from Image i join i.annotationLinks l join l.child a " +
                "where i.id = :id and a.file.name = '" + ORIGINALMETADATA.value + "' " +
                "and a.ns = '" + NSCOMPANIONFILE.value + "'");
        if (rsp.fileAnnotationId != null) {
            final IQuery iQuery = helper.getServiceFactory().getQueryService();
            final FileAnnotation fileAnnotation = iQuery.get(FileAnnotation.class, rsp.fileAnnotationId.getValue());
            final String filePath = pixelsService.getFilesPath(fileAnnotation.getFile().getId());
            parseOriginalMetadataTxt(new File(filePath));
        }
    }

	//
	// HELPERS
	//

	/**
	 * Use {@link IQuery#projection(String, Parameters)} to load the first
	 * long which matches the given query. This means that the first return
	 * value in the select statement should likely be the id of an object.
	 */
	protected omero.RLong firstIdOrNull(String query) {
		List<Object[]> ids = helper.getServiceFactory().getQueryService()
				.projection(query, new Parameters().addId(imageId).page(0, 1));
		if (ids != null && ids.size() > 0) {
		    Object[] id = ids.get(0);
		    if (id != null && id.length > 0) {
		        return omero.rtypes.rlong((Long) id[0]);
		    }
		}
		return null;
	}

	/**
	 * Use {@link IceMapper} to convert from {@link Object} instances in
	 * the given {@link Hashtable} to {@link RType} instances. This may
	 * throw an exception on unknown types.
	 */
	protected Map<String, RType> wrap(Hashtable<String, Object> table) {
		final Map<String, RType> rv = new HashMap<String, RType>();
		if (table == null || table.size() == 0) {
			return rv;
		}

		final IceMapper mapper = new IceMapper();
		for (Entry<String, Object> entry : table.entrySet()) {
			String key = entry.getKey();
			Object val = entry.getValue();
			try {
				if (val instanceof Short) {
					// Likely could be handled in toRType
					rv.put(key, mapper.toRType(((Short) val).intValue()));
				} else {
					rv.put(key, mapper.toRType(val));
				}
			} catch (Exception e) {
				String msg = String.format("Could not convert to rtype: " +
					"key=%s, value=%s, type=%s ", key, val,
					(val == null ? "null" : val.getClass()));
				if (helper == null) {
					// from command-line
					System.err.println(msg);
				} else {
					helper.warn(msg);
				}
			}
		}
		return rv;
	}

    /**
     * Split the given string at the rightmost '=' character among those that are the least enclosed by some kind of bracket.
     * @param keyValue the key = value string
     * @return the extracted key and value, or <code>null</code> if there is no '=' character
     */
    private static Map.Entry<String, String> splitOnEquals(String keyValue) {
        Integer equalsIndex = null;
        Integer equalsSmallestDepth = null;
        int currentIndex = 0;
        int currentDepth = 0;
        while (currentIndex < keyValue.length()) {
            switch (keyValue.charAt(currentIndex)) {
            case '(':
            case '[':
            case '{':
                currentDepth++;
                break;
            case ')':
            case ']':
            case '}':
                currentDepth--;
                break;
            case '=':
                if (equalsSmallestDepth == null || currentDepth <= equalsSmallestDepth) {
                    equalsIndex = currentIndex;
                    equalsSmallestDepth = currentDepth;
                }
                break;
            }
            currentIndex++;
        }
        if (equalsIndex == null) {
            return null;
        } else {
            return Maps.immutableEntry(keyValue.substring(0, equalsIndex).trim(),
                                       keyValue.substring(equalsIndex + 1).trim());
        }
    }

    /**
     * Read the given INI-style file and populate the maps with the properties from the corresponding sections.
     * @param file the file to read
     */
    protected void parseOriginalMetadataTxt(File file) {
        final Pattern section = Pattern.compile("\\s*\\[\\s*(.+?)\\s*\\]\\s*");
        rsp.globalMetadata = new TreeMap<String, RType>();
        rsp.seriesMetadata = new TreeMap<String, RType>();
        final ImmutableMap<String, Map<String, RType>> sections =
                ImmutableMap.of("GlobalMetadata", rsp.globalMetadata, "SeriesMetadata", rsp.seriesMetadata);
        Map<String, RType> currentSection = null;
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
            while (true) {
                String line;
                line = in.readLine();
                if (line == null) {
                    break;
                }
                Matcher matcher;
                if ((matcher = section.matcher(line)).matches()) {
                    currentSection = sections.get(matcher.group(1));
                } else if (currentSection != null) {
                    final Entry<String, String> keyValue = splitOnEquals(line);
                    if (keyValue != null) {
                        currentSection.put(keyValue.getKey(), omero.rtypes.rstring(keyValue.getValue()));
                    }
                }
            }
        } catch (IOException e) {
            if (helper != null) {
                helper.cancel(new ERR(), e, "reader-failure", "original-metadata", file.getPath());
            }
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) { }
            }
        }
    }

	public static void main(String[] args) throws Exception {
		OriginalMetadataRequestI omr = new OriginalMetadataRequestI(null);
		ImageReader reader = new ImageReader();
		for (String file : args) {
			reader.setId(file);
			final Hashtable<String, Object> bfglobal = reader.getGlobalMetadata();
			final Hashtable<String, Object> bfseries = reader.getSeriesMetadata();
			Map<String, RType> global = omr.wrap(bfglobal);
			Map<String, RType> series = omr.wrap(bfseries);
			printMap("[GlobalMetadata]", global);
			printMap("[SeriesMetadata]", series);
		}
		reader.close();
	}

	private static void printMap(String title, Map<String, RType> map) {
		System.out.println(title);
		for (Map.Entry<String, RType> entry : map.entrySet()) {
			System.out.print(entry.getKey());
			System.out.print("=");
			System.out.print(entry.getValue());
			System.out.print("\n");
		}
	}
}
