<%@page import="species.utils.ImageType"%>
<%@page import="species.utils.Utils"%>
<%@page import="species.auth.SUser"%>
<%@page import="species.participation.Featured"%>
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
    style="margin-left: 0px;width:${showDetails?'auto':'200px;'}">
    <div class="snippet tablet"
        style="display: table; width:100%;height: ${showDetails ? '100px;':'40px;'}">

            <span class="badge ${featured?'featured':''}" style="display:inherit;" data-title ="${featured? 'Featured : ' :''}" data-content="${featured? featuredNotes :''}"> </span>
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
            <g:if test="${!showDetails}">
            <div class="pull-left">
                <i class="icon-user"></i>
                ${membersCount}
            </div>
            </g:if>
    </div>
</div>
