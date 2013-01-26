package com.dixit;

public class Client {
	private static int clientCount = 0;
	private static final int VOTE_UNDEFINED = 0;

	private int id;
	private int score;
	private int votedCard;
	private int myCard;
	private String name;


	public Client(String name) {
		id = ++clientCount;
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public int getScore() {
		return score;
	}

	public void addScore(int scoreToAdd) {
		score += scoreToAdd;
	}

	public void vote(int cardNumber, int myCard) {
		votedCard = cardNumber;
		this.myCard = myCard;
	}

	public void resetVote() {
		votedCard = VOTE_UNDEFINED;
	}

	public int getVotedCard() {
		return votedCard;
	}

	public boolean hasVoted() {
		return votedCard != VOTE_UNDEFINED;
	}

	public String getName() {
		return name;
	}

	public int getMyCard() {
		return myCard;
	}
}
