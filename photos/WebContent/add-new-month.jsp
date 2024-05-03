<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="photos" uri="WEB-INF/tags.tld"%>

<%
String year = request.getParameter("year");
String month = request.getParameter("month");
if (month.length() == 1) {
	month = "0" + month;
}
String title = year + "-" + month;
%> 

<jsp:include page="head.jsp"></jsp:include>

<title>Photos - Add</title>

</head>

<body>
	<div id="top-bar">
		<div id="top-bar-center" class="top-menu">
			<photos:menu-albums />
			<photos:main-menu selected="Add"/>
		</div>
	</div>
	<div id="album-name-editor"></div>

	<button onclick="userClickedAddToAlbum();">Add</button>
	
	<photos:manage-photos/>
	
	<button onclick="userClickedAddToAlbum();">Add</button>

	<form name="selectedPhotosForm" id="selectedPhotosForm" action="add-photos-prepare-album" method="post">
		<input type="hidden" id="selectedPhotos" name="selectedPhotos">
	</form>

</body>

<script>
	//function userClickedPhoto(id) {
	//	console.log("userClickedPhoto(id = " + id + ")");
		//let i = '#' + id;
	//	$(this).toggleClass("selected-photo");
	//}

	function userClickedAddToAlbum() {
		const ids = new Array();

		$('.selected-photo').each(function(i, obj) {
			ids.push(obj.id);
		});

		let selected = "";
		for (let i = 0; i < ids.length; i++) {
			let id = ids[i];
			if (i > 0) {
				selected = selected + ",";
			}
			selected = selected + id;
		}
		
		$("#selectedPhotos").val(selected);
		console.log(selected);
		$("#selectedPhotosForm").submit();

	}
</script>

</html>