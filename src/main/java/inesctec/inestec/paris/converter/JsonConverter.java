/* Copyright (C) [2023-2026], INESC TEC
 *
 * dataspace-paris (DataSpace Participant information Service)
 * Implements a trusted directory of participants and connectors in accordance with the International Data Space Reference Architecture.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed 
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR 
 * CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations 
 * under the License.
 *
 * You can reach INESC TEC Technology Transfer Office (TTO) at tech-transfer@inesctec.pt, or
 * Campus da Faculdade de Engenharia da Universidade do Porto
 * Rua Dr. Roberto Frias
 * 4200-465 Porto
 * Portugal 
 */
package inesctec.inestec.paris.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.CollectionType;
import inesctec.inestec.paris.config.ConnectorDeserializer;
import inesctec.inestec.paris.model.Connector;
import inesctec.inestec.paris.util.MyJacksonObjectMapper;
import inesctec.inestec.paris.config.XMLGregorianCalendarDeserializer;
import org.apache.jena.base.Sys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.xml.datatype.XMLGregorianCalendar;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
@Scope("singleton")
public class JsonConverter {

    private final ObjectMapper objectMapper = new MyJacksonObjectMapper();

    @Autowired
    public JsonConverter() {
        super();
    }

    public JsonNode inputStream2Json(InputStream src) throws Exception {
        return objectMapper.readTree(src);
    }

    public <T> T json2Object(Class<T> type, JsonNode jsonNode) throws Exception {
        T obj = fromJson(jsonNode, type);
        if (obj != null)
            return obj;
        else
            throw new Exception("Invalid Json data, expecting data to populate " + type);
    }

    public <T> T json2ListOfObject(JsonNode jsonNode, CollectionType collectionType) throws Exception {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(XMLGregorianCalendar.class, new XMLGregorianCalendarDeserializer());
        objectMapper.registerModule(module);
        T obj=objectMapper.readValue(jsonNode.toString(), collectionType);
        if (obj != null)
            return obj;
        else
            throw new Exception("Invalid Json data, expecting data to populate ");
    }

    public JsonNode string2Json(String jsonString) throws Exception {
        return objectMapper.readTree(jsonString);
    }
    public JsonNode object2Json(final Object data) throws Exception {
        return objectMapper.valueToTree(data);
    }

    public <A> A fromJson(JsonNode json, Class<A> clazz) throws Exception {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(XMLGregorianCalendar.class, new XMLGregorianCalendarDeserializer());
        objectMapper.registerModule(module);
        return objectMapper.treeToValue(json, clazz);
    }
    public JsonNode file2Json(File jsonFile) throws Exception {
        return objectMapper.readTree(jsonFile);
    }

    public List<JsonNode> objectList2JsonList(final List<Object> objList) throws Exception {
        List<JsonNode> list = new ArrayList<JsonNode>();
        for (Object obj : objList)
            list.add(object2Json(obj));
        return list;
    }
}
