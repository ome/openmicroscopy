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

jQuery.fn.WeblitzViewport = function (server, refresh_cb) {
  return this.each
  (
   function () {
     jQuery._WeblitzViewport (this, server, refresh_cb);
   });
};

jQuery.WeblitzViewport = function (elm, server, refresh_cb) {
  var container = jQuery(elm).get(0);
  var rv = container.WeblitzViewport || (container.WeblitzViewport = new jQuery._WeblitzViewport(container, server));
  if (refresh_cb) {
    elm.unbind('refresh');
    elm.bind('refresh', refresh_cb);
  }
  return rv;
};

jQuery._WeblitzViewport = function (container, server) {
  this.self = jQuery(container);
  var _this = this;
  var thisid = this.self.attr('id');
  this.loadedImg = null;
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
  this.zslider.slider({ orientation: 'v', tooltip_prefix: 'Z=', repeatCallback: done_reload });
  this.tslider.slider({ tooltip_prefix: 'T=', repeatCallback: done_reload });
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
    //_this.refresh();
    if (callback) {
      callback();
    }
  }

  var _reset = function (data) {
    _this.loadedImg = data;
    data.current = {z: parseInt(data.z_count / 2), t: 0};
    _load(function () {_this.refresh(); _this.viewportimg.get(0).setZoomToFit(true, data.width, data.height);});
    _this.zslider.get(0).setSliderRange(1, data.z_count, data.current.z+1);
    _this.tslider.get(0).setSliderRange(1, data.t_count, data.current.t+1);
  }

  var _load = function (callback) {
    var href = server + '/render_image/' + _this.getRelUrl();
    var rcb = function () { after_img_load_cb(callback); _this.viewportimg.unbind('load', rcb); };
    _this.viewportmsg.show();
    _this.viewportimg.load(rcb);
    _this.viewportimg.attr('src', href);
  }

  this.setQuality = function (q) {
    q = parseFloat(q);
    if (!isNaN(q) && q > 0 && q <= 1.0) {
      opts['q'] = q;
    }
  }

  this.getQuality = function () {
    return opts.q;
  }

  this.load = function(iid) {
    _this.viewportmsg.show();
    //viewportimg.hide();
    jQuery.getJSON(server+'/imgData/'+iid, _reset);
  };

  this.refresh = function () {
    this.self.trigger('refresh', [_this]);
    _this.viewportimg.get(0).refresh();
  };

  this.getRelUrl = function () {
    var query = ''
    for (k in opts) {
      query += k + '=' + opts[k];
    }
    if (query.length) {
      query = '?' + query;
    }
    return this.loadedImg.id + '/' + this.loadedImg.current.z + '/' + this.loadedImg.current.t + '/' + query;
  }
};
