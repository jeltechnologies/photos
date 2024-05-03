var populatingCustomPeriods = false;
var mapLoaded = false;
var userLat;
var userLng;
var startLat;
var startLng;
var startZoom;
var map;
var myRenderer;
var markers;

var mode;
var from;
var to;
var mediatype;
var title;
var album;

const photosLayerGroup = L.layerGroup();
const videosLayerGroup = L.layerGroup();

function setMode() {
	const query = window.location.search;
	const p = new URLSearchParams(query);
	from = p.get("from");
	to = p.get("to");
	mediatype = p.get("mediatype");
	title = p.get("title");
	album = p.get("album");
	if (defined(album)) {
		mode = "album";
	} else {
		if (defined(from) && defined(to)) {
			mode = "timeline";
		} else {
			mode = "free";
		}
	}
	//console.log("setMode: " + mode);
}

function defined(variable) {
	return variable != undefined && variable != null && variable !== "";
}

function populateCustomPeriods() {
	populatingCustomPeriods = true;
	let dateLocale = window.navigator.userLanguage || window.navigator.language;
	let DateTime = luxon.DateTime;

	let oneMonth = luxon.Duration.fromObject({ months: 1 });

	let date = DateTime.local(2017, 1, 1, 1, 1);
	let year = date.year;
	let startYear = year;
	while (year === startYear) {
		let monthName = date.toFormat("MMMM");
		addOption("#customperiod-from-month", monthName, date.month);
		addOption("#customperiod-to-month", monthName, date.month);
		date = date.plus(oneMonth);
		year = date.year;
	}

	let earliestPhoto = DateTime.fromISO(earliestPhotoFromRequest).setLocale(dateLocale);
	let earliestYear = earliestPhoto.year;
	let now = DateTime.now();
	let thisYear = now.year;

	for (let year = thisYear; year >= earliestYear; year--) {
		addOption("#customperiod-from-year", year);
		addOption("#customperiod-to-year", year, year, false);
	}

	let selectedMonth = now.month;
	let selectedYear = thisYear - 1;

	$("#customperiod-from-month").val(selectedMonth);
	$("#customperiod-from-year").val(selectedYear);

	$("#customperiod-to-month").val(selectedMonth);
	$("#customperiod-to-year").val(thisYear);

	if (mode === "free") {
		$("#period").show();
	} else {
		$("#period").hide();
		$("#map-title").html(title);
	}

	populatingCustomPeriods = false;
}

function addOption(selectId, optText, optValue) {
	$(selectId).append(new Option(optText, optValue));
}

function periodChanged() {
	if (!populatingCustomPeriods) {
		let elementId = '#period';
		let period = $(elementId).val();
		let custom = period === "custom";
		if (custom) {
			$("#customperiod").show();
		} else {
			$("#customperiod").hide();
		}
		postLocation();
	}
}

function startMap() {
	//console.log("startMap");
	const mbAttr = 'Map data &copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors, Imagery © <a href="https://www.mapbox.com/">Mapbox</a>';
	const mbUrl = 'https://api.mapbox.com/styles/v1/{id}/tiles/{z}/{x}/{y}?access_token=' + accessToken;

	const streets = L.tileLayer(mbUrl, { id: 'mapbox/streets-v11', tileSize: 512, zoomOffset: -1, attribution: mbAttr });

	const osm = L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
		maxZoom: 19,
		attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>'
	});

	const satellite = L.tileLayer(mbUrl, { id: 'mapbox/satellite-v9', tileSize: 512, zoomOffset: -1, attribution: mbAttr });

	map = L.map('map', {
		center: [startLat, startLng],
		zoom: startZoom,
		layers: [streets, photosLayerGroup, videosLayerGroup]
	});

	const baseLayers = {
		'Streets': streets,
		'Satellite': satellite,
		'Terrain': osm
	};

	const overlays = {
		'Photos': photosLayerGroup,
		'Videos': videosLayerGroup
	};

	L.control.layers(baseLayers, overlays).addTo(map);

	map.on("moveend", mapChanged);

	//console.log("end startMap");
}

function receivePhotos(data) {
	//console.log("receivePhotos: " + data);
	photosLayerGroup.clearLayers();
	videosLayerGroup.clearLayers();
	let photos = enforceArrayFromElements(data);
	if (photos != undefined) {
		for (let i = 0; i < photos.length; i++) {
			let photo = photos[i];
			let lat = photo.coordinates.latitude;
			let lng = photo.coordinates.longitude;
			let html = getPopupHtml(photo);
			let layerGroup;
			if (photo.type === "Photo") {
				layerGroup = photosLayerGroup;
			} else {
				layerGroup = videosLayerGroup;
			}
			var latlng = L.latLng(lat, lng);
			L.circleMarker(latlng, {
				color: '#FF6A5C',
				radius: 10,
				renderer: myRenderer
			}).addTo(layerGroup).bindPopup(html, {
				className: 'map-popup',
				maxWidth : 560
			});
		}
	}
}

function mapChanged() {
	setTimeout(postLocation, 200);
}

function postLocation() {
	if (mapLoaded) {
		let bounds = map.getBounds();
		let mapCenter = map.getCenter();
		let zoom = map.getZoom();
		let northEast = bounds._northEast;
		let southWest = bounds._southWest;

		let url = "map";
		url = url + "?mode=" + mode;

		url = url + "&mapCenterLat=" + mapCenter.lat;
		url = url + "&mapCenterLng=" + mapCenter.lng;
		url = url + "&northEastLat=" + northEast.lat;
		url = url + "&northEastLng=" + northEast.lng;
		url = url + "&southWestLat=" + southWest.lat;
		url = url + "&southWestLng=" + southWest.lng;
		url = url + "&zoom=" + zoom;

		if (mode === "free") {
			let period = $("#period").val();
			url = url + "&period=" + period;
			if (period === "custom") {
				url = url + "&fromMonth=" + $("#customperiod-from-month").val();
				url = url + "&fromYear=" + $("#customperiod-from-year").val();
				url = url + "&toYear=" + $("#customperiod-to-year").val();
			}
		}

		if (mode === "album") {
			url = url + "&album=" + encodeURIComponent(album);
			url = url + "&title=" + encodeURIComponent(title);
		}

		if (mode === "timeline") {
			url = url + "&from=" + encodeURIComponent(from);
			url = url + "&to=" + encodeURIComponent(to);
			url = url + "&title=" + encodeURIComponent(title);
		}

		if (period === "custom") {
			url = url + "&fromMonth=" + $("#customperiod-from-month").val();
			url = url + "&fromYear=" + $("#customperiod-from-year").val();
			url = url + "&toMonth=" + $("#customperiod-to-month").val();
			url = url + "&toYear=" + $("#customperiod-to-year").val();
		}
		//console.log("mode: " + mode + " => " + url);
		getJson(url, receivePhotos);
	}
}

function getPopupHtml(photo) {
	let link = "photo.jsp?photo=" + encodeURIComponent(photo.id);
	link = link + "&album=" + encodeURIComponent(photo.relativeFolderName);
	let imageHtml = "<img class='map-popup-image' src='img?id=" + photo.id + "&size=small'></div>";
	//let dateLabel = getDateLabel(photo.dateTaken);
	let title = getTitle(photo);
	let html = "<p><a href='" + link + "'>" + imageHtml + "</a></p><p>" + title + "</p>";
	return html;
}

function getLocation() {
	if (navigator.geolocation) {
		navigator.geolocation.getCurrentPosition(showPosition, showError);
	} else {
		console.log("Geolocation is not supported by this browser.");
		handleGotLocation(false);
	}
}

function showPosition(position) {
	userLat = position.coords.latitude;
	userLng = position.coords.longitude;
	handleGotLocation(true);
}

function showError(error) {
	let errorMessage;
	switch (error.code) {
		case error.PERMISSION_DENIED:
			errorMessage = "User denied the request for geolocation.";
			break;
		case error.POSITION_UNAVAILABLE:
			errorMessageL = "Location information is unavailable.";
			break;
		case error.TIMEOUT:
			errorMessage = "The request to get user location timed out.";
			break;
		case error.UNKNOWN_ERROR:
			errorMessage = "An unknown error occurred.";
			break;
	}
	console.log("Warning: " + errorMessage);
	handleGotLocation(false);
}

function handleGotLocation(success) {
	//console.log("handleGotLocation(" + success + ") " + "lat: " + userLat + ", lng: " + userLng);
	if (success) {
		startLat = userLat;
		startLng = userLng;
		startZoom = defaultZoom;
	} else {
		startLat = startLat;
		startLng = startLng;
		startZoom = userZoom;
	}
	//console.log(startLat + "," + startLng + " zoom: " + startZoom);
	startMap();
	mapLoaded = true;
	postLocation();
}

function pageReady() {
	mapLoaded = false;
	setMode();
	populateCustomPeriods();
	getLocation();
}