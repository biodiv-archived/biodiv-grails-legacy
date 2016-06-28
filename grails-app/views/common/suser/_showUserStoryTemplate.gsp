<%@page import="species.participation.Observation"%>
<%@page import="species.utils.Utils"%>
<style>
.profilepic{position:relative;}
.img-thumbnail{height:250px; border:1px groove;margin-left:10px; border-color:#f2f2f2;}
</style>

<div class="observation_story" style="overflow: auto;">

	<g:if test="${!showDetails }">
		<h5 class="ellipsis">
			<a
				href="${uGroup.createLink([action:"show", controller:"user", id:userInstance.id, 'userGroup':userGroupInstance, 'userGroupWebaddress':userGroupWebaddress])}">
				${userInstance.name} </a>
		</h5>
	</g:if>

	<div class="pull-left" style="padding-bottom: 10px;width:100%;">
	<div class="profilepic pull-right">
 <a
                        href="${uGroup.createLink(action:'show', controller:'user', id:user.id, 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}">
                        <img class="img-thumbnail" src="${user.profilePicture()}" /> </a>                 

</div>
		<g:if test="${showDetails}">
			<div class="prop">
				<span class="name"><i class="icon-user"></i> <g:message
						code="suser.username.label" /> </span>
				<div class="value">
					${fieldValue(bean: userInstance, field: "username")}
				</div>
			</div>

			<div class="prop">
				<span class="name"><i class="icon-user"></i> <g:message
						code="suser.name.label" /> </span>
				<div class="value">
					${fieldValue(bean: userInstance, field: "name")}
				</div>
			</div>
			
                 <div class="prop">
                 <span class="name"><i class="icon-user"></i><g:message code="default.sex.label" /></span>
                  <div class="value">
                  <g:if test="${userInstance.sexType}">
                                   ${userInstance.sexType}  
                                   </g:if>
                                 <g:else>
                                   ${"Not Provided"}
                                   </g:else>                                   
                            </div>
                        </div>
                    
                   
                 <div class="prop">
                 <span class="name"><i class="icon-envelope"></i><g:message code="default.occupationtype.label" /></span>
                  <div class="value">
                  <g:if test="${userInstance.occupationType}">
                                   ${userInstance.occupationType}  
                                   </g:if>
                                 <g:else>
                                   ${"Not Provided"}
                                   </g:else>
                            </div>
                        </div>
                   

                 <div class="prop">
                 <span class="name"><i class="icon-user"></i><g:message code="default.institutiontype.label" /></span>
                  <div class="value">
                  			<g:if test="${userInstance.institutionType}">
                  			<% String institute= userInstance.institutionType;
                  				def institutiontype=institute.replace("_"," ");
                  			 %>
                  			${institutiontype}
                                   </g:if>
                                 <g:else>
                                   ${"Not Provided"}
                                   </g:else>
                            </div>
                        </div>
                   
			<sUser:ifOwnsOrIsPublic
				model="['user':userInstance, 'isPublic':!userInstance.hideEmailId]">
				<div class="prop">
					<span class="name"> <i class="icon-envelope"></i> <g:message
							code="suser.email.label" /> </span>
					<div class="value">

						<a href="mailto:${fieldValue(bean: userInstance, field: 'email')}">
							${fieldValue(bean: userInstance, field: "email")} </a>
					</div>
				</div>
			</sUser:ifOwnsOrIsPublic>

		</g:if>
		<g:if test="${userInstance.location}">
			<div class="prop">
				<span class="name"><i class="icon-map-marker"></i><g:message code="default.location.label" /></span>
				<div class="value">
					${userInstance.location}
				</div>
			</div>
		</g:if>

		 <div class="prop">
                        <span class="name"><i class="icon-time"></i><g:message code="default.member.since.label" /> </span>
                        <div class="value">
                            <g:formatDate format="dd/MM/yyyy" date="${userInstance.dateCreated}"
                                type="datetime" style="MEDIUM" />
                        </div>
                    </div>
                            <g:if test="${user.lastLoginDate}">
                        <div class="prop">
                            <span class="name"><i class="icon-time"></i><g:message code="default.last.visited.label" /></span>
                            <div class="value">
                                
                                    <g:formatDate format="dd/MM/yyyy" date="${user.lastLoginDate}"
                                        type="datetime" style="MEDIUM" />
                            
                            </div>
                        </div>
                    </g:if>
                     <g:if test="${userInstance.website}">
            <div class="prop">
                <span class="name"><i class="icon-road"></i><g:message code="text.website" /></span>
                <div class="value">
                    <div class="linktext pull-left">
                        ${userInstance.website}
                    </div>
                </div>
            </div>
        </g:if>
                    <div class="prop">
                        <span class="name" style="width: 150px;"> <g:message code="suser.show.intrested.species" /> &amp; <g:message code="default.habitats.label" /></span>
                        <div class="value">
                            <span style="float:left;"><sUser:interestedSpeciesGroups model="['userInstance':userInstance]" /></span>
                            <span style="float:left;"><sUser:interestedHabitats model="['userInstance':userInstance]" /></span>
                        </div>
                    </div>

		<g:if test="${!showDetails }">
			<div class="prop">
				<span class="name"><i class="icon-time"></i><g:message code="default.member.since.label" /> </span>
				<div class="value">
					<time class="timeago"
						datetime="${userInstance.dateCreated.getTime()}"></time>
				</div>
			</div>
			<g:if test="${userInstance.lastLoginDate}">
				<div class="prop">
					<span class="name"><i class="icon-time"></i><g:message code="default.last.visited.label" /> </span>
					<div class="value">
						<time class="timeago"
							datetime="${userInstance.lastLoginDate.getTime()}"></time>
					</div>
				</div>
			</g:if>
		</g:if>

	</div>
        <g:if test="${!showDetails }">
            <obv:getStats model="['user':userInstance, 'userGroup':userGroupInstance]"/>
        </g:if>
</div>





