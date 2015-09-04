<style type="text/css">
	#externalDbResults {
		width: 900px;
        left:39%;
    }
    .extDbResTable {
        border-bottom: 1px solid lightgray;
        text-align:center;
    }
    .extDbResTable td {
        border:1px solid lightgray;
        border-bottom:0px;
        border-top:0px;
    }
    .extDbResTable th {
        border:1px solid lightgray;
    }

</style>

<div id="externalDbResults" class="modal fade">
    <div class="modal-dialog addcloseevent">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                <h4 class="modal-title">Results for</h4><h6></h6>
            </div>
            <div class="modal-body">
                <table class="extDbResTable" style="width:100%">
                    <tr>
                        <th>Name</th>
                        <th>Rank</th>
                        <th>Name status</th>
                        <th>Group</th>
                        <th>Source Database</th>
                        <th>Action</th>
                    </tr>
                </table>
            </div>
        </div>
    </div>
</div>
