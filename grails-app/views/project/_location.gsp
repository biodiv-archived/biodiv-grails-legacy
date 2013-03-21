<div id="location${i}" class="location-div" <g:if test="${hidden}">style="display:none;"</g:if>>
    <g:hiddenField name='locationsList[${i}].id' value='${location?.id}'/>
    <g:hiddenField name='locationsList[${i}].deleted' value='false'/>
        <g:hiddenField name='locationsList[${i}].new' value="${location?.id == null?'true':'false'}"/>

    <g:textField name='locationsList[${i}].number' value='${location?.number}' />
    <g:select name="locationsList[${i}].type"
        from="${blog.omarello.location.locationType.values()}"
        optionKey="key"
        optionValue="value"
        value = "${location?.type?.key}"/>

    <span class="del-location">
        <img src="${resource(dir:'images/skin', file:'icon_delete.png')}"
            style="vertical-align:middle;"/>
    </span>
</div>
