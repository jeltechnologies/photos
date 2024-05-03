var photos;
var sorting;
var grouping;
var cover;
var timeline;
var title;
var period;
var nextButton;
var previousButton;

function addNewClicked() {
	location.replace("add-new.jsp?album=/Uncategorized");
}

function sortingChanged() {
	getPhotos();
}

function initParameters() {
	let urlParams = new URLSearchParams(window.location.search);
	let urlFrom = urlParams.get('from');
	let urlTo = urlParams.get('to');
	timeline = getSessionStorageTimeline();
	if (timeline == undefined) {
		console.log("Take the information from the request parameters");
		mediatype = urlParams.get('mediatype');
		if (mediatype === undefined || mediatype === null) {
			mediatype = "all";
		}
		sort = urlParams.get('sort');
		if (sort === undefined || sort === null) {
			sort = "OLDESTFIRST";
		}
		$("#sorting").val(sort);
		coverPhoto = urlParams.get('photo');
		period = {
			cover: { id: coverPhoto },
			from: urlFrom,
			to: urlTo
		}
		timeline = {
			curentPage: 0,
			payload: payload,
			mediaType: mediatype,
			sorting: sorting
		};
	} else {
		let itemFound = -1;
		for (let i = 0; i < timeline.payload.length; i++) {
			let item = timeline.payload[i];
			if (item.from === urlFrom && item.to === urlTo) {
				itemFound = i;
			}
		}
		if (itemFound > -1) {
			period = timeline.payload[itemFound];
		}

		if (itemFound > 0) {
			let previous = timeline.payload[itemFound - 1];
			let previousTitle = getTimelineTitle(previous.from, previous.to, timeline.grouping);
			previousButton = getLink(previousTitle, previous.from, previous.to);
		}
		if (itemFound < (timeline.payload.length - 1)) {
			let next = timeline.payload[itemFound + 1];
			let nextTitle = getTimelineTitle(next.from, next.to, timeline.grouping);
			nextButton = getLink(nextTitle, next.from, next.to);
		}

		console.log("Reuse stored timeline and found period " + period.from + "-" + period.to);
	}
	console.log(timeline);
}

function getLink(title, from, to) {
	let linkJs = 'goToPeriod("' + from + '", "' + to + '");';
	let html = "<button onclick='" + linkJs + "' class=\"gotoperiodbtn\">";
	html += title + "</button>";
	console.log(html);
	return html;
}

function goToPeriod(from, to) {
	let link = "timelineperiod.jsp?from=" + from + "&to=" + to;
	window.open(link, "_self");
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
		accessToken: mapAccessToken
	}).addTo(map);
}

function addPhotosToMap() {
	//console.log(photos);
	if (photos != undefined) {
		for (let i = 0; i < photos.length; i++) {
			let photo = photos[i];
			let coordinates = photo.coordinates;
			if (coordinates != undefined && coordinates !== null) {
				let lat = coordinates.latitude;
				let lng = coordinates.longitude;
				let html = photo.html;
				if (lat != undefined && lng != undefined) {
					var latlng = L.latLng(lat, lng);
					L.circleMarker(latlng, {
						color: '#3388ff',
						radius: 3,
						renderer: myRenderer
					}).addTo(map).bindPopup(html);
				}
			}
		}
	}
	map.on('click', function(e) {
		const queryString = window.location.search;
		const p = new URLSearchParams(queryString);
		let from = p.get("from");
		let to = p.get("to");
		let mediatype = p.get("mediatype");
		let title = p.get("title");
		let url = "map.jsp?from=" + from + "&to=" + to + "&mediatype=" + mediatype + "&title=" + encodeURIComponent(title);
		window.open(url, "_self")
	});
}

function getPhotos() {
	let url = "timeline?from=" + period.from;
	url = url + "&to=" + period.to;
	url = url + "&mediatype=" + timeline.mediaType;
	getJson(url, receivePhotos);
}

function receivePhotos(data) {
	photos = enforceArrayFromElements(data);
	let html = "<ul>";
	if (photos != undefined) {
		let sort = $("#sorting").val();
		console.log(sort);
		if (sort === "OLDESTFIRST") {
			for (let i = 0; i < photos.length; i++) {
				html += renderPhoto(i);
			}
		} else {
			for (let i = photos.length - 1; i >= 0; i--) {
				html += renderPhoto(i);
			}
		}
	}
	html = html + "<li></li></ul>";
	$('#album-items').html(html);
	let startLat = 30;
	let startLng = 0;
	let startZoom = 1;
	startMap(startLat, startLng, startZoom);
	addPhotosToMap();
	let coverPhoto = period.cover;
	console.log("CoverPhoto: " + coverPhoto);
	if (coverPhoto != undefined && coverPhoto != null && coverPhoto != "") {
		let link = "photo.jsp?photo=" + coverPhoto.id;
		let imgsrc = "img?id=" + coverPhoto.id + "&size=small";
		let html = "<a href='" + link + "'><img class='image-cover-album' src='" + imgsrc + "'></a>";
		$("#image-cover-album").html(html);
		console.log(html);
	}
	title = getTimelineTitle(period.from, period.to, timeline.grouping);
	$('#album-title').html(title);

	if (previousButton != undefined) {
		$("#previousButton").html(previousButton);
	}
	if (nextButton != undefined) {
		$("#nextButton").html(nextButton);
	}
}

function renderPhoto(photoIndex) {
	let photo = photos[photoIndex];
	let html = "";
	let id = photo.id;
	let link = "photo.jsp?photo=" + id + "&from=" + period.from + "&to=" + period.to + "&sort=" + "&mediatype=" + timeline.mediatype;
	html += "<li><a href='" + link + "'>";
	let image = "img?id=" + id + "&size=small";
	let sizeHtml = getDimensionHtml(photo);
	let loading;
	if (photoIndex < 10) {
		loading = "eager";
	} else {
		loading = "lazy";
	}
	let img = "<img class='image-in-album' src='" + image + "' loading='" + loading + "' " + sizeHtml + ">";
	//console.log(img);
	html += img;
	if (photo.type === "VIDEO") {
		let durationLabel = getVideoDurationLabel(photo);
		html += "<span class='video-icon'>" + PLAY_ICON + "</span>";
		html += "<span class='video-time-label'>" + durationLabel + "</span>"; 
	}
	html += "</a></li>";
	return html;
}

function pageReady() {
	initParameters();
	getPhotos();
}

$(document).ready(pageReady());