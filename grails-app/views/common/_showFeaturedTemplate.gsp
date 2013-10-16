<%@page import="species.participation.Featured"%>
<%@page import="species.participation.Observation"%>
<%@page import="species.groups.UserGroup"%>

<div class="is-featured">
    <g:set var="featuredInGroups" value="${Featured.isFeaturedIn(observationInstance)}">
    </g:set>
    <g:if test="${featuredInGroups}">
    <div class="sidebar_section ">
        <h5>Featured in Groups</h5>
        <div class="featured-groups-name">
            <ul class="tile" style="list-style: none; padding-left: 10px;">
                <g:each in="${featuredInGroups}" var="groupInfo">
                <%
                def ug
                if(groupInfo.userGroup == null){
                ug = new UserGroup(name:grailsApplication.config.speciesPortal.app.siteName);
                }
                else {
                ug = groupInfo.userGroup    
                }
                %>
                <li class="pull-left checkbox" title ="Why Featured : ${groupInfo.notes}" class=""><uGroup:showUserGroupSignature
                model="[ 'userGroup':ug]" /></li>

                </g:each> 
            </ul>
        </div>
    </div>
    </g:if>
</div>
