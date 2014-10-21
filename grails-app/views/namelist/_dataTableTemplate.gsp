<style type="text/css">

.tab_accept{
    border: 1px solid #ccc;
}
.tab_accept_inner{
    border-right: 1px solid #ccc;
    text-align: center;
}
.tab_div{
    border-bottom: 1px solid #ccc;
    padding:10px 0px 0px 0px;
}
</style>

<div class="row-fluid tab_accept" style="border: 1px solid #ccc;">
  <div class="span3 tab_accept_inner"><label>Name</label></div>
  <div class="span3 tab_accept_inner"><label>Source</label></div>
  <div class="span4 tab_accept_inner"><label>Contributor</label></div>
  <div class="span2 tab_accept_inner"><label>Action</label></div>
</div>
<div class="row-fluid tab_div">
 <form class="tab_form">
  <div class="span3"><input type="text" class="nameInputs span12" name="name"></div>
  <div class="span3"><input type="text" class="nameInputs span12" name="source"></div>
  <div class="span4"><input type="text" class="nameInputs span12" name="Contributor"></div>
  <div class="span2">
        <button class="btn btn-mini btn-primary" onClick='modifySynonym(this);' rel="add"><i class="icon-ok icon-white"></i></button>
        <button class="btn btn-mini" onClick='modifySynonym(this);' rel='delete'><i class="icon-remove"></i></button>
  </div>
 </form> 
</div>
<div class="row-fluid tab_div">
<form>
  <div class="span3"><input type="text" class="nameInputs span12" name="name"></div>
  <div class="span3"><input type="text" class="nameInputs span12" name="source"></div>
  <div class="span4"><input type="text" class="nameInputs span12" name="Contributor"></div>
  <div class="span2">
        <button class="btn btn-mini btn-primary" onClick='modifySynonym(this);' rel="add"><i class="icon-ok icon-white"></i></button>
        <button class="btn btn-mini" onClick='modifySynonym(this);' rel='delete'><i class="icon-remove"></i></button>
  </div>
</form>  
</div>

