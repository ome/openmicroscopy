import omero.*;
import omero.rtypes.*;

c = omero.client();
s = c.createSession();
q = s.getQueryService();

GET_IMAGES_WITH_PLATES = 'select i from Image i join i.wellSamples ws join ws.well w join w.plate p'; % Inner joins
GET_PLATE_FROM_IMAGE_ID = 'select p from Plate p join p.wells w join w.wellSamples ws join ws.image i where i.id = :id';

filter = omero.sys.Filter();
filter.limit = rint(100);
filter.offset = rint(0);
params = omero.sys.ParametersI();
params.theFilter = filter;

images = q.findAllByQuery(GET_IMAGES_WITH_PLATES, params);
disp(sprintf('Found %d images', images.size()));

for i=0:images.size()-1

    image = images.get(i);
    params = omero.sys.ParametersI();
    params.addId(image.getId().getValue());
    plate = q.findByQuery(GET_PLATE_FROM_IMAGE_ID, params); % Multiple plates per image will through an exception
    disp(sprintf('Image %d belongs to Plate %d (%s)', image.getId().getValue(), plate.getId().getValue(), char(plate.getName().getValue()) ))

end
