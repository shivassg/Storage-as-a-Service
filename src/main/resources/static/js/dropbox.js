$(document).ready(function(e) {

    getDropboxFiles("");

    $("#dropboxUploaddialog").dialog({
        modal: true,
        autoOpen: false,
        title: "File Upload",
        width: 500,
        height: 250
    });
    $("#dropbox-upload-tbr").click(function() {
        $('#dropboxUploaddialog').dialog('open');
    });

    $("#dropboxnewfldrDlg").dialog({
        modal: true,
        autoOpen: false,
        title: "Create New Folder",
        width: 400,
        height: 150
    });

    $("#dropbox-createfolder").click(function() {
        $('#dropboxnewfldrDlg').dialog('open');
    });
    
    $('body').on("click","a.downloadDb", function(e){
    	e.preventDefault();
    	e.stopPropagation();
    	e.stopImmediatePropagation();
    	var encodedPath = $(this).data("encode");
    	window.open('/dropboxdownload/'+encodedPath,"_blank");
    	
    	return false;
    })
})

function hexEncode(plain) {
    return plain.split("").map(c => c.charCodeAt(0).toString(16)).join("");
}

function getDropboxFiles(path) {
    ;
    if (path) {
        sessionStorage.setItem("curFolderPath", path);
    } else {
        sessionStorage.setItem("curFolderPath", "");
    }
    $.ajax({
        url: 'dropboxListfiles',
        method: 'GET',
        data: {
            "parentPath": path
        },
        success: function(data, error, xhr) {
            updateData(data);
        },
        error: function(err) {

        }
    });
    
}



function updateData(data) {

    var gtable;
    if ($.fn.dataTable.isDataTable('#dropbox-table')) {
        table = $('#dropbox-table').DataTable();
        table.clear();
        table.rows.add(data).draw();
    } else {
        var gtable = $("#dropbox-table").DataTable({
            "data": data,
            "scrollY": "200px",
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
                    "data": "path",
                    "visible": false
                },
                {
                    "data": "name"
                },
                {
                	"data": "download",
                	"render": function(data, type, row, meta){
                		;
                		if(!row.folder){
                			var path = row.path;
                			var encoded = path.split("").map(c => c.charCodeAt(0).toString(16)).join("");
                			//var url = "/dropboxdownload/"+encoded;
                			return "<a href='#' class='downloadDb' data-encode='"+encoded+"'>Download</a>";
                		}else{
                			return "";
                		}
                	}
                }
            ]
        });


        $("#dropbox-navin").on('click', function() {
            var rowData = gtable.rows(".selected").data();
            var path = rowData[0].path;
            var isfolder = rowData[0].folder;
            if (isfolder) {
                getDropboxFiles(path);
            } else {
                alert("Not a folder");
            }
        });



        $("#dropbox-delete").on('click', function(e) {
            var rowData = gtable.rows(".selected").data();
            var path = rowData[0].path;
            var parentPath = sessionStorage.getItem("curFolderPath");

            $.ajax({
                url: '/dropboxdeletefile',
                method: 'DELETE',
                data: {
                    "path": path
                },
                success: function(resp, err) {
                    getDropboxFiles(parentPath);
                },
                error: function(resp) {
                    getDropboxFiles(parentPath);
                }
            })
        });

        $("#dropboxNewFldrForm").on('submit', function(e) {
            e.preventDefault();
            var path = sessionStorage.getItem("curFolderPath");
            var modifiedPath = path;
            if(modifiedPath == ""){
            	modifiedPath ="root";
            }
            $.ajax({
                url: '/dropboxcreatefolder/' + hexEncode(modifiedPath),
                method: 'POST',
                data: $("#dropboxNewFldrForm").serialize(),
                success: function(resp, err) {
                    $('#dropboxnewfldrDlg').dialog('close');
                    getDropboxFiles(path);
                },
                error: function(resp) {
                    getDropboxFiles(path);
                }
            });
            return false;
        });

        $("#dropboxuploadForm").on('submit', function(e) {
            e.preventDefault();
            var path = sessionStorage.getItem("curFolderPath");

            var form = $("#dropboxuploadForm")[0];
            var data = new FormData(form);
            $.ajax({
                url: '/dropboxuploadfile/' + hexEncode(path),
                method: 'POST',
                enctype: 'multipart/form-data',
                processData: false,
                contentType: false,
                data: data,
                success: function(resp, err) {
                    $('#dropboxUploaddialog').dialog('close');
                    getDropboxFiles(path);
                },
                error: function(resp) {
                    getDropboxFiles(path);
                }
            });
            return false;
        });
    }
}