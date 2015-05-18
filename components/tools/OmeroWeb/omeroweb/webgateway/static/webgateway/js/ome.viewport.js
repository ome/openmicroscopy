/**
 * weblitz-thumbslider - Weblitz image viewport
 *
 * Depends on jquery, jquery-plugin-viewportImage, gs_utils, gs_slider
 * Uses weblitz.css
 *
 * Copyright (c) 2007-2014 Glencoe Software, Inc. All rights reserved.
 *
 * This software is distributed under the terms described by the LICENCE file
 * you can find at the root of the distribution bundle, which states you are
 * free to use it only for non commercial purposes.
 * If the file is missing please request a copy by contacting
 * jason@glencoesoftware.com.
 *
 * Author: Carlos Neves <carlos(at)glencoesoftware.com>
 */
 
 /*jshint bitwise:false */
 /*global parseQuery:true toRGB:true */

/* Public constructors */

jQuery.fn.WeblitzViewport = function (server, options) {
  return this.each
  (
   function () {
     jQuery._WeblitzViewport (this, server, options);
   });
};

jQuery.WeblitzViewport = function (elm, server, options) {
  var container = jQuery(elm).get(0);
  var rv = container.WeblitzViewport || (container.WeblitzViewport = new jQuery._WeblitzViewport(container, server, options));
  return rv;
};

/* Helper objects */

var Metadata = function () {
  this._loaded = false;
  this.current = {};
  this.rdefs = {};
  this._load = function (data) {
    var i,j;
    var cached = {};
    if (!this._loaded) {
      this._loaded = true;
      cached.rdefs = this.rdefs;
    }
    for (i in data) {
      this[i] = data[i];
    }
    for (i in cached) {
      for (j in cached[i]) {
        this[i][j] = cached[i][j];
      }
    }
    this.defaultZ = this.rdefs.defaultZ;
    this.current.z = this.rdefs.defaultZ;
    this.defaultT = this.rdefs.defaultT;
    this.current.t = this.rdefs.defaultT;
      if (this.rdefs.invertAxis) {
        var t = this.size.t;
        this.size.t = this.size.z;
        his.size.z = t;
      }
    this.current.zoom = 100;
  };
  
  this.hasSameSettings = function (other) {
    if (this.rdefs.model === other.rdefs.model) {
      for (var i in this.channels) {
        if (this.channels[i].active != other.channels[i].active ||
            OME.rgbToHex(this.channels[i].color) != OME.rgbToHex(other.channels[i].color) ||
            this.channels[i].emissionWave != other.channels[i].emissionWave ||
            this.channels[i].metalabel != other.channels[i].metalabel ||
            this.channels[i].window.end != other.channels[i].window.end ||
            this.channels[i].window.min != other.channels[i].window.min ||
            this.channels[i].window.max != other.channels[i].window.max ||
            this.channels[i].window.start != other.channels[i].window.start) {
          return false;
        }
      }
      return true;
    }
    return false;
  };
  return true;
};

/* (make believe) Private constructor */

/**
 * _WeblitzViewport class created in the jQuery namespace holds all logic for interfacing with the weblitz
 * viewport and ajax server.
 *
 * Own Events:
 *  - imageLoad: initial load of image data, before the actual image object is fetched (once per image id).
 *  - imageChange: image object loaded a specific image view.
 *  - channelChange:
 *  - modelChange:
 * Events Proxied from jquery-plugin-viewportImage:
 *
 *
 * @constructor
 */

String.prototype.endsWith = function(pattern) {
 var d = this.length - pattern.length;
 return d >= 0 && this.lastIndexOf(pattern) === d;
};

jQuery._WeblitzViewport = function (container, server, options) {
  if (server.endsWith("/")) {
    server = server.substring(0, server.length - 1);
  }
  this.viewport_server = server;
  this.self = jQuery(container);
  this.origHTML = this.self.html();
  this.self.html("");
  var _this = this;
  var thisid = this.self.attr('id');
  this.loadedImg = new Metadata();
  this.loadedImg_def = new Metadata();
  var topid = thisid + '-top';
  var zsliderid = thisid + '-zsl';
  var viewportid = thisid + '-vp';
  var viewportimgid = thisid + '-img';
  var viewportmsgid = thisid + '-msg';
  var bottomid = thisid + '-bot';
  var tsliderid = thisid + '-tsl';
  var ajaxTimeout;
  var tileHref;
  this.self.append('<div id="'+topid+'" id="'+topid+'" class="weblitz-viewport-top">');
  this.top = jQuery('#'+topid);
  this.top.append('<div id="'+zsliderid+'">');
  this.zslider = jQuery('#'+zsliderid);
  this.top.append('<div id="'+viewportid+'" class="weblitz-viewport-vp">');
  this.viewport = jQuery('#'+viewportid);
  this.viewport.append('<img id="'+viewportimgid+'">');
  this.viewportimg = jQuery('#'+viewportimgid);
  this.viewport.append('<div id="'+viewportmsgid+'" class="weblitz-viewport-msg"></div>');
  this.viewportmsg = jQuery('#'+viewportmsgid);
  this.self.append('<div id="'+bottomid+'" class="weblitz-viewport-bot">');
  this.bottom = jQuery('#'+bottomid);
  this.bottom.append('<div id="'+tsliderid+'">');
  this.tslider = jQuery('#'+tsliderid);

  var done_reload = function () {
    return _this.viewportmsg.is(':hidden');
  };

  this.viewportimg.viewportImage(options);
  this.viewportimg.bind('zoom', function (e,z) { _this.loadedImg.current.zoom = z; });
  this.zslider.gs_slider({ orientation: 'v', min:0, max:0, tooltip_prefix: 'Z=', repeatCallback: done_reload });
  this.tslider.gs_slider({ tooltip_prefix: 'T=', min:0, max:0, repeatCallback: done_reload });
  this.viewportimg.css('overflow', 'hidden');
  this.zslider.bind('change', function (e,pos) {
      if (_this.loadedImg.rdefs.invertAxis) {
	_this.loadedImg.current.t = pos-1;
      } else {
	_this.loadedImg.current.z = pos-1;
      }
        _load();
     });
  this.tslider.bind('change', function (e,pos) {
      if (_this.loadedImg.rdefs.invertAxis) {
	_this.loadedImg.current.z = pos-1;
      } else {
	_this.loadedImg.current.t = pos-1;
      }
        _load();
     });
     
  
  // Sets the Z position (1-based index) of the image viewer by delegating to the slider.
  // Setting the slider should also result in the image plane changing.
  this.setZPos = function(pos) {
      if (this.getZPos() != pos) {  // don't reload etc if we don't have to
          if (_this.loadedImg.rdefs.invertAxis) {
              this.tslider.get(0).setSliderPos(pos);
          } else {
              this.zslider.get(0).setSliderPos(pos);
          }
      }
  };
  
  this.setTPos = function(pos) {
      if (this.getTPos() != pos) {
          if (_this.loadedImg.rdefs.invertAxis) {
              this.zslider.get(0).setSliderPos(pos);
          } else {
              this.tslider.get(0).setSliderPos(pos);
          }
      }
  };

  var after_img_load_cb = function (callback) {
    hideLoading();
    _this.viewportimg.show();
    
    _this.zslider.get(0).pos = -1;
    if (_this.loadedImg.rdefs.projection.toLowerCase().substring(3,0) == 'int') {
	_this.zslider.get(0).setSliderRange(1, 1, _this.getPos().z+1, false);
    } else {
	_this.zslider.get(0).setSliderRange(1, _this.getSizes().z, _this.getPos().z+1, false);
    }
      _this.tslider.get(0).setSliderRange(1, _this.getSizes().t, _this.getPos().t+1, false);
    if (callback) {
      callback();
    }
    if (_this.hasLinePlot()) {
      _this.viewportimg.one('zoom', function () {_this.refreshPlot();});
    }
  };
  
  var getSizeDict = function () {
    var size;
    if (_this.loadedImg.rdefs.projection.toLowerCase() == 'split') {
      if (_this.isGreyModel()) {
        size =  _this.loadedImg.split_channel.g;
      } else {
        size =  _this.loadedImg.split_channel.c;
      }
    } else {
      size =  _this.loadedImg.size;
    }
    return size;
  };

  /**
   * Initializes the data structures with the supplied data values.
   * Gets called as callback from the json AJAX fetching of image metadata.
   *
   * @param {Dict} data The image data from the server as a dictionary with the following keys:
   *                     {id, width, height, z_count, t_count, c_count,
   *                      rdefs:{model,},
   *                      channels:[{emissionWave,color,active},]}
   *  If a 'ConcurrencyException' is thrown, this key will be in the returned data
   */
  var _reset = function (data, textStatus) {
    hideLoading();
    clearTimeout(ajaxTimeout);
    if(!data) {
        loadError("No data received from the server.");
        return;
    }
    // If 'ConcurrencyException' we can't do anything else but notify user
    if (data["ConcurrencyException"] !== undefined) {
        /* //backOff is hardcoded on the server
        //there is no point to display it right now
        
        backOff = data["ConcurrencyException"]['backOff'];
        if (backOff != undefined || backOff != null){
            seconds = backOff/1000;
            hrs = parseInt(seconds / 3600, 10);
            mins = parseInt((seconds % 3600)/60, 10);
            secs = parseInt(seconds % (3600 * 60), 10);
            if (hrs > 0) {
                label = hrs + " hours " + mins + " minutes";
            } else if (mins > 0) {
                label = mins + " minutes";
            } else {
                label = secs + " seconds";
            }
        } */
        loadError('A "Pyramid" of zoom levels is currently being calculated for this image. Please try viewing again later.');
        return;
    } else if (data["Exception"] !== undefined) {
        loadError(data["Exception"]);
        return;
    }
    _this.loadedImg._load(data);
    _this.loadedImg_def = jQuery.extend(true, {}, _this.loadedImg);
    if (_this.loadedImg.current.query) {
      _this.setQuery(_this.loadedImg.current.query);
    }
    // refresh allow_resize = true, seems to *prevent* resize (good) but don't fully understand
    _this.refresh(true);
    
    // Here we set up PanoJs Big image viewer...
    if (_this.loadedImg.tiles) {
        // This is called for every tile, each time they move
        var hrefProvider = function() {
          if (typeof tileHref == "undefined") {
            tileHref = server + '/render_image_region/' + _this.getRelUrl();
          }
          return tileHref;
        };
        // temporary solution for sharing. ShareId must me passed in a different way.
        thref = server + '/render_birds_eye_view/' + _this.loadedImg.id + "/";
        // if the url query had x and y, pass these to setUpTiles() so we can recenter
        var cx = _this.loadedImg.current.query.x,
          cy = _this.loadedImg.current.query.y,
          img_w = _this.loadedImg.size.width,
          img_h = _this.loadedImg.size.height,
          tile_w = _this.loadedImg.tile_size.width,
          tile_h = _this.loadedImg.tile_size.height,
          init_zoom = _this.loadedImg.init_zoom,
          zoom_levels = _this.loadedImg.levels,
          zoomLevelScaling = _this.loadedImg.zoomLevelScaling;  // may be 'undefined'
          nominalMagnification = _this.loadedImg.nominalMagnification;  // may be 'undefined'
          // If zm set in query, see if this is a supported zoom level
          if (typeof _this.loadedImg.query_zoom != "undefined") {
            var query_zm = _this.loadedImg.query_zoom / 100;
            for (var zm=0; zm<zoom_levels; zm++) {
              if (zoomLevelScaling[zm] == query_zm) {
                init_zoom = (zoom_levels-1) - zm;
                break;
              }
            }
          }
          // If init_zoom not defined, Zoom out until we fit in the viewport (window)
          if (typeof init_zoom === "undefined") {
            init_zoom = zoom_levels-1;   // fully zoomed in
            while (init_zoom > 0) {
              var omero_zm_index = (zoom_levels-1)-init_zoom,   // convert PanoJs to OMERO
                scale = zoomLevelScaling[omero_zm_index],
                scaled_w = img_w * scale,
                scaled_h = img_h * scale;
              if (scaled_w < (window.innerWidth-200) || scaled_h < (window.innerHeight-50)) {
                break;
              }
              init_zoom--;
            }
          }
        _this.viewportimg.get(0).setUpTiles(img_w, img_h, tile_w, tile_h, init_zoom, zoom_levels, hrefProvider, thref, cx, cy, zoomLevelScaling, nominalMagnification);
    }
    
    _load(function () {
      //_this.refresh();
      if (!_this.loadedImg.current.query.zm && !_this.loadedImg.tiles) {
        var size = getSizeDict();
        _this.viewportimg.get(0).setZoomToFit(false, size.width, size.height);
      }
      if (_this.loadedImg.current.query.lp) {
        _this.refreshPlot();
      }
      _this.self.trigger('imageLoad', [_this]);
    });

    channels_undo_stack = [];
    channels_undo_stack_ptr = -1;
    channels_bookmark = null;
    _this.save_channels();
  };

  /**
   * Request a particular view of the current image from the server.
   * The definition of the view will be whatever is set in loadedImg at this point.
   *
   */
  var _load = function (callback) {
    if (_this.loadedImg._loaded) {
      var href, thref;
      if (_this.loadedImg.rdefs.projection.toLowerCase() != 'split') {
        href = server + '/render_image/' + _this.getRelUrl();
      } else {
        href = server + '/render_split_channel/' + _this.getRelUrl();
      }
      
      var rcb = function () {
        after_img_load_cb(callback);
        _this.viewportimg.unbind('load', rcb);
        _this.self.trigger('imageChange', [_this]);
      };
      
      if (_this.loadedImg.tiles) {
          // clear the cached tiles href
          tileHref = undefined;
          showLoading();
          rcb();
          _this.viewportimg.get(0).refreshTiles();
      } else {
    if (href != _this.viewportimg.attr('src')) {
          showLoading();
    }
          _this.viewportimg.load(rcb);
          _this.viewportimg.attr('src', href);
      }
    }
  };

  var loadError = function (msg) {
    if (_this.origHTML) {
      _this.self.replaceWith(_this.origHTML);
    }
    hideLoading();
    if (msg!=='null') {
      showLoading(msg, 5);
    } else {
      showLoading('Error loading image!', 5);
    }
  };

  /**
   * @param {Integer} iid The image id on the database.
   * @param {Integer} dsid The Dataset id this image belongs to, optional.
   */
  this.load = function(iid, dsid, query) {
    showLoading();
    linePlot = null;
    _this.refreshPlot();
    _this.loadedImg.current.datasetId = dsid;
    _this.loadedImg.current.query = parseQuery(query);
    //viewportimg.hide();
    ajaxTimeout = setTimeout(loadError, 10000);
    jQuery.getJSON(server+'/imgData/'+iid+'/?callback=?', _reset);
  };

  var loadingQ = 0;
  var showLoading = function (msg, time) {
    if (_this.viewportmsg.is(':hidden')) {
      loadingQ = 0;
    }
    if (msg === undefined) {
      msg = 'Loading...';
    }
    _this.viewportmsg.html(msg);
    loadingQ++;
    _this.viewportmsg.show();
    if (time) {
      setTimeout(function() { hideLoading(); }, time*1000);
    }
  };

  var hideLoading = function () {
    if (loadingQ > 0) {
      loadingQ--;
    }
    if (loadingQ < 1) {
      _this.viewportmsg.hide();
    }
  };

  /* Line Plot related funcs */

  var LinePlot = function (pos, direction) {
    this.position = parseInt(pos, 10);
    this._isHorizontal = direction.substring(0,1).toLowerCase() == 'h';
    this.isHorizontal = function () {
      return this._isHorizontal;
    };
    this.isVertical = function () {
      return !this._isHorizontal;
    };
    this.getUrl = function () {
      var append = this.position;//+'/'+parseInt(Math.max(1,100/_this.getZoom()*1.0), 10);
      return server+'/render_'+(this.isHorizontal()?'row':'col')+'_plot/' + _this.getRelUrl(append);
    };
  };

  var linePlot = null;

  this.hasLinePlot = function () {
    return _this.loadedImg.rdefs.projection.toLowerCase().substring(0,6) == 'normal' && _this.viewportimg.get(0).overlayVisible();
  };

  this.getLinePlot = function () {
    return linePlot;
  };

  this.prepareLinePlot = function (axis) {
    if (!linePlot || (axis == 'v' != linePlot.isVertical())) {
      linePlot = new LinePlot(0,axis,100, server, _this.getRelUrl);
    }
  };

  var pickPosHandler = function (e) {
    if (_this.getLinePlot()) {
      var pos;
      var targetpos = _this.viewportimg.offset();
      if (_this.getLinePlot().isVertical()) {
        pos = _this.loadedImg.size.width * (e.pageX - targetpos.left) / _this.viewportimg.width();
      } else {
        pos = _this.loadedImg.size.height * (e.pageY - targetpos.top) / _this.viewportimg.height();
      }
      _this.self.trigger('linePlotPos', [Math.round(pos)]);
    }
    //_this.viewportimg.removeClass('pick-pos');
  };

  this.startPickPos = function () {
    this.viewportimg.bind('click', pickPosHandler);
    this.viewportimg.parent().addClass('pick-pos');
  };

  this.stopPickPos = function () {
    this.viewportimg.unbind('click', pickPosHandler);
    this.viewportimg.parent().removeClass('pick-pos');
  };

  this.loadPlot = function(pos) {
    linePlot.position = parseInt(pos, 10);
    this.refreshPlot();
  };

  /**
   * Loads a line plot for row {{ y }} as an image overlay.
   */
  this.loadRowPlot = function(y) {
    this.prepareLinePlot('h');
    linePlot.position = parseInt(y, 10);
    this.refreshPlot();
  };

  /**
   * Hide whatever overlay is showing.
   */
  this.hidePlot = function () {
    _this.viewportimg.get(0).hideOverlay();
  };

  /**
   * Request a refresh of the current plot.
   */
  this.refreshPlot = function (cb) {
    if (linePlot && linePlot.position <= (linePlot.isHorizontal() ? this.loadedImg.size.height : this.loadedImg.size.width)) {
      var _cb = function () {
        if (cb) {cb();}
        hideLoading();
      };
      var _error_cb = function () { hideLoading(); showLoading('Error loading line plot!', 5); };
      showLoading('Loading line plot...');
      this.viewportimg.get(0).showOverlay(linePlot.getUrl(), _cb, _error_cb);
    } else {
      this.hidePlot();
    }
    this.self.trigger('linePlotChange', [!!linePlot]);
  };

  var remember_allow_resize;
  /**
   * Recalculate GUI, useful in response to interface changes, like window resize.
   */
  this.refresh = function (allow_resize) {
    _this.viewportimg.get(0).refresh();
    var sli = jQuery('.slider-line', _this.tslider);
    var btn = jQuery('.slider-btn-up', _this.tslider);
    
    if (!(allow_resize || remember_allow_resize)) {
      var a1 = _this.self.height();
      var a2 = _this.self.width();
      var b1 = _this.top.height();
      var b2 = _this.viewport.width();
      if (a1 <= b1) {
        _this.top.css('height', _this.top.height()-_this.bottom.height());
      }
      if (a2 <= b2) {
        _this.viewport.css('width', _this.viewport.width() - _this.viewport.position().left - 3);
      }
    } else {
      remember_allow_resize = true;
    }
    _this.tslider.css('width', _this.viewport.width());
    sli.css('width', _this.tslider.width() - (parseInt(sli.css('left'), 10) *2) - 2);
    sli = jQuery('.slider-line', _this.zslider);
    btn = jQuery('.slider-btn-up', _this.zslider);
    sli.css('height', _this.top.height() - (parseInt(sli.css('top'), 10) *2) - 2);
    _this.viewport.css('height', _this.top.height() -3);
    _this.viewportimg.get(0).refresh();
  };

  /********************************************/
  /* Attribute getters/setters/wrappers below */

  this.getAuthor = function () {
    return _this.loadedImg.meta.author;
  };

  this.getChannels = function () {
    return _this.loadedImg.channels;
  };

  this.toggleChannel = function (idx) {
    this.setChannelActive(idx, !_this.loadedImg.channels[idx].active);
  };

  this.getCCount = function () {
    return _this.loadedImg.size.c;
  };

  this.channelChange = function () {
    for (var i = 0; i < _this.loadedImg.channels.length; i++) {
      _this.self.trigger('channelChange', [_this, i, _this.loadedImg.channels[i]]);
    }
  };

  this.setChannelActive = function (idx, act, noreload) {
    // GreyModel only allows a single active channel, if not 'split' view
    if (this.isGreyModel() && this.getProjection() != 'split') {
      /* Only allow activation of channels, and disable all other */
      if (act) {
	for (var i = 0; i < _this.loadedImg.channels.length; i++) {
          act = i == idx;
          if (act != _this.loadedImg.channels[i].active) {
            _this.loadedImg.channels[i].active = act;
            _this.self.trigger('channelChange', [_this, i, _this.loadedImg.channels[i]]);
          }
	}
        if (!noreload) {
          _load();
        }
      }
    } else {
      if (_this.loadedImg.channels[idx].active != act) {
        _this.loadedImg.channels[idx].active = act;
        _this.self.trigger('channelChange', [_this, idx, _this.loadedImg.channels[idx]]);
        if (!noreload) {
          _load();
        }
      }
    }
  };

  this.setChannelColor = function (idx, color, noreload) {
    _this.loadedImg.channels[idx].color = color;
    _this.self.trigger('channelChange', [_this, idx, _this.loadedImg.channels[idx]]);
    if (!noreload) {
      _load();
    }
  };

  this.setChannelLabel = function (idx, label, noreload) {
    _this.loadedImg.channels[idx].metalabel = label;
    _this.self.trigger('channelChange', [_this, idx, _this.loadedImg.channels[idx]]);
    if (!noreload) {
      _load();
    }
  };

  this.setChannelWindow = function (idx, start, end, noreload) {
    var channel = _this.loadedImg.channels[idx];
    if (parseInt(start, 10) > parseInt(end, 10)) {
      var t = start;
      start = end;
      end = t;
    }
    if (start < _this.loadedImg.pixel_range[0]) {
      start = _this.loadedImg.pixel_range[0];
    }
//    if (channel.window.min <= start) {
      channel.window.start = start;
//    }
    if (end > _this.loadedImg.pixel_range[1]) {
      end = _this.loadedImg.pixel_range[1];
    }
//    if (channel.window.max >= end) {
      channel.window.end = end;
//    }
    _this.self.trigger('channelChange', [_this, idx, _this.loadedImg.channels[idx]]);
    if (!noreload) {
      _load();
    }
  };

  this.setChannelMinMax = function () {
    var channels = _this.loadedImg.channels;
    for (var i=0; i < channels.length; i++) {
      this.setChannelWindow(i, channels[i].window.min, channels[i].window.max, true);
    }
    _load();
  };

  this.setChannelFullRange = function () {
    var channels = _this.loadedImg.channels,
      pixel_range = _this.loadedImg.pixel_range;
    for (var i=0; i < channels.length; i++) {
      this.setChannelWindow(i, pixel_range[0], pixel_range[1], true);
    }
    _load();
  };

  this.getMetadata = function () {
    return _this.loadedImg.meta;
  };

  this.getProjection = function () {
    return _this.loadedImg.rdefs.projection.toLowerCase();
  };

  this.setProjection = function (p, noreload) {
    p = p.toLowerCase();
    if (_this.loadedImg.rdefs.projection.toLowerCase() != p) {
      var was_split = _this.loadedImg.rdefs.projection.toLowerCase() == 'split',
          doReset = was_split ||  p == 'split';
      if (p.substring(3,0) == 'int' && _this.loadedImg.rdefs.invertAxis) {
        /* No intensity projections when axis are inverted */
        p = _this.loadedImg.rdefs.projection;
      } else {
        _this.loadedImg.rdefs.projection = p;
      }
      _this.self.trigger('projectionChange', [_this]);

      // if switching from 'split', and we're greyscale, need to check we only have 1 channel on
      if (was_split && this.isGreyModel()) {
        var found = false;
        for (var idx = 0; idx < _this.loadedImg.channels.length; idx++) {
          if (_this.loadedImg.channels[idx].active) {
            this.setChannelActive(idx, true, true);
            found = true;
            break;
          }
        }
        if (!found) {
          this.setChannelActive(0, true, true);
        }
      }

      if (!noreload) {
        _load(function () {
            if (doReset) {
              var size = getSizeDict();
              _this.viewportimg.get(0).setZoomToFit(true, size.width, size.height);
            }
          });
      }
    }
  };

  this.setInvertedAxis = function (p, noreload) {
    p = p=='1';
    if (_this.loadedImg.rdefs.invertAxis ^ p) {
      _this.loadedImg.rdefs.invertAxis = p;
      _this.self.trigger('invertAxis', [_this]);
	_this.loadedImg.current.query.ia = p ? "1":"0";
      if (!noreload) {
        _load(function () {
          });
      }
    }
  };

  this.setModel = function (m, noreload) {
    /* We only ever look at the first letter */
    m = m.toLowerCase().substring(0,1);
    if (_this.loadedImg.rdefs.model.toLowerCase().substring(0,1) != m) {
      _this.loadedImg.rdefs.model = m;
      //var lmc = _this.loadedImg.current.lastModelChannels;
      _this.loadedImg.current.lastModelChannels = [];
      for (var i in _this.loadedImg.channels) {
        _this.loadedImg.current.lastModelChannels.push(_this.loadedImg.channels[i].active);
      }
      ///* This is the last model state retrieval logic */
      //if (lmc) {
      //  for (i in lmc) {
      //    this.setChannelActive(i, lmc[i]);
      //  }
      //} else if (this.isGreyModel()) {
      //  this.setChannelActive(0, true);
      //}
      /* Alternative to the last model state, leftmost color going grey, selected channel only going color */
      if (this.isGreyModel()) {
        var found = false;
        for (var idx=0; idx < this.loadedImg.channels.length; idx++) {
          if (this.loadedImg.channels[idx].active) {
            this.setChannelActive(idx, true, true);
            found = true;
            break;
          }
        }
        if (!found) {
          this.setChannelActive(0, true, true);
        }
      }
      if (!noreload) {
        _load(function () {
            if (_this.loadedImg.rdefs.projection.toLowerCase() == 'split') {
              var size = getSizeDict();
              _this.viewportimg.get(0).setZoomToFit(true, size.width, size.height);
            }
          });
      }
      _this.self.trigger('modelChange', [_this]);
    }
  };

  this.getModel = function () {
    return _this.loadedImg.rdefs.model ? _this.loadedImg.rdefs.model.toLowerCase().substring(0,1) : null;
  };

  this.isGreyModel = function () {
    return _this.loadedImg.rdefs.model ? _this.loadedImg.rdefs.model.toLowerCase().substring(0,1) == 'g' : null;
  };

  this.getPixelSizes = function () {
    return _this.loadedImg.pixel_size;
  };

  this.setQuality = function (q, noreload) {
    q = parseFloat(q);
    if (q != _this.loadedImg.current.quality) {
      _this.loadedImg.current.quality = q;
      if (!noreload) {
	_load();
      }
    }
  };

  this.getQuality = function () {
    return _this.loadedImg.current.quality;
  };

  this.getServer = function () {
    return server;
  };

  this.getSizes = function () {
    var s = _this.loadedImg.size;
    var rv = {width: s.width,
        height: s.height,
        c: s.c};
    if (_this.loadedImg.rdefs.invertAxis) {
      rv.z= s.t;
      rv.t = s.z;
    } else {
      rv.z = s.z;
      rv.t = s.t;
    }
    return rv;
  };

  this.getTCount = function () {
    return _this.getSizes().t;
  };

  this.getPos = function () {
    var p = _this.loadedImg.current;
	var rv = {};
    if (_this.loadedImg.rdefs.invertAxis) {
      v.t = p.z;
      v.z = p.t;
    } else {
      rv.t = p.t;
      rv.z = p.z;
    }
    return rv;
  };

  this.getTPos = function () {
    return _this.getPos().t + 1;
  };

  this.getZCount = function () {
    return _this.getSizes().z;
  };

  this.getZPos = function () {
    return _this.getPos().z + 1;
  };

  this.setPixelated = function (pixelated) {
    _this.viewportimg.get(0).setPixelated(pixelated);
  };

  this.setZoom = function (z) {
    var size = getSizeDict();
    _this.viewportimg.get(0).setZoom(z, size.width, size.height);
  };

  this.getZoom = function () {
    if (_this.loadedImg.tiles) {
      var viewerBean = _this.viewportimg.get(0).getBigImageContainer();
      if (viewerBean) {
        return viewerBean.currentScale()*100;
      }
      return 100;
    }
    return _this.loadedImg.current.zoom;
  };

  this.setZoomToFit = function (only_shrink) {
    var size = getSizeDict();
    _this.viewportimg.get(0).setZoomToFit(only_shrink, size.width,size.height);
  };

  /*                       */
  /*************************/

  /**
   * Undo / Redo support
   */

  var channels_undo_stack = [];
  var channels_undo_stack_ptr = -1;
  var saved_undo_stack_ptr = 0;   // channels_undo_stack_ptr will start off here
  var channels_bookmark = null;

  var compare_stack_entries = function (e1, e2) {
    if (e1.model != e2.model) {
      return false;
    }
    for (var i=0; i<e1.channels.length; i++) {
      if (!(e1.channels[i].active == e2.channels[i].active &&
            OME.rgbToHex(e1.channels[i].color) == OME.rgbToHex(e2.channels[i].color) &&
            e1.channels[i].windowStart == e2.channels[i].windowStart &&
            e1.channels[i].windowEnd == e2.channels[i].windowEnd &&
            e1.channels[i].metalabel == e2.channels[i].metalabel)) {
        return false;
      }
    }
    return true;
  };

  this.save_channels = function () {
    /* Store all useful information */
    var entry = {channels:[], model: this.getModel()};
    var channels = _this.loadedImg.channels;
    for (i=0; i<channels.length; i++) {
      var channel = {active: channels[i].active,
                     color: toRGB(channels[i].color),
                     windowStart: channels[i].window.start,
                     windowEnd: channels[i].window.end,
                     metalabel: channels[i].metalabel};
      entry.channels.push(channel);
    }
    /* Trim stack to current position to dump potential redo information */
    if (channels_undo_stack_ptr == -1 || !compare_stack_entries(entry, channels_undo_stack[channels_undo_stack_ptr])) {
      channels_undo_stack_ptr++;
      channels_undo_stack.length = channels_undo_stack_ptr;
      channels_undo_stack.push(entry);
    }
  };

  this.undo_channels = function (redo) {
    if (channels_undo_stack_ptr >= 0) {
//      if (channels_undo_stack.length-1 == channels_undo_stack_ptr && !redo) {
//        /* Currently at the tip of the stack */
//        this.save_channels();
//      }
      channels_undo_stack_ptr--;
      var entry = channels_undo_stack[channels_undo_stack_ptr];
      this.setModel(entry.model);
      for (var i=0; i < entry.channels.length; i++) {
        this.setChannelWindow(i, entry.channels[i].windowStart, entry.channels[i].windowEnd, true);
        this.setChannelColor(i, entry.channels[i].color, true);
        this.setChannelActive(i, entry.channels[i].active, true);
        this.setChannelLabel(i, entry.channels[i].metalabel, true);
      }
      _load();
    }
  };

  // When we Save settings to the server, we can remember the point we saved.
  this.setSaved = function() {
    saved_undo_stack_ptr = channels_undo_stack_ptr;
    _this.loadedImg.defaultZ = this.getZPos()-1;
    _this.loadedImg.defaultT = this.getTPos()-1;
  };
  // Do we have any unsaved changes? (undo/redo since we last saved)
  this.getSaved = function() {
    var zSaved = _this.loadedImg.defaultZ === this.getZPos()-1;
    var tSaved = _this.loadedImg.defaultT === this.getTPos()-1;
    return (zSaved && tSaved && saved_undo_stack_ptr === channels_undo_stack_ptr);
  };

  this.doload = function(){
    _load();
  };

  this.has_channels_undo = function () {
    return channels_undo_stack_ptr > 0;
  };

  this.redo_channels = function () {
    if (channels_undo_stack_ptr > -1 && (channels_undo_stack.length-1 > channels_undo_stack_ptr)) {
      channels_undo_stack_ptr+=2;
      this.undo_channels(true);
    }
  };

  this.has_channels_redo = function () {
    return channels_undo_stack.length-1 > channels_undo_stack_ptr;
  };

  this.reset_channels = function () {
    if (channels_undo_stack.length > 0) {
      channels_undo_stack.length = 1;
      channels_undo_stack_ptr = 1;
      this.undo_channels(true);
    }
  };

  // bookmarks were previously set and used when rdef dialog
  // was hidden and show. Not used currently.
  this.bookmark_channels = function () {
    channels_bookmark = channels_undo_stack_ptr+1;
  };

  this.back_to_bookmarked_channels = function () {
    if (channels_bookmark) {
      channels_undo_stack_ptr = channels_bookmark;
      this.undo_channels(true);
    }
  };

  this.forget_bookmark_channels = function () {
    channels_bookmark = null;
  };


  /**
   * @return {String} The current query with state information.
   */
  this.getQuery = function (include_slider_pos, include_xy_pos, include_zoom) {
      
    var query = [];
    /* Channels (verbose as IE7 does not support Array.filter */
    var chs = [];
    var channels = this.loadedImg.channels;
    for (i=0; i<channels.length; i++) {
      var ch = channels[i].active ? '' : '-';
      ch += parseInt(i, 10)+1;
      ch += '|' + channels[i].window.start + ':' + channels[i].window.end;
      ch += '$' + OME.rgbToHex(channels[i].color);
      chs.push(ch);
    }
    query.push('c=' + chs.join(','));
    /* Rendering Model */
    query.push('m=' + this.loadedImg.rdefs.model.toLowerCase().substring(0,1));
    /* Projection */
    query.push('p=' + this.loadedImg.rdefs.projection.toLowerCase());
    /* Inverted Axis */
    query.push('ia=' + (this.loadedImg.rdefs.invertAxis?1:0));
    /* Image Quality */
    if (this.loadedImg.current.quality) {
      query.push('q=' + this.loadedImg.current.quality);
    }
    /* Slider positions */
    if (include_slider_pos) {
      query.push('t=' + (this.loadedImg.current.t+1));
      query.push('z=' + (this.loadedImg.current.z+1));
    }
    if (include_zoom) {
        /* Zoom - getZoom() also handles big images */
        query.push('zm=' + this.getZoom());
    }
    /* Image offset */
    if (include_xy_pos) {
        if ((_this.loadedImg.tiles) && (_this.viewportimg.get(0).getBigImageContainer() )) {
            // if this is a 'big image', calculate the current center of the viewport
            var big_viewer = _this.viewportimg.get(0).getBigImageContainer();
            var big_x = big_viewer.x * -1;
            var big_y = big_viewer.y * -1;
            var big_w = big_viewer.width / 2;
            var big_h = big_viewer.height / 2;
            var big_scale = big_viewer.currentScale();
            var big_center_x = (big_x + big_w) / big_scale;
            var big_center_y = (big_y + big_h) / big_scale;
            query.push('x=' + big_center_x);
            query.push('y=' + big_center_y);
        } else {
            query.push('x=' + this.viewportimg.get(0).getXOffset());
            query.push('y=' + this.viewportimg.get(0).getYOffset());
        }
    }
    /* Line plot */
    if (this.hasLinePlot()) {
      query.push('lp=' + (linePlot.isHorizontal()?'h':'v') + linePlot.position);
    }
    if (this.loadedImg.current.query.debug !== undefined) {
      query.push('debug='+this.loadedImg.current.query.debug);
    }
    return query.join('&');
  };

  this.setQuery = function (query) {
    // setModel first since this affects channels we can have active
    if (query.m) this.setModel(query.m, true);
    if (query.c) {
      var chs = query.c.split(',');
      for (j=0; j<chs.length; j++) {
        var t = chs[j].split('|');
        var idx;
        if (t[0].substring(0,1) == '-') {
          idx = parseInt(t[0].substring(1), 10)-1;
          this.setChannelActive(idx, false);
        } else {
          idx = parseInt(t[0], 10)-1;
          this.setChannelActive(idx, true);
        }
        if (t.length > 1) {
          t = t[1].split('$');
          var window = t[0].split(':');
          if (window.length == 2) {
            this.setChannelWindow(idx, parseFloat(window[0], 10), parseFloat(window[1], 10), true);
          }
        }
        if (t.length > 1) {
          this.setChannelColor(idx, toRGB(t[1]), true);
        }
      }
    }
    if (query.q) this.setQuality(query.q, true);
    if (query.p) this.setProjection(query.p, true);
    if (query.p) this.setInvertedAxis(query.ia, true);
    if (query.zm) {
      this.loadedImg.query_zoom = query.zm;  // for big images
      this.setZoom(parseInt(query.zm, 10));
    }
    if (query.t) {
      this.loadedImg.current.t = parseInt(query.t, 10)-1;
    }
    if (query.z) {
      this.loadedImg.current.z = parseInt(query.z, 10)-1;
    }
    if (query.x) this.viewportimg.get(0).setXOffset(parseInt(query.x, 10));
    if (query.y) this.viewportimg.get(0).setYOffset(parseInt(query.y, 10));
    if (query.lp) {
      this.prepareLinePlot(query.lp.substring(0,1));
      linePlot.position = parseInt(query.lp.substring(1), 10);
    }
  };

  this.getRelUrl = function (append) {
    append = append !== undefined ? '/'+append : '';
    return this.loadedImg.id + '/' + this.loadedImg.current.z + '/' + this.loadedImg.current.t + append + '/?' + this.getQuery();
  };

  this.getUrl = function (base) {
    var rv = server + '/' + base + '/' + this.getCurrentImgUrlPath();
    return rv + '?' + this.getQuery(true);
  };

  /**
   * Returns the image and optional dataset part of the url.
   */
  this.getCurrentImgUrlPath = function () {
    var rv = this.loadedImg.id + '/';
    if (this.loadedImg.current.datasetId) {
      rv += this.loadedImg.current.datasetId + '/';
    }
    return rv;
  };

  /**
   * Verifies if the image has suffered changes since it was first loaded.
   * Only changes that are related to the rendering settings are checked.
   * @return true if the rendering settings have changed, false otherwise
   */
  this.hasSettingsChanges = function () {
    return !_this.loadedImg.hasSameSettings(_this.loadedImg_def);
  };

  /**
   * Some events are handled by us, some are proxied to the viewport plugin.
   */
  this.bind = function (event, callback) {
    if (event == 'projectionChange' || event == 'modelChange' || event == 'channelChange' ||
    event == 'imageChange' || event == 'imageLoad' || event == 'linePlotPos' || event == 'linePlotChange') {
      _this.self.bind(event, callback);
    } else {
      _this.viewportimg.bind(event, callback);
    }
  };

  this.self.mousedown(function () {
    // Try to avoid selection on double click
    return false;
  });

//  this.refresh();

};

