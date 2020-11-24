package uy.gub.agesic.pdi.common.utiles;

import jlibs.xml.DefaultNamespaceContext;
import jlibs.xml.sax.dog.NodeItem;
import jlibs.xml.sax.dog.XMLDog;
import jlibs.xml.sax.dog.XPathResults;
import jlibs.xml.sax.dog.expr.Expression;
import org.jaxen.saxpath.SAXPathException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpressionException;
import java.io.StringReader;
import java.util.*;

public class XPathXmlDogUtil {

    private static Logger log = LoggerFactory.getLogger(XPathXmlDogUtil.class);

    public static List<Object> executeMultipleXPath(String xml, List<String> xpaths, Map<String, String> namespaces) throws SAXPathException, XPathException {
        List<Object> returnList = new LinkedList<>();
        try {
            DefaultNamespaceContext nsContext = new DefaultNamespaceContext();
            for (Map.Entry<String, String> entry : namespaces.entrySet()) {
                nsContext.declarePrefix(entry.getKey(), entry.getValue());
            }
            XMLDog dog = new XMLDog(nsContext, null, null);

            Map<String, Expression> exprsCache = new HashMap<>();
            for (String xpath : xpaths) {
                try {
                    Expression xpathExpr = dog.addXPath(xpath);
                    exprsCache.put(xpath, xpathExpr);
                } catch (SAXPathException e) {
                    log.error("Error compiling xpath expression", e);
                    throw e;
                }
            }
            XPathResults results = dog.sniff(getInputSource(xml));
            returnList = returnData(results, xpaths, exprsCache);
        } catch (XPathExpressionException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (XPathException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
        return returnList;
    }

    private static List<Object> returnData(XPathResults results, List<String> xpaths, Map<String, Expression> exprsCache) {
        List<Object> returnList = new LinkedList<>();
        for (String xpath : xpaths) {
            Expression expr = exprsCache.get(xpath);
            Object result = results.getResult(expr);
            if ((result instanceof Collection)) {
                Collection<NodeItem> items = (Collection<NodeItem>) result;
                if (items.size() <= 1) {
                    result = null;
                    for (NodeItem nodeItem : items) {
                        result = nodeItem.value;
                    }
                    returnList.add(result);
                } else {
                    List<String> itemResult = new ArrayList<>();
                    for (NodeItem nodeItem : items) {
                        returnList.add(Boolean.valueOf(itemResult.add(nodeItem.value)));
                    }
                }
            } else {
                returnList.add(result);
            }
        }
        return returnList;
    }

    public static InputSource getInputSource(String xml) {
        return new InputSource(new StringReader((String) xml));
    }
}
