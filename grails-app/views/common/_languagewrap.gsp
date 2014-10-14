<g:if test="${domainInstance?.language?.id != userLanguage?.id}">
'There is content in another language. Please click here to read it. <a href="javascript:void(0);" class="clickcontent btn btn-mini">'+domainInstance?.language?.twoLetterCode?.toUpperCase()+'</a>';
</g:if>

<div class="${(domainInstance?.language?.id != userLanguage?.id) ? 'hide' : ''}">
    <g:if test="${isHtml}">
    ${raw(contentValue)}
    </g:if>
    <g:else>
    ${contentValue}
    </g:else>
</div>	


