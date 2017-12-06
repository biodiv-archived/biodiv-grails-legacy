<div class="btn-group btn-group-vertical" style="border:solid 1px lightgray;width:100%;">
<g:each in="${dataTableTypes}" var="${dataTableType}">
    <li><button class="btn" style="width:100%" data-id="${dataTableType.ordinal()}" onclick="onDataTableClick(event, ${dataTableType.ordinal()}, ${datasetInstanceId}, ${dataTableInstanceId});return false;">Add ${dataTableType.value()}</button></li>
</g:each>
</div>

