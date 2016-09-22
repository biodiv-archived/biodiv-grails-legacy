<%@page import="species.Resource.ResourceType"%>
<%@page import="species.utils.ImageType"%>
<g:set var="mainImage" value="${observationInstance?.mainImage()}" />
<%
def imagePath = mainImage?mainImage.thumbnailUrl(null, !observationInstance.resource||observationInstance.dataset ? '.png' :null): null;
def controller = observationInstance.isChecklist ? 'checklist' :'observation'
def obvId = observationInstance?.id
%>
<g:if test="${observationInstance}">
    <g:set var="featureCount" value="${observationInstance.featureCount}"/>
</g:if>

<div class="snippet tablet ${styleviewcheck?'snippettablet': ''}" style="height:100%;">
    <g:if test="${featureCount}">
        <span class="badge ${(featureCount>0) ? 'featured':''}"  title="${(featureCount>0) ? g.message(code:'text.featured'):''}"></span>
    </g:if>

    <div class="figure"
        title='<g:if test="${obvTitle != null}">${obvTitle}</g:if>'>
                <g:link url="${uGroup.createLink(controller:controller, action:'show', id:obvId, 'pos':pos, 'userGroup':userGroup, 'userGroupWebaddress':userGroupWebaddress) }" name="g${pos}">
                
                <g:if
				test="${imagePath}">
				<span class="img-polaroid" style=" ${observationInstance.isChecklist? 'opacity:0.7;' :''} background-image:url('${imagePath}');">
                </span>
			</g:if>
			<g:else>                
                <span class="img-polaroid" title="${g.message(code:'showobservationsnippet.title.contribute')}" style="background-image:url(${createLinkTo( file:"no-image.jpg", base:grailsApplication.config.speciesPortal.resources.serverURL)});">
                </span>
			</g:else>
                        <g:if test="${observationInstance?.isChecklist}">
                        <div class="listtemplate_icon checklistCount">${observationInstance?.speciesCount}</div>
                        </g:if>
                        <g:elseif test="${observationInstance?.dataset}">
                        <div class="listtemplate_icon">
                        <img class="img-polaroid" title="${observationInstance.dataset.datasource.title}" src="${observationInstance.dataset.datasource.mainImage()?.fileName}"/>
                        </div>
                        </g:elseif>
		</g:link>
	</div>

<% def photonames ='', inc =0; %>
<g:each in="${observationInstance.resource}" var="r">
   <g:if test="${r.type == ResourceType.IMAGE}">
        <%   
            def resourceUrl = (r.fileName)?r.fileName:r.url;          
            if(inc == 0){
                photonames =resourceUrl;
            }else{
                photonames+=","+resourceUrl;
            }
            inc++;
            
        %>
   </g:if>
</g:each>
<g:if test="${photonames.trim()!=''}">
     <a href="javascript:void(0);" class="view_bootstrap_gallery" style="${styleviewcheck?'display:block;': 'display:none;'}" rel="${observationInstance.id}" data-img="${photonames}">${g.message(code:'default.instance.viewGallery')}<i class="icon-share icon-white"></i></a>
</g:if>     

	<div class="caption" style="${styleviewcheck?'height:20px;': 'height:50px;'}" >
		<obv:showStoryTablet
			model="['observationInstance':observationInstance, 'userGroup':userGroup, 'userGroupWebaddress':userGroupWebaddress]"></obv:showStoryTablet>
		<uGroup:objectPost model="['objectInstance':observationInstance, 'userGroup':userGroup, canPullResource:canPullResource]" />
	</div>
</div>

<div class="showObvDetails" rel="${observationInstance.id}" style="${styleviewcheck?'display:block;': 'display:none;'}">

    <div class="prop">
        <span class="name"><i class="icon-share-alt"></i><g:message code="default.name.label" /></span>
        <div class="value">
            <obv:showSpeciesName
            model="['observationInstance':observationInstance, 'userGroup':userGroup, 'userGroupWebaddress':userGroupWebaddress, 'isListView':!showDetails]" />
        </div>
    </div>
    <div class="prop">
        <span class="name"><i class="icon-map-marker"></i><g:message code="default.place.label" /></span>
        <div class="value ellipsis">
            <g:if test="${observationInstance.placeName == ''}">
            ${observationInstance.reverseGeocodedName}
            </g:if>
            <g:else>
            ${observationInstance.placeName}
            </g:else>
       </div>
    </div>

    <div class="prop">                
        <span class="name"><i class="icon-time"></i><g:message code="default.observed.on.label" /></span>
        <div class="value">
            <time class="timeago"
            datetime="${observationInstance.fromDate.getTime()}"></time>
            <g:if test="${observationInstance.toDate && observationInstance.fromDate != observationInstance.toDate}">&nbsp;
            <b>-</b>&nbsp; <time class="timeago" datetime="${observationInstance.toDate.getTime()}"></time>
            </g:if>
        </div>
    </div> 
    <div class="prop">                
        <span class="name"><i class="icon-time"></i><g:message code="default.submitted.on.label" /></span>
        <div class="value">
            <time class="timeago"
            datetime="${observationInstance.createdOn.getTime()}"></time>                    
        </div>
    </div>           
    <% def userLink = uGroup.createLink('controller':'user', action:'show', id:observationInstance.author.id,  userGroup:userGroup, 'userGroupWebaddress':userGroupWebaddress);
    def commonName = observationInstance.isChecklist ? observationInstance.title :observationInstance.fetchSuggestedCommonNames()%>
    <div class="prop bottom_user_fixed">
        <div class="user-icon pull-left" style="display:table;height:32px;">
            <a href="${userLink}"> 
                <img src="${observationInstance.author.profilePicture(ImageType.SMALL)}"
                class="small_profile_pic pull-left" title="${observationInstance.author.name}" /></a>
        </div>
        <div style="float:right">
            <div style="float: left;">
                <span class="habitat_icon_show group_icon habitats_sprites active ${observationInstance.habitat.iconClass()}" title="${observationInstance.habitat}"></span>

            </div>
            <div class="group_icon_show_wrap">
                <span class="group_icon group_icon_show species_groups_sprites active ${observationInstance.group.iconClass()} pull-left" title="Plants"></span>
                <div class="btn btn-small btn-primary edit_group_btn pull-left">${g.message(code:'default.button.edit.label')}
                </div>
            </div>

            <div class="propagateGrpHab group_icon_show_wrap" id="propagateGrpHab_${observationInstance.id}" style="display:none;">
                <form id="updateSpeciesGrp"  name="updateSpeciesGrp"                              
                    method="GET">
                    <g:render template="/common/speciesGroupDropdownTemplate" model="['observationInstance':observationInstance,'action':'show']"/>
                    <input type="hidden" name="prev_group" class="prev_group" value="${observationInstance?.group?.id}" />
                    <input type="hidden" name="observationId" value="${observationInstance?.id}"> 
                    <input type="submit" class="btn btn-small btn-primary save_group_btn" style="display:none;" value="Save" />
                </form>
            </div>
        </div>
    </div>            
</div>
    <div class="recommendations sidebar_section" style="margin-bottom:0px;padding:5px;text-align:center;position: relative; ${styleviewcheck? 'display:block;': 'display:none;'}">
        <div>
            <ul id="recoSummary" class="pollBars recoSummary_${observationInstance.id}">
              <g:if test="${recoVotes}">
               <g:render template="/common/observation/showObservationRecosTemplate" model ="['observationInstance':observationInstance, 'result':recoVotes.recoVotes, 'totalVotes':recoVotes.totalVotes, 'uniqueVotes':recoVotes.uniqueVotes, 'userGroupWebaddress':params.userGroupWebaddress]"/>
              </g:if>
            </ul>
             <g:if test="${observationInstance.isLocked}">
                  <div id="seeMoreMessage_${observationInstance.id}" class="alert alert-success isLocked">
                    <g:message code="species.validate.message" />
                  </div>
              </g:if>
              <g:else>
              <div id="seeMoreMessage_${observationInstance.id}" class="alert alert-info" style="display:none;"></div>                
              </g:else>
              <div id="seeMore_${observationInstance.id}" onclick="preLoadRecos(-1, 3, true,${observationInstance.id});" class="btn btn-mini" style="display:${recoVotes?.uniqueVotes>=3?'block':'none' };">
                  <g:message code="button.show.all" />
              </div>
        </div>
        <g:if test="${!observationInstance.isLocked}">
        <a href="javascript:void(0);" class="clickSuggest" style="display:block;" rel="${observationInstance.id}">${g.message(code:'default.reco.clickSuggest')}<i class="icon-chevron-down"></i></a>
        <div class="input-append" style="width:98%; display:none; height: 130px;">
            <g:hasErrors bean="${recommendationInstance}">
            <div class="errors">
                <g:renderErrors bean="${recommendationInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:hasErrors bean="${recommendationVoteInstance}">
            <div class="errors">
                <g:renderErrors bean="${recommendationVoteInstance}" as="list" />
            </div>
            </g:hasErrors>
            <div class="addRecommendation_wrap_place">
            </div>    

        </div>
        </g:if>
        <div>
           <uGroup:resourceInGroups model="['observationInstance':observationInstance,'isList':true]"  />   
        </div>
    </div>

