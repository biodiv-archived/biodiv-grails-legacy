<%@page import="species.groups.UserGroup"%>
<%@page import="species.participation.Featured"%>
<%@page import="species.participation.Observation"%>

<div class="resource_in_groups">
    <%
    ug = new UserGroup(name:grailsApplication.config.speciesPortal.app.siteName, icon:'/'+grailsApplication.config.speciesPortal.app.logo);
    ug.id = 0L;
    if(!observationInstance.userGroups) {
        observationInstance.userGroups = []
    }
    //observationInstance.userGroups.add(ug)
    def featuredInUserGroups = [:];
    %>

    <g:set var="featuredNotes" value="${observationInstance.featuredNotes()}"/>
    <ul class="tile" style="list-style: none;background:transparent;">
        <!-- groups where object is featured -->
        <g:each in="${featuredNotes}" var="featuredNotesItem">
            <li class="reco_block" style="clear:both;overflow:auto;margin-bottom:12px;">
            <div class="pull-left">
            <g:if test="${featuredNotesItem.userGroup}">
                <%featuredInUserGroups.put(featuredNotesItem.userGroup.id, true)%>
                <uGroup:showUserGroupSignature model="[ 'userGroup':featuredNotesItem.userGroup, featured:true]" />
            </g:if>
            <g:else>
                <uGroup:showUserGroupSignature model="[ 'userGroup':ug, featured:true]" />
                <%featuredInUserGroups.put(0L, true)%>
            </g:else>
            </div>
            <div class="featured_notes linktext">
                <div style="clear:both;">
                    <b> <small><g:message code="text.featured.on" />  <b>${featuredNotesItem.createdOn.format('MMMMM dd, yyyy')}</b> <g:message code="text.as" /> </small></b>
                    ${featuredNotesItem.notes}
                </div>
            </div>
            </li>

        </g:each>
            <g:if test="${!featuredInUserGroups.containsKey(ug.id)}">
            <li class="pull-left reco_block"  style="margin-bottom:12px;list-style:none;">
            <uGroup:showUserGroupSignature model="[ 'userGroup':ug, featured:false]" />
            </li>
            </g:if>
 
        <% def observationUserGroups = observationInstance.userGroups;
        if(observationInstance.hasProperty('sourceId')){
            if(observationInstance.id != observationInstance.sourceId){
                observationUserGroups.addAll(Observation.read(observationInstance.sourceId).userGroups);
            }
        }
        %>

        <g:each in="${observationUserGroups}" var="userGroup">
            <g:if test="${!featuredInUserGroups.containsKey(userGroup.id)}">
            <li class="pull-left reco_block"  style="margin-bottom:12px;list-style:none;">
            <uGroup:showUserGroupSignature model="[ 'userGroup':userGroup, featured:false]" />
            </li>
            </g:if>
        </g:each>       
    </ul>
</div>


