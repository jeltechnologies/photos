var photos;
var map;
var marker;
var swiper;

function userClickedSwiper() {
	toggleNavigation();
}

function handleCloseAction() {
	window.history.back();
}

var hideTimeout;

function toggleNavigation() {
	$("#navigation").show();
	$("#swiper-right-top-menu").show();
	$("#swiper-left-top-menu").show();
	scheduleHideNavigation();
}

function cancelTimeout() {
	if (hideTimeout != undefined) {
		clearTimeout(hideTimeout);
	}
}

function scheduleHideNavigation() {
	cancelTimeout();
	hideTimeout = setTimeout(hideNavigation, 10000);
}

function hideNavigation() {
	$("#navigation").hide();
	$("#swiper-right-top-menu").hide();
	$("#swiper-left-top-menu").hide();
}

function removeFromAlbumClicked() {
	let action = {};
	let index = swiper.realIndex;
	action.id = photos[index].id;
	action.action = "remove-from-album";
	postJson("photo", action);
}

function rotateClicked(direction) {
	let action = {};
	let index = swiper.realIndex;
	action.action = direction;
	action.relativeFileName = photos[index].relativeFileName;
	postJson("photo", action);
}

function setAsAlbumCoverClicked() {
	let index = swiper.realIndex;
	let photo = photos[index];
	let openUrl = "setalbumcover.jsp?photo=" + photo.id + "&album=" + encodeURIComponent(photo.relativeFolderName);
	openModalWebPage($('#set-album-cover-modal'), openUrl);
}

function userDecidedCoverForAlbum() {
	let albumUpdate = {};
	albumUpdate.coverPhotoId = $('#photo').val();
	let album = $('input[name="selected-album"]:checked').val()
	albumUpdate.relativeFileName = album;
	postJson("album", albumUpdate, coverForAlbumCompleted);
}

function coverForAlbumCompleted() {
	closeModal();
}

function pauseAllVideos() {
	$('.gallery-video').each(function() {
		let media = $(this).get(0);
		media.pause();
	});
}

function swiperSlideChange() {
	pauseAllVideos();
}

function downloadFromAlbumClicked() {
	let index = swiper.realIndex;
	let photo = photos[index];
	if (photo.type === "PHOTO") {
		extension = ".jpg";
	} else {
		if (photo.type === "VIDEO") {
			extension = ".mp4";
		} else {
			alert("Unsupported file for sharing");
		}
	}
	let id = photos[index].id;
	let location = "download/" + id + "?size=original";
	window.location = location;
}

function shareClicked(quality) {
	let photo = photos[swiper.realIndex];
	let fileName = photo.id;
	let size;

	if (quality === "normal") {
		size = "medium";
	} else {
		size = "original";
	}
	fileName = fileName + "-size-" + size

	if (photo.type === "PHOTO") {
		fileName = fileName + ".jpg";
		type = "image/jpeg";
	} else {
		fileName = fileName + ".mp4";
		type = "video/mp4";
	}

	let fileToDownload = "download/" + fileName;
	//alert(fileToDownload);
	shareImage(fileToDownload, type);
}

async function shareImage(imageUrl, type) {
	console.log("shareImage " + imageUrl);
	let index = imageUrl.lastIndexOf("/");
	let fileName = imageUrl.substring(index + 1);
	const response = await fetch(imageUrl);
	const blob = await response.blob();
	const filesArray = [
		new File([blob], fileName,
			{
				type: type,
				lastModified: new Date().getTime()
			}
		)];
	const shareData = {
		files: filesArray,
	};

	let canShare = navigator.share && navigator.canShare(shareData);
	console.log("Trying to share and canShare is " + canShare);
	console.log(shareData);
	try {
		navigator.share(shareData);
	}
	catch (err) {
		alert(err.message);
	}
}

function updateClipboard(newClip) {
	navigator.clipboard.writeText(newClip).then(function() {
		console.log("Succesfully updated clipboard");
	}, function() {
		console.log("Clipboard write faile");
	});
}

function showOrHideClicked() {
	let index = swiper.realIndex;
	let id = photos[index].id;
	let openUrl = "showorhidephoto.jsp?id=" + id;
	openModalWebPage($('#show-or-hide-photo-modal'), openUrl);
}

function userDecidedShowOrHide() {
	let action = {};
	let index = swiper.realIndex;
	action.id = photos[index].id;
	if ($("#hide-photo").is(":checked")) {
		action.action = "hide";
	} else {
		action.action = "show";
	}
	postJson("photo", action, showOrHideCompleted);
}

function showOrHideCompleted() {
	closeModal();
}

function shareClicked() {
	let photo = photos[swiper.realIndex];
	let fileName = photo.id;
	let size;
	if (photo.type === "PHOTO") {
		size = "original";
	} else {
		size = "medium";
	}
	fileName = fileName + "-size-" + size
	if (photo.type === "PHOTO") {
		fileName = fileName + ".jpg";
		type = "image/jpeg";
	} else {
		fileName = fileName + ".mp4";
		type = "video/mp4";
	}
	let fileToDownload = "download/" + fileName;
	console.log(fileToDownload);
	shareImage(fileToDownload, type);
}

async function shareImage(imageUrl, type) {
	// console.log("shareImage " + imageUrl);
	let index = imageUrl.lastIndexOf("/");
	let fileName = imageUrl.substring(index + 1);
	// console.log("Trying to get the blob: " + fileName);
	const response = await fetch(imageUrl);
	const blob = await response.blob();
	//alert("Recevied the blob");
	const filesArray = [
		new File([blob], fileName,
			{
				type: type,
				lastModified: new Date().getTime()
			}
		)];
	const shareData = {
		files: filesArray,
	};
	//let canShare = navigator.share && navigator.canShare(shareData);
	//alert("Trying to share and canShare is " + canShare);
	console.log(shareData);
	navigator.share(shareData);
}

function showInfoClicked() {
	cancelTimeout();
	let index = swiper.realIndex;
	let photo = photos[index];

	let coordinates = photo.coordinates;
	if (coordinates != undefined && coordinates !== null) {
		let latitude = coordinates.latitude;
		let longitude = coordinates.longitude;
		if (latitude !== null && latitude !== undefined && longitude !== null && longitude !== undefined) {
			startMap(latitude, longitude, 9);
			$('#map').show();
		} else {
			$('#map').hide();
		}
	}

	const startTable = "<table class='photo-info' width='100%'><tr>";
	const endTable = "</tr></table>";

	let infoHtml = startTable;
	let date = photo.dateTaken;
	if (date === null || date === undefined) {
		date = photo.dateLastModified;
	}
	infoHtml += cell(getDateLabel(date));
	infoHtml += cell(photo.source);
	infoHtml += cell(photo.fileName);
	infoHtml += cell("Album: " + photo.relativeFolderName);
	infoHtml += endTable;

	let title = startTable + cell(getLocationInfo(photo)) + endTable;

	$('#photo-title').html(title);
	$('#photo-meta').html(infoHtml);
	$('#show-information-modal').modal();
}

function startMap(startLat, startLng, startZoom) {
	//console.log("startMap(" + startLat + ", " + startLng + ", " + startZoom + ")");
	if (map === undefined) {
		map = L.map('map').setView([startLat, startLng], startZoom);
		L.canvas({ padding: 0.5 });
		L.tileLayer('https://api.mapbox.com/styles/v1/{id}/tiles/{z}/{x}/{y}?access_token={accessToken}', {
			attribution: 'Map data &copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors, Imagery © <a href="https://www.mapbox.com/">Mapbox</a>',
			maxZoom: 25,
			id: 'mapbox/streets-v11',
			tileSize: 512,
			zoomOffset: -1,
			accessToken: mapAccessToken
		}).addTo(map);
	}
	else {
		//console.log("defined");
		map.setView([startLat, startLng], startZoom);
	}
	if (marker === undefined) {
		marker = L.marker([startLat, startLng]).addTo(map);
	}
	else {
		marker.setLatLng([startLat, startLng]);
	}
	setTimeout(function() {
		map.invalidateSize();
	}, 10);
}

function mapChanged(e) {
	//setTimeout(postLocation, 500);
}

function getPhotos() {
	let urlParams = new URLSearchParams(window.location.search);
	let photo = urlParams.get('photo');
	let album = urlParams.get('album');
	let sort = urlParams.get('sort');
	let from = urlParams.get('from');
	let to = urlParams.get('to');
	let mediatype = urlParams.get('mediatype');
	let params = [];

	if (photo != null) {
		let part = "photo=" + encodeURIComponent(photo);
		params.push(part);
	}
	if (album != null) {
		let part = "album=" + encodeURIComponent(album);
		params.push(part);
	}
	if (sort != null) {
		params.push("sort=" + sort);
	}
	if (from != null) {
		params.push("from=" + from);
	}
	if (to != null) {
		params.push("to=" + to);
	}
	if (mediatype != null) {
		params.push("mediatype=" + mediatype);
	}
	let url = "gallery";

	for (let i = 0; i < params.length; i++) {
		if (i === 0) {
			url = url + "?";
		}
		else {
			url = url + "&";
		}
		url = url + params[i];
	}
	getJson(url, receivePhotos);
}

function createSwiperObject() {
	swiper = new Swiper(".mySwiper", {
		cssMode: true,
		navigation: {
			nextEl: ".swiper-button-next",
			prevEl: ".swiper-button-prev",
			hideOnClick: "true",
		},
		slidesPerView: 1,
		zoom: {
			toggle: true,
			maxRatio: 5,
		}
	});
}

function receivePhotos(data) {
	if (swiper == undefined) {
		createSwiperObject();
	}
	console.log("recievePhotos start");
	var urlParams = new URLSearchParams(window.location.search);
	let selectedPhoto = urlParams.get("photo");
	let selectedIndex = 0;
	photos = enforceArrayFromElements(data);
	if (photos != undefined) {
		for (let i = 0; i < photos.length; i++) {
			let photo = photos[i];
			if (photo.id === selectedPhoto) {
				selectedIndex = i;
			}
		}
		let slides = [];
		for (let i = 0; i < photos.length; i++) {
			let slide = addSlide(i);
			slides.push(slide);
		}
		swiper.appendSlide(slides);
	}
	swiper.slideTo(selectedIndex, 0);
	//console.log(selectedIndex);
	receivePhotosCompleted();
}

function addSlide(i) {
	const LOW_QUALITY = {
		video: "low",
		videoPoster: "small",
		image: "medium"
	};

	const HIGH_QUALITY = {
		video: "high",
		videoPoster: "medium",
		image: "medium"
	};

	let quality = HIGH_QUALITY;
	let html;
	let photo = photos[i];
	let id = photo.id;
	html = "<div class='swiper-slide'><div class='swiper-zoom-container'>";
	if (photo.type === "PHOTO") {
		let loading;
		if (i > 3) {
			loading = "lazy";
		} else {
			loading = "eager";
		}
		html += "<img id='slide-photo-" + i + "' src='img?id=" + id + "&size=" + quality.image + "' loading='" + loading + "'>";
	} else {
		let poster = "img?id=" + id + "&size=" + quality.videoPoster;
		let preload = "";
		if (i > 0) {
			preload = " preload='none'";
		}
		html += "<video id='slide-video-" + i + "' class='gallery-video' controls" + preload + " poster='" + poster + "'>";
		html += "<source src='video?id=" + id + "&quality=" + quality.video + "' type='video/mp4'>";
		html += "</video>";
	}
	let title = getTitle(photo);
	let titleSpan = "<span class='title'><span class='title-text' id='title-" + i + "'>" + title + "</span></span>";
	html += titleSpan;
	html += "</div>";
	if (photo.type === "PHOTO") {
		html += "<div class='swiper-lazy-preloader swiper-lazy-preloader-white'></div></div>";
	}
	console.log(html);
	return html;
}

function addKeyListeners() {
	window.addEventListener('keydown', function(event) {
		//console.log("keyDown: " + event.keyCode);
		switch (event.keyCode) {
			case 189: {
				// - minus
				handleCloseAction();
				break;
			}
			case 173: {
				// minus op desktop keyboard
				handleCloseAction();
				break;
			}
			case 27: {
				// esc
				handleCloseAction();
				break;
			}
			case 37: {
				// left key
				swiper.slidePrev();
				break;
			}
			case 39: {
				// right key
				swiper.slideNext();
				break;
			}
		}
	});
}

function receivePhotosCompleted() {
	setupSwiper();
}

function setupSwiper() {
	swiper.on('slideChange', swiperSlideChange);
	addKeyListeners();
	scheduleHideNavigation();
}

function documentReady() {
	getPhotos();
}

document.onreadystatechange = function() {
	console.log(document.readyState);
	if (document.readyState !== "complete") {
		document.querySelector("body").style.visibility = "hidden";
		document.querySelector("#loader").style.visibility = "visible";
	} else {
		document.querySelector("#loader").style.display = "none";
		document.querySelector("body").style.visibility = "visible";
	}
};

$(document).ready(documentReady());