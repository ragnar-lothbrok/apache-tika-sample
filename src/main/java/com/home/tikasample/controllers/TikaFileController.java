package com.home.tikasample.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AbstractParser;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.image.ImageParser;
import org.apache.tika.parser.jpeg.JpegParser;
import org.apache.tika.sax.BodyContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
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

	public static Map<String, AbstractParser> parserObjectMap = new HashMap<String, AbstractParser>();

	public TikaFileController() {
		try {
			printHiearchy(AbstractParser.class.getPackage().getName(), parserObjectMap);
			logger.info("Map : >> " + parserObjectMap);
		} catch (Exception e) {
			logger.error("Exception occured {}", e);
		}
	}

	@RequestMapping(
		method = RequestMethod.POST)
	public Map<String, Object> acceptAndReturnData(@RequestParam("uploadType") String uploadType, @RequestParam("uploadfile") MultipartFile[] files)
			throws IOException {
		Map<String, Object> metadataInformation = new HashMap<String, Object>();
		Metadata metadata = new Metadata();
		ContentHandler handler = new BodyContentHandler(100 * 1024 * 1024);
		Parser parser = null;
		ParseContext context = new ParseContext();
		InputStream stream  = files[0].getInputStream();
		metadataInformation.put("Original Name", files[0].getOriginalFilename());
		if ("other".equalsIgnoreCase(uploadType)) {
			parser = new AutoDetectParser();
		}else{
			parser = new ImageParser();
		}
		try {
			parser.parse(stream, handler, metadata, context);
		} catch (TikaException e) {
			logger.error("Exception occured {}", e);
		} catch (SAXException e) {
			logger.error("Exception occured {}", e);
		} catch (Exception e) {
			logger.error("Exception occured {}", e);
		} finally {
			stream.close(); // close the stream
		}
		return getInformation(metadata, metadataInformation, handler);
	}

	public Map<String, Object> getInformation(Metadata metadata, Map<String, Object> metadataInformation, ContentHandler handler) {
		for (String key : metadata.names()) {
			metadataInformation.put(key, metadata.get(key));
		}
		metadataInformation.put("file-content", handler.toString());
		return metadataInformation;
	}

	@SuppressWarnings("rawtypes")
	public void printHiearchy(String packageName, Map<String, AbstractParser> metadataInformation)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(true);
		provider.addIncludeFilter(new AssignableTypeFilter(AbstractParser.class));

		// scan in org.example.package
		Set<BeanDefinition> components = provider.findCandidateComponents(packageName);
		for (BeanDefinition component : components) {
			Class cls = Class.forName(component.getBeanClassName());
			if (!Modifier.isAbstract(cls.getModifiers()) && !Modifier.isInterface(cls.getModifiers())) {
				for (Constructor<?> constructor : cls.getConstructors()) {
					if (constructor.getParameterCount() == 0) {
						metadataInformation.put(cls.getSimpleName(), (AbstractParser) cls.newInstance());
					}
				}
			}
		}
	}

}
