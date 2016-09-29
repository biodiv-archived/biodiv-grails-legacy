<style type="text/css">
.div_fir{
    margin: 5px 0px 0px 0px;
}
.trait_btn{

    margin: 0;
    padding: 0;
    height: 40px;

}
.observations_list{max-height:410px;}
.svg_wrap{
    width: 50px;
    float: left;
}
.trait_label{width: 90px;margin-left:50px;position:absolute;margin-top:10px; word-wrap: break-word;float:left;}
.traitFilter{  
    border: 1px solid #ccc;
    padding: 5px;
 }
 .traitFilter h6{
    margin:0px;
    line-height: 12px;
 }
 .traitFilter .span2{
    height:36px;
 }
 .ellipsis_trait {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  display: block;
}
</style>
     <div class="observations_list pre-scrollable" style="clear: both;overflow-x:hidden;">
     <g:each in="${traitInstanceList}" var="traits" status="i">
    <div class="traitName row-fluid">
    	<h6><a href="${uGroup.createLink(action:'show', controller:'trait', id:traits.key.id)}">${traits.key.name}</a></h6>

	    <div class="traitValue">
	    	<g:render template="/trait/traitValueListTemplate" model="['traitValueInstanceList':traits.value,'rows':4,'traitName':traits.key.name,'hasLabel':false]"/>
	    </div>
    </div>
    </g:each>
    </div>
 


