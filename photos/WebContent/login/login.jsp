<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>

<html>

<head>
<link rel="apple-touch-icon" sizes="57x57" href="images/favicon/apple-icon-57x57.png">
<link rel="apple-touch-icon" sizes="60x60" href="images/favicon/apple-icon-60x60.png">
<link rel="apple-touch-icon" sizes="72x72" href="images/favicon/apple-icon-72x72.png">
<link rel="apple-touch-icon" sizes="76x76" href="images/favicon/apple-icon-76x76.png">
<link rel="apple-touch-icon" sizes="114x114" href="images/favicon/apple-icon-114x114.png">
<link rel="apple-touch-icon" sizes="120x120" href="images/favicon/apple-icon-120x120.png">
<link rel="apple-touch-icon" sizes="144x144" href="images/favicon/apple-icon-144x144.png">
<link rel="apple-touch-icon" sizes="152x152" href="images/favicon/apple-icon-152x152.png">
<link rel="apple-touch-icon" sizes="180x180" href="images/favicon/apple-icon-180x180.png">
<link rel="icon" type="image/png" sizes="192x192" href="images/favicon/android-icon-192x192.png">
<link rel="icon" type="image/png" sizes="32x32" href="images/favicon/favicon-32x32.png">
<link rel="icon" type="image/png" sizes="96x96" href="images/favicon/favicon-96x96.png">
<link rel="icon" type="image/png" sizes="16x16" href="images/favicon/favicon-16x16.png">
<link rel="manifest" href="images/favicon/manifest.json">

<title>Photos</title>

<style>


header .header {
	background-color: #fff;
	height: 45px;
}

header a img {
	width: 134px;
	margin-top: 4px;
}

.login-page {
	width: 360px;
	padding: 8% 0 0;
	margin: auto;
}

.login-page .form .login {
	margin-top: -31px;
	margin-bottom: 26px;
}

.form {
	position: relative;
	z-index: 1;
	background: #FFFFFF;
	max-width: 360px;
	margin: 0 auto 100px;
	padding: 45px;
	text-align: center;
	box-shadow: 0 0 20px 0 rgba(0, 0, 0, 0.2), 0 5px 5px 0
		rgba(0, 0, 0, 0.24);
}

.form input {
	font-family: "Trebuchet MS", Arial, Helvetica, sans-serif;
	outline: 0;
	background: #f2f2f2;
	width: 100%;
	border: 0;
	margin: 0 0 15px;
	padding: 15px;
	box-sizing: border-box;
	font-size: 14px;
}

.form button {
	font-family: "Trebuchet MS", Arial, Helvetica, sans-serif;
	text-transform: uppercase;
	outline: 0;
	background-color: #328f8a;
	cursor: pointer;
	width: 100%;
	border: 0;
	padding: 15px;
	color: #FFFFFF;
	font-size: 14px;
	-webkit-transition: all 0.3 ease;
	transition: all 0.3 ease;
	cursor: pointer;
}

.form .message {
	margin: 15px 0 0;
	color: #b3b3b3;
	font-size: 12px;
}

.form .message a {
	color: #4CAF50;
	text-decoration: none;
}

.container {
	position: relative;
	z-index: 1;
	max-width: 300px;
	margin: 0 auto;
}

body {
	background-color: black;
	background-image: url('login/login.jpg');
	font-family: "Trebuchet MS", Arial, Helvetica, sans-serif;
	-webkit-font-smoothing: antialiased;
	-moz-osx-font-smoothing: grayscale;
}
</style>

</head>


<body onload="autoProcess()">
	<div class="login-page">
		<div class="form">

			<div class="login">
				<div class="login-header">
					<h3>WELCOME</h3>
					<p>Please enter your credentials.</p>
				</div>
			</div>

			<form name="loginform" class="login-form" method="POST" action="j_security_check">

				<input type="text" id="username" name="j_username" placeHolder="User"/>
				<input type="password" id="password" name="j_password" placeHolder="Password"/>
				<br />
				<button id="loginbutton" type="submit">Log in</button>
				
			</form>
		</div>
	</div>
</body>
</html>