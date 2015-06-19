<%@page import="species.Language"%>

<div class="row-fluid tab_accept" style="border: 1px solid #ccc;">
  <div class="span4 tab_accept_inner"><label>Name</label></div>
  <div class="span2 tab_accept_inner"><label>Lang</label></div>
  <div class="span2 tab_accept_inner"><label>Source</label></div>
  <div class="span2 tab_accept_inner"><label>Contributor</label></div>
  <div class="span2 tab_accept_inner"><label>Action</label></div>
</div>
<g:set var="typeClass" value="${type}id"/>
<g:each in="${1..4}">
<div class="row-fluid tab_div singleRow">
    <span class="tab_form">
        <div class="span4"> 
            <input type="hidden" class = "${typeClass}" name="${typeClass}" value=""/>
            <input type="text" class="nameInputs span12" name="value">
        </div>
        <div class="span2">
            <select class="languageDropDown span12" >
                <g:each in="${Language.list(sort: 'name', order: 'asc')}" var="lang">
                <g:if test="${lang.name == 'English'}">
                    <option value="${lang.name}" selected>${lang.name}</option>
                </g:if>
                <g:else>
                    <option value="${lang.name}">${lang.name}</option>
                </g:else> 
                </g:each>
            </select>
        </div>
        <div class="span2"><input type="text" class="nameInputs span12" name="source"></div>
        <div class="span2"><input type="text" class="nameInputs span12" name="contributor"></div>
        <div class="span2" style="text-align:center;">
            <button class="btn btn-mini btn-primary addEdit  disabled" onClick='modifyContent(this, "${type}");' rel="add"><i class="icon-ok icon-white"></i></button>
            <button class="btn btn-mini delete disabled" onClick='modifyContent(this, "${type}");' rel='delete'><i class="icon-remove"></i></button>
        </div>
    </span> 
</div>
</g:each>


<button class="btn btn-success btn-mini add_new_row">Add Row</button>

