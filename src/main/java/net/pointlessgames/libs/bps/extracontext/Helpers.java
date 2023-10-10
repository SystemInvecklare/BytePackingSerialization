package net.pointlessgames.libs.bps.extracontext;

import java.io.IOException;
import java.util.Optional;

import net.pointlessgames.libs.bps.IDeserializationContext;
import net.pointlessgames.libs.bps.ISerializationContext;

/*package-protected*/ class Helpers  {
    public static <T, C> IDependentDeserializer<T, C> dependentDeserializer(int deserializerVersion, IDependentDeserializationFunction<T, C> deserialization) {
        return new IDependentDeserializer<T, C>() {
            @Override
            public int getVersion() {
                return deserializerVersion;
            }

			@Override
			public T deserialize(IDeserializationContext context, C extraContext) throws IOException {
				return deserialization.deserialize(context, extraContext);
			}
        };
    }

    public static <T, C> IDependentSerializer<T, C> multiVersionDependentDeserializer(IDependentSerializer<T, C> core, IDependentDeserializer<T, C> deserializer) {
        return new MultiVersionDependentDeserializer<T, C>(core, deserializer);
    }

    private static class MultiVersionDependentDeserializer<T, C> implements IMultiVersionDependentDeserializer<T, C>, IDependentSerializer<T, C> {
        private final IDependentSerializer<T, C> core;
        private final Optional<IMultiVersionDependentDeserializer<T, C>> chained;
        private final IDependentDeserializer<T, C> deserializer;

        @SuppressWarnings("unchecked")
        public MultiVersionDependentDeserializer(IDependentSerializer<T, C> core, IDependentDeserializer<T, C> deserializer) {
            this.core = core;
            if(core instanceof IMultiVersionDependentDeserializer) {
            	IMultiVersionDependentDeserializer<T, C> chainedCore = (IMultiVersionDependentDeserializer<T, C>) core;
				this.chained = Optional.of(chainedCore);
				if(chainedCore.getDependentDeserializer(deserializer.getVersion()) != null) {
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
        
        private static <T, C> IllegalArgumentException duplicateDeserializerException(IDependentSerializer<T, C> core, IDependentDeserializer<T, C> deserializer) {
        	return new IllegalArgumentException("Dependent serializer "+core+" already has dependent deserializer of version "+deserializer.getVersion());
        }

        @Override
        public IDependentDeserializer<T, C> getDependentDeserializer(int version) {
            if(version == deserializer.getVersion()) {
                return deserializer;
            } else if(version == core.getVersion()) {
            	return core;
            } else if(chained.isPresent()) {
                return chained.get().getDependentDeserializer(version);
            } else {
                return null;
            }
        }
        
        @Override
        public T deserialize(IDeserializationContext context, C extraContext) throws IOException {
        	return core.deserialize(context, extraContext);
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
