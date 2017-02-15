function showRecos(data, textStatus,observationId) {
    if(!data) return;
    var recoSummaryWrap,seeMoreMessage;
    if(typeof observationId === 'undefined'){
        recoSummaryWrap = $('#recoSummary');
        seeMoreMessage = $("#seeMoreMessage");
    }else{
        recoSummaryWrap = $('.recoSummary_'+observationId);
        seeMoreMessage = $("#seeMoreMessage_"+observationId);
    }
    if(textStatus && textStatus == 'append')
        recoSummaryWrap.append(data.model.recoHtml);
    else
        recoSummaryWrap.html(data.model.recoHtml);
    var speciesName =  data.model.speciesName;
     $('.species_title_'+observationId).replaceWith(data.model.speciesNameTemplate);
     $('.page-header .species-page-link').hide();
     $('.species-external-link').replaceWith(data.model.speciesExternalLinkHtml);
    if($('#carousel_a').length > 0) {
        reloadCarousel($('#carousel_a').data('jcarousel'), 'speciesName', speciesName);
    }
    showUpdateStatus(data.msg, data.success?'success':'error',seeMoreMessage);
}

function lockObv(url, lockType, recoId, obvId, ele) {
    if($(ele).hasClass('disabled')) {
        alert(window.i8ln.observation.show.lock);
        event.preventDefault();
        return false;               
    }
   
    if($(ele).hasClass('LockedNow')) {
        $(ele).removeClass('LockedNow');
        lockType = $(ele).attr('rel');
    }else{
        $(ele).addClass('LockedNow');
    }

    $.ajax({
        url:url,
        dataType: "json",
        data:{"lockType" : lockType, "recoId" : recoId},
        success: function(data){
            seeMoreMessage = $("#seeMoreMessage_"+obvId);            
            if(lockType == "Validate"){
                //$("#addRecommendation").hide();
                $('.nameContainer_'+obvId+' input').attr('disabled', 'disabled');
                $('.iAgree_'+obvId+' button').addClass('disabled');
                $(".lockObvId_"+obvId).addClass('disabled');              
                $(ele).attr('rel','Unlock');
                $(ele).html('<i class="icon-lock"></i>Unlock').removeClass('disabled');
                showUpdateStatus(data.msg, 'success',seeMoreMessage);
            }
            else{
                //$("#addRecommendation").show();
                $('.nameContainer_'+obvId+' input').removeAttr('disabled');
                $('.iAgree_'+obvId+' button').removeClass('disabled');
                $(".lockObvId_"+obvId).removeClass('disabled');              
                $(ele).attr('rel','Validate');
                $(ele).html('<i class="icon-lock"></i>Validate').removeClass('disabled');
                showUpdateStatus(data.msg, 'success',seeMoreMessage);
            }
            updateFeeds();
        }
    });
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
        showUpdateStatus(data.success, 'success');
    },

    statusCode: {
        401: function() {
            show_login_dialog();
        }                                       
    },
    error: function(xhr, status, error) {
        // $(".deleteCommentIcon").tooltip('hide');
        var msg = $.parseJSON(xhr.responseText);
        showUpdateStatus(msg.error, 'error');
    }
    });
}

function addAgreeRecoVote(obvId, recoId, currentVotes, liComponent, url, obj){
    if($(obj).hasClass('disabled')) {
        event.preventDefault();
        return false;               
    }
    $.ajax({
        url: url,
    data:{'obvId':obvId, 'recoId':recoId, 'currentVotes':currentVotes},

    success: function(data){
        if(data.status == 'success' || data.success == true) {
            if(data.canMakeSpeciesCall === false){
                $('#selectedGroupList').modal('show');
            } else {
                preLoadRecos(3, 0, false, obvId, liComponent);
                updateFeeds();
                setFollowButton();
                showUpdateStatus(data.msg, data.success?'success':'error');
            }
        } else {
            showUpdateStatus(data.msg, data.success?'success':'error');
        }
        return false;
    },

    error:function (xhr, ajaxOptions, thrownError){
        //successHandler is used when ajax login succedes
        var successHandler = this.success, errorHandler = showUpdateStatus;
        handleError(xhr, ajaxOptions, thrownError, successHandler, errorHandler);
    } 
    });
}

function removeRecoVote(obvId, recoId, url, obj){
    if($(obj).hasClass('disabled')) {
        event.preventDefault();
        return false;               
    }
    $.ajax({
        url: url,
    data:{'obvId':obvId, 'recoId':recoId},

    success: function(data){
        if(data.status == 'success' || data.success == true) {
            preLoadRecos(3, 0, false, obvId);
            updateFeeds();
            setFollowButton();
            showUpdateStatus(data.msg, data.success?'success':'error');
        } else {
            showUpdateStatus(data.msg, data.success?'success':'error');
        }
        return false;
    },

    error:function (xhr, ajaxOptions, thrownError){
        //successHandler is used when ajax login suceedes
        var successHandler = this.success, errorHandler = showUpdateStatus;
        handleError(xhr, ajaxOptions, thrownError, successHandler, errorHandler);
    } 
    });

}

function preLoadRecos(max, offset, seeAllClicked,observationId) {
   var getRecommendationVotesURL,seeMoreMessage,seeMore;
   if(typeof observationId === 'undefined'){
       getRecommendationVotesURL = window.params.observation.getRecommendationVotesURL;
       seeMoreMessage = $("#seeMoreMessage");
       seeMore = $("#seeMore");
   }else{
        getRecommendationVotesURL = window.params.observation.getRecommendationVotesURL+'/'+observationId;
        seeMoreMessage = $("#seeMoreMessage_"+observationId);
        seeMore = $("#seeMore_"+observationId);
   }
    
    if(seeMoreMessage.hasClass('isLocked') && observationId != 'undefined'){
        showUpdateStatus('This species ID has been validated by a species curator and is locked!', 'success',seeMoreMessage);
    }else{
        seeMoreMessage.hide();
    }
    seeMore.hide();    
    $.ajax({
        /*url: window.params.observation.getRecommendationVotesURL,*/
        url:getRecommendationVotesURL,
        method: "POST",
        dataType: "json",
        data: {max:max , offset:offset},    
        success: function(data) {
            if(data.status == 'success' || data.success == true) {
                if(offset>0) {
                    showRecos(data, null,observationId);
                } else {
                    showRecos(data, null,observationId);
                }
                //$("#recoSummary").html(data.recoHtml);
                var uniqueVotes = parseInt(data.model.uniqueVotes);
                if(uniqueVotes > offset+max){
                    if(max >= 0){
                        seeMore.show();
                    }                    
                } else {
                    seeMore.hide();
                }
                showUpdateStatus(data.msg, data.success?'success':'error');
            } else {
                showUpdateStatus(data.msg, data.success?'success':'error');
            }
        }, error: function(xhr, status, error) {
            handleError(xhr, status, error, undefined, function() {
                var msg = $.parseJSON(xhr.responseText);
                showUpdateStatus(msg.msg, msg.success?'success':'error');
            });
        }
    });
}

function customFieldInlineEdit(comp, url, cfId, obvId){
    var finalComp = $(comp).parent().prev().children(".cfInlineEdit");
    var valComp = $(comp).parent().prev().children(".cfStaticVal");
    if($(comp).text() == 'Edit'){
        valComp.hide();
        finalComp.show();
        $(comp).text('Submit');
    }else{
        if(!cfValidation(finalComp)){
            return false;
        }
        var inputComp = $(finalComp).find("input");
        if(inputComp.attr("name") == undefined){
            inputComp = $(finalComp).find("select");
        }
        if(inputComp.attr("name") == undefined){
            inputComp = $(finalComp).find("textarea");
        }
        var value = inputComp.val();
        var data = new Object();
        data['fieldValue'] = value;
        data['cfId'] = cfId;
        data['obvId'] = obvId;
        
        $.ajax({
            url: url,
            data:data,
            success: function(data){
                valComp.text(data.model.fieldName);
                finalComp.hide();
                valComp.show();
                $(comp).text('Edit');
                 updateFeeds();
                return true;
            },
            error:function (xhr, ajaxOptions, thrownError){
                //successHandler is used when ajax login succedes
                var successHandler = this.success, errorHandler = undefined;
                handleError(xhr, ajaxOptions, thrownError, successHandler, function(){
                });
            }
        });
    }   
    return false;
}

function showObservationMapView(obvId, observedOn, mapLocationPicker) {
    var params = {filterProperty:'speciesName',limit:-1,id:obvId}
    //var mapLocationPicker = new $.fn.components.MapLocationPicker(document.getElementById("big_map_canvas"));
    refreshMarkers(params, window.params.observation.relatedObservationsUrl, function(data){
        google.load('visualization', '1', {packages: ['corechart', 'table'], callback:function(){
            drawVisualization(data.model.observations);
        }});
    }, mapLocationPicker);
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
    var d = []
    if(rows) {
        var ignoreDate = -19800000 // representing Thu Jan 01 1970 00:00:00 GMT+0530 (IST) in miliseconds
        for(var i=0; i<rows.length; i++) {
            var obvDate = new Date(rows[i].fromDate);
            if(obvDate.getTime() != ignoreDate){
                //data.addRow([obvDate, 1]);
                d.push([obvDate, 1]);
            }
        }
    }
    if(d.length >0) {
        var gD = Array.apply(null, new Array(12)).map(Number.prototype.valueOf,0);
        for(var i=0; i<d.length; i++) {
            gD[d[i][0].getMonth()] = gD[d[i][0].getMonth()] + d[i][1]
        }

        $("#temporalDist").sparkline(gD, {
            type: 'bar', 
            barWidth: 24,
            height:'108px',
            width:'300px',
            tooltipFormat: '{{offset:offset}} : {{value}} Observations',
            tooltipValueLookups: {
                'offset': {
                    0:'Jan',
                    1:'Feb',
                    2:'Mar',
                    3:'Apr',
                    4:'May',
                    5:'Jun',
                    6:'Jul',
                    7:'Aug',
                    8:'Sep',
                    9:'Oct',
                    10:'Nov',
                    11:'Dec'
                }
            }
        });

/*    var data = new google.visualization.DataTable();
    data.addColumn('date', 'Date');
    data.addColumn('number', 'Observation');
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
    var table = new google.visualization.Table(document.getElementById('table'));
          table.draw(view, null);

          var grouped_table = new google.visualization.Table(document.getElementById('grouped_table'));
          grouped_table.draw(view, null);
    }*/
    } else {
        $("#grouped_table").html('<div id="relatedObservationMsg_a" class="alert alert-info" style="">No observations</div>');
    }
}


