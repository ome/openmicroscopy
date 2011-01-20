<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
          "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:py="http://purl.org/kid/ns#"
      py:extends="'master.kid'">
<head>
<meta content="text/html; charset=utf-8"
      http-equiv="Content-Type" py:replace="''"/>
<title>Validator</title>
</head>
<body>
	<div class="main_content">
		<h1 id="welcome">Welcome to OME-XML Data Model Validator</h1>
		<br/>
		<div><form action="upload" method="post" enctype="multipart/form-data" id="validator">
			<label>Choose the file you would like validated (OME, TIFF, XML files only).</label><br/><br/>
			<label for="upload_file">Local file:</label>
			<input type="file" name="upload_file" id="upload_file"/> 
			<input type="submit" name="submit_upload" value="Upload"/>
			<div><br/>Files are removed from the server at the end of the session.</div>
		</form>
		</div>
	</div>
</body>
</html>