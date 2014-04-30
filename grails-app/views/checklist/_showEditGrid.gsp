<r:script>
$(document).ready(function(){
    var columns = ${checklistColumns ?: 'undefined'} 
    var data = ${checklistData ?: 'undefined'}
    if(data && columns) {
        data = eval(data);
        columns = eval(columns);
        loadDataToGrid(data, columns, 'checklist', '${sciNameColumn?:""}', '${commonNameColumn?:""}');
    } else {
    	loadGrid("${uGroup.createLink(controller:'checklist', action:'getObservationGrid')}", "${observationInstance.id}");
    }
});
</r:script>
