package ome.formats.importer;


public interface IObserver
{
    void update(IObservable importLibrary, Object message, Object[] args);
}
