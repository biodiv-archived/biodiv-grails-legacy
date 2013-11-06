<%@page import="species.utils.ImageType"%>
<%@page import="species.utils.Utils"%>
<%@page import="species.auth.SUser"%>
<%@page import="species.participation.Featured"%>
<%@page import="species.groups.UserGroup"%>
<%
def groupUrl =  g.createLink(url:Utils.getIBPServerDomain())
int membersCount =  0;
String userGroupName = userGroup?userGroup.name:grailsApplication.config.speciesPortal.app.siteName
if(userGroup && userGroup.id) {
    groupUrl = uGroup.createLink(mapping:'userGroup', controller:'userGroup', action:'show', 'userGroup':userGroup)
    membersCount = userGroup.getAllMembersCount();
} else {
    membersCount = SUser.count();
}

%>

<div class="thumbnail clearfix ${showDetails ? '' : 'signature'}"
    style="margin-left: 0px;width:${showDetails?'auto':'250px;'}">
    <div class="snippet tablet"
        style="display: table; width:100%;height: ${showDetails ? '100px;':'40px;'}">

            <span class="badge ${featured?'featured':''}" style="display:inherit;"  title="${(featureCount>0) ? 'Featured in this group':''}"> </span>
            <div class="figure pull-left" style="display: table; width:32px;height: ${showDetails ? '100px;':'40px;'};">
                <a
                    href="${groupUrl}">
                    <g:if test="${featured}">
                    </g:if>
                    <g:else>
                    <img
                    class="${showDetails ? 'normal_profile_pic' : 'user-icon small_profile_pic'}"
                    src="${userGroup.mainImage()?.fileName}" title="${userGroupName}"
                    alt="${userGroupName}" /> 
                    </g:else>
                </a>
            </div>

            <a 
                href="${groupUrl}">
                <span class="ellipsis  ${showDetails ? 'multiline' : ''}" style="display: block;text-align:left;${showDetails ? 'width:auto' : 'width:120px'};"
                    title="${userGroupName}"> ${userGroupName} </span> 
            </a>
            <g:if test="${!showDetails && userGroup && userGroup.id}">
            <div class="footer-item" title="Members">
                <i class="icon-user"></i>
                ${membersCount}
            </div>
            <div class="footer-item" title="Species">
                <i class="icon-leaf"></i>
                ${userGroup.noOfSpecies()}
            </div>
             <div class="footer-item" title="Observations">
                <i class="icon-screenshot"></i>
                ${userGroup.noOfObservations()}
            </div>
            <div class="footer-item" title="Documents">
                <i class="icon-file"></i>
                ${userGroup.noOfDocuments()}
            </div>
            </g:if>
    </div>
</div>
