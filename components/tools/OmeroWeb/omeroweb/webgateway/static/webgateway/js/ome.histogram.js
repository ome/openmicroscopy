
(function() {

if (!window.OME) {
    window.OME = {};
}

window.OME.Histogram = function(element, webgatewayUrl, graphWidth, graphHeight) {

    graphWidth = graphWidth || 256;
    graphHeight = graphHeight || 150;
    var colCount = 256,
        currentImageId,
        currentZ,
        currentT,
        currentC;

    // 1px margin to right so slider marker not lost
    var svg = d3.select(element).append("svg")
        .attr("width", graphWidth + 1)
        .attr("height", graphHeight)
        .append("g");

    // line plot
    svg.append("g")
        .append("path")
        .attr("class", "line");

    // area fill
    svg.append("path")
        .attr("class", "area")
        .attr('opacity', 0.5);

    // Add slider markers
    svg.selectAll("rect")
        .data([0, 0])
        .enter().append("rect")
        .attr("y", 0)
        .attr("height", 300)
        .attr("width", 1)
        .attr("x", function(d, i) { return d * (graphWidth/2); });


    var plotJson = function(data, color) {

        // cache this for use by chartRange
        colCount = data.length;

        var x = d3.scale.linear()
            .domain([0, data.length - 1])
            .range([0, graphWidth]);

        var y = d3.scale.linear()
            .domain([
                d3.min(data),
                d3.max(data)
            ])
            .range([graphHeight, 0]);

        // line
        var line = d3.svg.line()
            .x(function(d, i) { return x(i); })
            .y(function(d, i) { return y(d); });
        svg.selectAll(".line")
            .datum(data)
            .attr("d", line)
            .attr('stroke', color);

        // area to fill under line
        var area = d3.svg.area()
            .x(function(d, i) { return x(i); })
            .y0(graphHeight)
            .y1(function(d) { return y(d); });
        svg.selectAll(".area")
            .datum(data)
            .attr("class", "area")
            .attr("d", area)
            .attr('fill', color);
    };

    var _loadAndPlot = function(imageId, theZ, theC, theT, color, window, proj){
        // window is {'min':0, 'max': 1000, 'start': 50, 'end': 250}
        color = color || "#000000";
        if (color[1] !== "#") {
            color = "#" + color;
        }
        proj = (proj === "intmax" || proj === "intmean") ? proj : false;

        this.plotStartEnd(window, color);
        // If we are already showing the required data
        if (currentImageId == imageId && theZ === currentZ && theC === currentC && theT === currentT) {
            // just upate color
            svg.selectAll(".area").attr('fill', color);
            svg.selectAll(".line").attr('stroke', color);
            return;
        }
        currentImageId = imageId;
        currentZ = theZ;
        currentC = theC;
        currentT = theT;

        var url = webgatewayUrl + 'histogram_json/' + imageId + "/channel/" + theC + "/";
        url += '?theT=' + theT + '&theZ=' + theZ;
        if (proj) {
            url += "&p=" + proj;
        }
        $.getJSON(url, function(data){
            plotJson(data, color);
            // this.plotStartEnd(window, color);
        });
    };

    // Don't want to rapidly re-load data...
    this.loadAndPlot = _.debounce(_loadAndPlot);

    this.plotStartEnd = function(window, color) {
        var start = window.start,
            end = window.end,
            min = window.min,
            max = window.max;
        var s = ((start - min)/(max - min)) * colCount;
        var e = ((end - min)/(max - min)) * colCount;

        svg.selectAll("rect")
        .data([s, e])
        .attr("x", function(d, i) { return d * (graphWidth/colCount); })
        .attr('fill', color);
    };
};

// Helper for creating a Histogram and binding it to change events from viewport
window.OME.createViewportHistogram = function(viewport, chartSelector, checkboxSelector, webgatewayUrl) {
    var histogram;
    var currChIdx = 0;
    var plotHistogram = function(opts) {
        if (!histogram) return;  // not shown/created yet
        opts = opts || {};
        var chIdx = opts.chIdx !== undefined ? opts.chIdx : currChIdx;
        currChIdx = chIdx;
        var img = viewport.loadedImg;
        var ch = img.channels[chIdx];
        var color = ch.color === 'FFFFFF' ? '000000' : ch.color;
        var curr = img.current;
        var theZ = opts.theZ !== undefined ? opts.theZ : curr.z;
        var theT = opts.theT !== undefined ? opts.theT : curr.t;
        var wndw = ch.window;
        if (opts.start !== undefined && opts.end !== undefined) {
            wndw = {'min': ch.window.min,
                    'max': ch.window.max,
                    'start': opts.start,
                    'end': opts.end};
        }
        histogram.loadAndPlot(img.id, theZ, chIdx, theT, color, wndw);
    };
    $(checkboxSelector).click(function(){
        var show = this.checked;
        if (show) {
            var plotWidth = $("#histogram").show().width();
            // since we don't support resizing of histogram, let's also fix width of container
            $("#histogram").css('width', plotWidth + 'px');
            if (!histogram) {
                histogram = new OME.Histogram(chartSelector, webgatewayUrl, plotWidth, 125);
                plotHistogram();
            }
        } else {
            $("#histogram").hide();
        }
    });

    // Will get lots of channelChange events on Copy/Paste/Reset etc
    // We don't change selected channel of histogram - simply refresh.
    // NB: debounce on histogram.loadAndPlot() prevents multiple loads
    viewport.bind('channelChange', function(){
        plotHistogram();
    });

    viewport.bind('channelToggle', function(event, viewport, chIdx, channel){
        // If channel has been turned off, find another channel
        if (!channel.active) {
            var active = viewport.loadedImg.channels.reduce(function(prev, ch, idx){
                if (ch.active) prev.push(idx);
                return prev;
            }, []);
            if (active.length > 0) {
                chIdx = active[0];
            }
        }
        plotHistogram({'chIdx': chIdx});
    });

    viewport.bind('channelSlide', function(event, viewport, chIdx, start, end){
        plotHistogram({'chIdx': chIdx, 'start': start, 'end': end});
    });

    viewport.bind('channelFocus', function(event, viewport, chIdx){
        plotHistogram({'chIdx': chIdx});
    });

    viewport.zslider.bind('change', function (e,pos) {
        plotHistogram({'theZ': pos-1});
    });

    viewport.tslider.bind('change', function (e,pos) {
        plotHistogram({'theT': pos-1});
    });
};

})();