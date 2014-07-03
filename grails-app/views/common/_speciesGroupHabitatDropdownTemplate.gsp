<%@page import="species.Habitat.HabitatType"%>
<%@page import="species.utils.ImageType"%>
<%@page import="species.groups.SpeciesGroup"%>
<%@page import="species.Habitat"%>
<div class ="control-group ${hasErrors(bean: observationInstance, field: 'group', 'error')} ${hasErrors(bean: observationInstance, field: 'habitat', 'error')}">
    <div class="help-inline control-label">
        <g:hasErrors bean="${observationInstance}" field="group">
        <g:message code="observation.group.not_selected" />
        </g:hasErrors>

        <g:hasErrors bean="${observationInstance}" field="habitat">
        <g:message code="observation.habitat.not_selected" />
        </g:hasErrors>
    </div>
</div>
<div class="row groups_super_div" style="clear:both;">
    <label for="group"><g:message
        code="observation.group.label" default="Group" />
    </label> 
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
    <div class="groups_div" class="bold_dropdown" style="z-index:3;">
        <div class="selected_group" class="selected_value">
            <img src="${createLinkTo(dir: 'images', file: SpeciesGroup.findByName('All').icon()?.fileName?.trim(), absolute:true)}" />
            <span class="display_value">Select group</span>
        </div>
        <div class="group_options" style="background-color:#fbfbfb;box-shadow:0 8px 6px -6px black; border-radius: 0 5px 5px 5px;display:none;">
            <ul>
                <g:each in="${species.groups.SpeciesGroup.list()}" var="g">
                <!--g:if
                test="${!g.name.equals(grailsApplication.config.speciesPortal.group.ALL)}"-->
                <li class="group_option" style="display:inline-block;padding:5px;" value="${g.id}">
                <div style="width:160px;">
                    <img src="${createLinkTo(dir: 'images', file: g.icon()?.fileName?.trim(), absolute:true)}"/>
                    <span class="display_value">${g.name}</span>
                </div>
                </li>
                <!--/g:if-->
                </g:each>
            </ul>
        </div>
    </div>
    <input class="group" type="hidden" name="group_id"></input>
</div>

<div class="row habitat_super_div" style="clear:both;">
    <label>Habitat</label>
    <div class="habitat_list">
        <!--select class="ui-widget-content">
        <option>None</option>
        <option>Forest</option>
        <option>Savanna</option>
        <option>Shrubland</option>
        <option>Grassland</option>
        <option>Wetlands</option>
        <option>Rocky Areas</option>
        <option>Caves and Subterranean Habitats</option>
        <option>Desert</option>
        <option>Marine</option>
        <option>Artificial - Terrestrial</option>
        <option>Artificial - Aquatic</option>
        <option>Introduced Vegetation</option>
        <option>Other</option>
        <option>Unknown</option>
        </select-->	

        <div class="habitat_div" class="bold_dropdown" style="z-index:2;">
            <div class="selected_habitat" class="selected_value">
                <img src="${resource(dir:'images/group_icons',file:'All.png', absolute:true)}"/><span class="display_value">Select habitat</span>
            </div>
            <div class="habitat_options" style="background-color:#fbfbfb;box-shadow:0 8px 6px -6px black; border-radius: 0 5px 5px 5px;display:none;">                                       
                <ul>
                    <g:each in="${species.Habitat.list()}" var="h">
                    <li class="habitat_option" style="display:inline-block;padding:5px;" value="${h.id}"><img src="${resource(dir:'images/group_icons',file:'All.png', absolute:true)}"/><span class="display_value">${h.name}</span></li>
                    </g:each>
                </ul>
            </div>
        </div>
    </div>	
    <input class="habitat" type="hidden" name="habitat_id"></input>
</div>
<g:javascript>

$(document).ready(function(){
    $(".selected_group").click(function(){
        $(this).closest(".groups_super_div").find(".group_options").toggle();
        //$(this).css({'background-color':'#fbfbfb', 'border-bottom-color':'#fbfbfb'});
    });

    $(".group_option").click(function(){
        $(this).closest(".groups_super_div").find(".group").val($(this).val());
        $(this).closest(".groups_super_div").find(".selected_group").html($(this).html());
        $(this).closest(".group_options").hide();
        $(this).closest(".groups_super_div").find(".selected_group").css({'background-color':'#e5e5e5', 'border-bottom-color':'#aeaeae'});
    });

    $(".selected_habitat").click(function(){
        $(this).closest(".habitat_super_div").find(".habitat_options").toggle();
        //$(this).css({'background-color':'#fbfbfb', 'border-bottom-color':'#fbfbfb'});
    });

    $(".habitat_option").click(function(){
        console.log("clicked here habitat");
        $(this).closest(".habitat_super_div").find(".habitat").val($(this).val());
        $(this).closest(".habitat_super_div").find(".selected_habitat").html($(this).html());
        $(this).closest(".habitat_options").hide();
        $(this).closest(".habitat_super_div").find(".selected_habitat").css({'background-color':'#e5e5e5', 'border-bottom-color':'#aeaeae'});
    });
});

</g:javascript>
