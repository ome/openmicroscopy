package omero.importer.engine;


public interface IObserver
{
    void update(IObservable observable, Object message);
}
