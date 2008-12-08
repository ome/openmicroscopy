package ome.formats.importer;


public interface IObservable
{
    boolean addObserver(IObserver object);
    boolean deleteObserver(IObserver object);
    void notifyObservers(Object message, Object[] args);
}
