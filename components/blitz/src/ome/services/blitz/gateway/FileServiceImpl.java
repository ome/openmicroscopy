/*
 * blitzgateway.service.FileServiceImpl 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package ome.services.blitz.gateway;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import omero.RType;
import omero.api.IQueryPrx;
import omero.api.IScriptPrx;
import omero.model.Format;
import omero.model.OriginalFile;

/**
 * 
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Revision: $Date: $)
 *          </small>
 * @since OME3.0
 */
class FileServiceImpl implements FileService {

    IScriptPrx scriptService;
    RawFileStoreService rawFileStore;
    IQueryPrx iQuery;

    /**
     * Create the FileService passing the gateway.
     * 
     * @param gateway
     */
    FileServiceImpl(RawFileStoreService fileStore, IScriptPrx script,
            IQueryPrx query) {
        rawFileStore = fileStore;
        scriptService = script;
        iQuery = query;
    }


	/* (non-Javadoc)
	 * @see omerojava.service.FileService#closeRawFileService(long)
	 */
	public void closeRawFileService(long fileId) throws omero.ServerError
	{
		rawFileStore.closeGateway(fileId);
	}

    /*
     * (non-Javadoc)
     * 
     * @see blitzgateway.service.FileService#fileExists(java.lang.String)
     */
    public boolean fileExists(long id) throws omero.ServerError {
        return rawFileStore.exists(id);
    }

    public OriginalFile getOriginalFile(long id) throws omero.ServerError {
        return (OriginalFile) iQuery.findByQuery(
                "from OriginalFile as o left outer "
                        + "join fetch o.format as f where o.id = " + id, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see blitzgateway.service.FileService#findFile(java.lang.String,
     *      omero.model.Format)
     */
    public List<Long> findFile(String fileName, Format fmt)
            throws omero.ServerError {
        String queryStr = "from OriginalFile as o left outer join fetch "
                + "o.format as f where f.value = " + fmt.getValue().getValue();
        List<OriginalFile> list = ServiceUtilities.collectionCast(
                OriginalFile.class, iQuery.findAllByQuery(queryStr, null));
        List<Long> lList = new ArrayList<Long>();
        for (OriginalFile file : list) {
            lList.add(new Long(file.getId().getValue()));
        }
        return lList;
    }

    /*
     * (non-Javadoc)
     * 
     * @see blitzgateway.service.FileService#getAllFormats()
     */
    public List<Format> getAllFormats() throws omero.ServerError {
        return ServiceUtilities.collectionCast(Format.class, iQuery
                .findAllByQuery("from Format as format", null));
    }

    /*
     * (non-Javadoc)
     * 
     * @see blitzgateway.service.FileService#getFileAsString(long)
     */
    public String getFileAsString(long id) throws omero.ServerError {
        byte[] data = getRawFile(id);
        return new String(data);
    }

    /*
     * (non-Javadoc)
     * 
     * @see blitzgateway.service.FileService#getFileFormat(long)
     */
    public Format getFileFormat(long id) throws omero.ServerError {
        OriginalFile file = getOriginalFile(id);
        return file.getFormat();
    }

    /*
     * (non-Javadoc)
     * 
     * @see blitzgateway.service.FileService#getFormat(java.lang.String)
     */
    public Format getFormat(String fmt) throws omero.ServerError {
        return (Format) iQuery.findByString("Format", "value", fmt);
    }

    /*
     * (non-Javadoc)
     * 
     * @see blitzgateway.service.FileService#getRawFile(long)
     */
    public byte[] getRawFile(long id) throws omero.ServerError {
        OriginalFile file = getOriginalFile(id);
        return rawFileStore.read(id, 0, (int) file.getSize().getValue());
    }

    /*
     * (non-Javadoc)
     * 
     * @see blitzgateway.service.FileService#fileExists(java.lang.Long)
     */
    public boolean fileExists(Long id) throws omero.ServerError {
        return rawFileStore.exists(id);
    }

    /*
     * (non-Javadoc)
     * 
     * @see blitzgateway.service.FileService#deleteScript(long)
     */
    public void deleteScript(long id) throws omero.ServerError {
        scriptService.deleteScript(id);
    }

    /*
     * (non-Javadoc)
     * 
     * @see blitzgateway.service.FileService#getParams(long)
     */
    public Map<String, RType> getParams(long id) throws omero.ServerError {
        return scriptService.getParams(id);
    }

    /*
     * (non-Javadoc)
     * 
     * @see blitzgateway.service.FileService#getScript(long)
     */
    public String getScript(long id) throws omero.ServerError {
        return scriptService.getScript(id);
    }

    /*
     * (non-Javadoc)
     * 
     * @see blitzgateway.service.FileService#getScriptID(java.lang.String)
     */
    public long getScriptID(String name) throws omero.ServerError {
        return scriptService.getScriptID(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see blitzgateway.service.FileService#getScripts()
     */
    public Map<Long, String> getScripts() throws omero.ServerError {
        return scriptService.getScripts();
    }

    /*
     * (non-Javadoc)
     * 
     * @see blitzgateway.service.FileService#runScript(long, java.util.Map)
     */
    public Map<String, RType> runScript(long id, Map<String, RType> map)
            throws omero.ServerError {
        return scriptService.runScript(id, map);
    }

    /*
     * (non-Javadoc)
     * 
     * @see blitzgateway.service.FileService#uploadScript(java.lang.String)
     */
    public long uploadScript(String script) throws omero.ServerError {
        return scriptService.uploadScript(script);
    }

    /*
     * (non-Javadoc)
     * 
     * @see blitzgateway.service.FileService#keepAlive()
     */
    public void keepAlive() throws omero.ServerError {
        rawFileStore.keepAlive();
    }

}
