function projectedImage = PerformProjection(gateway, pixelsId, zSection)

stack = getPlaneStack(gateway, pixelsId, zSection);
projectedImage = ProjectionOnStack(stack,'mean');
