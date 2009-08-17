function drawImagetest(gateway, pixelsId)
pixels = gateway.getPixels(pixelsId);
channel = 0;
figure(1);
for z=1:pixels.getSizeZ().getValue(),
    for t=1:pixels.getSizeT().getValue(),
        rawPlane = gateway.getPlane(pixelsId, z-1, channel, t-1);
        figure(1);
        plane2D = omerojava.util.GatewayUtils.getPlane2D(pixels,rawPlane);
        plane = plane2D.getPixelsArrayAsDouble(1);
        imagesc(plane);
        drawnow;
    end
end