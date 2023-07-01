package net.pointlessgames.libs.bps;

import java.io.IOException;
import java.util.Optional;

/*package-protected*/ class Helpers  {
    public static <T> IDeserializer<T> deserializer(int deserializerVersion, IDeserializationFunction<T> deserialization) {
        return new IDeserializer<T>() {
            @Override
            public int getVersion() {
                return deserializerVersion;
            }

            @Override
            public T deserialize(IDeserializationContext context) throws IOException {
                return deserialization.deserialize(context);
            }
        };
    }

    public static <T> ISerializer<T> multiVersionDeserializer(ISerializer<T> core, IDeserializer<T> deserializer) {
        return new MultiVersionDeserializer<T>(core, deserializer);
    }

    private static class MultiVersionDeserializer<T> implements IMultiVersionDeserializer<T>, ISerializer<T> {
        private final ISerializer<T> core;
        private final Optional<IMultiVersionDeserializer<T>> chained;
        private final IDeserializer<T> deserializer;

        @SuppressWarnings("unchecked")
        public MultiVersionDeserializer(ISerializer<T> core, IDeserializer<T> deserializer) {
            this.core = core;
            if(core instanceof IMultiVersionDeserializer) {
                IMultiVersionDeserializer<T> chainedCore = (IMultiVersionDeserializer<T>) core;
				this.chained = Optional.of(chainedCore);
				if(chainedCore.getDeserializer(deserializer.getVersion()) != null) {
					throw duplicateDeserializerException(core, deserializer);
				}
            } else {
                this.chained = Optional.empty();
				if(core.getVersion() == deserializer.getVersion()) {
					throw duplicateDeserializerException(core, deserializer);
				}
            }
            this.deserializer = deserializer;
        }
        
        private static <T> IllegalArgumentException duplicateDeserializerException(ISerializer<T> core, IDeserializer<T> deserializer) {
        	return new IllegalArgumentException("Serializer "+core+" already has deserializer of version "+deserializer.getVersion());
        }

        @Override
        public IDeserializer<T> getDeserializer(int version) {
            if(version == deserializer.getVersion()) {
                return deserializer;
            } else if(version == core.getVersion()) {
            	return core;
            } else if(chained.isPresent()) {
                return chained.get().getDeserializer(version);
            } else {
                return null;
            }
        }

        @Override
        public T deserialize(IDeserializationContext context) throws IOException {
            return core.deserialize(context);
        }

        @Override
        public int getVersion() {
            return core.getVersion();
        }

        @Override
        public void serialize(ISerializationContext context, T object) throws IOException {
            core.serialize(context, object);
        }
    }
}
