function deleteDataset(ele) {
    var me = $(ele);
    var id = me.data('id');
    console.log(id);
    $.ajax({
        url:window.params.dataset.deleteUrl,
        method:'POST',
        dataType: "json",
        data:{'id':id},
        success: function(data) {
            if(data.success) {
                $('#dataset_'+id).remove();          
            } else {
                alert(data.msg);
            }
        }
    });
}

function loadDataPackages(targetComp, url,offset,menuCall){	
	$(targetComp).html('');
	$.ajax({
 		url: url,
 		type: 'GET',
		dataType: "json",
		data: {"offset":offset},
		success: function(data) {
            for(var i=0; i<data.model.instanceList.length; i++) {
                $(targetComp).append("<li><a href='/dataset/list/?dataPackage="+data.model.instanceList[i].id+"'>"+data.model.instanceList[i].title+"</a></li>");
            }
			return false;
		}, error: function(xhr, status, error) {
			alert(xhr.responseText);
	   	}
	});
}
