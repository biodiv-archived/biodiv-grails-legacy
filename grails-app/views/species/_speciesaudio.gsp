<%@page import="species.Resource"%>
<%@page import="species.Resource.ResourceType"%>
<% def audioResource = 0 
   def audioCount    = 0 
   def isOtherResource = 0
%>   
<g:if test="${params.controller == 'observation' }" > 
   <% resourcesInstanceList = resourceInstance.listResourcesByRating()      
      resourcesServerURL = grailsApplication.config.speciesPortal.observations.serverURL
    %>
</g:if>
<g:elseif test="${params.controller == 'species'}"> 
   <% resourcesInstanceList = resourceInstance.getListResources() 
      resourcesServerURL = grailsApplication.config.speciesPortal.resources.serverURL
    %>
</g:elseif>


<g:each in="${resourcesInstanceList}" var="r">


<g:if test="${r.type == ResourceType.AUDIO}">                                                                    
    <% audioCount = audioCount +1 %>
</g:if>
<g:else>                                                                    
    <% isOtherResource = isOtherResource +1 %>
</g:else>

</g:each>

<g:if test="${ isOtherResource == 0}">

    <style type="text/css">
            .noTitle{
                display:none !important;
            }

    </style>    

</g:if>

 <g:if test="${audioCount >=2 }" > 
        <ul id="playlist" style="padding: 5px 0px 2px 0px;margin: 0px;">
            <% def tempVar = 0 %>
            <g:each in="${resourcesInstanceList}" var="r">

                <g:if test="${r.type == ResourceType.AUDIO}">
                	<%  tempVar = tempVar + 1 %>                                        
                    <li class="active" style="display: inline;">
                        <a href="${createLinkTo(file: r.fileName, base:resourcesServerURL)}" class="btn btn-small btn-success" rel="${tempVar}"  >Audio ${tempVar}</a>
                    </li>
                </g:if>
            </g:each>
        </ul>    
</g:if>

<g:if test="${audioCount >= 1}"> 
<g:each in="${resourcesInstanceList}" var="r">                            
        <g:if test="${r.type == ResourceType.AUDIO}">
        <g:if test="${audioResource == 0}" >                                               
            
                <audio controls style="padding: 8px 0px;width: 100%;">                                              
                  <source src="${createLinkTo(file: r.fileName, base:resourcesServerURL)}" type="audio/mpeg">
                    Your browser does not support the audio element.
                </audio>            	             
        </g:if>
    		<% audioResource += 1; %>
    		<div class="audio_metadata_wrapper audio_metadata_${audioResource}" 
    			style="background-color: rgb(235, 235, 235);padding: 10px;
    			<g:if test="${audioResource > 1}">
    				display:none;	
    			</g:if>"
    		>
             <g:imageAttribution model="['resource':r, base:resourcesServerURL]" />
            </div>
        </g:if>
</g:each>
</g:if>

<g:if test="${audioCount > 1}" > 

         <br><br><br>

</g:if>
<g:if test="${audioCount >= 1}"> 
<g:javascript>


$(document).ready(function(){
        var audio;
        var playlist;
        var tracks;
        var current;

        init();
        function init(){
            current = 0;
            audio = $('audio');
            playlist = $('#playlist');
            tracks = playlist.find('li a');
            len = tracks.length - 1;
            audio[0].volume = .10;
            //audio[0].play();
            playlist.find('a').click(function(e){
                e.preventDefault();
                link = $(this);                
                $('.audio_metadata_wrapper').hide();
                $('.audio_metadata_'+$(this).attr('rel')).show();
                current = link.parent().index();
                run(link, audio[0]);    
            });
            audio[0].addEventListener('ended',function(e){
                current++;
                if(current == len){
                    current = 0;
                    link = playlist.find('a')[0];
                }else{
                    link = playlist.find('a')[current];    
                }
                run($(link),audio[0]);
            });
        }
        function run(link, player){
                player.src = link.attr('href');
                par = link.parent();
                par.addClass('active').siblings().removeClass('active');
                audio[0].load();
                audio[0].play();
        }
    });    
 </g:javascript>
 </g:if>