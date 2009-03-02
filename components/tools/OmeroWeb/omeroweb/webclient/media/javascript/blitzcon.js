
 /**
  * Close the minimal viewer for a specific Dataset, and deselect the thumbnail.
  */
  function close_viewer (e, did) {
    $('#project-list').css({maxHeight: ''});
    $('#image-legend').hide('slow');
    if (did) {
      var elm = $('#iv'+did);
      if (elm.length) {
        elm.parent().hide('slow', function () {
          elm.parents('.image-viewport-wrapper').remove();//html('');
        });
        elm.get(0).WeblitzViewport = null;
      }
      var fb = ('.figure-box', elm.parent().parent());
      if (fb.length != 0) {
        $.ThumbSlider($('.figure-box')).rmFocus($(e));
      }
    } else {
      $('.image-viewport').each(function () {
        var elm = $(this);
        elm.parent().hide('slow', function () {
          elm.parents('.image-viewport-wrapper').remove();//html('');
        });
        elm.get(0).WeblitzViewport = null;
        var fb = ('.figure-box', elm.parent().parent());
        if (fb.length != 0) {
          $.ThumbSlider($('.figure-box')).rmFocus($(e));
        }
      });
    }
    //if (e) {
    //  $(e).parents('.image-viewport').siblings('.figure-box').find('img.selected').removeClass('selected');
    //}
  }
/**
 * Some design specific calculations are needed to fit everything correctly.
 */
 function _refresh_cb (ev, wb) {
   var calc_height = wb.self.height()-wb.bottom.height()-5;
   if (calc_height > 0) {
     wb.top.css('height', calc_height);
   }
   wb.self.parent().parent().css('width', wb.top.width() + wb.zslider.width());
   //var sli = jQuery('.slider-line', wb.tslider);
   //var btn = jQuery('.slider-btn-up', wb.tslider);
   //sli.css('width', wb.tslider.width() - (btn.width()*2) - parseInt(wb.bottom.css('left')));
   //sli = jQuery('.slider-line', wb.zslider);
   //btn = jQuery('.slider-btn-up', wb.zslider);
   //sli.css('height', wb.zslider.height() - (btn.height()*2));
   //var ith = wb.self.parent().parent().find('.image-thumbs');
 }

/**
 * Open the full viewer for a specific image.
 * Passing the dataset is needed to allow showing 'Figure List' on the viewer toolbar.
 */
 function popup (did, wname, iid) {
   if (iid == null) {
     iid = $.WeblitzViewport($('#iv'+did), '/webclient', _refresh_cb).loadedImg.id;
   }
   var w = window.open('/webclient/img_detail/' + iid + '/' + did + '/', wname,
               "toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=yes,width=1000,height=800");
 }

/**
 * Open or reuse a minimal viewer (one per Dataset). Also selects the appropriate thumbnail.
 */
 function launch_viewer (e, did, iid) {
   /* Close the tooltip */
   $('#details-popup').get(0).delayedHide(1, function (elm) {
     $(elm).removeShadow().slideUp('fast');
   });
   // 'e' will be something inside the thumb slider, so check if we have a viewport below it.
   var fb = $(e).parents('div.figure-box');
   if (fb.parent().find('#iv'+did).length == 0) {
     var vw = $('<div class="image-viewport-wrapper">').insertBefore(fb);
     var iv = $('<div class="image-viewport" id="vp'+did+'">').appendTo(vw);
     var tb = $('<div class="toolbox">').appendTo(iv);
     iv.append('<div class="image-viewer" id="iv'+did+'">');
     iv.append('<div class="viewport-thumb-name">aaa</div>');

     tb.append('<a href="#" onclick="popup('+did+",'_blank'"+'); close_viewer(this,'+did+'); return false;">\
                 Open Full Viewer</a>');
     tb.append('|');
     tb.append('<a href="#" onclick="close_viewer(this,'+did+'); return false;">\
                <img src="/media/img/blitzcon/close.gif" alt="close" /></a>');
     //vw.cornerz({radius: 25});
   }  
                 
   var elm = $('#iv'+did);
   elm.siblings('.viewport-thumb-name').html(global.cache['i'+did+':'+iid].shortName);
   var vp = $.WeblitzViewport(elm, '/webclient');//, _refresh_cb);
   //vp.bind('imageLoad', _refresh_cb);
   vp.setQuality(0.5);
   elm.parent().show('slow', function () {
     vp.load(iid);
     $('#image-legend .legend-text').html(global.cache['i'+did+':'+iid].full_description)//base.find('#image-description'+iid).html())
                 .css('max-height', elm.height());
     window.scrollTo(0,elm.parent().offset().top - 20);
     $('#project-list').css({maxHeight: elm.offset().top - $('#project-list').offset().top-30});
     $('#image-legend').css({top: elm.offset().top, position: 'absolute'}).show('slow');
   });
   $.ThumbSlider($('.figure-box')).setFocus($(e));
 }
