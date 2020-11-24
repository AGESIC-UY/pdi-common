package uy.gub.agesic.pdi.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit4.SpringRunner;
import org.xml.sax.SAXException;
import uy.gub.agesic.pdi.common.xml.XPathEvaluatorHelper;
import uy.gub.agesic.pdi.common.xml.XmlEvaluator;
import uy.gub.agesic.pdi.common.xml.XmlNode;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

@RunWith(SpringRunner.class)
public class XmlEvaluatorTest {

    @Value("classpath:xml-without-namespaces.xml")
    Resource resource;

    @Value("classpath:register_001.xml")
    Resource example1;

    @Test
    public void simple() throws IOException, SAXException, ParserConfigurationException {


        StringBuilder xml = new StringBuilder();
        String line = null;

        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            while ((line = bufferedReader.readLine()) != null) {
                xml.append(line);
            }
        }

        XmlNode xmlNode = XmlEvaluator.evaluate(xml.toString());

        Assert.assertEquals(4, xmlNode.getChildren().size());
        Assert.assertEquals(1, xmlNode.getChildren().get(0).getAttrs().size());
        Assert.assertEquals(4, xmlNode.getChildren().get(0).getChildren().size());
        Assert.assertEquals(8, xmlNode.getChildren().get(2).getChildren().size());
        Assert.assertEquals("count(//bookstore/book[@category='web'][author='James Linn'])",
                xmlNode.getChildren().get(2).getChildren().get(4).getExpressions().get(XmlNode.ExpressionType.COUNT).getExpression());

        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writeValueAsString(xmlNode));

        List<String> xPaths = xmlNode.xPathTraversal();
        XPathEvaluatorHelper evaluator = new XPathEvaluatorHelper(xml.toString());

        evaluate(xPaths, evaluator);
    }

    private void evaluate(List<String> xPaths, XPathEvaluatorHelper evaluator) {
        xPaths.forEach(s -> {

            try {
                Object evaluate = evaluator.evaluate(s, XPathConstants.STRING);
                System.out.println(s + ", found: " + evaluate.toString());
                Assert.assertNotNull(evaluate);
            } catch (XPathExpressionException e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    public void withNamespaces() throws IOException, SAXException, ParserConfigurationException {


        StringBuilder xml = new StringBuilder();
        String line = null;

        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(example1.getInputStream()))) {
            while ((line = bufferedReader.readLine()) != null) {
                xml.append(line);
            }
        }


        XmlNode xmlNode = XmlEvaluator.evaluate(xml.toString());

        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writeValueAsString(xmlNode));

        List<String> xPaths = xmlNode.xPathTraversal();
        XPathEvaluatorHelper evaluator = new XPathEvaluatorHelper(xml.toString());
        evaluate(xPaths,evaluator);

    }
}
