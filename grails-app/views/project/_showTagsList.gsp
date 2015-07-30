<%@ page import="org.grails.taggable.Tag"%>
<%
def controller = controller?controller:'document'
def action = action?action:'browser'
 %>
<div class="view_tags view_obv_tags">
    <ul class="${controller}_tagit tagit tagitAppend">
	<g:if test="${instance?.tags}">        
		<g:each in="${instance.tags}">
			<li class="tagit-choice" style="padding:0 5px;">
				${it}
			</li>
		</g:each>		
	</g:if>
    </ul>
</div>

<g:if test="${controller != 'project' && showDetails}">

<div class="add_obv_tags_wrapper" style="background-color: #a6dfc8;padding: 10px;display:none;" >
<div class="block sidebar-section">       
        <div class="create_tags section-item" style="clear: both;">
        <form id="addOpenTags"  name="addOpenTags"                              
                                method="GET">
            <input type="hidden" name="instanceId" value="${instance.id}"/> 
            <input type="hidden" name="domainObj" value="${controller}"/>
            <ul id="tags" class="obvCreateTags" rel="${g.message(code:'placeholder.add.tags')}">
                <g:each in="${instance.tags}">
                <li>${it}</li>
                </g:each>
            </ul>
            
                <input type="submit" class="btn btn-small btn-success save_open_tags" value="Submit" />
                <div class="btn btn-small btn-danger cancel_open_tags" >Cancel</div>
            </form>
        </div>
    </div>
</div>

</g:if>
	
<script type="text/javascript">
	$(document).ready(function() {
        var contRoller = "${controller}";
		if((${isAjaxLoad?:'false'} == 'false') || (!${isAjaxLoad?1:0})){
            $(".${controller}_tagit li.tagit-choice").click(function(){
			    var tg = $(this).contents().first().text();
	            window.location.href = "${uGroup.createLink(controller:controller, action: action, userGroupWebaddress:params.webaddress)}?tag="+tg ;
	        });
         }
<g:if test="${controller != 'project' && showDetails}">
         $("#tags").tagit({
        	select:true, 
        	allowSpaces:true, 
        	placeholderText:'${g.message(code:"placeholder.add.tags")}',
        	fieldName: 'tags', 
        	autocomplete:{
        		source: '/${controller}/tags'
        	}, 
        	triggerKeys:['enter', 'comma', 'tab'], 
        	maxLength:30
        });


        // For Open Tag

        $('.add_obv_tags').click(function(){
            $('.view_obv_tags, .add_obv_tags').hide();
            $('.add_obv_tags_wrapper').show();
        });

        $('.cancel_open_tags').click(function(){
            $('.view_obv_tags, .add_obv_tags').show();
            $('.add_obv_tags_wrapper').hide();
        });

         $('#addOpenTags').bind('submit', function(event) {

                 $(this).ajaxSubmit({ 
                    url: "${uGroup.createLink(controller:contRoller, action:'updateOraddTags')}",
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
</g:if>
	})
</script>

