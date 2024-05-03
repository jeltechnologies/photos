<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="photos" uri="WEB-INF/tags.tld"%>

<div >
	<photos:setalbumcover />
	<div>
	<button type="button" onclick="javascript:userDecidedCoverForAlbum();return false;">Set album cover</button>
	<button type="button" onclick="javascript:closeModal();return false;">Cancel</button>
	</div>
</div>

