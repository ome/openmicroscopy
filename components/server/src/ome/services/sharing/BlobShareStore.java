/*
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.sharing;

import java.sql.Types;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ome.api.IShare;
import ome.model.IObject;
import ome.services.sharing.data.ShareData;
import ome.services.sharing.data.ShareItem;

import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.jdbc.core.support.SqlLobValue;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.util.Assert;

import Ice.ReadObjectCallback;

/**
 * 
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta4
 * @see IShare
 */
public class BlobShareStore extends ShareStore {

    /**
     * Jdbc template to be used for system calls. Does not particpate in
     * hibernate sessions.
     */
    final protected JdbcOperations sysjdbc;

    /**
     * Jdbc templates to be used for user calls. Will take part in hibernate
     * sessions.
     */
    final protected JdbcOperations userjdbc;

    final protected LobHandler handler;

    // Initialization/Destruction
    // =========================================================================

    /**
     * 
     */
    public BlobShareStore(SimpleJdbcTemplate user, SimpleJdbcTemplate sys,
            LobHandler handler) {
        Assert.notNull(user);
        Assert.notNull(sys);
        this.userjdbc = user.getJdbcOperations();
        this.sysjdbc = sys.getJdbcOperations();
        this.handler = handler;
    }

    @Override
    public void doInit() {
        if (1 != sysjdbc
                .queryForInt(
                        "SELECT sum(CASE WHEN t.typname=? THEN 1 ELSE 0 END) FROM pg_type t",
                        new Object[] { "private_shares" },
                        new int[] { Types.VARCHAR })) {
            sysjdbc
                    .execute("CREATE TABLE private_shares "
                            + "(id BIGINT PRIMARY KEY, item_count INTEGER, data BYTEA)");
        }
    }

    // Overrides
    // =========================================================================

    @Override
    public int totalShares() {
        return sysjdbc.queryForInt("select count(id) from private_shares");
    }

    @Override
    public int totalSharedItems() {
        return sysjdbc
                .queryForInt("select sum(item_count) from private_shares");
    }

    @Override
    public void doSet(ShareData data, List<ShareItem> items) {
        Ice.OutputStream os = Ice.Util.createOutputStream(ic);
        byte[] bytes = null;
        try {
            os.writeObject(data);
            os.writePendingObjects();
            bytes = os.finished();
        } finally {
            os.destroy();
        }

        userjdbc
                .update(
                        "INSERT INTO private_shares (id, item_count, data) VALUES (?, ?, ?)",
                        new Object[] { data.id, items.size(),
                                new SqlLobValue(bytes, handler) }, new int[] {
                                Types.BIGINT, Types.INTEGER, Types.BLOB });
    }

    @Override
    public ShareData get(long id) {
        byte[] data = (byte[]) userjdbc.queryForObject(
                "select data from private_shares where id = ?",
                new Object[] { id }, new int[] { Types.BIGINT }, byte[].class);
        Ice.InputStream is = Ice.Util.createInputStream(ic, data);
        final ShareData[] shareData = new ShareData[1];
        try {
            is.readObject(new ReadObjectCallback() {

                public void invoke(Ice.Object arg0) {
                    shareData[0] = (ShareData) arg0;
                }
            });
            is.readPendingObjects();
        } finally {
            is.destroy();
        }
        return shareData[0];
    }

    @Override
    public <T extends IObject> boolean doContains(long sessionId, Class<T> kls,
            long objId) {
        ShareData data = get(sessionId);
        List<Long> ids = data.objects.get(kls.getName());
        return ids.contains(objId);
    }

    @Override
    public void doClose() {
        // no-op
    }

    @Override
    public Set<Long> keys() {
        return new HashSet<Long>(userjdbc.queryForList(
                "select id from seq_private_shares", Long.class));
    }

}
