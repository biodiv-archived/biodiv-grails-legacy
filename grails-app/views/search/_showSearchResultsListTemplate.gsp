<%@page import="species.utils.Utils"%>
<%@page import="species.*"%>
<%@page import="species.participation.*"%>
<%@page import="species.groups.*"%>
<%@page import="species.auth.SUser"%>
<%@page import="content.eml.Document"%>

            <g:set var="modules"  value="[All:[name:'All',displayName:g.message(code:'default.all.label') ], Species:[name:'Species', template:'species',displayName:g.message(code:'default.species.label')], Observation:[name:'Observation', template:'observation',displayName:g.message(code:'observation.label')], Document:[name:'Document', template:'document',displayName:g.message(code:'feature.part.document')], SUser:[name:'SUser', template:'SUser',displayName:g.message(code:'search.suser')], UserGroup:[name:'UserGroup', template:'userGroup',displayName:g.message(code:'userGroup.label')], Resource:[name:'Resource', template:'resource',displayName:g.message(code:'resource.label')], Checklists:[name:'Checklists', template:'checklists',displayName:g.message(code:'checklists.label')]]"/>

<div class="observations_list observation" style="top: 0px;">

    <div class="mainContentList">
        <div class="mainContent"  name="l${params?.offset}">
            <ul class="list_view single_list_view thumbnails">
                <g:each in="${instanceList}" status="i" var="instance">
                <li class="thumbnail feedParentContext clearfix">

                <g:set var="className" value="${org.hibernate.Hibernate.getClass(instance.instance).getSimpleName()}"/>
                <b>${modules[className].displayName}</b>
                <g:if test="${className == Species.simpleName}">
                <s:showSnippet model="['speciesInstance':instance.instance]" />
                    </g:if>
                    <g:elseif test="${className == Observation.simpleName || className == Checklists.simpleName}">
                    <obv:showSnippet model="['observationInstance':instance.instance]"></obv:showSnippet>
                    </g:elseif>
                    <g:elseif test="${className == Document.simpleName}">
                    <g:render template="/document/showDocumentSnippetTemplate" model="['documentInstance':instance.instance, showPDFViewer:false]"/>
                    </g:elseif>
                    <g:elseif test="${className == SUser.simpleName}">
                    <g:render template="/common/suser/showUserSnippetTemplate" model="['userInstance':instance.instance]"/>
                    </g:elseif>
                    <g:elseif test="${className == UserGroup.simpleName}">
                    <table class="table">
                        <tr class="mainContentList">
                            <g:render template="/common/userGroup/showUserGroupSnippetTemplate" model="['userGroupInstance':instance.instance, showJoin:false, showLeave:false]"/>
                        </tr>
                    </table>
                    </g:elseif>
                    <g:elseif test="${className == Resource.simpleName}">
                    <g:render template="/resource/showResourceSnippetTemplate" model="['resourceInstance':instance.instance, containers:instance.containers]"/>
                    </g:elseif>
                    <g:else>
                    ${instance.instance} ${className}
                    </g:else>
                    </li>
                    </g:each>
                </ul>
            </div>
        </div>
        <g:if test="${instanceTotal > (queryParams.max?:0)}">
        <div class="centered">
            <div class="btn loadMore">
                <span class="progress" style="display: none;">${g.message(code:'msg.loading')} </span> <span
                    class="buttonTitle">${g.message(code:'msg.load.more')}</span>
            </div>
        </div>
        </g:if>
        <%
        activeFilters?.loadMore = true
        activeFilters?.webaddress = userGroup?.webaddress
        %>
        <div class="paginateButtons" style="visibility: hidden; clear: both">

            <center>
                <p:paginate total="${instanceTotal?:0}" action="${params.action}"
                controller="${params.controller?:'species'}"
                userGroup="${userGroup}"
                userGroupWebaddress="${userGroupWebaddress}"
                max="${queryParams.max}" params="${activeFilters}" />
            </center>
        </div>
    </div>


