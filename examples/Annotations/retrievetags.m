% Simple example of using omero.api.IMetadataPrx
% to retrieve the annotations associated with an
% Image.

[client, sf] = loadOmero;
try

    metadataService = sf.getMetadataService();

    imageIds = java.util.ArrayList();
    imageIds.add(java.lang.Long(1));

    annotationTypes = java.util.ArrayList();
    annotationTypes.add('TagAnnotation');

    % Unused
    annotatorIds = java.util.ArrayList();
    parameters = omero.sys.Parameters();

    idSetMap = metadataService.loadAnnotations('Image', imageIds, annotationTypes, annotatorIds, parameters);
    itr = idSetMap.keySet().iterator();
    while itr.hasNext()
        disp(itr.next()); % Each image id in imageIds
    end

catch ME

    disp(ME);
    client.closeSession();

end
