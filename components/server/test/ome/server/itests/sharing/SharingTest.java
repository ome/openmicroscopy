/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests.sharing;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ome.api.IShare;
import ome.api.ThumbnailStore;
import ome.conditions.SecurityViolation;
import ome.conditions.ValidationException;
import ome.model.IObject;
import ome.model.annotations.Annotation;
import ome.model.annotations.TextAnnotation;
import ome.model.containers.Dataset;
import ome.model.core.Image;
import ome.model.internal.Permissions;
import ome.model.internal.Permissions.Right;
import ome.model.internal.Permissions.Role;
import ome.model.meta.Experimenter;
import ome.model.meta.Session;
import ome.model.meta.Share;
import ome.model.meta.ShareMember;
import ome.parameters.Filter;
import ome.parameters.Parameters;
import ome.server.itests.AbstractManagedContextTest;
import ome.services.sharing.ShareBean;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * 
 */
@Test(groups = { "sharing" })
public class SharingTest extends AbstractManagedContextTest {

    private static Filter justOne = new Filter().page(0, 1);

    protected IShare share;

    @BeforeMethod
    public void setup() {
        share = factory.getShareService();
        loginRoot();
    }

    @Test
    public void testDescription() {
        share = factory.getShareService();
        long id = share.createShare("before", null, null, null, null, false);
        share.setDescription(id, "after");
    }

    @Test(groups = "ticket:1201")
    public void testActive() {

        loginNewUser();

        share = factory.getShareService();
        long id = share.createShare("before", null, null, null, Arrays
                .asList("guest"), false);
        share.setActive(id, true);
        share.setDescription(id, "desc");

        Share s = (Share) share.getShare(id);
        assertEquals(1, share.getAllGuests(id).size());

        assertEquals(1, share.getOwnShares(true).size());
    }

    @Test(groups = "ticket:1208")
    public void testMembersAreLoaded() {

        Experimenter e1 = loginNewUser();
        Experimenter e2 = loginNewUser();

        share = factory.getShareService();
        long id = share.createShare("before", null, null, Arrays.asList(e1),
                Arrays.asList("guest"), false);

        loginUser(e1.getOmeName());
        Set<Session> sessions = share.getMemberShares(false);
        assertEquals(1, sessions.size());
        assertTrue(sessions.iterator().next().getOwner().isLoaded());
    }

    public void testSetOthers() {

        loginNewUser();

        share = factory.getShareService();
        long id = share.createShare("others", null, null, null, null, false);

        loginNewUser();
        try {
            share.setActive(id, true);
            fail("must throw");
        } catch (ValidationException ve) {
            // good.
        }

    }

    @Test
    public void testExpiration() {

        long time;

        time = ShareBean.expirationAsLong(0, null);
        assertTrue(time > System.currentTimeMillis());

        share = factory.getShareService();
        long id = share.createShare("before", null, null, null, null, false);
        long newExpiration = System.currentTimeMillis() + 100000;
        share.setExpiration(id, new Timestamp(newExpiration));

        Share s = (Share) share.getShare(id);
        assertEquals((newExpiration - s.getStarted().getTime()), s
                .getTimeToLive().longValue());
    }

    @Test
    public void testClose() {
        share = factory.getShareService();
        long id = share.createShare("to close", null, null, null, null, false);
        share.closeShare(id);
    }

    @Test
    public void testComments() {
        share = factory.getShareService();
        long id = share.createShare("disabled", null, null, null, null, false);
        TextAnnotation annotation = share.addComment(id, "hello");

        List<Annotation> annotations = share.getComments(id);
        assertContained(annotation, annotations);

        share.deleteComment(annotation);

        annotations = share.getComments(id);
        assertNotContained(annotation, annotations);

    }

    @Test
    public void testOwnerCommentsBelongToOwner() {
        
        Experimenter owner = loginNewUser();
        
        share = factory.getShareService();
        long id = share.createShare("disabled", null, null,
                null, null, false);
        TextAnnotation annotation = share.addComment(id, "hello");
        assertEquals(owner.getId(), annotation.getDetails().getOwner().getId());
    }
    
    
    @Test
    public void testMemberCommentsBelongToMembers() {
        
        Experimenter member = loginNewUser();
        Experimenter owner = loginNewUser();
        
        share = factory.getShareService();
        long id = share.createShare("disabled", null, null,
                Arrays.asList(member),
                null, false);
        loginUser(member.getOmeName());
        TextAnnotation annotation = share.addComment(id, "hello");
        assertEquals(member.getId(), annotation.getDetails().getOwner().getId());
    }
    
    @Test(groups = "ticket:1227")
    public void testCommentCounts() {

        Experimenter nonMember = loginNewUser();
        Experimenter member = loginNewUser();
        Experimenter owner = loginNewUser();

        share = factory.getShareService();

        long id0 = share.createShare("disabled", null, null, null, null, false);
        // No comments! share.addComment(id1, "hello");

        long id1 = share.createShare("disabled", null, null, null, null, false);
        share.addComment(id1, "hello");

        long id2 = share.createShare("disabled", null, null, null, null, false);
        share.addComment(id2, "hello");

        // Add comment as member
        share.addUser(id0, member);
        share.addUser(id1, member);
        share.addUser(id2, member);
        loginUser(member.getOmeName());
        share.addComment(id2, "hello");

        
        // as root
        loginRoot();
        Map<Long, Long> counts = share.getCommentCount(new HashSet<Long>(Arrays
                .asList(id0, id1, id2)));
        assertEquals(new Long(0), counts.get(id0));
        assertEquals(new Long(1), counts.get(id1));
        assertEquals(new Long(2), counts.get(id2));

        // as owner
        loginUser(owner.getOmeName());
        counts = share.getCommentCount(new HashSet<Long>(Arrays
                .asList(id0, id1, id2)));
        assertEquals(new Long(0), counts.get(id0));
        assertEquals(new Long(1), counts.get(id1));
        assertEquals(new Long(2), counts.get(id2));

        // as member
        loginUser(member.getOmeName());
        counts = share.getCommentCount(new HashSet<Long>(Arrays
                .asList(id0, id1, id2)));
        assertEquals(new Long(0), counts.get(id0));
        assertEquals(new Long(1), counts.get(id1));
        assertEquals(new Long(2), counts.get(id2));
        
        // as non-member
        loginUser(nonMember.getOmeName());
        // before ticket:1227, this method should have thrown
        counts = share.getCommentCount(new HashSet<Long>(Arrays
                .asList(id0, id1, id2)));
        assertEquals(new Long(0), counts.get(id0));
        assertEquals(new Long(0), counts.get(id1));
        assertEquals(new Long(0), counts.get(id2));
    }

    @Test
    public void testPrivateCommentsVisibleForMembers() {

        Experimenter e = loginNewUser();

        share = factory.getShareService();
        long id = share.createShare("with comments", null, null, null, null,
                true);
        TextAnnotation annotation = share.addComment(id, "hello");
        assertFalse(annotation.getDetails().getPermissions().isGranted(
                Role.GROUP, Right.READ));

        List<Annotation> annotations = share.getComments(id);
        assertContained(annotation, annotations);

        // Add a new user
        Experimenter member = loginNewUser();
        loginUser(e.getOmeName());
        share.addUser(id, member);

        // Then as that user try to obtain the comments
        loginUser(member.getOmeName());
        annotations = share.getComments(id);
        assertContained(annotation, annotations);

    }

    @Test
    public void testPrivateCommentsNotVisibleForNonMembers() {

        Experimenter e = loginNewUser();

        share = factory.getShareService();
        long id = share.createShare("with comments", null, null, null, null,
                true);
        TextAnnotation annotation = share.addComment(id, "hello");
        assertFalse(annotation.getDetails().getPermissions().isGranted(
                Role.GROUP, Right.READ));

        List<Annotation> annotations = share.getComments(id);
        assertContained(annotation, annotations);

        // NOT adding a new user
        Experimenter member = loginNewUser();
        // share.addUser(id, member);

        // Then as that user try to obtain the comments
        loginUser(member.getOmeName());
        annotations = share.getComments(id);
        assertNotContained(annotation, annotations);
    }

    @Test
    public void testPrivateCommentsVisibleForAdmin() {

        Experimenter e = loginNewUser();

        share = factory.getShareService();
        long id = share.createShare("with comments", null, null, null, null,
                true);
        TextAnnotation annotation = share.addComment(id, "hello");
        assertFalse(annotation.getDetails().getPermissions().isGranted(
                Role.GROUP, Right.READ));

        List<Annotation> annotations = share.getComments(id);
        assertContained(annotation, annotations);

        // NOT adding root either

        // Then as that root try to obtain the comments
        loginRoot();
        annotations = share.getComments(id);
        assertContained(annotation, annotations);
    }

    @Test
    public void testRetrieval() {
        loginRoot();

        long id = share.createShare("disabled", null, null, null, null, false);

        Set<Session> shares = share.getOwnShares(true);
        assertShareNotReturned(id, shares);

        shares = share.getOwnShares(false);
        assertShareReturned(id, shares);

        share.setActive(id, true);

        shares = share.getOwnShares(true);
        assertShareReturned(id, shares);

        share.activate(id);

        shares = share.getOwnShares(true);
        assertShareReturned(id, shares);

        // Create a new user and then add them to the share (as root)
        Experimenter member = loginNewUser();
        loginRoot();
        share.addUser(id, member);

        // Now as that user let's see how retrieval works
        loginUser(member.getOmeName());

        shares = share.getOwnShares(true);
        assertShareNotReturned(id, shares);

        shares = share.getOwnShares(false);
        assertShareNotReturned(id, shares);

        shares = share.getMemberShares(false);
        assertShareReturned(id, shares);

        shares = share.getMemberShares(true);
        assertShareReturned(id, shares);

        // As root, let's disable the share.
        loginRoot();
        share.setActive(id, false);
        loginUser(member.getOmeName());

        // Now it should not be returned
        shares = share.getMemberShares(true);
        assertShareNotReturned(id, shares);

    }

    @Test
    public void testMembershipFunctions() {

        Experimenter nonMember = loginNewUser();
        Experimenter secondMember = loginNewUser();
        Experimenter firstMember = loginNewUser();
        Experimenter owner = loginNewUser();

        String firstGuest = "example1@example.com";
        String secondGuest = "example2@example.com";

        Dataset d = new Dataset("Dataset for share");
        d.getDetails().setPermissions(Permissions.USER_PRIVATE);
        d = iUpdate.saveAndReturnObject(d);

        long id = share.createShare("description", null, Collections
                .singletonList(d), Arrays.asList(firstMember, secondMember),
                Arrays.asList(firstGuest, secondGuest), true);

        // Members

        assertEquals(1, share.getSharesOwnedBy(owner, true).size());
        assertEquals(0, share.getMemberSharesFor(owner, true).size());
        assertEquals(0, share.getSharesOwnedBy(firstMember, true).size());
        assertEquals(1, share.getMemberSharesFor(firstMember, true).size());
        assertEquals(0, share.getSharesOwnedBy(firstMember, true).size());
        assertEquals(1, share.getMemberSharesFor(firstMember, true).size());
        assertEquals(0, share.getSharesOwnedBy(nonMember, true).size());
        assertEquals(0, share.getMemberSharesFor(nonMember, true).size());
        assertEquals(2, share.getAllMembers(id).size());
        boolean foundFirst = false;
        boolean foundSecond = false;
        for (Experimenter e : share.getAllMembers(id)) {
            if (e.getId().equals(firstMember.getId())) {
                foundFirst = true;
            } else if (e.getId().equals(secondMember.getId())) {
                foundSecond = true;
            }
        }
        assertTrue(foundFirst);
        assertTrue(foundSecond);

        share.removeUser(id, secondMember);

        assertEquals(1, share.getSharesOwnedBy(owner, true).size());
        assertEquals(0, share.getMemberSharesFor(owner, true).size());
        assertEquals(0, share.getSharesOwnedBy(firstMember, true).size());
        assertEquals(1, share.getMemberSharesFor(firstMember, true).size());
        assertEquals(0, share.getSharesOwnedBy(secondMember, true).size());
        assertEquals(0, share.getMemberSharesFor(secondMember, true).size());
        assertEquals(0, share.getSharesOwnedBy(nonMember, true).size());
        assertEquals(0, share.getMemberSharesFor(nonMember, true).size());
        foundFirst = false;
        foundSecond = false;
        for (Experimenter e : share.getAllMembers(id)) {
            if (e.getId().equals(firstMember.getId())) {
                foundFirst = true;
            } else if (e.getId().equals(secondMember.getId())) {
                foundSecond = true;
            }
        }
        assertTrue(foundFirst);
        assertFalse(foundSecond);

        share.addUser(id, secondMember);

        assertEquals(1, share.getSharesOwnedBy(owner, true).size());
        assertEquals(0, share.getMemberSharesFor(owner, true).size());
        assertEquals(0, share.getSharesOwnedBy(firstMember, true).size());
        assertEquals(1, share.getMemberSharesFor(firstMember, true).size());
        assertEquals(0, share.getSharesOwnedBy(secondMember, true).size());
        assertEquals(1, share.getMemberSharesFor(secondMember, true).size());
        assertEquals(0, share.getSharesOwnedBy(nonMember, true).size());
        assertEquals(0, share.getMemberSharesFor(nonMember, true).size());
        foundFirst = false;
        foundSecond = false;
        for (Experimenter e : share.getAllMembers(id)) {
            if (e.getId().equals(firstMember.getId())) {
                foundFirst = true;
            } else if (e.getId().equals(secondMember.getId())) {
                foundSecond = true;
            }
        }
        assertTrue(foundFirst);
        assertTrue(foundSecond);

        // Guests

        assertEquals(2, share.getAllGuests(id).size());
        assertTrue(share.getAllGuests(id).contains(firstGuest));
        assertTrue(share.getAllGuests(id).contains(secondGuest));

        share.removeGuest(id, secondGuest);

        assertEquals(1, share.getAllGuests(id).size());
        assertTrue(share.getAllGuests(id).contains(firstGuest));
        assertFalse(share.getAllGuests(id).contains(secondGuest));

        share.addGuest(id, secondGuest);

        assertEquals(2, share.getAllGuests(id).size());
        assertTrue(share.getAllGuests(id).contains(firstGuest));
        assertTrue(share.getAllGuests(id).contains(secondGuest));

        // All users

        Set<String> names = share.getAllUsers(id);
        assertTrue(names.contains(firstGuest));
        assertTrue(names.contains(secondGuest));
        assertTrue(names.contains(firstMember.getOmeName()));
        assertTrue(names.contains(secondMember.getOmeName()));

        // Counts as different people
        assertEquals(new Long(3), share.getMemberCount(Collections.singleton(id)).get(id));
        loginRoot(); // ticket:1239
        assertEquals(new Long(3), share.getMemberCount(Collections.singleton(id)).get(id));
        loginUser(firstMember.getOmeName());
        assertEquals(new Long(3), share.getMemberCount(Collections.singleton(id)).get(id));
        loginUser(nonMember.getOmeName());
        try {
            share.getMemberCount(Collections.singleton(id)).get(id);
            fail("should throw");
        } catch (ValidationException ve) {
            // ok.
        }
        
    }

    @Test
    public void testElementFunctions() {

        Experimenter owner = loginNewUser();
        Dataset d = new Dataset("elements");
        d = iUpdate.saveAndReturnObject(d);
        long id = share.createShare("another description", null, null, null,
                null, true);

        assertEquals(0, share.getContentSize(id));
        assertEquals(0, share.getContents(id).size());
        assertEquals(0, share.getContentSubList(id, 0, 0).size());
        assertEquals(0, share.getContentMap(id).size());
        share.addObjects(id, d);
        assertEquals(1, share.getContentSize(id));
        assertEquals(1, share.getContents(id).size());
        assertEquals(1, share.getContentSubList(id, 0, 1).size());
        assertEquals(1, share.getContentMap(id).size());

        Dataset d2 = new Dataset("elements transitively");
        Image i2 = new Image("transitive image");
        d2.linkImage(i2);
        d2 = iUpdate.saveAndReturnObject(d2);

        share.addObject(id, d2);
        assertEquals(4, share.getContentSize(id));
    }

    @Test
    public void testOnlyOwnerMembersAndGuestsCanActivateShare() {

        Experimenter nonMember = loginNewUser();
        Experimenter member = loginNewUser();
        Experimenter owner = loginNewUser();

        Dataset d = new Dataset("Dataset for share");
        d.getDetails().setPermissions(Permissions.USER_PRIVATE);
        d = iUpdate.saveAndReturnObject(d);

        long id = share.createShare("description", null, Collections
                .singletonList(d), Collections.singletonList(member), null,
                true);

        loginUser(owner.getOmeName());
        share.activate(id);
        iQuery.get(Dataset.class, d.getId());

        loginUser(member.getOmeName());
        share.activate(id);
        iQuery.get(Dataset.class, d.getId());

        loginUser(nonMember.getOmeName());
        try {
            share.activate(id);
            fail("Should not be allowed");
        } catch (ValidationException e) {
            // ok
        }
        try {
            iQuery.get(Dataset.class, d.getId());
            fail("Should not be allowed");
        } catch (SecurityViolation e) {
            // ok
        }
    }

    @Test
    public void testShareCreationAndViewing() {

        // New user who should be able to see the share.
        Experimenter member = loginNewUser();

        loginRoot();
        Dataset d = new Dataset("Dataset for share");
        d.getDetails().setPermissions(Permissions.USER_PRIVATE);
        d = iUpdate.saveAndReturnObject(d);

        long id = share.createShare("description", null, Collections
                .singletonList(d), Collections.singletonList(member), null,
                true);

        loginUser(member.getOmeName());
        share.activate(id);
        iQuery.get(Dataset.class, d.getId());
    }

    @Test
    public void testUserAddsPrivateImageAndThenShares() {

        Experimenter owner = loginNewUser();
        Image i = new Image("test");
        i.getDetails().setPermissions(Permissions.USER_PRIVATE);
        i = iUpdate.saveAndReturnObject(i);

        long id = share.createShare("desc", null, Collections.singletonList(i),
                null, null, true);

        Experimenter member = loginNewUser();
        loginUser(owner.getOmeName());
        share.addUser(id, member);

        loginUser(member.getOmeName());
        assertEquals(1, share.getContentSize(id));

        try {
            iQuery.find(Image.class, i.getId());
            fail("Should throw");
        } catch (SecurityViolation sv) {
            // good.
        }
        share.activate(id);
        assertNotNull(iQuery.find(Image.class, i.getId()));

        Parameters p = new Parameters();
        p.addIds(Arrays.asList(i.getId()));
        String sql = "select im from Image im where im.id in (:ids) order by im.name";
        List<Image> res = iQuery.findAllByQuery(sql, p);
        assertEquals(1, res.size());
    }

    @Test(groups = "ticket:1197")
    public void testShareMembers() {
        Experimenter member1 = loginNewUser();
        Experimenter member2 = loginNewUser();
        Experimenter owner = loginNewUser();

        long id = share.createShare("desc", null, null, Arrays.asList(member1),
                null, true);

        share.addUser(id, member2);

        loginRoot();

        List<ShareMember> res = iQuery
                .findAllByQuery("select sm from ShareMember sm"
                        + " where sm.parent.id = " + id, null);
        assertEquals(3, res.size()); // includes owner
    }

    @Test
    public void testIsTheUUIDProtected() {
        Experimenter nonowner = loginNewUser();
        Experimenter owner = loginNewUser();

        long id = share.createShare("desc", null, null, null,
                null, true);

        iQuery.get(Share.class, id);
        
        loginUser(nonowner.getOmeName());
        
        iQuery.get(Share.class, id);
    }

    @Test(groups = "ticket:1234")
    public void testThatGetShareReturnsNull() {
        Experimenter nonMember = loginNewUser();
        Experimenter owner = loginNewUser();
        long id = share.createShare("desc",null, null, null, null, true);
        assertNotNull(share.getShare(id));
        assertNull(share.getShare(-1));
        loginUser(nonMember.getOmeName());
        assertNull(share.getShare(id));
        assertNull(share.getShare(-1));
    }
    
    public void testDuplicatesArentAllowedInContent() {
        Experimenter owner = loginNewUser();
        Dataset d = new Dataset("d");
        d = iUpdate.saveAndReturnObject(d);
        long id = share.createShare("desc",null, Arrays.asList(d,d), null, null, true);
        assertEquals(1, share.getContentSize(id));
        share.addObject(id, d);
        assertEquals(1, share.getContentSize(id));
    }
    
    public void testRemoveFunctions() {
        Experimenter owner = loginNewUser();
        Dataset d = new Dataset("d");
        d = iUpdate.saveAndReturnObject(d);
        long id = share.createShare("desc",null, Arrays.asList(d), null, null, true);
        assertEquals(1, share.getContentSize(id));
        share.removeObject(id, d);
        assertEquals(0, share.getContentSize(id));
    }
    
    @Test(groups = "ticket:2249")
    public void testJustAddImages() throws Exception {
        Experimenter e = loginNewUser();
        Image i = makeImage(false);
        long sid = share.createShare("ticket:2249", null, Arrays.asList(i),
                Arrays.asList(e), null, true);
        share.activate(sid);
        ThumbnailStore tb = factory.createThumbnailService();
        tb.getThumbnailSet(64, 64, Collections.singleton(i.getPrimaryPixels()
                .getId()));
    }

    // Assertions
    // =========================================================================

    private void assertShareReturned(long id, Set<Session> shares) {
        boolean found = false;
        for (Session session : shares) {
            found |= session.getId().longValue() == id;
        }
        assertTrue(found);
    }

    private void assertShareNotReturned(long id, Set<Session> shares) {
        for (Session session : shares) {
            assertFalse(id + "==" + session, id == session.getId());
        }
    }

    private <I extends IObject> boolean contains(I obj, List<I> list) {
        boolean found = false;
        for (I test : list) {
            if (test.getId().equals(obj.getId())) {
                found = true;
            }
        }
        return found;
    }

    private <I extends IObject> void assertContained(I obj, List<I> list) {
        boolean found = contains(obj, list);
        assertTrue(found);
    }

    private <I extends IObject> void assertNotContained(I obj, List<I> list) {
        boolean found = contains(obj, list);
        assertFalse(found);
    }

}
