<%@page import="species.Resource"%>
<%@page import="species.Resource.ResourceType"%>
<% def audioResource = 0 
   def audioCount    = 0 
%>   


<g:each in="${speciesInstance.getListResources()}" var="r">


 <g:if test="${r.type == ResourceType.IMAGE}">
        <%  def basePath 
            if(r.context.value() == Resource.ResourceContext.OBSERVATION.toString()){
                basePath = grailsApplication.config.speciesPortal.observations.serverURL
            }
            else if(r.context.value() == Resource.ResourceContext.SPECIES.toString() || r.context.value() == Resource.ResourceContext.SPECIES_FIELD.toString()){
                basePath = grailsApplication.config.speciesPortal.resources.serverURL
            }
        %>
        <%def gallImagePath = r.fileName.trim().replaceFirst(/\.[a-zA-Z]{3,4}$/, grailsApplication.config.speciesPortal.resources.images.gallery.suffix)%>
        <%def gallThumbImagePath = r.fileName.trim().replaceFirst(/\.[a-zA-Z]{3,4}$/, grailsApplication.config.speciesPortal.resources.images.galleryThumbnail.suffix)%>
        <a target="_blank"
            rel="${createLinkTo(file: r.fileName.trim(), base:basePath)}"
            href="${createLinkTo(file: gallImagePath, base:basePath)}">
            <img class="galleryImage"
            src="${createLinkTo(file: gallThumbImagePath, base:basePath)}"

            data-original="${createLinkTo(file: r.fileName.trim(), base:basePath)}" 
            /> </a>

        <g:imageAttribution model="['resource':r, base:basePath]" />
}
</g:if>

<g:elseif test="${r.type == ResourceType.VIDEO}">
<% isaudioResource = 1 %>
    <a href="${r.url }"><span class="video galleryImage">Watch this at YouTube</span></a>
    <g:imageAttribution model="['resource':r]" />
</g:elseif>
<g:elseif test="${r.type == ResourceType.AUDIO}">                                                                    
    <% audioCount = audioCount +1 %>
</g:elseif>


</g:each>




<g:if test="${isaudioResource == 0}" >
                    
    <style type="text/css">

            #gallery1{
                display:none !important;
            }
            .noTitle{
                display:none !important;
            }
    </style>
    

</g:if>            

<g:if test="${audioCount >=2 }" > 
        <ul id="playlist" style="padding: 5px 0px 2px 0px;margin: 0px;">
            <% def tempVar = 0 %>
            <g:each in="${speciesInstance.getListResources()}" var="r">
                <g:if test="${r.type == ResourceType.AUDIO}">
                    <%  tempVar = tempVar + 1 %>                                        
                    <li class="active" style="display: inline;">
                        <a href="${createLinkTo(file: r.fileName, base:grailsApplication.config.speciesPortal.observations.serverURL)}" class="btn btn-small btn-success" >Audio ${tempVar}</a>
                    </li>
                </g:if>
            </g:each>
        </ul>    
</g:if>

<g:if test="${audioCount >= 1}"> 
<g:each in="${speciesInstance.getListResources()}" var="r">                            
        <g:if test="${r.type == ResourceType.AUDIO}">
        <g:if test="${audioResource == 0}" >                                               
            <% audioResource = 1; %>
                <audio controls style="float: right;padding: 8px 0px;width: 100%;">                                              
                  <source src="${createLinkTo(file: r.fileName, base:grailsApplication.config.speciesPortal.observations.serverURL)}" type="audio/mpeg">
                    Your browser does not support the audio element.
                </audio>
            <g:if test="${isaudioResource == 0}" >
                 <g:imageAttribution model="['resource':r, base:grailsApplication.config.speciesPortal.observations.serverURL]" />
                   <%  isaudioResource = isaudioResource+1 %>
            </g:if>            
        </g:if>
        </g:if>
</g:each>
</g:if>

<g:if test="${audioCount > 1}" > 

         <br><br><br>

</g:if>


