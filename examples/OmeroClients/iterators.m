import omero.model.*;
image = ImageI();
dataset = DatasetI();
link = dataset.linkImage(image);

it = image.iterateDatasetLinks();
while it.hasNext()
   it.next().getChild().getName() 
end
