/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.blitz.test.utests;

import static ome.model.internal.Permissions.Role.*;
import static ome.model.internal.Permissions.Right.*;
import static ome.model.internal.Permissions.Flag.*;

import junit.framework.TestCase;

import ome.model.internal.Permissions;
import ome.util.Utils;
import omero.model.PermissionsI;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups = "ticket:685")
public class PermissionsITest extends TestCase {

    Permissions p;
    PermissionsI pI;
    
    @BeforeMethod
    public void configure() throws Exception {
        p = new Permissions();
        pI = new PermissionsI();
        pI.setPerm1( (Long) Utils.internalForm(p) );
        verify(p,pI);
    }

    public void testPermissionsDefault() throws Exception {
        pI = new PermissionsI();
        assertFalse(pI.isUserRead());
        assertFalse(pI.isUserAnnotate());
        assertFalse(pI.isUserWrite());
        assertFalse(pI.isGroupRead());
        assertFalse(pI.isGroupAnnotate());
        assertFalse(pI.isGroupWrite());
        assertFalse(pI.isWorldRead());
        assertFalse(pI.isWorldAnnotate());
        assertFalse(pI.isWorldWrite());
    }
    
    public void testPermissionsUserRead() throws Exception {
        p.grant(USER, READ);
        pI.setUserRead(true);
        verify(p,pI);
        p.revoke(USER, READ);
        pI.setUserRead(false);
        verify(p,pI);
    }

    public void testPermissionsUserAnnotate() throws Exception {
        p.grant(USER, ANNOTATE);
        pI.setUserAnnotate(true);
        verify(p,pI);
        p.revoke(USER, ANNOTATE);
        pI.setUserAnnotate(false);
        verify(p,pI);
    }

    public void testPermissionsUserWrite() throws Exception {
        p.grant(USER, WRITE);
        pI.setUserWrite(true);
        verify(p,pI);
        p.revoke(USER, WRITE);
        pI.setUserWrite(false);
        verify(p,pI);
    }

    public void testPermissionsGroupRead() throws Exception {
        p.grant(GROUP, READ);
        pI.setGroupRead(true);
        verify(p,pI);
        p.revoke(GROUP, READ);
        pI.setGroupRead(false);
        verify(p,pI);
    }
    
    public void testPermissionsGroupAnnotate() throws Exception {
        p.grant(GROUP, ANNOTATE);
        pI.setGroupAnnotate(true);
        verify(p,pI);
        p.revoke(GROUP, ANNOTATE);
        pI.setGroupAnnotate(false);
        verify(p,pI);
    }

    public void testPermissionsGroupWrite() throws Exception {
        p.grant(GROUP, WRITE);
        pI.setGroupWrite(true);
        verify(p,pI);
        p.revoke(GROUP, WRITE);
        pI.setGroupWrite(false);
        verify(p,pI);
    }
    
    public void testPermissionsWorldRead() throws Exception {
        p.grant(WORLD, READ);
        pI.setWorldRead(true);
        verify(p,pI);
        p.revoke(WORLD, READ);
        pI.setWorldRead(false);
        verify(p,pI);
    }

    public void testPermissionsWorldAnnotate() throws Exception {
        p.grant(WORLD, ANNOTATE);
        pI.setWorldAnnotate(true);
        verify(p,pI);
        p.revoke(WORLD, ANNOTATE);
        pI.setWorldAnnotate(false);
        verify(p,pI);
    }

    public void testPermissionsWorldWrite() throws Exception {
        p.grant(WORLD, WRITE);
        pI.setWorldWrite(true);
        verify(p,pI);
        p.revoke(WORLD, WRITE);
        pI.setWorldWrite(false);
        verify(p,pI);
    }

    void verify(Permissions p, PermissionsI pI) {
        assertEquals(p.isGranted(USER, READ), pI.isUserRead());
        assertEquals(p.isGranted(USER, ANNOTATE), pI.isUserAnnotate());
        assertEquals(p.isGranted(USER, WRITE), pI.isUserWrite());
        assertEquals(p.isGranted(GROUP, READ), pI.isGroupRead());
        assertEquals(p.isGranted(GROUP, ANNOTATE), pI.isGroupAnnotate());
        assertEquals(p.isGranted(GROUP, WRITE), pI.isGroupWrite());
        assertEquals(p.isGranted(WORLD, READ), pI.isWorldRead());
        assertEquals(p.isGranted(WORLD, ANNOTATE), pI.isWorldAnnotate());
        assertEquals(p.isGranted(WORLD, WRITE), pI.isWorldWrite());
    }

}
