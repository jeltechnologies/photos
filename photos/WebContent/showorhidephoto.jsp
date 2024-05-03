<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="photos" uri="WEB-INF/tags.tld"%>

<div>
	<photos:showorhidephoto />
	<div>
		<button type="button" onclick="javascript:userDecidedShowOrHide();return false;">OK</button>
		<button type="button" onclick="javascript:closeModal();return false;">Cancel</button>
	</div>
</div>

