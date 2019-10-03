package com.pdf.pdf;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@SpringBootApplication
@EnableAutoConfiguration
public class PdfApplication {
	private static final Logger LOGGER = LogManager.getLogger(PdfApplication.class);

	private static final String folderPath="\root";
	
	public static void main(String[] args) throws InvalidPasswordException, IOException {
		SpringApplication.run(PdfApplication.class, args);
		
		PdfApplication pdf = new PdfApplication();
		pdf.calculateAmount();
	}
	
	private void calculateAmount(){
		File folder = new File(folderPath);
		Float totalPayable = 0.0f;
		String[] files = folder.list();
		int count=0;
		if(files!=null){
		for (String file : files) {
			if ("pdf".equals(Arrays.stream(file.split("\\.")).reduce((a,b) -> b).orElse(null))) {
				LOGGER.debug("file Name : {}",file);
				PDDocument document=null;
				try {
					document = PDDocument.load(new File(folderPath + "\\" + file));
					document.getClass();

					if (!document.isEncrypted()) {

						PDFTextStripperByArea stripper = new PDFTextStripperByArea();
						stripper.setSortByPosition(true);

						PDFTextStripper tStripper = new PDFTextStripper();

						String pdfFileInText = tStripper.getText(document);

						String [] lines = pdfFileInText.split("\\r?\\n");
						for (String line : lines) {
							if (line.contains("Total Payable")) {
								LOGGER.debug("pdf line : {}",line);
								String payable = line.substring(line.indexOf('₹') + 1);
								LOGGER.debug("Payable amount : {}",payable);
								totalPayable += Float.valueOf(payable);
								count++;
							}
						}

					}

				} catch (Exception e) {
					LOGGER.error(e);
				}finally{
					if(document!=null)
						try {
							document.close();
						} catch (IOException e) {
							LOGGER.error(e);
						}
				}
			}
		}
		}
		LOGGER.info("TotalValue : {}" , totalPayable);
		LOGGER.info("Count : {}" , count);
	}
}
