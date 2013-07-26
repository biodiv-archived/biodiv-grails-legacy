 <r:script>
      $(document).ready(function(){
			loadGrid("${uGroup.createLink(controller:'checklist', action:'getObservationGrid')}", "${observationInstance.id}");
      });
</r:script>