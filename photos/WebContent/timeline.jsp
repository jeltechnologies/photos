<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="photos" uri="WEB-INF/tags.tld"%>
<%@ taglib prefix="icons" uri="jeltechnologies-icons"%>
<jsp:include page="head.jsp"></jsp:include>
<script src="app/timeline.js"></script>
<link rel="stylesheet" type="text/css" href="app/photos.css" />

<title>Photos | Timeline</title>

</head>
<body>

	<div id="top-bar">
		<div id="top-bar-center" class="top-menu">
			<photos:menu-timeline />
			<photos:main-menu selected="Timeline"/>
		</div>
		<div id="top-bar-center" class="view-menu">
			<select id="grouping" onchange='groupingChanged()' class="select-filter">
				<option value="YEAR">By year</option>
				<option value="MONTH" selected>By month</option>
				<option value="WEEK">By week</option>
			</select>
			<select id="sorting" onchange='sortingChanged()' class="select-filter">
				<option value="NEWESTFIRST" selected>Newest first</option>
				<option value="OLDESTFIRST">Oldest first</option>
			</select>
			<select id="paging-items-per-page" onchange='itemsChanged()' class="select-filter">
				<option value="10">10 per page</option>
				<option value="15" selected>15 per page</option>
				<option value="20">20 per page</option>
				<option value="50">50 per page</option>
				<option value="75">75 per page</option>
				<option value="100">100 per page</option>
				<option value="200">200 per page</option>
				<option value="500">500 per page</option>
				<option value="1000">1000 per page</option>
			</select>
			<select id="mediatype" onchange='mediaTypeChanged()' class="select-filter">
				<option value="ALL" selected>All</option>
				<option value="P">Photos</option>
				<option value="M">Movies</option>
			</select>
			<select id="random" onchange='randomChanged()' class="select-filter">
				<option value="random" selected>Random cover</option>
				<option value="lasttaken">Last taken</option>
			</select>
			
		</div>
	</div>

	<div id="album" class="album">
	</div>
	
<div class="paging-in-center">
	<div class="paging-navigation">
		<button onclick="pagingFirst();">
			<icons:icon name="chevron-bar-left" size="8"/>
		</button>

		<button onclick="pagingPrevious();">
			<icons:icon name="chevron-left" size="8"/>
		</button>

		<span class="currentPage" id='currentPage-1'>.</span><span class="totalPages">..</span>

		<button onclick="pagingNext();">
			<icons:icon name="chevron-right" size="8"/>
		</button>

		<button onclick="pagingLast();">
			<icons:icon name="chevron-bar-right" size="8"/>
		</button>

	</div>
</div>

<script>
	$(document).ready(documentReady());
</script>

</body>

</html>
