package ome.formats.importer;


public interface IObserver
{
    void update(IObservable importLibrary, ImportEvent event);
}
