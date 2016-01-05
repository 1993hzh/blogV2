function showUploadProgress(file) {
    var html = "<div id=\""+ file.id +"\"class=\"progress\"><div class=\"progress-bar progress-bar-striped active\" role=\"progressbar\" "
        + "aria-valuenow=\"0\" aria-valuemin=\"0\" aria-valuemax=\"100\"><span class=\"sr\">"+ file.name +"&nbsp;-&nbsp;</span>" +
        "<label class=\"percent\">0%</label></div></div>";
    $("#fileUploadProgress").append(html);
}

var updateProgress = function(id, percent) {
    var parent = $("#"+id);
    var progressbar = parent.find("div[role='progressbar']");
    if (percent == 100) {
        progressbar.removeClass("active");
    }
    progressbar.attr("aria-valuenow", percent);
    progressbar.width(percent + "%");
    parent.find("label[class='percent']").html(percent + "%");
}

var addImage = function(src, id, name) {
    var img = $("<img />");
    img.attr("src", src);
    img.attr("id", id);
    img.attr("name", name);
    img.addClass("qiniu_image");
    insertNodeAtCursor($(img)[0]);
}

function removeImg(obj) {
    $.ajax({
        url: '/manage/deleteFile',
        type: "GET",
        data: "fileName=" + obj.name,
        success: function (result) {
            if (result.isSuccess != true) {
                $.messager.popup(result.detail);
                insertNodeAtCursor($(obj)[0]);
            }
        },
        error: function (msg) {
            $.messager.popup("Internal Error");
        }
    })
}

$(function () {
    var observer = new MutationObserver(function (mutations) {
        mutations.forEach(function (mutation) {
            $(mutation.removedNodes).each(function (value, index) {
                // here listen the image change
                if (this.className === "qiniu_image" && !isToggleHtml(this)) {
                    removeImg(this)
                }
            });
        });
    });
    var config = {attributes: true, childList: true, characterData: true};
    var isToggleHtml = function(obj) {
        return $('#content').html().contains(obj.name);
    }
    observer.observe($('#content')[0], config);
});