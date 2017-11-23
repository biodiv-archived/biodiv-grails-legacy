
<g:if test="${entityName}">
<div class="page-header clearfix">
    <div style="width: 100%;">
        <div class="main_heading navbar" style="margin: 0px;">
            <h1 style="font-size:20px;">
                <g:if test="${(params.action=='create' || params.action == 'save' || params.action == 'upload')}">
                <ul class="nav">
                    <li class="${(params.controller == 'trait' && (params.action == 'create' || params.action == 'save'))?'active':''}"><a
                        href="${uGroup.createLink(
                        controller:'trait', action:'create', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}"
                        > <g:message code="link.add.trait" /></a>
                    </li>
                    <li class="divider-vertical"></li>
                    <li class="${(params.controller == 'trait' && params.action == 'upload')?'active':''}"><a
                        href="${uGroup.createLink(
                        controller:'trait', action:'upload', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}"
                        > <g:message code="link.upload.trait" /></a>
                    </li>
                </ul>
                </g:if>
                <g:else>
                    ${entityName}
                </g:else>
            </h1>
        </div>
    </div>
</div>
</g:if>


