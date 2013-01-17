package ome.formats.utests;

import loci.formats.IFormatReader;
import loci.formats.ImageReader;
import loci.formats.FormatTools;

public class TestReader extends ImageReader {

    private String[] domains = new String[] { FormatTools.LM_DOMAIN };

    @Override
    public IFormatReader getReader() {
        return this;
    }

    @Override
    public String[] getDomains() {
        return domains;
    }

    public void setDomains(String[] domains) {
        this.domains = domains;
    }
}
