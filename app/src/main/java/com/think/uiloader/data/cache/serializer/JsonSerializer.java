/**
 * Copyright (C) 2015 Fernando Cejas Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.think.uiloader.data.cache.serializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.inject.Inject;

/**
 * Class user as Serializer/Deserializer for user entities.
 */
public class JsonSerializer {

    private final Gson gson = new GsonBuilder().setLenient().create();

    @Inject
    public JsonSerializer() {
    }

    public <T> String serialize(T entity, Class<T> clazz) {
        String jsonString = gson.toJson(entity, clazz);
        return jsonString;
    }

    /**
     * Deserialize a json representation of an object.
     *
     * @param jsonString A json string to deserialize.
     */
    public <T> T deserialize(String jsonString, Class<T> clazz) {
        T userEntity = gson.fromJson(jsonString, clazz);
        return userEntity;
    }
}
