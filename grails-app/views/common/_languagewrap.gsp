
<g:if test="${domainInstance?.language?.id != userLanguage?.id}">
<a href="javascript:void(0);" class="clickcontent btn btn-mini">${domainInstance?.language?.threeLetterCode?.toUpperCase()}</a>
</g:if>

<div class="${(domainInstance?.language?.id != userLanguage?.id) ? 'hide' : ''}">
    <g:if test="${isHtml}">
    ${raw(contentValue)}
    </g:if>
    <g:else>
    ${contentValue}
    </g:else>
</div>	


