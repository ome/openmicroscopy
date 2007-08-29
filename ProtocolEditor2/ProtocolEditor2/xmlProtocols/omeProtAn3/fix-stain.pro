<?xml version="1.0" encoding="UTF-8"?>

<protocol input_type="Protocol Title" keywords="" elementelementName="PFA fix and Stain" protocol_file_elementelementName="fix-stain.pro">

<input input_type="Date" elementelementName="Date" value=""/>

<input default="" input_type="Memo Entry Step" elementelementName="Comments" value=""/>

<input description="This protocol describes fixing of adherent cells with formaldehyde" input_type="Protocol Section Title" elementelementName="PFA Fixation" substeps_collapsed="true">

	<input description="Make fresh formaldehyde. Mix 3.5ml ddH2O, 1.85g Merck paraformaldehyde and 10ul 10 N KOH in a 50ml tube. Boil water in beaker in microwave, place tube in water." input_type="Fixed Protocol Step" elementelementName="Prepare PFA"/>

	<input description="Let PFA mixture cool (5 mins) and then filter through 0.2um filter. This is 37% PFA.&#10;Dilute this in 1 x PBS. Usually dilute 10x to 3.7%" input_type="Fixed Protocol Step" elementelementName="Filter &amp; Dilute PFA"/>

	<input description="Take medium  off cells and wash coverslips by dipping 3 times in PBS" input_type="Fixed Protocol Step" elementelementName="Wash coverslips in buffer"/>

	<input description="Immediately add coverslips to the freshly diluted PFA. Swirl every 1-2 mins. Incubate for 5 mins." input_type="Fixed Protocol Step" elementelementName="Add coverslips to PFA"/>

	<input description="Aspirate off PFA. Add another bolus for another 5 mins." input_type="Fixed Protocol Step" elementelementName="Remove and add more PFA"/>

	<input description="Aspirate off PFA. Wash coverslips 2x with 0.1% Triton X-100 in PBS. Let them sit in it for 5 mins. Repeat." input_type="Fixed Protocol Step" elementelementName="Wash coverslips in detergent"/>

</input>

<input description="This protocol describes immunostaining of fixed and permeabilised cells" input_type="Protocol Section Title" elementelementName="Immuno-Staining" substeps_collapsed="true">

	<input description="Place coverslips in moist chamber, on parafilm, cell side up. Keep moist with wet tissues in chamber." input_type="Fixed Protocol Step" elementelementName="Prepare chamber"/>

	<input description="Block with 1% NDS/Abdil (spin before use) for 30-60 mins." input_type="Fixed Protocol Step" elementelementName="Block with Abdil"/>

	<input description="Antibodies are diluted in Abdil. Primary antibodies from different hosts can be mixed and incubated together. 30-60 minutes on cells." input_type="Fixed Protocol Step" elementelementName="Primary Antibody Incubation"/>

	<input description="Wash 3 times with TBS-0.1% Triton X-100.&#10;Wash is at least 3.5ml TBSTx, applied to one side of the coverslip and aspirated off the other side, so as to flow gently across the coverslip. Don't let them dry out. &#10;Repeat 3 times. &#10;Let sit for 3-5 mins." input_type="Fixed Protocol Step" elementelementName="Wash with TBS-Tx100"/><input description="Secondary antibody glycerol stocks are usually diluted 1:150 in Abdil. Incubate on coverslips for 30 mins.&#10;Wash as before with TBSTx." input_type="Fixed Protocol Step" elementelementName="Secondary Ab &amp; Wash"/>


</input>

<input description="Wash 3x with TBS. Coverlips will dry quickly. Don't let coverslips dry out!&#10;If using DAPI, incubate with 1ug/ml DAPI for 5 mins in TBS.&#10;Wash with TBS twice. &#10;Dab off xs TBS and mount onto a drop of mounting medium. Seal. " input_type="Fixed Protocol Step" elementelementName="Wash TBS"/><input input_type="Protocol Section Title" elementelementName="Blue Channel" substeps_collapsed="false"/>

<input default="DAPI" description="Usually DAPI:&#10;After washing off seconday Abs, wash with TBS (no Tween).&#10;DAPI is diluted 1:500 in TBS (no Triton), put on coverslips for a few seconds. " input_type="Text Entry Step" elementelementName="Name" value=""/>

<input input_type="Protocol Section Title" elementelementName="Green Channel" substeps_collapsed="false"/>

<input default="" input_type="Text Entry Step" elementelementName="Primary Antibody Name" value=""/>

<input default="1:500" description="Dilute in Abdil.&#10;Incubate on cells for 1 hour." input_type="Text Entry Step" elementelementName="Primary Antibody Dilution" value=""/>

<input default="" description="Usually diluted 1:150.&#10;Usually use Alexa dyes. Green is 488." input_type="Text Entry Step" elementelementName="Seconday Antibody" value=""/>

<input input_type="Protocol Section Title" elementelementName="Red Channel" substeps_collapsed=""/>

<input default="" input_type="Text Entry Step" elementelementName="Primary Antibody Name" value=""/>

<input default="1:500" description="Dilute in Abdil.&#10;Incubate on cells for 1 hour." input_type="Text Entry Step" elementelementName="Primary Antibody Dilution" value=""/>

<input default="" description="Usually diluted 1:150.&#10;Usually use Alexa dyes." input_type="Text Entry Step" elementelementName="Secondary Antibody" value=""/>

</protocol>