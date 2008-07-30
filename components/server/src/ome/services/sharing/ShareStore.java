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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;

/**
 * Entry to the Ice code generated data/ directory.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta4
 * @see IShare
 */
public class ShareStore {

    final private static Log log = LogFactory.getLog(ShareStore.class);

    final protected String envRoot;

    final protected String envMap;

    final protected String envItems;

    final protected File envLocation;

    final protected Freeze.Connection conn;

    // Initialization/Destruction
    // =========================================================================

    public ShareStore(String root, File location) {
        Assert.notNull(root);
        Assert.notNull(location);
        this.envRoot = root;
        this.envMap = root + "Map";
        this.envItems = root + "Items";
        this.envLocation = location;

        Ice.Communicator ic = null;
        try {
            ic = Ice.Util.initialize(new String[] {});
            conn = Freeze.Util.createConnection(ic, envLocation
                    .getAbsolutePath());
            ShareMap map = new ShareMap(conn, envMap, true);
            ShareItems items = new ShareItems(conn, envItems, true);
            int mapsize = map.size();
            int itemssize = items.size();
            log.info("Loaded store with " + mapsize + " shares and "
                    + itemssize + " objects");
            map.close();
            items.close();
        } catch (Exception e) {
            if (ic != null) {
                ic.destroy();
            }
            throw new RuntimeException(e);
        }
    }

    public void close() {
        if (conn != null) {
            Ice.Communicator ic = conn.getCommunicator();
            try {
                conn.close();
            } catch (Exception e) {
                log.error("Error closing store", e);
            }
            if (ic != null) {
                ic.destroy();
            }
        }
    }

    // User Methods
    // =========================================================================

    public <T extends IObject> void set(long id, String owner, List<T> objects,
            List<Long> members, List<String> guests, boolean enabled) {
        ShareData data = new ShareData();
        data.id = id;
        data.owner = owner;
        data.members = new ArrayList<Long>(members);
        data.guests = new ArrayList<String>(guests);
        data.enabled = enabled;
        data.objects = map(objects);

        List<ShareItem> shareItems = asItems(objects, members, guests);

        Freeze._TransactionOperationsNC tx = conn.beginTransaction();
        ShareMap map = new ShareMap(conn, envMap, false);
        ShareItems items = new ShareItems(conn, envItems, false);
        try {
            map.put(id, data);
            for (ShareItem shareItem : shareItems) {
                items.put(shareItem.type + ":_" + shareItem.id, shareItem);
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

    public Set<Long> keys() {
        ShareMap map = new ShareMap(conn, envMap, false);
        return map.keySet();
    }

    // User Methods
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
