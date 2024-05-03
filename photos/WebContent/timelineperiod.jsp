<%@page import="com.jeltechnologies.icons.IconTag"%>
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@page import="com.jeltechnologies.photos.Environment"%>
<%@ taglib prefix="photos" uri="WEB-INF/tags.tld"%>
<%@ taglib prefix="icons" uri="jeltechnologies-icons"%>
<jsp:include page="head.jsp"></jsp:include>
<link rel="stylesheet" type="text/css" href="files/leaflet-1.9.3/leaflet.css">
<script src="files/leaflet-1.9.3/leaflet.js"></script>
<script src="app/timelineperiod.js"></script>
<link rel="stylesheet" type="text/css" href="app/photos.css" />

<title>Photos | Timeline</title>

<script>
	const mapAccessToken = "<%=Environment.INSTANCE.getConfig().getMapBoxAccessToken()%>";
	const PLAY_ICON = '<%=new IconTag("play").toString()%>';
</script>

</head>

<body>
	<div id="top-bar">
		<div id="top-bar-center" class="top-menu">
			<photos:menu-timeline />
			<photos:main-menu selected="Timeline" />
		</div>
	</div>

	<div id="visible-albums">
		<div id="album" class="album">
			<div id="image-cover-album"></div>
			<div id="map-album"></div>
			<br style="clear: both" />
			<p>
				<span style="float: left">
					<button onclick="window.history.back();" class="backbtn">
						<icons:icon name="left" />
					</button>
				</span> 
				<span id="previousButton"></span> 
				<span id="album-title" class="album-title">Title</span> 
				<span id="nextButton"></span> 
				<select id="sorting" onchange='sortingChanged()' class="select-filter">
					<option value="NEWESTFIRST" selected>Newest first</option>
					<option value="OLDESTFIRST">Oldest first</option>
				</select>
			</p>
			<div id="album-items"></div>
		</div>
	</div>

</body>

</html>
