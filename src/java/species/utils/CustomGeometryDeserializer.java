package species.utils;

import com.bedatadriven.jackson.datatype.jts.serialization.GeometryDeserializer;

import com.bedatadriven.jackson.datatype.jts.parsers.GeometryParser;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.vividsolutions.jts.geom.Geometry;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.util.JsonParserSequence;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import java.io.IOException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Created by mihaildoronin on 11/11/15.
 */
public class CustomGeometryDeserializer<T extends Geometry> extends JsonDeserializer<T> {

    private GeometryParser<T> geometryParser;

    public CustomGeometryDeserializer(GeometryParser<T> geometryParser) {
        this.geometryParser = geometryParser;
    }

    @Override
    public T deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        System.out.println("CustomGeometryDeSerializer deserialize");
        //Object typeId = jp.getTypeId();
/*        String typeId = "";//_locateTypeId(jsonParser, deserializationContext);

        System.out.println(typeId);
        TokenBuffer tb = new TokenBuffer(null, false);
        tb.writeStartObject(); // recreate START_OBJECT
        tb.writeFieldName("GeoJson");
        tb.writeString(typeId);

        jsonParser.clearCurrentToken();
        jsonParser = JsonParserSequence.createFlattened(tb.asParser(jsonParser), jsonParser);
        jsonParser.nextToken();
*/
        ObjectCodec oc = jsonParser.getCodec();
        JsonNode root = oc.readTree(jsonParser);
        System.out.println(root);
        System.out.println(geometryParser);
System.out.println(geometryParser.geometryFromJson(root));
System.out.println(geometryParser.geometryFromJson(root).getClass());

        return geometryParser.geometryFromJson(root);
    }


    @Override
    public Object deserializeWithType(final JsonParser parser, final DeserializationContext context,
            final TypeDeserializer typeDeserializer) throws IOException {

        System.out.println("CustomGeometryDeSerializer deserializeWithType");
        System.out.println(typeDeserializer);
        // effectively assuming no type information at all
        //return deserialize(parser, context);
        return typeDeserializer.deserializeTypedFromAny(parser, context);

    }

}
