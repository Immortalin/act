package act.installer.reachablesexplorer;

import act.shared.Chemical;
import com.act.reachables.Cascade;
import com.act.reachables.ReactionPath;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mongojack.DBCursor;
import org.mongojack.JacksonDBCollection;
import org.twentyn.proteintodna.DNADesign;
import org.twentyn.proteintodna.DNAOrgECNum;
import org.twentyn.proteintodna.OrgAndEcnum;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class FreemarkerRenderer {
  public static final Logger LOGGER = LogManager.getFormatterLogger(FreemarkerRenderer.class);

  private static final String DEFAULT_REACHABLE_TEMPLATE_FILE = "Mediawiki.ftl";
  private static final String DEFAULT_PATHWAY_TEMPLATE_FILE = "MediaWikiPathways.ftl";

  private static final String DEFAULT_CASCADES_DIR = "/Volumes/shared-data/Michael/ActDataNewRun";

  private static final int MAX_SEQUENCE_LENGTH = 50;

  private static final int SEQUENCE_SAMPLE_START = 3000;
  private static final int SEQUENCE_SAMPLE_SIZE = 80;

  private static final Boolean HIDE_CASCADES_AND_PATHWAYS = false;

  private String reachableTemplateName;
  private String pathwayTemplateName;
  private Loader loader;

  // Note: there should be one of these per process.  TODO: make this a singleton.
  private Configuration cfg;
  private Template reachableTemplate;
  private Template pathwayTemplate;

  private JacksonDBCollection<DNADesign, String> dnaDesignCollection;

  public static void main(String[] args) throws Exception {
    Loader loader =
        new Loader("localhost", 27017, "wiki_reachables", "reachablesv8_release_thomas", "sequencesv8_release_thomas", "/Volumes/shared-data/Thomas/tempCacheRendering");

    FreemarkerRenderer renderer = FreemarkerRendererFactory.build(loader);
    //renderer.writePageToDir(new File("/Volumes/shared-data/Thomas/WikiPagesForUpload"));
    renderer.writePageToDir(
        new File("/Volumes/shared-data/Thomas/WikiContent/DecRelease/Reachables"),
        new File("/Volumes/shared-data/Thomas/WikiContent/DecRelease/Paths"),
        new File("/Volumes/shared-data/Thomas/WikiContent/DecRelease/Sequences"));
  }

  private FreemarkerRenderer(Loader loader) {
    this.reachableTemplateName = DEFAULT_REACHABLE_TEMPLATE_FILE;
    this.pathwayTemplateName = DEFAULT_PATHWAY_TEMPLATE_FILE;
    this.loader = loader;

  }

  private void init() throws IOException {
    cfg = new Configuration(Configuration.VERSION_2_3_23);

    cfg.setClassLoaderForTemplateLoading(
        this.getClass().getClassLoader(), "/act/installer/reachablesexplorer/templates");
    cfg.setDefaultEncoding("UTF-8");

    cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    cfg.setLogTemplateExceptions(true);

    reachableTemplate = cfg.getTemplate(reachableTemplateName);
    pathwayTemplate = cfg.getTemplate(pathwayTemplateName);

    // TODO: move this elsewhere.
    MongoClient client = new MongoClient(new ServerAddress("localhost", 27017));
    DB db = client.getDB("wiki_reachables");

    dnaDesignCollection = JacksonDBCollection.wrap(db.getCollection("dna_designs_4"), DNADesign.class, String.class);
  }

  public void writePageToDir(File reachableDestination,
                             File pathDestination,
                             File sequenceDestination) throws IOException, TemplateException{

    // Temporary fix: only write pages for molecules with pathways.  This should eventually iterate over all reachables.
    DBCursor<ReactionPath> cascadeCursor = Cascade.get_pathway_collection().find();

    Set<Long> seenIds = new HashSet<>();

    // Temporary fix: limit ourselves to only the molecules used in the demo wiki.
    Set<Long> targetsOfInterest = new HashSet<>(Arrays.asList(878L, 1443L, 1293L, 448L, 341L, 1496L, 1536L, 750L, 4026L, 716L));

    int i = 0;
    while (cascadeCursor.hasNext()) {
      // Hacked cursor munging to only consider targets of pathways.
      ReactionPath thisPath = cascadeCursor.next();
      if (!targetsOfInterest.contains(thisPath.getTarget())) {
        LOGGER.info("Skipping target not of interest");
        continue;
      }

      if (seenIds.contains(thisPath.getTarget())) {
        LOGGER.info("Skipping duplicate id %d", thisPath.getTarget());
        continue;
      }


      /* Temporary fix: create reachables on the fly for pathway targets to ensure we have documents to use when
       * generating the molecule pages.  This should not be necessary in a world where reachables are all loaded into
       * the DB before pathways. */
      Reachable r = loader.constructReachableFromId(thisPath.getTarget());
      loader.upsert(r);

      if (r == null) {
        LOGGER.error("Skipping id %d, because not found in the DB", thisPath.getTarget());
        continue;
      }

      seenIds.add(thisPath.getTarget());

      String inchiKey = r.getInchiKey();
      if (inchiKey != null) {
        LOGGER.info(inchiKey);
        List<PathwayDoc> pathwayDocsAndNames = generatePathDocuments(r, pathDestination, sequenceDestination);
        File f = new File(reachableDestination, inchiKey);
        Writer w = new PrintWriter(f);
        reachableTemplate.process(buildReachableModel(r, loader.getJacksonSequenceCollection(), pathwayDocsAndNames), w);
        w.close();
        assert f.exists();
        i++;

        if (i % 100 == 0) {
          LOGGER.info(i);
        }
      } else {
        LOGGER.error("page does not have an inchiKey");
      }
    }
  }

  private Object buildReachableModel(Reachable r,
                                     JacksonDBCollection<SequenceData, String> sequenceCollection,
                                     List<PathwayDoc> pathwayDocs
                                     ) {
    /* Freemarker's template language is based on a notion of "hashes," which are effectively just an untyped hierarchy
     * of maps and arrays culminating in scalar values (think of it like a JSON doc done up in plain Java types).
     * There are new facilities to run some Java accessors from within freemarker templates, but the language is
     * sufficiently brittle on its own that complicating things seems like a recipe for much pain.
     *
     * This is just how Freemarker works.  Oh well.
     */
    Map<String, Object> model = new HashMap<>();

    model.put("pageTitle", r.getPageName());

    model.put("inchi", r.getInchi());

    model.put("smiles", r.getSmiles());

    model.put("structureRendering", r.getStructureFilename());

    model.put("cascade", r.getPathwayVisualization());

    if (HIDE_CASCADES_AND_PATHWAYS) {
      model.put("hideCascades", true);
    }

    if (r.getWordCloudFilename() != null) {
      model.put("wordcloudRendering", r.getWordCloudFilename());
    }

    List<Precursor> orderedPrecursors = new ArrayList<>(r.getPrecursorData().getPrecursors());
    Collections.sort(orderedPrecursors, (a, b) -> {
      if (a.getSequences() != null && b.getSequences() == null) {
        return -1;
      }
      if (a.getSequences() == null && b.getSequences() != null) {
        return 1;
      }
      return Integer.valueOf(b.getSequences().size()).compareTo(a.getSequences().size());
    });

    List<Object> pathways = new ArrayList<>();
    for (PathwayDoc doc : pathwayDocs) {
      pathways.add(new HashMap<String, String>() {{
        put("link", doc.getPageName());
        put("name", doc.getPathText());
        // With help from http://stackoverflow.com/questions/13183982/html-entity-for-check-mark
        put("hasDna", doc.getHasDNA() ? "&#10003;" : "");
      }});
    }
    if (pathways.size() > 0) {
      model.put("pathways", pathways);
    }

    List<PatentSummary> patentSummaries = r.getPatentSummaries();
    if (patentSummaries != null && !patentSummaries.isEmpty()) {
      List<Map<String, String>> patentModel = patentSummaries.stream().
          map(p -> {
            return new HashMap<String, String>() {{
              put("title", p.getTitle());
              put("link", p.generateUSPTOURL());
              put("id", p.getId().replaceFirst("-.*$", "")); // Strip date + XML suffix, just leave grant number.
            }};
          }).
          collect(Collectors.toList());
      model.put("patents", patentModel);
    }

    Map<Chemical.REFS, BasicDBObject> xrefs = r.getXref();

    // Each XREF being populated in a Reachable as DBObjects, serialization and deserialization causes funky problems
    // to appear. In particular, casting to BasicDBObjects fails because Jackson deserialized it as a LinkedHashMap
    // See http://stackoverflow.com/questions/28821715/java-lang-classcastexception-java-util-linkedhashmap-cannot-be-cast-to-com-test
    if (xrefs.containsKey(Chemical.REFS.BING)) {
      BasicDBObject bingXref = xrefs.get(Chemical.REFS.BING);
      Map<String, Object> bingMetadata = (Map) bingXref.get("metadata");
      List<Map> bingUsageTerms = (List) bingMetadata.get("usage_terms");
      if (bingUsageTerms.size() > 0) {
        List<Map<String, Object>> bingUsageTermsModel = bingUsageTerms.stream()
            .map(usageTerm -> new HashMap<String, Object>() {{
              put("usageTerm", usageTerm.get("usage_term"));
              put("urls", usageTerm.get("urls"));
            }})
            .collect(Collectors.toList());
        Collections.sort(
            bingUsageTermsModel,
            (o1, o2) -> ((ArrayList) o2.get("urls")).size() - ((ArrayList) o1.get("urls")).size());
        model.put("bingUsageTerms", bingUsageTermsModel);
      }
    }

    if (xrefs.containsKey(Chemical.REFS.WIKIPEDIA)) {
      BasicDBObject wikipediaXref = xrefs.get(Chemical.REFS.WIKIPEDIA);
      String wikipediaUrl = wikipediaXref.getString("dbid");
      model.put("wikipediaUrl", wikipediaUrl);
    }

    if (r.getSynonyms() != null) {
      if (r.getSynonyms().getPubchemSynonyms() != null) {
        List<Map<String, Object>> pubchemSynonymModel = r.getSynonyms().getPubchemSynonyms().entrySet().stream()
            .map(entry -> new HashMap<String, Object>() {{
              put("synonymType", entry.getKey().toString());
              put("synonyms", entry.getValue().stream().collect(Collectors.toList()));
            }})
            .collect(Collectors.toList());
        model.put("pubchemSynonyms", pubchemSynonymModel);
      }
      if (r.getSynonyms().getMeshHeadings() != null) {
        List<Map<String, Object>> meshHeadingModel = r.getSynonyms().getMeshHeadings().entrySet().stream()
            .map(entry -> new HashMap<String, Object>() {{
              String key = entry.getKey().toString();
              put("synonymType", "NONE".equals(key) ? "MeSH" : key);
              put("synonyms", entry.getValue().stream().collect(Collectors.toList()));
            }})
            .collect(Collectors.toList());

        model.put("meshHeadings", meshHeadingModel);
      }
    }

    if (r.getPhysiochemicalProperties() != null) {
      PhysiochemicalProperties physiochemicalProperties = r.getPhysiochemicalProperties();
      model.put("physiochemicalProperties", new HashMap<String, String>() {{
        put("pka", String.format("%.2f", physiochemicalProperties.getPkaAcid1()));
        put("logp", String.format("%.2f", physiochemicalProperties.getLogPTrue()));
        put("hlb", String.format("%.2f", physiochemicalProperties.getHlbVal()));
      }});
    }

    return model;
  }

  // Limit long sequences to somethign we can show on a wiki page.
  private String truncateSequence(String seq) {
    if (seq.length() <= MAX_SEQUENCE_LENGTH) {
      return seq;
    }

    StringBuilder builder = new StringBuilder(MAX_SEQUENCE_LENGTH + 3);
    builder.append(seq.substring(0, MAX_SEQUENCE_LENGTH / 2));
    builder.append("...");
    builder.append(seq.substring(seq.length() - MAX_SEQUENCE_LENGTH / 2, seq.length() - 1));
    return builder.toString();
  }

  private static class PathwayDoc {
    String pageName;
    String pathText;
    Boolean hasDNA;

    public PathwayDoc(String pageName, String pathText, Boolean hasDNA) {
      this.pageName = pageName;
      this.pathText = pathText;
      this.hasDNA = hasDNA;
    }

    public String getPageName() {
      return pageName;
    }

    public String getPathText() {
      return pathText;
    }

    public Boolean getHasDNA() {
      return hasDNA;
    }
  }

  public List<PathwayDoc> generatePathDocuments(
      Reachable target, File pathDestination, File sequenceDestination) throws IOException, TemplateException {
    DBCursor<ReactionPath> cursor = Cascade.get_pathway_collection().find(new BasicDBObject("target", target.getId()));

    List<PathwayDoc> pathwayDocs = new ArrayList<>();
    while (cursor.hasNext()) {
      ReactionPath path = cursor.next();

      if (target == null) {
        // This should not happen, methinks.
        String msg = String.format("Unable to locate chemical %d in reachables db", path.getTarget());
        LOGGER.error(msg);
        throw new RuntimeException(msg);
      }

      String sourceDocName = makeSourceDocName(target);
      if (sourceDocName == null) {
        LOGGER.error("Target %d does not have inchiKey", path.getTarget());
        continue;
      }

      String pathwayDocName = String.format("Pathway_%s_%d", sourceDocName, path.getRank());

      List<Triple<String, String, DNAOrgECNum>> designDocsAndSummaries = path.getDnaDesignRef() != null ?
          renderSequences(sequenceDestination, pathwayDocName, path.getDnaDesignRef()) : Collections.emptyList();

      Pair<Object, String> model = buildPathModel(path, target, designDocsAndSummaries);
      if (model != null) {
        pathwayTemplate.process(model.getLeft(), new FileWriter(new File(pathDestination, pathwayDocName)));
      }

      pathwayDocs.add(new PathwayDoc(
          pathwayDocName,
          model.getRight(),
          designDocsAndSummaries != null && designDocsAndSummaries.size() > 0)
      );
    }
    return pathwayDocs;
  }

  private List<Triple<String, String, DNAOrgECNum>> renderSequences(File sequenceDestination, String docPrefix, String seqRef) throws IOException {
    DNADesign designDoc = dnaDesignCollection.findOneById(seqRef);
    if (designDoc == null) {
      LOGGER.error("Could not find dna seq for id %s", seqRef);
      return Collections.emptyList();
    }

    List<Triple<String, String, DNAOrgECNum>> sequenceFilesAndSummaries = new ArrayList<>();

    List<DNAOrgECNum> designs = new ArrayList<>(designDoc.getDnaDesigns());
    Collections.sort(designs, (a, b) -> {
      int result = a.getNumProteins().compareTo(b.getNumProteins());
      if (result != 0) {
        return result;
      }
      // Comparing org and ec number is expensive, so ignore it for now.
      return a.getDna().compareTo(b.getDna());
    });

    for (int i = 0; i < designs.size(); i++) {
      String design = designs.get(i).getDna();
      if (design == null) {
        LOGGER.error("No DNA found for design %d, reference %s", i, seqRef);
        continue;
      }
      design = design.toUpperCase();
      int designSize = design.length();
      String shortVersion;
      if (designSize > SEQUENCE_SAMPLE_START + SEQUENCE_SAMPLE_SIZE) {
        shortVersion = design.substring(SEQUENCE_SAMPLE_START, SEQUENCE_SAMPLE_START + SEQUENCE_SAMPLE_SIZE);
      } else {
        shortVersion = design.substring(
            designSize / 2 - SEQUENCE_SAMPLE_SIZE / 2,
            designSize / 2 + SEQUENCE_SAMPLE_SIZE / 2);
      }

      // Hack to get the DNA design sequence to be displayed on 4 lines. Introduce <br> tags at each 4th of the string.
      shortVersion = StringUtils.join(new String[]{
          shortVersion.substring(0, SEQUENCE_SAMPLE_SIZE / 4),
          shortVersion.substring(SEQUENCE_SAMPLE_SIZE / 4, SEQUENCE_SAMPLE_SIZE / 2),
          shortVersion.substring(SEQUENCE_SAMPLE_SIZE / 2, 3 * SEQUENCE_SAMPLE_SIZE / 4),
          shortVersion.substring(3 * SEQUENCE_SAMPLE_SIZE / 4)
      }, "<br />");

      String constructFilename = String.format("Design_%s_seq%d.txt", docPrefix, i + 1);

      try (FileWriter writer = new FileWriter(new File(sequenceDestination, constructFilename))) {
        writer.write(design);
        writer.write("\n");
      }

      sequenceFilesAndSummaries.add(Triple.of(constructFilename, shortVersion, designs.get(i)));
    }

    return sequenceFilesAndSummaries;
  }

  private String makeSourceDocName(Reachable r) throws IOException {
    String inchiKey = r.getInchiKey();
    if (inchiKey == null) {
      LOGGER.error("Reachable %s does not have inchiKey", r.getInchi());
      return null;
    }
    return inchiKey;
  }

  private Pair<Object, String> buildPathModel(ReactionPath p, Reachable target, List<Triple<String, String, DNAOrgECNum>> designs) throws IOException {

    Map<String, Object> model = new HashMap<>();

    LinkedList<Object> pathwayItems = new LinkedList<>();
    List<String> chemicalNames = new ArrayList<>();
    model.put("pathwayitems", pathwayItems);

    // Pathway nodes start at the target and work back, so reverse them to make page order go from start to finish.
    for (Cascade.NodeInformation i : p.getPath()) {
      Map<String, Object> nodeModel = new HashMap<>();
      pathwayItems.push(nodeModel);

      nodeModel.put("isreaction", i.getIsReaction());
      if (i.getIsReaction()) {
        String label = i.getLabel();
        label = label.replaceAll("&+", " ");
        List<String> ecNums = Arrays.stream(label.split("\\s")).filter(s -> !s.isEmpty()).collect(Collectors.toList());
        nodeModel.put("ecnums", ecNums); // TODO: clean up this title to make mediawiki happy with it.
        List<String> organisms = new ArrayList<>(i.getOrganisms());
        Collections.sort(organisms);
        nodeModel.put("organisms", organisms);
      } else {
        Reachable r = loader.constructReachableFromId(i.getId());
        loader.upsert(r);
        if (r == null) {
          LOGGER.error("Unable to locate pathway chemical %d in reachables db", i.getId());
          nodeModel.put("name", "(unknown)");
        } else {
          nodeModel.put("link", r.getInchiKey());
          // TODO: we really need a way of picking a good name for each molecule.
          // If the page name is the InChI, we reduce it to the formula for the purpose of pathway visualisation.
          String name = r.getPageName().startsWith("InChI") ? r.getPageName().split("/")[1] : r.getPageName();
          nodeModel.put("name", name);
          chemicalNames.add(name);
          if (r.getStructureFilename() != null) {
            nodeModel.put("structureRendering", r.getStructureFilename());
          } else {
            LOGGER.warn("No structure filename for %s", r.getPageName());
          }
        }
      }
    }

    String pageTitle = StringUtils.join(chemicalNames, " <- ");
    model.put("pageTitle", pageTitle);

    List<Map<String, Object>> dna = new ArrayList<>();
    int i = 1;

    for (Triple<String, String, DNAOrgECNum> design : designs) {
      final int num = i; // Sigh, must be final to use in this initialization block.

      dna.add(new HashMap<String, Object>() {{
        put("file", design.getLeft());
        put("sample", design.getMiddle());
        put("num", Integer.valueOf(num).toString());
        put("org_ec", renderDNADesignMetadata(design.getRight()));
      }});
      i++;
    }
    if (dna.size() > 0) {
      model.put("dna", dna);
    }

    return Pair.of(model, pageTitle);
  }


  private List<String> renderDNADesignMetadata(DNAOrgECNum dnaOrgECNum) {
    List<Set<OrgAndEcnum>> setOfOrgAndEcnum = dnaOrgECNum.getListOfOrganismAndEcNums();
    return setOfOrgAndEcnum.stream().
        filter(Objects::nonNull).
        map(this::renderSetOfProteinDesignMetadata).
        collect(Collectors.toList());
  }

  private String renderSetOfProteinDesignMetadata(Set<OrgAndEcnum> orgAndEcnumSet) {
    return StringUtils.capitalize(StringUtils.join(orgAndEcnumSet.stream().
        filter(Objects::nonNull).
        map(this::renderProteinMetadata).
        collect(Collectors.toList()),
        ", ")
    );
  }

  private String renderProteinMetadata(OrgAndEcnum orgAndEcnum) {
    String proteinMetadata = orgAndEcnum.getEcnum() == null ?
        String.format("from organism %s", orgAndEcnum.getOrganism()) :
        String.format("from organism %s, enzyme from EC# %s", orgAndEcnum.getOrganism(), orgAndEcnum.getEcnum());
    return proteinMetadata;
  }


  public static class FreemarkerRendererFactory {
    public static FreemarkerRenderer build(Loader loader) throws IOException {
      FreemarkerRenderer renderer = new FreemarkerRenderer(loader);
      renderer.init();
      return renderer;
    }
  }
}
