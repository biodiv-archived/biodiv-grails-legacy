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
    console.log('sdsdfsdfsdfsdf');
    console.log(traits);
    console.log(traits);
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
                        $('#imagediv_'+item[0]).append(showIcon(item1));
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

function showIcon(url){
    return  '<img src="'+url+'" width="32" height="32" />';
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
