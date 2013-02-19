package ome.services.checksum;

public interface ChecksumProviderFactory {

    ChecksumProvider getProvider();

    ChecksumProvider getProvider(ChecksumType checksumType);

}
