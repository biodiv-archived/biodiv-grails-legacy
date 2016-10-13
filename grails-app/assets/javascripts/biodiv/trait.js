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
    $('.trait button, .trait .none, .trait .any').each(function() {
        if($(this).hasClass('active')) {
            params['trait.'+$(this).attr('data-tid')] = $(this).attr('data-tvid');
        }
    });
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
                    itemMap.type='species';
                    var snippetTabletHtml = getSnippetTabletHTML(undefined, itemMap);
                    $matchingSpeciesTable.append('<tr class="jcarousel-item jcarousel-item-horizontal"><td>'+snippetTabletHtml+'</td><td><a href='+item[4]+'>'+item[1]+'</a></td></tr>');  
                });
                $me.data('offset', data.model.next);
                if(!data.model.next){
                    $me.hide();
                }
            }
           
    }
    });
}

/* For PopOver Traits*/
$(document).ready(function(){
$('.traitIcon').popover({
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
});