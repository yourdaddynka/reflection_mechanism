package edu.school21.htmlgenerator;

import edu.school21.service.HtmlForm;
import edu.school21.service.HtmlInput;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Set;


@SupportedAnnotationTypes("edu.school21.service.HtmlForm")
public class HtmlProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.size() == 0) {
            return false;
        }
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(HtmlForm.class);
        for (Element element : elements) {
            HtmlForm htmlForm = element.getAnnotation(HtmlForm.class);
            String fileName = htmlForm.fileName();
            String method = htmlForm.method();
            String action = htmlForm.action();
            StringBuilder htmlFile = new StringBuilder();
            htmlFile.append("<form action = \"").append(action).append("\" ").append("method = \"").append(method).append("\">\n");
            List<? extends Element> list = element.getEnclosedElements();
            for (Element element2 : list) {
                HtmlInput htmlInput = element2.getAnnotation(HtmlInput.class);
                if (htmlInput != null) {
                    String name = htmlInput.name();
                    String placeholder = htmlInput.placeholder();
                    String type = htmlInput.type();
                    htmlFile.append("\t<input type = \"").append(type).append("\" name = \"").append(name).append("\" placeholder = \"").append(placeholder).append("\">\n");
                }
            }
            htmlFile.append("\t<input type = \"submit\" value = \"Send\">\n").append("</form>\n");
            fileCreate(fileName, htmlFile.toString());
        }
        return true;
    }

    private void fileCreate(String filename, String htmlFile) {
        try (FileWriter fileWriter = new FileWriter(filename)) {
            fileWriter.write(htmlFile);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}