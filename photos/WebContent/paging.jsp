<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="photos" uri="WEB-INF/tags.tld"%>

<div class="paging-in-center">
	<div class="paging-navigation">
		<button onclick="pagingFirst();">
			<span class="typcn typcn-media-rewind"></span>
		</button>

		<button onclick="pagingPrevious();">
			<span class="typcn typcn-media-play-reverse"></span>
		</button>

		<span class="currentPage" id='currentPage-<%=request.getParameter("navigation_id")%>'>.</span> of <span class="totalPages">..</span>

		<button onclick="pagingNext();">
			<span class='typcn typcn-media-play'></span>
		</button>

		<button onclick="pagingLast();">
			<span class='typcn typcn-media-fast-forward'></span>
		</button>

		<select id="paging-items-per-page-<%=request.getParameter("navigation_id")%>" class="paging-items-per-page" onchange='itemsChanged(<%=request.getParameter("navigation_id")%>)'>
			<option value="10">10</option>
			<option value="20" selected>20</option>
			<option value="50">50</option>
			<option value="75">75</option>
			<option value="100">100</option>
			<option value="200">200</option>
			<option value="500">500</option>
			<option value="1000">1000</option>
		</select>
		
	</div>
</div>