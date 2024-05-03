<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="photos" uri="WEB-INF/tags.tld"%>

<jsp:include page="head.jsp"></jsp:include>
<link rel="stylesheet" type="text/css" href="app/photos.css" />
<jsp:include page="albums.css.jsp"></jsp:include>

<link rel="stylesheet" href="files/vakata-jstree-3.3.12/themes/default/style.min.css" />
<script src="files/vakata-jstree-3.3.12/jstree.min.js"></script>

<title>Photos - New</title>

<style>
#jstree {
	display: none;
	border: 1px solid black;
	border-radius: 5px;
}
</style>
</head>

<body>

	<div id="top-bar">
		<div id="top-bar-left">
			<div id="album-name-generated">Choose an album to add photos</div>
			<div id="album-name-editor"></div>
		</div>
	</div>

	<photos:albums-tree id="jstree"/>
	
	
	<button onclick="userPressedAdd()">Add to album</button>
	<button onclick="userPressedCancel()">Cancel</button>

	<script>
		const NODE_ID = "node-";
		
		var selectedAlbum = "";
		
		function userPressedAdd() {
			postJson("add-photos-to-album", selectedAlbum, addedPhotosComplete, addedPhotosFailed);
		}
		
		function addedPhotosComplete() {
			window.open("timeline.jsp", "_self");
		}
		
		function addedPhotosFailed() {
			alert("Could not add photos to album");
		}
		
		function userPressedCancel() {
			alert("Cancel");
		}

		function albumNodeClicked(id) {
			let idString = "" + id;
			let idNumber = idString.substring(NODE_ID.length);
			let relativeFolder = albumTreeIds[idNumber];
			selectedAlbum = relativeFolder;
		}

		$(function() {
			$('#jstree').jstree();

			$('#jstree').on("changed.jstree", function(e, data) {
				albumNodeClicked(data.selected);
				console.log(data.selected);
			});

			if (albumTreeOpenNodeID != undefined) {
				$('#jstree').jstree().open_node(albumTreeOpenNodeID);
				$('#jstree').jstree().select_node(albumTreeOpenNodeID);
			}

			$('#jstree').show();

		});
	</script>

</body>

</html>