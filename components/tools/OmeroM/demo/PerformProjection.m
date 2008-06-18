function PerformProjection(pixelsId, zSection)

omerojService = createOmeroJService('ice.config', 'root', 'omero');
pixels = getPixels(omerojService, pixelsId);
stack = getPlaneStack(omerojService, pixelsId, zSection);
projectedImage = ProjectionOnStack(stack,'mean');
