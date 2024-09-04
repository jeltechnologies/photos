const DEFAULT_PROGRAM = "last6months";
const DEFAULT_PERCENTATE = 25;
const SECONDS = 1000;
const NEXT_SLIDE = 60 * SECONDS;
const START_AMOUNT_SLIDES = 120;
const MINUTES = 60 * SECONDS;
const HIDE_NAVIGATION = 3 * SECONDS;
const HIDE_MODAL = 15 * SECONDS;
const MAX_HOURS_IN_SAME_PROGRAM_HOURS = 4; 
const REFRESH_PAGE_AFTER = 90; // minutes
const SHOW_VIDEO_CONTROLS = false; // also must change the css
const BACKGROUND_SELECTED = "#FF6A5C";
const BACKGROUND_NOT_SELECTED = "#CCDFCB";
const FOREGROUND_SELECTED = "white";
const FOREGROUND_NOT_SELECTED = "black";

var reloadTimer = 0;
var lastPlayedVideo = "";
var currentVideoElement = undefined;
var timeOutNextSlide;
var hideNavTimeout;
var hideModalTimeout;
var filterOption = {};
var accessToken;
var programOnLoad;
var programPercentageOnLoad;
var photos;
var photosOnLoad;
var likeThisPayload = null;
var inLikeThisSlideIndex;
var map;
var myRenderer;
var swiper;
var selectedProgram;
var programButtons = [];
var isModalOpen = false;


function hidePhoto() {
	showModal("#hide-photo-modal");
}

function hidePhotoOK() {
	let action = {};
	let index = swiper.realIndex;
	action.id = photos[index].checksum;
	action.action = "hide";
	postJson("photo", action, hidePhotoCompleted);
}

function serverLog(message) {
	let photo = photos[swiper.realIndex];
	let id = photo.id;
	let line = {};
	line.id = id;
	line.message = message;
	line.program = programOnLoad;
	line.percentage = programPercentageOnLoad;
	postJson("frame/log", line, logCompleted);
}

function logCompleted() {
}

function hidePhotoCompleted() {
	let index = swiper.realIndex;
	swiper.removeSlide(index);
	hideModal();
}

function hidePhotoCancel() {
	hideModal();
}

function showOptions() {
	hideNavigation();
	let src = "";
	if (photos.length > 0) {
		let index = swiper.realIndex;
		let id = photos[index].id;
		src = "img?id=" + id + "&size=small";
		let coordinates = photos[index].coordinates;
		if (coordinates == undefined || coordinates == null) {
			$("#div-program-same-time-and-place").hide();
			$("#div-program-same-place").hide();
		} else {
			$("#div-program-same-time-and-place").show();
			$("#div-program-same-place").show();
		}
	}
	$("#more-like-this-thumb").attr("src", src);
	showModal("#program-modal");
}


function shareClicked() {
	let photo = photos[swiper.realIndex];
	let quality;
	if (photo.type === "PHOTO") {
		quality = "original";
	} else {
		quality = "medium";
	}
	let postBody = {};
	postBody.id = photo.id;
	postBody.quality = quality;
	postJson("share", postBody, shareImage)
}

async function shareImage(data) {
	console.log(data);
	let imageUrl = data.url;
	let fileName = data.fileName;
	let mimeType = data.mimeType;
	serverLog("Trying to get the blob: " + imageUrl + " with fileName: " + fileName + ", mimeType: " + mimeType);
	const response = await fetch(imageUrl);
	const blob = await response.blob();
	serverLog("Recevied the blob");
	const filesArray = [
		new File([blob], fileName,
			{
				type: mimeType,
				lastModified: new Date().getTime()
			}
		)];
	const shareData = {
		files: filesArray,
	};
	serverLog("Trying navigator share");
	navigator.share(shareData);
}

function showInfo() {
	hideNavigation();
	showModal("#info-modal");
	let startZoom = 11;
	let index = swiper.realIndex;
	let photo = photos[index];
	let coordinates = photo.coordinates;
	if (coordinates !== undefined) {
		let lat = coordinates.latitude;
		let lng = coordinates.longitude;
		$('#map').show();
		startMap(lat, lng, startZoom);
		L.marker([lat, lng]).addTo(map);
	} else {
		$('#map').hide();
		console.log("No coordinates for this photo");
	}
	let date = photo.dateTaken;
	if (date === undefined || date === null) {
		date = photo.dateLastModified;
	}
	$("#photo-info-date").html(formatDateTime(date));
	$("#photo-info-ago").html(getTimeAgo(date));
	$("#photo-info-source").html(photo.source);
	$("#photo-info-location").html(getLocationInfo(photo));
}

function hideModal() {
	console.log("hideModal");
	$.modal.close();
	$('#photo').show();
}

function userClickedSwiper() {
	if (photos.length > 0) {
		showNavigation();
		if (currentVideoElement != undefined) {
			let muted = currentVideoElement.muted;
			let toggle;
			if (muted) {
				toggle = false;
			} else {
				toggle = true;
			}
			currentVideoElement.muted = toggle;
		}
	}
}

function showNavigation() {
	$("#navigation").show();
	$("#view-menu").show();
	scheduleHideNavigation();
	if (SHOW_VIDEO_CONTROLS) {
		let id = "slide-video-" + swiper.realIndex;
		let element = document.getElementById(id);
		if (typeof (element) != 'undefined' && element != null) {
			element.controls = true;
		}
	}
}

function scheduleHideNavigation() {
	if (hideNavTimeout != undefined) {
		clearTimeout(hideNavTimeout);
	}
	hideNavTimeout = setTimeout(hideNavigation, HIDE_NAVIGATION);
}

function scheduleHideModal() {
	if (hideModalTimeout != undefined) {
		clearTimeout(hideModalTimeout);
	}
	hideModalTimeout = setTimeout(hideModal, HIDE_MODAL);
}

function hideNavigation() {
	$("#navigation").hide();
}

function startVideo(id) {
	let element = document.getElementById(id);
	console.log("startVideo " + id);
	if (typeof (element) != 'undefined' && element != null) {
		element.play();
		let vid = element;
		vid.onabort = function() {
			serverLog("Video load aborted");
		};
		vid.onerror = function(e) {
			serverLog("Cannot start video");
		};
		vid.onended = function() {
			console.log("The video has ended");
		};
		lastPlayedVideo = id;
		currentVideoElement = element;
	} else {
		currentVideoElement = null;
	}
}

function pauseVideo(id) {
	let element = document.getElementById(id);
	//console.log(id);
	if (typeof (element) != 'undefined' && element != null) {
		//console.log("video exists");
		element.muted = true;
		element.pause();
		//console.log("pause");
	}
}

function swiperSlideChange() {
	if (photos.length > 0) {
		serverLog("Showing");
		if (timeOutNextSlide != undefined) {
			clearTimeout(timeOutNextSlide);
		}
		scheduleNextSlide();
		if (lastPlayedVideo != "") {
			pauseVideo(lastPlayedVideo);
		}
		let index = swiper.realIndex;
		let photo = photos[index];
		let id = "slide-" + photo.id;
		if (photo.type === "VIDEO") {
			startVideo(id);
		} else {
			showHighQualityPhoto(photo, id);
		}
	}
}

function showHighQualityPhoto(photo, slideId) {
	let div = "#" + slideId;
	let src = "img?size=medium&id=" + photo.id;
	$(div).attr("src", src);
}

function scheduleNextSlide() {
	//console.log("scheduleNextSlide()");
	let index = swiper.realIndex;
	let durationSeconds = photos[index].duration + 1;
	let durationMs = durationSeconds * SECONDS;
	if (durationMs < NEXT_SLIDE) {
		durationMs = NEXT_SLIDE;
	}
	console.log("Scheduling next slide in " + durationMs + " milliseconds");
	timeOutNextSlide = setTimeout(nextSlideIfNotModalShown, durationMs);
}


function nextSlideIfNotModalShown() {
	if (!isModalOpen) {
		nextSlide();
	} else {
		scheduleNextSlide();
	}
}

function nextSlide() {
	console.log("nextSlide");
	if (swiper.isEnd) {
		refreshPage();
	}
	swiper.slideNext();
}

function refreshPage() {
	console.log("gotoNextSlide");
	location.reload();
}

function startMap(startLat, startLng, startZoom) {
	var container = L.DomUtil.get('map'); if (container != null) { container._leaflet_id = null; }
	map = L.map('map').setView([startLat, startLng], startZoom);
	myRenderer = L.canvas({ padding: 0.5 });
	L.tileLayer('https://api.mapbox.com/styles/v1/{id}/tiles/{z}/{x}/{y}?access_token={accessToken}', {
		attribution: 'Map data &copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors, Imagery © <a href="https://www.mapbox.com/">Mapbox</a>',
		maxZoom: 25,
		id: 'mapbox/streets-v11',
		tileSize: 512,
		zoomOffset: -1,
		accessToken: accessToken
	}).addTo(map);
	console.log("startMap completed");
}

function setupSwiper() {
	initPrograms();
	//createTitles();
	swiper.on('slideChange', swiperSlideChange);
	hideNavigation();
	reloadTimer = 0;
	//setInterval(gotoNextSlideTimer, 60 * SECONDS);
	swiperSlideChange();
	console.log("setupSwiper completed");
}

function initPrograms() {
	let program = filterOption.program.name;
	if (program == undefined) {
		program = "ALL";
	}
	$("#radio-" + program).prop("checked", true);
	let programPercentage = filterOption.percentage;
	if (programPercentage == undefined) {
		programPercentage = 100;
	}
	$('#program-percentage-label').html(programPercentage + "%");
	$('#program-percentage').val(programPercentage);
	$(document).on('input', '#program-percentage', function() {
		$('#program-percentage-label').html($(this).val() + "%");
	});
}

function createTitles() {
	for (let i = 0; i < photos.length; i++) {
		let divName = "title-" + i;
		let div = document.getElementById(divName);
		if (div !== undefined && div !== null) {
			let photo = photos[i];
			div.innerHTML = getTitle(photo);
		}
	}
}

function getPhotos() {
	showPleaseWait("Loading...");
	let urlParams = new URLSearchParams(window.location.search);
	let program = urlParams.get('program');
	if (program == undefined || program === "") {
		program = DEFAULT_PROGRAM;
	}
	let amount = urlParams.get('amount');
	if (amount == undefined || amount === "") {
		amount = START_AMOUNT_SLIDES;
	}
	let percentage = urlParams.get('program-percentage');
	if (percentage == undefined || percentage === "") {
		percentage = DEFAULT_PERCENTATE;
	}
	let photoInSlideShow = urlParams.get("photo-in-slideshow");
	filterOption = {};
	let programObject = {};
	programObject.name = program;
	filterOption.program = programObject;
	filterOption.amount = amount;
	filterOption.percentage = percentage;
	filterOption.photoInSlideshow = photoInSlideShow;
	updateSelectedPrograms(program);
	let url = "frame/random?program=" + program + "&amount=" + amount + "&program-percentage=" + percentage;
	if (photoInSlideShow != undefined && photoInSlideShow != null) {
		url = url + "&photo-in-slideshow=" + photoInSlideShow;
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
	let program = data.program;
	if (program === "same-time" || program === "same-time-and-place" || program === "same-place") {
		sinceHours = data.sinceHours;
		console.log("In same program since " + sinceHours + " hours");
		if (sinceHours > MAX_HOURS_IN_SAME_PROGRAM_HOURS) {
			refreshWithDefaultProgram();
		}
	}
	if (swiper == undefined) {
		createSwiperObject();
	}
	var urlParams = new URLSearchParams(window.location.search);
	let selectedPhoto = urlParams.get("photo");
	let selectedIndex = 0;
	photos = enforceArrayFromElements(data.payload);
	photosOnLoad = photos;
	accessToken = data.mapkey;
	console.log(data);
	programOnLoad = data.description;
	programPercentageOnLoad = data.percentage;
	let menuTitle = htmlEncode(data.percentage + "% of " + data.description.toLowerCase());
	likeThisPayload = null;
	$('#view-menu-title').html(menuTitle);
	for (let i = 0; i < photos.length; i++) {
		let photo = photos[i];
		if (photo.id === selectedPhoto) {
			selectedIndex = i;
		}
	}
	updateSlidesAndMoveTo(selectedIndex);
	receivePhotosCompleted();
	$('#view-menu-title').html(programOnLoad);
	$('#no-photos-program').html(programOnLoad);
}

function getNoPhotosFoundSlide() {
	let html = "";
	html += "<div class='boohoo' onclick='showOptions();'>";
	html += "  <div class='no-photos-info'>";
	html += "    <div class='view-menu-icon'>";
	html += "      <svg xmlns='http://www.w3.org/2000/svg' width='128' height='128' fill='currentColor' viewBox='0 0 16 16'><path d='M8 15A7 7 0 1 1 8 1a7 7 0 0 1 0 14m0 1A8 8 0 1 0 8 0a8 8 0 0 0 0 16'/><path d='M6.831 11.43A3.1 3.1 0 0 1 8 11.196c.916 0 1.607.408 2.25.826.212.138.424-.069.282-.277-.564-.83-1.558-2.049-2.532-2.049-.53 0-1.066.361-1.536.824q.126.27.232.535.069.174.135.373ZM6 11.333C6 12.253 5.328 13 4.5 13S3 12.254 3 11.333c0-.706.882-2.29 1.294-2.99a.238.238 0 0 1 .412 0c.412.7 1.294 2.284 1.294 2.99M7 6.5C7 7.328 6.552 8 6 8s-1-.672-1-1.5S5.448 5 6 5s1 .672 1 1.5m4 0c0 .828-.448 1.5-1 1.5s-1-.672-1-1.5S9.448 5 10 5s1 .672 1 1.5m-1.5-3A.5.5 0 0 1 10 3c1.162 0 2.35.584 2.947 1.776a.5.5 0 1 1-.894.448C11.649 4.416 10.838 4 10 4a.5.5 0 0 1-.5-.5M7 3.5a.5.5 0 0 0-.5-.5c-1.162 0-2.35.584-2.947 1.776a.5.5 0 1 0 .894.448C4.851 4.416 5.662 4 6.5 4a.5.5 0 0 0 .5-.5'/></svg>";
	html += "    </div>";
	html += "    <h2>Nothing to see here</h2>";
	html += "    <p>No photos and videos found for <span id='no-photos-program'></span></p>";
	html += "  </div>";
	html += "</div>";
	return html;
}

function updateSlidesAndMoveTo(index) {
	let slides = [];
	if (photos.length == 0) {
		slides.push(getNoPhotosFoundSlide());
		setTimeout(goBack, HIDE_MODAL);
	} else {
		for (let i = 0; i < photos.length; i++) {
			let loading;
			if (i > 1) {
				loading = "lazy";
			} else {
				loading = "eager";
			}
			let slide = addSlide(photos[i], loading);
			slides.push(slide);
		}
	}
	swiper.removeAllSlides();
	swiper.appendSlide(slides);
	if (photos.length >= index) {
		swiper.slideTo(index, 0);
	}
}

function goBack() {
	history.back();
}

function refreshWithDefaultProgram() {
	window.location.href = window.location.pathname;
}

function addSlide(photo, loading) {
	let html;
	let id = photo.id;
	html = "<div class='swiper-slide'><div class='swiper-zoom-container'>";
	if (photo.type === "PHOTO") {
		html += "<img id='slide-" + id + "' src='img?id=" + id + "&size=small' loading='" + loading + "'>";
	} else {
		let preload = " preload='none'";
		let poster = "img?id=" + id + "&size=small";
		html += "<video id='slide-" + id + "' " + preload + " muted loop poster='" + poster + "'>";
		html += "<source src='video?id=" + id + "&quality=high' type='video/mp4'>";
		html += "</video>";
	}
	let title = htmlEncode(getTitle(photo));
	let titleSpan = "<span class='title'><span class='title-text' id='title-" + id + "'>" + title + "</span></span>";
	html += titleSpan;
	html += "</div>";
	if (photo.type === "PHOTO") {
		html += "<div class='swiper-lazy-preloader swiper-lazy-preloader-white'></div></div>";
	}
	//console.log(html);
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

function showModal(div){
	isModalOpen = true;	
	$(div).modal();
	$(div).on($.modal.AFTER_CLOSE, userClosedModal);
	scheduleHideModal();
}

function userClosedModal() {
	console.log("userCloseModal");
	isModalOpen = false;
}

function getProgramGroups() {
	getJson("frame/programs", getProgramGroupsCompleted);
}

function getProgramGroupsCompleted(data) {
	let programGroups = data;
	initFrameProgramGroups(programGroups);
	getPhotos();
}

function initFrameProgramGroups(groups) {
	programButtons = [];
	let html = "";
	for (let i = 0; i < groups.length; i++) {
		let group = groups[i];
		html += groupToHtml(group);
	}
	//console.log(html);
	let existingHtml = $('#frame-program-groups').html();
	let newHtml = html + existingHtml;
	$('#frame-program-groups').html(newHtml);
}

function groupToHtml(group) {
	let html = "";
	let title = group.title;
	if (title.name === "more-like") {
		html += '<div class="option-group-more-like-this">';
		html += '<div class="option-group-title-more-like-this">' + htmlEncode(title.description) + "</div>";
		html += '<div class="option-more-like-this-thumb"><img id="more-like-this-thumb"> ';
		html += "</div>";
	} else {
		html += '<div class="option-group">';
		html += "<div class='option-group-title'>" + htmlEncode(title.description) + "</div>";
	}
	let programs = group.programs;
	for (let i = 0; i < programs.length; i++) {
		let program = programs[i];
		html += programToHtml(program);
	}
	html += "</div>";
	return html;
}

function programToHtml(program) {
	let id = "div-program-" + program.name;
	let clickFunction = "updateSelectedPrograms('" + program.name + "');";
	let html = '<div id="' + id + '"' + ' class="option-program-button" onClick="' + clickFunction + '">';
	html += htmlEncode(program.description);
	html += "</div>";
	let programButton = {};
	programButton.buttonId = id;
	programButton.program = program;
	programButtons.push(programButton);
	return html;
}

function updateSelectedPrograms(program) {
	$('#program').val(program);
	selectedProgram = program;
	for (let i = 0; i < programButtons.length; i++) {
		let button = programButtons[i];
		let background;
		let foreground;
		if (button.program.name === selectedProgram) {
			background = BACKGROUND_SELECTED;
			foreground = FOREGROUND_SELECTED;
		} else {
			background = BACKGROUND_NOT_SELECTED;
			foreground = FOREGROUND_NOT_SELECTED;
		}
		let id = '#' + button.buttonId;
		$(id).css("background-color", background);
		$(id).css("color", foreground);
	}
}

function refreshWithNewProgram() {
	let index = swiper.realIndex;
	let id = photos[index].id;
	$('#photo-in-slideshow').val(id);
	document.getElementById("program-form").submit();
}

function receivePhotosCompleted() {
	setupSwiper();
	hidePleaseWait();
	addKeyListeners();
}

function documentReady() {
	getProgramGroups();
}

$(document).ready(documentReady());