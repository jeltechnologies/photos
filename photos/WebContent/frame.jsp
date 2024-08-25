<%@page import="com.jeltechnologies.photos.tags.PhotoGallerySwiper"%>
<%@page import="com.jeltechnologies.photos.Settings"%>
<%@page import="com.jeltechnologies.photos.Environment"%>
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="photos" uri="WEB-INF/tags.tld"%>
<%@ taglib prefix="icons" uri="jeltechnologies-icons"%>
<%@ include file="head.jsp"%>

<title>Photo Frame</title>

<link rel="stylesheet" type="text/css" href="files/swiper-11.0.5/swiper-bundle.min.css">
<script src="files/swiper-11.0.5/swiper-bundle.min.js"></script>
<link rel="stylesheet" type="text/css" href="files/leaflet-1.9.3/leaflet.css">
<script src="files/leaflet-1.9.3/leaflet.js"></script>
<link rel="stylesheet" type="text/css" href="app/frame.css">
<script src="app/frame.js"></script>

</head>

<body>
	<div class="swiper mySwiper" onclick="userClickedSwiper();">
		<div class="swiper-wrapper"></div>
		<div id="navigation">
			<div class="swiper-button-next"></div>
			<div class="swiper-button-prev"></div>

			<div id="view-menu">
				<div class="view-icons">
					<div id="view-menu-title" class="view-menu-title">All photos and videos</div>
					<div class="view-menu-icon">
						<icons:icon name="info-circle-fill" size="64" onclick="showInfo();" />
					</div>
					<div class="view-menu-icon">
						<icons:icon name="clock-history" size="64" onclick="showOptions();" />
					</div>
					<div class="view-menu-icon">
						<icons:icon name="share-fill" size="64" onclick="shareClicked();" />
					</div>
					<div class="view-menu-icon">
						<icons:icon name="trash3-fill" size="64" onclick="hidePhoto();" />
					</div>
				</div>
			</div>
		</div>
	</div>

	<div id="info-modal" class="modal">
		<div id="infos">
			<div class="photo-info-3-columns">
				<div id="photo-info-date"></div>
				<div id="photo-info-ago"></div>
				<div id="photo-info-source"></div>
			</div>
			<div class="mapflexbox">
				<div id="map"></div>
			</div>
			<div class="photo-info-1-column">
				<div id="photo-info-location"></div>
			</div>
		</div>
	</div>

	<div id="program-modal" class="modal">
		<form id="program-form" action="frame.jsp" method="get">
			<div id="frame-program-groups" class="options"></div>
			<hr>
			<div class="option-center" id="options-mix">
				<div class="option-random-mix">
					<p>
						<strong>Mix</strong>
					</p>
					<input type="range" id="program-percentage" name="program-percentage" min="0" max="100" step="25" value="80"> <label id="program-percentage-label" for="program-percentage">80%</label>
					<button class="option-button" onclick="refreshWithNewProgram()">Go</button>
					<input type="hidden" id="program" name="program"> 
					<input type="hidden" id="photo-in-slideshow" name="photo-in-slideshow">
				</div>
			</div>
		</form>
	</div>

	<div id="hide-photo-modal" class="modal">
		<p>Hide this photo or video?</p>
		<button class="option-button" onclick="hidePhotoOK()">Yes</button>
		<button class="option-button" onclick="hidePhotoCancel();">No</button>
	</div>

</body>
</html>