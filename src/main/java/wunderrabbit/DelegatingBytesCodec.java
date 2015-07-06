package wunderrabbit;

import org.projectodd.wunderboss.codecs.BytesCodec;
import org.projectodd.wunderboss.codecs.Codec;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class DelegatingBytesCodec extends BytesCodec<Object> {

    public DelegatingBytesCodec(Codec delegate) {
        super(delegate.name(), delegate.contentType());
        this.delegate = delegate;
    }

    @Override
    public byte[] encode(Object data) {
        Object encoded = this.delegate.encode(data);

        if (encoded != null) {
            Class encodesTo = this.delegate.encodesTo();
            if (encodesTo == String.class) {
                encoded = ((String) encoded).getBytes();
            } else if (encodesTo == Object.class) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                try (ObjectOutputStream objStream = new ObjectOutputStream(out)){
                    objStream.writeObject(encoded);
                    encoded = out.toByteArray();
                } catch (IOException e) {
                    throw new RuntimeException("Failed to encode", e);
                }
            }
        }

        return (byte[])encoded;
    }

    @Override
    public Object decode(byte[] encoded) {
        Object data = encoded;
        if (data != null) {
            Class encodesTo = this.delegate.encodesTo();
            if (encodesTo == String.class) {
                data = new String(encoded);
            } else if (encodesTo == Object.class) {
                try (ObjectInputStream objStream = new ObjectInputStream(new ByteArrayInputStream(encoded))) {
                    data = objStream.readObject();
                } catch (ClassNotFoundException | IOException e) {
                    throw new RuntimeException("Failed to decode", e);
                }
            }
        }

        return this.delegate.decode(data);
    }

    private final Codec delegate;
}
