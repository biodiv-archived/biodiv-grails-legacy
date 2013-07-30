<div class="pull-right btn-group">
    <span class="name" style="color: #b1b1b1;font-size:14px;vertical-align:middle;">Add: </span>
    <g:if test="${!(params.controller=='observation' && params.action=='create')}">
    <a class="btn btn-link"
        href="${uGroup.createLink(
        controller:'observation', action:'create', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}"
        > <i class="icon-plus"></i>Single Observation</a>
    </g:if>
    <g:if test="${!(params.controller=='checklist' && params.action=='create')}">
    <a class="btn btn-link"
        href="${uGroup.createLink(
        controller:'checklist', action:'create', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}"
        > <i class="icon-plus"></i>Checklist</a>
    </g:if>

</div>


