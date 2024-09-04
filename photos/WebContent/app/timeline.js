var height = 300;
var width = 400;
var pagingChanging = false;
var initialPageLoad = true;
var payload;
var timeline = {};

function getPagingSettings() {
	timeline.grouping = $("#grouping").val();
	timeline.sorting = $("#sorting").val();
	timeline.mediaType = $("#mediatype").val();
	timeline.items = parseInt($("#paging-items-per-page").val());
	timeline.randomCover = $("#random").val() === 'random';
}

function addNewClicked() {
	location.replace("add-new.jsp?album=/Uncategorized");
}

function sortingChanged() {
	updatePage();
}

function groupingChanged() {
	getNewItemsFromServer();
	postPreferences();
}

function randomChanged() {
	console.log("randomChanged");
	getNewItemsFromServer();
	postPreferences();
}

function mediaTypeChanged() {
	getNewItemsFromServer();
	postPreferences();
}

function itemsChanged() {
	console.log("itemsChanged");
	if (!pagingChanging) {
		getPagingSettings();
		pagingChanging = true;
		updatePage();
		postPreferences();
		pagingChanging = false;
	}
}

function pagingPrevious() {
	if (timeline.currentPage > 1) {
		timeline.currentPage = timeline.currentPage - 1;
		updatePage();
	}
}

function pagingNext() {
	console.log("pagingNext on " + timeline.currentPage);
	if (timeline.currentPage < timeline.payload.length) {
		timeline.currentPage = timeline.currentPage + 1;
		updatePage();
	}
}

function pagingFirst() {
	if (timeline.currentPage !== 1) {
		timeline.currentPage = 1;
		updatePage();
	}
}

function pagingLast() {
	if (timeline.currentPage !== timeline.payload.length) {
		timeline.currentPage = timeline.totalPages;
		updatePage();
	}
}

function pagingSelected() {
	let selectedPage = $('#selectedPage').val();
	timeline.currentPage = selectedPage;
	updatePage();
}

function getNewItemsFromServer() {
	getPagingSettings();
	timeline.currentPage = 1;
	let url = 'timeline-page?';
	url = url + "&randomize=" + timeline.randomCover;
	url = url + "&mediatype=" + timeline.mediaType;
	url = url + "&grouping=" + timeline.grouping;
	//console.log(url);
	showPleaseWait("Loading ...");
	getJson(url, updateImages);
}

function updatePage() {
	let html = "";
	if (timeline.payload.length > 0) {
		getPagingSettings();
		html += "<ul>";
		let page = timeline.currentPage;
		//console.log("page: " + page);
		let start;
		let end;
		if (timeline.sorting === 'NEWESTFIRST') {
			start = timeline.items * (page - 1);
			end = start + timeline.items;
			//console.log(start + " - " + end);
			for (let i = start; i < end; i++) {
				html += generateAlbum(i);
			}
		} else {
			start = timeline.payload.length - (timeline.items * (page - 1)) - 1;
			end = start - timeline.items;
			for (let i = start; i > end; i--) {
				html += generateAlbum(i);
			}
		}
		html += "<li></li></ul>";
	}
	$('#album').html(html);
	updatePaging();
	hidePleaseWait();
	initialPageLoad = false;
}

function generateAlbum(i) {
	let html = "";
	if (i >= 0 && i < timeline.payload.length) {
		let item = timeline.payload[i];
		let cover = item.cover;
		let from = item.from;
		let to = item.to;
		let title = getTimelineTitle(from, to, timeline.grouping);
		html += "<li><div>";
		let link = "timelineperiod.jsp";
		link += "?from=" + item.from + "&to=" + item.to;
		link += "&photo=" + cover.id;
		link += "&mediatype=" + timeline.mediaType;
		link += "&title=" + encodeURIComponent(title);
		let loading;
		if (i > 10) {
			loading ="lazy";
		} else {
			loading = "eager";
		}
		let dimensionHtml = getThumbDimensionHtml(cover);
		let img = "<img class='image-in-album' src='img?id=" + cover.id + "&size=small' loading='" + loading + "' " + dimensionHtml + "' onclick=\"goto('" + link + "');\">";
		html += img;
		if (title !== "") {
			let titleSpan = "<div class='photo-title-container'><span class='photo-title'>" + title + "</span></div>"
			html += titleSpan;
		}
		html += "</div></li>";
	}
	return html;
}

function saveStateBeforeGoto(url) {
	getPagingSettings();
	setSessionStoreTimeline(timeline);
	console.log(timeline);
	console.log("now going to " + url);
}

function updatePaging() {
	timeline.totalPages = Math.ceil(timeline.payload.length / timeline.items);
	$('.totalPages').html(" of " + timeline.totalPages);
	let select = "<select id='selectedPage' class='selectedPage' onchange='pagingSelected()'>";
	for (let i = 1; i <= timeline.totalPages; i++) {
		select = select + "<option value='" + i + "'";
		if (i == timeline.currentPage) {
			select = select + " selected";
		}
		select = select + ">" + i + "</option>";
	}
	select = select + "</select>";
	$('.currentPage').html(select);
}

function updateImages(data) {
	timeline.payload = data.payload;
	updatePage();
}

function postPreferences() {
	//setSessionStoreTimeline(timeline);
	getPagingSettings();
	let preferences = {};
	let timelineCopy = {
		grouping: timeline.grouping,
		sorting: timeline.sorting,
		randomCover: timeline.randomCover,
		items: timeline.items,
		mediaType: timeline.mediaType
	}
	preferences.timeline = timelineCopy;
	console.log(preferences);
	postJson("preferences", preferences, postPreferencesCompleted);
}

function postPreferencesCompleted() {
	console.log("Post preferences completed");
}

function setPageControls() {
	$("#sorting select").val(timeline.sorting);
	$("#grouping").val(timeline.grouping);
	if (timeline.randomCover) {
		$("#random").val("random");
	} else {
		$("#random").val("lasttaken");
	}
	$("#mediatype").val(timeline.mediaType);
	$("#paging-items-per-page").val(timeline.items);
}

function getLoadPreferencesCompleted(data) {
	console.log("getLoadPreferencesCompleted");
	console.log(data);
	timeline = data.timeline;
	timeline.currentPage = 1;
	setPageControls();
	getNewItemsFromServer();
}

function init(referalUrl) {
	timeline = getSessionStorageTimeline();
	if (timeline == undefined || undefined === null) {
		console.log("Page was refreshed");
		getJson("preferences", getLoadPreferencesCompleted);
	} else {
		console.log("Reusing timeline");
		console.log(timeline);
		setPageControls();
		updatePage();
	}
}

function documentReady() {
	let previousURL = document.referrer;
	console.log(previousURL);
	let n = previousURL.lastIndexOf('/');
	let result = previousURL.substring(n + 1);
	console.log(result);
	init(result);
}
