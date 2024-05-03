<%@page import="com.jeltechnologies.photos.Settings"%>
<style>
  		
body {
  font: normal normal 13px/1.4 Segoe,"Segoe UI",Calibri,Helmet,FreeSans,Sans-Serif;
}

.album-title {
	color: <%=Settings.get(session).getColorBlack()%>;
	font-size: x-large;
}

#album {

}

#album-name-edit-input {
 	font-size: 18px;
 }
 
#album-name-edit button {
 	margin: 4px;
 }

#album-name-sub {
	float: left;
	font-size: x-large;
}

.new-photo-list ul {
	position: relative;
	list-style-type: none;
	display: block;
	padding : 0px;
}

.new-photo-list li {
	position: relative;
	list-style-type: none;
	display: inline-block;
	margin: auto;
	text-align: center;
	overflow: hidden;
	padding-right: 4px;
}

.new-photo-list img {
	padding: 16px;
}

.selected-photo {
	 border: 5px solid blue;
}

.image-in-album, .image-cover-album  {
	position: relative;
	padding-right: 4px;
}

.image-cover-album {
	float: left;
}

/*

.albumlist {
	display: flex;
	justify-content: space-between;
	flex-wrap: wrap;
}

.albumlist::after {
    content: "";
    flex: auto;
}

*/

.album img {
	height: <%=Settings.get(session).getAlbumThumbHeight()%>px;
	width: <%=Settings.get(session).getAlbumThumbWidth()%>px;
	object-fit: cover; 
}

.video-icon, .phototitle {

}

.video-icon {
	position: absolute;
	bottom: 20px;
	left: 80px;
	display: flex;
	font-size: xx-large;
	color: <%=Settings.get(session).getColorWhite()%>;
}

.phototitle, .hiddentitle {
	font-size: small;
	color: white;
	background: rgba(0, 0, 0, 0.3);
	padding-left:  4px;
	padding-right: 4px;
	position: absolute;
	bottom: 10px;
	left: 20px;
	display: flex;
}

.hiddentitle {
	position: absolute;
	bottom: 100px;
	left: 80px;
	display: flex;
}

.backbtn {
	background-color: <%=Settings.get(session).getColorWhite()%>;
	color: <%=Settings.get(session).getColorBlue()%>;
	padding-right: 16px;
	font-size:  xx-large;
	border: none;
	cursor: pointer;
}

.backbtn:hover, .backbtn:focus {
	background-color: <%=Settings.get(session).getColorWhite()%>;
}

#top-bar {
	margin-bottom: 16px;
}

#top-bar-left {
	color: <%=Settings.get(session).getColorBlack()%>;
}

#top-bar-center {
    display: flex;
    align-items: center;
    justify-content: center;
}

#top-bar-right {
}

#top-bar-left, 
#top-bar-center, 
#top-bar-right {
	display: inline-block;
	padding-right: 64px;
}

.sitelogo {
	padding-right: 32px;
	max-height: 24px;
}

.view-selected, .view {
	font-size: large;
}

.view-selected {
	text-decoration:overline;
	padding-right: 32px;
}

.top-menu a:link, #top-bar-center a:visited {
   color:  grey;
   padding-right: 32px;
   text-decoration: none;
   display: inline-block;
}

.top-menu a:hover, #top-bar-center a:active {
  color: <%=Settings.get(session).getColorBlack()%>;
}

#album-name-edit {
 	display: none;
}

.switch {
  position: relative;
  display: inline-block;
  width: 40px;
  height: 20px;
  background-color: rgba(0, 0, 0, 0.25);
  border-radius: 20px;
  transition: all 0.3s;
  cursor: pointer;
}

.switch::after {
  content: '';
  position: absolute;
  width: 18px;
  height: 18px;
  border-radius:50%;
  background-color: white;
  top: 1px;
  left: 1px;
  transition: all 0.3s;
}

.checkbox-switch:checked + .switch::after {
  left : 20px;
}
.checkbox-switch:checked + .switch {
  background-color: <%=Settings.get(session).getColorBlue()%>;
}

.checkbox-switch {
  display : none;
}



.button {
  background-color: <%=Settings.get(session).getColorBlue()%>;
  border: none;
  color: white;
  padding: 15px 32px;
  margin: 8px;
  text-align: center;
  text-decoration: none;
  display: inline-block;
  font-size: 16px;
}

/*
DROPDOWN from https://www.w3schools.com/howto/tryit.asp?filename=tryhow_css_js_dropdown_hover
*/

.dropbtn {
	background-color: <%=Settings.get(session).getColorWhite()%>;
	color: <%=Settings.get(session).getColorBlack()%>;
	padding: 16px;
	font-size: small;
	border: none;
	cursor: pointer;
	z-index: 10;
}

.dropbtn:hover, .dropbtn:focus {
	background-color: <%=Settings.get(session).getColorWhite()%>;
}

.dropdown {
	position: relative;
	display: inline-block;
}

.dropdown-content {
	display: none;
	position: absolute;
	background-color: <%=Settings.get(session).getColorWhite()%>;
	min-width: 160px;
	overflow: auto;
	box-shadow: 0px 8px 16px 0px rgba(0, 0, 0, 0.2);
	right: 0;
	z-index: 1;
}

.dropdown-content a {
	color: <%=Settings.get(session).getColorBlack()%>;
	padding: 12px 16px;
	text-decoration: none;
	display: block;
} 

.dropdown a:hover {
	background-color: <%=Settings.get(session).getColorYellow()%>;
}

.dropdown-content a:hover {background-color: <%=Settings.get(session).getColorYellow()%>;}

.dropdown:hover .dropdown-content {display: block;}

.dropdown:hover .dropbtn {background-color: <%=Settings.get(session).getColorYellow()%>;}


.paging-navigation {
	display: inline-block;
	margin: auto;
}

.paging-navigation button {
	width: auto;
	margin-left: 4px;
	display: inline;
	color: <%=Settings.get(session).getColorBlack()%>;
}

.paging-navigation select {
	width: auto;
	padding: 0px;
	margin-left: 4px;
	display: inline;
	color: <%=Settings.get(session).getColorBlack()%>;
}

.paging-in-center {
  display: flex;
  align-items: center;
  justify-content: center
  
}

.leaflet-attribution-flag {
	display: hidden;
}

.leaflet-popup {
	width: 442px;
}

.image-map-popup {
	display: block;
  	margin-left: auto;
}
  		
</style>