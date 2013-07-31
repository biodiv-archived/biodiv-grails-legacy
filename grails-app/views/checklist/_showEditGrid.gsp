<r:script>
$(document).ready(function(){
    var columns = ${params.checklistColumns ?: 'undefined'} 
    var data = ${params.checklistData ?: 'undefined'}
    if(data && columns) {
        data = eval(data);
        columns = eval(columns);
        loadDataToGrid(data, columns, '${params.sciNameColumn}', '${params.commonNameColumn}');
    } else {
    	loadGrid("${uGroup.createLink(controller:'checklist', action:'getObservationGrid')}", "${observationInstance.id}");
    }
});
</r:script>
