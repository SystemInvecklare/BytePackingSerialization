package net.pointlessgames.libs.bps.extracontext;

import java.io.IOException;

import net.pointlessgames.libs.bps.ISerializationContext;
import net.pointlessgames.libs.bps.nested.InnerTypeRegistry;

public interface IDependentSerializer<T, C> extends IDependentDeserializer<T, C> {
	void serialize(ISerializationContext context, T object) throws IOException;
	
	default IDependentSerializer<T, C> with(int deserializerVersion, IDependentDeserializationFunction<T, C> deserialization) {
		return with(Helpers.dependentDeserializer(deserializerVersion, deserialization));
	}
	
	/**
	 * Note: When using {@link InnerTypeRegistry}, writing the following won't compile: 
	 * <pre>
	 *    private static final IDependentSerializer<IInnerType, SomeOuter> INNER_TYPE_REGISTRY = InnerTypeRegistry.configure(1, registry -> {
	 *    	...
	 *    }.with(InnerTypeRegistry.configue(registry -> {
	 *      ...
	 *    }));
	 * </pre>
	 * because types and stuff. In that case you can write the following:
	 * <pre>
	 *    private static final IDependentSerializer<IInnerType, SomeOuter> INNER_TYPE_REGISTRY_V1 = InnerTypeRegistry.configure(1, registry -> {
	 *    	...
	 *    });
	 *    private static final IDependentSerializer<IInnerType, SomeOuter> INNER_TYPE_REGISTRY_V0 = InnerTypeRegistry.configure(registry -> {
	 *    	...
	 *    });
	 *    private static final IDependentSerializer<IInnerType, SomeOuter> INNER_TYPE_REGISTRY = INNER_TYPE_REGISTRY_V1.with(INNER_TYPE_REGISTRY_V0);
	 * </pre>
	 * and that will (should) work. 
	 */
	default IDependentSerializer<T, C> with(IDependentDeserializer<T, C> deserializer) {
		return Helpers.multiVersionDependentDeserializer(this, deserializer);
	}
}
