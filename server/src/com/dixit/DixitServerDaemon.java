package com.dixit;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * The server class.
 *
 * It uses the embedded lightweight NanoHTTPD http server to handle
 * communication with clients.
 */
public class DixitServerDaemon extends NanoHTTPD {

	// Server constants
	private static final int SERVER_PORT = 8080;

	private static List<Client> clients = new ArrayList<Client>();

	/**
	 * The index of the Player who is telling the story.
	 *
	 * It is the index inside clients array. It is incremented every time the
	 * score is computed.
	 */
	private static int storyTellerIndex = 0;

	/**
	 * Server's constructor.
	 */
	public DixitServerDaemon() throws IOException {
		// I have no clue what the file instance represents :p
		super(SERVER_PORT, new File("."));
	}

	/**
	 * Global http server handler. Is the "dispatcher" of all Api handlers.
	 */
	public Response serve(String uri, String method, Properties header,
			Properties params, Properties files) {

		if (uri.startsWith("/newclient")) {
			// New client Api call
			return serveNewClient(params);
		} else if (uri.startsWith("/score")) {
			// Score Api call
			return serveScore();
		} else if (uri.startsWith("/vote")) {
			// Vote Api call
			return serveVote(params);
		} else {
			return new NanoHTTPD.Response(HTTP_NOTFOUND, MIME_HTML, "");
		}
	}

	/**
	 * New client/User/Player Api handler
	 */
	public Response serveNewClient(Properties params) {
		if (params.containsKey("name") == false) {
			String message = "Must specify a name to login";
			return new NanoHTTPD.Response(HTTP_BADREQUEST, MIME_PLAINTEXT,
					message);
		}

		String name = params.getProperty("name");

		// Make sure the name is not taken
		for (int i = 0; i < clients.size(); ++i) {
			if (clients.get(i).getName().equalsIgnoreCase(name)) {
				String message = "Name is already taken";
				return new NanoHTTPD.Response(HTTP_BADREQUEST, MIME_PLAINTEXT,
						message);
			}
		}

		Client client = new Client(name);
		clients.add(client);

		System.out.println("New client: " + client.getId() + " -> "
				+ client.getName());

		String response = "" + client.getId();
		return new NanoHTTPD.Response(HTTP_OK, MIME_HTML, response);
	}

	/**
	 * Score Api handler
	 */
	public Response serveScore() {
		StringBuilder builder = new StringBuilder();

		for (int i = 0; i < clients.size(); ++i) {
			Client client = clients.get(i);
			String line = client.getName() + "=" + client.getScore();

			builder.append(line + "\n");
		}

		String message = builder.toString();
		return new NanoHTTPD.Response(HTTP_OK, MIME_PLAINTEXT, message);
	}

	/**
	 * Vote api handler
	 */
	public Response serveVote(Properties params) {
		if (params.containsKey("userId") == false
				|| params.containsKey("votedCard") == false
				|| params.containsKey("myCard") == false) {
			String message = "Must specify user id, card to vote and your card";
			return new NanoHTTPD.Response(HTTP_BADREQUEST, MIME_PLAINTEXT,
					message);
		}

		String userId = params.getProperty("userId");
		String votedCard = params.getProperty("votedCard");
		String myCard = params.getProperty("myCard");

		System.out.println("Voted : " + userId + " " + votedCard);

		for (int i = 0; i < clients.size(); ++i) {
			Client client = clients.get(i);

			if (client.getId() == Integer.parseInt(userId)) {
				client.vote(Integer.parseInt(votedCard),
						Integer.parseInt(myCard));
				break;
			}
		}

		boolean allUsersVoted = true;

		for (int i = 0; i < clients.size(); ++i) {
			if (clients.get(i).hasVoted() == false) {
				allUsersVoted = false;
				break;
			}
		}

		if (allUsersVoted) {
			updateScore();
		}

		return new NanoHTTPD.Response(HTTP_OK, MIME_PLAINTEXT, "");
	}

	/**
	 * Computes and updates the score.
	 */
	private void updateScore() {
		System.out.println("Updating score");

		int theRightCard = clients.get(storyTellerIndex).getMyCard();

		int[] scoresToAdd = new int[clients.size() + 1];
		int[] cardOwners = new int[clients.size() + 1];
		int[] votes = new int[clients.size() + 1];

		// build card owners and vote arrays
		for (int i = 0; i < clients.size(); ++i) {
			Client client = clients.get(i);
			cardOwners[client.getMyCard()] = client.getId();

			votes[client.getVotedCard()]++;
		}

		// The story teller votes his own card. Here that point is removed
		votes[theRightCard]--;

		// Compute the number of points each player gets in the current round
		if (votes[theRightCard] == clients.size() - 1
				|| votes[theRightCard] == 0) {
			// Treat special cases : the right card was chosen by all or none
			// player,
			// so all player except the story teller gets 2 points.

			for (int i = 0; i < clients.size(); ++i) {
				if (storyTellerIndex != i) {
					scoresToAdd[clients.get(i).getId()] += 2;
				}
			}
		} else {
			// The right card was voted at least one time and not by all
			// players.
			// The story teller and whoever guessed the card gets 3 points.
			//
			// The owner of other voted card gets 1 point.

			for (int i = 0; i < clients.size(); ++i) {
				Client client = clients.get(i);

				// If this player guessed the right card, he gets 3 points.
				// In this is included even the story teller.
				if (client.getVotedCard() == theRightCard) {
					scoresToAdd[client.getId()] += 3;
				}

				// Add 1 point for each time the card of the current player was
				// voted.
				// NOTE : The current player must not be the story teller.
				if (i != storyTellerIndex) {
					scoresToAdd[client.getId()] += votes[client.getMyCard()];
				}
			}
		}

		// Add scores to the board and reset votes
		for (int i = 0; i < clients.size(); ++i) {
			Client client = clients.get(i);
			clients.get(i).resetVote();
			clients.get(i).addScore(scoresToAdd[client.getId()]);
		}

		// The next person is the story teller
		storyTellerIndex = (storyTellerIndex + 1) % clients.size();
	}

	public static void main(String[] args) {
		try {
			new DixitServerDaemon();
		} catch (IOException ex) {
			System.err.println("Couldn't start server:\n" + ex);
			System.exit(-1);
		}
		System.out.println("Listening on port 8080. Hit Enter to stop.\n");
		try {
			System.in.read();
		} catch (Throwable t) {
			System.err.println("System.in.read did not behave well:\n" + t);
		}

	}
}
