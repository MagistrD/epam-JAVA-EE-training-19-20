$(document).ready(function() {
    changePageAndSize();
});

function changePageAndSize() {
    $('#pageSizeSelect').change(function(evt) {
        window.location.replace("/?pageSize=" + this.value + "&page=1");
    });
}


function deleteData(type, id) {
    $.ajax({
        type: "DELETE",
        url: type + "/delete/" + id,
        success: function () {
            displayList(type)
        }
    })
}

function displayList(type) {
    $.ajax({
        type: "GET",
        url: "/" + type,

    })

}