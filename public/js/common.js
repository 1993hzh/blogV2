function reply(toId, toName) {
    $("#comment").attr("placeholder", "Re @" + toName + ": ");
    $("#toId").attr("value", toId);
    $("#toName").attr("value", toName);
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
    $.ajax({
        url: "/comment",
        type: "POST",
        data: {
            content: content,
            toId: toId,
            toName: toName,
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
    $.ajax({
        url: "/doLogin",
        type: "POST",
        data: {
            name: name,
            password: password
        },
        cache: false,
        success: function (result) {
            if (result == "Success") {
                window.location.href = "/index";
                return;
            }
            showError(result);
            //clear all fields
            $('#loginForm').trigger("reset");
        },
        error: function (msg) {
            showError(msg);
            $('#loginForm').trigger("reset");
        },
    })
}

function hideError() {
    $("#error").html("");
}

function showError(msg) {
    $('#error').html("<div class='alert alert-danger'>");
    $('#error > .alert-danger').html("<button type='button' class='close' data-dismiss='alert' aria-hidden='true'>&times;").append("</button>");
    $('#error > .alert-danger').append("<strong>" + msg + "</strong>");
    $('#error > .alert-danger').append('</div>');
}