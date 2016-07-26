<div id="identifications" class="section" style="clear:both;">
<div class=" jcarousel-skin-ie7">
<div style="clear: both; position: relative; display: block;" class="jcarousel-container jcarousel-container-horizontal" id="carousel_user">
    	<div class="jcarousel-clip jcarousel-clip-horizontal" style="position: relative;">
    	<ul style="list-style: outside none none; width: 1742px; margin: 0px; overflow: hidden; position: relative; top: 0px; padding: 0px; left: 0px;" class="jcarousel-list jcarousel-list-horizontal">
    	<g:each in="${contributedSpecies}" var="relatedInstanceDetails">
        <li class="jcarousel-item jcarousel-item-horizontal jcarousel-item-1 jcarousel-item-1-horizontal" style="float: left; list-style: outside none none; overflow: hidden; width: 75px;" jcarouselindex="1">
        <div class="thumbnail">
        <div class="observation_th snippet tablet">
        <div class="figure">
        <a href="${uGroup.createLink(action:'show', controller:'species',id:relatedInstanceDetails.species)}">
        <g:if test="{relatedInstanceDetails.image!=null}">
        <img class="img-polaroid" alt="" title=<%=relatedInstanceDetails.description %> src=http://localhost.indiabiodiversity.org/biodiv/species/+<%= relatedInstanceDetails.image?.file_name %>>
        </g:if>
        </a>
        </div>
        </div>
        </div>        
        </li>
        </g:each>
         </ul>
         </div>
         <div class="jcarousel-prev jcarousel-prev-horizontal jcarousel-prev-disabled jcarousel-prev-disabled-horizontal" style="display: block;" ></div>
		 <div class="jcarousel-next jcarousel-next-horizontal jcarousel-next-disabled jcarousel-next-disabled-horizontal" style="display: block;" ></div>
         </div>
         </div>
         </div>