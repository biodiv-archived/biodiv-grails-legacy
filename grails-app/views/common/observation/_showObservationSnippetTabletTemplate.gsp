<%@page import="species.Resource.ResourceType"%>
<g:set var="mainImage" value="${observationInstance?.mainImage()}" />
<%
def imagePath = mainImage?mainImage.thumbnailUrl(null, !observationInstance.resource ? '.png' :null): null;
def controller = observationInstance.isChecklist ? 'checklist' :'observation'
def obvId = observationInstance?.id
%>

<g:if test="${observationInstance}">
    <g:set var="featureCount" value="${observationInstance.featureCount}"/>
</g:if>
<div class="snippet tablet">
    <span class="badge ${(featureCount>0) ? 'featured':''}"  title="${(featureCount>0) ? g.message(code:'text.featured'):''}">
                </span>

    <div class="figure"
        title='<g:if test="${obvTitle != null}">${obvTitle}</g:if>'>
                <g:link url="${uGroup.createLink(controller:controller, action:'show', id:obvId, 'pos':pos, 'userGroup':userGroup, 'userGroupWebaddress':userGroupWebaddress) }" name="g${pos}">
                
                <g:if
				test="${imagePath}">
				<img class="img-polaroid" style=" ${observationInstance.isChecklist? 'opacity:0.7;' :''}"
					src="${imagePath}" />
			</g:if>
			<g:else>
				<img class="img-polaroid"
					src="${createLinkTo( file:"no-image.jpg", base:grailsApplication.config.speciesPortal.resources.serverURL)}"
					title="${g.message(code:'showobservationsnippet.title.contribute')}" />
			</g:else>
                        <g:if test="${observationInstance?.isChecklist}">
                        <div class="checklistCount">${observationInstance?.speciesCount}</div>
                        </g:if>
		</g:link>
                <!--div class="mouseover" style="padding-left:0px;">
                </div-->
             

	</div>

<% def photonames, inc =0; %>
<g:each in="${observationInstance.resource}" var="r">
   <g:if test="${r.type == ResourceType.IMAGE}">
        <%            
            if(inc == 0){
                photonames =r.fileName;
            }else{
                photonames+=","+r.fileName;
            }
            inc++;
            
        %>
   </g:if>
</g:each>
     <a href="javascript:void(0);" class="view_bootstrap_gallery" style="display:none;" rel="${observationInstance.id}" data-img="${photonames}">View gallery</a>
	<div class="caption species_title_wrapper" >
		<obv:showStoryTablet
			model="['observationInstance':observationInstance, 'userGroup':userGroup, 'userGroupWebaddress':userGroupWebaddress]"></obv:showStoryTablet>
		<uGroup:objectPost model="['objectInstance':observationInstance, 'userGroup':userGroup, canPullResource:canPullResource]" />
	</div>
</div>

<div class="showObvDetails" rel="${observationInstance.id}" style="display:none;">

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
                    <!-- <br /> Lat:
                    <g:formatNumber number="${observationInstance.latitude}"
                    type="number" maxFractionDigits="2" />
                    , Long:
                    <g:formatNumber number="${observationInstance.longitude}"
                    type="number" maxFractionDigits="2" />
                    -->
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


            <% def userLink = uGroup.createLink('controller':'user', action:'show', id:observationInstance.author.id,  userGroup:userGroup, 'userGroupWebaddress':userGroupWebaddress);%>
            <div class="prop">
                <div style="float:left;">
                    <div class="figure user-icon pull-left" style="display:table;height:32px;">
                        <a href="${userLink}"> 
                        <img src="${grailsApplication.config.speciesPortal.users.serverURL+'/'+observationInstance.author.icon}"
                        class="small_profile_pic pull-left" title="${observationInstance.author.name}" /></a>
                    </div>
                    <a title="${observationInstance.author.name}"
                        href="${userLink}">
                        <span> ${observationInstance.author.name}</span>
                    </a>
                </div>
                <div style="float:right">
                    <div style="float: left;">
                    <span class="habitat_icon_show group_icon habitats_sprites active urban_gall_th" title="Urban"></span>
                    </div>
                    <div class="group_icon_show_wrap" id="group_icon_show_wrap_${observationInstance.id}">
                        <span class="group_icon group_icon_show_${observationInstance.id} species_groups_sprites active ${observationInstance.group.iconClass()}" title="Plants"></span>
                        <div class="btn btn-small btn-primary edit_group_btn" id="${observationInstance.id}">Edit
                        </div>
                    </div>

                <div class="column propagateGrpHab" id="propagateGrpHab_${observationInstance.id}" style="display:none;">
                 <form id="updateSpeciesGrp"  name="updateSpeciesGrp"                              
                                method="GET">
                    <g:render template="/common/speciesGroupDropdownTemplate" model="['observationInstance':observationInstance]"/>
                    <input type="hidden" name="prev_group" value="${observationInstance?.group?.id}" />
                    <input type="hidden" name="observationId" value="${observationInstance?.id}"> 
                    <input type="submit" class="btn btn-small btn-primary save_group_btn" style="display:none;" value="Save" />
                </form>
                </div>



                </div>
              
            </div>
<div class="recommendations sidebar_section" style="  width: 97%;float: right;top: -35px;  margin-bottom: -35px;position: relative;">
<div>
    <ul id="recoSummary" class="pollBars recoSummary_${observationInstance.id}">

    </ul>
    <div id="seeMoreMessage_${observationInstance.id}" 
        class="message ${ (!observationInstance.isLocked) ? '': 'isLocked'}"></div>
    <div id="seeMore_${observationInstance.id}" class="btn btn-mini">
        <g:message code="button.show.all" />
    </div>
</div>
<g:if test="${!observationInstance.isLocked}">
<a href="javascript:void(0);" class="clickSuggest pull-right">Click to suggest</a>
<div class="input-append" style="width:98%; display:none;">
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

        <form id="addRecommendation" name="addRecommendation"
            action="${uGroup.createLink(controller:'observation', action:'addRecommendationVote')}"
            method="GET" class="form-horizontal addRecommendation addRecommendation_${observationInstance.id}">
            <div class="reco-input">
            <reco:create
                model="['recommendationInstance':recommendationInstance]" />
                <input type="hidden" name='obvId'
                        value="${observationInstance.id}" />
                
                 <input type="submit"
                        value="${g.message(code:'title.value.add')}" class="btn btn-primary btn-small pull-right" style="position: relative; border-radius:4px" />
            </div>
            
        </form>

</div>
</g:if>