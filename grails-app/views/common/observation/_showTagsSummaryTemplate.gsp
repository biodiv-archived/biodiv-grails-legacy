<div style="height: 26px;">
	<i class="icon-tags"></i> <g:message code="default.tags.label" />
	<div class="btn btn-small pull-right btn-primary add_obv_tags" style="margin-right: 16px;">Add Tag</div>
</div>
<div class="view_tags view_obv_tags" style="margin-bottom:25px;">		               
		<ul class="tagit tagitAppend">
		<g:if test="${tags}"> 
			<g:each in="${tags.entrySet()}">
				<li class="tagit-choice" style="padding:0 5px;">
					${it.getKey()} <span class="tag_stats"> ${it.getValue()}</span>
				</li>
			</g:each>
		</g:if>
		</ul>
</div>



<div class="add_obv_tags_wrapper" style="background-color: #a6dfc8;padding: 10px;display:none;" >
<div class="block sidebar-section">
        <h5>
            <label>
                <i class="icon-tags"></i>
                <g:message code="default.tags.label" /> 
                <small>
                    <g:message code="observation.tags.message" default="" />
                </small>
            </label>
        </h5>
        <div class="create_tags section-item" style="clear: both;">
        <form id="addOpenTags"  name="addOpenTags"                              
                                method="GET">
            <input type="hidden" name="observationId" value="${observationInstance.id}"/> 
            <ul id="tags" class="obvCreateTags" rel="${g.message(code:'placeholder.add.tags')}">
                <g:each in="${tags.entrySet()}" var="tag">
                <li>${tag.getKey()}</li>
                </g:each>
            </ul>
            
                <input type="submit" class="btn btn-small btn-success save_open_tags" value="Submit" />
                <div class="btn btn-small btn-danger cancel_open_tags" >Cancel</div>
            </form>
        </div>
    </div>
</div>


<script type="text/javascript">

$(document).ready(function(){
// For Open Tag

        $('.add_obv_tags').click(function(){
            $('.view_obv_tags, .add_obv_tags').hide();
            $('.add_obv_tags').parent().hide();
            $('.add_obv_tags_wrapper').show();
        });

        $('.cancel_open_tags').click(function(){
            $('.view_obv_tags, .add_obv_tags').show();
            $('.add_obv_tags').parent().show();
            $('.add_obv_tags_wrapper').hide();
        });

         $('#addOpenTags').bind('submit', function(event) {

                 $(this).ajaxSubmit({ 
                    url: "${uGroup.createLink(controller:'observation', action:'updateOraddTags')}",
                    dataType: 'json', 
                    type: 'GET',                
                    success: function(data, statusText, xhr, form) {
                        console.log("data "+data +" statusText = "+statusText+" xhr = "+xhr+" form = "+form);
                        console.log(data);
                        var tagsData = data;
                        if(tagsData.success){
                            var outHtml = '';
                            console.log(tagsData.hasOwnProperty("model"));
                            //console.log(Object.keys(data.model).length);
                            if(tagsData.hasOwnProperty("model")){
                                if(Object.keys(data.model).length > 0){
                                    $.each(data.model, function( index, value ) {
                                        outHtml+= '<li class="tagit-choice" style="padding:0 5px;">';
                                        outHtml+= index;
                                        outHtml+= '&nbsp;<span class="tag_stats">'+value +'</span>';
                                        outHtml+= '</li>';
                                    });
                                    $('.tagitAppend').html(outHtml);                        
                                    $('.view_obv_tags, .add_obv_tags').show();
                                    $('.add_obv_tags_wrapper').hide();                                    
                                }
                            }else{
                                $('.tagitAppend').empty();
                                $('.view_obv_tags, .add_obv_tags_wrapper').hide();
                                $('.add_obv_tags').show();
                            }
                            updateFeeds();
                        }
                        return false;
                    },
                    error:function (xhr, ajaxOptions, thrownError){
                        //successHandler is used when ajax login succedes
                        var successHandler = this.success, errorHandler = showUpdateStatus;
                        handleError(xhr, ajaxOptions, thrownError, successHandler, errorHandler);
                    } 

                 });    
               
            event.preventDefault(); 
        });
        $(".obvCreateTags").tagit({
            select:true, 
            allowSpaces:true, 
            placeholderText:$(".obvCreateTags").attr('rel'),//'Add some tags',
            fieldName: 'tags', 
            autocomplete:{
                source: '/observation/tags'
            }, 
            triggerKeys:['enter', 'comma', 'tab'], 
            maxLength:30
        });
});

</script>
