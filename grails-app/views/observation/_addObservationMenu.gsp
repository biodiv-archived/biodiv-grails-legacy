
<g:if test="${entityName}">
<div class="page-header clearfix">
    <div style="width: 100%;">
        <div class="main_heading navbar" style="margin: 0px;">
            <h1 style="font-size:20px;">
                <g:if test="${(params.action=='create' || params.action == 'save' || params.action == 'bulkCreate')}">
                <ul class="nav">
                    <li class="${(params.controller == 'observation' && (params.action == 'create' || params.action == 'save'))?'active':''}"><a
                        href="${uGroup.createLink(
                        controller:'observation', action:'create', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}"
                        > <g:message code="link.add.observation" /></a>
                    </li>
                    <li class="divider-vertical"></li>
                    <li class="${(params.controller == 'observation' && params.action == 'bulkCreate')?'active':''}"><a
                        href="${uGroup.createLink(
                        controller:'observation', action:'bulkCreate', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}"
                        > <g:message code="link.add.multiple" /></a>
                    </li>
                    <li class="divider-vertical"></li>
                    <li class="${params.controller == 'checklist'?'active':''}"><a
                        href="${uGroup.createLink(
                        controller:'checklist', action:'create', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}"
                        > <g:message code="link.add.list" /></a></li>
                </ul>
                </g:if>
                <g:else>
                    ${entityName}
                </g:else>
            </h1>
        </div>
    </div>
    </g:if>
    <g:hasErrors bean="${observationInstance}">
    <i class="icon-warning-sign"></i>
    <span class="label label-important"> <g:message
        code="fix.errors.before.proceeding" default="Fix errors" /> </span>
    <%--<g:renderErrors bean="${observationInstance}" as="list" />--%>
    </g:hasErrors>
</div>


