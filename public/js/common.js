function reply(toId, toName, toCommentId) {
    $("#comment").attr("placeholder", "Re @" + toName + ": ");
    $("#comment").empty();
    $("#toId").attr("value", toId);
    $("#toName").attr("value", toName);
    $("#toCommentId").attr("value", toCommentId);
}

function doComment() {
    hideError();
    var content = $("#comment").val();
    if (!isRequired(content, "comment")) {
        return false;
    }
    var passageId = $("input#passageId").val();
    var toId = $("input#toId").val();
    var toName = $("input#toName").val();
    var toCommentId = $("input#toCommentId").val();
    $.ajax({
        url: "/logged/comment",
        type: "POST",
        data: {
            content: content,
            toId: toId,
            toName: toName,
            toCommentId: toCommentId,
            passageId: passageId
        },
        cache: false,
        success: function (result) {
            if (result == "Success") {
                window.location.href = "/passage?id=" + passageId;
                return;
            }
            // error message
            showError(result)
        },
        error: function (msg) {
            showError(msg)
        },
    });
}

var isRequired = function (value, id) {
    if ($.trim(value) == "") {
        showError(id + " shouldn't be empty!")
        return false;
    }
    return true;
}

function doLogin() {
    hideError();
    var name = $("input#name").val();
    if (!isRequired(name, "name")) {
        return false;
    }
    var password = $("input#password").val();
    if (!isRequired(password, "password")) {
        return false;
    }
}

function hideError() {
    $("#error").html("");
}

function showError(msg) {
    $('#error').html("<div class='alert alert-danger'>");
    $('#error > .alert-danger').html("<button type='button' class='close' data-dismiss='alert' aria-hidden='true'>&times;</button>");
    $('#error > .alert-danger').append("<strong>" + msg + "</strong>");
    $('#error > .alert-danger').append('</div>');
}

function markAs(type, commentId) {
    $.ajax({
        url: "/logged/markAs",
        type: "GET",
        data: {
            markType: type,
            commentId: commentId
        },
        cache: false,
        success: function (result) {
            if (result == "Success") {
                window.location.href = window.location.href
                return;
            }
            // error message
            showError(result)
        },
        error: function (msg) {
            showError(msg)
        },
    });
}

function remove(id, url) {
    $.messager.confirm("Delete Passage", "You are going to delete passage, sure?", function () {
        doRemove(id, url);
    })
}

function doRemove(id, url) {
    $.ajax({
        url: url,
        type: "GET",
        data: {
            id: id
        },
        cache: false,
        success: function (result) {
            if (result == "Success") {
                $("#" + id).remove();
                return;
            }
            // error message
            showError(result)
        },
        error: function (msg) {
            showError(msg)
        },
    });
}

function pop_createUser() {
    $('#userForm').trigger("reset");
    $("#userwrap").show();
    $("#userwrap").dialog({
        title: "CreateUser", classed: "mydialog", onClose: function () {
            $(this).dialog("close");
        },
        buttons: [
            {
                text: "Submit", classed: "btn-primary", click: function () {
                createUser();
            }
            },
            {
                text: "Cancel", classed: "btn-default", click: function () {
                $(this).dialog("close");
            }
            }
        ]
    });
}

function createUser() {
    var userName = $("input#userName").val()
    var password = $("input#password").val()
    var mail = $("input#mail").val()
    $.ajax({
        url: "/manage/user/create",
        type: "POST",
        data: {
            userName: userName,
            password: password,
            mail: mail
        },
        cache: false,
        success: function (result) {
            if (result == "Success") {
                window.location.href = window.location.href
            }
            // error message
            $.messager.alert(result)
        },
        error: function (msg) {
            $.messager.alert("Internal Error")
        },
    });
}

//here is the drag
$(function () {
    var isClear = true;

    function markAllAsRead() {
        $.ajax({
            url: '/logged/markAll',
            dataType: "text",
            type: "post",
            success: function (result) {
                if (result != "Success") {
                    $.messager.popup(result);
                    return;
                } else {
                    $('#unreadDrag').text(0);
                }
            },
            error: function (msg) {
                $.messager.popup("Internal Error");
            }
        });
    }

    $('#unreadDrag').on({
        dragstart: function (e) {
            isClear = true;
            e.originalEvent.dataTransfer.effectAllowed = "move"
            e.originalEvent.dataTransfer.dropEffect = "none"
            //prevent chrome from open a new tab, firefox need this, but it sucks
            //e.originalEvent.dataTransfer.setData('text/plain', 'Any');
        },
        dragend: function (e) {
            e.preventDefault();
            if (isClear) {
                $.messager.confirm("Empty notifications", "You are going to mark all notifications as read, sure?", function () {
                    markAllAsRead();
                })
            }
        }
    });

    //$('#unreadDrag').parent().attr("draggable", "false");
    $('#unreadDrag').parent().parent().on({
        dragover: function (e) {
            e.preventDefault();
        },
        drop: function (e) {
            e.preventDefault();
            isClear = false
        }
    });
})