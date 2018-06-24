package edu.uniba.di.lacam.kdde.ws4j.util;

import edu.uniba.di.lacam.kdde.lexical_db.ILexicalDatabase;
import edu.uniba.di.lacam.kdde.lexical_db.data.Concept;
import edu.uniba.di.lacam.kdde.lexical_db.item.Link;

import java.util.ArrayList;
import java.util.List;

public class GlossFinder {

	private static List<Link[]> linkPairs = new ArrayList<Link[]>(){{
		add(new Link[]{null, null});
		add(new Link[]{null, Link.HYPERNYM});
		add(new Link[]{null, Link.HYPONYM});
		add(new Link[]{null, Link.MERONYM});
		add(new Link[]{null, Link.HOLONYM});

		add(new Link[]{Link.HYPERNYM, null});
		add(new Link[]{Link.HYPERNYM, Link.HYPERNYM});
		add(new Link[]{Link.HYPERNYM, Link.HYPONYM});
		add(new Link[]{Link.HYPERNYM, Link.MERONYM});
		add(new Link[]{Link.HYPERNYM, Link.HOLONYM});

		add(new Link[]{Link.HYPONYM, null});
		add(new Link[]{Link.HYPONYM, Link.HYPERNYM});
		add(new Link[]{Link.HYPONYM, Link.HYPONYM});
        add(new Link[]{Link.HYPONYM, Link.MERONYM});
        add(new Link[]{Link.HYPONYM, Link.HOLONYM});

        add(new Link[]{Link.MERONYM, null});
        add(new Link[]{Link.MERONYM, Link.HYPERNYM});
        add(new Link[]{Link.MERONYM, Link.HYPONYM});
        add(new Link[]{Link.MERONYM, Link.MERONYM});
        add(new Link[]{Link.MERONYM, Link.HOLONYM});

        add(new Link[]{Link.SYNSET, null});
        add(new Link[]{Link.SYNSET, Link.HYPERNYM});
        add(new Link[]{Link.SYNSET, Link.HYPONYM});
        add(new Link[]{Link.SYNSET, Link.MERONYM});
        add(new Link[]{Link.SYNSET, Link.HOLONYM});
	}};

	private ILexicalDatabase db;

	public GlossFinder(ILexicalDatabase db) {
		this.db = db;
	}

	public List<SuperGloss> getSuperGlosses(Concept synset1, Concept synset2) {
		List<SuperGloss> glosses = new ArrayList<>(linkPairs.size());
		for (Link[] links : linkPairs) {
			SuperGloss sg = new SuperGloss();
			sg.gloss1 = db.getGloss(synset1, links[0]);
			sg.gloss2 = db.getGloss(synset2, links[1]);
			sg.link1  = links[0] != null ? links[0].getName() : " ";
			sg.link2  = links[1] != null ? links[1].getName() : " ";
			sg.weight = 1.0D;
			glosses.add(sg);
		}
		return glosses;
	}

	public static class SuperGloss {

		private List<String> gloss1;
		private List<String> gloss2;
		private String link1;
		private String link2;
		private double weight;

		public List<String> getGloss1() {
			return gloss1;
		}

		public List<String> getGloss2() {
			return gloss2;
		}

		public String getLink1() {
			return link1;
		}

		public String getLink2() {
			return link2;
		}

		public double getWeight() {
			return weight;
		}
	}
}