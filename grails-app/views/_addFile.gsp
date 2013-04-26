<div id="ufile${i}" class="ufile-div" <g:if test="${hidden}">style="display:none;"</g:if>>
    <g:hiddenField name='${id}List[${i}].id' value='${uFile?.id}'/>
    <g:hiddenField name='${id}List[${i}].deleted' value='false'/>
        <g:hiddenField name='${id}List[${i}].new' value="${ufile?.id == null?'true':'false'}"/>

			<uploader:uploader id="uFileUploader${i}" >
			
			<g:textField name="${id}List[${i}].name" value='{uFile?.name}'/>
			<div class='field'>
				<div class='name-field'>
					<label for="ufilesList[${i}].name"> Name </label>
					<input type='text' name='ufilesList[${i}].name' value='{uFile?.name}'/>
				</div>
				<div class='tags-field'>
					<label for='ufilesList[${i}].tags'> Tags </label>
					<ul class='file-tags' name='ufilesList[${i}].tags'><li></li></ul>
				</div>
			</div>
			<div class='field desc-field'>
				<label for="description"> Description </label>
				<textarea rows='5' columns='10' name='ufilesList[${i}].description'> </textarea>
			</div>			
		
    <span class="del-ufile">
        <img src="${resource(dir:'images/skin', file:'icon_delete.png')}"
            style="vertical-align:middle;"/>
    </span>
</div>