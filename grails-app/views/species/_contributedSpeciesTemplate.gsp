<%@page import="species.utils.ImageType"%>
<div id="identifications" class="section" style="clear:both;">
<div class=" jcarousel-skin-ie7">
<div style="clear: both; position: relative; display: block;" class="jcarousel-container jcarousel-container-horizontal" id="carousel_user">
    	<div class="jcarousel-clip jcarousel-clip-horizontal" style="position: relative;">
    	<ul style="list-style: outside none none; width: 1742px; margin: 0px; overflow: hidden; position: relative; top: 0px; padding: 0px; left: 0px;" class="jcarousel-list jcarousel-list-horizontal">
    	<g:each in="${contributedSpecies}" var="relatedInstanceDetails">
        <g:set var="mainImage" value="${relatedInstanceDetails.mainImage()}" />
<%
    def imagePath = '';
    def speciesGroupIcon =  relatedInstanceDetails.fetchSpeciesGroup().icon(ImageType.ORIGINAL)
    if(mainImage?.fileName == speciesGroupIcon.fileName) { 
        imagePath = mainImage.thumbnailUrl(null, '.png');
    } else
        imagePath = mainImage?mainImage.thumbnailUrl():null;
        def obvId = relatedInstanceDetails.id
        %>
    
        <li class="jcarousel-item jcarousel-item-horizontal jcarousel-item-1 jcarousel-item-1-horizontal" style="float: left; list-style: outside none none; overflow: hidden; width: 75px;" jcarouselindex="1">
        <div class="thumbnail">
        <div class="observation_th snippet tablet">
        <div class="figure"
        title='${relatedInstanceDetails.title.replaceAll("</i>","").replaceAll("<i>","").replaceAll(">","")}>'>
                <g:link url="${uGroup.createLink(controller:'species', action:'show', id:obvId) }">
                <g:if test="${imagePath}">
                <span class="img-polaroid" style="background-image:url('${imagePath}');">
                </span>
                </g:if>
                <g:else>
                <span class="img-polaroid" title="${g.message(code:'showobservationsnippet.title.contribute')}" style="background-image:url(${createLinkTo( file:"no-image.jpg", base:grailsApplication.config.speciesPortal.resources.serverURL)});">
                </g:else>
        </g:link>
    </div>
        </div>
        </div>        
        </li>
        </g:each>
         </ul>
         </div>
         <div class="jcarousel-prev jcarousel-prev-horizontal" style="display: block;" ></div>
		 <div class="jcarousel-next jcarousel-next-horizontal" style="display: block;" ></div>
         </div>
         </div>
         </div>