/**
 * Copyright 2009-2015 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ibatis.mapping;

/**
 * 该接口表示映射文件或者注解中定义的SQL语句
 * Represents the content of a mapped statement read from an XML file or an annotation. 
 * It creates the SQL that will be passed to the database out of the input parameter received from the user.
 *
 * @author Clinton Begin
 */
public interface SqlSource {

    // 根据传入的参数，返回实际可执行的SQL
    BoundSql getBoundSql(Object parameterObject);

}
