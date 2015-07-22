<div class="view_tags view_obv_tags" style="margin-bottom:25px;">
	<div style="height: 26px;">
		<i class="icon-tags"></i> <g:message code="default.tags.label" />
		<div class="btn btn-small pull-right btn-primary add_obv_tags" style="  margin-right: 16px;">Add Tag</div>
	</div>
	<g:if test="${tags}">                
		<ul class="tagit tagitAppend">
			<g:each in="${tags.entrySet()}">
				<li class="tagit-choice" style="padding:0 5px;">
					${it.getKey()} <span class="tag_stats"> ${it.getValue()}</span>
				</li>
			</g:each>
		</ul>
	</g:if>
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
