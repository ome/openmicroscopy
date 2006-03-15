/*
 * pojos.CategoryData
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
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

package pojos;

// Java imports
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Third-party libraries

// Application-internal dependencies
import ome.model.IObject;
import ome.model.containers.Category;
import ome.model.containers.CategoryGroup;
import ome.model.core.Image;
import ome.util.CBlock;

/**
 * The data that makes up an <i>OME</i> Category along with a back pointer to
 * the Category Group that contains this Category and all the Images that have
 * been classified under this Category. Morover there's a link to the
 * Experimenter that defined this Category &#151; and hence, owns it.
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author <br>
 *         Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:a.falconi@dundee.ac.uk"> a.falconi@dundee.ac.uk</a>
 * @version 2.2 <small> (<b>Internal version:</b> $Revision: $ $Date: $)
 *          </small>
 * @since OME2.2
 */
public class CategoryData extends DataObject
{

    public final static String NAME                 = Category.NAME;

    public final static String DESCRIPTION          = Category.DESCRIPTION;

    public final static String IMAGES               = Category.IMAGELINKS;

    public final static String CATEGORY_GROUP_LINKS = Category.CATEGORYGROUPLINKS;

    /**
     * All the Images classified under this Category. The elements of this set
     * are {@link ImageData} objects. If this Category contains no Images, then
     * this set will be empty &#151; but never <code>null</code>.
     */
    private Set                images;

    /** The Category Group this Category belongs in. */
    private CategoryGroupData  group;

    public CategoryData()
    {
        setDirty( true );
        setValue( new Category() );
    }

    public CategoryData( Category category )
    {
        setValue( category );
    }

    // Immutables
    
    public void setName( String name )
    {
        setDirty(true);
        asCategory().setName(name);
    }

    public String getName()
    {
        return asCategory().getName();
    }

    public void setDescription( String description )
    {
        setDirty(true);
        asCategory().setDescription( description );
    }

    public String getDescription()
    {
        return asCategory().getDescription();
    }

    // Singleton set.
    
    public CategoryGroupData getGroup()
    {
        if ( group == null && asCategory().sizeOfCategoryGroupLinks() >= 0 )
        {
            List list = asCategory().linkedCategoryGroupList();
            if ( list != null )
                if ( list.size() > 0 )
                    group = new CategoryGroupData( (CategoryGroup) list.get(0) );
                else
                    ;// TODO what now?
        }
        return group;
    }

    public void setGroup( CategoryGroupData group )
    {
        if ( group != getGroup() )
        {
            setDirty( true );
            this.group = group;
            asCategory().clearCategoryGroupLinks();
            
            if ( group != null )
                asCategory().linkCategoryGroup( group.asCategoryGroup() );

        }
    }

    
    // Lazy loaded links
    
    public Set getImages()
    {
        if (images == null && asCategory().sizeOfImageLinks() >= 0 )
        {
            images = new HashSet( asCategory().eachLinkedImage( new CBlock()
            {
                public Object call(IObject object)
                {
                    return new ImageData( (Image) object );
                }
            }));
        }
        return images == null ? null : new HashSet( images );
    }

    // Link mutators
    
    public void setImages( Set newValue )
    {
        Set currentValue = getImages(); 
        SetMutator m = new SetMutator( currentValue, newValue );
        
        while ( m.moreDeletions() )
        {
            setDirty( true );
            ImageData imgData = (ImageData) m.nextAddition();
            asCategory().unlinkImage( imgData.asImage() );

            Set categories = imgData.getCategories();
            categories.remove( this );
            imgData.setCategories( categories );
            
        }
        
        while ( m.moreAdditions() )
        {
            setDirty( true );
            ImageData imgData = (ImageData) m.nextAddition();
            asCategory().linkImage( imgData.asImage() );
            
            Set categories = imgData.getCategories();
            categories.add( this );
            imgData.setCategories( categories );
        }

        images = m.result();
    }

}
