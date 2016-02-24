import React from 'react';
import Well from './Well';

var PlateGrid = React.createClass({

    getInitialState: function() {
        return {
            data: undefined,
            selectedWellIds: [],
        };
    },

    componentDidMount: function() {
        var plateId = this.props.plateId,
            fieldIdx = this.props.fieldIdx;

        var url = "/webgateway/plate/" + plateId + "/" + fieldIdx + "/";
        $.ajax({
            url: url,
            dataType: 'json',
            cache: false,
            success: function(data) {
                if (this.isMounted()) {
                    var wellIds = this.getWellIdsFromUrlQuery(data);
                    this.setState({
                        data: data,
                        selectedWellIds: wellIds
                    });
                    if (wellIds.length > 0) {
                        OME.well_selection_changed(wellIds, this.props.fieldIdx);
                    }
                }
            }.bind(this)
        });

        // set up drag-select on <table> (empty at this point)
        var self = this;
        $(this.refs.table).selectable({
            filter: 'td.well',
            distance: 2,
            stop: function() {
                var wellIds = [];
                $(".ui-selected").each(function(){
                    var wellId = $(this).attr('data-wellid');
                    wellIds.push(parseInt(wellId, 10));
                });
                self.setState({selectedWellIds: wellIds});
                OME.well_selection_changed(wellIds, fieldIdx);
            }
        });
    },

    componentWillUnmount: function() {
        // cleanup plugin
        $(this.refs.table).selectable("destroy");
    },

    // Uses the url ?show=well-123 or image-123 to get well IDs from data
    getWellIdsFromUrlQuery: function(data) {
        var param = OME.getURLParameter('show'),
            wellIds = [];
        if (param) {
            param.split("|").forEach(function(p) {
                var wellId, imgId;
                if (p.split("-")[0] === "well") {
                    wellId = parseInt(p.split("-")[1], 10);
                } else if (p.split("-")[0] === "image") {
                    imgId = parseInt(p.split("-")[1], 10);
                }
                // Validate well Id is in this plate
                wellId = this.getWellId(data, wellId, imgId);
                if (wellId) {
                    wellIds.push(wellId);
                }
            }.bind(this));
        }
        return wellIds;
    },

    // Find well in data using wellId OR imageId, return wellId
    getWellId: function (data, wellId, imageId) {
        var wId;
        data.grid.forEach(function(row){
            row.forEach(function(well) {
                if (well && (well.id === imageId || well.wellId === wellId)) {
                    wId = well.wellId;
                }
            });
        });
        return wId;
    },

    handleWellClick: function(event, wellId) {
        // update selected state for range of wells etc...
        var isWellSelected = function(wellId) {
            return (this.state.selectedWellIds.indexOf(wellId) > -1);
        }.bind(this);

        var newSel = [];
        if (event.shiftKey) {
            // select range
            var wellIds = [],
                selectedIdxs = [];
            // make a list of all well IDs, and index of selected wells...
            this.state.data.grid.forEach(function(row){
                row.forEach(function(w){
                    if (w) {
                        wellIds.push(w.wellId);
                        if (isWellSelected(w.wellId)) {
                            selectedIdxs.push(wellIds.length-1);
                        }
                    }
                });
            });
            // extend the range of selected wells with index of clicked well...
            var clickedIdx = wellIds.indexOf(wellId),
                startIdx = Math.min(clickedIdx, selectedIdxs[0]),
                endIdx = Math.max(clickedIdx, selectedIdxs[selectedIdxs.length-1]);
            //...and select all wells within that range
            wellIds.forEach(function(wellId, idx){
                if (startIdx <= idx && idx <= endIdx) {
                    newSel.push(wellId);
                }
            });
        } else if (event.metaKey) {
            // toggle selection of well
            var found = false;
            // make a new list from old, removing clicked well
            this.state.selectedWellIds.forEach(function(id){
                if (wellId !== id) {
                    newSel.push(id);
                } else {
                    found = true;
                }
            });
            // if well wasn't already seleced, then select it
            if (!found) {
                newSel.push(wellId);
            }
        } else {
            // Select only this well
            newSel = [wellId];
        }
        this.setState({selectedWellIds: newSel});
        // Calls to ome.webclient.actions.js
        OME.well_selection_changed(newSel, this.props.fieldIdx);
    },

    render: function() {
        var data = this.state.data,
            iconSize = this.props.iconSize,
            placeholderStyle = {
                width: iconSize + 'px',
                height: iconSize + 'px',
            },
            selectedWellIds = this.state.selectedWellIds,
            handleWellClick = this.handleWellClick;
        if (!data) {
            return (<table ref="table" />)
        }
        var columnNames = data.collabels.map(function(l){
            return (<th key={l}>{l}</th>);
        });
        var grid = data.grid;
        var rows = data.rowlabels.map(function(r, rowIndex){
            var wells = data.collabels.map(function(c, colIndex){
                var well = grid[rowIndex][colIndex];
                if (well) {
                    var selected = selectedWellIds.indexOf(well.wellId) > -1;
                    return (
                        <Well
                            key={well.wellId}
                            id={well.wellId}
                            iid={well.id}
                            thumbUrl={well.thumb_url}
                            selected={selected}
                            iconSize={iconSize}
                            handleWellClick={handleWellClick}
                            row={r}
                            col={c} />
                    )
                } else {
                    return (
                        <td className="placeholder" key={r + "_" + c}>
                            <div style={placeholderStyle} />
                        </td>);
                }
            });
            return (
                <tr key={r}>
                    <th>{r}</th>
                    {wells}
                </tr>
            );
        });

        return (
            <table ref="table">
                <tbody>
                    <tr>
                        <th> </th>
                        {columnNames}
                    </tr>
                    {rows}
                </tbody>
            </table>
        );
    }
});

export default PlateGrid;
