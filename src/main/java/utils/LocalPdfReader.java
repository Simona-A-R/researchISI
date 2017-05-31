package utils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.pdfbox.util.TextPosition;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

/**
 * Created by sroboiu on 30-May-17.
 */
public class LocalPdfReader {

    public String pdfToTxt(String filename) {
        PDDocument pd = null;
        BufferedWriter wr = null;
        String outputFilename = filename.split(".pdf")[0] + ".txt";
        try {
            File input = new File(filename);
            File output = new File(outputFilename);
            System.out.println("Output filename:" + output.getName());

            pd = PDDocument.load(input);
            System.out.println("Encrypted:" + pd.isEncrypted());
            PDFTextStripper stripper = new PDFTextStripper()
            {
                @Override
                protected void processTextPosition(TextPosition text)
                {
                    String character = text.getCharacter();
                    if (character != null && character.trim().length() != 0)
                        super.processTextPosition(text);
                }
            };
            stripper.setSortByPosition(true);
            stripper.setLineSeparator(" ");
            stripper.setPageSeparator(" ");
            wr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output)));
            wr.write(stripper.getText(pd).replace("\r\n", " ").replaceAll("[\\.!?]+\\s+", ".\n"));
            //stripper.writeText(pd, wr);
            if (pd != null) {
                pd.close();
            }
            wr.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return outputFilename;
    }

    public static void main(String[] args) {
        new LocalPdfReader().pdfToTxt("pdfTest2.pdf");
    }
}
