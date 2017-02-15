<%@page import="species.participation.Observation"%>
<%@page import="species.utils.Utils"%>
<style>
.profilepic{position:relative;}
.img-thumbnail{height:250px; max-width:250px; border:1px groove;margin-left:10px; border-color:#f2f2f2;}
.observation_story{height:554px;}
</style>

<div class="observation_story" style="overflow: auto;">

	<g:if test="${!showDetails }">
		<h5 class="ellipsis">
			<a
				href="${uGroup.createLink([action:"show", controller:"user", id:userInstance?.id, 'userGroup':userGroupInstance, 'userGroupWebaddress':userGroupWebaddress])}">
				${userInstance.name} </a>
		</h5>
	</g:if>

	<div class="pull-left" style="padding-bottom: 0px;width:100%;">
    <g:if test="${showDetails}">
	<div class="profilepic pull-right">
 <a
                        href="${uGroup.createLink(action:'show', controller:'user', id:userInstance?.id, 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}">
                        <img class="img-thumbnail" src="${userInstance.profilePicture()}" /> </a>                 

</div>
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
				<div class="prop email">
					<span class="name"> <i class="icon-envelope"></i> <g:message
							code="suser.email.label" /> </span>
					<div class="value">

						<a href="mailto:${fieldValue(bean: userInstance, field: 'email')}">
							${fieldValue(bean: userInstance, field: "email")} </a>
					</div>
				</div>
			</sUser:ifOwnsOrIsPublic>

		</g:if>
		
			<div class="prop" style="max-height:80px; min-height:26px; overflow-y:auto;">
				<span class="name"><i class="icon-map-marker"></i><g:message code="default.location.label" /></span>
				<div class="value">
        <g:if test="${userInstance.location}">
					${userInstance.location}
          </g:if>
            <g:else>
            ${"Not Provided"}
            </g:else>
				</div>
			</div>
		

		 <div class="prop">
                        <span class="name"><i class="icon-time"></i><g:message code="default.member.since.label" /> </span>
                        <div class="value">
                            <g:formatDate format="dd/MM/yyyy" date="${userInstance.dateCreated}"
                                type="datetime" style="MEDIUM" />
                        </div>
                    </div>
                            <g:if test="${userInstance.lastLoginDate}">
                        <div class="prop">
                            <span class="name"><i class="icon-time"></i><g:message code="default.last.visited.label" /></span>
                            <div class="value">
                                
                                    <g:formatDate format="dd/MM/yyyy" date="${userInstance.lastLoginDate}"
                                        type="datetime" style="MEDIUM" />
                            
                            </div>
                        </div>
                    </g:if>
                    
            <div class="prop">
                <span class="name"><i class="icon-road"></i><g:message code="text.website" /></span>
               <div class="value pre-scrollable" style="display:block;height:20px;overflow-y: auto;">
                    <div class="linktext pull-left">
                     <g:if test="${userInstance.website}">
                        ${userInstance.website}
                        </g:if>
                         <g:else>
                          ${"Not Provided"}
                        </g:else>
                    </div>
                </div>
            </div>
        
                    <div class="prop">
                        <span class="name" style="width: 150px;"> <g:message code="suser.show.intrested.species" /> &amp; <g:message code="default.habitats.label" /></span>
                        <div class="value" style="display:block;height:65px;">
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
       <div class="prop">
                                <span class="name"><i class="icon-user"></i><g:message code="default.about.me.label" /></span>
                                <div class="value pre-scrollable" id="aboutMe" style="display:block;max-height:78px;min-height:78px;overflow-y: auto;">
                                            <g:if test="${userInstance.aboutMe}">

                                            <%  def styleVar = 'block';
                                                def clickcontentVar = '' 
                                            %> 
                                            <g:if test="${userInstance?.language?.id != userLanguage?.id}">
                                                <%  
                                                  styleVar = "none"
                                                  clickcontentVar = '<a href="javascript:void(0);" class="clickcontent btn btn-mini">'+userInstance?.language?.threeLetterCode.toUpperCase()+'</a>';
                                                %>
                                            </g:if>

                                            ${raw(clickcontentVar)}
                                            <div style="display:${styleVar}">
                                                ${raw(userInstance.aboutMe.replace('\n', '<br/>\n'))}
                                            </div>
                                                
                                            </g:if>
                                </div>
                            </div>
        <g:if test="${!showDetails }">
            <obv:getStats model="['user':userInstance, 'userGroup':userGroupInstance]"/>
        </g:if>
        </div>
</div>



