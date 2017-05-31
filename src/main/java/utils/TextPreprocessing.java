package utils;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import org.apache.commons.lang3.StringUtils;
import org.datavec.api.util.ClassPathResource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Created by sroboiu on 30-May-17.
 */
public class TextPreprocessing {

    private static Set<String> stopWords = new HashSet<>();
    private static Set<String> pronouns = new HashSet<>();
    private static HashMap<String, String> lemmas = new HashMap<>();
    private static List<String> punctuations = Arrays.asList(",", "~", "`", "!", "@", "#", "$", "%", "^", "&", "*", "(", ")", "-", "+", "_", "=", "{", "}", "[", "]", "|", "\\", ";", ":", "'", "\"", "<", ">", "/", ",", ".", "?");

    {
        //load words for pre-processing
        loadStopWords();
        loadPronouns();
        loadLemmas();
    }

    public void preprocessingText(String filename) {
        String filePath = filename;
        String outFilename = filename.substring(0, filename.length() - 4);
        outFilename = outFilename.concat("-out.txt");
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new FileWriter(outFilename));
            filePath = new ClassPathResource(filename).getFile().getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (Scanner scan = new Scanner(new File(filePath));) {
            scan.useDelimiter(Pattern.compile("[.?!]+\\s+"));
            while (scan.hasNext()) {
                String logicalLine = scan.next();
                String resultLine = new String();
                //stop words, pronouns and some symbols are skipped
                for (String word : logicalLine.split("[^'`\\.?!a-zA-Z0-9]")) {
                    if (StringUtils.isEmpty(word) || word.length() == 1)
                        continue;
                    if (stopWords.contains(word.toLowerCase()) || pronouns.contains(word.toLowerCase()) || punctuations.contains(word) || word.matches("\\s+[0-9]+.[0-9]*\\s+"))
                        continue;
                    //stemming and lematization
                    if (word.endsWith("sses")) {
                        word = word.substring(0, word.length() - 2);
                    } else if (word.endsWith("ies")) {
                        word = word.substring(0, word.length() - 2);
                    } else if (word.endsWith("ss")) {
                        ;
                    } else if (word.endsWith("s")) {
                        word = word.substring(0, word.length() - 1);
                    } else if (word.length() > 6 && word.endsWith("ement")) {
                        word = word.substring(0, word.length() - 5);
                    }
                    String processWord = lemmas.get(word.toLowerCase());
                    if (StringUtils.isEmpty(processWord)) {
                        resultLine = resultLine.concat(word.toLowerCase() + " ");
                    } else {
                        resultLine = resultLine.concat(processWord + " ");
                    }
                }
                if (StringUtils.isNotEmpty(resultLine))
                    pw.append(resultLine + ".\n");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            pw.close();
        }
    }

    public void preprocessingTextWithNlp(String filename) throws IOException {
        File inputFile = new File(filename);
        String text = com.google.common.io.Files.toString(inputFile, Charset.forName("UTF-8"));
        String outFilename = filename.substring(0, filename.length() - 4);
        outFilename = outFilename.concat("-out.txt");

        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new FileWriter(outFilename));
        } catch (IOException e) {
            e.printStackTrace();
        }

        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma, parse");

        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        Annotation document = new Annotation(text);
        pipeline.annotate(document);
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

        for (CoreMap sentence : sentences) {
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                String word = token.get(CoreAnnotations.TextAnnotation.class).toLowerCase();
                if (stopWords.contains(word) || pronouns.contains(word)
                        || punctuations.contains(word) || word.matches("[0-9]+.[0-9]*"))
                    continue;

                String processedWord = token.get(CoreAnnotations.LemmaAnnotation.class).toLowerCase();
                if (StringUtils.isEmpty(processedWord) || processedWord.length() == 1
                        || processedWord.startsWith("'"))
                    continue;

                pw.append(processedWord + " ");
                System.out.println(processedWord);
            }
            pw.append("\n");
        }
        pw.close();
    }

    public void loadStopWords() {
        try {
            String filePath = new ClassPathResource("en_stopwords.txt").getFile().getAbsolutePath();
            try (Stream<String> stream = Files.lines(Paths.get(filePath))) {
                stream.map(w -> w.trim()).forEach(stopWords::add);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadPronouns() {
        try {
            String filePath = new ClassPathResource("en_pronouns.txt").getFile().getAbsolutePath();
            try (Stream<String> stream = Files.lines(Paths.get(filePath))) {
                stream.filter(line -> !line.startsWith("[") && !line.isEmpty()).map(line -> line.trim()).forEach(pronouns::add);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadLemmas() {
        try {
            String filePath = new ClassPathResource("en_lemmas.txt").getFile().getAbsolutePath();
            try (Stream<String> stream = Files.lines(Paths.get(filePath))) {
                stream.forEach(line -> {
                            if(!line.isEmpty()) {
                                String[] m = line.trim().split("\t");
                                lemmas.put(m[1].toLowerCase(), m[0].toLowerCase());
                            }
                        });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        //String filename = new PdfReaderClass().pdfToTxt("pdfTest2.pdf");
        String filename = "src/main/resources/sample-content2.txt";
        new TextPreprocessing().preprocessingTextWithNlp(filename);
    }
}
