<g:if test="${annotations.size() > 0 }">
	<div>
		<table class="table table-hover"
			style="margin-left: 0px;table-layout:fixed;display:block;height:300px;overflow-y:auto;">
			<tbody class="mainContentList">
				<g:each in="${annotations}" status="i"
					var="annot">
                    <g:if test="${annot.value}">
					<tr class="mainContent">
						<td style="word-wrap:break-word;">
							${annot.key.replaceAll("_", " ").capitalize()}
						</td>
						<td class="ellipsis multiline" style="word-wrap:break-word;">
                            <g:if test="${annot.value instanceof Map}">
                                <span class="linktext">
                                    ${annot.value.value}
                                </span>
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
