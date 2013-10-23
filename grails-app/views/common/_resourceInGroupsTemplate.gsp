<%@page import="species.groups.UserGroup"%>
<%@page import="species.participation.Featured"%>

<div class="resource_in_groups">
    <%
    ug = new UserGroup(name:grailsApplication.config.speciesPortal.app.siteName);
    observationInstance.userGroups.add(ug)
    %>
    <g:if test="${observationInstance.userGroups}">
    <h5>Groups</h5>
    <ul class="tile" style="list-style: none;background:transparent;">
        <g:each in="${observationInstance.userGroups}" var="userGroup">
        <g:set var="featured" value="${Boolean.FALSE}"/>
        <g:if test="${observationInstance}">
            <g:set var="featuredNotes" value="${Featured.isFeaturedInGroup(observationInstance, userGroup.id)}"/>
            <g:if test="${featuredNotes != ''}">
                <g:set var="featured" value="${Boolean.TRUE}"/>
            </g:if>
        </g:if>

        <li class="pull-left checkbox ${featured?'featured':''}" title ="${featured? 'Why featured : ' + featuredNotes : ''}">
        <uGroup:showUserGroupSignature
        model="[ 'userGroup':userGroup]" />
        </li>
        </g:each>
    </ul>
    </g:if>
</div>

