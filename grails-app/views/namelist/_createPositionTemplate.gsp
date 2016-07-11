<div class="control-group">
    <label class="control-label ${isPopup?'span3':''}">Position
    
<i class="icon-question-sign" data-toggle="tooltip" data-trigger="hover" data-original-title="${g.message(code:'namelist.position.info')}"></i>
    </label>
    <select id="positionDropDown" name="position" class="position ${isPopup?'span3':''}">
        <option value="choosePosition">Choose Position</option>
        <g:each in="${NamePosition.list()}" var="t">
        <option value="${t.toString().toLowerCase()}">${t}</option>
        </g:each>
    </select>
</div>