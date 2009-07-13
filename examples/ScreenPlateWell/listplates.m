import omero.*

c = omero.client();
s = c.createSession();
q = s.getQueryService();

QUERY = 'select p from Plate p left outer join fetch p.wells w left outer join fetch w.wellSamples s left outer join fetch s.image where p.id = :id';

filter = omero.sys.Filter();
plates = q.findAll('Plate', filter);


disp('Plate:');
for i=0:plates.size()-1
    params = omero.sys.ParametersI();
    params.addId(plates.get(i).getId().getValue());
    plate = q.findByQuery(QUERY, params);
    disp(sprintf('    %d %s %s', i, char(plate.getName().getValue())))
    wells = java.util.HashMap();

    well_it = plate.copyWells().listIterator();
    while well_it.hasNext()
        well = well_it.next();
        row = well.getRow().getValue();
        col = well.getColumn().getValue();
        if wells.containsKey(row)
            row_list = wells.get(row);
        else
            row_list = java.util.ArrayList();
            wells.put(row, row_list);
        end
        row_list.add(col);
        java.util.Collections.sort(row_list);
    end

    rows = java.util.ArrayList(wells.keySet());
    java.util.Collections.sort(rows);

    row_it = rows.listIterator();
    while row_it.hasNext();
        row = row_it.next();
        msg = '        ';
        col_list = java.util.ArrayList(wells.get(row));
        java.util.Collections.sort(col_list);
        col_it = col_list.listIterator();
        while col_it.hasNext()
            col = col_it.next();
            msg = [msg, sprintf('%2dx%2d ', row, col)];
        end
        disp(msg)
    end
end
