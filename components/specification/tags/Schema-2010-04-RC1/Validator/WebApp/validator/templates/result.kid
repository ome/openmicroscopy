<?xml version="1.0"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:py="http://purl.org/kid/ns#" py:extends="'master.kid'">
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <title>Result for Validation</title>
  </head>
  <body>
    <div class="main_content">
      <h1>Summary</h1>
      <h2>Validated file: ${filepath}</h2>
      <h3>File schema: ${schema}</h3>
      <p>
        <ul>
          <li>
            <span py:if="result.isOmeXml and not result.hasUnresolvableIds">The file is <strong>valid</strong>. </span>
            <span py:if="result.isOmeXml and result.hasUnresolvableIds">The file is <strong>valid</strong> but <strong>incomplete</strong> as it has unresolved IDs. </span>
            <span py:if="not result.isOmeXml">The file is <strong>invalid</strong>. </span>
          </li>
          <li>
            <span py:if="result.isOmeTiff">The file is an OME-TIFF. </span>
            <span py:if="not result.isOmeTiff">The file is not an OME-TIFF. </span>
          </li>
          <li>
            <span py:if="result.hasParsedXml is True">The file's xml could be parsed. </span>
            <span py:if="result.hasParsedXml is None">The file's xml was not parsed. </span>
            <span py:if="result.hasParsedXml is False">The file's xml could not be parsed. </span>
          </li>
          <li>
            <span py:if="result.isInternallyConsistent is True">The file appears internally consistent. </span>
            <span py:if="result.isInternallyConsistent is None">The file was not checked for internal consistency. </span>
            <span py:if="result.isInternallyConsistent is False">The file failed checks for internal consistency. </span>
          </li>
          <li>
            <span py:if="result.isOmeTiffConsistent is True">The OME-TIFF data appears consistent with the OME-TIFF metadata. </span>
            <span py:if="result.isOmeTiffConsistent is None">The OME-TIFF data was not compared to the OME-TIFF metadata. </span>
            <span py:if="result.isOmeTiffConsistent is False">The OME-TIFF data appears inconsistent with the OME-TIFF metadata. </span>
          </li>
          <li>
            <span py:if="result.hasCustomAttributes">The file has custom attributes. </span>
            <span py:if="result.hasCustomAttributes is None">The file was not checked for custom attributes. </span>
            <span py:if="result.hasCustomAttributes is False">No custom attributes were found. </span>
          </li>
        </ul>
		<div id="debug-block" style="display:none;background-color:#DDDDFF;" >
	        <p>--debug-begin-------------------</p>
	        <p py:if="result.hasParsedXml">hasParsedXml: ${result.hasParsedXml}</p>
	        <p py:if="result.isOmeXml">isOmeXml: ${result.isOmeXml}</p>
	        <p py:if="result.isXsdValid">isXsdValid: ${result.isXsdValid}</p>
	        <p py:if="result.hasUnresolvableIds">hasUnresolvableIds: ${result.hasUnresolvableIds}</p>
	        <p py:if="result.isInternallyConsistent">isInternallyConsistent: ${result.isInternallyConsistent}</p>
	        <p py:if="result.isOmeTiff">isOmeTiff: ${result.isOmeTiff}</p>
	        <p py:if="result.isOmeTiffConsistent">isOmeTiffConsistent: ${result.isOmeTiffConsistent}</p>
	        <p py:if="result.hasCustomAttributes">hasCustomAttributes: ${result.hasCustomAttributes}</p>
	        <p py:if="result.theNamespace">theNamespace: ${result.theNamespace}</p>
	        <p>--debug-end---------------------</p>
		</div>
      </p>
      <div>
        <h3 class="table">Error list</h3>
        <table py:if="result.errorList">
          <tr>
            <th class="line">Line</th>
            <th class="column">Column</th>
            <th class="type">Type</th>
            <th class="msg">Message</th>
          </tr>
          <tr py:for="error in result.errorList">
            <td py:content="error.line">error here</td>
            <td py:content="error.column">error here</td>
            <td py:content="error.errortype">error here</td>
            <td>
              <div id="long">
                <p py:content="error.message">error here</p>
              </div>
            </td>
          </tr>
        </table>
        <p py:if="not result.errorList">None</p>
        <h3 class="table">Warning list</h3>
        <table py:if="result.warningList">
          <tr>
            <th class="line">Line</th>
            <th class="column">Column</th>
            <th class="type">Type</th>
            <th class="msg">Message</th>
          </tr>
          <tr py:for="warning in result.warningList">
            <td py:content="warning.line">error here</td>
            <td py:content="warning.column">error here</td>
            <td py:content="warning.errortype">error here</td>
            <td>
              <div id="long">
                <p py:content="warning.message">error here</p>
              </div>
            </td>
          </tr>
        </table>
        <p py:if="not result.warningList">None</p>
        <h3 class="table">Unresolved list</h3>
        <table py:if="result.unresolvedList">
          <tr>
            <th class="line">Line</th>
            <th class="column">Column</th>
            <th class="type">Type</th>
            <th class="msg">Message</th>
          </tr>
          <tr py:for="unresolve in result.unresolvedList">
            <td py:content="unresolve.line">error here</td>
            <td py:content="unresolve.column">error here</td>
            <td py:content="unresolve.errortype">error here</td>
            <td>
              <div id="long">
                <p py:content="unresolve.message">error here</p>
              </div>
            </td>
          </tr>
        </table>
        <p py:if="not result.unresolvedList">None</p>
      </div>
    </div>
  </body>
</html>
