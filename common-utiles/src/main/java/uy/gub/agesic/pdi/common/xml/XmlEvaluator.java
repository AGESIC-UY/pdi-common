package uy.gub.agesic.pdi.common.xml;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
public class XmlEvaluator {

    public static XmlNode evaluate(String xml) throws ParserConfigurationException, SAXException, IOException {


        SAXParserFactory spf = SAXParserFactory.newInstance();
        SAXParser sp = spf.newSAXParser();
        XMLReader xr = sp.getXMLReader();
        XPathContentHandler handler = new XPathContentHandler(xr);
        xr.setContentHandler(handler);
        xr.parse(new InputSource(new ByteArrayInputStream(xml.getBytes())));

        process(handler.getRoot(), xml);
        return handler.getRoot();

    }

    private static void process(XmlNode node, String xml) throws IOException, SAXException, ParserConfigurationException {

        XPathEvaluatorHelper helper = new XPathEvaluatorHelper(xml);

        Map<String, NodeCounter> repeat = new HashMap<>();

        List<XmlNode> nodes = node.getChildren().stream()
                .sorted(Comparator.comparing(XmlNode::getQName))
                .peek(n -> {
                    NodeCounter counter = repeat.get(n.getQName());
                    if (counter == null) {
                        counter = new NodeCounter();
                        repeat.put(n.getQName(), counter);
                    } else {
                        counter.increment();
                    }
                }).collect(Collectors.toList());


        for (XmlNode n : nodes) {
            if (n.getValue() != null) {
                n.addExpression(XmlNode.ExpressionType.COUNT, getCountExp(helper, "count(%s[%s='%s'])",
                        n.getParent().getXPath(), n.getQName(), n.getValue()));

            }
            if (n.getChildren().isEmpty()) {
                n.addExpression(XmlNode.ExpressionType.VALUE, getValueExp(helper, "%s/text()", n.getXPath()));
            }

            n.getAttrs().forEach(a -> a.addExpression(XmlNode.ExpressionType.VALUE, getValueExp(helper, "string(%s)", a.getXPath())));

            process(n, xml);
        }
    }

    private static XmlNode.ExpressionResult getValueExp(XPathEvaluatorHelper helper, String pattern, Object... args) {
        String valueExp = String.format(pattern, args);
        String valueValue = null;
        try {
            valueValue = helper.evaluate(valueExp, XPathConstants.STRING).toString();
        } catch (XPathExpressionException e) {
            log.warn("Cannot get the value for count expression {}", valueExp);
        }
        return XmlNode.ExpressionResult.builder()
                .expression(valueExp)
                .value(valueValue)
                .build();
    }

    private static XmlNode.ExpressionResult getCountExp(XPathEvaluatorHelper helper, String pattern, Object... args) {
        String countExp = String.format(pattern, args);
        String countValue = null;
        try {
            countValue = helper.evaluate(countExp, XPathConstants.NUMBER).toString();
        } catch (XPathExpressionException e) {
            log.warn("Cannot get the value for count expression {}", countExp);
        }
        return XmlNode.ExpressionResult.builder()
                .value(countValue)
                .expression(countExp)
                .build();
    }

    @Data
    private static class NodeCounter {
        private String name;
        private int counter = 1;
        private int occurrences = 1;

        int increment() {
            counter++;
            return occurrences++;
        }

        int decrement() {
            return --counter;
        }

        boolean repeat() {
            return occurrences > 1;
        }
    }

}
