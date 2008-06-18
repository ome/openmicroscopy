function drawImage(pixelsId)
omerojService = createOmeroJService('/Users/donald/OMERO3-TRUNK/dist/etc/ice.config', 'root', 'omero');
pixels = getPixels(omerojService, pixelsId);
channel = 0;
figure(1);
for z=1:pixels.sizeZ.val,
    for t=1:pixels.sizeT.val,
        plane = getPlane(omerojService, pixelsId, z-1, channel, t-1);
        figure(1);
        imagesc(plane);
        drawnow;
    end
end
