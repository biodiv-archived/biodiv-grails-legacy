<%@page import="species.groups.UserGroup"%>
<%@page import="species.participation.Featured"%>

<div class="resource_in_groups">
    <%
    ug = new UserGroup(id:0, name:grailsApplication.config.speciesPortal.app.siteName);
    observationInstance.userGroups.add(ug)
    def featuredInUserGroups = [:];
    %>
    <g:if test="${observationInstance.userGroups}">

    <g:set var="featuredNotes" value="${observationInstance.featuredNotes()}"/>
    <ul class="tile" style="list-style: none;background:transparent;">
        <g:each in="${featuredNotes}" var="featuredNotesItem">
        <li class="reco_block" style="clear:both;overflow:auto;margin-bottom:12px;">
        <div class="pull-left">
        <g:if test="${featuredNotesItem.userGroup}">
        <%featuredInUserGroups.put(featuredNotesItem.userGroup.id, true)%>
        <uGroup:showUserGroupSignature model="[ 'userGroup':featuredNotesItem.userGroup, featured:true]" />
        </g:if>
        <g:else>
        <uGroup:showUserGroupSignature model="[ 'userGroup':ug, featured:true]" />
        <%featuredInUserGroups.put(0, true)%>
        </g:else>
        </div>
        <div class="featured_notes linktext">
            <div style="clear:both;">
                <b> <small>Featured on <b>${featuredNotesItem.createdOn.format('MMMMM dd, yyyy')}</b> as : </small></b>
                ${featuredNotesItem.notes}
            </div>
        </div>
        </li>

        </g:each>
        <g:each in="${observationInstance.userGroups}" var="userGroup">
        <g:if test="${!featuredInUserGroups.get(userGroup.id)}">
        <!--data-title ="${featured? 'Featured : ' :''}" data-content="${featured? featuredNotes :''}"-->
        <li class="pull-left reco_block"  style="margin-bottom:12px;list-style:none;">
        <uGroup:showUserGroupSignature model="[ 'userGroup':userGroup, featured:false]" />
        </li>
        </g:if>
        </g:each>
    </ul>
    </g:if>
</div>


