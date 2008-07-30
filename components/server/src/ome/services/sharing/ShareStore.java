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
import ome.model.meta.Share;
import ome.services.sharing.data.Obj;
import ome.services.sharing.data.ShareData;
import ome.services.sharing.data.ShareItem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.orm.hibernate3.HibernateTemplate;

import Ice.ReadObjectCallback;

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

    public <T extends IObject> void set(Share share, long owner,
            List<T> objects, List<Long> members, List<String> guests,
            boolean enabled) {
        ShareData data = new ShareData();
        data.id = share.getId();
        data.owner = owner;
        data.members = new ArrayList<Long>(members);
        data.guests = new ArrayList<String>(guests);
        data.enabled = enabled;
        data.objectMap = map(objects);
        data.objectList = list(objects);

        List<ShareItem> shareItems = asItems(share.getId(), objects, members,
                guests);

        doSet(share, data, shareItems);

    }

    public void update(Share share, ShareData data) {
        List<ShareItem> shareItems = asItems(data);
        doSet(share, data, shareItems);
    }

    // Parsing
    // =========================================================================

    public final byte[] parse(ShareData data) {
        Ice.OutputStream os = Ice.Util.createOutputStream(ic);
        byte[] bytes = null;
        try {
            os.writeObject(data);
            os.writePendingObjects();
            bytes = os.finished();
        } finally {
            os.destroy();
        }
        return bytes;
    }

    public final ShareData parse(byte[] data) {

        if (data == null) {
            return null; // EARLY EXIT!
        }

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

    // Template methods
    // =========================================================================

    /**
     * Calls {@link #doInit()} within a transaction with a session available to
     * all {@link HibernateTemplate} callbacks.
     */
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

    public final <T extends IObject> boolean contains(long sessionId,
            Class<T> kls, long objId) {
        return doContains(sessionId, kls, objId);
    }

    // Abstract Methods
    // =========================================================================

    public abstract void doInit();

    public abstract int totalShares();

    public abstract int totalSharedItems();

    public abstract Set<Long> keys();

    public abstract ShareData get(long id);

    public abstract List<ShareData> getShares(boolean active);

    public abstract List<ShareData> getShares(long userId, boolean own,
            boolean active);

    public abstract <T extends IObject> boolean doContains(long sessionId,
            Class<T> kls, long objId);

    public abstract void doSet(Share share, ShareData data,
            List<ShareItem> items);

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

    private <T extends IObject> List<ShareItem> asItems(ShareData data) {
        Map<String, List<Long>> map = data.objectMap;

        List<ShareItem> shareItems = new ArrayList<ShareItem>();
        for (String type : map.keySet()) {
            for (Long id : map.get(type)) {
                ShareItem shareItem = new ShareItem();
                shareItem.share = data.id;
                shareItem.id = id;
                shareItem.type = type;
                shareItem.members = data.members;
                shareItem.guests = data.guests;
                shareItems.add(shareItem);
            }
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

    private <T extends IObject> List<Obj> list(List<T> items) {
        List<Obj> objList = new ArrayList<Obj>();
        for (T t : items) {
            Obj obj = new Obj();
            String kls = t.getClass().getName();
            obj.type = kls;
            obj.id = t.getId();
            objList.add(obj);
        }
        return objList;
    }
}
