<div class="row-fluid tab_accept" style="border: 1px solid #ccc;">
  <div class="span10 tab_accept_inner"><label>Name</label></div>  
  <div class="span2 tab_accept_inner"><label>Action</label></div>
</div>
<g:set var="typeClass" value="${type}id"/>
<g:each in="${1..4}">
<div class="row-fluid tab_div singleRow">
 <form class="tab_form">
  <div class="span10"> 
      <input type="hidden" class = "${typeClass}" name="${typeClass}" value=""/>
            <input type="text" class="nameInputs span12" name="value">
  </div>  
  <div class="span2">
        <button class="btn btn-mini btn-primary" onClick='modifySynonym(this);' rel="add"><i class="icon-ok icon-white"></i></button>
        <button class="btn btn-mini" onClick='modifySynonym(this);' rel='delete'><i class="icon-remove"></i></button>
  </div>
 </form> 
</div>
</g:each>


<button class="btn btn-success btn-mini add_new_row">Add Row</button>

