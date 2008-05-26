/**
 * Image Thumbnail Slider
 *
 * Depends on jquery
 *
 * Author: C. Neves <carlos@glencoesoftware.com>
 *
 * Copyright (c) 2008 Glencoe Software, Inc. All rights reserved.
 * 
 * This software is distributed under the terms described by the LICENCE file
 * you can find at the root of the distribution bundle, which states you are
 * free to use it only for non commercial purposes.
 * If the file is missing please request a copy by contacting
 * jason@glencoesoftware.com.
 *
 */

/* Public constructors */

jQuery.fn.ThumbSlider = function () {
  return this.each
  (
   function () {
     jQuery._ThumbSlider (this);
   });
};

jQuery.ThumbSlider = function (elm) {
  var container = jQuery(elm).get(0);
  var rv = container.ThumbSlider || (container.ThumbSlider = new jQuery._ThumbSlider(container));
  return rv;
};


/* Aspect Oriented Programming plugin handled methods */

var aop_handler = function (meth) {
  return function () {
    if (this.get(0) && typeof this.get(0)[meth] == 'function') {
      this.get(0)[meth]();
    }
  };
};

jQuery.aop.after( {target: jQuery, method: 'show'}, aop_handler('thumbslider_open_handler'));
jQuery.aop.after( {target: jQuery.fn, method: 'hide'}, aop_handler('thumbslider_close_handler'));

/* (make believe) Private constructor */

/**
 * @constructor 
 */

jQuery._ThumbSlider = function (container) {
  this.self = jQuery(container);
  var _this = this;
  var thisid = this.self.attr('id');
  this.self.addClass('thumbslider');
  var divleft = $('<div class="ts-div-left">').appendTo(this.self);
  var divright = $('<div class="ts-div-right">').appendTo(this.self);
  var wrapper = $('<div class="ts-wrap">').appendTo(this.self);
  var strip = $('<div class="ts-strip">').appendTo(wrapper);
  var slider = $('<div class="ts-slider">').appendTo(this.self);
  var btnleft = $('<div class="ts-left">').appendTo(slider);
  var btnright = $('<div class="ts-right">').appendTo(slider);
  var focus = $('<div class="ts-slider-focus">').appendTo(slider);
  var handle = $('<div class="ts-handle">').appendTo(slider);

  var ondrag = false;
  var _ts_strip_width = 1;
  var _ts_current_span = 0;
  var _ts_dragpos = -1;

  var _recalcSize = function () {
      _move();
      handle.css({width: _curspan() + '%'});
  };

  var _thumb_refcount = 0;

  this.addThumb = function (elm) {
    var thumb = $(elm).appendTo(strip);
    _thumb_refcount++;
    var onload = function () {
      thumb.addClass('ts-thumb');
      _ts_strip_width = strip.width();
      _recalcSize();
      thumb.unbind('load', onload);
      _thumb_refcount--;
      if (_thumb_refcount == 0) {
        _this.self.trigger('thumbsLoaded');
        strip.children().fadeIn('fast');
        _ts_strip_width = strip.width();
        _recalcSize();
      }
    };
    thumb.load(onload);
  };

  this.clear = function () {
    strip.empty();
  };

  var _move = function (d) {
    var p = 0;
    var leftmost = 0;
    var leftpos = parseInt(strip.css('left'));
    var ww = wrapper.width();
    var rightmost = ww-_ts_strip_width;
    if (d == null) {
      // Center stuff
      if (_ts_strip_width > 0) {
        p = rightmost/2;
      }
      if (p <= leftmost && leftpos >= leftmost) { 
        p = leftmost;
      }
    } else {
      // Moving left or right
      if (leftpos > 0) {
        // Strip smaller than available space
        return false;
      }
      p = leftpos + (d>0?64:-64);
      if (p > leftmost && leftpos <= leftmost) { 
        p = leftmost;
      } else if (p < rightmost) {
        p = rightmost;
      }
    }
    strip.css({left: p});
    handle.css({left: _curpos() + '%'});
  }

  var _curspan = function () {
    var ww = wrapper.width();
    _ts_current_span = Math.min(ww*100.0/_ts_strip_width, 100);
    return _ts_current_span;
  }

  var _curpos = function () {
    var leftpos = parseInt(strip.css('left'));
    return Math.max((-leftpos)*100.0/(_ts_strip_width||1), 0);
  }

  btnleft.click(function () {_move(1)});
  btnright.click(function () {_move(-1)});

  container.thumbslider_open_handler = function () {
    _ts_strip_width = strip.width();
    _move();
  };

  slider.click(function (e) {
      /* Move slider to a specific position */
      _ts_dragpos = handle.get(0).clientWidth / 2;
      ondrag = true;
      domove(e);
      ondrag = false;
    });

  handle.mousedown(function (e) {
      /* Start handle drag */
      jQuery(document).mousemove(domove);
      jQuery(document).mouseup(stopdrag);
      _ts_dragpos = e.pageX - handle.offset().left;
      ondrag = true;
    });

  var stopdrag = function (e) {
    ondrag = false;
  };
  
  var domove = function (e) {
    if (ondrag && parseInt(strip.css('left')) <= 0) {
      var xypos, sliderSize;
      sliderSize = slider.get(0).clientWidth;
      xypos = e.pageX - slider.offset().left - _ts_dragpos; //(handle.get(0).clientWidth / 2);
      xypos = xypos * 100.0 / sliderSize ;
      xypos = Math.max(0, Math.min(xypos, 100-_ts_current_span));
      handle.css('left', xypos+'%');
      strip.css('left', -parseInt(xypos*_ts_strip_width/100.0) );
    }
  };

  this.setFocus = function (elm) {
    /* get the position and size of the element, relative to the full strip */
    var relpos = (elm.offset().left-strip.offset().left)*100.0/_ts_strip_width;
    var relwidth = elm.width()*100.0/_ts_strip_width;
    focus.css({width: relwidth+'%', left: relpos+'%'}).show();
    elm.addClass('ts-focus').siblings().removeClass('ts-focus');
  };

  this.rmFocus = function (elm) {
    focus.hide();
    strip.children().removeClass('ts-focus');
  }

  jQuery(window).resize(_recalcSize);
};
