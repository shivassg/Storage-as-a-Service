$(document).ready(function(e) {

    getGoogleDriveFiles(0);

    $("#gDriveUploaddialog").dialog({
        modal: true,
        autoOpen: false,
        title: "File Upload",
        width: 500,
        height: 250
    });
    $("#gdrive-upload-tbr").click(function() {
        $('#gDriveUploaddialog').dialog('open');
    });

    $("#gDrivenewfldrDlg").dialog({
        modal: true,
        autoOpen: false,
        title: "Create New Folder",
        width: 400,
        height: 150
    });

    $("#gdrive-createfolder").click(function() {
        $('#gDrivenewfldrDlg').dialog('open');
    });
    
    $('body').on("click","a.download", function(e){
    	e.preventDefault();
    	e.stopPropagation();
    	e.stopImmediatePropagation();
    	var fileid = $(this).data("fileid");
    	window.open('/gdrivedownload/'+fileid,"_blank");
    	return false;
    })
})

function getGoogleDriveFiles(parentId) {

    if (parentId) {
        sessionStorage.setItem("currentFolderid", parentId);
    } else {
        sessionStorage.setItem("currentFolderid", "0");
    }

    $.ajax({
        url: 'gDriveListfiles/' + parentId,
        method: 'GET',
        success: function(data, error, xhr) {
            updateData(data);
        },
        error: function(err) {

        }
    });
}

function updateData(data) {

    var gtable;
    if ($.fn.dataTable.isDataTable('#gdrive-table')) {
        table = $('#gdrive-table').DataTable();
        table.clear();
        table.rows.add(data).draw();
    } else {
        var gtable = $("#gdrive-table").DataTable({
            "data": data,
            "scrollY": "250px",
            "select": true,
            "columns": [{
                    "data": "folder",
                    "render": function(data, type, row, meta) {
                        if (data) {
                            return "<img src='images/folder-icon.png'/>";
                        } else {
                            return "<img src='images/file-icon.png'/>";
                        }
                    }
                },
                {
                    "data": "id",
                    "visible": false
                },
                {
                    "data": "name"
                },
                {
                	"data": "download",
                	"render": function(data, type, row, meta){
                		
                		if(!row.folder){
                			var id = row.id;
                			return "<a href='#' class='download' data-fileid='"+id+"'>Download</a>";
                		}else{
                			return "";
                		}
                	}
                }
            ]
        });


        $("#gdrive-navin").on('click', function() {
            var rowData = gtable.rows(".selected").data();
            var id = rowData[0].id;
            var isfolder = rowData[0].folder;
            if (isfolder) {
                getGoogleDriveFiles(id);
            } else {
                alert("Not a folder");
            }
        });



        $("#gdrive-delete").on('click', function(e) {
            var rowData = gtable.rows(".selected").data();
            var id = rowData[0].id;
            var parentId = sessionStorage.getItem("currentFolderid");

            $.ajax({
                url: '/gdrivedeletefile/' + id,
                method: 'DELETE',
                success: function(resp, err) {
                    getGoogleDriveFiles(parentId);
                },
                error: function(resp) {
                    getGoogleDriveFiles(parentId);
                }
            });
        });

        $("#gDriveNewFldrForm").on('submit', function(e) {
            e.preventDefault();

            var id = sessionStorage.getItem("currentFolderid");
            $.ajax({
                url: '/gdrivecreatefolder/' + id,
                method: 'POST',
                data: $("#gDriveNewFldrForm").serialize(),
                success: function(resp, err) {
                    $('#gDrivenewfldrDlg').dialog('close');
                    getGoogleDriveFiles(id);
                },
                error: function(resp) {
                    getGoogleDriveFiles(id);
                }
            });
            return false;
        });

        $("#gDriveuploadForm").on('submit', function(e) {
            e.preventDefault();

            var id = sessionStorage.getItem("currentFolderid");
            var form = $("#gDriveuploadForm")[0];
            var data = new FormData(form);

            $.ajax({
                url: '/gdriveuploadfile/' + id,
                method: 'POST',
                enctype: 'multipart/form-data',
                processData: false,
                contentType: false,
                data: data,
                success: function(resp, err) {
                    $('#gDriveUploaddialog').dialog('close');
                    getGoogleDriveFiles(id);
                },
                error: function(resp) {
                    getGoogleDriveFiles(id);
                }
            });
            return false;
        });

    }
}