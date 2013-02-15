package ome.services.checksum;

public class ChecksumProviderFactoryImpl implements ChecksumProviderFactory {

    public ChecksumProvider getChecksumProvider(ChecksumType checksumType) {
        // Dumb implementation for now
        // TODO: remove the switch statement
        switch (checksumType) {
            case MD5:
                return new MD5ChecksumProviderImpl();
            case SHA1:
            default:
                return new SHA1ChecksumProviderImpl();
        }
    }

}
