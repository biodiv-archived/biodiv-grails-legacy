<link rel="stylesheet" type="text/css" media="all"
	href="${resource(dir:'js/jquery/jquery.jcarousel-0.2.8/themes/classic/',file:'skin.css', absolute:true)}" />

<g:javascript src="jquery/jquery.jcarousel-0.2.8/jquery.jcarousel.js"
	base="${grailsApplication.config.grails.serverURL+'/js/'}" />

<g:javascript src="species/carousel.js"
	base="${grailsApplication.config.grails.serverURL+'/js/'}" />

<g:javascript type="text/javascript"> 
$(document).ready(function() {
	$('#carousel_${id}').jcarousel({
        itemLoadCallback : itemLoadCallback,
        url:"${createLink(controller:controller, action:action, id:observationId)}",
        filterProperty:"${filterProperty}",
        filterPropertyValue:"${filterPropertyValue}"
	});
    
});
</g:javascript> 
<div class="grid_5 observation_story" style="clear:both">
  	
  <div id="carousel_${id}" class="jcarousel-skin-ie7"> 
    <ul> 
      <!-- The content will be dynamically loaded in here --> 
    </ul> 
  </div> 
</div>
