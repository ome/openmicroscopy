import omero.*;
import omero.rtypes.*;

c = omero.client();
s = c.createSession();
q = s.getQueryService();

LOAD_WELLS = ['select w from Well w join fetch w.wellSamples ws ',...
              'join fetch ws.image i join fetch i.pixels p where w.plate.id = :id'];

filter = omero.sys.Filter();
filter.limit = rint(10);
filter.offset = rint(0);


plates = q.findAll('Plate', filter);
if plates.size() == 0
    disp('No plates');
    return;
else
    r = randint(0,plates.size()-1);
    example_plate = plates.get(r);
    disp(sprintf('Loading wells for Plate %d (%s)', example_plate.getId().getValue(), char(example_plate.getName().getValue())))
end

% An example of true paging
filter.limit = rint(12);
params = omero.sys.ParametersI();
params.addId(example_plate.getId().getValue());
params.theFilter = filter;

offset = 0;
while true

    wells = q.findAllByQuery(LOAD_WELLS, params);
    if wells.size() == 0
        return
    else
        offset = offset + wells.size();
        params.theFilter.offset = rint( offset );
    end

    well_it = wells.listIterator();
    while well_it.hasNext()
        well = well_it.next();
        id = well.getId().getValue();
        row = well.getRow().getValue();
        col = well.getColumn().getValue();
        images = java.util.ArrayList();

        planes = 0;
        ws_it = well.copyWellSamples().listIterator();
        while ws_it.hasNext()
            ws = ws_it.next();
            img = ws.getImage();
            pix = img.getPixels(0);
            sizeC = pix.getSizeC().getValue();
            sizeT = pix.getSizeT().getValue();
            sizeZ = pix.getSizeZ().getValue();
            images.add( java.lang.Long.toString(img.getId().getValue()) );
            planes = planes + (sizeC*sizeZ*sizeT);
        end
        images = char(java.util.Arrays.toString(images));
        disp(sprintf('Well %d (%2dx%2d) contains the images: %s with a total of %d planes', id, row, col, images, planes));
    end
end
