package com.dna.comms.entities;

import java.util.Date;

public class ProsecutionCommTitle {
	private int id;
	private String link;
	private Date date;
	private String title;
	private String titleUnicode;

	public ProsecutionCommTitle(int id, Date date) {
		super();
		this.id = id;
		this.date = date;
	}

	public ProsecutionCommTitle(int id, Date date, String link, String title, String titleUnicode) {
		super();
		this.id = id;
		this.link = link;
		this.title = title;
		this.titleUnicode = titleUnicode;
		this.date = date;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitleUnicode() {
		return titleUnicode;
	}

	public void setTitleUnicode(String titleUnicode) {
		this.titleUnicode = titleUnicode;
	}

	@Override
	public String toString() {
		return "CommSuspectTitle [id=" + id + ", link=" + link + ", date=" + date + ", title=" + title + "]";
	}

}
