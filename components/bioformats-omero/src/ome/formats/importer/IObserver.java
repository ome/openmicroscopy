package ome.formats.importer;


public interface IObserver
{
    void update(IObservable observable, Object message);
}
