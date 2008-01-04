/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.client.utests.session;

import junit.framework.TestCase;
import ome.client.Storage;
import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.model.core.Image;

import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

public class StorageTest extends TestCase {

    Storage storage;

    @Override
    @Configuration(beforeTestMethod = true)
    protected void setUp() throws Exception {
        storage = new Storage();
    }

    @Test
    public void test_store_persistent() throws Exception {
        try {
            storage.storePersistent(new Project());
            fail("Should have have thrown.");
        } catch (IllegalArgumentException e) {
            // good
        }

        storage.storePersistent(new Project(new Long(1), true));
        Project p = (Project) storage
                .findPersistent(Project.class, new Long(1));
        assertNotNull(p);
        assertTrue(storage.isPersistent(Project.class, new Long(1)));

        storage.storePersistent(new Image(new Long(2), true));
        Image i = (Image) storage.findPersistent(Image.class, new Long(2));
        assertNotNull(i);
        assertTrue(storage.isPersistent(Image.class, new Long(2)));

        Dataset d = (Dataset) storage
                .findPersistent(Dataset.class, new Long(0));
        assertNull(d);

        assertFalse(storage.isPersistent(Dataset.class, new Long(0)));
    }

    @Test
    public void test_make_dirty() throws Exception {
        Dataset d;

        d = new Dataset();
        try {
            storage.storeDirty(d);
            fail("Should have thrown.");
        } catch (IllegalArgumentException e) {
            // good;
        }

        d = new Dataset(new Long(1), true);
        storage.storeDirty(d);
        assertTrue(storage.isDirty(Dataset.class, new Long(1)));
    }

    @Test
    public void test_new_new() throws Exception {
        Image i;

        i = new Image(new Long(1), true);
        try {
            storage.storeTransient(i);
            fail("Should have thrown.");
        } catch (IllegalArgumentException e) {
            // good;
        }

        i = new Image();
        storage.storeTransient(i);
        assertTrue(storage.isTransient(i));

    }

    @Test
    public void test_deleted() throws Exception {
        Image i;

        i = new Image();
        try {
            storage.storeDeleted(i);
            fail("Should have thrown.");
        } catch (IllegalArgumentException e) {
            // good;
        }

        i = new Image(new Long(1), true);
        storage.storeDeleted(i);
        assertTrue(storage.isDeleted(Image.class, new Long(1)));

    }

}
