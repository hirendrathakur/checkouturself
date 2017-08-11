
$(".choose-profile").click(function(){
	var profile = $(this).data("profile");
	localStorage.setItem("profile", profile);
	$("#homescreen").show();
	$("#welcome").hide();
	$("#preview").attr("src", "/resources/people/"+profile+".jpg")
	return false;
})

$("#camera").click(function(){
	 $("#capture-input").click();
	 return false;
})

$(".dress-change").click(function () {
	var profile = localStorage.getItem("profile");
	var did = $(this).data("dressid");
	console.log(did);
	$("#loader").show();
	$("#preview").one("load", function(state) {
        // image loaded here
        console.log("succsess")
        console.log(state);
        $("#loader").hide();
    }).attr("src", "http://172.20.44.62:9090/getTheLook?profileId="+profile+"&productId="+did);

	return false;
})


$("#capture-input").on('change', function(e){

      var file = e.target.files[0]; 
    // frame.src = URL.createObjectURL(file);
    var formData = new FormData();
    formData.append("file", file);
    formData.append("name", "lorem");
    $("#loader").show();
    $.ajax({
        url: "http://172.20.44.62:9090/upload",
        type: 'POST',
        data: formData,
        dataType: "json",
        contentType: false,
        async: true,
        success: function (result) {
            console.log(result)
            profile = result.response.data.profileId
            console.log("profile="+profile)
            $("#loader").hide();
         	localStorage.setItem("profile", profile);
         	$("#homescreen").show();
			$("#welcome").hide();
			$("#preview").attr("src", "http://172.20.44.62:9090/getImage?profileId="+profile)
        },
        error: function (jqXHR, exception) {
            console.log(jqXHR);
             $("#loader").hide();
        },
        cache: false,
        processData: false
    });

    return false;
  })
