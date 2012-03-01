<g:javascript type="text/javascript"> 
$(document).ready(function() {
	$('#carousel_${id}').jcarousel({
        itemLoadCallback : itemLoadCallback,
        url:"${createLink(controller:controller, action:action, id:observationInstance.id)}"
	});
    
});
</g:javascript> 
<div class="grid_5 observation_story">
  	
  <div id="carousel_${id}" class="jcarousel-skin-ie7"> 
    <ul> 
      <!-- The content will be dynamically loaded in here --> 
    </ul> 
  </div> 
</div>
