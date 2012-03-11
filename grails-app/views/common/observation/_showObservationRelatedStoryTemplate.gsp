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
<style>
.jcarousel-skin-ie7 .jcarousel-container-horizontal {
    padding: 0 40px;
}
.jcarousel-skin-ie7 .jcarousel-container {
    background: none;
    border:0;            
}
.jcarousel-skin-ie7 .jcarousel-prev-horizontal {
    background: url("${resource(dir:'images',file:'arrow-left-right.png', absolute:true)}") no-repeat scroll 0 0 transparent;
    cursor: pointer;
    height: 75px;
    position: absolute;
    top: 0px;
    width: 17px;
    opacity:0.7;       
}
.jcarousel-skin-ie7 .jcarousel-prev-horizontal:focus, .jcarousel-skin-ie7 .jcarousel-prev-horizontal:hover{
    background-position: 0 0;            
    opacity:1;       
}
.jcarousel-skin-ie7 .jcarousel-next-horizontal {
    background: url("${resource(dir:'images',file:'arrow-left-right.png', absolute:true)}") no-repeat scroll 0 0 transparent;
    background-position: -17px 0;            
    cursor: pointer;
    height: 75px;
    position: absolute;
    top: 0px;
    width: 17px;
    opacity:0.7;       
}
.jcarousel-skin-ie7 .jcarousel-next-horizontal:focus, .jcarousel-skin-ie7 .jcarousel-next-horizontal:hover{
    background-position: -17px 0;            
    opacity:1;       
}
.jcarousel-skin-ie7 .jcarousel-prev-disabled-horizontal, .jcarousel-skin-ie7 .jcarousel-prev-disabled-horizontal:hover, .jcarousel-skin-ie7 .jcarousel-prev-disabled-horizontal:focus, .jcarousel-skin-ie7 .jcarousel-prev-disabled-horizontal:active {
    opacity: 0.2;
}
.jcarousel-skin-ie7 .jcarousel-next-disabled-horizontal, .jcarousel-skin-ie7 .jcarousel-next-disabled-horizontal:hover, .jcarousel-skin-ie7 .jcarousel-next-disabled-horizontal:focus, .jcarousel-skin-ie7 .jcarousel-next-disabled-horizontal:active {
    opacity: 0.2;
}
.jcarousel-skin-ie7 .jcarousel-container-horizontal {
    padding: 0 25px;
}
.title {
    font-weight:bold;
}
</style>
<div class="grid_5 related_observation" style="clear:both">
  <span class="title">Other observations of the same species</span>	
  <div id="carousel_${id}" class="jcarousel-skin-ie7"> 
    <ul> 
      <!-- The content will be dynamically loaded in here --> 
    </ul> 
  </div> 
</div>
