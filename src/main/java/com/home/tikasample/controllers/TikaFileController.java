package com.home.tikasample.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

@RestController
@RequestMapping(
	value = "/file")
public class TikaFileController {

	final static Logger logger = LoggerFactory.getLogger(TikaFileController.class);

	@RequestMapping(
		method = RequestMethod.POST)
	public Map<String, Object> acceptAndReturnData(@RequestParam("uploadfile") MultipartFile[] files) throws IOException {
		Metadata metadata = new Metadata();
		ContentHandler handler = new BodyContentHandler(100 * 1024 * 1024);
		ParseContext context = new ParseContext();
		Parser parser = new AutoDetectParser();
		InputStream stream = files[0].getInputStream();
		try {
			parser.parse(stream, handler, metadata, context);
		} catch (TikaException e) {
			logger.error("Exception occured {}", e);
		} catch (SAXException e) {
			logger.error("Exception occured {}", e);
		} catch (IOException e) {
			logger.error("Exception occured {}", e);
		} finally {
			stream.close(); // close the stream
		}
		return getInformation(metadata);
	}

	public Map<String, Object> getInformation(Metadata metadata) {
		Map<String, Object> metadataInformation = new HashMap<String, Object>();
		for (String key : metadata.names()) {
			metadataInformation.put(key, metadata.get(key));
		}
		return metadataInformation;
	}

}
