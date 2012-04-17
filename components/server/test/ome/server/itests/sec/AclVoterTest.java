/*
 * Copyright (C) 2012 Glencoe Software, Inc. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package ome.server.itests.sec;

import org.testng.annotations.Test;

import ome.conditions.SecurityViolation;
import ome.model.ILink;
import ome.model.IObject;
import ome.model.annotations.CommentAnnotation;
import ome.model.annotations.ImageAnnotationLink;
import ome.model.containers.Dataset;
import ome.model.containers.DatasetImageLink;
import ome.model.core.Image;
import ome.model.core.Pixels;
import ome.model.display.RenderingDef;
import ome.model.internal.Permissions;
import ome.model.meta.ExperimenterGroup;
import ome.security.basic.BasicACLVoter;
import ome.security.basic.OmeroInterceptor;
import ome.server.itests.AbstractManagedContextTest;
import ome.system.EventContext;
import ome.testing.ObjectFactory;

/**
 * Similar to {@link SecurityFilterTest}, but rather than testing READ
 * operations tests WRITE (and ANNOTATE) operations, both in the form of the
 * {@link BasicACLVoter} as well as {@link OmeroInterceptor} which is
 * responsible for evaluating linkages.
 */
@Test(groups = { "ticket:8565", "security" })
public class AclVoterTest extends AbstractManagedContextTest {

    // ~ User (i.e. owner) write operations
    // =========================================================================
    // read-only should prevent annotating
    // read-annotate for user should prevent event the user from modifying data
    // read-write should allow everything.

    void assertUserCan(String perms,
            boolean canRender, boolean canAnnotate,
            boolean canEdit, boolean canUse) {

        // The data has to be created in a group that is at least read-write
        // since otherwise the Pixels object cannot be linked to the Image
        // object.
        Pixels p = pixels("rw----");
        Image i = p.getImage();
        EventContext user = iAdmin.getEventContext();
        ExperimenterGroup group = currentGroup();
        loginRootKeepGroup();
        iAdmin.changePermissions(group, Permissions.parseString(perms));
        login(user);
        user = iAdmin.getEventContext(); // Refresh


        // Render
        RenderingDef rdef = ObjectFactory.createRenderingDef();
        rdef.setPixels(p);
        assertSave(rdef, canRender);

        // Annotate
        assertSave(annotate(i), canAnnotate);

        // use
        assertSave(use(i), canUse);

        // edit
        i = reload(i);
        i.setName(uuid());
        i = assertSave(i, canEdit); // store with new update event.

    }

    @Test
    public void testUser() {
        assertUserCan("r-----", true, false, false, false);
        assertUserCan("ra----", true, true, false, false);
        assertUserCan("rw----", true, true, true, true);
    }

    // ~ Helpers
    // =========================================================================

    Pixels pixels(String perms) {
        loginNewUser(Permissions.parseString(perms));
        Pixels p = ObjectFactory.createPixelGraph(null);
        Image i = iUpdate.saveAndReturnObject(p.getImage());
        return i.getPrimaryPixels();

    }

    Image image(String perms) {
        loginNewUser(Permissions.parseString(perms));
        Image i = new_Image("ticket:8565");
        return iUpdate.saveAndReturnObject(i);
    }

    ILink annotate(Image i) {
        ImageAnnotationLink ial = new ImageAnnotationLink();
        ial.link(i, new CommentAnnotation());
        return ial;
    }

    ILink use(Image i) {
        Dataset d = new Dataset();
        d.setName("tickeT:8565");
        DatasetImageLink dil = new DatasetImageLink();
        dil.link(d, i);
        return dil;
    }

    @SuppressWarnings("unchecked")
    <T extends IObject> T reload(T o) {
        return (T) iQuery.get(o.getClass(), o.getId());
    }

    <T extends IObject> T assertSave(T o, boolean pass) {
        try {
            T rv = iUpdate.saveAndReturnObject(o);
            if (!pass) {
                fail("No secvio thrown! :" + o);
            }
            return rv;
        }
        catch (SecurityViolation sv) {
            if (pass) {
                throw sv;
            }
            return o; // Return original.
        }
    }

}
