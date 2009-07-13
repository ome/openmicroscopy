function drawImagetest(gateway, pixelsId)
pixels = getPixels(gateway, pixelsId);
channel = 0;
figure(1);
for z=1:pixels.getSizeZ().getValue(),
    for t=1:pixels.getSizeT().getValue(),
        plane = getPlane(gateway, pixelsId, z-1, channel, t-1);
        figure(1);
        imagesc(plane);
        drawnow;
    end
end
