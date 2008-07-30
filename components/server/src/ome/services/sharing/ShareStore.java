/*
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.sharing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ome.api.IShare;
import ome.model.IObject;
import ome.services.sharing.data.ShareData;
import ome.services.sharing.data.ShareItem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Entry to the Ice code generated data/ directory. Subclasess of
 * {@link ShareStore} know how to efficiently store and look up
 * {@link ShareData} instances.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta4
 * @see IShare
 */
public abstract class ShareStore {

    final protected Log log = LogFactory.getLog(this.getClass());

    final protected Ice.Communicator ic = Ice.Util.initialize();

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

        List<ShareItem> shareItems = asItems(id, objects, members, guests);

        doSet(data, shareItems);

    }

    public final void init() {
        doInit();
        int mapsize = totalShares();
        int itemssize = totalSharedItems();
        log.info("Loaded store " + this + " with " + mapsize + " shares and "
                + itemssize + " objects");

    }

    public final void close() {
        try {
            doClose();
        } finally {
            ic.destroy();
        }
    }

    // Abstract Methods
    // =========================================================================

    public abstract void doInit();

    public abstract int totalShares();

    public abstract int totalSharedItems();

    public abstract Set<Long> keys();

    public abstract ShareData get(long id);

    public abstract void doSet(ShareData data, List<ShareItem> items);

    public abstract void doClose();

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
