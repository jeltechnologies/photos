const USER_LOCALE = navigator.languages && navigator.languages.length ? navigator.languages[0] : navigator.language;
const LOCALE_MONTHS = luxon.Info.months("long", { locale: USER_LOCALE });
const THUMB_HEIGHT = 300;

function getTimelineTitle(from, to, grouping) {
	let luxonFrom = luxon.DateTime.fromISO(from);
	luxonFrom.setLocale(USER_LOCALE);
	let luxonTo = luxon.DateTime.fromISO(to);
	luxonTo.setLocale(USER_LOCALE);
	let title;
	let year = luxonFrom.year;
	if (grouping === 'WEEK') {
		let translationForWeek = getTranslationForWeek();
		if (translationForWeek != undefined && translationForWeek != "") {
			title = translationForWeek + luxonFrom.toFormat(' n, y');
		}
	} else {
		if (grouping === 'MONTH') {
			let month = LOCALE_MONTHS[luxonFrom.month - 1];
			title = capitalizeFirstLetter(month + " " + year);
		} else {
			if (grouping === 'YEAR') {
				title = year;
			}
		}
	}
	if (title == undefined || title === "") {
		let format = luxon.DateTime.DATE_SHORT;
		title = luxonFrom.toLocaleString(format) + " - " + luxonTo.toLocaleString(format);
	}
	return title;
}

function getTranslationForWeek() {
	let translation;
	//console.log(USER_LOCALE);
	let beforeMinus = USER_LOCALE.split("-")[0];
	if (beforeMinus != undefined && beforeMinus != null) {
		language = beforeMinus.toLowerCase();
		switch (language) {
			case 'en':
				translation = "Week";
				break;
			case 'nl':
				translation = "Week";
				break;
			case 'sv':
				translation = "Vecka";
				break;
			case 'de':
				translation = "Woche";
				break;
			case 'fry':
				translation = "Wike";
				break;
			case 'zh': {
				translation = "星期";
				break;
			}
		}
	}
	return translation;
}

function getJson(url, callbackFunction) {
	$.ajax({
		url: url,
		type: 'GET',
		contentType: "application/json",
		error: function(xhr) {
			console.log("An error occured: " + xhr.status + " " + xhr.statusText);
		},
		success: function(result) {
			callbackFunction(result);
		}
	});
}

function postJson(url, dataToSend, callbackFunction) {
	$.ajax({
		url: url,
		type: 'POST',
		data: JSON.stringify(dataToSend),
		contentType: "application/json",
		error: function(xhr) {
			console.log("An error occured: " + xhr.status + " " + xhr.statusText);
		},
		success: callbackFunction
	});
}

function putJson(url, dataToSend, callbackFunction) {
	$.ajax({
		url: url,
		type: 'PUT',
		data: JSON.stringify(dataToSend),
		contentType: "application/json",
		error: function(xhr) {
			console.log("An error occured: " + xhr.status + " " + xhr.statusText);
		},
		success: callbackFunction
	});
}

function deleteJson(url, callbackFunction) {
	$.ajax({
		url: url,
		type: 'DELETE',
		contentType: "application/json",
		error: function(xhr) {
			console.log("An error occured: " + xhr.status + " " + xhr.statusText);
		},
		complete: callbackFunction
	});
}

function openModalWebPage(modal, webPageUrl) {
	// @see https://github.com/kylefox/jquery-modal
	$.ajax({
		url: webPageUrl,
		success: function(newHTML, textStatus, jqXHR) {
			modal.body = "";
			$(newHTML).appendTo('body').modal();
			$.modal.getCurrent().options.showClose = false;
		},
		error: function(jqXHR, textStatus, errorThrown) {
			console.log("AJAX error, cannot openModalWebPage");
		}
	});
	modal.modal();
}

function closeModal() {
	$.modal.close();
}

function showPleaseWait() {
	$('body').loadingModal({
		animation: 'cubeGrid'
	});
	$('body').loadingModal('show');
}

function showPleaseWait(message) {
	$('body').loadingModal({
		text: message,
		animation: 'cubeGrid'
	});
	$('body').loadingModal('show');
}

function hidePleaseWait() {
	$('body').loadingModal('destroy');
}

function enforceArrayFromElements(filesData) {
	var result;
	if (filesData == undefined) {
		result = undefined;
	} else {
		if (Array.isArray(filesData)) {
			result = filesData;
		} else {
			var filesArray = new Array();
			filesArray[0] = filesData;
			result = filesArray;
		}
	}
	return result;
}

function row(name, value) {
	let html;
	if (value == undefined) {
		html = "";
	} else {
		html = "<tr><td>" + name + "</td><td>" + value + "</td></tr>";
	}
	return html;
}

function cell(value) {
	return "<td>" + value + "</td>";
}

function capitalizeFirstLetter(line) {
	if (line != null && line !== undefined && line !== "") {
		return line.charAt(0).toUpperCase() + line.slice(1);
	} else {
		return line;
	}
}

function findAfterFirst(line, after) {
	return line.substring(line.indexOf(after) + after.length);
}

function formatDateTime(isoDate) {
	let DateTime = luxon.DateTime;
	let date = DateTime.fromISO(isoDate).setLocale(USER_LOCALE);
	let format = date.toLocaleString(DateTime.DATE_HUGE) + ", ";

	let beforeMinus = USER_LOCALE.split("-")[0];
	if (beforeMinus != undefined && beforeMinus != null) {
		language = beforeMinus.toLowerCase();
		if (language === 'nl') {
			format = format + date.toFormat("H.mm") + " uur";
		} else {
			if (language === 'sv') {
				format = format + "kl. " + date.toFormat("HH.mm");
			} else {
				format = format + date.toLocaleString(DateTime.TIME_24_SIMPLE);
			}
		}
	}
	return format;
}

function formatDate(isoDate) {
	let DateTime = luxon.DateTime;
	let date = DateTime.fromISO(isoDate).setLocale(USER_LOCALE);
	let format = date.toLocaleString(DateTime.DATE_FULL);
	return format;
}

function getTimeAgo(isoDate) {
	let DateTime = luxon.DateTime;
	let date = DateTime.fromISO(isoDate).setLocale(USER_LOCALE);
	let ago = date.toRelativeCalendar();
	ago = capitalizeFirstLetter(ago);
	return ago;
}

function getDateLabel(isoDate) {
	let fullDateTime = formatDate(isoDate);
	let ago = getTimeAgo(isoDate);
	let dateLabel = ago + ", " + fullDateTime;
	return dateLabel;
}

function getTimeAgoAndDate(isoDate) {
	let timeAgo = getTimeAgo(isoDate);
	let date = formatDate(isoDate);
	return timeAgo + ", " + date;
}

function getTitle(photo) {
	let date = photo.dateTaken;
	if (date === undefined || date === null) {
		date = photo.dateLastModified;
	}
	let dateTitle;
	if (date === undefined || date === null) {
		dateTitle = "";
	} else {
		dateTitle = getTimeAgoAndDate(date);
	}
	let locationInfo = getLocationInfo(photo);
	let title = dateTitle;
	if (locationInfo !== "") {
		title = title + ", " + locationInfo;
	}
	return title;
}

function getLocationInfo(photo) {
	let locationInfo = "";
	let address = photo.address;
	if (address != undefined) {
		let addressInfo = "";
		if (address.street != undefined && address.street !== '') {
			addressInfo = addressInfo + address.street + ", ";
		}
		if (address.place != undefined && address.place !== '') {
			addressInfo = addressInfo + address.place + ", ";
		}
		addressInfo = addressInfo + address.country.name;
		locationInfo += ". ";
		let distance = photo.distanceFromAddress;
		let distanceInfo = "";
		let km = distance.kilometers;
		if (km > 0) {
			distanceInfo = km + " kilometre";
			if (km > 1) {
				distanceInfo += "s";
			}
			distanceInfo += " from ";
		} else {
			let hecto = distance.hectometres;
			if (hecto > 5) {
				distanceInfo = hecto + "00" + " metres from ";
			}
		}
		if (distanceInfo === "") {
			distanceInfo = "at ";
		}
		locationInfo = distanceInfo + addressInfo;
		locationInfo = htmlEncode(locationInfo);
	}
	return locationInfo;
}

function getLargeInfoLabel(photo) {
	let info = getLocationInfo(photo) + '<br>';
	info += formatDateTime(photo.dateTaken) + "<br>";
	if (photo.source != undefined) {
		info += htmlEncode(photo.source);
	}
	return info;
}

function getThumbDimensionHtml(photo) {
	let thumbHeight = photo.thumbHeight;
	let thumbWidth = photo.thumbWidth;
	let width = thumbWidth / thumbHeight * THUMB_HEIGHT;
	let title = photo.title;
	if (title == undefined || title !== "") {
		title = "..."
	}
	let sizeHtml = "alt='" + title + "' width='" + width + "' height='" + THUMB_HEIGHT + "'";
	return sizeHtml;
}

function getVideoDurationLabel(photo) {
	dateObj = new Date(photo.duration * 1000);
	hours = dateObj.getUTCHours();
	minutes = dateObj.getUTCMinutes();
	seconds = dateObj.getSeconds();
	timeString = hours.toString().padStart(2, '0') + ':' +
		minutes.toString().padStart(2, '0') + ':' +
		seconds.toString().padStart(2, '0');
	return timeString;
}

function setSessionStoreTimeline(timeline) {
	sessionStorage.setItem("PHOTOS_TIMELINE", JSON.stringify(timeline));
}

function getSessionStorageTimeline() {
	return JSON.parse(sessionStorage.getItem("PHOTOS_TIMELINE"));
}

function goto(url) {
	if (typeof saveStateBeforeGoto === "function") {
		saveStateBeforeGoto(url);
	}
	location.href = url;
}

