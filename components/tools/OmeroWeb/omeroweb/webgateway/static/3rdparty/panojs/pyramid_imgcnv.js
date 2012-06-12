/*******************************************************************************
  ImgcnvPyramid - creates an image URL pyramid based on imgcnv tool
  <http://www.bioimage.ucsb.edu/downloads/BioImage%20Convert>
  
  GSV 3.0 : PanoJS3
  @author Dmitry Fedorov  <fedorov@ece.ucsb.edu>   
  
  Copyright (c) 2010 Dmitry Fedorov, Center for Bio-Image Informatics

*******************************************************************************/

function formatInt(n, pad) {
    var s = n.toString();
    while (s.length<pad)
        s = '0'+s;
    return s;
};   

// -----------------------------------------------------
// ImgcnvLevel
// -----------------------------------------------------

function ImgcnvLevel( width, height, tilesize, level ) {
    this.width = width;
    this.height = height;
    this.xtiles = Math.ceil( width / tilesize );
    this.ytiles = Math.ceil( height / tilesize );
    this.level = level;
}

ImgcnvLevel.prototype.tiles = function() {
    return this.xtiles * this.ytiles;
}

// -----------------------------------------------------
// ImgcnvPyramid
// -----------------------------------------------------

function ImgcnvPyramid( width, height, tilesize ) {
    this.width = width;
    this.height = height;
    this.tilesize = tilesize;
    this._pyramid = Array();
    
    var level_id = 0;
    var level_width = width;    
    var level_height = height;   
    var min_size = (tilesize / 2) + 1;
    while (level_width > min_size || level_height > min_size ) {      
    //while (level_width > tilesize | level_height > tilesize ) {
        var level = new ImgcnvLevel( level_width, level_height, tilesize, level_id );
        this._pyramid.push( level );
        level_width  = Math.floor( level_width / 2 );
        level_height = Math.floor( level_height / 2 );
        level_id++;
    }
    this._pyramid.reverse();
    
    this.length = this._pyramid.length;
    this.levels = this._pyramid.length;
}

ImgcnvPyramid.prototype.getMaxLevel = function() {
    return this.levels - 1;    
}

ImgcnvPyramid.prototype.getLevel = function( level ) {
    if (level<this._pyramid.length)
        return this._pyramid[ level ];    
    else
        return this._pyramid[ this._pyramid.length-1 ];          
}

ImgcnvPyramid.prototype.tiles_upto_level = function( level ) {
    var tiles = 0;
    for (var i = 0; i < level; i++) {
        tiles = tiles + this._pyramid[i].tiles();
    }
    return tiles;
}

ImgcnvPyramid.prototype.tiles = function() {
    return this.tiles_upto_level( this.levels );
}

ImgcnvPyramid.prototype.tile_index = function( level, x_coordinate, y_coordinate ) {
    return x_coordinate + y_coordinate * this._pyramid[ level ].xtiles + this.tiles_upto_level( level );
}

ImgcnvPyramid.prototype.tile_filename = function( level, x_coordinate, y_coordinate ) {
    var l = formatInt( this.getLevel(level).level , 3);
    var x = formatInt(x_coordinate, 3);
    var y = formatInt(y_coordinate, 3);    
    return "" + l + "_" + x + "_" + y + ".jpg";//?"+level;    
}

