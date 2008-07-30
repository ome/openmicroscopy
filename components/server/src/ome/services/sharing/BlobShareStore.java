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

    final protected JdbcOperations jdbc;

    final protected LobHandler handler;

    // Initialization/Destruction
    // =========================================================================

    /**
     * 
     */
    public BlobShareStore(SimpleJdbcTemplate template, LobHandler handler) {
        Assert.notNull(template);
        Assert.notNull(handler);
        this.jdbc = template.getJdbcOperations();
        this.handler = handler;
    }

    @Override
    public void doInit() {
        this.jdbc
                .execute("CREATE TABLE private_shares (id BIGINT PRIMARY KEY, data bytea)");
        this.jdbc.execute("CREATE SEQUENCE seq_private_shares");

    }

    // Overrides
    // =========================================================================

    @Override
    public int totalShares() {
        return jdbc.queryForInt("select count(id) from private_shares");
    }

    @Override
    public int totalSharedItems() {
        return jdbc
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

        long id = jdbc.queryForLong("SELECT nextval('seq_private_shares')");
        jdbc.update("INSERT INTO private_shares (id, data) VALUES (?, ?)",
                new Object[] { id, new SqlLobValue(bytes, handler) },
                new int[] { Types.BIGINT, Types.BLOB });
    }

    @Override
    public ShareData get(long id) {
        byte[] data = (byte[]) jdbc.queryForObject(
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
    public void doClose() {
        // no-op
    }

    @Override
    public Set<Long> keys() {
        return new HashSet<Long>(jdbc.queryForList(
                "select id from seq_private_shares", Long.class));
    }

}
