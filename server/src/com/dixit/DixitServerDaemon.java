package com.dixit;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class DixitServerDaemon extends NanoHTTPD {

	private static List<Client> clients = new ArrayList<Client>();

	private static int storyTellerIndex = 0;

	public DixitServerDaemon() throws IOException {
		super(8080, new File("."));
	}

	public Response serve(String uri, String method, Properties header,
			Properties params, Properties files) {

		if (uri.startsWith("/newclient")) {
			return serveNewClient(params);
		} else if (uri.startsWith("/score")) {
			return serveScore();
		} else if (uri.startsWith("/vote")) {
			return serveVote(params);
		} else {
			return new NanoHTTPD.Response(HTTP_NOTFOUND, MIME_HTML, "");
		}
	}

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

	private void updateScore() {
		System.out.println("Updating score");

		int theRightCard = clients.get(storyTellerIndex).getVotedCard();
		boolean theRightCardWasChosen = false;
		int[] scoresToAdd = new int[clients.size()];

		clients.get(storyTellerIndex).addScore(1);

		// for ()

		for (int i = 0; i < clients.size(); ++i) {
			clients.get(i).resetVote();
		}

		storyTellerIndex++;
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
