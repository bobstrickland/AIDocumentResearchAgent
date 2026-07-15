package claudeagent.documentImport;

import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class PdfFileHandler {

	
	
	public static String extractText(byte[] pdfBytes) {
		String text = null;
		try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            text = pdfStripper.getText(document);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return text;
	}
}
