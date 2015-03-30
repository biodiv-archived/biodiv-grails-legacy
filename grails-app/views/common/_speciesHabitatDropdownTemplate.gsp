<%@page import="species.Habitat.HabitatType"%>
<%@page import="species.utils.ImageType"%>
<%@page import="species.groups.SpeciesGroup"%>
<%@page import="species.Habitat"%>
<div class ="control-group ${hasErrors(bean: observationInstance, field: 'habitat', 'error')}">

    <label><g:message code="default.habitats.label" /></label>
    <div class="help-inline control-label">
        <g:hasErrors bean="${observationInstance}" field="habitat">
        <g:message code="observation.habitat.not_selected" />
        </g:hasErrors>
    </div>

<div class="habitat_super_div" style="clear:both;">
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

        <div class="habitat_div dropdown" style="z-index:11;">
            <div class="selected_habitat selected_value dropdown-toggle btn" data-toggle="dropdown">
                <span style="float:left;"
                    class="habitat_icon group_icon habitats_sprites active ${Habitat.findByName('All').iconClass()}"
                    title="${Habitat.findByName('All').name}"></span>
                <!--img src="${resource(dir:'images/group_icons',file:'All.png', absolute:true)}" style="width:22px;"/-->
                <span class="display_value"><g:message code="default.select.habitat.label" /> </span>
            <b class="caret"></b>
            </div>
            <ul class="habitat_options dropdown-menu">                                       
                    <g:each in="${species.Habitat.list()}" var="h">
                    <li class="habitat_option" value="${h.id}">
                    <div>
                        <span style="float:left;"
                    class="habitat_icon group_icon habitats_sprites active ${h.iconClass()}"
                    title="${h.name}"></span>

                    <!--img src="${resource(dir:'images/group_icons',file:'All.png', absolute:true)}" style="width:22px;"/-->
                    <span class="display_value">${h.name}</span>

                    </div>
                    </li>
                    </g:each>
            </ul>
        </div>
    </div>	
    <input class="habitat" type="hidden" name="habitat_id"></input>
</div>

<r:script>

$(document).ready(function(){
    $(".selected_habitat").unbind('click').click(function(){
        $(this).closest(".habitat_super_div").find(".habitat_options").toggle();
        //$(this).css({'background-color':'#fbfbfb', 'border-bottom-color':'#fbfbfb'});
    });

    $(".habitat_option").unbind('click').click(function(){
        $(this).closest(".habitat_super_div").find(".habitat").val($(this).val());
        $(this).closest(".habitat_super_div").find(".selected_habitat").html($(this).html());
        $(this).closest(".habitat_options").hide();
        //$(this).closest(".habitat_super_div").find(".selected_habitat").css({'background-color':'#e5e5e5', 'border-bottom-color':'#aeaeae'});
        if($(this).closest(".habitat_super_div").find(".selected_habitat b").length == 0){
            $('<b class="caret"></b>').insertAfter($(this).closest(".habitat_super_div").find(".selected_habitat .display_value"));
        }

    });
});

</r:script>
</div>
