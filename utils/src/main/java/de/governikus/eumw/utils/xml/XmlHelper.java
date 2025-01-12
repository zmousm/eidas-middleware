/*
 * Copyright (c) 2020 Governikus KG. Licensed under the EUPL, Version 1.2 or as soon they will be approved by the
 * European Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in compliance
 * with the Licence. You may obtain a copy of the Licence at: http://joinup.ec.europa.eu/software/page/eupl Unless
 * required by applicable law or agreed to in writing, software distributed under the Licence is distributed on an
 * "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the Licence for the
 * specific language governing permissions and limitations under the Licence.
 */

package de.governikus.eumw.utils.xml;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.lang3.StringUtils;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import lombok.extern.slf4j.Slf4j;


/**
 * this class should provide utility helper methods for XML
 */
@Slf4j
public final class XmlHelper
{

  private final static JAXBContext JAXB_CONTEXT;

  static
  {
    try
    {
      JAXB_CONTEXT = JAXBContext.newInstance("de.governikus.eumw.config");
    }
    catch (JAXBException e)
    {
      throw new RuntimeException("Cannot create JAXBContext for the EUMW config", e);
    }
  }

  /**
   * utility class constructor
   */
  private XmlHelper()
  {
    super();
  }

  /**
   * this method will marshal any element that is annotated with {@link javax.xml.bind.annotation.XmlRootElement}
   *
   * @param object the annotated xml-object
   * @return the string representation of the xml-object
   */
  public static String marshalObject(Object object)
  {
    if (log.isTraceEnabled())
    {
      log.trace("translating java instance of type '{}' to a xml-string.", object.getClass());
    }
    try
    {
      JAXBContext jc = JAXBContext.newInstance(object.getClass());
      Marshaller marshaller = jc.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
      marshaller.setProperty(Marshaller.JAXB_ENCODING, StandardCharsets.UTF_8.name());
      final StringWriter w = new StringWriter();
      marshaller.marshal(object, w);
      return w.toString();
    }
    catch (JAXBException e)
    {
      throw new XmlException("error while marshalling class " + object.getClass().getName(), e);
    }
  }

  /**
   * this method will be used to unmarshal a XML string into a POJO
   *
   * @param xml the xml string that should be unmarshalled
   * @param clazz the destination class that will be able to hold the data of the XML structure
   * @return the unmarshalled POJO
   */
  public static <T> T unmarshal(String xml, Class<T> clazz)
  {
    try
    {
      JAXBContext jaxbContext;
      // Use the static instance of the JAXBContext for the frequently unmarshalled middleware configuration
      if ("de.governikus.eumw.config".equals(clazz.getPackageName()))
      {
        jaxbContext = JAXB_CONTEXT;
      }
      else
      {
        jaxbContext = JAXBContext.newInstance(clazz);
      }
      Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
      return (T)jaxbUnmarshaller.unmarshal(new StringReader(xml));
    }
    catch (Exception ex)
    {
      throw new XmlException("could not parse given XML \n'" + xml + "'\n", ex);
    }
  }

  /**
   * this method will take some input xml and will check it against its schema and throws an exception if the
   * requirements are not met
   *
   * @param xml the XML that should be checked against its schema
   * @param schemaLocation the schema that should be used to check the XML @throws
   * @throws XmlException if the validation has failed for any reason
   */
  public static void validateWithSchema(String xml, URL schemaLocation) throws XmlException
  {
    if (schemaLocation == null)
    {
      throw new XmlException("No schema found: location is null");
    }
    else if (StringUtils.isBlank(schemaLocation.getFile()))
    {
      throw new XmlException(String.format("Schema location '%s' does not exist...", schemaLocation));
    }
    try
    {
      // build the schema
      SchemaFactory factory = getSchemaFactory();
      Schema schema = factory.newSchema(schemaLocation);
      Validator validator = schema.newValidator();
      validator.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
      validator.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

      // create a source from a string
      Source source = new StreamSource(new StringReader(xml));

      validator.validate(source);
    }
    catch (IOException | SAXException e)
    {
      throw new XmlException(e.getMessage(), e);
    }
  }

  /**
   * Returns an initialized {@link SchemaFactory} ready to use, configured with security features preventing several XXE
   * attacks.
   *
   * @return the schema factory
   */
  public static SchemaFactory getSchemaFactory() throws SAXNotRecognizedException, SAXNotSupportedException
  {
    SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI,
                                                 "com.sun.org.apache.xerces.internal.jaxp.validation.XMLSchemaFactory",
                                                 null);
    sf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    sf.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
    sf.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
    return sf;
  }
}
