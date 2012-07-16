
 
<div id="childList${speciesFieldInstance.id}">
    <g:each var="reference" in="${speciesFieldInstance?.references?}" status="i">
        <g:render template='/common/editReferenceTemplate' model="['referenceInstance':reference,'i':speciesFieldInstance.id+'_'+i, 'hidden':false]"/>
    </g:each>
</div>
<input type="button" value="Add Reference" onClick="addReference(${speciesFieldInstance?.references?.size()} + 0, '${speciesFieldInstance?.id}');" />