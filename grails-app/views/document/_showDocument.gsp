<div class="observation_story sidebar_section">
    <g:if test="${documentInstance.uFile || documentInstance.uri}">
    <div class="sidebar_section" style="margin-left: 0px;">

        <g:if test="${documentInstance.uFile}">

        <dl class="dl-horizontal">

            <dt>File</dt>
            <dd>

            <fileManager:displayFile
            filePath="${ documentInstance?.uFile?.path}"
            fileName="${ documentInstance?.uFile?.path}"></fileManager:displayFile>
            </dd>
        </dl>
        </g:if>
        <g:if test="${documentInstance.uri}">
        <dl class="dl-horizontal">

            <dt>URL</dt>
            <dd class="linktext">
            ${documentInstance.uri}
            </dd>
        </dl>
        </g:if>
    </div>
    </g:if>

    <div class="prop">
        <span class="name">Type</span>
        <div class="value">
            ${documentInstance.type?.value }
        </div>
    </div>

    <g:if test="${documentInstance?.description}">
    <div class="prop">
        <span class="name">Description</span>
        <div class="notes_view linktext value">
            ${documentInstance?.description}
        </div>
    </div>
    </g:if>
    <g:if test="${documentInstance?.contributors}">
    <div class="prop">
        <span class="name">Contributor(s)</span>
        <div class="value">
            ${documentInstance?.contributors}
        </div>
    </div>
    </g:if>
    <g:if test="${documentInstance?.attribution}">
    <div class="prop">
        <span class="name">Attribution</span>
        <div class="value">
            ${documentInstance?.attribution}
        </div>
    </div>
    </g:if>
    <g:if test="${documentInstance?.license}">
    <div class="prop">
        <span class="name">License</span>

        <div class="value"><img
            src="${resource(dir:'images/license',file:documentInstance?.license?.name.value().toLowerCase().replaceAll('\\s+','')+'.png', absolute:true)}"
            title="${documentInstance.license.name}" /></div>
    </div>
    </g:if>
</div>
