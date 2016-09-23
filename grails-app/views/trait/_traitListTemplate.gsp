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
.trait_label{ margin-left:50px;position:absolute;margin-top:10px; word-wrap: break-word;float:left;}
</style>
     <div class="observations_list pre-scrollable" style="clear: both;overflow-x:hidden;">
     <g:each in="${traitInstanceList}" var="traits" status="i">
    <div class="traitName row-fluid">
    	<h6>${traits.key.name}</h6>

	    <div class="traitValue">
	    	<g:render template="/trait/traitValueListTemplate" model="['traitValueInstanceList':traits.value,'rows':4,'traitName':traits.key.name]"/>
	    </div>
    </div>
    </g:each>
    </div>
 


