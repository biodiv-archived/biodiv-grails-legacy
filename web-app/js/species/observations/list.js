$(document).ready(function(){
    $('#selected_sort').tooltip({placement:'top'});
    $('button').tooltip();
    $('.dropdown-toggle').dropdown();

    $('#speciesNameFilter').button();
    if(window.params.speciesName == 'Unknown'){
        $("#speciesNameFilterButton").addClass('active');
        $("#speciesNameAllButton").removeClass('active');
    }else {
        $("#speciesNameFilterButton").removeClass('active');
        $("#speciesNameAllButton").addClass('active');
    }
    $("#speciesNameFilter").val(window.params.speciesName);

    $('#observationFlagFilter').button();
    if(window.params.isFlagged == 'true' ){
        $("#observationFlaggedButton").addClass('active');
        $("#observationWithNoFlagFilterButton").removeClass('active')
    }else{
        $("#observationFlaggedButton").removeClass('active');
        $("#observationWithNoFlagFilterButton").addClass('active')
    }


    $('#observationAllChecklistFilter').button();
    if(window.params.isChecklistOnly == 'true' ){
        $("#observationChecklistOnlyButton").addClass('active');
        $("#observationAllButton").removeClass('active')
    }else{
        $("#observationChecklistOnlyButton").removeClass('active');
        $("#observationAllButton").addClass('active')
    }

    $("#observationChecklistOnlyButton").click(function() {
        if($("#observationChecklistOnlyButton").hasClass('active')){
            return false;
        }
        $("#observationAllChecklistFilter").val('true')
        $("#observationAllButton").removeClass('active')
        $("#observationChecklistOnlyButton").addClass('active')

        updateGallery(undefined, window.params.queryParamsMax, window.params.offset, undefined, window.params.isGalleryUpdate);
    return false;
    });

    $("#observationAllButton").click(function() {
        if($("#observationAllButton").hasClass('active')){
            return false;
        }
        $("#observationAllChecklistFilter").val('false');
        $("#observationChecklistOnlyButton").removeClass('active');
        $("#observationAllButton").addClass('active');

        updateGallery(undefined, window.params.queryParamsMax, window.params.offset, undefined, window.params.isGalleryUpdate);
        return false;
    });

    $('#observationMediaFilter').button();
    if(window.params.isMediaFilter ==  undefined || window.params.isMediaFilter == 'true' ){
        $("#observationMediaAllFilterButton").removeClass('active');
        $("#observationMediaOnlyFilterButton").addClass('active');
    }else{
        $("#observationMediaAllFilterButton").addClass('active');
        $("#observationMediaOnlyFilterButton").removeClass('active');
    }
    
    $("#observationMediaAllFilterButton").click(function() {
        if($("#observationMediaAllFilterButton").hasClass('active')){
            return false;
        }
        $("#observationMediaFilter").val('false');
        $("#observationMediaOnlyFilterButton").removeClass('active');
        $("#observationMediaAllFilterButton").addClass('active');

        updateGallery(undefined, window.params.queryParamsMax, window.params.offset, undefined, window.params.isGalleryUpdate);
        return false;
    });


    $("#observationMediaOnlyFilterButton").click(function() {
        if($("#observationMediaOnlyFilterButton").hasClass('active')){
            return false;
        }
        $("#observationMediaFilter").val('true');
        $("#observationMediaAllFilterButton").removeClass('active');
        $("#observationMediaOnlyFilterButton").addClass('active');

        updateGallery(undefined, window.params.queryParamsMax, window.params.offset, undefined, window.params.isGalleryUpdate);
        return false;
    });

    $("#observationFlaggedButton").click(function() {
        if($("#observationFlaggedButton").hasClass('active')){
            return false;
        }
        $("#observationFlagFilter").val('true')
        $("#observationWithNoFlagFilterButton").removeClass('active')
        $("#observationFlaggedButton").addClass('active')

        updateGallery(undefined, window.params.queryParamsMax, window.params.offset, undefined, window.params.isGalleryUpdate);
    return false;
    });



    $("#observationWithNoFlagFilterButton").click(function() {
        if($("#observationWithNoFlagFilterButton").hasClass('active')){
            return false;
        }
        $("#observationFlagFilter").val('false');
        $("#observationFlaggedButton").removeClass('active');
        $("#observationWithNoFlagFilterButton").addClass('active');

        updateGallery(undefined, window.params.queryParamsMax, window.params.offset, undefined, window.params.isGalleryUpdate);
        return false;
    });



    $("#speciesNameAllButton").click(function() {
        if($("#speciesNameAllButton").hasClass('active')){
            return false;
        }
        $("#speciesNameFilter").val('All');
        $("#speciesNameFilterButton").removeClass('active');
        $("#speciesNameAllButton").addClass('active');

        updateGallery(undefined, window.params.queryParamsMax, window.params.offset, undefined, window.params.isGalleryUpdate);
        return false;
    });

    $("#speciesNameFilterButton").click(function() {
        if($("#speciesNameFilterButton").hasClass('active')){
            return false;
        }
        $("#speciesNameFilter").val('Unknown');
        $("#speciesNameFilterButton").addClass('active');
        $("#speciesNameAllButton").removeClass('active');

        updateGallery(undefined, window.params.queryParamsMax, window.params.offset, undefined, window.params.isGalleryUpdate);
        return false;
    });

    $('#speciesGroupFilter button').click(function(){
        if($(this).hasClass('active')){
            return false;
        }
        $('#speciesGroupFilter button.active').removeClass('active');
        $(this).addClass('active');

        updateGallery(undefined, window.params.queryParamsMax, window.params.offset, undefined, window.params.isGalleryUpdate);
        return false;
    });

    $('#habitatFilter button').click(function(){
        if($(this).hasClass('active')){
            return false;
        }
        $('#habitatFilter button.active').removeClass('active');
        $(this).addClass('active');
        updateGallery(undefined, window.params.queryParamsMax, window.params.offset, undefined, window.params.isGalleryUpdate);
        return false;
    });

    $('.sort_filter_label').click(function(){
        var caret = '<span class="caret"></span>'
        if(stringTrim(($(this).html())) == stringTrim($("#selected_sort").html().replace(caret, ''))){
            return true;
        }
    $('.sort_filter_label.active').removeClass('active');
    $(this).addClass('active');
    $("#selected_sort").html($(this).html() + caret);
    updateGallery(undefined, window.params.queryParamsMax, window.params.offset, undefined, window.params.isGalleryUpdate);
    return true;   
    });

    $(".paginateButtons a").click(function() {
    	$('.paginateButtons a.active').removeClass('active');
    	$(this).addClass('active');
        updateGallery($(this).attr('href'), window.params.queryParamsMax, undefined, undefined, false);
        return false;
    });
    
//    $("ul[name='tags']").tagit({select:true,  tagSource: window.params.tagsLink});
    $(".observation_story li.tagit-choice").live('click', function(){
        setActiveTag($(this).contents().first().text());
        updateGallery(undefined, window.params.queryParamsMax, window.params.offset, undefined, window.params.isGalleryUpdate);
        return false;
    });

//   $('#tc_tagcloud a').live('click', function(){
//   		setActiveTag($(this).contents().first().text());
//		updateGallery(undefined, window.params.queryParamsMax, window.params.offset, undefined, window.params.isGalleryUpdate);
//		return false;
//   });

    $("#removeTagFilter").live('click', function(){
        var oldActiveTag = $("li.tagit-choice.active");
        if(oldActiveTag){
            oldActiveTag.removeClass('active');
        }
        var oldActiveTag = $("#tc_tagcloud a.active");
        if(oldActiveTag){
            oldActiveTag.removeClass('active');
        }
        $("input#tag").val('');
        updateGallery(undefined, window.params.queryParamsMax, window.params.offset, undefined, window.params.isGalleryUpdate);
        return false;
    });

    $("#removeUserFilter").live('click', function(){
        updateGallery(undefined, window.params.queryParamsMax, window.params.offset, true, window.params.isGalleryUpdate);
        return false;
    });

    $("#removeObvFilter").live('click', function(){
        updateGallery(undefined, window.params.queryParamsMax, window.params.offset, false, window.params.isGalleryUpdate, true);
        return false;
    });

    $("#removeDateRange").live('click', function(){
        $("input[name='daterangepicker_start']").val("");
        $("input[name='daterangepicker_end']").val("");
        updateGallery(undefined, window.params.queryParamsMax, window.params.offset, false, window.params.isGalleryUpdate);
        return false;
    });

    $("#removeQueryFilter").live('click', function(){
        var removeParam = undefined;
        if($($(this).attr('data-target').replace('.','\\.')).length != 0)
        $($(this).attr('data-target').replace('.','\\.')).val('')
        else {
            $( "#searchTextField" ).val('');	
        }
    removeParam = $(this).attr('data-target').replace('#','');
    updateGallery(undefined, window.params.queryParamsMax, window.params.offset, undefined, window.params.isGalleryUpdate, undefined, undefined, undefined, removeParam);
    return false;
    });

    $('select[name="limit"]').live('change', function() {
        updateGallery(undefined, $(this).val(), window.params.offset, false, window.params.isGalleryUpdate);
    });

    var tmpTarget =  window.location.pathname + window.location.search;
    setActiveTag($('<a href="'+ tmpTarget +'"></a>').url().param()["tag"]);

    $('.list_view_bttn').live('click', function() {
        $('.grid_view').hide();
        $('.list_view').show();
        $(this).addClass('active');
        $('.grid_view_bttn').removeClass('active');
        $.cookie("listing", "list", {path    : '/'});
        adjustHeight();
    });

    $('.grid_view_bttn').live('click', function() {
        $('.grid_view').show();
        $('.list_view').hide();
        $(this).addClass('active');
        $('.list_view_bttn').removeClass('active');
        $.cookie("listing", "grid", {path    : '/'});
    });

    $('.loadMore').live('click', function() {
        $.autopager({

            autoLoad : false,
            // a selector that matches a element of next page link
            link : 'div.paginateButtons a.nextLink',

            // a selector that matches page contents
            content : '.mainContent',

            //insertBefore: 'div.checklist_list_main > .table > .table-footer', 
            appendTo : '.mainContentList',

            // a callback function to be triggered when loading start 
            start : function(current, next) {

                $(".loadMore .progress").show();
                $(".loadMore .buttonTitle").hide();
            },

            // a function to be executed when next page was loaded. 
            // "this" points to the element of loaded content.
            load : function(current, next) {
                $(".mainContent:last").hide().fadeIn(3000);

                $("div.paginateButtons a.nextLink").attr('href', next.url);
                if (next.url == undefined) {
                    $(".loadMore").hide();
                } else {
                    $(".loadMore .progress").hide();
                    $(".loadMore .buttonTitle").show();
                }
                if ($('.grid_view_bttn.active')[0]) {
                    $('.grid_view').show();
                    $('.list_view').hide();
                } else {
                    $('.grid_view').hide();
                    $('.list_view').show();
                }

                var a = $('<a href="'+current.url+'"></a>');
                var url = a.url();
                var params = url.param();
                delete params["append"]
                delete params["loadMore"]
                params['max'] = parseInt(params['offset'])+parseInt(params['max']);
                params['offset'] = 0
                var History = window.History;
                History.pushState({state:1}, "Species Portal", '?'+decodeURIComponent($.param(params))); 
                updateRelativeTime();
                last_actions();
                eatCookies();
                $('.observations_list_wrapper').trigger('updatedGallery');
            }
        });

        $.autopager('load');
        return false;
    });

    //	last_actions();
    eatCookies();
    $('.observations_list_wrapper').trigger('updatedGallery');

    $('#distinctRecoTableAction').click(function(){
        var $me = $(this);
        var target = window.location.pathname + window.location.search;
        var a = $('<a href="'+target+'"></a>');
        var url = a.url();
        var href = url.attr('path');
        var params = getFilterParameters(url);
        params['max'] = $(this).data('max');
        params['offset'] = $(this).data('offset');
        var $distinctRecoTable = $('#distinctRecoTable');
        $.ajax({
            url:window.params.observation.distinctRecoListUrl,
            dataType: "json",
            data:params,
            success: function(data) {
            	$('#distinctRecoList .distinctRecoHeading').html(data.totalRecoCount?(' (' + data.totalRecoCount + ')'):'');
                if(data.status === 'success') {
                	$.each(data.distinctRecoList, function(index, item) {
                        if(item[1])
                            $distinctRecoTable.append('<tr><td><i>'+item[0]+'</i></td><td>'+item[2]+'</td></tr>');  
                        else
                            $distinctRecoTable.append('<tr><td>'+item[0]+'</td><td>'+item[2]+'</td></tr>');
                    });
                	$me.data('offset', data.next);
                    if(data.totalRecoCount <= data.next){
                    	$me.hide();
                    }
                } else {
                	$me.hide();
                }
            }
        });
    });
    $('#distinctRecoTableAction').click();
});

if (typeof String.prototype.startsWith != 'function') {
    // see below for better implementation!
    String.prototype.startsWith = function (str){
        return this.indexOf(str) == 0;
    };
}

function eatCookies() {	
    var hashString = window.location.hash.substring(1)
        if ($.cookie("listing") == "list") {
            if(!hashString.startsWith('l')) {
                if(hashString.startsWith('g')) {
                    window.location.hash = "l"+hashString.substring(1);
                } else if(hashString){
                    window.location.hash = "l"+hashString;
                }
            }
            $('.list_view').show();
            $('.grid_view').hide();
            $('.grid_view_bttn').removeClass('active');
            $('.list_view_bttn').addClass('active');
        } else {
            if(!hashString.startsWith('g')) {
                if(hashString.startsWith('l')) {
                    window.location.hash = "g"+hashString.substring(1);
                } else if(hashString){
                    window.location.hash = "g"+hashString;
                }
            }
            $('.grid_view').show();
            $('.list_view').not('.single_list_view').hide();
            $('.grid_view_bttn').addClass('active');
            $('.list_view_bttn').removeClass('active');
        }
    adjustHeight();
}

function stringTrim(s){
    return $.trim(s);
}

function getSelectedGroup() {
    var grp = ''; 
    $('#speciesGroupFilter button').each (function() {
        if($(this).hasClass('active')) {
            grp += $(this).attr('value') + ',';
        }
    });

    grp = grp.replace(/\s*\,\s*$/,'');
    return grp;	
} 

function getSelectedHabitat() {
    var hbt = ''; 
    $('#habitatFilter button').each (function() {
        if($(this).hasClass('active')) {
            hbt += $(this).attr('value') + ',';
        }
    });
    hbt = hbt.replace(/\s*\,\s*$/,'');
    return hbt;	
} 

function getSelectedSortBy() {
    var sortBy = ''; 
    $('.sort_filter_label').each (function() {
        if($(this).hasClass('active')) {
            sortBy += $(this).attr('value') + ',';
        }
    });

    sortBy = sortBy.replace(/\s*\,\s*$/,'');
    return sortBy;	
} 

function getSelectedFlag() {
    var flag = ''; 
    flag = $("#observationFlagFilter").attr('value');
    if(flag) {
        flag = flag.replace(/\s*\,\s*$/,'');
        return flag;
    }	
}

function getSelectedAllChecklistFlag() {
    var flag = ''; 
    flag = $("#observationAllChecklistFilter").attr('value');
    if(flag) {
        flag = flag.replace(/\s*\,\s*$/,'');
        return flag;
    }	
}

function getSelectedMedia() {
    var media = ''; 
    media = $("#observationMediaFilter").attr('value');
    if(media) {
        media = media.replace(/\s*\,\s*$/,'');
        return media;
    }	
}

function getSelectedSpeciesName() {
    var sName = ''; 
    sName = $("#speciesNameFilter").attr('value');
    if(sName) {
        sName = sName.replace(/\s*\,\s*$/,'');
        return sName;
    }	
} 

function getSelectedTag() {
    var tag = $("li.tagit-choice.active").contents().first().text();
    if(!tag){
        tag = $("#tc_tagcloud a.active").contents().first().text();	
    }  
    if(tag) {
        tag = stringTrim(tag.replace(/\s*\,\s*$/,''));
        return tag;
    } else {
        return $("input#tag").val()
    }
} 

function getSelectedUserGroup() {
    return $('#advSearchForm input[name=uGroup]:radio:checked').val()
} 

function getFilterParameters(url, limit, offset, removeUser, removeObv, removeSort, isRegularSearch, removeParam) {
    var params = url.param();

    if(removeParam) {
        delete params[removeParam]
    }
    removeSort = (typeof removeSort === "undefined") ? false : removeSort;

    if(!removeSort) {
        var sortBy = getSelectedSortBy();
        if(sortBy) {
            params['sort'] = sortBy;
        }
    }

    var sName = getSelectedSpeciesName();
    if(sName) {
        params['speciesName'] = sName;
    }

    var flag = getSelectedFlag();
    if(flag) {
        params['isFlagged'] = flag;
    }

    var allChecklistFlag = getSelectedAllChecklistFlag();
    if(allChecklistFlag) {
        params['isChecklistOnly'] = allChecklistFlag;
    }

    //    var mediaFilter = getSelectedMedia();
    //    if(mediaFilter) {
    //            params['isMediaFilter'] = mediaFilter;
    //    }

    var grp = getSelectedGroup();
    if(grp) {
        params['sGroup'] = grp;
    }

    var habitat = getSelectedHabitat();
    if(habitat) {
        params['habitat'] = habitat;
    }


    var tag = getSelectedTag();
    if(tag){
        params['tag'] = tag;
    }else{
        // removing old tag from url
        if(params['tag'] != undefined){
            delete params['tag'];
        }
    }

    var query = $( "#searchTextField" ).val();
    if(query){
        params['query'] = query;
    }else{
        // removing old tag from url
        if(params['query'] != undefined){
            delete params['query'];
        }
    }

    if(!isRegularSearch) {
        $("#advSearchForm :input").each(function(index, ele) {
            var field = $(this).attr('name');
            var query = $( this ).val();
            if(query){
                params[field] = query;
            } else {
                // removing old tag from url
                if(params[field] != undefined){
                    delete params[field];
                }
            }
        });
        if((params['daterangepicker_start'] === new Date(0).toString('dd/MM/yyyy')) && (params['daterangepicker_end'] === Date.today().toString('dd/MM/yyyy'))){
            delete params['daterangepicker_start'];
            delete params['daterangepicker_end'];
        }
    }

    if($("#limit").length != 0) {
        params['max'] = $('select[name="limit"]').val();
    } else if(limit != undefined) {
        params['max'] = limit.toString();
    } 

    if(offset != undefined) {
        params['offset'] = offset.toString();
    }

    if(removeUser){
        if(params['user'] != undefined){
            delete params['user'];
        }
    }

    if(removeObv){
        if(params['observation'] != undefined){
            delete params['observation'];
        }
    }

    var uGroup = getSelectedUserGroup();
    if(uGroup) {
        params['uGroup'] = uGroup;
    } else {
        if(params['uGroup'] != undefined){
            delete params['uGroup'];
        }
    }

    var bounds = $("#bounds").val();
    if(bounds) {
        params['bounds'] = bounds;
    } else {
        delete params['bounds'];
    }

    var isMapView = $("#isMapView").val()
        if(isMapView) {
            params['isMapView'] = isMapView
        } else {
            delete params['isMapView']
        }
    return params;
}	

function setActiveTag(activeTag){
    if(activeTag != undefined){
        $('li.tagit-choice').each (function() {
            if(stringTrim($(this).contents().first().text()) == stringTrim(activeTag)) {
                $(this).addClass('active');
            }
            else{
                if($(this).hasClass('active')){
                    $(this).removeClass('active');
                }
            }
        });

        $('#tc_tagcloud a').each(function() {
            if(stringTrim($(this).contents().first().text()) == stringTrim(activeTag)) {
                $(this).addClass('active');
            }else{
                if($(this).hasClass('active')){
                    $(this).removeClass('active');
                }
            }
        });

    }
}

function updateListPage(activeTag) {
    return function (data) {
        $('.observations_list').replaceWith(data.obvListHtml);
        $('#info-message').replaceWith(data.obvFilterMsgHtml);
        $('#tags_section').replaceWith(data.tagsHtml);
        if(data.chartModel) {
            visualization_data = new google.visualization.DataTable();
            $.each(data.chartModel.columns, function(index, item) {
                visualization_data.addColumn( item[0], item[1]);
            });

            $.each(data.chartModel.data, function(index, item) {
                visualization_data.addRow(item);
            });
            visualization.draw(visualization_data, {legend: 'bottom'});
        }
        //$('.observation_location_wrapper').replaceWith(data.mapViewHtml);
        updateDistinctRecoTable();
        setActiveTag(activeTag);
        updateDownloadBox(data.instanceTotal);
        updateRelativeTime();
        last_actions();
        eatCookies();			
        $('.observations_list_wrapper').trigger('updatedGallery');
    }
}

function getUpdateGalleryParams(target, limit, offset, removeUser, isGalleryUpdate, removeObv, removeSort, isRegularSearch, removeParam) {
    if(target === undefined) {
        target = window.location.pathname + window.location.search;
    }

    var a = $('<a href="'+target+'"></a>');
    var url = a.url();
    var href = url.attr('path');
    var params = getFilterParameters(url, limit, offset, removeUser, removeObv, removeSort, isRegularSearch, removeParam);
    params['href'] = href;
    params['base'] = url.attr('base');
    return params;
}

function updateGallery(target, limit, offset, removeUser, isGalleryUpdate, removeObv, removeSort, isRegularSearch, removeParam) {
    
    var params = getUpdateGalleryParams(target, limit, offset, removeUser, isGalleryUpdate, removeObv, removeSort, isRegularSearch, removeParam);

    isGalleryUpdate = (isGalleryUpdate == undefined)?true:isGalleryUpdate
    if(isGalleryUpdate)
    	params["isGalleryUpdate"] = isGalleryUpdate;
    var href = params.href;
    var base = params.base;
    delete params["href"]
    delete params["base"]
    var recursiveDecoded = decodeURIComponent($.param(params));
    
    var doc_url = href+'?'+recursiveDecoded;
    var History = window.History;
    delete params["isGalleryUpdate"]
    History.pushState({state:1}, document.title, '?'+decodeURIComponent($.param(params))); 
    console.log("doc_url " + doc_url);
    if(isGalleryUpdate) {
        $.ajax({
            url: doc_url,
            dataType: 'json',

            beforeSend : function(){
                $('.observations_list').css({"opacity": 0.5});
                $('#tags_section').css({"opacity": 0.5});
            },
            success: updateListPage(params["tag"]),
            statusCode: {
                401: function() {
                    show_login_dialog();
                }	    				    			
            },
            error: function(xhr, status, error) {
                var msg = $.parseJSON(xhr.responseText);
                $('.message').html(msg);
            }
        });
        if(params.isMapView === "true" || params.bounds != undefined) {
            updateMapView(params);
        }
    } else {
        window.location = base+doc_url;
    }
}
 
var oldParams = {}
function updateMapView (params, callback) {
//    if($('#observations_list_map').is(':hidden')) {
//        $('#observations_list_map').slideToggle(mapViewSlideToggleHandler);
//    }
    var p = jQuery.extend({}, params);
    delete p.bounds;
    delete oldParams.bounds;
    //console.log(JSON.stringify(oldParams));
    //console.log(JSON.stringify(p));
    if(isMapViewLoaded !== true) {
        loadGoogleMapsAPI(function() {
            initialize(document.getElementById("big_map_canvas"), false);
            refreshMarkers(p);
            refreshMapBounds();
            oldParams = params;
	    $('#big_map_canvas').trigger('maploaded');
        })

    } else {
        //TODO:remove bounds before comparision
        //order of params is important for this test to pass
        if(JSON.stringify(oldParams) != JSON.stringify(p))
            refreshMarkers(p);
        refreshMapBounds();
        oldParams = params;
    }
}

function refreshMapBounds() {
    var bounds = $('#bounds').val();
    if(bounds) {
        bounds = bounds.split(',');
        if(bounds.length == 4) {
            map.fitBounds([
                    [bounds[0], bounds[1]],
                    [bounds[2], bounds[3]]
                    ]);
        }
    } else {
        resetMap();
    }
}

function showMapView() {
    updateMapView(getUpdateGalleryParams(undefined, undefined, 0, undefined, window.params.isGalleryUpdate));
}

function refreshList(bounds){
    if (bounds !== undefined){
        $("#bounds").val(bounds);
    } else {
        $("#bounds").val('');
    }
    updateGallery(undefined, undefined, 0, undefined, window.params.isGalleryUpdate);
}

function mapViewSlideToggleHandler() {
    if ($('#observations_list_map').is(':hidden')) {
        $('div.observations > div.observations_list').show();
        $('#map_view_bttn').css('background-color', 'transparent');
        $('#map_results_list > div.observations_list').remove();
        $("#isMapView").val("false");
        $('#bounds').val('');
    } else {
        $('div.observations > div.observations_list').hide();
        $('div.observations > div.observations_list').html('');
        $("#isMapView").val("true");
        showMapView();
    }
}

function updateDistinctRecoTable(){
	$('#distinctRecoTable tbody').empty();
	var me = $('#distinctRecoTableAction');
	$(me).show();
	$(me).data('offset', 0);
	$(me).click();
}

/*                        function getRandomNumber(){
                          return ((Math.random() -.5) / 200);
                          }

                          function jitterCloseMarker(big_map){
                          var zoomLevel = big_map.getZoom();
                          if(zoomLevel >= 13){
                          var markerKeys = [];
                          var mapBounds = big_map.getBounds()
                          for (var i = 0; i < markers.length; i++) {
                          var pos = markers[i].getPosition();
                          if(mapBounds.contains(pos)){
                          if($.inArray(pos.toString(), markerKeys) != -1){
                          markers[i].setPosition(new google.maps.LatLng(pos.lat() + getRandomNumber(), pos.lng() + getRandomNumber()));
                          }else{
                          markerKeys.push(pos.toString());
                          }
                          }
                          }
                          }
                          }  


                          google.maps.event.addListener(big_map, 'zoom_changed', function() {
                          jitterCloseMarker(big_map);

                          });
                          var markerCluster = new MarkerClusterer(big_map, markers, {gridSize: 30, maxZoom:13});
                          */                       

function load_content(params){
    var marker = this
    $.ajax({
        url: window.params.snippetUrl+"/"+params.id,
        success: function(data){
            marker.bindPopup("<div id='info-content' class='thumbnail'>" + data + "</div>").openPopup();;
        }
    });
}
/*

   google.maps.event.addListener(big_map, 'dragend', function() { checkBounds(); });

   function checkBounds() {
   if (allowedBounds.contains(big_map.getCenter())) return;

   var c = big_map.getCenter(),
   x = c.lng(),
   y = c.lat(),
   maxX = allowedBounds.getNorthEast().lng(),
   maxY = allowedBounds.getNorthEast().lat(),
   minX = allowedBounds.getSouthWest().lng(),
   minY = allowedBounds.getSouthWest().lat();

   if (x < minX) x = minX;
   if (x > maxX) x = maxX;
   if (y < minY) y = minY;
   if (y > maxY) y = maxY;

   big_map.setCenter(new google.maps.LatLng(y, x));
   }
   */

function speciesHabitatInterestHandler(event){
    if(!event.data.multiSelect) 
        $(event.data.button_group).find('button.active').removeClass('active');
    if($(this).hasClass('active')) {
        $(this).removeClass('active');
    } else {
        $(this).addClass('active');
    }
    return false;
}

function intializesSpeciesHabitatInterest(multiSelect){
    
    multiSelect = typeof multiSelect !== 'undefined' ? multiSelect : true;

    $('#speciesGroupFilter button').unbind('click');
    $('#speciesGroupFilter button').attr('data-toggle', 'buttons-checkbox').click({'button_group':$('#speciesGroupFilter'), 'multiSelect':multiSelect}, speciesHabitatInterestHandler);

    $('#habitatFilter button').unbind('click');
    $('#habitatFilter button').attr('data-toggle', 'buttons-checkbox').click({'button_group':$('#habitatFilter'), 'multiSelect':multiSelect}, speciesHabitatInterestHandler);
}

function updateDownloadBox(instanceTotal){
    if(instanceTotal > 0){
        $('#download-box').show();
    }else{
        $('#download-box').hide();
    }
}
	
//
//$(".observations_list .thumbnail").hover(function() {
//	
//	$("#postToUGroup").fadeToggle("fast");
//})/

$(document).ready(function(){
    $(".snippet.tablet .figure").hover(function() {
        $(this).children('.mouseover').toggle('slow')
    });

});
    
