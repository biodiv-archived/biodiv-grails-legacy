    var handlePaginateButtons = function() {
    	$('.paginateButtons a.active').removeClass('active');
    	$(this).addClass('active');
        updateGallery($(this).attr('href'), window.params.queryParamsMax, undefined, undefined, window.params.isGalleryUpdate);
        return false;
    };
 
$(document).ready(function(){
    $('#selected_sort').tooltip({placement:'top'});
    $('button').tooltip();
    $('.dropdown-toggle').dropdown();

    console.log('updated gallery trigger');
    $('.list').trigger('updatedGallery');

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

    $(".paginateButtons a").off('click').on('click', handlePaginateButtons);
   
//    $("ul[name='tags']").tagit({select:true,  tagSource: window.params.tagsLink});
    $(".observation_story li.tagit-choice").on('click', function(){
        setActiveTag($(this).contents().first().text());
        updateGallery(undefined, window.params.queryParamsMax, window.params.offset, undefined, window.params.isGalleryUpdate);
        return false;
    });

//   $('#tc_tagcloud a').on('click', function(){
//   		setActiveTag($(this).contents().first().text());
//		updateGallery(undefined, window.params.queryParamsMax, window.params.offset, undefined, window.params.isGalleryUpdate);
//		return false;
//   });

    /*$(".resource_in_groups li:has('.featured')").popover({ 
                    trigger:(is_touch_device ? "click" : "hover"),
                });*/
    $("#removeTagFilter").on('click', function(){
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

    $("#removeUserFilter").on('click', function(){
        updateGallery(undefined, window.params.queryParamsMax, window.params.offset, true, window.params.isGalleryUpdate);
        return false;
    });
    $('#action-tabs a').click(function (e) {
        var tab = $(this);
        if(tab.parent('li').hasClass('active')){
            window.setTimeout(function(){
                $("#action-tab-content .tab-pane").removeClass('active');
                tab.parent('li').removeClass('active');
                },1);
        }
    });

    $("#removeObvFilter").on('click', function(){
        updateGallery(undefined, window.params.queryParamsMax, window.params.offset, false, window.params.isGalleryUpdate, true);
        return false;
    });

    $("#removeDateRange").on('click', function(){
        $("input[name='daterangepicker_start']").val("");
        $("input[name='daterangepicker_end']").val("");
        updateGallery(undefined, window.params.queryParamsMax, window.params.offset, false, window.params.isGalleryUpdate);
        return false;
    });

    $(".removeQueryFilter").on('click', function(){
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

    $('select[name="limit"]').on('change', function() {
        updateGallery(undefined, $(this).val(), window.params.offset, false, window.params.isGalleryUpdate);
    });

    var tmpTarget =  window.location.pathname + window.location.search;
    setActiveTag($('<a href="'+ tmpTarget +'"></a>').url().param()["tag"]);

/*    $('.list_view_bttn').on('click', function() {
        $('.grid_view').hide();
        $('.list_view').show();
        $(this).addClass('active');
        $('.grid_view_bttn').removeClass('active');
        $.cookie("listing", "list", {path    : '/'});
        adjustHeight();
    });

    $('.grid_view_bttn').on('click', function() {
        $('.grid_view').show();
        $('.list_view').hide();
        $(this).addClass('active');
        $('.list_view_bttn').removeClass('active');
        $.cookie("listing", "grid", {path    : '/'});
    });
*/    
   

    $('.observation').on("click", ".loadMore", function() {
        console.log('loadmore');
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
/*                if ($('.grid_view_bttn.active')[0]) {
                    $('.grid_view').show();
                    $('.list_view').hide();
                } else {
                    $('.grid_view').hide();
                    $('.list_view').show();
                }
*/
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
                //$('.list').trigger('updatedGallery');
            }
        });

        $.autopager('load');
        return false;
    });

    //	last_actions();
    eatCookies();
});

/**
 */
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
/*            $('.list_view').show();
            $('.grid_view').hide();
            $('.grid_view_bttn').removeClass('active');
            $('.list_view_bttn').addClass('active');
*/        } else {
            if(!hashString.startsWith('g')) {
                if(hashString.startsWith('l')) {
                    window.location.hash = "g"+hashString.substring(1);
                } else if(hashString){
                    window.location.hash = "g"+hashString;
                }
            }
/*            $('.grid_view').show();
            $('.list_view').not('.single_list_view').hide();
            $('.grid_view_bttn').addClass('active');
            $('.list_view_bttn').removeClass('active');
*/        }
    adjustHeight();
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

function selectTickUserGroupsSignature(parentGroupId) {
    $(".userGroups button").click(function(e){
        var ug = this;
        if($(this).hasClass('active')) {
            //trying to unselect group
            //if on obv create page	and one group is coming as parent group		
            if($(ug).closest(".userGroupsClass").hasClass('create') && ($(ug).closest(".userGroupsClass button.create").length > 0)){
                //this group is parent group
                if($(this).hasClass('create') && parentGroupId != '' && $(this).hasClass("'"+parentGroupId+"'")){
                    alert("Can't unselect parent group");
                }else{
                    //un selecting other group
                    $(this).removeClass('btn-success');
                    $(this).find(".icon-ok").removeClass("icon-black").addClass("icon-white");
                }	
            }else{
                $(this).removeClass('btn-success');
                $(this).find(".icon-ok").removeClass("icon-black").addClass("icon-white");
                if($(this).hasClass("single-post")) {
                    $(ug).closest(".userGroupsClass").find(".groupsWithSharingNotAllowed button.single-post").removeClass('disabled')
                    $(ug).closest(".userGroupsClass").find(".groupsWithSharingAllowed button.multi-post").removeClass('disabled')
                } else {
                    if($(ug).closest(".userGroupsClass").find(".groupsWithSharingAllowed button.active").length == 0) {
                        $(ug).closest(".userGroupsClass").find(".groupsWithSharingAllowed button.multi-post").removeClass('disabled')
                    }
                }
            }
        } else {
            //trying to select new group
            //if on obv create page and one group is coming as parent group
            if($(ug).closest(".userGroupsClass").hasClass('create') && ($(ug).closest(".userGroups button.create").length > 0)){
                //either current one belongs to exclusive group or parent group is exclusive group
                if($(this).hasClass("single-post") ||($(ug).closest(".userGroupsClass").find(".groupsWithSharingNotAllowed button.create").length > 0)){
                    alert("Can't select this group because it will unselect parent group");
                }else{
                    //parent group is multipost one and this new group is also belong to multi select so selecting it
                    $(this).removeClass('disabled').addClass('btn-success');
                    $(this).find(".icon-ok").removeClass("icon-white").addClass("icon-black");
                }
            }else{
                //on obv edit page
                if($(this).hasClass("single-post")) {
                    $(ug).closest(".userGroupsClass").find(".groupsWithSharingAllowed button.multi-post").addClass('disabled').removeClass('active btn-success').find(".icon-ok").removeClass("icon-black").addClass("icon-white");
                    $(ug).closest(".userGroupsClass").find(".groupsWithSharingNotAllowed button.single-post").addClass('disabled').removeClass('active btn-success').find(".icon-ok").removeClass("icon-black").addClass("icon-white");
                    $(this).removeClass('disabled').addClass('btn-success');
                } else {
                    $(ug).closest(".userGroupsClass").find(".groupsWithSharingNotAllowed button.single-post").addClass('disabled').removeClass('active btn-success').find(".icon-ok").removeClass("icon-black").addClass("icon-white");
                    $(this).removeClass('disabled').addClass('btn-success');
                }
                $(this).find(".icon-ok").removeClass("icon-white").addClass("icon-black");
            }
        }
        e.preventDefault();
    });
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

function getSelectedFilters($ele) {
    var selected = [];
    $ele.each(function() {
        var name = $(this).attr('value');
        if(name.toLowerCase() == 'all') {
            selected = ['All']
            return;
        }
        selected.push(name);
    });
    return selected.join(' or ');
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

    console.log(isRegularSearch);

    if(!isRegularSearch) {
        $("#advSearchForm :input, #advSearchForm select").each(function(index, ele) {
            var field = $(this).attr('name');
            var query = $( this ).val();
            if(field == 'aq.object_type' && query == 'All') {

            } else if(query){
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

        delete params['query'];
        $( "#searchTextField" ).val('');

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

    var object_type = getSelectedFilters($("input.moduleFilter.active:checked"))
    if(object_type) {
        params['object_type'] = object_type
    } else {
        delete params['object_type']
    }

    var sGroup = getSelectedFilters($("input.sGroupFilter.active:checked"))
    if(sGroup) {
        params['sGroup'] = sGroup
    } 

    var contributor = getSelectedFilters($("input.contributorFilter.active:checked"))
    if(contributor) {
        params['contributor'] = contributor
    } else {
        delete params['contributor']
    }

    var tag = getSelectedFilters($("input.tagFilter.active:checked"))
    if(tag) {
        params['tag'] = tag
    } else {
        delete params['tag']
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
        //$('#filterPanel').replaceWith(data.filterPanel);
        //$('.observation_location').replaceWith(data.mapViewHtml);
        setActiveTag(activeTag);
        updateDownloadBox(data.instanceTotal);
        reInitializeGroupPost();
        updateRelativeTime();
        last_actions();
        eatCookies();			
        $(".paginateButtons a").off('click').on('click', handlePaginateButtons);
    console.log('updated gallery trigger');
        $('.list').trigger('updatedGallery');
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
    var mapLocationPicker = $('#big_map_canvas').data('maplocationpicker'); 
    if(mapLocationPicker == undefined) {
        loadGoogleMapsAPI(function() {
            mapLocationPicker = new $.fn.components.MapLocationPicker(document.getElementById("big_map_canvas"));
            mapLocationPicker.initialize();
            $('#big_map_canvas').data('maplocationpicker', mapLocationPicker);
            refreshMarkers(p, undefined, undefined, mapLocationPicker);
            refreshMapBounds(mapLocationPicker);
            oldParams = params;
	    $('#big_map_canvas').trigger('maploaded');
        })

    } else {
        //TODO:remove bounds before comparision
        //order of params is important for this test to pass
        if(JSON.stringify(oldParams) != JSON.stringify(p))
            refreshMarkers(p, undefined, undefined, mapLocationPicker);
        refreshMapBounds(mapLocationPicker);
        oldParams = params;
    }
}

function refreshMapBounds(mapLocationPicker) {
    var bounds = $('#bounds').val();
    if(bounds) {
        bounds = bounds.split(',');
        if(bounds.length == 4) {
            mapLocationPicker.map.fitBounds([
                    [bounds[0], bounds[1]],
                    [bounds[2], bounds[3]]
                    ]);
        }
    } else {
        mapLocationPicker.resetMap();
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

function load_content(params){
    var marker = this;
    if(params.id) {
        $.ajax({
            url: window.params.snippetUrl+"/"+params.id,
            success: function(data){
                marker.bindPopup("<div id='info-content' class='thumbnail'>" + data + "</div>").openPopup();;
            }
        });
    }
}

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



function loadSpeciesGroupCount() {
    var $me = $(this);
    var target = window.location.pathname + window.location.search;
    var a = $('<a href="'+target+'"></a>');
    var url = a.url();
    var href = url.attr('path');
    var params = getFilterParameters(url);
    $.ajax({
        url:window.params.observation.speciesGroupCountListUrl,
        dataType: "json",
        data:params,
        success: function(data) {
            if(data.status === 'success') {
                if(data.speciesGroupCountList) {
                    loadGoogleVisualizationAPI(function(){
                        var visualization_data = new google.visualization.DataTable();
                        $.each(data.speciesGroupCountList.columns, function(index, item) {
                            visualization_data.addColumn( item[0], item[1]);
                        });

                        $.each(data.speciesGroupCountList.data, function(index, item) {
                            visualization_data.addRow(item);
                        });

                        var columnChart = new google.visualization.ColumnChart(
                            document.getElementById('speciesGroupCountList'));

                        columnChart.draw(visualization_data,  {
                            title:window.i8ln.species.specie.ops,
                            vAxis:{minValue:0, maxValue:5, format: '#'},
                            legend:{position: 'bottom'},
                            chartArea:{width:'80%'}
                        });
                    })
                } else {
                    $("#speciesGroupCountList").html('<div id="relatedObservationMsg_a" class="alert alert-info" style="">No data!!</div>');
                }
            } else {
                $("#speciesGroupCountList").html('<div class="alert alert-error">'+data.msg+'</div>');
            }
        }
    });
}
    
