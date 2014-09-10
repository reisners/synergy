package de.syngenio.test.speechrecognition;

import java.io.FileInputStream;
import java.io.IOException;

import org.junit.Test;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.api.StreamSpeechRecognizer;

public class TestStreamSpeechRecognition
{

    @Test
    public void test() throws IOException
    {
        Configuration configuration = new Configuration();

        // Set path to acoustic model.
//        configuration.setAcousticModelPath("resource:/edu/cmu/sphinx/models/acoustic/wsj_8kHz");
        configuration.setAcousticModelPath("resource:/voxforge.cd_cont_3000");
        // Set path to dictionary.
//        configuration.setDictionaryPath("resource:/edu/cmu/sphinx/models/acoustic/wsj_8kHz/dict/cmudict.0.6d");
        configuration.setDictionaryPath("resource:/voxforge.dic");

        // Set language model.
        configuration.setLanguageModelPath("resource:/voxforge.lm.dmp");

        StreamSpeechRecognizer recognizer = new StreamSpeechRecognizer(configuration);
        recognizer.startRecognition(new FileInputStream("wav/ziffernfolgen.wav"));
        SpeechResult result;
        while ((result = recognizer.getResult()) != null)
        {
            System.out.println(result.getHypothesis());
        }
        recognizer.stopRecognition();
    }

}
