/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.testing;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;

import ome.api.RawFileStore;
import ome.conditions.ApiUsageException;
import ome.model.core.OriginalFile;
import ome.model.enums.Format;
import ome.model.internal.Permissions;
import ome.model.meta.Experimenter;
import ome.system.ServiceFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Utility to upload {@link File files} or {@link String text} as as an
 * {@link OriginalFile}.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public class FileUploader implements Runnable {

    private final static Log log = LogFactory.getLog(FileUploader.class);

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

    public FileUploader(ServiceFactory sf, File file) throws Exception {
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
        ofile.setSha1(bufferToSha1(rBuf));

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
        rSha1 = bufferToSha1(buf);

        assert ofile.getName() != null;
        assert ofile.getPath() != null;

        if (ofile.getFormat() == null) {
            ofile.setFormat(new Format("text/plain"));
        }

    }

    private void handleFile() throws Exception {
        rSize = file.length();
        rBuf = new byte[(int) rSize];
        FileInputStream fis = new FileInputStream(file);
        assert (int) rSize == fis.read(rBuf) : "read whole file";
        rSha1 = bufferToSha1(rBuf);

        if (ofile.getName() == null) {
            ofile.setName(file.getName());
        }

        if (ofile.getPath() == null) {
            ofile.setPath(file.getAbsolutePath());
        }

        if (ofile.getFormat() == null) {
            ofile.setFormat(new Format("text/plain"));
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

    // Copied from server/test/ome/io/nio/Helper.java
    private String bufferToSha1(byte[] buffer) {
        MessageDigest md;

        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(
                    "Required SHA-1 message digest algorithm unavailable.");
        }

        md.reset();
        md.update(buffer);
        byte[] digest = md.digest();

        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < digest.length; i++) {
            buf.append(byteToHex(digest[i]));
        }
        return buf.toString();
    }

    private static String byteToHex(byte data) {
        StringBuffer buf = new StringBuffer();
        buf.append(toHexChar(data >>> 4 & 0x0F));
        buf.append(toHexChar(data & 0x0F));
        return buf.toString();
    }

    private static char toHexChar(int i) {
        if (0 <= i && i <= 9) {
            return (char) ('0' + i);
        } else {
            return (char) ('a' + i - 10);
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

    public synchronized String getFormat() {
        return ofile.getFormat() == null ? null : ofile.getFormat().getValue();
    }

    public synchronized void setFormat(String format) {
        checkLocked();
        ofile.setFormat(new Format(format));
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