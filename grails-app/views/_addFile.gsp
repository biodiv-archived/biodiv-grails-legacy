<div id="ufile${i}" class="ufile-div" <g:if test="${hidden}">style="display:none;"</g:if>>
    <g:hiddenField name='ufilesList[${i}].id' value='${uFile?.id}'/>
    <g:hiddenField name='ufilesList[${i}].deleted' value='false'/>
        <g:hiddenField name='ufilesList[${i}].new' value="${ufile?.id == null?'true':'false'}"/>

			<div class='file-field'>
				<label for="file"> File </label>
				<input type='file' name='file' />
			</div>
			<div class='field'>
				<div class='name-field'>
					<label for="name"> Name </label>
					<input type='text' name='name'/>
				</div>
				<div class='tags-field'>
					<label for='tags'> Tags </label>
					<ul class='file-tags' name='tags'><li></li></ul>
				</div>
			</div>
			<div class='field desc-field'>
				<label for="description"> Description </label>
				<textarea rows='5' columns='10' name='description'> </textarea>
			</div>			
		
    <span class="del-ufile">
        <img src="${resource(dir:'images/skin', file:'icon_delete.png')}"
            style="vertical-align:middle;"/>
    </span>
</div>