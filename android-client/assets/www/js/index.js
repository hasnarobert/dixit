/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
var app = {
    // Application Constructor
    initialize: function() {
        this.bindEvents();
    },
    // Bind Event Listeners
    //
    // Bind any events that are required on startup. Common events are:
    // 'load', 'deviceready', 'offline', and 'online'.
    bindEvents: function() {
        document.addEventListener('deviceready', this.onDeviceReady, false);
    },
    // deviceready Event Handler
    //
    // The scope of 'this' is the event. In order to call the 'receivedEvent'
    // function, we must explicity call 'app.receivedEvent(...);'
    onDeviceReady: function() {
		// Setup listeners
		var loginButton = document.getElementById("login-button");
		loginButton.addEventListener("click", app.loginHandler, false);
	
	},

	loginHandler: function() {
		var serverAddress = document.getElementById("server-address").value;
		var playerName = document.getElementById("player-name").value;

		app.serverAddress = serverAddress;

		$.ajax(serverAddress + "newclient?name="+ playerName)
			.done(function(message) {
				app.clientId = parseInt(message);

				app.showOtherScreen();
				
			}).fail(function(xhr, textStatus, error) {
				if (error == "Bad Request") {
					alert("The name is already taken. Choose other.");
				} else {
					alert("Could not connect to server.")
				}
			});
	},

	showOtherScreen: function() {
		// Swap visibility of the containers
		document.getElementById("login-container").style.visibility = "hidden";
		document.getElementById("game-container").style.visibility = "visible";

		app.startGameLoop();
	},

	startGameLoop: function() {
		$.ajax(app.serverAddress + "score")
			.done(function(message) {
				alert(message);
			})
			.fail(function() {
				alert("Could not get score from server");
			});
	},

	buildDropDown: function(container, numberOfPlayers, callback) {
		// Remove all elements
		$(container).empty();

		// Recreate all elements
		for (var i = 0; i < numberOfPlayers; ++i) {
			var current = document.createElement("li");
			var a = document.createElement("a");
			current.appendChild(a);

			a.setAttribute("tabindex", "-1");
			a.setAttribute("href", "#");
			a.innerText = i+1;

			current.addEventListener("click", callback, false);

			var divider = document.createElement("li");
			divider.setAttribute("class", "divider");
			
			container.appendChild(current);
			if (i < numberOfPlayers - 1) {
				container.appendChild(divider);
			}
		}
	},

	voteChangedHandler: function () {
		var currentVoteContainer = document.getElementById("current-vote");
		currentVoteContainer.innerText = this.textContent;
	},

	mycardChangedHandler: function() {
		var mycardContainer = document.getElementById("current-mycard");
		mycardContainer.innerText = this.textContent;
	}

};


// Inititalize app
app.initialize();

