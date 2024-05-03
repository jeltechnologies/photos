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
						<icons:icon name="info" size="64" onclick="showInfo();" />
					</div>
					<div class="view-menu-icon">
						<svg onclick="showOptions();" width="64" height="64" fill="currentColor" xmlns="http://www.w3.org/2000/svg" shape-rendering="geometricPrecision" text-rendering="geometricPrecision"
							image-rendering="optimizeQuality" fill-rule="evenodd" clip-rule="evenodd" viewBox="0 0 512 513.11">
							<path fill-rule="nonzero"
								d="M210.48 160.8c0-14.61 11.84-26.46 26.45-26.46s26.45 11.85 26.45 26.46v110.88l73.34 32.24c13.36 5.88 19.42 21.47 13.54 34.82-5.88 13.35-21.47 19.41-34.82 13.54l-87.8-38.6c-10.03-3.76-17.16-13.43-17.16-24.77V160.8zM5.4 168.54c-.76-2.25-1.23-4.64-1.36-7.13l-4-73.49c-.75-14.55 10.45-26.95 25-27.69 14.55-.75 26.95 10.45 27.69 25l.74 13.6a254.258 254.258 0 0136.81-38.32c17.97-15.16 38.38-28.09 61.01-38.18 64.67-28.85 134.85-28.78 196.02-5.35 60.55 23.2 112.36 69.27 141.4 132.83.77 1.38 1.42 2.84 1.94 4.36 27.86 64.06 27.53 133.33 4.37 193.81-23.2 60.55-69.27 112.36-132.83 141.39a26.24 26.24 0 01-12.89 3.35c-14.61 0-26.45-11.84-26.45-26.45 0-11.5 7.34-21.28 17.59-24.92 7.69-3.53 15.06-7.47 22.09-11.8.8-.66 1.65-1.28 2.55-1.86 11.33-7.32 22.1-15.7 31.84-25.04.64-.61 1.31-1.19 2-1.72 20.66-20.5 36.48-45.06 46.71-71.76 18.66-48.7 18.77-104.46-4.1-155.72l-.01-.03C418.65 122.16 377.13 85 328.5 66.37c-48.7-18.65-104.46-18.76-155.72 4.1a203.616 203.616 0 00-48.4 30.33c-9.86 8.32-18.8 17.46-26.75 27.29l3.45-.43c14.49-1.77 27.68 8.55 29.45 23.04 1.77 14.49-8.55 27.68-23.04 29.45l-73.06 9c-13.66 1.66-26.16-7.41-29.03-20.61zM283.49 511.5c20.88-2.34 30.84-26.93 17.46-43.16-5.71-6.93-14.39-10.34-23.29-9.42-15.56 1.75-31.13 1.72-46.68-.13-9.34-1.11-18.45 2.72-24.19 10.17-12.36 16.43-2.55 39.77 17.82 42.35 19.58 2.34 39.28 2.39 58.88.19zm-168.74-40.67c7.92 5.26 17.77 5.86 26.32 1.74 18.29-9.06 19.97-34.41 3.01-45.76-12.81-8.45-25.14-18.96-35.61-30.16-9.58-10.2-25.28-11.25-36.11-2.39a26.436 26.436 0 00-2.55 38.5c13.34 14.2 28.66 27.34 44.94 38.07zM10.93 331.97c2.92 9.44 10.72 16.32 20.41 18.18 19.54 3.63 36.01-14.84 30.13-33.82-4.66-15-7.49-30.26-8.64-45.93-1.36-18.33-20.21-29.62-37.06-22.33C5.5 252.72-.69 262.86.06 274.14c1.42 19.66 5.02 39 10.87 57.83z" /></svg>
					</div>
					<div class="view-menu-icon">
						<icons:icon name="share" size="64" onclick="shareClicked();" />
					</div>
					<div class="view-menu-icon">
						<icons:icon name="trash" size="64" onclick="hidePhoto();" />
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