<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="photos" uri="WEB-INF/tags.tld"%>
<%@ taglib prefix="icons" uri="jeltechnologies-icons"%>
<%@page import="com.jeltechnologies.icons.IconTag"%>

<jsp:include page="head.jsp"></jsp:include>
<script src="app/add-new.js"></script>
<link rel="stylesheet" type="text/css" href="app/photos.css" />
<title>Photos - New</title>
<script>
	const PLAY_ICON = '<%=new IconTag("play").toString()%>';
</script>
</head>
<body>
	<div id="status-modal" class="modal">
		<p id="status-title"></p>
		<div id="status-text"></div>
	</div>
	
	<div id="result-modal" class="modal">
		<icons:icon cssClass="result-icon" name="check" size="64"/>
		<div id="result-info"></div>
	</div>
	
	<div id="top-bar-center" class="top-menu">
		<photos:menu-new />
		<photos:main-menu selected="Add" />
	</div>
	<div id="add-new-periods">
	</div>
</body>
</html>