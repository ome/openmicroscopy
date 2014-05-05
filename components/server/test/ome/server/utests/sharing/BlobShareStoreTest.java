/*
 *   $Id$
 *
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.utests.sharing;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import ome.services.sharing.BlobShareStore;
import ome.services.sharing.data.Obj;
import ome.services.sharing.data.ShareData;

import org.springframework.util.ResourceUtils;
import org.testng.annotations.Test;

/**
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 4.4.0
 */
@Test(groups = "sharing")
public class BlobShareStoreTest extends TestCase {

    BlobShareStore store = new BlobShareStore();

    /**
     * Test whether different versions of blobs (i.e. created under older/newer
     * Ice versions) are still readable by the current Ice version.
     */
    public void testReadBlobs() throws Exception {
        ShareData[] data = loadData();
        assertEquals(3, data.length);
        ShareData d = data[0];
        for (int i = 1; i < data.length; i++) {
            ShareData t = data[i];
            assertEquals(d.enabled, t.enabled);
            assertEquals(d.id, t.id);
            assertEquals(d.optlock, t.optlock);
            assertEquals(d.owner, t.owner);
            assertEquals(d.guests.toString(), t.guests.toString());
            assertEquals(d.members.toString(), t.members.toString());
            assertEquals(toString(d.objectList), toString(t.objectList));
            assertEquals(toString(d.objectMap), toString(d.objectMap));
        }
    }

    String toString(List<Obj> objs) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (Obj obj : objs) {
            sb.append("Obj(");
            sb.append(obj.type);
            sb.append(":");
            sb.append(obj.id);
            sb.append(")");
        }
        sb.append("]");
        return sb.toString();
    }

    String toString(Map<String, List<Long>> map) {
        List<String> keys = new ArrayList<String>(map.keySet());
        Collections.sort(keys);

        StringBuilder sb = new StringBuilder();
        sb.append("[");

        for (String key : keys) {
            sb.append(key);
            sb.append("=");
            sb.append(map.get(key).toString());
            sb.append(" ");
        }
        sb.append("]");
        return sb.toString();
    }


    ShareData[] loadData() throws Exception {
        File[] blobs = loadBlobs();
        ShareData[] datas = new ShareData[blobs.length];
        for (int i = 0; i < blobs.length; i++) {
            File blob = blobs[i];
            byte[] buf = loadFile(blob);
            datas[i] = store.parse(1, buf);
        }
        return datas;
    }

    byte[] loadFile(File file) throws Exception{
        int size = (int) file.length();
        FileInputStream fis = new FileInputStream(file);
        byte[] buf = new byte[size];
        fis.read(buf);
        fis.close();
        return buf;
    }

    File[] loadBlobs() throws Exception {
        File f33 = ResourceUtils
                .getFile("classpath:ome/server/utests/sharing/33.blob");
        File f34 = ResourceUtils
                .getFile("classpath:ome/server/utests/sharing/34.blob");
        File f35 = ResourceUtils
                .getFile("classpath:ome/server/utests/sharing/35.blob");
        return new File[] { f35, f34, f33 };
    }

}
