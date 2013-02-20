package ome.util.checksum;

public interface ChecksumProviderFactory {

    ChecksumProvider getProvider();

    ChecksumProvider getProvider(ChecksumType checksumType);

}
