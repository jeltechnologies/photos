const ADD_NEW_PHOTO_HEIGHT = 400;

var addedIds = new Array();

function addClicked() {
	const itemIds = new Array();
	$('.selected-photo').each(function(i, obj) {
		let id = findAfterFirst(obj.id, "item-");
		itemIds.push(id);
	});

	if (itemIds.length > 0) {
		addedIds = itemIds;
		showPleaseWait("Adding");
		postJson("add-photos", itemIds, addSuccess, addFailed);
	}
}


function addSuccess() {
	let info = "<p>Successfully added " + addedIds.length;
	if (addedIds.length == 1) {
		info += " photo or video";
	} else {
		info += " photos and videos";
	}
	info += "...</p>";
	$('#result-info').html(info);
	hidePleaseWait();
	$('#result-modal').modal();
	$('#result-modal').on($.modal.BEFORE_CLOSE, resultModalClosed);
}

function resultModalClosed() {
	console.log("resultModalClosed");
	goto('timeline.jsp');
}

function addFailed() {
	hidePleaseWait();
	alert("Adding pictures failed");
}


function userClickedGetLatest() {
	$('#status-title').html("Please wait");
	$('#status-text').html("Getting latest photos from Synology ....");
	getJson("get-latest", gotLatest);
	$('#status-modal').modal();
}

function updateStatus() {
	getJson("get-latest?status=true", gotLatest);
}

function gotLatest(data) {
	let photosQueue = data.thumbsQueue;
	let videosQueue = data.videosQueue;

	let status;
	let complete;
	if (photosQueue === 0 && videosQueue === 0) {
		status = "Done";
		complete = true;
	} else {
		status = "Creating thumbnails...";
		complete = false;
	}

	let html = "";
	html = html + "<table class='status-info'>";
	html = html + row("Photos left", photosQueue);
	html = html + row("Videos left", videosQueue);
	html = html + "</table>";

	$('#status-title').html(status);
	$('#status-text').html(html);

	if (complete === true) {
		$.modal.close();
	} else {
		setTimeout(updateStatus, 1000);
	}

	console.log(data);
}


function getPhotos(month, year) {
	let url = "add-photos?month=" + month + "&year=" + year;
	getJson(url, receivePhotos);
}

function receivePhotos(data) {
	let html = "";
	html += '<div id="add-new-period" class="add-new-period album">';
	let month = data.month;
	let year = data.year;
	let photos = enforceArrayFromElements(data.photos);
	let titleId = "title-" + month + "-" + year;
	if (photos != undefined) {
		let title;
		if (photos.length == 0) {
			title = "Nothing found";
		} else {
			title = htmlEncode(capitalizeFirstLetter(LOCALE_MONTHS[(month - 1)] + " " + year));
		}
		html += "<h2 id='" + titleId + "'>" + title + "</h2>";
		html += "<ul>";
		for (let i = 0; i < photos.length; i++) {
			html += renderPhoto(photos[i], i);
		}
		html += "<li></li></ul>";
	} else {
		html += "<h2 id='" + titleId + "'>Nothing found</h2>";
	}

	html += "</div>";
	let buttonId = "next-" + month + "-" + year;
	html += "<div id='" + buttonId + "' class='add-new-buttons'>";
	let onclickNext = "onclick=\"getPreviousMonthClicked(" + month + ", year=" + year + ", id='" + buttonId + "');return false;\"";
	html += "<button " + onclickNext + ">Get more</button>";
	html += "<button onclick='addClicked()'>Add</button>";
	html += " </div>";

	$('#add-new-periods').append(html);

	const today = new Date();
	thisMonth = today.getMonth() + 1;
	thisYear = today.getFullYear();

	if (thisMonth != month || thisYear != year) {
		scrollToBottom(titleId);
	}
}

function scrollToBottom(id) {
	const element = document.getElementById(id);
	element.scrollIntoView({ behavior: "smooth" });
}

function getPreviousMonthClicked(oldMonth, oldYear, div) {
	$('#' + div).hide();
	let newMonth = oldMonth - 1;
	let newYear = oldYear;
	if (newMonth === 0) {
		newMonth = 12;
		newYear = newYear - 1;
	}
	getPhotos(newMonth, newYear);
}

function renderPhoto(photo, photoIndex) {
	let html = "";
	let id = photo.id;
	let onclick = " onclick=\"$(this).toggleClass('selected-photo');$(this).toggleClass('unselected-photo');return false;\"";
	let itemId = "item-" + id;
	html += "<li id='" + itemId + "' class='unselected-photo album'" + onclick + ">";
	//html += "<li>";
	let sizeHtml = getDimensionHtml(photo, ADD_NEW_PHOTO_HEIGHT);

	let label = getLargeInfoLabel(photo);
	if (photo.type === "PHOTO") {
		let image = "img?id=" + id + "&size=original";
		let loading;
		if (photoIndex < 10) {
			loading = "eager";
		} else {
			loading = "lazy";
		}
		let img = "<img class='add-new-image'" + " src='" + image + "' loading='" + loading + "' " + sizeHtml + ">";
		html += img;

	} else {
		let poster = " poster='img?id=" + id + "&size=medium'";
		let video = '<video ' + sizeHtml + ' controls preload="metadata"' + poster + ' >';
		let source = '<source src="video?id=' + id + '&quality=high" type="video/mp4">';
		video = video + source + "</video>";
		html += video;
	}
	html += '<div class="add-new-title-container"><span class="photo-title">' + label + '</span></div>';
	html += "</li>";
	console.log(html);
	return html;
}

function documentReady() {	
	const today = new Date();
	month = today.getMonth() + 1;
	year = today.getFullYear();
	getPhotos(month, year);
}

$(document).ready(documentReady());