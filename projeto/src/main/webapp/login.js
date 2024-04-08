
function login() {
	fetch('../rest/login/',  {
		ethod: 'POST',
		headers: {
			'Content-type': 'application/json'
		},
		body: JSON.stringify({
			username: document.getElementById("login").value,
			password: document.getElementById("password").value,
		})
	}).then(response => {
              console.log('Response status:', response.status);
              if (!response.ok) {
                throw new Error('Login failed: ' + response.statusText);
              }
            })
            .then(data => {
              console.log('Response data:', data);
              alert('Login successful!');
              window.location.href = "user.html";
            })
            .catch(error => {
              console.error('Error:', error);
              alert(error.message);
            });

  }
function getCookies() {
	fetch('../rest/getCookie/', {
		method: 'POST',
		headers: {
			'Content-type': 'application/json'
		}
	}).then(res => res.json())
		.then(data => console.log(data))
		.catch(error => console.log(error))

}

function changePassword() {
	fetch('../rest/change/password/', {
		method: 'POST',
		headers: {
			'Content-type': 'application/json'
		},
		body: JSON.stringify({
			username: document.getElementById("username").value,
			oldPwd: document.getElementById("currentPassword").value,
			confirmPwd: document.getElementById("confirmNewPassword").value,
			newPwd: document.getElementById("newPassword").value,

		})

	}).then(res => {
		if (res.ok) {
			console.log('SUCESS')
			window.location.href = 'changePassword.html'
		} else {
			console.log('NOT SUCCESSFUL')
			window.location.href = 'user.html';
		}
		return res.json()
	})
		.then(data => console.log(data))
		.catch(error => console.log(error))
}

function redirectToSelectedOption() {
	// Get the selected option
	var selectedOption = document.getElementById("opt").value;

	// Redirect based on the selected option
	switch (selectedOption) {
		case "changeAttributes":
			window.location.href = "changeAttributes.html";
			break;
		case "changePassword":
			window.location.href = "changePassword.html";
			break;
		case "changeState":
			window.location.href = "changeState.html";
			break;
		case "changeRole":
			window.location.href = "changeRole.html";
			break;
		case "listUsers":
			window.location.href = "listUsers.html";
			break;
		case "removeUsers":
			window.location.href = "removeUsers.html";
			break;
		case "registerResource":
			window.location.href = "registerResource.html";
			break;
		case "login":
			window.location.href = 'index.html';
			break;
		default:
			// Handle default case
			break;
	}

}



/**
 *
 */