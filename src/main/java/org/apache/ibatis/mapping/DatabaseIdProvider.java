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

import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

/**
 * Hibernate中支持屏蔽不同数据库实现中sql的差异，而mybaits不支持
 * 但mybaits提供了另一种机制，允许在mapper映射文件中指定databaseId，运行时，会根据当前的具体的数据库执行响应的sql
 *
 * 配置文件中节点<databaseIdProvider> 提供了所有支持的dataBaseId
 * Should return an id to identify the type of this database.
 * That id can be used later on to build different queries for each database type
 * This mechanism enables supporting multiple vendors or versions
 *
 * @author Eduardo Macarron
 */
public interface DatabaseIdProvider {

    void setProperties(Properties p);

    String getDatabaseId(DataSource dataSource) throws SQLException;
}
