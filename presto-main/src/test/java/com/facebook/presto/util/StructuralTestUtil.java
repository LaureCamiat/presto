/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.presto.util;

import com.facebook.presto.block.BlockEncodingManager;
import com.facebook.presto.metadata.FunctionRegistry;
import com.facebook.presto.spi.block.Block;
import com.facebook.presto.spi.block.BlockBuilder;
import com.facebook.presto.spi.block.BlockBuilderStatus;
import com.facebook.presto.spi.block.InterleavedBlockBuilder;
import com.facebook.presto.spi.type.StandardTypes;
import com.facebook.presto.spi.type.Type;
import com.facebook.presto.spi.type.TypeManager;
import com.facebook.presto.spi.type.TypeSignatureParameter;
import com.facebook.presto.sql.analyzer.FeaturesConfig;
import com.facebook.presto.type.MapType;
import com.facebook.presto.type.TypeRegistry;
import com.google.common.collect.ImmutableList;

import java.util.Map;

import static com.facebook.presto.type.TypeJsonUtils.appendToBlockBuilder;

public final class StructuralTestUtil
{
    private static final TypeManager TYPE_MANAGER = new TypeRegistry();
    static {
        // associate TYPE_MANAGER with a function registry
        new FunctionRegistry(TYPE_MANAGER, new BlockEncodingManager(TYPE_MANAGER), new FeaturesConfig());
    }

    private StructuralTestUtil() {}

    public static Block arrayBlockOf(Type elementType, Object... values)
    {
        BlockBuilder blockBuilder = elementType.createBlockBuilder(new BlockBuilderStatus(), values.length);
        for (Object value : values) {
            appendToBlockBuilder(elementType, value, blockBuilder);
        }
        return blockBuilder.build();
    }

    public static Block mapBlockOf(Type keyType, Type valueType, Map<?, ?> value)
    {
        BlockBuilder blockBuilder = new InterleavedBlockBuilder(ImmutableList.of(keyType, valueType), new BlockBuilderStatus(), value.size() * 2);
        for (Map.Entry<?, ?> entry : value.entrySet()) {
            appendToBlockBuilder(keyType, entry.getKey(), blockBuilder);
            appendToBlockBuilder(valueType, entry.getValue(), blockBuilder);
        }
        return blockBuilder.build();
    }

    public static MapType mapType(Type keyType, Type valueType)
    {
        return (MapType) TYPE_MANAGER.getParameterizedType(StandardTypes.MAP, ImmutableList.of(
                TypeSignatureParameter.of(keyType.getTypeSignature()),
                TypeSignatureParameter.of(valueType.getTypeSignature())));
    }
}
