
<div id="${controller}_connList" style="clear:both;">

    <div class="connection_wrapper_row1">${controller.capitalize()} 
    </div>

    <div class="connection_wrapper_row2">
        <!--table id="${controller}_connTable" class="table table-bordered table-condensed table-striped">
            <tbody>
            </tbody>
        </table-->
        <a data-href="${uGroup.createLink('controller':controller, action:'list')}"><span id="${controller}InstanceTotal">${instanceTotal?:''}</span></a>
        <!--button class="btn btn-mini loadConnection" data-controller="${controller}" data-offset='0'>Load</button-->
    </div>
</div>
