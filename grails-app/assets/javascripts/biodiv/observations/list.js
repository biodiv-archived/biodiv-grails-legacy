var checkView = false;
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

    $('.list').trigger('updatedGallery');
    $('.listIdentified').trigger('updatedGallery');

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
    if(window.params.isMediaFilter ==  undefined || window.params.isMediaFilter == 'true' || window.params.isMediaFilter == ''){
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
    
    $('#areaFilter').button();
    if(window.params.areaFilter ==  undefined || window.params.areaFilter == 'all' ){
    	$("#allAreaButton").addClass('active');
        $("#localAreaButton").removeClass('active');
        $("#regionAreaButton").removeClass('active');
        $("#countryAreaButton").removeClass('active');
    }else if(window.params.areaFilter == 'local'){
    	$("#allAreaButton").removeClass('active');
        $("#localAreaButton").addClass('active');
        $("#regionAreaButton").removeClass('active');
        $("#countryAreaButton").removeClass('active');
    }else if(window.params.areaFilter == 'region'){
    	$("#allAreaButton").removeClass('active');
        $("#localAreaButton").removeClass('active');
        $("#regionAreaButton").addClass('active');
        $("#countryAreaButton").removeClass('active');
    }else{
    	$("#allAreaButton").removeClass('active');
        $("#localAreaButton").removeClass('active');
        $("#regionAreaButton").removeClass('active');
        $("#countryAreaButton").addClass('active');
    }
    
    $("#allAreaButton").click(function() {
        if($("#allAreaButton").hasClass('active')){
            return false;
        }
        $("#areaFilter").val('all');
        $("#allAreaButton").addClass('active');
        $("#localAreaButton").removeClass('active');
        $("#regionAreaButton").removeClass('active');
        $("#countryAreaButton").removeClass('active');

        updateGallery(undefined, window.params.queryParamsMax, window.params.offset, undefined, window.params.isGalleryUpdate);
        return false;
    });
    
    $("#localAreaButton").click(function() {
        if($("#localAreaButton").hasClass('active')){
            return false;
        }
        $("#areaFilter").val('local');
        $("#allAreaButton").removeClass('active');
        $("#localAreaButton").addClass('active');
        $("#regionAreaButton").removeClass('active');
        $("#countryAreaButton").removeClass('active');

        updateGallery(undefined, window.params.queryParamsMax, window.params.offset, undefined, window.params.isGalleryUpdate);
        return false;
    });

    $("#regionAreaButton").click(function() {
        if($("#regionAreaButton").hasClass('active')){
            return false;
        }
        $("#areaFilter").val('region');
        $("#allAreaButton").removeClass('active');
        $("#localAreaButton").removeClass('active');
        $("#regionAreaButton").addClass('active');
        $("#countryAreaButton").removeClass('active');

        updateGallery(undefined, window.params.queryParamsMax, window.params.offset, undefined, window.params.isGalleryUpdate);
        return false;
    });

    $("#countryAreaButton").click(function() {
        if($("#countryAreaButton").hasClass('active')){
            return false;
        }
        $("#areaFilter").val('country');
        $("#allAreaButton").removeClass('active');
        $("#localAreaButton").removeClass('active');
        $("#regionAreaButton").removeClass('active');
        $("#countryAreaButton").addClass('active');

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

    $(document).on('click', '.traitFilter button, .traitFilter .none, .traitFilter .any', function(){
        if($(this).hasClass('active')){
            return false;
        }
        if($(this).hasClass('MULTIPLE_CATEGORICAL')) {
            $(this).parent().parent().find('.all, .any, .none').removeClass('active btn-success');
            if($(this).hasClass('btn-success')) 
                $(this).removeClass('active btn-success');
            else
                $(this).addClass('active btn-success');
        } else {
            $(this).parent().parent().find('button, .all, .any, .none').removeClass('active btn-success');
            $(this).addClass('active btn-success');
        }

        updateGallery(undefined, window.params.queryParamsMax, window.params.offset, undefined, window.params.isGalleryUpdate);
        return false;
    });
    $('#speciesIdentifiedGroupFilter button').click(function(){
        if($(this).hasClass('active')){
            return false;
        }
        $('#speciesIdentifiedGroupIdentifiedFilter button.active').removeClass('active');
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
    
    $('.hasMedia_filter_label').click(function(){
        var caret = '<span class="caret"></span>'
        if(stringTrim(($(this).html())) == stringTrim($("#has_media").html().replace(caret, ''))){
            return true;
        }
	    $('.hasMedia_filter_label.active').removeClass('active');
	    $(this).addClass('active');
	    $("#has_media").html($(this).html() + caret);
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

    $(document).on('click', ".removeQueryFilter", function(){
        var removeParam = undefined;
        if($('input[name="'+$(this).attr('data-target')+'"]').length != 0) {
            $('input[name="'+$(this).attr('data-target')+'"]').val('')
        } else if($('select[name="'+$(this).attr('data-target')+'"]').length != 0) {
            $('select[name="'+$(this).attr('data-target')+'"]').val('')
        } else {
            $( "#searchTextField" ).val('');	
        }
        if($(this).attr('data-target') == 'taxon') {
            $("input#taxon").val();
            $('#taxonHierarchy').find(".taxon-highlight").removeClass('taxon-highlight');
        } else if($(this).attr('data-target').startsWith('trait.')) {
            var t = $(this).attr('data-target').split('=');
            var tid = t[0].replace('trait.','');
            var tvid = t[1];
            if(!tvid.startsWith('rgb') && tvid.indexOf(':') == -1) {
                if(tvid == 'none') {
                    $('.traitFilter div[data-tid='+tid+'][data-tvid='+tvid+']').removeClass('active btn-success');
                    $('.trait div[data-tid='+tid+'][data-tvid='+tvid+']').removeClass('active btn-success');
                } else {
                    $('.traitFilter button[data-tid='+tid+'][data-tvid='+tvid+']').removeClass('active btn-success');
                    $('.trait button[data-tid='+tid+'][data-tvid='+tvid+']').removeClass('active btn-success');
                }
                $('.traitFilter div[data-tid='+tid+'][data-tvid=all]').addClass('active btn-success');
                $('.trait div[data-tid='+tid+'][data-tvid=all]').addClass('active btn-success');
            }

            $(".trait_range_slider").each(function(){
                var v = $(this).val();
                if(v) {
                    trait = $(this).attr('data-tid');
                    if(trait == tid) {
                        var a = [$(this).data('slider-min'), $(this).data('slider-max')];
                        $(this).slider('setValue',a).val('');
                    }
                }
            });

            $('.trait_date_range').each(function(){
                var v = $(this).val();
                if(v) {
                    trait = $(this).attr('data-tid');
                    if(trait == tid) {
                        $(this).val('');
                    }
                }
            });
            $(".colorpicker-component").each(function(){
                var v = $(this).find('input').val();
                if(v) {
                    trait = $(this).find('input').attr('data-tid');
                    if(trait == tid) {
                        $(this).find('input').val('');
                        //$('this').colorpicker('setValue','');
                        //change colorpicker value with .colorpicker('setValue', value)
                    }
                } 
            });

 
        }
        removeParam = $(this).attr('data-target').replace('#','');
        updateGallery($(this).prev().attr('href'), window.params.queryParamsMax, window.params.offset, undefined, window.params.isGalleryUpdate, undefined, undefined, undefined, removeParam);
        return false;
    });

    $('select[name="limit"]').on('change', function() {
        updateGallery(undefined, $(this).val(), window.params.offset, false, window.params.isGalleryUpdate);
    });

    var tmpTarget =  window.location.pathname + window.location.search;
    setActiveTag($('<a href="'+ tmpTarget +'"></a>').url().param()["tag"]);

    $('.observation').on("click", ".loadMore", function() {
        $.autopager({

            autoLoad : true,
            // a selector that matches a element of next page link
            link : 'div.paginateButtons a.nextLink',

            // a selector that matches page contents
            content : '.mainContent:first',

            //insertBefore: 'div.checklist_list_main > .table > .table-footer', 
            appendTo : '.mainContentList:first',

            // a callback function to be triggered when loading start 
            start : function(current, next) {

                $(".loadMore .progress").show();
                $(".loadMore .buttonTitle").hide();
            },

            // a function to be executed when next page was loaded. 
            // "this" points to the element of loaded content.
            load : function(current, next) {
                checkList();
                $(".mainContent:last").hide().fadeIn(3000);

                $("div.paginateButtons a.nextLink").attr('href', next.url);
                if (next.url == undefined) {
                    $(".loadMore").hide();
                } else {
                    $(".loadMore .progress").hide();
                    $(".loadMore .buttonTitle").show();
                }
    
                var a = $('<a href="'+current.url+'"></a>');
                var url = a.url();
                var params = url.param();
                if(checkView){
                    params["view"] = "list";
                }else{
                    params["view"] = "grid";
                }
                delete params["append"];
                delete params["loadMore"];
                params['max'] = parseInt(params['offset'])+parseInt(params['max']);
                params['offset'] = 0;
                var History = window.History;
                History.pushState({state:1}, "Portal", '?'+decodeURIComponent($.param(params))); 
                updateRelativeTime();
                last_actions();
                eatCookies();
//                $('.observations_list').not('.trait_list').find('.recommendations .nav-tabs').tab();

                //$('.list').trigger('updatedGallery');
            }
        });

        $.autopager('load');
        return false;
    });

    $('.download-close').click(function(){
        var me = this;
        var download_box = $(me).parents('.download-box');
        $(download_box).find('.downloadModal').modal('hide');
    });

    $('.download-action').click(function(){
        var me = this;
        var download_box = $(me).parent('.download-box');
        $.ajax({ 
            url:window.params.isLoggedInUrl,
            success: function(data, statusText, xhr, form) {
                if(data === "true"){
                    $(download_box).find('.downloadModal').modal('show');
                    return false;
                }else{
                    window.location.href = window.params.loginUrl+"?spring-security-redirect="+window.location.href;
                }
            },
            error:function (xhr, ajaxOptions, thrownError){
                return false;
            } 
        });
    });

    $('.download-form').bind('submit', function(event) {
        var downloadFrom = $(this).find('input[name="downloadFrom"]').val();
        var filterUrl = '';
        if(downloadFrom == 'uniqueSpecies') {
            var hostName = 'http://' + document.location.hostname;
            var target = window.location.pathname + window.location.search;
            var a = $('<a href="'+target+'"></a>');
            var url = a.url();
            var href = url.attr('path');
            href = href.replace('list', 'distinctReco');
            var params = getFilterParameters(url);
            filterUrl = hostName + href + '?';
            params['actionType'] = 'list';
            $.each(params, function(key, value){
                filterUrl = filterUrl + key + '=' + value + '&';
            });
        } else {
            filterUrl = window.location.href;
        }
        var queryString =  window.location.search
        $(this).ajaxSubmit({ 
            url:window.params.requestExportUrl + queryString,
            dataType: 'json', 
            type: 'POST',
            beforeSubmit: function(formData, jqForm, options) {
                formData.push({ "name": "filterUrl", "value": filterUrl});
                //formData.push({ "name": "source", "value": "${source}"});
                //formData.push({ "name": "downloadObjectId", "value": "${downloadObjectId}"});
            }, 
            success: function(data, statusText, xhr, form) {
                var msg = '';
                for(var i=0; i<data.length; i++) {
                    if(data[0].success)
            $(".alertMsg").removeClass('alert alert-error').addClass('alert alert-success').html("Scheduled "+data.length+" job(s) for every 5000 records. "+data[0].msg);
                    else 
            $(".alertMsg").removeClass('alert alert-success').addClass('alert alert-error').html(data[0].msg+"   "+JSON.stringify(data[0].errors));
                }
                $('.download-box').find('.download-options').hide();
                $("html, body").animate({ scrollTop: 0 });
                return false;
            },
            error:function (xhr, ajaxOptions, thrownError){
                //successHandler is used when ajax login succedes
                var successHandler = this.success, errorHandler = null;
                handleError(xhr, ajaxOptions, thrownError, successHandler, errorHandler);
            } 
        });
    event.preventDefault();
    });

    //	last_actions();
    eatCookies();

    $('#taxonHierarchy').on("reloadGrid", function() {
        updateGallery(window.location.pathname + window.location.search, 40, 0, undefined, true);
    }); 

    /* Added for  Species Update*/
    var group_icon = $('.group_icon_show');
    var label_group = $('label.group');
    var propagateGrpHab = $('.propagateGrpHab');
    $('.propagateGrpHab .control-group  label').hide();

    $(document).on('click','.edit_group_btn',function(){
        $(this).parent().hide();
        $(this).parent().parent().find('.propagateGrpHab').show();
        $('label.label_group').hide();
        initializeSpeciesGroupHabitatDropdowns();
        /*var obvId = $(this).attr('id');           
        $('#group_icon_show_wrap_'+obvId).hide();
        //habitat_icon.hide();
        $('label.label_group').hide();
        $('#propagateGrpHab_'+obvId).show();
        */
    }); 

    $(document).on('submit','#updateSpeciesGrp', function(event) {

        var that = $(this);
        $(this).ajaxSubmit({ 
            url: "/observation/updateSpeciesGrp",
            dataType: 'json', 
            type: 'GET',  
            beforeSubmit: function(formData, jqForm, options) {
                /*console.log(formData);
                  if(formData.group_id == formData.prev_group){
                  alert("Nothing Changes!");
                  return false;
                  }*/
            },               
            success: function(data, statusText, xhr, form) {
                var parentWrap = that.parent();
                parentWrap.parent().find('.group_icon_show').removeClass(data.model.prevgroupIcon).addClass(data.model.groupIcon).attr('title',data.model.groupName);
                parentWrap.parent().find('.group_icon_show_wrap').show();                
                parentWrap.parent().find('.prev_group').val(data.model.prev_group);
                parentWrap.hide();
                //$('.group_icon_show_'+data.instance.id).removeClass(data.model.prevgroupIcon).addClass(data.model.groupIcon).attr('title',data.model.groupName);
                //$('#group_icon_show_wrap_'+data.instance.id).show();
                //habitat_icon.show();
                //$('#propagateGrpHab_'+data.instance.id).hide();
                //$('.prev_group_'+data.instance.id).val(data.model.prev_group);
                updateFeeds();
            },
            error:function (xhr, ajaxOptions, thrownError){
                //successHandler is used when ajax login succedes
                var successHandler = this.success, errorHandler = showUpdateStatus;
                handleError(xhr, ajaxOptions, thrownError, successHandler, errorHandler);
            } 

        });    

        event.preventDefault(); 
    }); 

    $(document).on('submit','.addRecommendation', function(event) {
        var that = $(this);
        $(this).ajaxSubmit({
            url:window.params.observation.addRecommendationVoteURL,
            dataType: 'json', 
            type: 'GET',
            beforeSubmit: function(formData, jqForm, options) {
                formData.push({'name':'format', 'value':'json', 'type':'text'});
                updateCommonNameLanguage(that.find('.languageComboBox'));
                return true;
            }, 
            success: function(data, statusText, xhr, form) {
                if(data.status == 'success' || data.success == true) {
                    if(data.canMakeSpeciesCall === 'false'){
                        $('#selectedGroupList').modal('show');
                    } else{
                        preLoadRecos(3, 0, false,data.instance.observation);
                        if(that.hasClass('showPage')){
                            updateUnionComment(null, window.params.comment.getAllNewerComments);
                            updateFeeds();
                        }
                        setFollowButton();
                        showUpdateStatus(data.msg, data.success?'success':'error');
                    }
                    $(".addRecommendation_"+data.instance.observation)[0].reset();
                    that.find(".canName").val("");   
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
        event.preventDefault();
    });

    $(document).on('click','#obvList',function(){           
        checkUrl("grid","list");        
        params['view'] = "list"; 
        checkView = true;           
        $(this).addClass('active');
        $('#obvGrid').removeClass('active');
        addListLayout();
    });

    $(document).on('click','#obvGrid',function(){              
        checkUrl("list","grid");
        params['view'] = "grid"; 
        checkView = false;
        $(this).addClass('active');
        $('#obvList').removeClass('active');
        addGridLayout();
    });

    $(document).on('click','.clickSuggest',function(){  
        var obv_id = $(this).attr('rel');
        var ele_nxt = $(this).parent().parent().parent();
        var wrap_place = ele_nxt.find('.addRecommendation_wrap_place');
        wrap_place.is(':empty')
        if(!$.trim( wrap_place.html() ).length){
            wrap_place.html($('#addRecommendation_wrap').html());
            wrap_place.find('.addRecommendation').addClass('addRecommendation_'+obv_id);
            wrap_place.find('input[type="hidden"][name="obvId"]').val(obv_id);
            initializeNameSuggestion();
            initializeLanguage(wrap_place.find('.languageComboBox'));
        }
        //ele_nxt.show('slow');

    });

    $(document).on('click','.view_bootstrap_gallery',function(){
        var ovbId = $(this).attr('rel');
        var images = $(this).attr('data-img').split(",");
        $('#links').empty();
        appendGallery(ovbId,images);           
    });
    
    initializeSpeciesGroupHabitatDropdowns();
    
});

/**
 */
function eatCookies() {	
/*    var hashString = window.location.hash.substring(1);
    if ($.cookie("listing") == "list") {
        if(!hashString.startsWith('l')) {
            if(hashString.startsWith('g')) {
                window.location.hash = "l"+hashString.substring(1);
            } else if(hashString){
                window.location.hash = "l"+hashString;
            }
        }
    } else {
        if(!hashString.startsWith('g')) {
            if(hashString.startsWith('l')) {
                window.location.hash = "g"+hashString.substring(1);
            } else if(hashString){
                window.location.hash = "g"+hashString;
            }
        }
    }
*/    adjustHeight();
}

function getSelectedGroup() {
    var grp = ''; 
    $('#speciesGroupFilter button, #speciesIdentifiedGroupFilter button').each (function() {
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

function getSelectedTrait($traitFilter, putValue) {
    putValue = (putValue === undefined)?false:true;
    if($traitFilter == undefined)
        $traitFilter = $('.traitFilter.filterable button, .traitFilter.filterable .none, .traitFilter.filterable .any, .trait.filterable button, .trait.filterable .none, .trait.filterable .any');
    var trait='',selTrait={}; 
    $traitFilter.each(function(){
        if($(this).hasClass('btn-success')) {
            trait = $(this).attr('data-tid');
            if(trait) {
            var v = putValue==true? $(this).attr('value') : $(this).attr('data-tvid');
            if(selTrait[trait] == undefined) selTrait[trait]='';
            if(v) selTrait[trait] += v+',';
            }
        }
    });
    $(".trait_range_slider").each(function(){
        var v = $(this).val();
        if(v) {
            v = v.replace(',',':');
            trait = $(this).attr('data-tid');
            if(selTrait[trait] == undefined) selTrait[trait]='';
            selTrait[trait] += v+',';
        } else if($('input[data-tid]').length == 1) {
            //is from trait show page
            selTrait[$(this).attr('data-tid')] = 'any,';
        };

    });

    $('.trait_date_range').each(function(){
        var v = $(this).val();
        if(v) {
             trait = $(this).attr('data-tid');
            if(selTrait[trait] == undefined) selTrait[trait]='';
            selTrait[trait] += v+',';
        } else if($('input[data-tid]').length == 1) {
            //is from trait show page
            selTrait[$(this).attr('data-tid')] = 'any,';
        };

    });
    $(".colorpicker-component").each(function(){
        var v = $(this).find('input').val();
        if(v) {
            trait = $(this).find('input').attr('data-tid');
            if(selTrait[trait] == undefined) selTrait[trait]='';
            selTrait[trait] += v+',';
        } else if($('input[data-tid]').length == 1) {
            //is from trait show page
            selTrait[$(this).find('input').attr('data-tid')] = 'any,';
        };
    });

    return selTrait;
}

function getSelectedTraitStr($traitFilter, putValue) {
    putValue = (putValue === undefined)?false:true;
    var traits = getSelectedTrait($traitFilter, putValue);
    var traitsStr = '';
    for(var m in traits) {
        traitsStr += m+':'+traits[m].substring(0,traits[m].length-1)+';';
    }
    return traitsStr;
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

function getMediaFilterBy() {
    var hasMedia = ''; 
    $('.hasMedia_filter_label').each (function() {
        if($(this).hasClass('active')) {
            hasMedia += $(this).attr('value');
        }
    });
    return hasMedia;	
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

function getSelectedAreaFilter() {
    var area = ''; 
    area = $("#areaFilter").attr('value');
    if(area) {
        area = area.replace(/\s*\,\s*$/,'');
        return area;
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

function getSelectedFilters($ele, noneSelected) {
    var selected = [];
    var allSelected = true;
    var noneSelected = (noneSelected != undefined) ? noneSelected : true;
    $ele.each(function() {
        if($(this).hasClass('active') && $(this).is(':checked')) {
            var name = $(this).attr('value');
            if(name.toLowerCase() == 'all') {
                //selected = ['All']
                //return;
            } else {
                selected.push(name);
            }
            noneSelected = false;
        } else {
            allSelected = false;
        }
    });
    if(noneSelected) resetSearchFilters($ele.parent().parent());
    if(allSelected == false) return selected.join(' OR ');
} 

function getFilterParameters(url, limit, offset, removeUser, removeObv, removeSort, isRegularSearch, removeParam) {
    var params = url.param();
    if(removeParam) {
        if(removeParam.match('trait\\.')) {
            var kv = removeParam.split('=');
            var tP = params[kv[0]];
            var tvStr = params[kv[0]].split(',');
            params[kv[0]] = '';
            for(var i=0; i<tvStr.length; i++) {
                console.log(tvStr[i]+'  '+kv[1])
                if(decodeURIComponent(tvStr[i]) != kv[1]) params[kv[0]] += tvStr[i]+',';
            }
            params[kv[0]] = params[kv[0]].substring(0,params[kv[0]].length-1);
        } else {
            delete params[removeParam];
        }
    }
    removeSort = (typeof removeSort === "undefined") ? false : removeSort;

    if(!removeSort) {
        var sortBy = getSelectedSortBy();
        if(sortBy) {
            params['sort'] = sortBy;
        }
    }
    if(getMediaFilterBy() != '') {
        params['hasMedia'] = getMediaFilterBy();
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

    var mediaFilter = getSelectedMedia();
    if(mediaFilter) {
    	params['isMediaFilter'] = mediaFilter;
    }

    var areaFilter = getSelectedAreaFilter();
    if(areaFilter) {
    	params['areaFilter'] = areaFilter;
    }

    var grp = getSelectedGroup();
    if(grp) {
        params['sGroup'] = grp;
    }

    var habitat = getSelectedHabitat();
    if(habitat) {
        params['habitat'] = habitat;
    }

    /*for(var key in params) {
        if(key.match('trait\\.')) {
            delete params[key];
        }
    }*/
    var trait = getSelectedTrait();
    for(var key in trait) { 
        params['trait.'+key]=trait[key];
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
        if(params['query'] != undefined){
            delete params['query'];
        }
    }

    if(!isRegularSearch) {
        $("#advSearchForm :input, #advSearchForm select").each(function(index, ele) {
            var field = $(this).attr('name');
            if(field) {
                var query = $( this ).val();
                var queryStr = '';
                if($.isArray(query)) {
                    for(var i=0; i< query.length; i++) {
                        queryStr += query[i]
                        if(i < query.length-1) queryStr += " OR "
                    }
                    queryStr += ""
                } else {
                    queryStr = query;
                }
                if(field == 'aq.object_type' && query == 'All') {

                } 
                else if(query){
                    params[field] = queryStr;
                } else {
                    if(params[field] != undefined){
                        delete params[field];
                    }
                }
            }
        });
        
        delete params['daterangepicker_start'];
        delete params['daterangepicker_end'];

        $.each($(document).find('#searchToggleBox input[name=daterangepicker_start]'), function(index, value) {
            if($(value).closest('#observedOnDatePicker').length > 0) {
                params['observedon_start'] = $(value).val();
            } else {
                params['daterangepicker_start'] = $(value).val();
            }
        });
        $.each($(document).find('#searchToggleBox input[name=daterangepicker_end]'), function(index, value) {
            if($(value).closest('#observedOnDatePicker').length > 0) {
                params['observedon_end'] = $(value).val();
            } else {
                params['daterangepicker_end'] = $(value).val();
            }
        });
        if((params['daterangepicker_start'] === new Date(0).toString('dd/MM/yyyy')) && (params['daterangepicker_end'] === Date.today().toString('dd/MM/yyyy'))){
            delete params['daterangepicker_start'];
            delete params['daterangepicker_end'];
        }
        if((params['observedon_start'] === new Date(0).toString('dd/MM/yyyy')) && (params['observedon_end'] === Date.today().toString('dd/MM/yyyy'))){
            delete params['observedon_start'];
            delete params['observedon_end'];
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

    var user = $("#user").val();
    if(user) {
        params['user'] = user;
    } 

    var object_type = getSelectedFilters($("input.moduleFilter"))
    if(object_type) {
        params['object_type'] = object_type
    } else {
        delete params['object_type']
    }

    var uGroup = getSelectedFilters($("input.uGroupFilter"))
    if(uGroup) {
        params['uGroup'] = uGroup
    } else {
        //delete params['uGroup']
    }

    var sGroup = getSelectedFilters($("input.sGroupFilter"))
    if(sGroup) {
        params['sGroup'] = sGroup
    } 

    var contributor = getSelectedFilters($("input.contributorFilter"))
    if(contributor) {
        params['contributor'] = contributor
    } else {
        delete params['contributor']
    }

    var tagFilter = getSelectedFilters($("input.tagFilter"))
    if(tagFilter) {
        params['tagFilter'] = tagFilter
    } else {
        delete params['tagiFilter']
    }

    var taxon = $("input#taxon").val();
    if(taxon) {
        var ibpClassification = $('.ibpClassification').val();
        var classificationId = ibpClassification?ibpClassification:$('#taxaHierarchy option:selected').val();
        if(classificationId) 
            params['classification'] = classificationId
        params['taxon'] = taxon
    } else {
        delete params['taxon']
        delete params['classification']
        $(".jstree-anchor").removeClass('taxon-highlight');
    }

    var taxonRank = $("input#taxonRank").val();
    if(taxonRank) {
        params['taxonRank'] = taxonRank
    } else {
        delete params['taxonRank']
    }

    var status = $("input#status").val();
    if(status) {
        params['status'] = status
    } else {
        delete params['status']
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
        $('.observations_list:first').replaceWith(data.model.obvListHtml);
        $('#info-message').replaceWith(data.model.obvFilterMsgHtml);
        $('#tags_section').replaceWith(data.model.tagsHtml);
        $('#summary_section').replaceWith(data.model.summaryHtml);        
        //$('#filterPanel').replaceWith(data.model.filterPanel);
        //$('.observation_location').replaceWith(data.model.mapViewHtml);
        setActiveTag(activeTag);
        updateDownloadBox(data.model.instanceTotal);
        reInitializeGroupPost();
        updateRelativeTime();
        last_actions();
        eatCookies();			
        $(".paginateButtons a").off('click').on('click', handlePaginateButtons);
//        $('.observations_list').not('.trait_list').find('.recommendations .nav-tabs').tab();
        $('.list').trigger('updatedGallery');
        checkList();
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

function updateGallery(target, limit, offset, removeUser, isGalleryUpdate, removeObv, removeSort, isRegularSearch, removeParam,updateHistory) {    
    var params = getUpdateGalleryParams(target, limit, offset, removeUser, isGalleryUpdate, removeObv, removeSort, isRegularSearch, removeParam);
    isGalleryUpdate = (isGalleryUpdate == undefined)?true:isGalleryUpdate
    if(isGalleryUpdate)
    	params["isGalleryUpdate"] = isGalleryUpdate;
    var href = params.href;
    var base = params.base;
    if(checkView){
        params["view"] = "list";
    }else{
        params["view"] = "grid";
    }
    delete params["href"]
    delete params["base"]
    var recursiveDecoded = decodeURIComponent($.param(params));
    
    var doc_url = href+'?'+recursiveDecoded;
    var History = window.History;
    delete params["isGalleryUpdate"]
    if(updateHistory != false){
        History.pushState({state:1}, document.title, '?'+decodeURIComponent($.param(params))); 
    }
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
    //delete p.bounds;
    //delete oldParams.bounds;
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
            mapLocationPicker.map.on('zoomend', function() {
                var bounds = mapLocationPicker.getSelectedBounds()
                $("#bounds").val(bounds);
                showMapView();
            });
        })

    } else {
        //TODO:remove bounds before comparision
        //order of params is important for this test to pass
        if(JSON.stringify(oldParams) != JSON.stringify(p)) {
            refreshMarkers(p, undefined, undefined, mapLocationPicker);
        }
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

    $('#speciesGroupFilter button, #speciesIdentifiedGroupFilter button').unbind('click');
    $('#speciesGroupFilter button, #speciesIdentifiedGroupFilter button').attr('data-toggle', 'buttons-checkbox').click({'button_group':$('#speciesGroupFilter ,#speciesIdentifiedGroupFilter'), 'multiSelect':multiSelect}, speciesHabitatInterestHandler);

    $('#habitatFilter button').unbind('click');
    $('#habitatFilter button').attr('data-toggle', 'buttons-checkbox').click({'button_group':$('#habitatFilter'), 'multiSelect':multiSelect}, speciesHabitatInterestHandler);
}

function updateDownloadBox(instanceTotal){
    $('#instanceTotal').val(instanceTotal);
    if(instanceTotal > 0){
        $('.download-box').show();
/*        if(instanceTotal > 5000) {
            jQuery(".download-box input:radio").attr('disabled',true);
            jQuery(".download-box input[type='submit']").attr('disabled',true);
        } else {
            jQuery(".download-box input:radio").removeAttr('disabled')
            jQuery(".download-box input[type='submit']").removeAttr('disabled');
        }
*/    }else{
        $('.download-box').hide();
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
            if(data.success == true) {
                if(data.model.speciesGroupCountList) {
                    loadGoogleVisualizationAPI(function(){
                        var visualization_data = new google.visualization.DataTable();
                        $.each(data.model.speciesGroupCountList.columns, function(index, item) {
                            visualization_data.addColumn( item[0], item[1]);
                        });

                        $.each(data.model.speciesGroupCountList.data, function(index, item) {
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
function checkList(){   
  /*  if(checkView){
        $('#obvList').trigger('click');        
    }
    $('.obvListwrapper').show();
    */
}


function appendGallery(ovbId,images){
        $("#links").removeClass();
        $("#links").addClass('links'+ovbId);
        var carouselLinks = [],
        linksContainer = $('.links'+ovbId),
        baseUrl,
        thumbUrl;
        $.each(images, function (index, photo) {
            baseUrl = (photo.indexOf('http://') == -1)?""+window.params.observation.serverURL+photo:photo;
            $('<a/>')
                .append($('<img>'))
                .prop('href', baseUrl)                
                .attr('data-gallery', '')
                .appendTo(linksContainer);
            carouselLinks.push({
                href: baseUrl              
            });
        }); 
        $('.links'+ovbId+' a:first').trigger('click');
    }


function loadSpeciesnameReco(){
    $('.showObvDetails').each(function(){
        var observationId = $(this).attr('rel');
        if(!$(this).find('.recoSummary_'+observationId).hasClass('addSuccess')){
            $(".recoSummary_"+observationId).html('<li style="text-align: center;"><img src="'+window.params.spinnerURL+'" alt="Loading..." /></li>')
            preLoadRecos(3, 0, false,observationId);
        }
    });
}
function addListLayout(){
    $('.thumbnails>li').css({'width':'100%'}).addClass('addmargin');
    $('.snippet.tablet').addClass('snippettablet');
    $('.showObvDetails, .view_bootstrap_gallery, .recommendations').show();
    $('.species_title_wrapper').hide();
    $('.species_title_wrapper').parent().css({'height':'20px'});
    //loadSpeciesnameReco();
    initializeLanguage();

}

function addGridLayout(){
    $('.thumbnails>li').css({'width':'inherit'}).removeClass('addmargin');
    $('.snippet.tablet').removeClass('snippettablet');
    $('.species_title_wrapper').show();
    $('.species_title_wrapper').parent().css({'height':'50px'});
    $('.showObvDetails, .view_bootstrap_gallery, .recommendations').hide();    
}

function checkUrl(viewText,changeText){
    var ls = window.location.search;
    ls = ls.slice(1);
    if((!params['view'] || params['view'] == viewText) && ( !ls && ls.split("&").length == 1)){
        var newurl = window.location.protocol + "//" + window.location.host + window.location.pathname + '?view='+changeText;
        window.history.pushState({path:newurl},'',newurl);               
    }else{      
        var lang_key = "view=";
        var ps = ls.split("&");
        var flag, i;
        if(ps) {
            for(i=0; i<ps.length; i++){
                if(ps[i].indexOf(lang_key) == 0){
                    flag = true;
                    break;
                }
                else{
                    flag = false;
                }
            }

            if(flag){
                ps[i] = lang_key + changeText;
                ls = ps.join("&");
            }
            else{
                ls += "&" + lang_key + changeText;
            }
        }

        newurl = window.location.href.replace(window.location.search, "?"+ls);
        window.history.pushState({path:newurl},'',newurl);

        }

        var nextLink = $('.nextLink');
        nextLink.attr('href',nextLink.attr('href').replace('view='+viewText,'view='+changeText));        
    }

function initializeSpeciesGroupHabitatDropdowns() {
    /*var selectedGroupHandler = function(e){
        e.stopPropagation();
        //$(this).dropdown('toggle');
        $(this).closest(".groups_super_div").find(".group_options").toggle();
        //$(this).css({'background-color':'#fbfbfb', 'border-bottom-color':'#fbfbfb'});
    }*/
    var selectedGroupOptionHandler = function() {
        var is_save_btn_exists = $(this).closest(".groups_super_div").parent().parent().find('.save_group_btn');
        if(is_save_btn_exists.length == 1){
            is_save_btn_exists.show();
        }
        $(this).closest(".groups_super_div").find(".group").val($(this).val());
        $(this).closest(".groups_super_div").find(".selected_group").html($(this).html());
//        $(this).closest(".group_options").hide();
        //$(this).closest(".groups_super_div").find(".selected_group").css({'background-color':'#e5e5e5', 'border-bottom-color':'#aeaeae'});
        if($(this).closest(".groups_super_div").find(".selected_group b").length == 0){
            $('<b class="caret"></b>').insertAfter($(this).closest(".groups_super_div").find(".selected_group .display_value"));
        }
    }
    
    /*var selectedHabitatHandler = function(e) {
        e.stopPropagation();
        //$(this).dropdown('toggle');
        $(this).closest(".habitat_super_div").find(".habitat_options").toggle();
        //$(this).css({'background-color':'#fbfbfb', 'border-bottom-color':'#fbfbfb'});
    }*/

    var selectedHabitatOptionHandler = function() {
        $(this).closest(".habitat_super_div").find(".habitat").val($(this).val());
        $(this).closest(".habitat_super_div").find(".selected_habitat").html($(this).clone());
        //$(this).closest(".habitat_options").hide();
        //$(this).closest(".habitat_super_div").find(".selected_habitat").css({'background-color':'#e5e5e5', 'border-bottom-color':'#aeaeae'});
        if($(this).closest(".habitat_super_div").find(".selected_habitat b").length == 0){
            $('<b class="caret"></b>').insertAfter($(this).closest(".habitat_super_div").find(".selected_habitat .display_value"));
        }
    }

//    $('.groups_super_div').off('click', '.selected_group', selectedHabitatHandler).on('click', '.selected_group', selectedHabitatHandler);
    $('.groups_super_div').off('click', '.group_option', selectedGroupOptionHandler).on('click',".group_option",selectedGroupOptionHandler);

//    $('.habitat_super_div').off('click', '.selected_habitat', selectedHabitatHandler).on('click',".selected_habitat",selectedHabitatHandler);
    $('.habitat_super_div').off('click', '.habitat_option', selectedHabitatOptionHandler).on('click',".habitat_option",selectedHabitatOptionHandler);
    
}

