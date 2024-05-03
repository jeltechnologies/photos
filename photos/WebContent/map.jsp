<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@page import="com.jeltechnologies.photos.picures.map.CustomPeriod"%>
<%@page import="com.jeltechnologies.photos.picures.map.MapServlet"%>
<%@page import="com.jeltechnologies.photos.picures.map.Coordinate"%>
<%@page import="com.jeltechnologies.photos.picures.map.MapView"%>
<%@page import="com.jeltechnologies.photos.Environment"%>
<%@page import="com.jeltechnologies.photos.Settings"%>
<%@ taglib prefix="photos" uri="WEB-INF/tags.tld"%>
		<jsp:include page="head.jsp"></jsp:include>
		<title>Photos - Map</title>
		<link rel="stylesheet" type="text/css" href="app/photos.css" />
		<link rel="stylesheet" type="text/css" href="app/map.css" />
		<link rel="stylesheet" type="text/css" href="files/leaflet-1.9.3/leaflet.css">
		<script src="files/leaflet-1.9.3/leaflet.js"></script>
		<script src="app/map.js"></script>
	</head>
	<body>

		<div id="top-bar-center" class="top-menu">
			<photos:menu-timeline />
			<photos:main-menu selected="Map"/>
		</div>
		<div class="view-menu">
			<select id="period" onchange='periodChanged()' style='display: none; width: 150px' class="select-filter">
				<option value="thismonth">This month</option>
				<option value="last3months">Last 3 months</option>
				<option value="last12months" selected>Last 12 months</option>
				<option value="last24months">Last 24 months</option>
				<option value="last36months">Last 36 months</option>
				<option value="all">All</option>
				<option value="custom">Custom</option>
			</select>
			<span id="customperiod" style="margin-left: 16px; margin-right: 4px; display: none">
				From
				<select id="customperiod-from-month" onchange='periodChanged()' class="select-filter"></select>
				<select id="customperiod-from-year" onchange='periodChanged()' class="select-filter"></select>
				<span style="margin: 4px">To</span>
				<select id="customperiod-to-month" onchange='periodChanged()' class="select-filter"></select>
				<select id="customperiod-to-year" onchange='periodChanged()' class="select-filter"></select>
			</span>
		</div>

		<div id="map"></div>

		<script>
			<% 
			MapView view = MapView.getMapView(session);
			Coordinate mapCenter = view.getMapCenter();
			%>
			var defaultZoom = <%=MapServlet.DEFAULT_ZOOM%>;
			var userZoom = <%=view.getZoom()%>;
			var startLat = <%=mapCenter.getLatitude()%>;
			var startLng = <%=mapCenter.getLongitude()%>;
			var accessToken = "<%=Environment.INSTANCE.getConfig().getMapBoxAccessToken()%>";
			var earliestPhotoFromRequest = <%=CustomPeriod.getEarliestPhoto(request)%>;
	
			$(document).ready(pageReady());
		</script>

	</body>
</html>
