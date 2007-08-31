/*
 * omero.importer.engine.FileQModel
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2007 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
*/

package omero.importer.engine;

import java.util.ArrayList;

import omero.importer.gui.FileQRow_JTable;

public class FileQModel 
    implements IObservable
{
    private static final long serialVersionUID = -6300693803659305281L;
    
    ArrayList<IObserver> observers = new ArrayList<IObserver>();
    ArrayList<IFileQRow>  fileQArrayList = null;
    
    
    @SuppressWarnings("unchecked")
    public FileQModel(ArrayList fileQ)
    {
        this.fileQArrayList = fileQ;
    }

    public void addRow(FileQRow_JTable row)
    {
        fileQArrayList.add(row);
        notifyObservers("Row added, table size: " + getSize());
    }

    public void deleteRow(int index)
    {
        fileQArrayList.remove(index);
        notifyObservers("Row deleted, table size: " + getSize());
    }

    public IFileQRow getRow(int index)
    {
        IFileQRow fileQRow = (IFileQRow) fileQArrayList.get(index);
        notifyObservers("Row '" + fileQRow.getOmeroName() + "' retrieved.");
        return fileQRow;
    }

    public Integer getSize()
    {
        return fileQArrayList.size();
    }

    // Observable methods
    
    public boolean addObserver(IObserver object)
    {
        return observers.add(object);
    }
    
    public boolean deleteObserver(IObserver object)
    {
        return observers.remove(object);
        
    }

    public void notifyObservers(Object message)
    {
        for (IObserver observer:observers)
        {
            observer.update(this, message);
        }
    }
}
