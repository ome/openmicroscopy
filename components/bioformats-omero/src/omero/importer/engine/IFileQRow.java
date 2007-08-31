package omero.importer.engine;

import java.io.File;

import ome.model.containers.Dataset;

public interface IFileQRow
{
    public enum Status {ADDED, PENDING, ACTIVE, FAILED, DONE}
    
    File getFilePath();
    String getOmeroName();
    Dataset getDataset();
    Status getStatus();
    Boolean getArchived();
}