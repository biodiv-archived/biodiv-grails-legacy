<g:if test="${localeLanguages && !hideLanguages}">
<tr class="prop">
	<td valign="top"
		class="value ${hasErrors(bean: newsletterInstance, field: 'language', 'errors')}">
		<select name="language">
		<g:each in="${localeLanguages}" var="localeLanguage">
				<option value="${localeLanguage.id}" rel="${localeLanguage.id}" ${(newsletterInstance?.language?.id == localeLanguage.id) ? "selected" : ""}>${localeLanguage.name}</option>										
		</g:each>
		</select>								
	</td>
</tr>
</g:if>
<g:else>
<input type="hidden" name="language" value="${newsletterInstance?.language?.id}" />
</g:else>
<input type="hidden" name="parent" value="${(newsletterInstance?.parentId)?newsletterInstance?.parent_id:0}" />