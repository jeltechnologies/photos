<%@page import="com.jeltechnologies.photos.utils.StringUtils"%>
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@page import="com.jeltechnologies.photos.datatypes.usermodel.RoleModel"%>
<%@page import="com.jeltechnologies.photos.datatypes.usermodel.User"%>
<%@page import="com.jeltechnologies.photos.Settings"%>
<%@page import="com.jeltechnologies.photos.Environment"%>
<%@ taglib prefix="photos" uri="WEB-INF/tags.tld"%>

<%
	String queryString = request.getQueryString();
	if (queryString == null) {
	    queryString = "";
	}
	String randomParam = (String) request.getParameter("random");
	boolean random = Boolean.parseBoolean(randomParam);

	User user = RoleModel.getUser(request);
	
	String album = (String) request.getParameter("album");
	String albumEncoded = StringUtils.encodeURL(album);
%>

<jsp:include page="head.jsp"></jsp:include>
<link rel="stylesheet" type="text/css" href="app/photos.css" />
<link rel="stylesheet" type="text/css" href="files/leaflet-1.9.3/leaflet.css">
<script src="files/leaflet-1.9.3/leaflet.js"></script>

<title>Photos - Albums</title>

</head>

<body>
	<div id="top-bar">
		<div id="top-bar-center" class="top-menu">
			<photos:menu-albums />
			<photos:main-menu selected="Albums"/>
		</div>
	</div>
	<photos:albumlist id="album" cssClass="album" />
</body>

<script>

var photosOnPage = <%=pageContext.getAttribute("photos-on-page")%>;

function addNewClicked() {
	location.replace("add-new.jsp?album=/Uncategorized");
}

function removePicturesClicked() {
	location.replace("remove-from-album.jsp?album=<%=albumEncoded%>");
}

function downloadAlbumClicked(source) {
	let fileName = '<%=album%>.zip';
	window.location = 'download' + fileName + "?source=" + source;
}

function replaceUrlParam(url, paramName, paramValue) {
    if (paramValue == null) {
        paramValue = '';
    }
    var pattern = new RegExp('\\b('+paramName+'=).*?(&|#|$)');
    if (url.search(pattern)>=0) {
        return url.replace(pattern,'$1' + paramValue + '$2');
    }
    url = url.replace(/[?#]$/,'');
    return url + (url.indexOf('?')>0 ? '&' : '?') + paramName + '=' + paramValue;
}

function init() {
	$('#chk-random').prop('checked', <%=random%>);
	$('#chk-random').click(handleRandom);
	if (document.getElementById("map-album")) {
		let startLat = 30;
		let startLng = 0;
		let startZoom = 1;
		startMap(startLat, startLng, startZoom);
		addPhotosToMap();
	}
}

function handleRandom() {
	let random = $('#chk-random').prop('checked');
	let url = "albumlist.jsp?random=" + random;
	let albumEncoded = "<%=albumEncoded%>";
	if (albumEncoded !== "null") { 
		url = url + "&album=" + albumEncoded;
	}
	$("#visible-albums").load(url);
}

function handleRandomOld() {
	var currentUrl= "albums.jsp?<%=queryString%>";
	var newUrl = replaceUrlParam(currentUrl, "random", <%=!random%>);
	location.replace(newUrl);
}

function handleEditUserOK() {
	var albumUpdate = {};
	albumUpdate.relativeFileName = "<%=request.getParameter("album")%>";
	albumUpdate.name = $("#txtName").val();
	console.log(albumUpdate);
	postJson("album", albumUpdate, renameAlbumCompleted);
}

function handleEditUserCancel() {
	closeModal();
}

function renameAlbumCompleted() {
	location.reload();
}

function renameAlbumClicked() {
	let albumEncoded = "<%=StringUtils.encodeURL(album)%>";
	let openUrl = "editalbum.jsp?album=" + albumEncoded
	openModalWebPage($('#set-album-cover-modal'), openUrl);
}

function startMap(startLat, startLng, startZoom) {
	map = L.map('map-album').setView([startLat, startLng], startZoom);
	myRenderer = L.canvas({ padding: 0.5 });
	L.tileLayer('https://api.mapbox.com/styles/v1/{id}/tiles/{z}/{x}/{y}?access_token={accessToken}', {
    	attribution: 'Map data &copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors, Imagery © <a href="https://www.mapbox.com/">Mapbox</a>',
    	maxZoom: 25,
    	id: 'mapbox/streets-v11',
    	tileSize: 512,
    	zoomOffset: -1,
    	dragging: false,
    	doubleClickZoom: false,
    	accessToken: '<%=Environment.INSTANCE.getConfig().getMapBoxAccessToken()%>'
	}).addTo(map);
}

function addPhotosToMap() {
	let photos = enforceArrayFromElements(photosOnPage);
	if (photos != undefined) {
		for (let i = 0; i < photos.length; i++) {
			let photo = photos[i];
			let coordinates = photo.coordinates;
			if (coordinates != undefined && coordinates !== null) {
				let lat = coordinates.latitude;
				let lng = coordinates.longitude;
				let html = photo.html; 
				//console.log("lat: " + lat + ", lng: " + lng);
				if (lat != undefined && lng != undefined) {
					var latlng = L.latLng(lat, lng);
					var circleMarker = L.circleMarker(latlng, {
			    	color: '#3388ff',
			    	radius: 3,
				    renderer: myRenderer
				}).addTo(map).bindPopup(html);
				}
			}
		}
		if (photos.length == 0) {
			$('#map-album').hide();
		}
	}
	map.on('click', function(e){
		const queryString = window.location.search;
		const p = new URLSearchParams(queryString);
		let album = p.get("album");
		let title = '<%=pageContext.getAttribute("album-title")%>';
		let url = "map.jsp?album=" + encodeURIComponent(album) + "&mediatype=all&title=" + encodeURIComponent(title);
		window.open(url, "_self")
    });
}
	
$(document).ready(init());

</script>

</html>
