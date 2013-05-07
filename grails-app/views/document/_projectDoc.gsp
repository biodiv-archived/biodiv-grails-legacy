
<%@page import="species.License"%>
<table class="ufile-block span8">
	<thead></thead>
	<tbody>
		<tr>
			<td><input name="${docId}.uFile.path" type="hidden"
				value='${filePath}' />
				 <input name="${docId}.id" type="hidden"
				value='${docId}' /> 
				<input name="docs" type="hidden"
				value='${docId}' /> 
				
				<input name="${name}" type="hidden"
				value='${docId}' />
				
				 <input type="hidden" name='${docId}.deleted'
					value='false' /> 
					
				<input type="hidden" name='${docId}.new'
					value="${documentInstance?'false':'true'}" />

				<div>
					<span class="qq-upload-file"><i class="icon-file"></i> ${docName}
					</span> <span class="qq-upload-size"> ${fileSize}
					</span>

				</div>

				<div class="control-group">
					<label class="control-label" for="name"> Title </label>

					<div class="controls">
						<input class="input-xlarge" type='text' name='${docId}.title'
							value='${docName}' placeholder="Name for the file" />

					</div>

				</div>

				<div class="control-group">
					<label class="control-label" for="description"> Description
					</label>
					<div class="controls">
						<textarea rows='5' name='${docId}.description'
							value='${documentInstance?.description}'
							placeholder="Describe the file"> </textarea>
					</div>


				</div>

 <label class="control-label" for='tags'> <i
					class="icon-tags"></i>Tags
			</label>
				<div class="controls">
					<ul class='file-tags' id="${docId}-tags" name="${docId}.tags">
						<g:if test='${documentInstance}'>
							<g:each in="${documentInstance.tags}" var="tag">
								<li>
									${tag}
								</li>
							</g:each>
						</g:if>
					</ul>
				</div> 
			

				<div
					class="control-group ${hasErrors(bean: documentInstance, field: 'contributors', 'error')}">
					<label class="control-label" for="contributors">Contributor(s)</label>
					<div class="controls">
						<g:textField name="${docId}.contributors"
							value="${documentInstance?.contributors }" />
					</div>
				</div>



				<div
					class="control-group ${hasErrors(bean: documentInstance, field: 'attribution', 'error')}">
					<label class="control-label" for="attribution">Attribution</label>
					<div class="controls">
						<g:textField name="${docId}.attribution"
							value="${documentInstance?.attribution}" />
					</div>
				</div> <label class="control-label" for="License"> License </label>

				<div id="${docId}.license" class="licence_div dropdown">

					<a id="selected_license_${docId}"
						class="btn dropdown-toggle btn-mini" data-toggle="dropdown"> <img
						src="${documentInstance?.license?documentInstance.license.name.getIconFilename()+'.png':resource(dir:'images/license',file:'cc_by.png', absolute:true)}"
						title="Set a license for this file" /> <b class="caret"></b>
					</a>

					<ul id="license_options_${docId}"
						class="controls dropdown-menu license_options">
						<span>Choose a license</span>
						<g:each in="${License.list()}" var="l">
							<li class="license_option"
								onclick="$('#license_${docId}').val($.trim($(this).text()));$('#selected_license_${docId}').find('img:first').replaceWith($(this).html());">
								<img
								src="${resource(dir:'images/license',file:l?.name?.getIconFilename()+'.png', absolute:true)}" /><span
								style="display: none;"> ${l?.name?.value}
							</span>
							</li>
						</g:each>
					</ul>

				</div></td>
			<td><span class="del-document"> <img
					src="${resource(dir:'images/skin', file:'close.png')}"
					style="vertical-align: middle;" /></td>
		</tr>
	</tbody>
</table>