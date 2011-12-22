/*
 * imgAreaSelect jQuery plugin
 * version 0.8
 *
 * Copyright (c) 2008-2009 Michal Wojciechowski (odyniec.net)
 *
 * Dual licensed under the MIT (MIT-LICENSE.txt) 
 * and GPL (GPL-LICENSE.txt) licenses.
 *
 * http://odyniec.net/projects/imgareaselect/
 *
 */

(function($) {

$.imgAreaSelect = { onKeyPress: null };

$.imgAreaSelect.init = function (img, options) {
    var $img = $(img), imgLoaded, $box = $('<div />'), $area = $('<div />'),
        $border1 = $('<div />'), $border2 = $('<div />'), $areaOpera,
        $outLeft = $('<div />'), $outTop = $('<div />'),
        $outRight = $('<div />'), $outBottom = $('<div />'),
        $handles = $([]), handleWidth, handles = [ ], left, top, M = Math,
        imgOfs, imgWidth, imgHeight, $parent, parOfs,
        zIndex = 0, position = 'absolute', $p, startX, startY,
        scaleX = 1, scaleY = 1, resizeMargin = 10, resize = [ ], V = 0, H = 1,
        d, aspectRatio, x1, x2, y1, y2, x, y, adjusted, shown, i,
        selection = { x1: 0, y1: 0, x2: 0, y2: 0, width: 0, height: 0 };

    var $o = $outLeft.add($outTop).add($outRight).add($outBottom);

    function viewX(x)
    {
        return x + imgOfs.left - parOfs.left;
    }

    function viewY(y)
    {
        return y + imgOfs.top - parOfs.top;
    }

    function selX(x)
    {
        return x - imgOfs.left + parOfs.left;
    }

    function selY(y)
    {
        return y - imgOfs.top + parOfs.top;
    }

    function evX(event)
    {
        return event.pageX - parOfs.left;
    }

    function evY(event)
    {
        return event.pageY - parOfs.top;
    }

    function trueSelection()
    {
        return { x1: M.round(selection.x1 * scaleX),
            y1: M.round(selection.y1 * scaleY),
            x2: M.round(selection.x2 * scaleX),
            y2: M.round(selection.y2 * scaleY),
            width: M.round(selection.x2 * scaleX) - M.round(selection.x1 * scaleX),
            height: M.round(selection.y2 * scaleY) - M.round(selection.y1 * scaleY) };
    }

    function getZIndex()
    {
        $p = $img;

        while ($p.length && !$p.is('body')) {
            if (!isNaN($p.css('z-index')) && $p.css('z-index') > zIndex)
                zIndex = $p.css('z-index');
            if ($p.css('position') == 'fixed')
                position = 'fixed';

            $p = $p.parent();
        }

        if (!isNaN(options.zIndex))
            zIndex = options.zIndex;
    }

    function adjust()
    {
        imgOfs = { left: M.round($img.offset().left), top: M.round($img.offset().top) };
        imgWidth = $img.width();
        imgHeight = $img.height();

        if ($().jquery == '1.3.2' && $.browser.safari && position == 'fixed') {
            imgOfs.top += M.max(document.documentElement.scrollTop, $('body').scrollTop()); 
            imgOfs.left += M.max(document.documentElement.scrollLeft, $('body').scrollLeft());
        }

        parOfs = $.inArray($parent.css('position'), ['absolute', 'relative']) != -1 ? 
            { left: M.round($parent.offset().left) - $parent.scrollLeft(),
                top: M.round($parent.offset().top) - $parent.scrollTop() } :
            position == 'fixed' ?
                { left: $(document).scrollLeft(), top: $(document).scrollTop() } :
                { left: 0, top: 0 };

        left = viewX(0);
        top = viewY(0);
    }

    function update(resetKeyPress)
    {
        if (!shown) return;

        $box.css({
            left: viewX(selection.x1) + 'px', top: viewY(selection.y1) + 'px',
            width: selection.width + 'px', height: selection.height + 'px'
        });
        $area.add($border1).add($border2).css({
            left: '0px', top: '0px',
            width: M.max(selection.width - options.borderWidth * 2, 0) + 'px',
            height: M.max(selection.height - options.borderWidth * 2, 0) + 'px'
        });
        $border1.css({ borderStyle: 'solid', borderColor: options.borderColor1 });
        $border2.css({ borderStyle: 'dashed', borderColor: options.borderColor2 });
        $border1.add($border2).css({ opacity: options.borderOpacity });
        $outLeft.css({ left: left + 'px', top: top + 'px',
            width: selection.x1 + 'px', height: imgHeight + 'px' });
        $outTop.css({ left: left + selection.x1 + 'px', top: top + 'px',
            width: selection.width + 'px', height: selection.y1 + 'px' });
        $outRight.css({ left: left + selection.x2 + 'px', top: top + 'px',
            width: imgWidth - selection.x2 + 'px', height: imgHeight + 'px' });
        $outBottom.css({ left: left + selection.x1 + 'px', top: top + selection.y2 + 'px',
            width: selection.width + 'px', height: imgHeight - selection.y2 + 'px' });

        if (handles.length) {
            handles[1].css({ left: selection.width - handleWidth + 'px' });
            handles[2].css({ left: selection.width - handleWidth + 'px',
                top: selection.height - handleWidth + 'px' });
            handles[3].css({ top: selection.height - handleWidth + 'px' });

            if (handles.length == 8) {
                handles[4].css({ left: (selection.width - handleWidth) / 2 + 'px' });
                handles[5].css({ left: selection.width - handleWidth + 'px',
                    top: (selection.height - handleWidth) / 2 + 'px' });
                handles[6].css({ left: (selection.width - handleWidth) / 2 + 'px',
                    top: selection.height - handleWidth + 'px' });
                handles[7].css({ top: (selection.height - handleWidth) / 2 + 'px' });
            }
        }

        if (resetKeyPress !== false) {
            if ($.imgAreaSelect.keyPress != docKeyPress)
                $(document).unbind($.imgAreaSelect.keyPress,
                    $.imgAreaSelect.onKeyPress);

            if (options.keys)
                $(document).bind($.imgAreaSelect.keyPress,
                    $.imgAreaSelect.onKeyPress = docKeyPress);
        }

        if ($.browser.msie && options.borderWidth == 1 && options.borderOpacity < 1) {
            $border1.add($border2).css('margin', '0');
            setTimeout(function () { $border1.add($border2).css('margin', 'auto'); }, 0);
        }
    }

    function areaMouseMove(event)
    {
        if (!adjusted) {
            adjust();
            adjusted = true;

            $box.one('mouseout', function () { adjusted = false; });
        }

        x = selX(evX(event)) - selection.x1;
        y = selY(evY(event)) - selection.y1;

        resize = [ ];

        if (options.resizable) {
            if (y <= resizeMargin)
                resize[V] = 'n';
            else if (y >= selection.height - resizeMargin)
                resize[V] = 's';
            if (x <= resizeMargin)
                resize[H] = 'w';
            else if (x >= selection.width - resizeMargin)
                resize[H] = 'e';
        }

        $box.css('cursor', resize.length ? resize.join('') + '-resize' :
            options.movable ? 'move' : '');
        if ($areaOpera)
            $areaOpera.toggle();
    }

    function docMouseUp(event)
    {
        resize = [ ];

        $('body').css('cursor', '');

        if (options.autoHide || selection.width * selection.height == 0)
            $box.add($o).hide();

        options.onSelectEnd(img, trueSelection());

        $(document).unbind('mousemove', selectingMouseMove);
        $box.mousemove(areaMouseMove);
    }

    function areaMouseDown(event)
    {
        if (event.which != 1) return false;

        adjust();

        if (options.resizable && resize.length > 0) {
            $('body').css('cursor', resize.join('') + '-resize');

            x1 = viewX(selection[resize[H] == 'w' ? 'x2' : 'x1']);
            y1 = viewY(selection[resize[V] == 'n' ? 'y2' : 'y1']);

            $(document).mousemove(selectingMouseMove)
                .one('mouseup', docMouseUp);
            $box.unbind('mousemove', areaMouseMove);
        }
        else if (options.movable) {
            startX = left + selection.x1 - evX(event);
            startY = top + selection.y1 - evY(event);

            $box.unbind('mousemove', areaMouseMove);

            $(document).mousemove(movingMouseMove)
                .one('mouseup', function () {
                    options.onSelectEnd(img, trueSelection());

                    $(document).unbind('mousemove', movingMouseMove);
                    $box.mousemove(areaMouseMove);
                });
        }
        else
            $img.mousedown(event);

        return false;
    }

    function aspectRatioXY()
    {
        x2 = M.max(left, M.min(left + imgWidth,
            x1 + M.abs(y2 - y1) * aspectRatio * (x2 < x1 ? -1 : 1)));
        y2 = M.round(M.max(top, M.min(top + imgHeight,
            y1 + M.abs(x2 - x1) / aspectRatio * (y2 < y1 ? -1 : 1))));
        x2 = M.round(x2);
    }

    function aspectRatioYX()
    {
        y2 = M.max(top, M.min(top + imgHeight,
            y1 + M.abs(x2 - x1) / aspectRatio * (y2 < y1 ? -1 : 1)));
        x2 = M.round(M.max(left, M.min(left + imgWidth,
            x1 + M.abs(y2 - y1) * aspectRatio * (x2 < x1 ? -1 : 1))));
        y2 = M.round(y2);
    }

    function doResize()
    {
        if (options.minWidth && M.abs(x2 - x1) < options.minWidth) {
            x2 = x1 - options.minWidth * (x2 < x1 ? 1 : -1);

            if (x2 < left)
                x1 = left + options.minWidth;
            else if (x2 > left + imgWidth)
                x1 = left + imgWidth - options.minWidth;
        }

        if (options.minHeight && M.abs(y2 - y1) < options.minHeight) {
            y2 = y1 - options.minHeight * (y2 < y1 ? 1 : -1);

            if (y2 < top)
                y1 = top + options.minHeight;
            else if (y2 > top + imgHeight)
                y1 = top + imgHeight - options.minHeight;
        }

        x2 = M.max(left, M.min(x2, left + imgWidth));
        y2 = M.max(top, M.min(y2, top + imgHeight));

        if (aspectRatio)
            if (M.abs(x2 - x1) / aspectRatio > M.abs(y2 - y1))
                aspectRatioYX();
            else
                aspectRatioXY();

        if (options.maxWidth && M.abs(x2 - x1) > options.maxWidth) {
            x2 = x1 - options.maxWidth * (x2 < x1 ? 1 : -1);
            if (aspectRatio) aspectRatioYX();
        }

        if (options.maxHeight && M.abs(y2 - y1) > options.maxHeight) {
            y2 = y1 - options.maxHeight * (y2 < y1 ? 1 : -1);
            if (aspectRatio) aspectRatioXY();
        }

        selection = { x1: selX(M.min(x1, x2)), x2: selX(M.max(x1, x2)),
            y1: selY(M.min(y1, y2)), y2: selY(M.max(y1, y2)),
            width: M.abs(x2 - x1), height: M.abs(y2 - y1) };

        update();

        options.onSelectChange(img, trueSelection());
    }

    function selectingMouseMove(event)
    {
        x2 = !resize.length || resize[H] || aspectRatio ? evX(event) : viewX(selection.x2);
        y2 = !resize.length || resize[V] || aspectRatio ? evY(event) : viewY(selection.y2);

        doResize();

        return false;        
    }

    function doMove(newX1, newY1)
    {
        x2 = (x1 = newX1) + selection.width;
        y2 = (y1 = newY1) + selection.height;

        selection = $.extend(selection, { x1: selX(x1), y1: selY(y1),
            x2: selX(x2), y2: selY(y2) });

        update();

        options.onSelectChange(img, trueSelection());
    }

    function movingMouseMove(event)
    {
        x1 = M.max(left, M.min(startX + evX(event), left + imgWidth - selection.width));
        y1 = M.max(top, M.min(startY + evY(event), top + imgHeight - selection.height));

        doMove(x1, y1);

        event.preventDefault();     
        return false;
    }

    function startSelection(event)
    {
        adjust();

        x2 = x1;
        y2 = y1;       
        doResize();

        resize = [ ];

        $box.add($o.is(':visible') ? null : $o).show();
        shown = true;

        $(document).unbind('mouseup', cancelSelection)
            .mousemove(selectingMouseMove).one('mouseup', docMouseUp);
        $box.unbind('mousemove', areaMouseMove);

        options.onSelectStart(img, trueSelection());
    }

    function cancelSelection()
    {
        $(document).unbind('mousemove', startSelection);
        $box.add($o).hide();

        selection = { x1: 0, y1: 0, x2: 0, y2: 0, width: 0, height: 0 };

        options.onSelectChange(img, selection);
        options.onSelectEnd(img, selection);
    }

    function imgMouseDown(event)
    {
        if (event.which != 1) return false;

        adjust();
        startX = x1 = evX(event);
        startY = y1 = evY(event);

        $(document).one('mousemove', startSelection)
            .one('mouseup', cancelSelection);

        return false;
    }

    function parentScroll()
    {
        adjust();
        update(false);
        x1 = viewX(selection.x1); y1 = viewY(selection.y1);
        x2 = viewX(selection.x2); y2 = viewY(selection.y2);
    }

    function imgLoad()
    {
        imgLoaded = true;

        if (options.show) {
            shown = true;
            adjust();
            update();
            $box.add($o).show();
        }

        $box.add($o).css({ visibility: '' });
    }

    var docKeyPress = function(event) {
        var k = options.keys, d, t, key = event.keyCode || event.which;

        d = !isNaN(k.alt) && (event.altKey || event.originalEvent.altKey) ? k.alt :
            !isNaN(k.ctrl) && event.ctrlKey ? k.ctrl :
            !isNaN(k.shift) && event.shiftKey ? k.shift :
            !isNaN(k.arrows) ? k.arrows : 10;

        if (k.arrows == 'resize' || (k.shift == 'resize' && event.shiftKey) ||
            (k.ctrl == 'resize' && event.ctrlKey) ||
            (k.alt == 'resize' && (event.altKey || event.originalEvent.altKey)))
        {
            switch (key) {
            case 37:
                d = -d;
            case 39:
                t = M.max(x1, x2);
                x1 = M.min(x1, x2);
                x2 = M.max(t + d, x1);
                if (aspectRatio) aspectRatioYX();
                break;
            case 38:
                d = -d;
            case 40:
                t = M.max(y1, y2);
                y1 = M.min(y1, y2);
                y2 = M.max(t + d, y1);
                if (aspectRatio) aspectRatioXY();
                break;
            default:
                return;
            }

            doResize();
        }
        else {
            x1 = M.min(x1, x2);
            y1 = M.min(y1, y2);

            switch (key) {
            case 37:
                doMove(M.max(x1 - d, left), y1);
                break;
            case 38:
                doMove(x1, M.max(y1 - d, top));
                break;
            case 39:
                doMove(x1 + M.min(d, imgWidth - selX(x2)), y1);
                break;
            case 40:
                doMove(x1, y1 + M.min(d, imgHeight - selY(y2)));
                break;
            default:
                return;
            }
        }

        return false;
    };

    this.setOptions = function(newOptions)
    {
        if (newOptions.parent)
            ($parent = $(newOptions.parent)).append($box.add($o));

        adjust();
        getZIndex();

        if (newOptions.x1 != null) {
            selection = { x1: newOptions.x1, y1: newOptions.y1,
                x2: newOptions.x2, y2: newOptions.y2 };
            newOptions.show = !newOptions.hide;

            x1 = viewX(selection.x1); y1 = viewY(selection.y1);
            x2 = viewX(selection.x2); y2 = viewY(selection.y2);
            selection.width = x2 - x1;
            selection.height = y2 - y1;
        }

        if (newOptions.handles != null) {
            $handles.remove();
            $handles = $(handles = [ ]);

            i = newOptions.handles ? newOptions.handles == 'corners' ? 4 : 8 : 0;

            while (i--)
                $handles = $handles.add(handles[i] = $('<div />'));

            handleWidth = 4 + options.borderWidth;

            $handles.css({ position: 'absolute', borderWidth: options.borderWidth + 'px',
                borderStyle: 'solid', borderColor: options.borderColor1, 
                opacity: options.borderOpacity, backgroundColor: options.borderColor2,
                width: handleWidth + 'px', height: handleWidth + 'px',
                fontSize: '0px', zIndex: zIndex > 0 ? zIndex + 1 : '1' })
                .addClass(options.classPrefix + '-handle');

            handleWidth += options.borderWidth * 2;
        }

        update();

        options = $.extend(options, newOptions);

        if (options.imageWidth || options.imageHeight) {
            scaleX = (parseInt(options.imageWidth) || imgWidth) / imgWidth;
            scaleY = (parseInt(options.imageHeight) || imgHeight) / imgHeight;
        }

        if (newOptions.keys)
            options.keys = $.extend({ shift: 1, ctrl: 'resize' },
                newOptions.keys === true ? { } : newOptions.keys);

        $o.addClass(options.classPrefix + '-outer');
        $area.addClass(options.classPrefix + '-selection');
        $border1.addClass(options.classPrefix + '-border1');
        $border2.addClass(options.classPrefix + '-border2');

        $box.add($area).add($border1).add($border2).css({ borderWidth: options.borderWidth + 'px' });
        $area.css({ backgroundColor: options.selectionColor, opacity: options.selectionOpacity });       
        $border1.css({ borderStyle: 'solid', borderColor: options.borderColor1 });
        $border2.css({ borderStyle: 'dashed', borderColor: options.borderColor2 });
        $border1.add($border2).css({ opacity: options.borderOpacity });
        $o.css({ opacity: options.outerOpacity, backgroundColor: options.outerColor });

        $box.append($area.add($border1).add($border2).add($handles).add($areaOpera));

        if (newOptions.hide)
            $box.add($o).hide();
        else if (newOptions.show && imgLoaded) {
            shown = true;
            update();
            $box.add($o).show();
        }

        aspectRatio = options.aspectRatio && (d = options.aspectRatio.split(/:/)) ?
            d[0] / d[1] : null;

        if (aspectRatio)
            if (options.minWidth)
                options.minHeight = parseInt(options.minWidth / aspectRatio);
            else if (options.minHeight)
                options.minWidth = parseInt(options.minHeight * aspectRatio);

        if (options.disable || options.enable === false) {
            $box.unbind('mousemove', areaMouseMove).unbind('mousedown', areaMouseDown);
            $img.add($o).unbind('mousedown', imgMouseDown);
            $(window).unbind('resize', parentScroll);
            $img.add($img.parents()).unbind('scroll', parentScroll);
        }
        else if (options.enable || options.disable === false) {
            if (options.resizable || options.movable)
                $box.mousemove(areaMouseMove).mousedown(areaMouseDown);

            if (!options.persistent)
                $img.add($o).mousedown(imgMouseDown);
            $(window).resize(parentScroll);
            $img.add($img.parents()).scroll(parentScroll);
        }

        options.enable = options.disable = undefined;
    };

    if ($.browser.msie)
        $img.attr('unselectable', 'on');

    $.imgAreaSelect.keyPress = $.browser.msie ||
        $.browser.safari ? 'keydown' : 'keypress';

    if ($.browser.opera)
        ($areaOpera = $('<div style="width: 100%; height: 100%; position: absolute;" />'))
            .css({ zIndex: zIndex > 0 ? zIndex + 2 : '2' });

    this.setOptions(options = $.extend({
        borderColor1: '#000',
        borderColor2: '#fff',
        borderWidth: 1,
        borderOpacity: .5,
        classPrefix: 'imgareaselect',
        movable: true,
        resizable: true,
        selectionColor: '#fff',
        selectionOpacity: 0,
        outerColor: '#000',
        outerOpacity: .4,
        parent: 'body',
        onSelectStart: function () {},
        onSelectChange: function () {},
        onSelectEnd: function () {}
    }, options));

    $box.add($o).css({ visibility: 'hidden', position: position,
        overflow: 'hidden', zIndex: zIndex > 0 ? zIndex : '0' });
    $area.css({ borderStyle: 'solid' });
    $box.css({ position: position, zIndex: zIndex > 0 ? zIndex + 2 : '2' });
    $area.add($border1).add($border2).css({ position: 'absolute' });

    img.complete || img.readyState == 'complete' || !$img.is('img') ?
        imgLoad() : $img.one('load', imgLoad);
};

$.fn.imgAreaSelect = function (options) {
    options = options || {};

    this.each(function () {
        if ($(this).data('imgAreaSelect'))
            $(this).data('imgAreaSelect').setOptions(options);
        else {
            if (options.enable === undefined && options.disable === undefined)
                options.enable = true;

            $(this).data('imgAreaSelect', new $.imgAreaSelect.init(this, options));
        }
    });

    return this;
};

})(jQuery);
