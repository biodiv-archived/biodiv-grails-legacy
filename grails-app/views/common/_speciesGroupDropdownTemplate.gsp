<%@page import="species.Habitat.HabitatType"%>
<%@page import="species.utils.ImageType"%>
<%@page import="species.groups.SpeciesGroup"%>
<%@page import="species.Habitat"%>
<div class ="control-group ${hasErrors(bean: observationInstance, field: 'group', 'error')}" style="clear:both;">
   <g:if
    test="${action != 'show'}">
    <label for="group"><g:message
        code="observation.group.label" default="${g.message(code:'default.group.label')}" />
    </label> 
    </g:if>
    <div class="help-inline control-label">
        <g:hasErrors bean="${observationInstance}" field="group">
        <g:message code="observation.group.not_selected" />
        </g:hasErrors>
    </div>
<div class="groups_super_div" style="clear:both;">
    <!--select name="group_id" class="ui-widget-content ui-corner-all">
    <g:each in="${species.groups.SpeciesGroup.list()}" var="g">
    <g:if
    test="${!g.name.equals(grailsApplication.config.speciesPortal.group.ALL)}">
    <option value="${g.id}"
    ${(g.id == observationInstance?.group?.id)?'selected':''}>
    ${g.name}
    </option>
    </g:if>
    </g:each>
    </select-->


<%

 def selected_group = (observationInstance?.group?.name) ? observationInstance?.group?.name : 'All';
 def message_selected_group = (observationInstance?.group?.name) ? observationInstance?.group?.name : g.message(code:'default.select.group.label');
%>

    <div class="groups_div dropdown" style="z-index:15;">
        <div class="dropdown-toggle btn selected_group selected_value " data-toggle="dropdown">
            <span style="float:left;"
                        class="group_icon species_groups_sprites active ${SpeciesGroup.findByName(selected_group).iconClass()}"
                    title="${SpeciesGroup.findByName(selected_group).name}"></span>
            <!--img src="${createLinkTo(dir: 'images', file: SpeciesGroup.findByName('All').icon()?.fileName?.trim(), absolute:true)}" style="width:22px;"/-->
            <span class="display_value">${message_selected_group}</span>
            <b class="caret"></b>
        </div>
            <ul class="group_options dropdown-menu">
                <g:each in="${species.groups.SpeciesGroup.list()}" var="g">
                <!--g:if
                test="${!g.name.equals(grailsApplication.config.speciesPortal.group.ALL)}"-->
                <li class="group_option" value="${g.id}">
                <div>
                    <span style="float:left;"
                        class="group_icon species_groups_sprites active ${g.iconClass()}"
                    title="${g.name}"></span>

                    <!--img src="${createLinkTo(dir: 'images', file: g.icon()?.fileName?.trim(), absolute:true)}" style="width:22px;"/-->
                    <span class="display_value">${g.name}</span>
                </div>
                </li>
                <!--/g:if-->
                </g:each>
            </ul>
    </div>
    <input class="group" type="hidden" name="group_id"></input>
</div>

<r:script>

$(document).ready(function(){
    $(".selected_group").unbind('click').click(function(){
        $(this).closest(".groups_super_div").find(".group_options").toggle();
        //$(this).css({'background-color':'#fbfbfb', 'border-bottom-color':'#fbfbfb'});
    });

    $(".group_option").unbind('click').click(function(){
        var is_save_btn_exists = $(this).closest(".groups_super_div").parent().parent().find('.save_group_btn');
           if(is_save_btn_exists.length == 1){
                is_save_btn_exists.show();
           }
       

        $(this).closest(".groups_super_div").find(".group").val($(this).val());
        $(this).closest(".groups_super_div").find(".selected_group").html($(this).html());
        $(this).closest(".group_options").hide();
        //$(this).closest(".groups_super_div").find(".selected_group").css({'background-color':'#e5e5e5', 'border-bottom-color':'#aeaeae'});
        if($(this).closest(".groups_super_div").find(".selected_group b").length == 0){
            $('<b class="caret"></b>').insertAfter($(this).closest(".groups_super_div").find(".selected_group .display_value"));
        }
    });

});

</r:script>
</div>
