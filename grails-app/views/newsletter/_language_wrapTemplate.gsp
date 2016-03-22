<%@page import="utils.Newsletter"%>
<% def language = (newsletterInstance?.language)?:userLanguage%>
<input type="hidden" name="language" value="${language?.id}" />
<input type="hidden" name="parentId" class="inp_parentId" value="${newsletterInstance?.parentId}" />

<tr class="prop">
	<td valign="top"
		class="value ${hasErrors(bean: newsletterInstance, field: 'language', 'errors')}">
<% 
	def c = Newsletter.createCriteria();
	def newsLetters = c.list{
		eq('parentId',0)
		and{			
			eq('language',language)
			order "id", "desc"
			if(params.userGroup){
				eq("userGroup",params.userGroup)
			}else{
				isNull("userGroup")
			}
		}
	}

%>
<label class="checkbox" style="text-align: left;"> 
	<g:checkBox	style="margin-left:0px;" name="parentCheckbox"  class="newsl_parent" checked="${(newsletterInstance?.parentId == 0)?'checked':''}" /> Parent
</label>
<label class="checkbox" style="text-align: left;"> 
	<g:checkBox	style="margin-left:0px;" name="subParentCheckbox" class="newsl_subparent" checked="${(newsletterInstance?.parentId != 0)?'checked':''}" /> Subpage of 

<select class="newsl_subp_selection" >
<g:each in="${newsLetters}" var="newsLetter">
	<option class="testi" value="${newsLetter.id}" ${(newsletterInstance?.parentId == newsLetter.id)? "selected":""}>${newsLetter.title}</option>
</g:each>
</select>
</label>
</td>
</tr>

