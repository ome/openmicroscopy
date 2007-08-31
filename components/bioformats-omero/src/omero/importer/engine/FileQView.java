package omero.importer.engine;



public class FileQView implements IObserver
{

    public FileQView(FileQModel fileQModel)
    {
        // TODO Auto-generated constructor stub
        
    }

    public void update(IObservable observable, Object message)
    {
        if(message instanceof String)
        {
            System.out.println(message);
        }
    }

}
