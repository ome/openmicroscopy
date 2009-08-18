function projectedImage = PerformProjection(gateway, pixelsId, channel, timePoint)

stack = getPlaneStack(gateway, pixelsId, channel, timePoint);
projectedImage = ProjectionOnStack(stack,'mean');
