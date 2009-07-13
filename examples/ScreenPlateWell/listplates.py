import omero.*

c = omero.client()
s = c.createSession()
q = s.getQueryService()

QUERY(1) = {'select p from Plate p'};
QUERY(2) = {'left outer join fetch p.wells w'};
QUERY(3) = {'left outer join fetch w.wellSamples '};
QUERY(4) = {'left outer join fetch s.image where p.id = :id'};

filter = omero.sys.Filter()
plates = q.findAll('Plate', filter)

disp('Plate:')
for i=1:length(plates)
    params = omero.sys.ParametersI()
    params.addId(plates(i).getId().getValue())
    plate = q.findByQuery(QUERY, params)
    disp(sprintf('\t%s %s %s', i, plate.getName().getValue()))
    wells = {}

    well_it = plate.copyWells().listIterator()
    while well_it.hasNext()
        well = well_it.next()
        row = well.getRow().getValue()
        col = well.getColumn().getValue()
        if wells.hasKey(row)
            row_list = wells.get(row)
        else
            row_list = java.util.ArrayList()
            wells.put(row, row_list)
        end
        row_list.add(col)
        row_list.sort()
    end

    rows = java.util.ArrayList(wells.keySet())
    rows.sort()

    row_it = rows.listIterator()
    while row_it.hasNext();
        row = row_it.next()
        msg = '\t\t'
        col_list = java.util.ArrayList(wells.get(row))
        col_list.sort()
        col_it = col_list.listIterator()
        while col_it.hasNext()
            col = col_it.next()
            msg = [msg, sprintf('%2sx%2s ', row, col)]
        end
        disp(msg)
    end
end

