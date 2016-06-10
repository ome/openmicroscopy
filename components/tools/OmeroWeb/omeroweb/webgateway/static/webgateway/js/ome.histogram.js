
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

    var svg = d3.select(element).append("svg")
        .attr("width", graphWidth)
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
    // with labels to show position
    var t = svg.selectAll("text")
        .data([0, 0])
        .enter().append("text")
        .attr("font-family", "sans-serif")
        .attr("font-size", "20px")
        .attr("y", 20)
        .attr("fill", "black");


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

    this.loadAndPlot = function(imageId, theZ, theC, theT, color, window, proj){
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

    this.plotStartEnd = function(window, color) {
        var start = window.start,
            end = window.end,
            min = window.min,
            max = window.max;
        var s = ((start - min)/(max - min)) * 256;
        var e = ((end - min)/(max - min)) * 256;
        console.log(window, s, e, graphWidth, colCount);

        svg.selectAll("rect")
        .data([s, e])
        .attr("x", function(d, i) { return d * (graphWidth/colCount); })
        .attr('fill', color);

        svg.selectAll("text")
            .data([[start, s], [end, e]])
            .text(function(d) { return "" + d[0]; })
            .attr('x', function(d) { return (d[1] * (graphWidth/colCount)) + 3; });
    };
};

})();