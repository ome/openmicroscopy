/**
 * Weblitz image viewport
 *
 * Depends on jquery, jquery-plugin-viewportImage
 *
 * Author: C. Neves <carlos@glencoesoftware.com>
 *
 * Copyright (c) 2007, 2008 Glencoe Software, Inc. All rights reserved.
 * 
 * This software is distributed under the terms described by the LICENCE file
 * you can find at the root of the distribution bundle, which states you are
 * free to use it only for non commercial purposes.
 * If the file is missing please request a copy by contacting
 * jason@glencoesoftware.com.
 *
 */

/* Public constructors */

jQuery.fn.WeblitzViewport = function (server) {
  return this.each
  (
   function () {
     jQuery._WeblitzViewport (this, server);
   });
};

jQuery.WeblitzViewport = function (elm, server) {
  var container = jQuery(elm).get(0);
  var rv = container.WeblitzViewport || (container.WeblitzViewport = new jQuery._WeblitzViewport(container, server));
  return rv;
};

/* Helper objects */

var Metadata = function () {
  this._loaded = false;
  this.current = {};
  this.rdefs = {};
  this._load = function (data) {
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
    this.current.z = parseInt(this.size.z / 2);
    this.current.t = 0;
    this.current.zoom = 100;
  }
  return true;
}

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

jQuery._WeblitzViewport = function (container, server) {
  this.self = jQuery(container);
  var _this = this;
  var thisid = this.self.attr('id');
  this.loadedImg = new Metadata();
  var topid = thisid + '-top';
  var zsliderid = thisid + '-zsl';
  var viewportid = thisid + '-vp';
  var viewportimgid = thisid + '-img';
  var viewportmsgid = thisid + '-msg';
  var bottomid = thisid + '-bot';
  var tsliderid = thisid + '-tsl';
  var opts = {};
  this.self.append('<div id="'+topid+'" class="weblitz-viewport-top">');
  this.top = jQuery('#'+topid);
  this.top.append('<div id="'+zsliderid+'">');
  this.zslider = jQuery('#'+zsliderid);
  this.top.append('<div id="'+viewportid+'" class="weblitz-viewport-vp">');
  this.viewport = jQuery('#'+viewportid);
  this.viewport.append('<img id="'+viewportimgid+'">');
  this.viewportimg = jQuery('#'+viewportimgid);
  this.viewport.append('<div id="'+viewportmsgid+'" class="weblitz-viewport-msg">Loading...</div>');
  this.viewportmsg = jQuery('#'+viewportmsgid);
  this.self.append('<div id="'+bottomid+'" class="weblitz-viewport-bot">');
  this.bottom = jQuery('#'+bottomid);
  this.bottom.append('<div id="'+tsliderid+'">');
  this.tslider = jQuery('#'+tsliderid);

  var done_reload = function () {
    return _this.viewportmsg.is(':hidden');
  };

  this.viewportimg.viewportImage();
  this.viewportimg.bind('zoom', function (e,z) { _this.loadedImg.current.zoom = z; });
  this.zslider.slider({ orientation: 'v', min:0, max:0, tooltip_prefix: 'Z=', repeatCallback: done_reload });
  this.tslider.slider({ tooltip_prefix: 'T=', min:0, max:0, repeatCallback: done_reload });
  this.viewportimg.css('overflow', 'hidden');
  this.zslider.bind('change', function (e,pos) {
	_this.loadedImg.current.z = pos-1;
        _load();
     });
  this.tslider.bind('change', function (e,pos) {
	_this.loadedImg.current.t = pos-1;
        _load();
     });

  var after_img_load_cb = function (callback) {
    _this.viewportmsg.hide();
    _this.viewportimg.show();
    if (callback) {
      callback();
    }
    _this.self.trigger('imageLoadSuccess', [_this]);
  }
  
  var after_img_fail_cb = function (callback) {
    _this.viewportmsg.hide();
    _this.viewportimg.hide();
    if (callback) {
      callback();
    }
    _this.self.trigger('imageLoadFailure', [_this]);
  }
  
  /**
   * Initializes the data structures with the supplied data values.
   * Gets called as callback from the json AJAX fetching of image metadata.
   *
   * @param {Dict} data The image data from the server as a dictionary with the following keys:
   *                     {id, width, height, z_count, t_count, c_count,
   *                      rdefs:{model,},
   *                      channels:[{emissionWave,color,active},]}
   */
  var _reset = function (data) {
    _this.loadedImg._load(data);
    if (_this.loadedImg.current.query) {
      _this.setQuery(_this.loadedImg.current.query);
    }
    _load(function () {
      _this.refresh();
      //if (!_this.viewportimg.get(0).getOrigWidth()) {
	_this.viewportimg.get(0).setZoomToFit(true, _this.loadedImg.size.width, _this.loadedImg.size.height);
	//}
    });
    _this.zslider.get(0).setSliderRange(1, _this.loadedImg.size.z, _this.loadedImg.current.z+1, false);
    _this.tslider.get(0).setSliderRange(1, _this.loadedImg.size.t, _this.loadedImg.current.t+1, false);
    _this.self.trigger('imageLoad', [_this]);
  }

  /**
   * Request a particular view of the current image from the server.
   * The definition of the view will be whatever is set in loadedImg at this point.
   * 
   */
  var _load = function (callback) {
    if (_this.loadedImg._loaded) {
      var href = server + '/render_image/' + _this.getRelUrl();
      var rcb = function () { 
        after_img_load_cb(callback);
        _this.viewportimg.unbind('load', rcb);
        _this.viewportimg.unbind('error', fcb);
      };
      var fcb = function () {
          alert("Image cannot be visible. If you are in share please reload the share.")
        after_img_fail_cb(callback);
        _this.viewportimg.unbind('load', rcb);
        _this.viewportimg.unbind('error', fcb);
      };
      _this.viewportmsg.show();
      _this.viewportimg.load(rcb);
      _this.viewportimg.bind('error', fcb);
      _this.viewportimg.attr('src', href);
      _this.self.trigger('imageChange', [_this]);
    }
  }

  /**
   * @param {Integer} iid The image id on the database.
   * @param {Integer} dsid The Dataset id this image belongs to, optional.
   */
  this.load = function(iid, share_id, query) {
    _this.viewportmsg.show();
    _this.loadedImg.current.share_id = share_id;
    _this.loadedImg.current.query = query;
    //viewportimg.hide();
    if (_this.loadedImg.current.share_id) {
        jQuery.getJSON(server+'/imgData/'+iid +'/'+share_id, _reset);
    } else {
        jQuery.getJSON(server+'/imgData/'+iid, _reset);
    }
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
        _this.viewport.css('width', _this.viewport.width() - _this.viewport.position().left);
      }
    } else {
      remember_allow_resize = true;
    }
    _this.tslider.css('width', _this.viewport.width());
    sli.css('width', _this.tslider.width() - (btn.width()*2) - _this.bottom.position().left);
    sli = jQuery('.slider-line', _this.zslider);
    btn = jQuery('.slider-btn-up', _this.zslider);
    sli.css('height', _this.zslider.height() - (btn.height()*2));
    _this.viewportimg.get(0).refresh();
  };

  /********************************************/
  /* Attribute getters/setters/wrappers below */

  this.getAuthor = function () {
    return _this.loadedImg.meta.author;
  }

  this.getChannels = function () {
    return _this.loadedImg.channels;
  }

  this.toggleChannel = function (idx) {
    this.setChannelActive(idx, !_this.loadedImg.channels[idx].active);
  }

  this.getCCount = function () {
    return _this.loadedImg.size.c;
  }

  this.setChannelActive = function (idx, act, noreload) {
    if (this.isGreyModel()) {
      /* Only allow activation of channels, and disable all other */
      if (act) {
	for (i in _this.loadedImg.channels) {
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
  }

  this.setChannelColor = function (idx, color, noreload) {
    _this.loadedImg.channels[idx].color = color;
    _this.self.trigger('channelChange', [_this, idx, _this.loadedImg.channels[idx]]);
    if (!noreload) {
      _load();
    }
  }

  this.setChannelWindow = function (idx, start, end, noreload) {
    var channel = _this.loadedImg.channels[idx];
    if (channel.window.min <= start) {
      channel.window.start = start;
    }
    if (channel.window.max >= end) {
      channel.window.end = end;
    }
    _this.self.trigger('channelChange', [_this, idx, _this.loadedImg.channels[idx]]);
    if (!noreload) {
      _load();
    }
  }

  this.getMetadata = function () {
    return _this.loadedImg.meta;
  }

  this.setModel = function (m, noreload) {
    /* We only ever look at the first letter */
    m = m.toLowerCase().substring(0,1);
    if (_this.loadedImg.rdefs.model.toLowerCase().substring(0,1) != m) {
      _this.loadedImg.rdefs.model = m;
      var lmc = _this.loadedImg.current.lastModelChannels;
      _this.loadedImg.current.lastModelChannels = new Array();
      for (i in _this.loadedImg.channels) {
	_this.loadedImg.current.lastModelChannels.push(_this.loadedImg.channels[i].active);
      }
      if (lmc) {
	for (i in lmc) {
	  this.setChannelActive(i, lmc[i]);
	}
      } else if (this.isGreyModel()) {
	this.setChannelActive(0, true);
      }
      if (!noreload) {
	_load();
      }
      _this.self.trigger('modelChange', [_this]);
    }
  }

  this.getModel = function () {
    return _this.loadedImg.rdefs.model ? _this.loadedImg.rdefs.model.toLowerCase().substring(0,1) : null;
  }

  this.isGreyModel = function () {
    return _this.loadedImg.rdefs.model ? _this.loadedImg.rdefs.model.toLowerCase().substring(0,1) == 'g' : null;
  }

  this.getPixelSizes = function () {
    return _this.loadedImg.pixel_size;
  }

  this.setQuality = function (q, noreload) {
    q = parseFloat(q);
    if (q != _this.loadedImg.current.quality) {
      _this.loadedImg.current.quality = q;
      if (!noreload) {
	_load();
      }
    }
  }

  this.getQuality = function () {
    return _this.loadedImg.current.quality;
  }

  this.getServer = function () {
    return server;
  }

  this.getSizes = function () {
    return _this.loadedImg.size;
  }

  this.getTCount = function () {
    return _this.loadedImg.size.t;
  }

  this.getTPos = function () {
    return _this.loadedImg.current.t + 1;
  }

  this.getZCount = function () {
    return _this.loadedImg.size.z;
  }

  this.getZPos = function () {
    return _this.loadedImg.current.z + 1;
  }

  this.setZoom = function (z) {
    _this.viewportimg.get(0).setZoom(z, _this.loadedImg.size.width,_this.loadedImg.size.height);
  }

  this.getZoom = function () {
    return _this.loadedImg.current.zoom;
  }

  this.setZoomToFit = function (only_shrink) {
    _this.viewportimg.get(0).setZoomToFit(only_shrink, _this.loadedImg.size.width,_this.loadedImg.size.height);
  }

  /*                       */
  /*************************/

  /**
   * Undo / Redo support
   */

  var channels_undo_stack = [];
  var channels_undo_stack_ptr = -1;

  this.save_channels = function () {
    /* Store all useful information */
    var entry = [];
    var channels = _this.loadedImg.channels;
    for (i in channels) {
      var channel = {active: channels[i].active,
                     color: channels[i].color,
                     windowStart: channels[i].window.start,
                     windowEnd: channels[i].window.end};
      entry.push(channel);
    }
    /* Trim stack to current position to dump potential redo information */
    channels_undo_stack_ptr++;
    channels_undo_stack.length = channels_undo_stack_ptr;
    channels_undo_stack.push(entry);
  }

  this.undo_channels = function (redo) {
    if (channels_undo_stack_ptr >= 0) {
      if (channels_undo_stack.length-1 == channels_undo_stack_ptr && !redo) {
        /* Currently at the tip of the stack */
        this.save_channels();
        channels_undo_stack_ptr--;
      }
      entry = channels_undo_stack[channels_undo_stack_ptr];
      channels_undo_stack_ptr--;
      for (i in entry) {
        this.setChannelWindow(i, entry[i].windowStart, entry[i].windowEnd, true);
        this.setChannelColor(i, entry[i].color, true);
        this.setChannelActive(i, entry[i].active, true);
      }
      _load();
    }
  }

  this.has_channels_undo = function () {
    return channels_undo_stack_ptr >= 0;
  }

  this.redo_channels = function () {
    if (channels_undo_stack_ptr >= -1 && (channels_undo_stack.length-2 > channels_undo_stack_ptr)) {
      channels_undo_stack_ptr+=2;
      this.undo_channels(true);
    }
  }

  this.has_channels_redo = function () {
    return channels_undo_stack.length-2 > channels_undo_stack_ptr;
  }

  this.reset_channels = function () {
    if (channels_undo_stack.length > 0) {
      channels_undo_stack.length = 1;
      channels_undo_stack_ptr = 0;
      this.undo_channels(true);
    }
  }


  /**
   * @return {String} The current query with state information.
   */
  this.getQuery = function (include_slider_pos) {
    var query = new Array();
    /* Channels (verbose as IE7 does not support Array.filter */
    var chs = new Array();
    var channels = this.loadedImg.channels
    for (i in channels) {
      var ch = channels[i].active ? '' : '-';
      ch += parseInt(i)+1;
      ch += '|' + channels[i].window.start + ':' + channels[i].window.end;
      ch += '$' + rgbToHex(channels[i].color);
      chs.push(ch);
    }
    query.push('c=' + chs.join(','));
    /* Rendering Model */
    query.push('m=' + this.loadedImg.rdefs.model.toLowerCase().substring(0,1));
    /* Image Quality */
    if (this.loadedImg.current.quality) {
      query.push('q=' + this.loadedImg.current.quality);
    }
    /* Zoom */
    query.push('zm=' + this.loadedImg.current.zoom);
    /* Slider positions */
    if (include_slider_pos) {
      query.push('t=' + (this.loadedImg.current.t+1));
      query.push('z=' + (this.loadedImg.current.z+1));
    }
    /* Image offset */
    query.push('x=' + this.viewportimg.get(0).getXOffset());
    query.push('y=' + this.viewportimg.get(0).getYOffset());
    return query.join('&');
  }

  this.setQuery = function (query) {
    if (query.substring(0,1) == '?') {
      query = query.substring(1);
    }
    query = query.split('&');
    for (i in query) {
      var k = query[i].split('=');
      switch (k[0]) {
      case 'c':
	var chs = k[1].split(',');
	for (j in chs) {
	  var t = chs[j].split('|');
	  var idx;
	  if (t[0].substring(0,1) == '-') {
	    idx = parseInt(t[0].substring(1))-1;
	    this.setChannelActive(idx, false);
	  } else {
	    idx = parseInt(t[0])-1;
	    this.setChannelActive(idx, true);
	  }
	  if (t.length > 1) {
	    t = t[1].split('$');
	    var window = t[0].split(':');
	    if (window.length == 2) {
	      this.setChannelWindow(idx, parseInt(window[0]), parseInt(window[1]), true);
	    }
	  }
	  if (t.length > 1) {
	    this.setChannelColor(idx, t[1], true);
	  }
	}
	break;
      case 'm':
	this.setModel(k[1], true);
	break;
      case 'q':
	this.setQuality(k[1], true);
	break;
      case 'zm':
	this.setZoom(parseInt(k[1]));
	break;
      case 't':
	this.loadedImg.current.t = parseInt(k[1]-1);
	break;
      case 'z':
	this.loadedImg.current.z = parseInt(k[1]-1);
	break;
      case 'x':
	this.viewportimg.get(0).setXOffset(parseInt(k[1]));
	break;
      case 'y':
	this.viewportimg.get(0).setYOffset(parseInt(k[1]));
	break;
      }
    }
  }

  this.getRelUrl = function () {
    var rv = this.loadedImg.id + '/' + this.loadedImg.current.z + '/' + this.loadedImg.current.t;
    if (this.loadedImg.current.share_id) {
        rv += '/' + this.loadedImg.current.share_id;
    }
    return rv + '/?' + this.getQuery();
  }

  this.getUrl = function (base) {
    var rv = server + '/' + base + '/' + this.loadedImg.id;
    if (this.loadedImg.current.share_id) {
      rv += '/' + this.loadedImg.current.share_id;
    }
    return rv + '/?' + this.getQuery(true);
  }

  /**
   * Some events are handled by us, some are proxied to the viewport plugin.
   */
  this.bind = function (event, callback) {
    if (event == 'modelChange' || event == 'channelChange' || event == 'imageChange' || event == 'imageLoad' || event == 'imageLoadSuccess' || event == 'imageLoadFailure') {
      _this.self.bind(event, callback);
    } else {
      _this.viewportimg.bind(event, callback);
    }
  }

};

function rgbToHex(rgb) {
  if (rgb.substring(0,1) == '#') {
    return rgb.substring(1);
  }
  var rgbvals = /rgb\((.+),(.+),(.+)\)/i.exec(rgb);
  if (!rgbvals) return rgb;
  var rval = parseInt(rgbvals[1]).toString(16);
  var gval = parseInt(rgbvals[2]).toString(16);
  var bval = parseInt(rgbvals[3]).toString(16);
  if (rval.length == 1) rval = '0' + rval;
  if (gval.length == 1) gval = '0' + gval;
  if (bval.length == 1) bval = '0' + bval;
  return (
    rval +
    gval +
    bval
  ).toUpperCase(); 
}

