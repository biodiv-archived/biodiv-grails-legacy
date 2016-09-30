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
            if(data.success == true && data.model.matchingSpeciesList) {
                $.each(data.model.matchingSpeciesList, function(index, item) {
                    $matchingSpeciesTable.append('<tr><td><a href='+item[3]+'>'+item[0]+'</a></td></tr>');  
                });
                $me.data('offset', data.model.next);
                if(!data.model.next){
                    $me.hide();
                }
            }
           
    }
    });
}

