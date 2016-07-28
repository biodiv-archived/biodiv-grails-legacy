function updateDistinctRecoTable(){
	$('#distinctRecoTable tbody').empty();
	var me = $('#distinctRecoTableAction');
	$(me).show();
	$(me).data('offset', 0);
	$(me).click();

}
    function updateDistinctIdentifiedRecoTable(){
    $('#distinctIdentifiedRecoTable tbody').empty();
    var me = $('#distinctRecoIdentifiedTableAction');
    $(me).show();
    $(me).data('offset', 0);
    $(me).click();
}
function loadDistinctRecoList() {
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
            
            $('#distinctRecoList .distinctRecoHeading').html(data.model.totalRecoCount?(' (' + data.model.totalRecoCount + ')'):'');
            if(data.success == true && data.model.distinctRecoList && href.toLowerCase().indexOf("user/show") >= 0) {
                $.each(data.model.distinctRecoList, function(index, item) {
                    if(item[1])
                    $distinctRecoTable.append('<tr><td><i>'+item[0]+'</i></td><td>'+item[3]+'</td></tr>');  
                    else
                    $distinctRecoTable.append('<tr><td>'+item[0]+'</td><td>'+item[3]+'</td></tr>');
                });
                $me.data('offset', data.model.next);
                if(!data.model.next){
                    $me.hide();
                }
            } 
                if(data.success == true && data.model.distinctRecoList && href.toLowerCase().indexOf("user/show") <= 0) {
                $.each(data.model.distinctRecoList, function(index, item) {
                    if(item[1])
                    $distinctRecoTable.append('<tr><td><i>'+item[0]+'</i></td><td>'+item[2]+'</td></tr>');  
                    else
                    $distinctRecoTable.append('<tr><td>'+item[0]+'</td><td>'+item[2]+'</td></tr>');
                });
                $me.data('offset', data.model.next);
                if(!data.model.next){
                    $me.hide();
                }
            } 

            else {
                $me.hide();
            }
    }
    });
}
function loadDistinctIdentifiedRecoList() {
    var $me = $(this);
    var target = window.location.pathname + window.location.search;
    var a = $('<a href="'+target+'"></a>');

    var url = a.url();
    var href = url.attr('path');
    var params = getFilterParameters(url);
    params['max'] = $(this).data('max');
    params.identified=true;
    params['offset'] = $(this).data('offset');
    var $distinctIdentifiedRecoTable = $('#distinctIdentifiedRecoTable');
    $.ajax({
        url:window.params.observation.distinctIdentifiedRecoListUrl,
        dataType: "json",
        data:params,
        success: function(data) {
            
            $('#distinctRecoIdentifiedList .distinctIdentifiedRecoHeading').html(data.model.totalRecoCount?(' (' + data.model.totalRecoCount + ')'):'');
            if(data.success == true && data.model.distinctIdentifiedRecoList) {
                $.each(data.model.distinctIdentifiedRecoList, function(index, item) {
                    if(item[1])
                    $distinctIdentifiedRecoTable.append('<tr><td><i>'+item[0]+'</i></td><td>'+item[2]+'</td></tr>');  
                    else
                    $distinctIdentifiedRecoTable.append('<tr><td>'+item[0]+'</td><td>'+item[2]+'</td></tr>');
                });
                $me.data('offset', data.model.next);
                if(!data.model.next){
                    $me.hide();
                }
            } else {
                $me.hide();
            }
        }
    });
}
