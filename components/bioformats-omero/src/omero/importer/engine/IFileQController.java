package omero.importer.engine;

import omero.importer.gui.FileQRow_JTable;

public interface IFileQController
{
    void        addRow(FileQRow_JTable row);
    void        deleteRow(int index);
    IFileQRow    getRow(int index);
    Integer     getSize();
}