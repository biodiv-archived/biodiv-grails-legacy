<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <link rel="stylesheet" type="text/css" href="${resource(dir:'css',file:'classDiagram.css')}" />
        <link rel="stylesheet" type="text/css" href="${resource(dir:'css',file:'form.css')}" />
        <link rel="stylesheet" type="text/css" href="${resource(dir:'css/ui-lightness',file:'jquery-ui-1.7.2.custom.css')}" />
        <title>Class Diagram Visualisation</title>
        <g:javascript library="jquery-1.3.2.min" />                
        <g:javascript library="jquery-ui-1.7.2.custom.min" />                
        <g:javascript library="classDiagram" />                
        
    </head>
    <body>
        <div class="body">
            <div class="nav">
                <div class="menuButton">
                    <a id="menuItem_showPreferences" href="#">Preferences</a>
                    <a id="menuItem_showLegend" href="#" title="Show legend of symbols used in the class diagram">Legend</a>
                    <a id="menuItem_hideMenu" href="#" title="Hide this menu and show model as raw image">Hide menu</a>
                    <a id="menuItem_pluginHome" href="http://grails.org/plugin/class-diagram" title="Plugin home directory on grails.org">Plugin Home</a>
                </div>
            </div>
            <div id="image">
            </div>
        </div>
        
        <div id="preferences" title="Preferences">
            <g:render template="preferences" plugin="classDiagram"/>
        </div>
        <div id="legend" title="Legend">
            <img class="legend-img" src='' href="${createLink(action:'legend', params: prefs)}"/>    
        </div>
        <div id="spinner" class="spinner" style="display:none;">
            <img src="${resource(dir:pluginContextPath+'/images',file:'spinner.gif')}" alt="Spinner" />
        </div>    
    </body>
</html>
