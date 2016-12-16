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
    console.log(url);
    var href = url.attr('path');
    var params = getFilterParameters(url);
    for(var key in params) {
        if(key.match('trait.')) {
            delete params[key];
        }
    }
    var History = window.History;
    var traits = getSelectedTrait($('.trait button, .trait .none, .trait .any'));
    for(var m in traits) {
        params['trait.'+m] = traits[m].substring(0,traits[m].length-1);
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
                    $.each(imagepath,function(index1,item1){ 
                        $('#imagediv_'+item[0]).append(showIcon(item1[0],item1[1],item1[2]));
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

function showIcon(value,name,url){
    return  '<img src="'+url+'" width="32" height="32" src="'+name+'-'+value+'" />';
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
            console.log(data);
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
                console.log(arguments);
                $me.parent().parent().find('.alert').removeClass('alert alert-info').addClass('alert alert-error').html(arguments.msg).show();
            }
            handleError(xhr, ajaxOptions, thrownError, successHandler, errorHandler);
        } 
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
            return "<div style='width:150px;height:150px;'><img src='"+$(this).data('imageUrl')+"' width='150' height='150' /></div>";
        }
    });
        
	$(document).on('click', '.editFact', function () {
        $(this).parent().parent().find('.row:first').hide();
        $(this).hide();
        $(this).parent().find('.submitFact, .cancelFact').show();
        $(this).parent().parent().find('.editFactPanel').show();
        console.log($(this).parent().parent().find('.editFactPanel'));
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
