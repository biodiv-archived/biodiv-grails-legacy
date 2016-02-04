<g:if test="${annotations.size() > 0 }">
<div class="annotationsWrapper">
		<table class="table table-hover"
            style="margin: 0px;table-layout:fixed;display:block;overflow-y:auto;${height?'height:'+height+'px;':''}">
			<tbody>
				<g:each in="${annotations}" status="i"
					var="annot">
                    <g:if test="${annot.value}">
					<tr>
						<td style="word-wrap:break-word;">
                            <g:if test="${annot.value instanceof Map && annot.value.url}">
                                <a href="${annot.value.url}">${annot.key.replaceAll("_", " ").capitalize()}</a>
                            </g:if>
                            <g:else>
							    ${annot.key.replaceAll("_", " ").capitalize()}
                            </g:else>
						</td>
						<td class="ellipsis multiline linktext" style="word-wrap:break-word;">
                            <g:if test="${annot.value instanceof Map}">
                                    ${annot.value.value}
                            </g:if>
                            <g:else>
							    ${annot.value}
                            </g:else>
						</td>
                    </tr>
                    </g:if>
				</g:each>
			</tbody>
		</table>
	</div>
</g:if>
