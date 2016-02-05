import React from 'react';

var IconTableHeader = React.createClass({

    handleLayoutClick: function(event) {
        var layout = event.target.getAttribute('data-layout');
        this.props.setLayout(layout);
    },

    handleFilterChange: function(event) {
        var filterText = event.target.value;
        this.props.setFilterText(filterText);
    },

    render: function() {
        var layout = this.props.layout,
            filterText = this.props.filterText;
        var iconBtnClass = layout === "icon" ? "checked" : "",
            tableBtnClass = layout === "table" ? "checked" : "";
        return (
            <div className="toolbar iconTableHeader">
                <div id="layout_chooser">
                    <button
                        onClick={this.handleLayoutClick}
                        id="icon_layout"
                        title="View as Thumbnails"
                        data-layout="icon"
                        className={iconBtnClass} />
                    <button
                        onClick={this.handleLayoutClick}
                        id="table_layout"
                        title="View as List"
                        data-layout="table"
                        className={tableBtnClass} />
                </div>
                <form className="search filtersearch" id="filtersearch" action="#" style={{top: 4}}>
                    <div>
                        <input
                            id="id_search"
                            type="text"
                            placeholder="Filter Images"
                            onKeyUp={this.handleFilterChange}
                            size={25} />
                    </div>
                    <span className="loading" style={{display: 'none'}} />
                </form>
            </div>
        );
    }
});

export default IconTableHeader
