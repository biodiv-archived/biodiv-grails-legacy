<%@page import="species.groups.UserGroup"%>
<%@page import="species.participation.Featured"%>
<%@page import="species.participation.Observation"%>

<% def observationUserGroups = observationInstance.userGroups;
    if(observationInstance.hasProperty('sourceId')){
//        if(observationInstance.sourceId && observationInstance.id != observationInstance.sourceId){
//            observationUserGroups.addAll(Observation.read(observationInstance.sourceId).userGroups);
//        }
    }
%>
<div class="resource_in_groups prop" rel="${isList}" style="display:${( observationUserGroups.size()==0 && isList) ? 'none;': 'block;' }">
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
            <li class="reco_block pull-left" style="clear:both;overflow:auto;margin-bottom:12px;">
            <div class="">
            <g:if test="${featuredNotesItem.userGroup}">
                <%featuredInUserGroups.put(featuredNotesItem.userGroup.id, true)%>
                <uGroup:showUserGroupSignature model="[ 'userGroup':featuredNotesItem.userGroup, featured:true]" />
            </g:if>
            <g:else>
                <uGroup:showUserGroupSignature model="[ 'userGroup':ug, featured:true]" />
                <%featuredInUserGroups.put(0L, true)%>
            </g:else>
            </div>
            <g:if test="${!isList}">
            <div class="featured_notes linktext">
                <div style="clear:both;">
                    <b> <small><g:message code="text.featured.on" />  <b>${featuredNotesItem.createdOn.format('MMMMM dd, yyyy')}</b> <g:message code="text.as" /> </small></b>
                    ${featuredNotesItem.notes}
                </div>
            </div>
            </g:if>
            </li>

        </g:each>
            <g:if test="${!featuredInUserGroups.containsKey(ug.id) && !isList}">
                    <li class="pull-left reco_block"  style="margin-bottom:12px;list-style:none;">
                    <uGroup:showUserGroupSignature model="[ 'userGroup':ug, featured:false]" />
            </li>
            </g:if>
        <g:each in="${observationUserGroups}" var="userGroup">
            <g:if test="${!featuredInUserGroups.containsKey(userGroup.id)}">
            <li class="pull-left reco_block"  style="margin-bottom:12px;list-style:none;">
            <uGroup:showUserGroupSignature model="[ 'userGroup':userGroup, featured:false]" />
            </li>
            </g:if>
        </g:each>       
    </ul>
</div>


