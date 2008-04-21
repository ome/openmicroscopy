function [pixels] = getPixels(gateway, pixelsId)
import blitzgateway.*;
pixels = gateway.getPixels(pixelsId);