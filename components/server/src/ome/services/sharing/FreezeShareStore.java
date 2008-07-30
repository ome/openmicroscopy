/*
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.sharing;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ome.api.IShare;
import ome.model.IObject;
import ome.services.sharing.data.ShareData;
import ome.services.sharing.data.ShareItem;
import ome.services.sharing.data.ShareItems;
import ome.services.sharing.data.ShareMap;

import org.springframework.util.Assert;

/**
 * Entry to the Ice code generated data/ directory.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta4
 * @see IShare
 */
public class FreezeShareStore extends ShareStore {

    final protected String envRoot;

    final protected String envMap;

    final protected String envItems;

    final protected File envLocation;

    final protected Freeze.Connection conn;

    // Initialization/Destruction
    // =========================================================================

    public FreezeShareStore(String root, File location) {
        Assert.notNull(root);
        Assert.notNull(location);
        this.envRoot = root;
        this.envMap = root + "Map";
        this.envItems = root + "Items";
        this.envLocation = location;
        conn = Freeze.Util.createConnection(ic, envLocation.getAbsolutePath());
    }

    @Override
    public void doInit() {
        ShareMap map = new ShareMap(conn, envMap, true);
        map.close();
        ShareItems items = new ShareItems(conn, envItems, true);
        items.close();
    }

    // Overrides
    // =========================================================================

    @Override
    public int totalShares() {
        ShareMap map = new ShareMap(conn, envMap, false);
        try {
            int mapsize = map.size();
            return mapsize;
        } finally {
            map.close();
        }
    }

    @Override
    public int totalSharedItems() {
        ShareItems items = new ShareItems(conn, envItems, false);
        try {
            int itemssize = items.size();
            return itemssize;
        } finally {
            items.close();
        }
    }

    @Override
    public <T extends IObject> boolean doContains(long sessionId, Class<T> kls,
            long objId) {
        throw new UnsupportedOperationException("NYI");
    }

    @Override
    public void doClose() {
        if (conn != null) {
            Ice.Communicator ic = conn.getCommunicator();
            try {
                conn.close();
            } catch (Exception e) {
                log.error("Error closing store", e);
            }
        }
    }

    // User Methods
    // =========================================================================

    @Override
    public void doSet(ShareData data, List<ShareItem> items) {
        Freeze._TransactionOperationsNC tx = conn.beginTransaction();
        ShareMap map = new ShareMap(conn, envMap, false);
        ShareItems shareItems = new ShareItems(conn, envItems, false);
        try {
            map.put(data.id, data);
            for (ShareItem shareItem : items) {
                shareItems.put(shareItem.type + ":_" + shareItem.id, shareItem);
            }
        } catch (Exception e) {
            tx.rollback();
            tx = null;
            throw new RuntimeException(e);
        } finally {
            if (tx != null) {
                tx.commit();
            }
        }
    }

    @Override
    public ShareData get(long id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ShareData> getShares(boolean active) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ShareData> getShares(long userId, boolean own, boolean active) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Long> keys() {
        ShareMap map = new ShareMap(conn, envMap, false);
        return map.keySet();
    }

    // Helper Methods
    // =========================================================================

    private <T extends IObject> List<ShareItem> asItems(long share,
            List<T> items, List<Long> members, List<String> guests) {
        List<ShareItem> shareItems = new ArrayList<ShareItem>(items.size());
        for (T item : items) {
            ShareItem shareItem = new ShareItem();
            shareItem.share = share;
            shareItem.id = item.getId();
            shareItem.type = item.getClass().getName();
            shareItem.members = new ArrayList<Long>(members);
            shareItem.guests = new ArrayList<String>(guests);
            shareItems.add(shareItem);
        }
        return shareItems;
    }

    private <T extends IObject> Map<String, List<Long>> map(List<T> items) {
        Map<String, List<Long>> map = new HashMap<String, List<Long>>();
        for (T t : items) {
            String kls = t.getClass().getName();
            List<Long> ids = map.get(kls);
            if (ids == null) {
                ids = new ArrayList<Long>();
                map.put(kls, ids);
            }
            ids.add(t.getId());
        }
        return map;
    }

}
