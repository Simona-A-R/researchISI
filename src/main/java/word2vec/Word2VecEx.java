package word2vec;

import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.text.sentenceiterator.BasicLineIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.deeplearning4j.util.SerializationUtils;
import org.nd4j.linalg.factory.Nd4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collection;

/**
 * Created by sroboiu on 30-May-17.
 */
public class Word2VecEx {
    private static Logger log = LoggerFactory.getLogger(Word2VecEx.class);

    public static void main(String[] args) throws Exception {
        //Word2VecEx vec = trainModel("pdfTest2-out.txt");
        Word2Vec vec = loadModel("GoogleNews-vectors-negative300.bin.gz");

        String testWord = "http";
        log.info("Closest 10 Words to {} :", testWord);
        Collection<String> lst = vec.wordsNearest(testWord, 10);
        System.out.println("10 Words closest to '" + testWord + "': " + lst);
    }

    public static Word2Vec trainModel(String filename) throws Exception {
        // Gets Path to Text file:
        String filePath = new File(filename).getAbsolutePath();

        log.info("Load & Vectorize Sentences....");
        // Strip white space before and after for each line
        SentenceIterator iter = new BasicLineIterator(filePath);
        //SentenceIterator iter = new UimaSentenceIterator(path,AnalysisEngineFactory.createEngine(AnalysisEngineFactory.createEngineDescription(TokenizerAnnotator.getDescription(), SentenceAnnotator.getDescription())));

        // Split on white spaces in the line to get words
        TokenizerFactory t = new DefaultTokenizerFactory();

        /*
        CommonPreprocessor will apply the following regex to each token: [\d\.:,"'\(\)\[\]|/?!;]+
        So, effectively all numbers, punctuation symbols and some special symbols are stripped off.
        Additionally it forces lower case for all tokens.
        */
        t.setTokenPreProcessor(new CommonPreprocessor());

        log.info("Building model....");
        org.deeplearning4j.models.word2vec.Word2Vec vec = new org.deeplearning4j.models.word2vec.Word2Vec.Builder()
                .sampling(1e-5)
                .batchSize(1000)
                .useAdaGrad(false)
                .learningRate(0.05)
                .minLearningRate(3e-2)
                .negativeSample(10)

                .minWordFrequency(5)
                .iterations(1)
                .layerSize(200)
                .seed(42)
                .windowSize(20)
                .iterate(iter)
                .tokenizerFactory(t)
                .build();

        log.info("Fitting Word2VecEx model....");
        vec.fit();

        log.info("Writing word vectors to text file....");
        WordVectorSerializer.writeWordVectors(vec, "pathToWriteTo.txt");
        log.info("Save vectors....");
        WordVectorSerializer.writeWord2VecModel(vec, "pathToSaveModel.txt");

        Nd4j.ENFORCE_NUMERICAL_STABILITY = true;
        SerializationUtils.saveObject(vec, new File("w2v_model.ser"));

        return vec;
    }

    public static Word2Vec loadModel(String filename) throws Exception {
        File gModel = new File(filename);
        Word2Vec vec = WordVectorSerializer.readWord2VecModel(gModel);

//        SentenceIterator iter = new BasicLineIterator(gModel.getAbsolutePath());
//        TokenizerFactory t = new DefaultTokenizerFactory();
//        t.setTokenPreProcessor(new CommonPreprocessor());
//        vec.setTokenizerFactory(t);
//        vec.setSentenceIterator(iter);
//        vec.getConfiguration().setUseHierarchicSoftmax(false);

//        vec.getConfiguration().setNegative(5.0);
//        vec.setElementsLearningAlgorithm(new CBOW<VocabWord>());

//        vec.fit();
        return vec;
    }
}
