<%@page import="species.Resource.ResourceType"%>
<g:set var="mainImage" value="${observationInstance.mainImage()}" />
<%
def imagePath = mainImage?mainImage.thumbnailUrl(null, observationInstance.isChecklist ? '.png' :null): null;
def controller = observationInstance.isChecklist ? 'checklist' :'observation'
def obvId = observationInstance.id
%>
<div class="snippet tablet">
	<div class="figure" style="height:100px;"
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
					title="You can contribute!!!" />
			</g:else>
                        <g:if test="${observationInstance.isChecklist}">
                        <div class="checklistCount">${observationInstance.speciesCount}</div>
                        </g:if>
		</g:link>
                <div class="mouseover" style="padding-left:0px;display:none;">
                    <h5>
                        <obv:showSpeciesName
                        model="['observationInstance':observationInstance, 'userGroup':userGroup, 'userGroupWebaddress':userGroupWebaddress,isListView:true]" />
                        <div class="user-icon pull-right">
                            <a href="${uGroup.createLink(controller:'SUser', action:'show', id:observationInstance.author.id, 'userGroupWebaddress':userGroup?userGroup.webaddress:userGroupWebaddress)}"> <img
                                src="${observationInstance.author.profilePicture()}" class="small_profile_pic"
                                title="${observationInstance.author.name}" /> </a>

                        </div>


                    </h5>
                </div>

	</div>
	<div class="caption" >
		<obv:showStoryTablet
			model="['observationInstance':observationInstance, 'userGroup':userGroup, 'userGroupWebaddress':userGroupWebaddress]"></obv:showStoryTablet>
	</div>
</div>
