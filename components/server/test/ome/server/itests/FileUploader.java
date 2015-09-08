/*
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.server.itests;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Timestamp;

import ome.api.RawFileStore;
import ome.conditions.ApiUsageException;
import ome.model.core.OriginalFile;
import ome.model.internal.Permissions;
import ome.model.meta.Experimenter;
import ome.system.ServiceFactory;
import ome.util.checksum.ChecksumProviderFactory;
import ome.util.checksum.ChecksumProviderFactoryImpl;
import ome.util.checksum.ChecksumType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility to upload {@link File files} or {@link String text} as as an
 * {@link OriginalFile}.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public class FileUploader implements Runnable {

    private final static Logger log = LoggerFactory.getLogger(FileUploader.class);

    // Ctor fields
    private final ServiceFactory sf;
    private final File file;
    private final String text;

    // Non-configurable fields, calculated by handle* methods
    private long rSize = 0L;
    private byte[] rBuf = null;
    private String rSha1 = null;
    private boolean locked = false;

    // Main target
    private OriginalFile ofile = new OriginalFile();

    // Collaborators
    private final ChecksumProviderFactory cpf = new ChecksumProviderFactoryImpl();

    public FileUploader(ServiceFactory sf, File file)
            throws Exception {
        if (sf == null || file == null) {
            throw new ApiUsageException("Non null arguments.");
        }
        this.sf = sf;
        this.file = file;
        this.text = null;
    }

    public FileUploader(ServiceFactory sf, String text, String name, String path)
            throws Exception {
        if (sf == null || text == null || name == null || path == null) {
            throw new ApiUsageException("Non null arguments.");
        }
        this.sf = sf;
        this.file = null;
        this.text = text;
        ofile.setName(name);
        ofile.setPath(path);
    }

    public synchronized void init() {

        locked = true;

        File target;
        try {
            if (text != null) {
                handleString();
            } else {
                handleFile();
            }
        } catch (Exception e) {
            String msg = "Error handling " + (text == null ? "file" : "text")
                    + " for file upload.";
            log.error(msg, e);
            throw new RuntimeException(msg, e);
        }

        // Non-configurable
        ofile.setSize(rSize);
        ofile.setHash(this.cpf.getProvider(ChecksumType.SHA1).putBytes(rBuf).checksumAsString());

    }

    public synchronized void run() {

        init();
        ofile = sf.getUpdateService().saveAndReturnObject(ofile);

        // Upload file
        RawFileStore rfs = sf.createRawFileStore();
        rfs.setFileId(ofile.getId());
        rfs.write(rBuf, 0L, (int) rSize);
        rfs.close();

    }

    private void handleString() throws Exception {

        byte[] buf = text.getBytes();

        rSize = buf.length;
        rBuf = buf;
        rSha1 = this.cpf.getProvider(ChecksumType.SHA1).putBytes(buf).checksumAsString();

        assert ofile.getName() != null;
        assert ofile.getPath() != null;

        if (ofile.getMimetype() == null) {
            ofile.setMimetype("text/plain"); // ticket:2211 - FIXME should this be octet-stream
        }

    }

    private void handleFile() throws Exception {
        rSize = file.length();
        rBuf = new byte[(int) rSize];
        FileInputStream fis = new FileInputStream(file);
        assert (int) rSize == fis.read(rBuf) : "read whole file";
        rSha1 = this.cpf.getProvider(ChecksumType.SHA1).putBytes(rBuf).checksumAsString();

        if (ofile.getName() == null) {
            ofile.setName(file.getName());
        }

        if (ofile.getPath() == null) {
            ofile.setPath(file.getAbsolutePath());
        }

        if (ofile.getMimetype() == null) {
            ofile.setMimetype("text/plain"); // ticket:2211 - FIXME should this be octet-stream
        }

    }

    private void checkLocked() {
        if (locked) {
            throw new ApiUsageException("File already uploaded.");
        }
    }

    private void checkAdmin() {
        boolean admin = sf.getAdminService().getEventContext()
                .isCurrentUserAdmin();
        if (!admin) {
            throw new ApiUsageException(
                    "Owner and group can only be set by admins.");
        }
    }

    // Accessors

    public synchronized Long getId() {
        return ofile.getId();
    }

    public synchronized String getName() {
        return ofile.getName();
    }

    public synchronized void setName(String name) {
        checkLocked();
        ofile.setName(name);
    }

    public synchronized String getPath() {
        return ofile.getPath();
    }

    public synchronized void setPath(String path) {
        checkLocked();
        ofile.setPath(path);
    }

    public synchronized String getMimetype() {
        return ofile.getMimetype() == null ? null : ofile.getMimetype();
    }

    public synchronized void setMimetype(String mimetype) {
        checkLocked();
        ofile.setMimetype(mimetype);
    }

    public synchronized Timestamp getAtime() {
        return ofile.getAtime();
    }

    public synchronized void setAtime(Timestamp atime) {
        checkLocked();
        ofile.setAtime(atime);
    }

    public synchronized Timestamp getMtime() {
        return ofile.getMtime();
    }

    public synchronized void setMtime(Timestamp mtime) {
        checkLocked();
        ofile.setMtime(mtime);
    }

    public synchronized Timestamp getCtime() {
        return ofile.getCtime();
    }

    public synchronized void setCtime(Timestamp ctime) {
        checkLocked();
        ofile.setCtime(ctime);
    }

    public synchronized Permissions getPerms() {
        return ofile.getDetails().getPermissions();
    }

    public synchronized void setPerms(Permissions perms) {
        checkLocked();
        ofile.getDetails().setPermissions(perms);
    }

    public synchronized String getOwner() {
        return ofile.getDetails().getOwner() == null ? null : ofile
                .getDetails().getOwner().getOmeName();
    }

    public synchronized void setOwner(String owner) {
        checkLocked();
        checkAdmin();
        Experimenter e = sf.getAdminService().lookupExperimenter(owner);
        ofile.getDetails().setOwner(e);
    }

    public synchronized String getGroup() {
        return ofile.getDetails().getGroup() == null ? null : ofile
                .getDetails().getGroup().getName();
    }

    public synchronized void setGroup(String group) {
        checkLocked();
        checkAdmin();
        Experimenter e = sf.getAdminService().lookupExperimenter(group);
        ofile.getDetails().setOwner(e);
    }
}
