/*
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.ldap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import junit.framework.TestCase;
import omero.util.TempFileManager;

import org.apache.commons.io.FileUtils;
import org.apache.directory.server.core.DefaultDirectoryService;
import org.apache.directory.server.core.DirectoryService;
import org.apache.directory.server.core.partition.Partition;
import org.apache.directory.server.core.partition.ldif.LdifPartition;
import org.apache.directory.server.core.schema.SchemaPartition;
import org.apache.directory.shared.ldap.schema.SchemaManager;
import org.apache.directory.shared.ldap.schema.ldif.extractor.SchemaLdifExtractor;
import org.apache.directory.shared.ldap.schema.ldif.extractor.impl.DefaultSchemaLdifExtractor;
import org.apache.directory.shared.ldap.schema.loader.ldif.LdifSchemaLoader;
import org.apache.directory.shared.ldap.schema.manager.impl.DefaultSchemaManager;
import org.apache.directory.shared.ldap.schema.registries.SchemaLoader;
import org.springframework.util.ResourceUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Uses LDIF text files along with property files of good and bad user names to
 * test that the LDAP plugin is properlty functioning.
 */
public class LdapTest extends TestCase {

    File dir;

    DirectoryService service;

    @BeforeClass
    protected void init() throws Exception {
        dir = TempFileManager.create_path("test-", ".ldap", true);
        dir.mkdirs();

        service = new DefaultDirectoryService();
        service.setWorkingDirectory(dir);
        service.getChangeLog().setEnabled(false);

        schema();
        service.startup();
    }

    protected void schema() throws Exception {
        File schemaRepository = new File(dir, "schema");

        SchemaPartition schemaPartition = service.getSchemaService()
                .getSchemaPartition();
        LdifPartition ldifPartition = new LdifPartition();
        ldifPartition.setWorkingDirectory(schemaRepository.getAbsolutePath());

        new DefaultSchemaLdifExtractor(dir).extractOrCopy();

        schemaPartition.setWrappedPartition(ldifPartition);

        SchemaLoader loader = new LdifSchemaLoader(schemaRepository);
        SchemaManager schemaManager = new DefaultSchemaManager(loader);
        service.setSchemaManager(schemaManager);

        schemaManager.loadAllEnabled();
        schemaPartition.setSchemaManager(schemaManager);
    }

    @AfterClass
    protected void stop() throws Exception {
        service.shutdown();
    }

    /**
     * Data provider which returns all "*.ldif" files in the directory
     * containing the class file of this test.
     */
    @DataProvider(name = "ldif_files")
    public Object[][] getLDIFFFiles() throws Exception {
        String name = LdapTest.class.getName();
        name = name.replaceAll("[.]", "//");
        name = "classpath:" + name + ".class";
        File file = ResourceUtils.getFile(name);
        File dir = file.getParentFile();
        Collection<?> coll = FileUtils.listFiles(dir, new String[] { "ldif" },
                false);
        Object[][] files = new Object[coll.size()][];
        int count = 0;
        for (Object object : coll) {
            files[count] = new Object[] { object };
            count++;
        }
        return files;
    }

    /**
     * Runs the LDAP test suite against each of the given *.ldif files, by
     * attempting to login against an embedded ldap store with both the good
     * names and the bad names.
     */
    @Test(dataProvider = "ldif_files")
    public void testLdiffFile(File file) throws Exception {
        addPartition(file);
        assertPasses(file, parse(good(file)));
        assertFails(file, parse(bad(file)));
    }

    private Partition addPartition(File ldifFile) throws Exception {
        Properties p = props(config(ldifFile));
        String partitionId = ldifFile.getName();
        String partitionDn = p.getProperty("omero.ldap.base");

        LdifPartition partition = new LdifPartition();
        partition.setId(partitionId);
        partition.setSuffix(partitionDn);
        partition.setWorkingDirectory(new File(dir, partitionId)
                .getAbsolutePath());
        service.addPartition(partition);
        return partition;
    }

    protected void assertPasses(File file, Map<String, String[]> users) {
        fail();
    }

    protected void assertFails(File file, Map<String, String[]> users) {
        fail();
    }

    @SuppressWarnings("unchecked")
    protected Map<String, String[]> parse(File file) throws Exception {

        Properties properties = props(file);
        Set<Object> names = properties.keySet();

        Map<String, String[]> rv = new HashMap<String, String[]>();
        for (Object key : names) {
            Object value = properties.get(key);
            rv.put(key.toString(), value.toString().split(","));
        }
        return rv;
    }

    private Properties props(File file) throws IOException,
            FileNotFoundException {
        FileInputStream fis = new FileInputStream(file);
        try {
            Properties properties = new Properties();
            properties.load(fis);
            return properties;
        } finally {
            fis.close();
        }
    }

    protected File config(File file) throws Exception {
        String path = stripLdif(file);
        return new File(path + ".config");
    }

    protected File good(File file) throws Exception {
        String path = stripLdif(file);
        return new File(path + ".good");
    }

    protected File bad(File file) throws Exception {
        String path = stripLdif(file);
        return new File(path + ".bad");
    }

    protected String stripLdif(File file) {
        String path = file.getAbsolutePath();
        path = path.substring(0, path.length() - 5);
        return path;
    }

}
