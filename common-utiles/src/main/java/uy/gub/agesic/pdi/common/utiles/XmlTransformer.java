package uy.gub.agesic.pdi.common.utiles;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class XmlTransformer {
    private static Map<String, Transformer> transformers = new HashMap<>();

    public static String domToString(Node node) throws Exception {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(node), new StreamResult(writer));
        return writer.getBuffer().toString();
    }

    public static String xslt(String xml, XSLTInformation xsltInformation, String encoding)
            throws UnsupportedEncodingException, TransformerFactoryConfigurationError, TransformerException {
        if (encoding == null) {
            encoding = "UTF-8";
        }
        ByteArrayInputStream in = new ByteArrayInputStream(xml.getBytes());
        Source source = new StreamSource(new InputStreamReader(in, encoding));
        if (!transformers.containsKey(xsltInformation.getPath())) {
            InputStream stream = XmlTransformer.class.getClassLoader().getResourceAsStream(xsltInformation.getPath());
            StreamSource transformSource = new StreamSource(stream);

            TransformerFactory transFact = TransformerFactory.newInstance();
            Transformer transformer = transFact.newTransformer(transformSource);
            transformer.setOutputProperty("indent", "no");
            transformer.setOutputProperty("encoding", encoding);
            transformers.put(xsltInformation.getPath(), transformer);
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        OutputStreamWriter resultWriter = new OutputStreamWriter(outputStream, encoding);
        StreamResult transformResult = new StreamResult(resultWriter);

        Transformer transformer = transformers.get(xsltInformation.getPath());
        synchronized (transformer) {
            resolveParameters(transformer, xsltInformation);
            transformer.transform(source, transformResult);
        }
        return new String(outputStream.toByteArray());
    }

    private static void resolveParameters(Transformer transformer, XSLTInformation xsltInformation) {
        transformer.clearParameters();
        Iterator<String> iter = xsltInformation.getParameters().keySet().iterator();
        while (iter.hasNext()) {
            String key = iter.next();
            String value = xsltInformation.getParameters().get(key);
            transformer.setParameter(key, value);
        }

    }
}
