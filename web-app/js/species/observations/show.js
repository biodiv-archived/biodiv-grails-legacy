function showRecos(data, textStatus) {
    if(!data) return;
    if(textStatus && textStatus == 'append')
        $('#recoSummary').append(data.recoHtml);
    else
        $('#recoSummary').html(data.recoHtml);
    var speciesName =  data.speciesName;
    $('.species_title').replaceWith(data.speciesNameTemplate);
    $('.page-header .species-page-link').hide();
    $('.species-external-link').replaceWith(data.speciesExternalLinkHtml);
    if($('#carousel_a').length > 0) {
        reloadCarousel($('#carousel_a').data('jcarousel'), 'speciesName', speciesName);
    }
    showRecoUpdateStatus(data.msg, data.status);
}

function showRecoUpdateStatus(msg, type) {
    if(!msg) return;

    if(type === 'info') {
        $("#seeMoreMessage").html(msg).show().removeClass().addClass('alert alert-info');
    } else if(type === 'success') {
        $("#seeMoreMessage").html(msg).show().removeClass().addClass('alert alert-success');
    } else if(type === 'error') {
        $("#seeMoreMessage").html(msg).show().removeClass().addClass('alert alert-error');
    } else {
        $("#seeMoreMessage").hide();
    }
}

function removeRecoComment(recoVoteId, commentDivId, url, commentComp){
    $.ajax({
        url: url,
    data:{"id":recoVoteId},

    success: function(data){
        if($(commentDivId + ' li').length > 1){
            commentComp.remove();
        }else{
            $(commentDivId).remove(); 
        }
        // $(".deleteCommentIcon").tooltip('hide');
        showRecoUpdateStatus(data.success, 'success');
    },

    statusCode: {
        401: function() {
            show_login_dialog();
        }	    				    			
    },
    error: function(xhr, status, error) {
        // $(".deleteCommentIcon").tooltip('hide');
        var msg = $.parseJSON(xhr.responseText);
        showRecoUpdateStatus(msg.error, 'error');
    }
    });
}

function addAgreeRecoVote(obvId, recoId, currentVotes, liComponent, url){
    $.ajax({
        url: url,
    data:{'obvId':obvId, 'recoId':recoId, 'currentVotes':currentVotes},

    success: function(data){
        if(data.status == 'success') {
            if(data.canMakeSpeciesCall === false){
                $('#selectedGroupList').modal('show');
            } else {
                preLoadRecos(3, 0, false, obvId, liComponent);
                updateFeeds();
                setFollowButton();
                showRecoUpdateStatus(data.msg, data.status);
            }
        } else {
            showRecoUpdateStatus(data.msg, data.status);
        }
        return false;
    },

    error:function (xhr, ajaxOptions, thrownError){
        //successHandler is used when ajax login succedes
        var successHandler = this.success, errorHandler = showRecoUpdateStatus;
        handleError(xhr, ajaxOptions, thrownError, successHandler, errorHandler);
    } 
    });
}

function removeRecoVote(obvId, recoId, url){
    $.ajax({
        url: url,
    data:{'obvId':obvId, 'recoId':recoId},

    success: function(data){
        if(data.status == 'success') {
            preLoadRecos(3, 0, false, obvId);
            updateFeeds();
            setFollowButton();
            showRecoUpdateStatus(data.msg, data.status);
        } else {
            showRecoUpdateStatus(data.msg, data.status);
        }
        return false;
    },

    error:function (xhr, ajaxOptions, thrownError){
        //successHandler is used when ajax login suceedes
        var successHandler = this.success, errorHandler = showRecoUpdateStatus;
        handleError(xhr, ajaxOptions, thrownError, successHandler, errorHandler);
    } 
    });

}

function preLoadRecos(max, offset, seeAllClicked) {
    $("#seeMoreMessage").hide();
    $("#seeMore").hide();

    $.ajax({
        url: window.params.observation.getRecommendationVotesURL,
        method: "POST",
        dataType: "json",
        data: {max:max , offset:offset},	
        success: function(data) {
            if(data.status == 'success') {
                if(offset>0)
        showRecos(data, 'append');
                else
        showRecos(data, null);
    //$("#recoSummary").html(data.recoHtml);
    var uniqueVotes = parseInt(data.uniqueVotes);
    if(uniqueVotes > offset+max){
        $("#seeMore").show();
    } else {
        $("#seeMore").hide();
    }
    showRecoUpdateStatus(data.msg, data.status);
            } else {
                showRecoUpdateStatus(data.msg, data.status);
            }
        }, error: function(xhr, status, error) {
            handleError(xhr, status, error, undefined, function() {
                var msg = $.parseJSON(xhr.responseText);
                showRecoUpdateStatus(msg.msg, msg.status);
            });
        }
    });
}

function showObservationMapView(obvId) {
    var params = {filterProperty:'speciesName',limit:-1,id:obvId}
    refreshMarkers(params, window.params.observation.relatedObservationsUrl, function(data){
        google.load('visualization', '1', {packages: ['corechart', 'table'], callback:function(){
            drawVisualization(data.observations);
        }});
    });
    $('#big_map_canvas').trigger('maploaded');
}

var months = [ "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" ]; // Store month names in array
function getMonth(someDate) {
    return someDate.getMonth();
}

function getMonthName(monthIndex) {
    return months[monthIndex];
}

function drawVisualization(rows) {
    var data = new google.visualization.DataTable();
    data.addColumn('date', 'Date');
    data.addColumn('number', 'Observation');
    if(rows) {
        for(var i=0; i<rows.length; i++) {
            data.addRow([new Date(rows[i].observedOn), 1]);
        }
    }
    if(data.getNumberOfRows() > 0) {
    var grouped_dt = google.visualization.data.group (
            data, [{column:0, modifier:getMonth, type:'number', label:'MonthNo'}],
            [{'column': 1, 'aggregation': google.visualization.data.sum, type: 'number', label:'#Observations'}, {'column': 0, 'aggregation': getMonthName, type: 'string', label:'Month'}]
            );

    //sorting on month number and replacing month number with string
    var noOfRows = grouped_dt.getNumberOfRows();
    var m = new Array(12);
    for(var i=0; i<noOfRows; i++) {
        m[grouped_dt.getValue(i,0)] = true;
    }

    for (var i=0;i<12;i++){
        if(m[i] == false || m[i] == undefined)
            grouped_dt.addRows([[i,0,months[i]]]);
    }
    
    //setting month name explicitly
    for (var i=0;i<12;i++){
    	grouped_dt.setValue(i, 2, months[grouped_dt.getValue(i, 0)]);
    }
    
    grouped_dt.sort([{column:0}]);

    var view = new google.visualization.DataView(grouped_dt);
    view.setColumns([2,1]);

    var columnChart = new google.visualization.ColumnChart(
            document.getElementById('temporalDist'));

    columnChart.draw(view,  {
        title:"No of observations by month",
        hAxis: {title: 'Month', slantedText:true, showTextEvery:1},
        vAxis:{minValue:0, maxValue:5, format: '#'},
        legend:{position: 'none'},
        chartArea:{width:'80%'}
    });
    /*    var table = new google.visualization.Table(document.getElementById('table'));
          table.draw(view, null);

          var grouped_table = new google.visualization.Table(document.getElementById('grouped_table'));
          grouped_table.draw(view, null);
          */
    } else {
        $("#grouped_table").html('<div id="relatedObservationMsg_a" class="alert alert-info" style="">No observations</div>');
    }
}


