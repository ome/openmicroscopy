
            $("#free_space").text(FREE_SPACE.filesizeformat());

            var progressbar = $( "#progressbar" ),
                progressLabel = $( ".progress-label" );

            progressbar.progressbar({
              value: 1,
              change: function() {
                progressLabel.text( progressbar.progressbar( "value" ) + "%" );
              },
            });

            var fillTable = function(chart_data) {

                var tableRows = [],
                    rowData, rowId;
                for(i=0; i<chart_data.length; i++) {
                    rowData = chart_data[i];
                    rowId = typeof rowData.userId != 'undefined' ? rowData.userId : rowData.groupId;
                    tableRows.push('<tr><td class="link">' + rowData.label + '(id:'+ rowId +')</td>');
                    tableRows.push('<td class="link">' + rowData.data.filesizeformat() + '</td></tr>');
                }
                
                $('#drivespaceTable tbody').html(tableRows.join(""));
                $('#drivespaceTable').show();
            };

            var plotJson = function(jsonUrl, options) {

                // show 'loading...'
                $("#status").html('loading...');
                $("#progress").show();

                $.getJSON(jsonUrl, function(data) {

                    // hide 'loading...'
                    $("#status").html('');
                    $("#progress").hide();

                    // save the data to use for chart click handling etc.
                    $("#placeholder").data('chart_data', data);

                    fillTable(data);

                    // bin all data > 11 users/groups
                    var total = 0,
                        chart_data = [],
                        MAX_SLICES = 10;

                    for(i=0; i<data.length; i++) {
                        var slice = data[i];
                        if(i === MAX_SLICES){
                            chart_data.push({label:'Others', data:slice.data});
                        } else if (i > MAX_SLICES) {
                            chart_data[MAX_SLICES].data = chart_data[MAX_SLICES].data + slice.data;
                        } else {
                            chart_data.push(slice);
                        }
                        total += slice.data;
                    }

                    $('#total').text(total.filesizeformat());
                    var usagePercent = 100 * total/(total + FREE_SPACE);
                    progressbar.progressbar( "value", parseInt(usagePercent, 10));

                    $("#placeholder").css('width',700).css('height',300);
                    $.plot($("#placeholder"), chart_data,
                    {
                        series: {
                            pie: {
                                show: true,
                                radius: 1,
                                label: {
                                    show: true,
                                    radius: 0.9,
                                    formatter: function(label, series){
                                        return '<div class="pieLabel">'+Math.round(series.percent)+'%</div>';
                                    },
                                    background: { opacity: 0 }
                                }
                            }
                        },
                        legend: {
                            show: true
                        },
                        grid: {
                            hoverable: true,
                            clickable: true
                        },
                    });

                    if (options && options.success) {
                        options.success();
                    }
                });
            };
