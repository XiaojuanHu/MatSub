/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2018 The Contributors of the realKD Project
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */
package de.unibonn.realkd.common;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

import com.google.common.io.Files;

import de.unibonn.realkd.RealKD;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;

/**
 * @author Mario Boley
 * 
 * @since 0.7.0
 * 
 * @version 0.7.0
 *
 */
public class KdonDocumentation {

	private static final Logger LOGGER = Logger.getLogger(KdonDocumentation.class.getName());

	private static final Configuration FREEMARKER_CONFIG = new Configuration(Configuration.VERSION_2_3_28);
	static {
		FREEMARKER_CONFIG.setClassForTemplateLoading(KdonDocumentation.class, ".");
		// cfg.setIncompatibleImprovements(new Version(2, 3, 20));
		FREEMARKER_CONFIG.setDefaultEncoding("UTF-8");
		FREEMARKER_CONFIG.setLocale(Locale.US);
		FREEMARKER_CONFIG.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
	}

	private static void writeDoc(Path path, Template template, KdonType spec) throws IOException {
		Map<String, Object> model = new HashMap<String, Object>();

		model.put("title", "KDON documentation - realKD " + RealKD.version());
		model.put("type", spec);

		File outputFile = path.resolve(spec.name + ".html").toFile();
		Files.createParentDirs(outputFile);

		try (Writer fileWriter = new FileWriter(outputFile)) {
			template.process(model, fileWriter);
		} catch (Exception ex) {
			LOGGER.severe(ex.getMessage());
		}
	}

	public static void main(String[] args) throws Exception {

		KdonType[] specs = KdonTypes.kdonTypes();

		Path path = FileSystems.getDefault().getPath("target", "kdondoc");
		Template template = FREEMARKER_CONFIG.getTemplate("kdondoc.ftl");

		for (KdonType spec : specs) {
			writeDoc(path, template, spec);
		}
	}

}
