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
            if (result.isSuccess == true) {
                window.location.href = result.detail;
                return;
            } else {
                showError(result.detail);
            }
        },
        error: function (msg) {
            showError(msg.statusText)
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
            if (result.isSuccess == true) {
                window.location.href = window.location.href;
                return;
            } else {
                showError(result.detail);
            }
        },
        error: function (msg) {
            showError(msg.statusText)
        },
    });
}

function remove(id, url) {
    $.messager.confirm("Delete", "You are going to delete, sure?", function () {
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
            if (result.isSuccess == true) {
                $("#" + id).remove();
                return;
            } else {
                showError(result.detail);
            }
        },
        error: function (msg) {
            showError(msg.statusText)
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
            if (result.isSuccess == true) {
                window.location.href = window.location.href
                return;
            } else {
                showError(result.detail);
            }
        },
        error: function (msg) {
            showError(msg.statusText)
        },
    });
}

function pop_createTag() {
    $('#tagForm').trigger("reset");
    $("#tagwrap").show();
    $("#tagwrap").dialog({
        title: "Tag", classed: "mydialog", onClose: function () {
            $(this).dialog("close");
        },
        buttons: [
            {
                text: "Submit", classed: "btn-primary", click: function () {
                createOrUpdateTag();
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

function pop_updateTag(id, name, description) {
    pop_createTag();

    $("#id").val(id);
    $("#name").val(name);
    $("#description").val(description);
}

function createOrUpdateTag() {
    var id = $("input#id").val();
    var name = $("input#name").val();
    var description = $("input#description").val();
    $.ajax({
        url: "/manage/tag/upsert",
        type: "POST",
        data: {
            id: id,
            name: name,
            description: description
        },
        cache: false,
        success: function (result) {
            if (result.isSuccess == true) {
                window.location.href = window.location.href
                return;
            } else {
                showError(result.detail);
            }
        },
        error: function (msg) {
            showError(msg.statusText)
        },
    });
}

function deleteKeyword(obj) {
    $(obj).parent().parent("span.label").remove();
}

function addKeyword() {
    var newKw = $("input#keywordAdd").val();
    if (!isRequired(newKw, "keyword")) {
        return;
    }

    var index = $("div#keywordList").find("span.label").length;

    var append = "<span class=\"label label-primary\">" +
        "<input type=\"text\" class=\"hidden\" name=\"keywords[" + index + "]\" value=\"" + newKw + "\">" + newKw +
        "<a href=\"javascript:void(0);\"><i class=\"fa fa-times\" onclick=\"deleteKeyword(this)\"></i></a></span> &nbsp;";
    $("div#keywordList").append(append);

    $("input#keywordAdd").val("");
    $("input#keywordAdd").focus();
}

function switchLang(lang) {
    $.ajax({
        url: '/switchLang',
        type: "get",
        data: {
            lang: lang
        },
        success: function (result) {
            if (result.isSuccess == true) {
                window.location.href = window.location.href;
                return;
            } else {
                $.messager.popup(result.detail);
            }
        },
        error: function (msg) {
            $.messager.popup("Internal Error");
        }
    });
}

function switchEffects(effect) {
    $.ajax({
        url: '/switchEffect',
        type: "get",
        data: {
            effect: effect
        },
        success: function (result) {
            if (result.isSuccess == true) {
                window.location.href = window.location.href;
                return;
            } else {
                $.messager.popup(result.detail);
            }
        },
        error: function (msg) {
            $.messager.popup("Internal Error");
        }
    });
}

//here is the drag
$(function () {
    var isClear = true;

    function markAllAsRead() {
        $.ajax({
            url: '/logged/markAll',
            type: "post",
            success: function (result) {
                if (result.isSuccess == true) {
                    $('#unreadDrag').text(0);
                    return;
                } else {
                    $.messager.popup(result.detail);
                }
            },
            error: function (msg) {
                $.messager.popup("Internal Error");
            }
        });
    }

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

    $("#passageFormSubmit").on("click", function (event) {
        event.preventDefault();
        hideError();
        var content = $("#content").html();
        var data = $("#passageForm").serialize();
        $.ajax({
            url: '/manage/passage/doCreateOrUpdate?' + data,
            type: "post",
            data: {
                content: content,
            },
            success: function (result) {
                if (result.isSuccess == true) {
                    $(window).unbind("beforeunload");
                    window.location.href = result.detail;
                    return;
                } else {
                    showError(result.detail);
                }
            },
            error: function (msg) {
                showError("Internal Error");
            }
        });
    });

    //$("#loginFormSubmit").on("click", function (event) {
    //    event.preventDefault();
    //    hideError();
    //    var data = $("#loginForm").serialize();
    //    $.ajax({
    //        url: '/doLogin',
    //        type: "post",
    //        data: data,
    //        success: function (result) {
    //            if (result.isSuccess == true) {
    //                window.location.href = result.detail;
    //                return;
    //            } else {
    //                showError(result.detail);
    //            }
    //        },
    //        error: function (msg) {
    //            showError("Internal Error");
    //        }
    //    });
    //});
});

// Navigation Scripts to Show Header on Scroll-Up
jQuery(document).ready(function ($) {
    var MQL = 1170;

    //primary navigation slide-in effect
    if ($(window).width() > MQL) {
        var headerHeight = $('.navbar-custom').height();
        $(window).on('scroll', {previousTop: 0}, function () {
            var currentTop = $(window).scrollTop();
            //check if user is scrolling up
            if (currentTop < this.previousTop) {
                //if scrolling up...
                if (currentTop > 0 && $('.navbar-custom').hasClass('is-fixed')) {
                    $('.navbar-custom').addClass('is-visible');
                } else {
                    $('.navbar-custom').removeClass('is-visible is-fixed');
                }
            } else {
                //if scrolling down...
                $('.navbar-custom').removeClass('is-visible');
                if (currentTop > headerHeight && !$('.navbar-custom').hasClass('is-fixed')) $('.navbar-custom').addClass('is-fixed');
            }
            this.previousTop = currentTop;
        });
    }
});

function insertNodeAtCursor(node) {
    var sel, range, html;
    if (window.getSelection) {
        sel = window.getSelection();
        if (sel.getRangeAt && sel.rangeCount) {
            sel.getRangeAt(0).insertNode(node);
        }
    } else if (document.selection && document.selection.createRange) {
        range = document.selection.createRange();
        html = (node.nodeType == 3) ? node.data : node.outerHTML;
        range.pasteHTML(html);
    }
}