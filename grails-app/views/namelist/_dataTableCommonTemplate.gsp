
<div class="row-fluid tab_accept" style="border: 1px solid #ccc;">
  <div class="span3 tab_accept_inner"><label>Name</label></div>
  <div class="span2 tab_accept_inner"><label>Lang</label></div>
  <div class="span3 tab_accept_inner"><label>Source</label></div>
  <div class="span2 tab_accept_inner"><label>Contributor</label></div>
  <div class="span2 tab_accept_inner"><label>Action</label></div>
</div>
<g:set var="typeClass" value="${type}id"/>
<g:each in="${1..4}">
<div class="row-fluid tab_div singleRow">
 <form class="tab_form">
  <div class="span3"> 
      <input type="hidden" class = "${typeClass}" name="${typeClass}" value=""/>
            <input type="text" class="nameInputs span12" name="value">
  </div>
<div class="nameContainer textbox" style="position:relative;">
  <div class="span2">
    <s:chooseLanguage />
    </div>
</div>
  <div class="span3"><input type="text" class="nameInputs span12" name="source"></div>
  <div class="span2"><input type="text" class="nameInputs span12" name="contributor"></div>
  <div class="span2">
        <button class="btn btn-mini btn-primary" onClick='modifyContent(this, "${type}");' rel="add"><i class="icon-ok icon-white"></i></button>
        <button class="btn btn-mini" onClick='modifyContent(this, "${type}");' rel='delete'><i class="icon-remove"></i></button>
  </div>
 </form> 
</div>
</g:each>


<button class="btn btn-success btn-mini add_new_row">Add Row</button>

