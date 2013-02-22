package ome.util.checksum;

import ome.util.checksum.ChecksumProvider;
import ome.util.checksum.ChecksumProviderFactoryImpl;
import ome.util.checksum.ChecksumType;
import ome.util.checksum.MD5ChecksumProviderImpl;
import ome.util.checksum.SHA1ChecksumProviderImpl;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ChecksumProviderFactoryImplTest {

    private ChecksumProviderFactoryImpl cpf;
    
    @BeforeClass
    protected void setUp() throws Exception {
        this.cpf = new ChecksumProviderFactoryImpl();
    }

    @Test
    public void testGetProvider() {
        ChecksumProvider cp = this.cpf.getProvider();
        Assert.assertEquals(cp.getClass(), SHA1ChecksumProviderImpl.class);
    }

    @Test
    public void testGetProviderWithSHA1ChecksumType() {
        ChecksumProvider cp = this.cpf.getProvider(ChecksumType.SHA1);
        Assert.assertEquals(cp.getClass(), SHA1ChecksumProviderImpl.class);
    }

    @Test
    public void testGetProviderWithMD5ChecksumType() {
        ChecksumProvider cp = this.cpf.getProvider(ChecksumType.MD5);
        Assert.assertEquals(cp.getClass(), MD5ChecksumProviderImpl.class);
    }

}
