<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
          "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:py="http://purl.org/kid/ns#"
      py:extends="'master.kid'">
<head>
<meta content="text/html; charset=utf-8"
      http-equiv="Content-Type" py:replace="''"/>
<title>Result for Validation</title>
</head>
<body>
	<div class="main_content">
		<div>
			<table>
				<tr><th colspan="2">Files uploaded</th></tr>
				<tr py:for="file in uploadlist">
					<td py:content="file">path here</td>
					<td><a href="${tg.url('/result', filename=file)}">validate</a></td>
				</tr>
				<tr><th colspan="2"><br/></th></tr>
				<tr><td colspan="2">Files are removed from the server at the end of the session.</td></tr>
			</table>
		</div>
	</div>
</body>
</html>