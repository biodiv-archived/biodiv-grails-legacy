function updateMatchingSpeciesTable() {
	$('#matchingSpeciesTable tbody').empty();
	var me = $('#matchingSpeciesTableAction');
	$(me).show();
	$(me).data('offset', 0);
	$(me).click();
}

function loadMatchingSpeciesList() {
    var $me = $(this);
    var target = window.location.pathname + window.location.search;
    var a = $('<a href="'+target+'"></a>');
    var url = a.url();
    var href = url.attr('path');
    var params = getFilterParameters(url);
    delete params['daterangepicker_start'];
    delete params['daterangepicker_end'];
    delete params['observedon_start'];
    delete params['observedon_end'];
    var element = {};
    var listFilter = $('.listFilter');
        listFilter.each(function(){
        if($(this).hasClass('active')) {
           var traitType = $(this).val();
           params['traitType'] = traitType;
        }
    });
    for(var key in params) {
        if(key.match('traitType')){
                if(params[key]=='observation'){
                element = $('div[data-isNotObservation="true"]');
                    $(element).each(function(){
                        $(this).parent().parent().hide();
                    });
            }
            else if (params[key]=='species'){
                element = $('div[data-isNotObservation="false"]');
                $(element).each(function(){
                    $(this).parent().parent().hide();
                });
            }
            else{
                element = $('div[data-isNotObservation]');
                $(element).each(function(){
                    $(this).parent().parent().show();
                });
            }
        }
        if(key.match('trait.')) {
            delete params[key];
        }
    }
    var History = window.History;
    var traits = getSelectedTrait($('.trait button, .trait .none, .trait .any'));
    for(var m in traits) {
        params['trait.'+m] = traits[m];
    }
    params['max'] = $(this).data('max');
    params['offset'] = $(this).data('offset');
    History.pushState({state:1}, document.title, '?'+decodeURIComponent($.param(params))); 
    var $matchingSpeciesTable = $('#matchingSpeciesTable');
    $.ajax({
        url:window.params.trait.matchingSpeciesListUrl,
        dataType: "json",
        data:params,
        success: function(data) {   
            if(data.success == false){
                $me.hide();
            }
            $('#matchingSpeciesList .matchingSpeciesHeading').html(data.model.totalCount?(' (' + data.model.totalCount + ')'):'');
            $('#matchingSpeciesFilterMsg').html(data.model.obvFilterMsgHtml);
            if(data.success == true && data.model.matchingSpeciesList) {
                $.each(data.model.matchingSpeciesList, function(index, item) {
                    var itemMap = {};
                    itemMap.id = item[0];
                    //itemMap.title = item[1];
                    itemMap.url = item[4];
                    itemMap.imageLink = item[5];
                    //itemMap.notes = item[6];
                    itemMap.traitIcon=item[6];
                    itemMap.type='species';
                    var imagepath=item[7];
                    //$.each(imagepath,function(index1,item1){ alert(item1); });
                    //alert(array.split(','));
                    var snippetTabletHtml = getSnippetTabletHTML(undefined, itemMap);
                    $matchingSpeciesTable.append('<tr class="jcarousel-item jcarousel-item-horizontal"><td>'+snippetTabletHtml+'<a href='+item[4]+'>'+item[1]+'</a></td><td><div id=imagediv_'+item[0]+'></div></td></tr>');
                    $('#imagediv_'+item[0]).empty();
                    $.each(imagepath,function(index1,item1){ 
                        $('#imagediv_'+item[0]).append(showIcon(item1[0], item1[1], item1[2], item1[3]));
                    });
                });
                $me.data('offset', data.model.next);
                if(!data.model.next){
                    $me.hide();
                }
            }
    }
    });
}

function showIcon(value,name,url, type){
    if(url) {
        return  '<img src="'+url+'" width="32" height="32" title="'+name+'-'+value+'" />';
    } else if(type == 'Color'){
        return '<img style="height:32px;width:32px;display:inline-block;background-color:'+value+';" tooltip="'+name+'-'+value+'" ></img>'
    } else {
//        return '<b>'+name+'</b> :'+ value;
//        return '';
    }
}

function onSubmitFact($me, objectId, objectType) {
    var id = $me.data("id");
    var traitsStr = getSelectedTraitStr($me.parent().parent().find('.trait button, .trait .none, .trait .any'), true);
    var params = {};
    params['traits'] = traitsStr;
    params['traitId'] = id;
    params['objectId'] = objectId;
    params['objectType'] = objectType;
    $.ajax({ 
        url:window.params.fact.updateFactUrl,
        method:'POST',
        data:params,
        success: function(data, statusText, xhr, form) {
            //TODO:update traits panel
            if(data.success) {
                $me.parent().parent().find('.alert').removeClass('alert alert-error').addClass('alert alert-info').html(data.msg).show();
                $me.parent().parent().replaceWith(data.model.traitHtml);
                $me.parent().parent().find('.row:first').show();
                $me.parent().parent().find('.editFactPanel').hide();
                $me.parent().find('.submitFact, .cancelFact').hide();
                $me.parent().parent().find('.row:first').show();
                $me.hide();
                $me.parent().find('.editFact').show();//.css("position","");
                updateFeeds();
            } else {
                $me.parent().parent().find('.alert').removeClass('alert alert-info').addClass('alert alert-error').html(data.msg).show();
            }
        },
        error:function (xhr, ajaxOptions, thrownError){
            //successHandler is used when ajax login succedes
            var successHandler = this.success, errorHandler = function() {
                //TODO:show error msg
                $me.parent().parent().find('.alert').removeClass('alert alert-info').addClass('alert alert-error').html(arguments.msg).show();
            }
            handleError(xhr, ajaxOptions, thrownError, successHandler, errorHandler);
        } 
    });
}

function loadTraits($me, compId) {
    var params = {};//$me.data();
    params['objectId'] = $me.data('objectid');
    params['objectType'] = $me.data('objecttype');
    params['sGroup'] = $me.data('sgroup');
    params['isObservationTrait'] = $me.data('isobservationtrait');
    params['isParticipatory'] = $me.data('isparticipatory');
    params['ifOwns'] = $me.data('ifowns');
    params['showInObservation'] = $me.data('showinobservation');
    params['loadMore'] = true;
    params['displayAny'] = false;
    params['editable'] = true;
    params['fromObservationShow'] = 'show';
    params['filterable'] = false;
    $.ajax({
        url:window.params.trait.listUrl,
        method:'GET',
        data:params,
        success: function(data) {   
            $(compId).html(data);
        }
    });
}

function loadCustomFields($me, compId) {
    var params = {};//$me.data();
    params['objectId'] = $me.data('objectid');
    $.ajax({
        url:window.params.observation.customFieldsUrl,
        method:'GET',
        data:params,
        success: function(data) {   
            if(data.html) 
                $(compId).html(data.html);
            else
                $(compId).html("<div class='alert alert-info'>No Custom Fields</div>");
        }
    });
}

var startFlag = 0;
function initTraitFilterControls() {
    $('.trait_range_slider').ionRangeSlider({
        grid:'true',
        onChange:function(data) {
            startFlag = 1;
        },
        onFinish :  function(data) {
            if(startFlag == 1) 
                updateMatchingSpeciesTable();
        }
    });

    $('.trait_date_range_slider').ionRangeSlider({
        grid:'true',
        values: [
            "January", "February", "March",
            "April", "May", "June",
            "July", "August", "September",
            "October", "November", "December"
        ],
        onChange:function(data) {
            startFlag = 1;
        },
        onFinish :  function(data) {
            if(startFlag == 1) 
                updateMatchingSpeciesTable();
        }
    });


    $('.trait_date_range').each(function(){
        var options = {
            parentEl: '#'+$(this).parent().parent().attr('id'),
            autoUpdateInput: false,
            locale:{
                format: 'DD/MM/YYYY'
            },
            maxDate: moment()
        }
        var d = $(this).val().split(':');
        if(d.length >1) {
            options['startDate'] = d[0];
            options['endDate'] = d[1];
        }
        $(this).daterangepicker(options).on('apply.daterangepicker', function(ev, picker) {
            $(this).val(picker.startDate.format('DD/MM/YYYY') + ':' + picker.endDate.format('DD/MM/YYYY'));
            updateMatchingSpeciesTable();
        })/*.on('cancel.daterangepicker', function(ev, picker) {
            $(this).val('');
        })*/;
    });

    $('.colorpicker-component').colorpicker({
        format:'rgb', 
        container: true,
        inline: true,
        colorSelectors: {
            'black': '#000000',
            'gray' : '#808080',
            'silver' : '#C0C0C0',
            'white': '#ffffff',
            'maroon' : '#800000 ',
            'red': '#FF0000',
            'olive' : '#808000',
            'yellow' : '#FFFF00',
            'green' : '#008000',
            'lime' : '#00FF00',
            'teal' : '#008080',
            'aqua' : '#00FFFF',
            'navy' : '#000080',
            'blue' : '#0000FF',
            'purple' : '#800080',
            'fuchsia' : '#FF00FF'
        }
    }).on('changeColor', function(ev){
        updateMatchingSpeciesTable();
    });



}


/* For PopOver Traits*/
$(document).ready(function(){
    //FIX: will not owrk after load more on any ajax load of trait list panel
    $('.traitIcon').popover({
        'container':'body',
        'trigger':'hover',
        'html':true,
        'placement':'top',
        'delay': { 
        'show': "500", 
        'hide': "100"
        },
        'content':function(){
            return "<div style='width:150px;height:200px;'><img src='"+$(this).data('imageUrl')+"' width='150' height='150' /><p>"+$(this).data('trait')+"-"+$(this).data('traitvalue')+"</p></div>";
        }
    });
        
	$(document).on('click', '.editFact', function () {
        $(this).parent().parent().find('.row:first').hide();
        $(this).hide();
        $(this).parent().find('.submitFact, .cancelFact').show();
        $(this).parent().parent().find('.editFactPanel').show();
        return false;
	});

	$(document).on('click', '.cancelFact', function () {

        $(this).parent().parent().find('.editFactPanel').hide();
        $(this).parent().find('.submitFact, .cancelFact').hide();
        $(this).parent().parent().find('.row:first').show();
        $(this).hide();
        $(this).parent().find('.editFact').show();
        $(this).parent().parent().find('.alert').removeClass('alert alert-error').hide();
	});

    $(document).on('click', '.submitFact', function () {
        var $me = $(this);
        onSubmitFact($me, $me.data('objectid'), $me.data('objecttype'));
    });

});
