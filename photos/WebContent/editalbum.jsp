<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="photos" uri="WEB-INF/tags.tld"%>

<div>
	<photos:editalbum />
	<div>
	  <button type="button" onclick="javascript:handleEditUserOK();return false;">OK</button>
	  <button type="button" onclick="javascript:handleEditUserCancel();return false;">Cancel</button>
	</div>
</div>