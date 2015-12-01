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

var addImage = function(src) {
    var img = $("<img />");
    img.attr("src", src);
    $("#content").append($(img));
}