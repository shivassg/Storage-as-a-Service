$(document).ready(function() {

    $.ajax({
        url: '/codes',
        method: 'GET',
        success: function(data, status, xhr) {

            if (data.googleCode) {
                $("#gdrive-login").hide();
                $("#gdrive-loggedin").show();
            } else {
                $("#grdive-login").show();
                $("#gdrive-loggedin").hide();
            }

            if (data.dropboxCode) {
                $("#dropbox-login").hide();
                $("#dropbox-loggedin").show();
            } else {
                $("#dropbox-login").show();
                $("#dropbox-loggedin").hide();
            }

        },
        error: function(jqXhr, textStatus, errorMsg) {
            alert(erroMsg);
        }
    });



    $("#gdrive-loggedin").on('click', function(e) {
        refreshGdrive();
        return false;
    });

    $("#dropbox-loggedin").on('click', function(e) {
        refreshDropbox();
        return false;
    });

})

function refreshGdrive() {
    $.ajax({
        method: 'GET',
        url: 'gdrivePage',
        success: function(data, status, xhr) {
            $("#main").html(data);
        },
        error: function(jqXhr, textStatus, errorMsg) {
            alert(erroMsg);
        }
    });
}

function refreshDropbox() {
    $.ajax({
        method: 'GET',
        url: 'dropboxPage',
        success: function(data, status, xhr) {
            $("#main").html(data);
        },
        error: function(jqXhr, textStatus, errorMsg) {
            alert(erroMsg);
        }
    });
}