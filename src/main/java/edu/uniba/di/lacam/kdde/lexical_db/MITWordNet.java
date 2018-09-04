package edu.uniba.di.lacam.kdde.lexical_db;

import edu.mit.jwi.IRAMDictionary;
import edu.mit.jwi.RAMDictionary;
import edu.mit.jwi.data.ILoadPolicy;
import edu.mit.jwi.item.*;
import edu.uniba.di.lacam.kdde.lexical_db.data.Concept;
import edu.uniba.di.lacam.kdde.lexical_db.item.Link;
import edu.uniba.di.lacam.kdde.lexical_db.item.POS;
import edu.uniba.di.lacam.kdde.ws4j.util.Log;
import edu.uniba.di.lacam.kdde.ws4j.util.Morpha;
import edu.uniba.di.lacam.kdde.ws4j.util.WS4JConfiguration;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public class MITWordNet implements ILexicalDatabase {

    private static final String WORDNET_PATH = System.getProperty("user.dir") + File.separator + "dict";

    private static IRAMDictionary dict;
    private static ConcurrentMap<String, List<String>> cache;

    private static final ILexicalDatabase db = new MITWordNet();

    private MITWordNet() {
        try {
            if (WS4JConfiguration.getInstance().useMemoryDB()) {
                Log.info("Loading WordNet into memory...");
                long t = System.currentTimeMillis();
                dict = new RAMDictionary(new URL("file", null, WORDNET_PATH), ILoadPolicy.IMMEDIATE_LOAD);
                dict.open();
                Log.info("WordNet loaded into memory in %d sec.", (System.currentTimeMillis()-t) / 1000L);
            } else {
                dict = new RAMDictionary(new URL("file", null, WORDNET_PATH), ILoadPolicy.NO_LOAD);
                dict.open();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (WS4JConfiguration.getInstance().useCache()) cache = new ConcurrentHashMap<>();
    }

    public static ILexicalDatabase getInstance() {
        return db;
    }

    @Override
    public Concept getConcept(String lemma, POS pos, int sense) {
        IIndexWord indexWord = dict.getIndexWord(lemma, edu.mit.jwi.item.POS.getPartOfSpeech(pos.getTag()));
        return indexWord != null ? new Concept(indexWord.getWordIDs().get(sense-1).getSynsetID().toString(), pos, lemma) : null;
    }

    @Override
    public List<Concept> getAllConcepts(String lemma, POS pos) {
        IIndexWord indexWord = dict.getIndexWord(lemma, edu.mit.jwi.item.POS.getPartOfSpeech(pos.getTag()));
        return indexWord != null ? indexWord.getWordIDs().stream().map(wordID -> new Concept(
                wordID.getSynsetID().toString(), pos, lemma)).collect(Collectors.toList()) : Collections.emptyList();
    }

    @Override
    public List<Concept> getLinkedSynsets(Concept concept, Link link) {
        List<Concept> linkedSynsets = new ArrayList<>();
        if (link == null || link.equals(Link.SYNSET)) linkedSynsets.add(concept);
        else {
            ISynsetID synsetID = SynsetID.parseSynsetID(concept.getSynsetID());
            if (link.equals(Link.MERONYM)) {
                linkedSynsets.addAll(getRelatedSynsets(synsetID, Link.MERONYM_MEMBER));
                linkedSynsets.addAll(getRelatedSynsets(synsetID, Link.MERONYM_SUBSTANCE));
                linkedSynsets.addAll(getRelatedSynsets(synsetID, Link.MERONYM_PART));
            } else if (link.equals(Link.HOLONYM)) {
                linkedSynsets.addAll(getRelatedSynsets(synsetID, Link.HOLONYM_MEMBER));
                linkedSynsets.addAll(getRelatedSynsets(synsetID, Link.HOLONYM_SUBSTANCE));
                linkedSynsets.addAll(getRelatedSynsets(synsetID, Link.HOLONYM_PART));
            } else linkedSynsets.addAll(getRelatedSynsets(synsetID, link));
        }
        return linkedSynsets;
    }

    private List<Concept> getRelatedSynsets(ISynsetID synsetID, Link link) {
        return dict.getSynset(synsetID).getRelatedSynsets(Pointer.getPointerType(link.getSymbol(), null))
                .stream().map(synset -> new Concept(synset.toString())).collect(Collectors.toList());
    }

    @Override
    public List<String> getWords(Concept concept) {
        return dict.getSynset(SynsetID.parseSynsetID(concept.getSynsetID())).getWords().stream().map(IWord::getLemma)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getGloss(Concept concept, Link link) {
        String key = concept + " " + link;
        if (WS4JConfiguration.getInstance().useCache()) {
            List<String> cachedObj = cache.get(key);
            if (cachedObj != null) return new ArrayList<>(cachedObj);
        }
        List<Concept> linkedSynsets = getLinkedSynsets(concept, link);
        List<String> glosses = new ArrayList<>(linkedSynsets.size());
        for (Concept linkedSynset : linkedSynsets) {
            String gloss;
            if (Link.SYNSET.equals(link)) gloss = concept.getName();
            else gloss = dict.getSynset(SynsetID.parseSynsetID(linkedSynset.getSynsetID())).getGloss()
                    .replaceFirst("; \".+", "");
            if (gloss == null) continue;
            gloss = gloss.replaceAll("[.;:,?!(){}\"`$%@<>]", " ")
                    .replaceAll("&", " and ")
                    .replaceAll("_", " ")
                    .replaceAll("[ ]+", " ")
                    .replaceAll("(?<!\\w)'", " ")
                    .replaceAll("'(?!\\w)", " ")
                    .replaceAll("--", " ").toLowerCase();
            if (WS4JConfiguration.getInstance().useStem()) gloss = Morpha.stemSentence(gloss);
            glosses.add(gloss);
        }
        if (WS4JConfiguration.getInstance().useCache()) cache.put(key, new ArrayList<>(glosses));
        return glosses;
    }
}
