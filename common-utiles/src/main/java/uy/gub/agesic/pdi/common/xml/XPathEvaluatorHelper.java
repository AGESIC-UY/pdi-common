package uy.gub.agesic.pdi.common.xml;

import jlibs.xml.DefaultNamespaceContext;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.StringReader;

import static org.joox.JOOX.$;

@Slf4j
public class XPathEvaluatorHelper {

    private final Document document;
    private final XPath evaluator;

    public XPathEvaluatorHelper(String xml) throws IOException, SAXException, ParserConfigurationException {

        //parse xml document
          document = $(new StringReader(xml)).document();
//        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//        factory.setNamespaceAware(true);
//        DocumentBuilder builder = factory.newDocumentBuilder();
//        document = builder.parse(xml);


        //read namespaces
        DefaultNamespaceContext nsContext = new DefaultNamespaceContext();
        XPath xPath1 = XPathFactory.newInstance().newXPath();
        try {
            NodeList namespaces = (NodeList) xPath1.compile("//namespace::*").evaluate(document, XPathConstants.NODESET);
            for (int i = 0, len = namespaces.getLength(); i < len; i++) {
                nsContext.declarePrefix(namespaces.item(i).getLocalName(), namespaces.item(i).getNodeValue());
            }
        } catch (XPathExpressionException e) {
            log.error("Error reading the namespaces of the xml document", e);
        }

        evaluator = XPathFactory.newInstance().newXPath();
        evaluator.setNamespaceContext(nsContext);

    }

    public Object evaluate(String expression, QName type) throws XPathExpressionException {
        return evaluator.compile(expression).evaluate(document, type);
    }
}
