<%@page import="species.SpeciesField"%>
<%@page import="species.Field"%>
<%
def fieldInstance = Field.findByCategory(fieldFromName.references);
def results = [];
def sfInstance 	  = category.value.get('speciesFieldInstance');
def references = [];
if(sfInstance) {
/*
if(sfInstance.size() >0 ){
for (int i = 0; i < sfInstance.size(); i++) {
    if(sfInstance && sfInstance[i]?.description && sfInstance[i]?.description != 'dummy') { %>    	
        <s:updatereference model="['referenceId': '','speciesId':speciesInstance.id,'speciesFieldId':sfInstance[i].id,
                        'fieldId': sfInstance[i]?.field?.id,'value':sfInstance[i]?.description]" />
    <%		
        sfInstance[i]?.description = 'dummy';
        sfInstance[i].save();
    }
  }
}
*/
def criteria = SpeciesField.createCriteria();
results = criteria.list {
    groupProperty('field')
    eq('species', speciesInstance)
    //eq('language', userLanguage)
    order("field", "asc")
}

}


def sortedRef = [:]
def conRef = []
def titleRef = []
  results.each{result->
  if(result.references){
  		result.references.each{ r ->
			//println r.speciesField.field?.connection
			def con = r.speciesField.field?.connection
			def fL = r.speciesField.field.toString()
			if(r.speciesField.field.language == userLanguage){
				titleRef[con] = r.speciesField.field.toString()
			}
			if(conRef.contains(con)){			
	  			sortedRef[con] << r
	  		}else{
	  			sortedRef[con] = [r]
	  			conRef << con
	  		}
  		}	
  		references	<< result.references
  	}
  }
%>

<g:if test="${sortedRef}">
	<g:each in="${sortedRef}" status="i" var="title">
		<h6>
      		<span style="color: #413D3D;text-decoration: none;">${titleRef[title.key]}</span>
	  	</h6>
	  	<ol class="references references_${i}" style="list-style:disc;list-style-type:decimal">
	  	<g:each in="${title.value}" var="r">
			
	  		<li class="linktext">
   
			<span class="ref_val" data-species-id ="${speciesInstance?.id}" data-field-id="${r?.speciesField?.field?.id}" 
				 data-refference-id="${r?.id}" data-speciesfield-id="${r?.speciesField?.id}">				 
			    <g:if test="${r.url}">
			     ${r.title?r.title:r.url}
			    </g:if> <g:else>
			    ${r?.title}
			    </g:else>
			</span>
			<sUser:ifOwns model="['user':r?.speciesField?.uploader]">
				
				<span class="ed_de" style="display:none;">
					<a class="btn btn-small btn-primary edit_ref_val" style="padding:0px;" ><i class="icon-edit"></i></a>
			  		<a class="btn btn-small btn-danger del_ref_val" style="padding:0px;"><i class="icon-trash"></i></a>
			  	</span>
			</sUser:ifOwns>
	</li>


		</g:each>
		</ol>
	</g:each>

</g:if>


<a class='addFieldButton btn btn-success pull-left add_ref' style="display:none;" ><i class="icon-plus"></i>${g.message(code:'title.value.add')}</a>

<div class="new_form_add_referrence" style="display:none;">
	
	<br><br><br>
	<label>Add References : <span style="font-size: 12px;color: #A79F9F;">(Hint :- add multiple references separated by a line break (hit Enter))</span></label>
	<textarea class="add_description_ref" style="width: 900px;height: 50px;" data-species-id ="${speciesInstance?.id}" data-field-id="${fieldInstance?.id}"></textarea>
	<div class="editable-buttons editable-buttons-bottom pull-right">
		<button type="submit" class="btn btn-primary sav_ref"><i class="icon-ok icon-white"></i>${g.message(code:'button.save')}</button>
		<button type="button" class="btn can_ref"><i class="icon-remove"></i>${g.message(code:'button.cancel')}</button>
	</div>
</div>

<asset:script type="text/javascript">

function save_reference(that,action,speciesid,fieldId,pk,cid,text_val){
	var act = '';
	if(action == "delete"){
		act = action;
	}
	$.post('/species/update/',
			{ 
				'name': 'reference',
				'act' : act,
				'speciesid' : speciesid,
				'cid' : cid,
				'value' : text_val,
				'fieldId' : fieldId,
				'pk' : pk
			},function(data){
				console.log(data);
				if(data.success){
					if(action == 'update'){
						var html_v = that.parent().parent().find('.ref_val');
						console.log(html_v);					
						html_v.html(text_val).show();
						html_v.parent().find('.ed_de').show();
						html_v.parent().find('.edit_ref_form').hide();
						alert(data.msg);
					}else if(action == 'add'){
						var reference_ele = $('.references');
						var output = '';
						var content = data.content;
						for(var i=0; i<content.length;i++){
							output += '<li class="linktext">';
							output += '<span class="ref_val" data-species-id="'+speciesid+'" data-field-id="'+fieldId+'" data-refference-id="'+content[i].id+'" data-speciesfield-id="'+data.id+'">';
							output += content[i].title;
							output +='</span>&nbsp;';
							output +='<span class="ed_de"><a class="btn btn-small btn-primary edit_ref_val" style="padding:0px;"><i class="icon-edit"></i></a>&nbsp;<a class="btn btn-small btn-danger del_ref_val" style="padding:0px;"><i class="icon-trash"></i></a></span>';
							output +='</li>';
						}						
						$('.new_form_add_referrence').hide();
						$('.new_form_add_referrence').prev().show();
						$('.references').append(output);
						$('.add_description_ref').val('');

						var alert_msg = '';
						if( data.count_chk.success_count> 0){
							alert_msg += "Successfully added "+data.count_chk.success_count+" reference(s).";
						}
						if(data.count_chk.failure_count >0 ){
							alert_msg += " "+data.count_chk.failure_count+" duplicate(s) not added";
						}						
						alert(alert_msg);
					}else if(action == 'delete'){
						console.log(data);
						that.parent().parent().remove();
					}	
				}else{
					alert(data.msg);
				}
			return false;			
		});
}
	$(document).ready(function(){

		$('.references').each(function(index,value){
			var mylist = $('.references_'+index);
			var listitems = mylist.children('li').get();

			listitems.sort(function(a, b) {
			   return $(a).text().toUpperCase().localeCompare($(b).text().toUpperCase());
			})

			mylist.empty().append(listitems);
		});

		$('.add_ref').click(function(){
			$(this).hide();
			$(this).next().show();
		});
		$('.can_ref').click(function(){
			var that = $(this);
			that.parent().parent().hide();
			that.parent().parent().prev().show();
		});
		$('.sav_ref').click(function(){
			var that = $(this);
			var add_description_ref = that.parent().parent().find('.add_description_ref');
			console.log(add_description_ref.val());	
			var text_val = add_description_ref.val();					
			var speciesid = add_description_ref.attr('data-species-id');
			var fieldId = add_description_ref.attr('data-field-id');
			var pk = fieldId;			
			//save_reference(speciesid,fieldId,pk,cid,text_val)
			save_reference(that,'add',speciesid,fieldId,pk,'',text_val);
		});

		$(document).on('click',".edit_ref_val",function(){		
				var that = $(this);
				var ref_parent = that.parent().parent();
				var ref_wrap = ref_parent.find('.ref_val');
				var	ref_wrap_html = $.trim(ref_wrap.text());
				console.log(ref_wrap_html);
				that.parent().hide();
				var output_text = '<div class="edit_ref_form" style="width:850px;"><input class="add_description_ref" style="width: 800px;height: 20px;" value="" />';					
					output_text += '<div class="btn btn-success up_ref" style="padding: 0px;margin: 2px;"><i class="icon-ok icon-white"></i></div>';
					output_text += '<div class="btn btn-danger up_can_ref" style="padding: 0px;margin: 2px;"><i class="icon-remove"></i></div></div>';
				ref_wrap.hide();
				ref_parent.append(output_text);
				ref_parent.find('.add_description_ref').val(ref_wrap_html);
		});

		$(document).on('click',".up_ref",function(){
			var that = $(this);
			var text_val =  that.parent().find('.add_description_ref').val();
			var ref_detail_wrapper = that.parent().parent();
			var cid = ref_detail_wrapper.find('.ref_val').attr('data-refference-id');
			var speciesid = ref_detail_wrapper.find('.ref_val').attr('data-species-id');
			var fieldId = ref_detail_wrapper.find('.ref_val').attr('data-field-id');
			var pk = ref_detail_wrapper.find('.ref_val').attr('data-speciesfield-id');	
			if(!pk){
				pk = fieldId;
			}		 
			save_reference(that,'update',speciesid,fieldId,pk,cid,text_val);
		});	

		$(document).on('click',".up_can_ref",function(){				
				var that = $(this);
				that.parent().parent().find('.ref_val').show();
				that.parent().parent().find('.ed_de').show();
				that.parent().remove();
		});	

		$(document).on('click',".del_ref_val",function(){

			if(confirm("Are you sure to delete this?")){
				var that = $(this);
				var li_ref = that.parent().parent();
				var cid = li_ref.find('.ref_val').attr('data-refference-id');
				var speciesid = li_ref.find('.ref_val').attr('data-species-id');
				var fieldId = li_ref.find('.ref_val').attr('data-field-id');
				var pk = li_ref.find('.ref_val').attr('data-speciesfield-id');	
				if(!cid && !pk){
					alert("Error on Delete!");
				}
				save_reference(that,'delete',speciesid,fieldId,pk,cid,'');
			}	
		});

	});

</asset:script>

