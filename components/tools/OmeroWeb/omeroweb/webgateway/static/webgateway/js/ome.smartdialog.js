/**
 * (somewhat) Smart(er) Dialog plugin for JQuery.
 *
 * Todo: better documentation and example usage.
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

$.fn.sdialog = function(cfg) {
  return this.each(function(){
    /* The basic setup */
    var self = jQuery(this);

    var dragbar = self.find('h1:first');
    var other = dragbar.siblings();
    other.remove();
    dragbar.after('<div class="sdialog-content">');
    var content = self.find('.sdialog-content').append(other);
    self
      //.append('<div class="sdialog-resize-bar">')
      .addClass('sdialog');

    this.recalcSize = function (xtra_height) {
      var internsize = dragbar.height()+(xtra_height || 0);// + self.find('.sdialog-resize-bar').height();
      var contentsize=self.height()-internsize;
      if (contentsize < 0) {
        self.css('height', internsize);
        contentsize = 0;
      }
      content.css('height', contentsize);
    };

    this.placeAround = function (elm, xoffset, yoffset) {
      var epos = $(elm).offset();
      var left = epos.left;
      var size = {w: self.width(), h: self.height()};
      var wsize = {w: $(window).width(), h: $(window).height()};
      if (wsize.w < size.w) {
        size.w = wsize.w;
        self.css('width', size.w);
      }
      left -= (size.w/2) - ($(elm).width() / 2);
      if (left + size.w > wsize.w) {
        left = wsize.w - size.w - 10;
      }
      
      if (xoffset) {
        left -= xoffset;
      }
      if (yoffset) {
        size.h -= yoffset;
      }
      self.css({top: epos.top - size.h, left: left});
      this.recalcSize();
    }

    var _showdelay = null;
    var _hidedelay = null;
    this.delayedShow = function (delay, func) {
      if (_hidedelay != null) {
        clearTimeout(_hidedelay);
        _hidedelay = null;
      } else if (_showdelay == null) {
        _showdelay = setTimeout(function () {
            func && func(self.get(0)) || self.show();
            _showdelay = null;
          }, delay);
        return true;
      }
      return false;
    }

    this.delayedHide = function (delay, func) {
      if (_showdelay != null) {
        clearTimeout(_showdelay);
        _showdelay = null;
      } else if (_hidedelay == null) {
        _hidedelay = setTimeout(function () {
            func && func(self.get(0)) || self.hide();
            _hidedelay = null;
          }, delay);
        return true;
      }
      return false;
    }

  });
}

