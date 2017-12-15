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

