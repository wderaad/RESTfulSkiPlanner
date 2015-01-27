<!DOCTYPE html>
<html lang="en">
<head>
<title></title>
<link rel="stylesheet" type="text/css" href="/css/site.css">
<link rel="stylesheet" type="text/css" href="/css/jquery-ui.min.css">
<link rel="stylesheet" type="text/css" href="/DataTables-1.10.4/media/css/jquery.dataTables.css">
<link rel="stylesheet" type="text/css" href="/DataTables-1.10.4/Plugins/integration/jqueryui/dataTables.jqueryui.css">
<script type="text/javascript" charset="utf8" src="/js/jquery-1.11.2.min.js"></script>
<script type="text/javascript" charset="utf8" src="/css/jquery-ui.min.js"></script>
<script type="text/javascript" charset="utf8" src="/DataTables-1.10.4/media/js/jquery.dataTables.js"></script>
<script type="text/javascript" charset="utf8" src="/DataTables-1.10.4/Plugins/integration/jqueryui/dataTables.jqueryui.js"></script>
<script type="text/javascript" charset="utf8" src="/js/site.js"></script>
<script>
var contactDataTable;
var dialog;
$(function() {
	$(document).ready( function () {
		contactDataTable = $('#contactTable').DataTable(
	    	{
	    		"paging":true,
	    		"ajax": restURL+contactBase,
	    		"columns":[
	    			{"sTitle":"ID", "data":"id", "defaultContent":"", "sClass":"center"},
	    			{"sTitle":"First Name", "data":"nameFirst", "defaultContent":"", "sClass":"center"},
	    			{"sTitle":"Last Name", "data":"nameLast", "defaultContent":"", "sClass":"center"},
	    			{"sTitle":"Phone Number", "data":"numberCell", "defaultContent":"", "sClass":"center"},
	    			{"sTitle":"Email address", "data":"email", "defaultContent":"", "sClass":"center"},
	    			{"sTitle":"Birthday", "data":"birthday", "defaultContent":"", "sClass":"center"}
	    		           
	    		],
	    		"columnDefs": [
					{"targets": [ 0 ], "visible": false, "searchable": false}
				]
	    	}		
	    );
	    
	    $('#contactTable tbody').on( 'click', 'tr', function () {
	        if ( $(this).hasClass('selected') ) {
	            $(this).removeClass('selected');
	        }
	        else {
	        	contactDataTable.$('tr.selected').removeClass('selected');
	            $(this).addClass('selected');
	        }
	    } );
	    
	    dialog = $( "#contactPopupDiv" ).dialog({
	        autoOpen: false,
	        height: 380,
	        width: 540,
	        modal: true,
	        dialogClass: "noclose",
	        buttons: {
	          "Save": sendContact,
	          "Cancel": function() {
	            dialog.dialog( "close" );
	          }
	        },
	        close: function() {
	          //resetContactForm();
	          //allFields.removeClass( "ui-state-error" );
	        }
	      });
	    
	    $( "#createContactButton" ).button({icons: { primary: "ui-icon-person" }}
	      ).on( "click", function() {
	    	resetContactForm();
	        dialog.dialog( "open" );
	      });

	    $( "#editContactButton" ).button({icons: { primary: "ui-icon-pencil" }}
	      ).on( "click", function() {
	    	  var row = contactDataTable.row('.selected').data();
	    	  if(row === undefined) {
	    		  alert("Please select a contact and then click the Edit button.");
	    	  } else {
	    	  	fillContactForm(row);
	    	  	dialog.dialog( "open" );
	    	  }
	      });

	    
	    $( "#removeContactButton" ).button({icons: { primary: "ui-icon-trash" }}
	      ).on( "click", function() {
	    	  var row = contactDataTable.row('.selected').data();
	    	  if(row === undefined) {
	    		  alert("Please select a contact and then click the Remove button.");
	    	  } else {
	    	  	$.ajax({
	    		    type: "DELETE",
	    			url: restURL+contactBase+row.id,
	    		}).done(function(data) {
	    			if(showMessage(data)) {
	    				contactDataTable.row('.selected').remove().draw( false );
	    			}
	    		}).fail(function() {
	    			showMessage(data);
	    		});
	    	  	//contactDataTable.row('.selected').remove().draw( false );
	    	  }
	      });
	    
	    $( "#datePicker" ).datepicker({
	        changeMonth: true,
	        changeYear: true,
	        yearRange: ((new Date().getFullYear())-100)+':'+(new Date().getFullYear())
	      });
	});
});


var sendContact = function() {
	var cId = document.getElementById("contactPopupForm")['contact.id'].value;
	var method = (cId ? "PUT" : "POST");
	$.ajax({
	    type: method,
		data: $('#contactPopupForm').serialize(),
		url: restURL+contactBase+cId
	}).done(function(data) {
		if(showMessage(data)) {
			if(method == "PUT") {
				contactDataTable.row('.selected').remove().draw( false );
			}
			contactDataTable.row.add(data.data).draw();
			dialog.dialog( "close" );
		}
	}).fail(function() {
		showMessage(data);
	});
		
};


var showMessage = function(data) {
	if(!data.success) {
		alert(data.message);
	}
	return data.success;
}

var fillContactForm = function(contactData) {
	var form = document.getElementById("contactPopupForm");
	form['contact.id'].value=(contactData.id ? contactData.id : "");
	form['contact.nameFirst'].value=(contactData.nameFirst ? contactData.nameFirst : "");
	form['contact.nameLast'].value=(contactData.nameLast ? contactData.nameLast : "");
	form['contact.numberCell'].value=(contactData.numberCell ? contactData.numberCell : "");
	form['contact.email'].value=(contactData.email ? contactData.email : "");
	form['contact.bday'].value=(contactData.birthday ? contactData.birthday : "");
	return;
} 
var resetContactForm = function() {
	var form = document.getElementById("contactPopupForm");
	form['contact.id'].value="";
	form['contact.nameFirst'].value="";
	form['contact.nameLast'].value="";
	form['contact.numberCell'].value="";
	form['contact.email'].value="";
	form['contact.bday'].value="";
	return;
}
</script>
</head>
<body>

<a id="createContactButton" href="#">Add</a>
<a id="editContactButton" href="#">Edit</a>
<a id="removeContactButton" href="#">Remove</a>
<br/><br/>
<table id="contactTable">

</table>

<div id="contactPopupDiv" title="Contact Form">
<form id="contactPopupForm">
<br/>
<table>
	<tbody id="contactPopupFormTable">
		<tr>
			<th>First Name</th>
			<td><input type="text" name="contact.nameFirst" maxlength="254"/></td>
		</tr>
		<tr>
			<th>Last Name</th>
			<td><input type="text" name="contact.nameLast" maxlength="254"/></td>
		</tr>
		<tr>
			<th>Phone Number</th>
			<td><input type="text" name="contact.numberCell" maxlength="30"/></td>
		</tr>
		<tr>
			<th>Email Address</th>
			<td><input type="text" name="contact.email" maxlength="254"/></td>
		</tr>
		<tr>
			<th>Birthday</th>
			<td>
				<input id="datePicker" type="text" name="contact.bday" maxlength="10"/>
				<input type="hidden" name="contact.id" value=""/>
			</td>
		</tr>
	</tbody>
</table>

</form>
</div>



</body>
</html>