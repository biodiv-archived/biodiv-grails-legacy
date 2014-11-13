<style type="text/css">
	#newNamePopup {
		width: 358px;
        left:54%;
    }
</style>

<div id="newNamePopup" class="modal fade">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                <h4 class="modal-title">Provide the name</h4>
            </div>
            <div class="modal-body">
                Name : <input class="newName" type="text" placeholder="Name">
                <button class="pull-right btn btn-primary" onclick="searchDatabase(true)"> OK </button>
            </div>
        </div>
    </div>
</div>
