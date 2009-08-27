image = omero.model.ImageI();
pixels1 = omero.model.PixelsI();
pixels2 = omero.model.PixelsI();

image.addPixels(pixels1);
image.addPixels(pixels2);
image.getPixels(0)
image.setPrimaryPixels(pixels2);
