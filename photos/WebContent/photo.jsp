<%@page import="com.jeltechnologies.photos.tags.PhotoGallerySwiper"%>
<%@page import="com.jeltechnologies.photos.datatypes.usermodel.User"%>
<%@page import="com.jeltechnologies.photos.datatypes.usermodel.RoleModel"%>
<%@page import="com.jeltechnologies.photos.Settings"%>
<%@page import="com.jeltechnologies.photos.Environment"%>
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="photos" uri="WEB-INF/tags.tld"%>
<%@ taglib prefix="icons" uri="jeltechnologies-icons"%>

	<jsp:include page="head.jsp"></jsp:include>
	<%
		User user = RoleModel.getUser(pageContext);
	%>
	<link rel="stylesheet" type="text/css" href="files/swiper-11.0.5/swiper-bundle.min.css">
	<script src="files/swiper-11.0.5/swiper-bundle.min.js"></script>
	<link rel="stylesheet" type="text/css" href="files/leaflet-1.9.3/leaflet.css">
	<script src="files/leaflet-1.9.3/leaflet.js"></script>
	<script>
		var mapAccessToken = "<%=Environment.INSTANCE.getConfig().getMapBoxAccessToken()%>";
	</script>
	
	<script src="app/photoswiper.js"></script>
	<link rel="stylesheet" type="text/css" href="app/photoswiper.css">
	<title>Photos</title>

</head>
<body>

 	<div id="loader" class="center"></div>
 
	<div id="swiper-left-top-menu">
		<button class="dropbtn" onclick="window.history.back();"><icons:icon name="x" size="48"/></button>
	</div>

	<div id="swiper-right-top-menu">
		<div class="dropdown">
  			<button class="dropbtn"><icons:icon name="three-dots-vertical" size="32"/></button>
  			<div class="dropdown-content">
    			<a href="javascript:downloadFromAlbumClicked()">Download</a>
    			<%
				if (user.isAdmin()) {
				%>
				<a href="javascript:setAsAlbumCoverClicked()">Set as album cover</a> 
				<a href="javascript:removeFromAlbumClicked()">Remove from album</a><a href="javascript:showOrHideClicked()">Show or hide</a> 
				<%
				}
				%>
  			</div>
		</div>
		
		<div class="dropdown">
  			<button class="dropbtn"><icons:icon name="share-fill" size="32"/></button>
  			<div class="dropdown-content">
    			<a href="javascript:shareClicked('normal')">Share - normal quality</a>
    			<a href="javascript:shareClicked('high')">Share - high quality</a>
  			</div>
		</div>
		
		<div class="dropdown">
  			<button class="dropbtn"><icons:icon name="info-circle-fill" size="32"/></button>
  			<div class="dropdown-content">
    			<a href="javascript:showInfoClicked()">Information</a>
  			</div>
		</div>
		
	</div>

	<div class="swiper mySwiper" onclick="userClickedSwiper();">
		<div class="swiper-wrapper">
		</div>
		<div id="navigation">
			<div class="swiper-button-next"></div>
			<div class="swiper-button-prev"></div>
			<div class="swiper-pagination"></div>
		</div>
	</div>
	
	<div id="set-album-cover-modal" class="modal"></div>
	<div id="show-or-hide-photo-modal" class="modal"></div>
	<div id="show-shared-link-modal" class="modal"></div>
	<div id="show-information-modal" class="modal">
		<div id="photo-meta"></div>
		<div id="map"></div>
		<div id="photo-title"></div>
	</div>
	
</body>


</html>