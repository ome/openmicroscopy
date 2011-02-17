/**
 * Post-it plugin for JQuery.
 *
 * Called on a container, creates a post-it like thingy
 *
 * Todo: better documentation and example usage.
 *
 * Depends on jquery, jquery.dimensions.js, jqDnR.js, aop, jquery-plugin-smartdialog.js
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

var aop_handler = function (meth) {
  return function () {
    if (this.get(0) && typeof this.get(0)[meth] == 'function') {
      this.get(0)[meth]();
    }
  };
};

jQuery.aop.before( {target: jQuery, method: 'show'}, aop_handler('postit_open_handler'));
jQuery.aop.after( {target: jQuery.fn, method: 'hide'}, aop_handler('postit_close_handler'));

$.fn.postit = function(cfg) {
  this.sdialog();
  return this.each(function(){
    /* The basic setup */
    var self = jQuery(this);
    this.postit_open_handler = function () {
      self.trigger('opening');
    };
    this.postit_close_handler = function () {
      self.trigger('closed');
    };
    /* Some extra details on the dragbar */
    var dragbar = self.find('h1:first');
    dragbar.dblclick(function(e) { self.toggleClass('collapsed'); });
    self.bind("mousewheel", function(e){
      // Respond to mouse wheel in IE. (It returns up/dn motion in multiples of 120)
      if (e.wheelDelta >= 120)
        self.addClass('collapsed');
      else if (e.wheelDelta <= -120)
        self.removeClass('collapsed');
      e.preventDefault();
    })

    if (dragbar.get(0).addEventListener) {
      // Respond to mouse wheel in Firefox
      dragbar.get(0).addEventListener('DOMMouseScroll', function(e) { 
      if (e.detail > 0)
        self.removeClass('collapsed');
      else if (e.detail < 0)
        self.addClass('collapsed');
      e.preventDefault();
      }, false);
    }

    self
      .append('<div class="postit-toggle-btn">')
      .append('<div class="postit-close-btn">')
      .append('<div class="postit-resize-bar">')
      .addClass('postit')
      .removeClass('sdialog');
    /* The buttons */
    self.find('.postit-toggle-btn').click(function(e) { self.toggleClass('collapsed'); });
    self.find('.postit-close-btn').click(function(e) { self.hide(); });
    self.find('.sdialog-content').removeClass('sdialog-content').addClass('postit-content');
    if (cfg && !cfg.noResize) {
      var resize_bar = self.find('.postit-resize-bar');
      var _timer_recalcSize = false;
      var timedRecalcSize = function () {
        if (_timer_recalcSize) {
          return false;
        } else {
          _timer_recalcSize = setTimeout(function () {
              //self.get(0).recalcSize(resize_bar.height());
              _timer_recalcSize = false;
            }, 50);
        };
        return false;
      };
      var target;
      if (cfg && cfg.resizeTarget) {
        target = $(cfg.resizeTarget, self);
        target.append('<div class="postit-resize-btn"></div>');
        if (!$.browser.firefox) {
          target.bind('jqResize', function (e) {
              w = $(e.target).width();
              $(e.target).parents('.postit').css('width',w+8);
            });
        }
        target.jqResize('.postit-resize-btn', {minW: 200, minH: 20});
      } else {
        resize_bar.append('<div class="postit-resize-btn"></div>');
        target = self;
        target.bind('jqResize', timedRecalcSize);
        target.jqResize('.postit-resize-btn', {minW: 40, minH: 40});
      }
    }

    /* We're done, make it draggable */
    var dropEvent = function (e) {
      var viewport = {width: $('body').width(), height: $('body').height()};
      var boundaries = self.position();
      boundaries.right = boundaries.left + self.width();
      boundaries.bottom = boundaries.top + self.height();
      if (boundaries.left < 0) {
        self.css('left', 0);
      } else if (boundaries.right > viewport.width) {
        self.css('left', viewport.width - self.width());
      }
      if (boundaries.top < 0) {
        self.css('top', 0);
      } else if (boundaries.bottom > viewport.height) {
        var top = Math.max(viewport.height - self.height(), 0);
        self.css('top', top);
      }
    };
    self.jqDrag('h1:first');
    self.find('h1:first').bind('mouseup', dropEvent)
  });
}
