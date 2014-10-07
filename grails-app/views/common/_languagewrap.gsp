    <g:if test="${domainInstance?.language?.id != userLanguage?.id}">
        <%  
          styleVar = "none"
          clickcontentVar = '<a href="javascript:void(0);" class="clickcontent btn btn-mini">'+domainInstance?.language?.twoLetterCode?.toUpperCase()+'</a>';
        %>
    </g:if>
        
        ${raw(clickcontentVar)}
    <div style="display:${styleVar}">
      <g:if test="${isHtml}">
		    ${raw(contentValue)}
      </g:if>
      <g:else>
        ${contentValue}
      </g:else>
	  </div>	
	

