package ome.services.checksum;

public interface ChecksumProviderFactory {

    ChecksumProvider getChecksumProvider(ChecksumType checksumType);

}
