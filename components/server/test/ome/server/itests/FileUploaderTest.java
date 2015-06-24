/*
 *   $Id$
 *
 *   Copyright 2007-2014 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests;

import java.io.File;
import java.sql.Timestamp;
import java.util.UUID;

import junit.framework.TestCase;
import ome.conditions.ApiUsageException;
import ome.model.core.OriginalFile;
import ome.model.internal.Permissions;
import ome.model.meta.Experimenter;
import ome.security.SecuritySystem;
import ome.system.OmeroContext;
import ome.system.Principal;
import ome.server.itests.FileUploader;
import ome.tools.spring.ManagedServiceFactory;
import ome.util.checksum.ChecksumProviderFactory;

import org.springframework.util.ResourceUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
@Test(groups = { "integration", "broken" })
public class FileUploaderTest extends TestCase {

    OmeroContext ctx;
    ManagedServiceFactory sf;
    SecuritySystem sec;
    FileUploader f;
    OriginalFile of;

    @BeforeClass
    public void setup() {
        ctx = OmeroContext.getManagedServerContext();
        sf = new ManagedServiceFactory();
        sec = (SecuritySystem) ctx.getBean("securitySystem");
        {
            sf.setApplicationContext(ctx);
            sec.login(new ome.system.Principal("root", "user", "Test"));
        }

    }

    @Test
    public void testUploadingFromClasspath() throws Exception {
        File file = ResourceUtils.getFile("classpath:omero.properties");
        f = new FileUploader(sf, file);
        f.run();
    }

    @Test
    public void testRandomFormatWillBeCreated() throws Exception {
        f = new FileUploader(sf, "test-string", "test-name", "test-path");
        f.setMimetype("random");
        f.run();
    }

    @Test(expectedExceptions = ApiUsageException.class)
    public void testAfterUploadNoMutators() throws Exception {
        f = new FileUploader(sf, "test-string", "test-name", "test-path");
        f.setMimetype("random");
        f.run();
        f.setName("boom");
    }

    @Test(expectedExceptions = ApiUsageException.class)
    public void testNoSettingOwnerAsNonRoot() throws Exception {
        Experimenter e = new Experimenter();
        String uuid = UUID.randomUUID().toString();
        e.setOmeName(uuid);
        e.setFirstName("FileUploaderTest");
        e.setLastName(uuid);
        e.setLdap(false);
        sf.getAdminService().createUser(e, "default");

        sec.login(new Principal(uuid, "user", "Test"));
        f = new FileUploader(sf, "test-string", "test-name", "test-path");
        f.setOwner("root");
    }

    @Test
    public void testCanAccessViaId() throws Exception {
        f = new FileUploader(sf, "test-string", "test-name", "test-path");
        f.setMimetype("random");
        f.run();
        Long id = f.getId();
        assertTrue(id != null);
        of = sf.getQueryService().get(OriginalFile.class, id);
    }

    @Test
    public void testCheckProperties() throws Exception {
        f = new FileUploader(sf, "test-string", "test-name", "test-path");
        f.setMimetype("random");
        f.setName("boo");
        f.setPath("/dev/hi");
        f.setCtime(new Timestamp(System.currentTimeMillis()));
        f.setPerms(Permissions.GROUP_IMMUTABLE);
        f.run();
        Long id = f.getId();
        assertTrue(id != null);
        of = sf.getQueryService().get(OriginalFile.class, id);
        assertTrue(of.getCtime() != null);
        assertEquals("boo", of.getName());
        assertEquals("/dev/hi", of.getPath());
        Permissions p = of.getDetails().getPermissions();
        assertTrue(p + ":" + Permissions.GROUP_IMMUTABLE, p
                .sameRights(Permissions.GROUP_IMMUTABLE));
    }

    @Test
    public void testDefaultFormatIsTextPlain() throws Exception {
        f = new FileUploader(sf, new File("/dev/null"));
        f.init();
        assertTrue(f.getMimetype().equals("text/plain"));
    }

    @Test(groups = "broken")
    public void testFormatShouldBeDeterminedByFileEnding() throws Exception {
        fail("NYI");
    }

}
